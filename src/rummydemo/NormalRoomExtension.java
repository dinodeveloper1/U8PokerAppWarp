/**
 * 
 */
package rummydemo;

import static pokercard.Suit.*;
import static pokercard.Rank.*;
import pokercard.*;

import com.shephertz.app42.server.domain.Room;
import com.shephertz.app42.server.domain.User;
import com.shephertz.app42.server.idomain.BaseRoomAdaptor;
import com.shephertz.app42.server.idomain.HandlingResult;
import com.shephertz.app42.server.idomain.IRoom;
import com.shephertz.app42.server.idomain.ITurnBasedRoom;
import com.shephertz.app42.server.idomain.IUser;
import com.shephertz.app42.server.idomain.IZone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.smartcardio.CardChannel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONWriter;

import pokercard.Card;
import pokercard.Card.*;

/**

 *
 */
@SuppressWarnings("unused")
public class NormalRoomExtension extends BaseRoomAdaptor
{
	/*** iVars Declaration ***/
    private IZone izone;
    private IRoom gameRoom;
    
    Timer passTimer; TimerTask passTask;
    String lastWinner;
    
    ArrayList<IUser> userList = new ArrayList<IUser>();    
    ArrayList<IUser> waitingUsers = new ArrayList<IUser>();
    ArrayList<IUser> viewersList = new ArrayList<IUser>();
    
    ArrayList<PlayerDetail> arrPlayerDetails = new ArrayList<PlayerDetail>();
    ArrayList<PlayerDetail> arrWaitingPlayerDetails = new ArrayList<PlayerDetail>();
    ArrayList<PlayerDetail> arrViewers = new ArrayList<PlayerDetail>();
    
    Deck cardDeck = new Deck();
    boolean schedualStarted = false; 
    private byte GAME_STATUS;
    private int defaultRaiseAmount = 0; private int gameRound = 0;
    
    private final int MSecs = 1000;
    private boolean allBettingDone = false;
    
    private final int MAX_NO_OF_CARDS = 8;// for each user
    
    ArrayList<Integer> centerTableCard = new ArrayList<Integer>();
    ArrayList<HashMap<String, Object>> arrLastCardDetails = new ArrayList<HashMap<String, Object>>();
    
    private int currentPlayerIndex,playStartIndex,passcounts;
    String playStartUsername = "";
    String currentPlayuserName = "";
    int nextGamePlayTurn = -1;    
    
    long centerTableAmount;
    long callAmountDefault;
    
    /*** End iVars Declaration ***/
    
    /******** Methods  *******/
    public NormalRoomExtension(IZone izone, IRoom room)
    {
    	this.izone = izone;
    	this.gameRoom = room;
    	GAME_STATUS = CardsConstants.STOPPED;
    	nextGamePlayTurn = 0;
    	Object value = this.gameRoom.getProperties().get(CardsConstants.kRoomDefaultAmount);
    	if (this.gameRoom.getProperties().containsKey(CardsConstants.kRoomDefaultAmount))
    	{
    		Integer intvalue = 100;
    		try
    		{
    			if (value instanceof String)
    			{
    				String str = value.toString();
    				System.out.println("value inside room "+str);
    				intvalue = Integer.parseInt(str); 
    			}
    			else if (value instanceof Integer)
    			{
    				intvalue = (Integer)value;    	   
    			}
    		}
    		catch(Exception e)
    		{
    			System.out.println("value properties error "+e.getMessage());
    		}
    		finally
    		{
    			callAmountDefault = intvalue.intValue();
    		}
    	}
    	else
    	{
    		callAmountDefault = 100;
    	}
    }
    private int getNextPlayerTurn()
    {
    	nextGamePlayTurn++;
    	if (nextGamePlayTurn>=userList.size())
    	{
    		nextGamePlayTurn=0;
		}
    	return nextGamePlayTurn;
    }
    public boolean endGameWinner(String winnerName)
    {
    	IUser user = null;
    	lastWinner = winnerName;
    	
    	allBettingDone = false;
    	
    	for(IUser tempUser: userList)
    	{
    		if (tempUser.getName().equalsIgnoreCase(winnerName))
    		{
    			user = tempUser;
    			playStartIndex = userList.indexOf(user);
    			playStartUsername = user.getName();
    			break;
			}
    	}
    	
    	if (user!=null)
    	{
    		try
    		{
    			this.clearLastCardsData();
    			
    			this.resetPassFlags();
        		this.resetFoldFlags();

        		GAME_STATUS = CardsConstants.STOPPED;
        		cardDeck = null;
        		cardDeck = new Deck();

        		final String winName = winnerName;
        		PlayerDetail winnerPlayer = null;
        		long amount=0;
        		for(PlayerDetail player : arrPlayerDetails)
        		{
        			if (player.userName.equalsIgnoreCase(winName))
        			{
        				winnerPlayer = player;
        				long won = Long.parseLong(winnerPlayer.totalWon);
        				won++;
        				winnerPlayer.totalWon = Long.toString(won);
    				}
        			player.cardList.clear(); // clear cards for players if any    			
        			amount+=player.raiseChipAmount;    				
        			player.raiseChipAmount= 0;    
        			player.roundBetAmount = 0;
        			player.didBet = false;
        			player.ChipsValue = Long.toString(player.totalChipAmount);
        		}
        		
        		System.out.println("table and winner amount "+centerTableAmount+" and chips amount "+amount);
        		
        		amount += centerTableAmount;
        		
        		winnerPlayer.totalChipAmount+=amount;
        		winnerPlayer.ChipsValue = Long.toString(winnerPlayer.totalChipAmount);

        		System.out.println("GameWinner is "+winnerPlayer.userName);
        		System.out.println("GameWinner total amount "+winnerPlayer.totalChipAmount);
        		System.out.println("***After game player details.***");
        		for(PlayerDetail player : arrPlayerDetails)
        		{
        			System.out.println("GamePlayer is "+player.userName);
            		System.out.println("GamePlayer total amount "+player.totalChipAmount);
        		}
        		
        		/*
        		 Rest all iVars
        		 */
        		passcounts = gameRound = 0;
        		centerTableAmount = 0;
        		
        		currentPlayerIndex = playStartIndex=-1;
        		playStartUsername = currentPlayuserName = ""; 
        		
        		/*
        		 just in case any user have 0 amount add clear out that users to waiting state.
        		 */
        		ArrayList<PlayerDetail> arrOutUsers = new ArrayList<PlayerDetail>();
        		ArrayList<PlayerDetail> arrPlayerCopy = new ArrayList<PlayerDetail>();
        		arrPlayerCopy.addAll(arrPlayerDetails);
        		
        		for(PlayerDetail player : arrPlayerCopy)
        		{
        			if(!(player.totalChipAmount>0))
        			{
        				arrOutUsers.add(player);
        				arrPlayerDetails.remove(player);
        			}        			
        		}
        		arrPlayerCopy.clear();
        		/*****************/
        		
        		for(PlayerDetail player : arrWaitingPlayerDetails)
        		{
        			if (player.totalChipAmount>0)
        			{
        				player.isInGame = true;
            			player.raiseChipAmount= 0;
            			arrPlayerDetails.add(player);	
					}
        		}
        		
        		arrWaitingPlayerDetails.clear();    		
        		for(IUser users : waitingUsers)
        		{
        			userList.add(users);
        		}
        		waitingUsers.clear();
        		
        		///*** Adding arrOutUsers in waiting list**/
        		for (PlayerDetail player : arrOutUsers)
        		{
        			player.isInGame = false;
        			arrWaitingPlayerDetails.add(player);
            		userList.remove(player.user);
    				waitingUsers.add(player.user);       
    				System.out.println(":::-:"+player.userName+" is out of game now. ");	
        		}
        		
        		System.out.println(" at end game userListSize  "+userList.size());
        		
        		//**NOW all clear**////
    			centerTableCard.clear();

        		if (userList.size()>=2)
        		{    			
            		Timer newTimer = new Timer();
            		TimerTask task = new TimerTask() {
        				@Override
        				public void run() {
        					
        					if (GAME_STATUS != CardsConstants.RUNNING)
        					{
//            					distributeCardsForGame(winName); // comment on 19 march 2015        						
            					if(userList.size()>=2)
            					{
            						// added on 19 march 15
                					int nextIdx = getNextPlayerTurn();
                					IUser user = userList.get(nextIdx);
                					String name = user.getName();
                					System.out.println("//**New Game Begins with "+name+" !!");            					
                					distributeCardsForGame(name);
                					// added end on 19 march 15   
            					}
            				}
        				}
        			};
            		newTimer.schedule(task, 5*MSecs);
    			}
        		else
        		{
        			// handle no game play.
        		}
        		
    		}
    		catch(Exception e)
    		{
//    			System.out.println("SomeIssue in parsing End Game msg or Array>>: "+e.getLocalizedMessage());
    			e.printStackTrace();
    		}
    		
        	return true;	
    	}
    	else
    	{
        	return false;
    	}
    }
    private boolean allUserBetsDone()
    {
    	long maxBet = 0;
    	int count,totalPlayers;
    	totalPlayers = count =0;
    	for(PlayerDetail player : arrPlayerDetails)
    	{
    		if(player.isInGame == true)
    		{
    			totalPlayers++;
                if (maxBet!=0)
                {
                	if (player.roundBetAmount == maxBet)
                    {
                        count++;
                    }
                	else
                	{
                		if ((player.roundBetAmount>0) && player.roundBetAmount != maxBet)
                        {
                            count++;
                        }
                	}
                }
                else
                {
                	if (player.roundBetAmount !=0)
                    {
                        maxBet = player.roundBetAmount;
                        count++;
                    }
                }
    		}
    	}
    	if (count==totalPlayers)
    	{
    		return true;
    	}
    	return false;
    }
    
    private boolean allUserChecked()
    {
    	int check = 0;
    	int totalPlayer = 0;
    	for(PlayerDetail player : arrPlayerDetails)
    	{
    		if(player.isInGame == true)
    		{
    			totalPlayer++;
    			if (player.didPass)
        		{
        			check++;
        		}	
    		}
    	}
    	    	
    	if (check == totalPlayer)
    	{
    		return true;
    	}
    	return false;
    }
    public String requestNewCar(String username) throws JSONException
    {
		JSONObject output = new JSONObject();    	
    	try
    	{
    		if (checkUserExist(username))
    		{
            	JSONArray objStr = new JSONArray();
            	for(int i=0;i<MAX_NO_OF_CARDS;i++)
            	{
            		Card card = getNewCard();
            		int cardValue = card.getCards().getCardValue() +card.getSuits().getSuitsValue();
            		objStr.put(cardValue);
            	}    	
            	output.put("success", "1");
            	output.put("data", objStr);
			}
    		else
    		{
    			output.put("success", "0");
    			output.put("data", "Error! User does not exist");
    		}    		
       	}
    	catch(JSONException e)
    	{
            e.printStackTrace();
        }
    	output.put("username", username);

		return output.toString(); 
    }
    private void clearLastCardsData()
    {
    	arrLastCardDetails.clear();
    }
    private boolean checkUserExist(String username)
    {
    	boolean returnValue = false;
    	for(IUser user : userList)
    	{
    		if (user.getName().equalsIgnoreCase(username))
    		{
    			returnValue = true;
    			break;
			}
    	}
    	return returnValue;
	}
    private boolean checkUserExistInWait(String username)
    {
    	boolean returnValue = false;
    	
    	for(IUser user : waitingUsers)
    	{
    		if (user.getName().equalsIgnoreCase(username))
    		{
    			returnValue = true;
    			break;
			}
    	}
    	return returnValue;
    }
    private boolean checkUserExistInViewers(String username)
    {
    	boolean returnValue = false;

    	for(IUser user : viewersList)
    	{
    		if (user.getName().equalsIgnoreCase(username))
    		{
    			returnValue = true;
    			break;
    		}
    	}

    	return returnValue;
    }
	private IUser getIUserFromList(String username)
    {
    	IUser strUser = null;
    	for(IUser user : userList)
    	{
    		if (user.getName().equalsIgnoreCase(username))
    		{
    			strUser = user; 
    			break;
    		}
    	}
    	return strUser;
	}
	/*
	 * This method return last element of TOTAL_CARDS
	 * In case of empty list again shuffle cards
	 */
    private Card getNewCard()
    {
    	if (cardDeck.hasMoreCards())
    	{
    		Card cardis = cardDeck.dealCard();
    		return cardis;
//        	Integer cardValue  = cardis.getCardValue().getCardValue() +cardis.getSuit().getSuitsValue();
//        	return cardValue;	
		}
    	else
    	{
    		cardDeck = null;
    		cardDeck = new Deck();
    		Card cardis = cardDeck.dealCard();
    		return cardis;
//        	Integer cardValue  = cardis.getCardValue().getCardValue() +cardis.getSuit().getSuitsValue();
//        	return cardValue;
    	}
     }

    private void distributeCardsForGame(String startingPlayer)
    {
    		// notify to start game
    		JSONObject obj  = new JSONObject();
    		try {
    			   
    			obj.put(CardsConstants.kSuccessKey, "1");
         	
    			JSONObject objUserList = new JSONObject();
    			
    			for(IUser user : this.userList)
    			{
    				JSONArray objStr = new JSONArray();
    				ArrayList<Card> cardList = new ArrayList<Card>();
    				
    				for (int i = 0; i < MAX_NO_OF_CARDS; i++) 
    				{
    					Card card = getNewCard();
                		int cardValue = card.getCards().getCardValue() +card.getSuits().getSuitsValue();
    					objStr.put(cardValue);
    					cardList.add(card);
    				}
    				
    				if (cardList.size()>0)
    				{
    					PlayerDetail player = getPlayerDetails(user.getName());
    					if (player != null)
    					{
    						player.setCardList(cardList);	
    					}
    				}
    				objUserList.put(user.getName(),objStr);
    			}
    			obj.put(CardsConstants.kUserListKey,objUserList);
    			obj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeGameStartWithCard);
//    			obj.put(CardsConstants.kPlayerTurnKey, userList.get(0).getName());
    			obj.put(CardsConstants.kPlayerTurnKey, startingPlayer);			
    		}
    		catch (JSONException e)
    		{
    			e.printStackTrace();
    		}
    		   
    		if (userList.size()>1)
           	{
    			IUser user = getIUserFromList(startingPlayer);
      		   	playStartIndex = userList.indexOf(user);
      		   	playStartUsername = user.getName();
      		   	currentPlayerIndex = playStartIndex;
      		   	currentPlayuserName = user.getName();
      		   	passcounts = 0;
      		   	lastWinner = null;
      		   
           		GAME_STATUS = CardsConstants.RUNNING;
           		this.gameRoom.BroadcastChat(CardsConstants.SERVER_NAME, obj.toString());		
        		this.sendInitialBetMessage(false); // get bets from all users.           		
           	}    	
       		else
       		{
       			GAME_STATUS = CardsConstants.STOPPED;
       			// clear card list if any.
       			for(PlayerDetail player : arrPlayerDetails)
       			{
       				player.cardList.clear();       				
       			}
       			System.out.println("Minimum Player need to play game!!");
       		}
    }
    
    public void handleChatRequest(IUser sender, String message, HandlingResult result)
    {
//		System.out.println("Sender :: "+sender.getName());
//		System.out.println("Message:: "+message);
    	if (!sender.getName().equalsIgnoreCase(CardsConstants.SERVER_NAME))
    	{		
			try 
			{
				JSONObject jsonObject = new JSONObject(message);
				int response = jsonObject.getInt(CardsConstants.kTypeMessageKey);
				
				String strSender = jsonObject.getString(CardsConstants.kUserNameKey);
				String strNextUser = null;
				PlayerDetail nextuser = null;				
				if (jsonObject.has(CardsConstants.kUserTurnKey))
				{
					strNextUser = jsonObject.getString(CardsConstants.kUserTurnKey);
					nextuser = getPlayerDetails(strNextUser);					
				}
				PlayerDetail playerSender = getPlayerDetails(strSender);
					
				switch (response)
				{
					case CardsConstants.kTypeCardDistribution:
					case CardsConstants.kTypeCardDistributionCall:
					case CardsConstants.kTypeCardDistributionRaise:						
					{
						int index = arrPlayerDetails.indexOf(nextuser);
						currentPlayerIndex = index;	 
						currentPlayuserName = nextuser.userName;
						gameRound = jsonObject.getInt(CardsConstants.kGameRoundKey);
						System.out.println("****** Parsing message Type in Play******"+response);
						
						if (jsonObject.has(CardsConstants.kUserCallAmountKey))
						{
							int amount = jsonObject.getInt(CardsConstants.kUserCallAmountKey);
							playerSender.raiseChipAmount+=amount;
							playerSender.totalChipAmount-= amount;	
							
							playerSender.ChipsValue = Long.toString(playerSender.totalChipAmount);
						}
						
						if (centerTableCard.size()>0)
						{
							HashMap<String, Object> newData = new HashMap<String, Object>();
							newData.put(CardsConstants.kUserCardsKey, centerTableCard);
							newData.put(CardsConstants.kUserNameKey, playStartUsername);
							arrLastCardDetails.add(newData);
						}
						
						// Maintain Play Count;
						long totalPlay = Long.parseLong(playerSender.totalPlay);
						totalPlay++;
						playerSender.totalPlay = Long.toString(totalPlay);
												
						JSONArray arrCardList = jsonObject.getJSONArray(CardsConstants.kUserCardsKey);
						centerTableCard =  convertJsonToCardList(arrCardList);
						
						playStartIndex = userList.indexOf(sender);
						passcounts = 0;
						playStartUsername = sender.getName();
						
						try
						{
							for(int i=0;i<centerTableCard.size();i++)
							{
								playerSender.cardList.remove(0);	
							}
						}
						catch(Exception e)
						{
							System.out.println("Error in card play remove array");
							e.printStackTrace();
						}
						finally
						{
							System.out.println("**in Did Play playerSender "+playerSender.userName+" and raiseAmount "+playerSender.raiseChipAmount);
							System.out.println("**in Did Play playerSender "+playerSender.userName+" and tableAmount "+playerSender.totalChipAmount);						
							System.out.println("**in Did Play PlayerNext  "+nextuser.userName+" and tableAmount "+nextuser.totalChipAmount);							
						}
					}
						break;
					case CardsConstants.kTypeUserTimeOut:
					case CardsConstants.kTypeUserPass:
					{
						int index = arrPlayerDetails.indexOf(nextuser);
						currentPlayerIndex = index;
						currentPlayuserName = nextuser.userName;
//						centerTableAmount = jsonObject.getInt(CardsConstants.kCurrentTableAmountKey);
						passcounts++;
						
						gameRound = jsonObject.getInt(CardsConstants.kGameRoundKey);
						
						System.out.println("******Checking Pass Message******");
						System.out.println("PlaystartUserName  "+playStartUsername);
						System.out.println("currentPlayuserName  "+currentPlayuserName);						
						
						System.out.println("passCounts:  "+passcounts);
						
						PlayerDetail player = getPlayerDetails(sender.getName());
						player.didPass = true;
						
//						this.processPassFor(index,jsonObject);
						int totalActivePlayer = getTotalPlayingPlayer();
						System.out.println("totalPlayers:  "+ totalActivePlayer);
//					if (playStartIndex == currentPlayerIndex && (getTotalPlayingPlayer()-1) == 0)
						if (playStartUsername.equalsIgnoreCase(currentPlayuserName) && (totalActivePlayer-1) == 0)							
						{
							// Working here on new round.....
							passcounts = 0;	
							if (centerTableCard.size()>0)
							{
								this.resetPassFlags();
								// new round begins. send msg for new round.
								System.out.println(":::>>>>>New Rounds Begins...");
								playStartIndex = arrPlayerDetails.indexOf(nextuser);
								playStartUsername = nextuser.userName;
								
//								PlayerDetail roundWinner = arrPlayerDetails.get(playStartIndex);
								PlayerDetail roundWinner = nextuser;								
								int amount = 0;
								
								for (PlayerDetail players : arrPlayerDetails)
								{
									amount += players.raiseChipAmount;
									players.raiseChipAmount = 0;
								}
								
								long totalChips = roundWinner.totalChipAmount;
								
								totalChips+=amount;
								roundWinner.totalChipAmount = totalChips;
								roundWinner.ChipsValue = Long.toString(roundWinner.totalChipAmount);
								
								// Won Count of players
								long totalWon = Long.parseLong(roundWinner.totalWon);
								totalWon++;
								roundWinner.totalWon = Long.toString(totalWon);
								
								this.clearLastCardsData();
								
								JSONObject passObj = new JSONObject();
								
								passObj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeNewRoundBegin);
								passObj.put(CardsConstants.kUserNameKey,roundWinner.getUsernme());
								passObj.put(CardsConstants.kCurrentUserIndexKey,playStartIndex);							
								passObj.put(CardsConstants.kGameRoundKey, gameRound);
								
		    					String msg = passObj.toString();
//		    					System.out.println("sending passmsg : "+msg);
		    					gameRoom.BroadcastChat(CardsConstants.SERVER_NAME, msg);
								
								this.sendInitialBetMessage(true);		
								
								centerTableCard.clear();
								
								int tblamount = jsonObject.getInt(CardsConstants.kCurrentTableAmountKey);
								
								if (tblamount != centerTableAmount)
								{
									System.out.println("*client and server side center table ammount issue");
									System.out.println("*client table ammount "+tblamount);
									System.out.println("*server table ammount "+centerTableAmount);
								}
								this.resetPassFlags();
							}
							else
							{
//								passcounts = 0;	
								System.out.println("*Not New Round YET while in side loop");							
							}	
						}
						else
						{
//							if (playStartIndex == userList.indexOf(sender) && passcounts==1)
							if (playStartUsername.equalsIgnoreCase(sender.getName()) && passcounts==1)								
							{
								passcounts = 0;
								passcounts = 0;	
								
								System.out.println("RestWith Username "+sender.getName());
								System.out.println("ResetingPlaystartIndex  "+playStartUsername);
								System.out.println("ResetingcurrentPlayerIndex  "+currentPlayuserName);
								playStartIndex = currentPlayerIndex;
//								playStartUsername = nextuser.userName;
								playStartUsername = currentPlayuserName;								
								
								this.resetPassFlags();
								
								System.out.println("*Reseting reseting StartIndex and pass count as Firstplayer Pass");	
							}
							System.out.println("*Not New Round YET");							
						}
					}
						break;
					case CardsConstants.kTypeWinner:
					{
						System.out.println("winner Msg "+message);
//						centerTableAmount = jsonObject.getInt(CardsConstants.kCurrentTableAmountKey);
					}
						break;
					case CardsConstants.kTypeFold:
					{
						// Original Code
						playerSender.isInGame = false;
						gameRound = jsonObject.getInt(CardsConstants.kGameRoundKey);
						
						int index = arrPlayerDetails.indexOf(nextuser);
						currentPlayerIndex = index;
						currentPlayuserName = nextuser.userName;
						
						// New Code Added BELOW, if any issue remove it.						
						if(!allBettingDone)
						{
							//nextuser
							if (this.allUserBetsDone()) 
							{
								allBettingDone = true;
								// set all bets to mainpots
								this.moveRoundBetsToMainPoit();
							}
							else
							{
								if (this.allUserChecked())
								{
									allBettingDone = true;
								// set all bets to mainpots
									this.moveRoundBetsToMainPoit();
								}
							}
						}
						else
						// New Code Added ABOVE, if any issue remove it.
						{
//							if (playStartIndex == userList.indexOf(sender))
							if (playStartUsername.equalsIgnoreCase(nextuser.userName))							
							{
								playStartIndex = currentPlayerIndex;
								passcounts = 0;
								playStartUsername = currentPlayuserName;
							}
							else
							{
								int count=0;
								
								for(PlayerDetail player : arrPlayerDetails)
								{
									if (player.isInGame)
									{
										count++;	
									}
								}
								
								if (count >1)
								{
									final int newIndex = index;
						    		Timer newTimer = new Timer();
					        		TimerTask task = new TimerTask() {
					    				@Override
					    				public void run() 
					    				{
											processPassFor(newIndex);
					    				}
					    			};
					        		newTimer.schedule(task, 0*MSecs);
								}
							}
						}
					}
						break;					
					case CardsConstants.kTypeUserCheck:
					{
						int index = arrPlayerDetails.indexOf(nextuser);
						currentPlayerIndex = index;
						currentPlayuserName = nextuser.userName;
						gameRound = jsonObject.getInt(CardsConstants.kGameRoundKey);
						
//						if (playStartIndex == userList.indexOf(sender))
//						{
//							playStartIndex = currentPlayerIndex;
//							passcounts = 0;
//						}
//						else
						{
//							this.processPassFor(index);
							
							// temp change here.
							//=====//
//							if (getTotalPlayingPlayer() >1)
//							{
//								final int newIndex = index;
//					    		Timer newTimer = new Timer();
//				        		TimerTask task = new TimerTask() {
//				    				@Override
//				    				public void run() 
//				    				{
//										processPassFor(newIndex);
//				    				}
//				    			};
//				        		newTimer.schedule(task, 0*MSecs);
//							}
							//=====//
						}
						
//						centerTableAmount = jsonObject.getInt(CardsConstants.kCurrentTableAmountKey);
					}
						break;						
					case CardsConstants.kTypeUserStatus:
					{						
						String playerName = jsonObject.getString(CardsConstants.kUserName);
						String total = jsonObject.getString(CardsConstants.kUserTotalHands);
						String totalWon = jsonObject.getString(CardsConstants.kUserTotalWonHands);						
						String chipsValue = jsonObject.getString(CardsConstants.kUserGamesChipsValue);
						String userID = jsonObject.getString(CardsConstants.kUserAccountID);
						String giftID = jsonObject.getString(CardsConstants.kUserGiftID);
						String imageUrl = jsonObject.getString(CardsConstants.kUserImageUrl);
						
						if (waitingUsers.contains(sender))
						{
							PlayerDetail waitingPlayers = getWaitingPlayerDetails(strSender);
							System.out.println("** In Waiting ** User Status "+waitingPlayers.userName+" with amount "+chipsValue);
							waitingPlayers.totalChipAmount = Long.parseLong(chipsValue);
							waitingPlayers.playerName = playerName;
							waitingPlayers.totalPlay= total;
							waitingPlayers.totalWon= totalWon;							
							waitingPlayers.ChipsValue = chipsValue;
							waitingPlayers.userID = userID;
							waitingPlayers.defaultGiftID = giftID;
							waitingPlayers.profileImageUrl = imageUrl;
						}
						else if (userList.contains(sender))
						{
							System.out.println("**In Before Gameplay ** User Status "+playerSender.userName+" with amount "+chipsValue);							
							playerSender.totalChipAmount = Long.parseLong(chipsValue);			
							playerSender.playerName = playerName;
							playerSender.totalPlay= total;
							playerSender.totalWon= totalWon;
							playerSender.ChipsValue = chipsValue;
							playerSender.userID = userID;		
							playerSender.defaultGiftID = giftID;
							playerSender.profileImageUrl = imageUrl;
						}
						else if (viewersList.contains(sender))
						{
							PlayerDetail viewerDetails = getViewersPlayerDetails(strSender);
							System.out.println("**In Viewers Gameplay ** User Status "+playerSender.userName+" with amount "+chipsValue);							
							viewerDetails.totalChipAmount = Long.parseLong(chipsValue);			
							viewerDetails.playerName = playerName;
							viewerDetails.totalPlay= total;
							viewerDetails.totalWon= totalWon;
							viewerDetails.ChipsValue = chipsValue;
							viewerDetails.userID = userID;		
							viewerDetails.defaultGiftID = giftID;
							viewerDetails.profileImageUrl = imageUrl;
						}
					}
						break;
					case CardsConstants.kTypeUserChat:
					{
						System.out.println("Receive Chat Message");
					}
						break;
					case CardsConstants.kTypeSendGift:
					{
						System.out.println("Receive Gift Message");						
					}
						break;	
					case CardsConstants.kTypeBetRoundBet:
					{
						if (jsonObject.has(CardsConstants.kUserCallAmountKey))
						{
							int amount = jsonObject.getInt(CardsConstants.kUserCallAmountKey);
							playerSender.raiseChipAmount+=amount;
							playerSender.totalChipAmount-= amount;	
							playerSender.roundBetAmount += amount;
							playerSender.ChipsValue = Long.toString(playerSender.totalChipAmount);
							playerSender.didBet = true;
							playerSender.didPass = false;
							
							//nextuser
							if (this.allUserBetsDone()) 
							{
								allBettingDone = true;
								// set all bets to mainpots
								this.moveRoundBetsToMainPoit();
							}
							else
							{
								if (this.allUserChecked())
								{
									allBettingDone = true;
								// set all bets to mainpots
									this.moveRoundBetsToMainPoit();
								}
							}
						}
					}
						break;
					case CardsConstants.kTypeBetRoundCheck:
					{
						playerSender.didPass = true;
						
						System.out.println("Receive kTypeBetRoundCheck Message");
						if (this.allUserChecked())
						{
							allBettingDone = true;
							// set all bets to mainpots
							this.moveRoundBetsToMainPoit();
						}
					}
						break;
					case CardsConstants.kTypeBetRoundRaise:
					{
						System.out.println("Receive kTypeBetRoundRaise Message");	
						if (jsonObject.has(CardsConstants.kUserCallAmountKey))
						{
							int amount = jsonObject.getInt(CardsConstants.kUserCallAmountKey);
							playerSender.raiseChipAmount+=amount;
							playerSender.totalChipAmount-= amount;	
							playerSender.roundBetAmount += amount;
							playerSender.ChipsValue = Long.toString(playerSender.totalChipAmount);
							playerSender.didBet = true;
						}
					}
					break;
					
					case CardsConstants.kTypeJoinRequest:
					{
						PlayerDetail details = getViewersPlayerDetails(strSender);
						if (details != null)
						{
							String playerName = jsonObject.getString(CardsConstants.kUserName);
							String total = jsonObject.getString(CardsConstants.kUserTotalHands);
							String totalWon = jsonObject.getString(CardsConstants.kUserTotalWonHands);						
							String chipsValue = jsonObject.getString(CardsConstants.kUserGamesChipsValue);
							String userID = jsonObject.getString(CardsConstants.kUserAccountID);
							String giftID = jsonObject.getString(CardsConstants.kUserGiftID);
							String imageUrl = jsonObject.getString(CardsConstants.kUserImageUrl);
							
							details.totalChipAmount = Long.parseLong(chipsValue);			
							details.playerName = playerName;
							details.totalPlay= total;
							details.totalWon= totalWon;
							details.ChipsValue = chipsValue;
							details.userID = userID;		
							details.defaultGiftID = giftID;
							details.profileImageUrl = imageUrl;
							
							if (GAME_STATUS == CardsConstants.RUNNING)
							{
								// game is running
								waitingUsers.add(sender);
								arrWaitingPlayerDetails.add(details);	
								
								viewersList.remove(sender);
								arrViewers.remove(details);
							}
							else if (GAME_STATUS == CardsConstants.STOPPED)
							{
								// check if enough players then start game again.
								userList.add(sender);
								arrPlayerDetails.add(details);
								
								viewersList.remove(sender);
								arrViewers.remove(details);
								
								if (userList.size() >=2){
									Timer newTimer = new Timer();
				            		TimerTask task = new TimerTask() {
				        				@Override
				        				public void run() {				            					
				            					// added on 25 march 15
				            					int nextIdx = getNextPlayerTurn();
				            					IUser user = userList.get(nextIdx);
				            					String name = user.getName();
				            					System.out.println("//**New Game Begins with "+name+" !!");            					
				            					distributeCardsForGame(name);
				            					// added end on 25 march 15            					
				        				}
				        			};
				            		newTimer.schedule(task, 3*MSecs);
								}
							}
						}
					}
						break;
					default:						
					{
						System.out.println("Unknown type Message receive .._.. "+message);
					}
						break;
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
    	else
    	{
    		System.out.println("Server Send Message");    		
    	}
    }
    
    private void moveRoundBetsToMainPoit()
    {
    	this.resetPassFlags();
    	System.out.println("moving bets");
    	long amounts = 0;
    	for(PlayerDetail player : arrPlayerDetails)
    	{
    		amounts+= player.roundBetAmount;
    		player.roundBetAmount = 0;
    		player.raiseChipAmount = 0;
    	}
    	System.out.println("moving bets... amount "+amounts);
    	centerTableAmount += amounts;
    	System.out.println("totalCenter... amount "+centerTableAmount);    	
    }
    
//    /** 
//     * Invoked when a user sends an unsubscribe request  
//     *  
//     * @param sender sender of the request 
//     */  
//    @Override
//    public void onTimerTick(long time) 
//    {
//  
//    }
    
    public void handleUserJoinRequest(IUser user, HandlingResult result)  
    {
    	System.out.println("inside > handleUserJoinRequest name : "+user.getName());
    	
    }
    
    /** 
    * Invoked when a subscribe room request is received by the room. 
    *  
    * By default this will result in a success response sent back the user,  
    * the user will be added to the list of subscribed users of the room. 
    *  
    * @param sender sender of the request 
    * @param result use this to override the default behavior 
    */  
    
   public void handleUserSubscribeRequest(IUser sender, HandlingResult result)  
   {
	   System.out.println("inside > handleUserSubscribeRequest");
	   if (checkUserExist(sender.getName()))
	   {
		   // here user already exist
		   System.out.println("Already exist req rec for :"+ sender.getName()+" from IP: "+sender.getIPAddress());
		   
		   PlayerDetail details =  getPlayerDetails(sender.getName());
		   userList.remove(details.user);		   
		   arrPlayerDetails.remove(details);
	   }
//	   else
	   {
		   if(GAME_STATUS == CardsConstants.STOPPED ) //&& !schedualStarted
		   {
			   if (userList.size()>0)
			   {
				   // send user status with game not running;
				   this.sendInitialGameState();
			   }
			   if (!checkUserExist(sender.getName()))
			   {
				   userList.add(sender);
				   PlayerDetail player = new PlayerDetail(sender);
				   arrPlayerDetails.add(player);
				   System.out.println("Before GameStart: "+sender.getName()+" with size:"+userList.size());			   
			   }
			   else
			   {
				   System.out.println("****User exist");
				   for(IUser user : userList)
			    	{
					   System.out.println(">Name "+user.getName());
			    	}
			   }
			   
			   if (userList.size()>=2 )
			   {
//				   if(GAME_STATUS == CardsConstants.STOPPED)
//				   {
					   GAME_STATUS = CardsConstants.RUNNING;
					   schedualStarted = true;
					   Timer timer = new Timer();
					   TimerTask delayedThreadStartTask = new TimerTask()
					   {
							@Override
							public void run() 
							{
								if(lastWinner!=null)
								{
//									IUser user = getIUserFromList(lastWinner);
//									int index = userList.indexOf(user);
//									playStartIndex = index;
//									playStartUsername = user.getName();
//									
//									String player = lastWinner;
//									lastWinner = null;
//									distributeCardsForGame(player);
									
									int nextIdx = getNextPlayerTurn();
	            					IUser user = userList.get(nextIdx);
	            					String name = user.getName();
	            					System.out.println("//**New Game Begins with "+name+" !!");            					
	            					distributeCardsForGame(name);
								}
								else
								{
									playStartIndex = 0;
									playStartUsername = userList.get(0).getName();
									distributeCardsForGame(userList.get(0).getName());	
								}
								schedualStarted = false;
							}
					   };
					   timer.schedule(delayedThreadStartTask, 3 * MSecs); //3 secs
//				   }
			   }			   
		   }
		   else if (GAME_STATUS == CardsConstants.RUNNING)
		   {
			   int totalSize = arrPlayerDetails.size() + arrWaitingPlayerDetails.size();
			   
			   if (totalSize < CardsConstants.MaxPlayers)
			   {
				   // let them add in waiting states
				   if (!checkUserExistInWait(sender.getName()))
				   {
					   //add in waiting user list.
					   waitingUsers.add(sender);
					   PlayerDetail player = new PlayerDetail(sender);
					   arrWaitingPlayerDetails.add(player);
					   System.out.println("In waiting State:"+sender.getName()+" with result:"+waitingUsers.size());			   
				   }
				   else
				   {
					   int i =0;
					   
						for(IUser user : waitingUsers)
						{
							if (user.getName().equalsIgnoreCase(sender.getName()))
							{	
								waitingUsers.remove(user);
								waitingUsers.add(i, sender);
								break;
							}
							i++;
						}
						
						PlayerDetail player1 = getWaitingPlayerDetails(sender.getName());
						if (player1!=null)
						{
							player1.user = sender;
						}
					   
					   System.out.println("In waiting State Use Exists:"+sender.getName()+" with result:"+waitingUsers.size());			   				   
				   }
			   }
			   else
			   {
				   // add them as game viewers
				   if (!checkUserExistInViewers(sender.getName()))
				   {
					   //add in waiting user list.
					   viewersList.add(sender);
					   
					   PlayerDetail player = new PlayerDetail(sender);
					   arrViewers.add(player);
					   System.out.println("In Viewwer State:"+sender.getName()+" with result:"+viewersList.size());			   
				   }
				   else
				   {
					   int i =0;
					   
						for(IUser user : viewersList)
						{
							if (user.getName().equalsIgnoreCase(sender.getName()))
							{	
								viewersList.remove(user);
								viewersList.add(i, sender);
								break;
							}
							i++;
						}
						
						PlayerDetail player1 = getViewersPlayerDetails(sender.getName());
						if (player1!=null)
						{
							player1.user = sender;
						}
						System.out.println("In Viewwer State:"+sender.getName()+" with result:"+viewersList.size());	
				   }
			   }
			  
			   this.sendGameStatus();
		   }
	   }
   }
   
   public void onUserUnsubscribeRequest(IUser sender)  
   {
	   if (viewersList.contains(sender))
	   {
		   viewersList.remove(sender);
		   arrViewers.remove(this.getViewersPlayerDetails(sender.getName()));
		   System.out.println("onDisconnectUser:"+sender.getName()+" viewers Size: "+viewersList.size());
	   }
	   else if (waitingUsers.contains(sender))
       {
		   System.out.println("onUserUnsubscribeRequest:"+sender.getName()+" waitingUsers>Size: "+waitingUsers.size());		   
		   waitingUsers.remove(sender);
		   arrWaitingPlayerDetails.remove(this.getWaitingPlayerDetails(sender.getName()));		   
	   }
	   else if(userList.contains(sender))
	   {
		   userList.remove(sender);
		   arrPlayerDetails.remove(getPlayerDetails(sender.getName()));
		   System.out.println("onUserUnsubscribeRequest:"+sender.getName()+" Size: "+userList.size());
		   if (userList.size()<=1)
		   {
			   GAME_STATUS = CardsConstants.STOPPED;	
			   this.clearAllUserDetails();       			
		   }
	   }  
	   else
	   {
		   System.out.println("onUserUnsubscribeRequest was not successfull:"+sender.getName()+" Size: "+userList.size());		   
	   }
   }
   public void onUserLeaveRequest(IUser user)  
   {    	
	   System.out.println("onUserLeaveRequest. "+user.getName());
	   this.onDisconnectUser(user);
   }
   
   /** Process to clear user from list From Any state*/
   public void onDisconnectUser(IUser sender)
   {
	   if (viewersList.contains(sender))
	   {
		   viewersList.remove(sender);
		   arrViewers.remove(this.getViewersPlayerDetails(sender.getName()));
		   System.out.println("onDisconnectUser:"+sender.getName()+" viewers Size: "+viewersList.size());
	   }
	   else if (waitingUsers.contains(sender))
       {
		   waitingUsers.remove(sender);
		   arrWaitingPlayerDetails.remove(this.getWaitingPlayerDetails(sender.getName()));
		   System.out.println("onDisconnectUser:"+sender.getName()+" waiting Size: "+waitingUsers.size());		   
	   } 
	   else if(userList.contains(sender))
	   {
		   StartUserleftProcess(sender.getName());

		   this.processUserLeft(sender.getName());// process leave request in ivars

		   userList.remove(sender); 
		   arrPlayerDetails.remove(getPlayerDetails(sender.getName()));
		   
		   final int newIndex = currentPlayerIndex;
		   Timer newTimer = new Timer();
		   TimerTask task = new TimerTask() {
			   @Override
			   public void run() 
			   {
				   processPassFor(newIndex);
			   }
		   };
		   newTimer.schedule(task, 0*MSecs);

		   System.out.println("onDisconnectUser:"+sender.getName()+" Size: "+userList.size());
		   if (userList.size()<=1)
		   {
			   GAME_STATUS = CardsConstants.STOPPED;	
			   this.clearAllUserDetails();
			   
//			   if (waitingUsers.size()>0)
//			   {
//					for(PlayerDetail player : arrWaitingPlayerDetails)
//	        		{
//	        			if (player.totalChipAmount>0)
//	        			{
//	        				player.isInGame = true;
//	            			player.raiseChipAmount= 0;
//	            			arrPlayerDetails.add(player);	
//						}
//	        		}
//	        		
//	        		arrWaitingPlayerDetails.clear();    		
//	        		for(IUser users : waitingUsers)
//	        		{
//	        			userList.add(users);
//	        		}
//	        		waitingUsers.clear();
//			   }
//			   
//			   if(userList.size() >= 2)
//			   {
//					int nextIdx = getNextPlayerTurn();
//					IUser user = userList.get(nextIdx);
//					String name = user.getName();
//					System.out.println("//**New Game Begins with "+name+" !!");            					
//					distributeCardsForGame(name);
//			   }
		   }
	   }  
	   else
	   {
		   if (userList.size() > 0 || waitingUsers.size() > 0 || viewersList.size() > 0 )
		   {
//			   System.out.println("Listing All Types User \\ Game Play users");
			   boolean didRemove = false;
			   
			   for (IUser user1 : waitingUsers)
			   {
				   System.out.println("UserInWaiting>>>"+user1.getName());				   
				   if (user1.getName().equalsIgnoreCase(sender.getName()))
				   {
					   didRemove = true;
					  
					   waitingUsers.remove(user1);
					   arrWaitingPlayerDetails.remove(this.getWaitingPlayerDetails(user1.getName()));
					   System.out.println("onDisconnectUser:"+sender.getName()+" waiting Size: "+waitingUsers.size());
					   System.out.println("Waiting Users Loop Break");
					   break;
				   }
			   }
			   if (!didRemove)
			   {
				   for (IUser user1 : viewersList)
				   {
					   System.out.println("UserInViewerList>>>"+user1.getName());
					   
					   if (user1.getName().equalsIgnoreCase(sender.getName()))
					   {
						   didRemove = true;
						   viewersList.remove(user1);
						   arrViewers.remove(this.getViewersPlayerDetails(user1.getName()));
						   System.out.println("onDisconnectUser:"+sender.getName()+" viewers Size: "+viewersList.size());
						   System.out.println("Waiting Users Loop Break");
						   break;
					   }
				   }
				   if (!didRemove) {
					   for (IUser user1 : userList)
					   {
						   System.out.println("UserInPlay>>>"+user1.getName());
						   if (user1.getName().equalsIgnoreCase(sender.getName()))
						   {
							  didRemove = true;
							   
							  this.StartUserleftProcess(user1.getName());
							  this.processUserLeft(user1.getName());// process leave request in ivars
							  
							  userList.remove(user1); 
							  arrPlayerDetails.remove(getPlayerDetails(user1.getName()));
							  
							  final int newIndex = currentPlayerIndex;
					    		Timer newTimer = new Timer();
					    		TimerTask task = new TimerTask() {
									@Override
									public void run() 
									{
										processPassFor(newIndex);
									}
								};
					    		newTimer.schedule(task, 0*MSecs);
					    		
					    		if (userList.size()<=1)
					       		{
					       			GAME_STATUS = CardsConstants.STOPPED;	
					       			this.clearAllUserDetails();
								}
					 		   System.out.println("onDisconnectUser:"+sender.getName()+" Size: "+userList.size());
							   System.out.println("Players Users Loop Break");
							   break;
						   }
					   }
				   }
			   }
			   if (!didRemove)
			   {
				   System.out.println("error in onDisconnectUser for: "+sender.getName());
//				   System.out.println("error in onDisconnectUser Location: "+sender.getLocation());
//				   System.out.println("error in onDisconnectUser IPAddress:"+sender.getIPAddress());
//				   System.out.println("error in onDisconnectUser CustomData: "+sender.getCustomData());
			   }
		   }
		   else{
			   System.out.println("User have just joined room but did not subscribe it.");
		   }
	   }
   }
   
   private void StartUserleftProcess(String userName)
   {
	   try
	   {
		   if (GAME_STATUS==CardsConstants.STOPPED)
		   {
			   System.out.println("Game Not Started Yet.");
			   return;
		   }
		   
		   if (playStartUsername.equalsIgnoreCase(userName))
		   {
			   centerTableCard.clear();
			   
			   if (arrLastCardDetails.size()>0)
			   {
				   HashMap<String, Object> data = (HashMap<String, Object>)arrLastCardDetails.get(arrLastCardDetails.size()-1);
	               
	               String uName = (String) data.get(CardsConstants.kUserNameKey);
	               @SuppressWarnings("unchecked")
	               ArrayList<Integer> cards = ( ArrayList<Integer> )data.get(CardsConstants.kUserCardsKey);
	               centerTableCard = cards;
	               
	               playStartUsername = uName;
	               arrLastCardDetails.remove(arrLastCardDetails.size()-1);
	             
	               /*
	                * Following Condition place for
	                * >: 0 1 2 3 4 5 , started play with 3rd ,4 and 5 play, now 0,1,2 pass,, 
	                *  Now 3rd turn,. and 5 , 4 left, now start play is currentplay, so he is winner and start new round.
	                * */
	               
//	               if (playStartUsername.equalsIgnoreCase(currentPlayuserName))
//	               {	
//	            	   this.processPassFor(currentPlayerIndex);
//	               }
			   }
			   else
			   {
	               playStartUsername = userList.get(currentPlayerIndex).getName();
			   }
		   }
		   else
		   {
			   if (arrLastCardDetails.size()>0)
			   {
				   int i=0;
	               while (i<arrLastCardDetails.size())
	               {
	                   HashMap<String, Object> data = (HashMap<String, Object>)arrLastCardDetails.get(i);
	                   
	                   String uName = (String) data.get(CardsConstants.kUserNameKey);
	                   
	                   if (userName.equalsIgnoreCase(uName))
	                   {
	                       arrLastCardDetails.remove(data);
	                       break;
	                       
	                   }
	                   i++;
	               }
			   }
		   }
	   }
	   catch(Exception e)
	   {
		   System.out.println("********************In StartUserLeftProcess  CUrrentplay index Error.***********");
		   System.out.println("error: "+e.getLocalizedMessage());
		   e.printStackTrace();
	   }
   }
  
   private void processPassFor(int nextIndex )
   {
	   if (userList.size()>0)
	   {
		   try
		   {
			   if (currentPlayerIndex==-1)
			   {
				   return;
			   }
//			   User user = (User) userList.get(currentPlayerIndex);

			   if (playStartUsername.equalsIgnoreCase(currentPlayuserName) && (getTotalPlayingPlayer()-1) == 0)		   
			   {
					// Working here on new round.....
					System.out.println("checking condition of enter");
					
					if (centerTableCard.size()>0)
					{
						passcounts = 0;	
						this.resetPassFlags();
						
						// new round begins. send msg for new round.
						System.out.println(":::>>>>>New Rounds Begins...");
						
						playStartIndex = currentPlayerIndex;
						playStartUsername = currentPlayuserName;
						
						PlayerDetail roundWinner = getPlayerDetails(playStartUsername);
						int amount = 0;
						
						for (PlayerDetail players : arrPlayerDetails)
						{
							amount += players.raiseChipAmount;
							players.raiseChipAmount = 0;
							players.roundBetAmount = 0;
						}
						
						long totalChips = roundWinner.totalChipAmount;
						totalChips+=amount;
						roundWinner.totalChipAmount = totalChips;
						roundWinner.ChipsValue = Long.toString(totalChips);

						long won = Long.parseLong(roundWinner.totalWon);
						won++;
						roundWinner.totalWon = Long.toString(won);
						
						this.clearLastCardsData();
						
						try
						{
							JSONObject passObj = new JSONObject();
							
							passObj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeNewRoundBegin);
							passObj.put(CardsConstants.kUserNameKey,roundWinner.getUsernme());
							passObj.put(CardsConstants.kCurrentUserIndexKey,playStartIndex);							
							passObj.put(CardsConstants.kGameRoundKey, gameRound);
							
							String msg = passObj.toString();
//							System.out.println("sending passmsg : "+msg);
							gameRoom.BroadcastChat(CardsConstants.SERVER_NAME, msg);
							this.sendInitialBetMessage(true);		
							
							centerTableCard.clear();
						}
						catch(JSONException e)
						{
							e.printStackTrace();
							System.out.println("In Pass reset>> "+e.getLocalizedMessage());
						}
					}
			   }
//			   else
//			   {
//				   System.out.println("No need New Round Message");
//			   }
		   }
		   catch(Exception e)
		   {
			   System.out.println("********************In Pass  CUrrentplay index Error.***********");
		   }
	   }
   }
   
   private void processUserLeft(String userName)
   {
	   IUser current = getIUserFromList(userName);
	   
	   if (!(arrPlayerDetails.size() > 1))
	   {
		   System.out.println("only 1 player leave process");
		   return;
	   }
	   
	   PlayerDetail player = getPlayerDetails(userName);
	   int index = arrPlayerDetails.indexOf(player);
//	   if (currentPlayerIndex == index)
	   
	   System.out.println("****CurrentPlay is :" +currentPlayuserName);
	   System.out.println("****playStartUsernameis :" +playStartUsername);	
	   
	   if (currentPlayuserName.equalsIgnoreCase(player.userName))		   
	   {
		   System.out.println("in processUserLeft currentGameplay "+player.userName+" left");
		   index++;
		   if (index >= arrPlayerDetails.size()) index =0;
		   
		   // SOME ISSUE HERE..
		   PlayerDetail nextPlayer = null;
		   while (index < arrPlayerDetails.size())
		   {
			   System.out.println("user.. idx "+index);
			   PlayerDetail nextTurn = arrPlayerDetails.get(index);

			   if (nextTurn.isInGame==true && nextTurn.didPass == false)
			   {
				   nextPlayer = nextTurn;
				   System.out.println("LOOP BREAK.... with "+nextPlayer.playerName);
				   break;
			   }
			   System.out.println("USER NOT SATISFY CONDITION.. with "+nextTurn.playerName);			   
			   index++;
			   if (index >= arrPlayerDetails.size()) index =0;		   
		   }

		   currentPlayerIndex = arrPlayerDetails.indexOf(nextPlayer);
		   currentPlayuserName = nextPlayer.userName;
		   centerTableAmount += player.raiseChipAmount;

		   JSONObject passObj = new JSONObject();
		   try
		   {
			   passObj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeNewUserTurn);
			   passObj.put(CardsConstants.kUserNameKey,nextPlayer.userName);

			   String strObj = passObj.toString();
			   //				System.out.println("Fold User left Turn : "+strObj);

			   this.gameRoom.BroadcastChat(CardsConstants.SERVER_NAME, strObj);
		   }
		   catch(JSONException e)
		   {
			   e.printStackTrace();
		   }
	   }
	   else
	   {
		   System.out.println("in processUserLeft normal "+player.userName+" left");
		   centerTableAmount += player.raiseChipAmount;
	   }
   }
   /**
	Get All Players who are in game , except fold players.
    ***/
   public void onTimerTick(long time)  
   {
//	   System.out.println("timer tick method.....");
   }
   private int getTotalPlayingPlayer()
   {
	   int count=0;
		for(PlayerDetail player : arrPlayerDetails)
		{
//			System.out.println(player.playerName+" isInGameValue "+player.isInGame);
//			System.out.println(player.playerName+" didPassValue "+player.didPass);
			
			if (player.isInGame==true && player.didPass==false)
			{
				count++;	
			}
		}
		return count;   
   }
   private void resetFoldFlags()
   {
	   for(PlayerDetail player : arrPlayerDetails)
	   {
		   player.isInGame= true;
	   }
   }
   private void resetPassFlags()
   {
	   for(PlayerDetail player : arrPlayerDetails)
	   {
//		   System.out.println("ResetPass for "+player.userName);
		   player.didPass = false;
	   }
   }
   /***
    *Check Weather minimum users available to next round
    ***/
   private boolean checkValidUsersGamePlay() 
   {
//	   arrWaitingPlayerDetails;
	   System.out.println("checkValidUsersGamePlay");
	   int count = 0;
	   for(PlayerDetail player : arrPlayerDetails)
	   {
		   if (player.isInGame && player.totalChipAmount==0)
		   {
//			   player.isInGame = false;
//			   arrPlayerDetails.remove(player);
//			   arrWaitingPlayerDetails.add(player);
			   count++;
//			   userList.remove(player.user);
//			   waitingUsers.add(player.user);
		   }
	   }
	   
	   if ((getTotalPlayingPlayer()-count)>1)
	   {
		   return true;
	   }
	   else
	   {
		   System.out.println("New Game round will not start ");		   
		   return false;
	   }
   }
   
   /**
    
    isNewRound indicates wheather its new round of first gamepaly
    @param if TRUE, server side center tble amount will add
    if FALSE, server side center table amount will add and will send msg to all clients
    */
   private void sendInitialBetMessage(boolean isNewRound)
   {
//	   if (arrPlayerDetails.size()>1)
	   if (checkValidUsersGamePlay())		   
	   {
		   System.out.println("***Getting Bet Amount "+callAmountDefault);
		   //kTypeGetTableBet

		   //Integer value = (Integer)this.gameRoom.getProperties().get(CardsConstants.kRoomDefaultAmount);
		   if ((gameRound+1) <=1)// change here added condition on 17 March 2015
		   {
			   long callAmount = callAmountDefault;// value.intValue(); 
			   int amount = 0;
			   for(PlayerDetail player : arrPlayerDetails)
			   {
				   if (player.isInGame)
				   {
					   player.totalChipAmount-= callAmount;
					   amount+=callAmount;	
					   player.ChipsValue = Long.toString(player.totalChipAmount);
					   System.out.println(player.userName+" is in play inGameFlag");				   
				   }
				   else
				   {
					   System.out.println(player.userName+" is in play but some issue to set inGameFlag");
				   }
			   }
			   	   
			   if (!isNewRound)
			   {
				   System.out.println("before value CentarTableAmount : "+centerTableAmount);		   		   
				   centerTableAmount=amount;
				   System.out.println("CentarTableAmount : "+centerTableAmount);		   
				   JSONObject obj = new JSONObject();
				   try
				   {
					  obj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeGetTableBet);			  		   
					  obj.put(CardsConstants.kCurrentTableAmountKey, centerTableAmount);
					  String strObj = obj.toString();
					  this.gameRoom.BroadcastChat(CardsConstants.SERVER_NAME, strObj);
				   }
				   catch (JSONException e)
				   {
					   e.printStackTrace();
				   }
			   }
			   else
			   {
				   System.out.println("before CentarTableAmount in new "+centerTableAmount);
				   centerTableAmount+=amount;
				   System.out.println("CentarTableAmount in new: "+centerTableAmount);		   
			   }
		   }
	   }
	   else
	   {
		   // cleanup all data here.
	   }
   }
   
   private void sendInitialGameState()
   {
	   JSONObject obj  = new JSONObject();
	   try
	   {
		   obj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeInitStatus);
		   obj.put(CardsConstants.kCurrentTableAmountKey, centerTableAmount);
		   obj.put(CardsConstants.kPlayerDetailsCount, arrPlayerDetails.size());
		   
		   JSONArray playingObj = new JSONArray();			  
		   for(PlayerDetail player : arrPlayerDetails)
		   {
				  JSONObject objplayer = new JSONObject();
				  objplayer.put(CardsConstants.kUserNameKey, player.userName);
				  
				  objplayer.put(CardsConstants.kUserName, player.playerName);
				  objplayer.put(CardsConstants.kUserTotalHands, player.totalPlay);
				  objplayer.put(CardsConstants.kUserTotalWonHands, player.totalWon);
				  objplayer.put(CardsConstants.kUserGamesChipsValue, player.ChipsValue);
				  objplayer.put(CardsConstants.kUserAccountID, player.userID);
				  objplayer.put(CardsConstants.kUserGiftID, player.defaultGiftID);
				  objplayer.put(CardsConstants.kUserImageUrl, player.profileImageUrl);
				  playingObj.put(objplayer);
		   }
		   obj.put(CardsConstants.kUserListKey, playingObj);
		   
		   String msg = obj.toString();
//		   System.out.println("sended initi msg "+msg);
		   this.gameRoom.BroadcastChat(CardsConstants.SERVER_NAME, msg);
	   }
	   catch (JSONException e)
	   {
		   System.out.println("error "+e.getMessage());
		   e.printStackTrace();   
	   }
   }
   private void sendGameStatus()
   {
	   // notify to start game
	   JSONObject obj  = new JSONObject();
	   try
	   {	
		  obj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeGameStatus);
		  obj.put(CardsConstants.kCurrentTableAmountKey, centerTableAmount);
		  
		  if (currentPlayerIndex!=-1)
		  {
//			  obj.put(CardsConstants.kCurrentUserTurnName, arrPlayerDetails.get(currentPlayerIndex).userName);
			  obj.put(CardsConstants.kCurrentUserTurnName, currentPlayuserName);
		  }
		  else
		  {
			  obj.put(CardsConstants.kCurrentUserTurnName, arrPlayerDetails.get(0).userName);			  
		  }
		  
		  JSONObject playingObj = new JSONObject();
		  
		  for(PlayerDetail player : arrPlayerDetails)
		  {
			  JSONObject objplayer = new JSONObject();
			  objplayer.put(CardsConstants.kUserCallAmountKey, player.raiseChipAmount);
			  
			  objplayer.put(CardsConstants.kUserName, player.playerName);
			  objplayer.put(CardsConstants.kUserTotalHands, player.totalPlay);
			  objplayer.put(CardsConstants.kUserTotalWonHands, player.totalWon);
			  objplayer.put(CardsConstants.kUserGamesChipsValue, player.ChipsValue);
			  objplayer.put(CardsConstants.kUserAccountID, player.userID);
			  objplayer.put(CardsConstants.kUserGiftID, player.defaultGiftID);
			  objplayer.put(CardsConstants.kUserImageUrl, player.profileImageUrl);
			  
			  JSONArray cardList = new JSONArray();
			  for(Card card : player.cardList)
			  {
				  int cardValue = card.getCards().getCardValue() + card.getSuits().getSuitsValue();
				  cardList.put(cardValue);
			  }
			  objplayer.put(CardsConstants.kUserCardsKey, cardList);
//			  obj.put(player.userName, objplayer);
			  playingObj.put(player.userName, objplayer);
		  }
		  obj.put(CardsConstants.kUserDetailsPlaying, playingObj);
		  
		  JSONObject waitingObj = new JSONObject();

		  for(PlayerDetail player : arrWaitingPlayerDetails)
		  {
			  JSONObject objplayer = new JSONObject();
			  objplayer.put(CardsConstants.kUserCallAmountKey, player.raiseChipAmount);
			  
			  objplayer.put(CardsConstants.kUserName, player.playerName);
			  objplayer.put(CardsConstants.kUserTotalHands, player.totalPlay);
			  objplayer.put(CardsConstants.kUserTotalWonHands, player.totalWon);
			  objplayer.put(CardsConstants.kUserGamesChipsValue, player.ChipsValue);
			  objplayer.put(CardsConstants.kUserAccountID, player.userID);
			  objplayer.put(CardsConstants.kUserGiftID, player.defaultGiftID);
			  objplayer.put(CardsConstants.kUserImageUrl, player.profileImageUrl);
			  
			  waitingObj.put(player.userName, objplayer);
		  }
		  obj.put(CardsConstants.kUserDetailsWaiting,waitingObj);
		  JSONArray cardList = new JSONArray();
		  for (Integer value: centerTableCard)
		  {
			  cardList.put(value.intValue());
		  }
		  obj.put(CardsConstants.kUserCardsKey, cardList);
		  String strObj = obj.toString();
		  System.out.println("GameStatus : "+strObj);
		  this.gameRoom.BroadcastChat(CardsConstants.SERVER_NAME, strObj);
	   }
	   catch (JSONException e)
	   {
		   e.printStackTrace();   
	   }
   }
   
   private ArrayList<Integer> convertJsonToCardList(JSONArray arrCardObj)
   {
	    ArrayList<Integer> cardsList = new ArrayList<Integer>();
//	    System.out.println("****Card Values Start**** TOTAL "+arrCardObj.length());
	    for(int i=0;i<arrCardObj.length();i++)
	    {
	    	Integer value;
	    	try {
	    		value = arrCardObj.getInt(i);
//	    		System.out.println("****Card Values "+value);
	    		cardsList.add(value);
	    	} catch (Exception e) {
//	    		System.out.println("******\n***....Inside Card Convert error.....*****\n*****");
				e.printStackTrace();
			}
	    }
//	    System.out.println("****Card Values END****");	    
	    return cardsList;
   }
   private PlayerDetail getViewersPlayerDetails(String username)
   {
	   PlayerDetail details = null;
	   for(PlayerDetail player : arrViewers)
	   {
		   if (player.getUsernme().equalsIgnoreCase(username))
		   {
			   details = player; 
			   break;
		   }
	   }
	   return details;	
   }
   private PlayerDetail getWaitingPlayerDetails(String username)
   {
	   PlayerDetail details = null;
	   for(PlayerDetail player : arrWaitingPlayerDetails)
	   {
		   if (player.getUsernme().equalsIgnoreCase(username))
		   {
			   details = player; 
			   break;
		   }
	   }
	   return details;	   	   
   }
   private PlayerDetail getPlayerDetails(String username)
   {
	   PlayerDetail details = null;
	   for(PlayerDetail player : arrPlayerDetails)
	   {
		   if (player.getUsernme().equalsIgnoreCase(username))
		   {
			   details = player; 
			   break;
		   }
	   }
	   return details;
   }
   private void clearAllUserDetails()
   {
	   System.out.println("****clearAllUserDetails*****");
	   for(IUser user : userList)
	   {
		   System.out.println("USER> : "+user.getName());   
	   }
	   cardDeck = null;
	   cardDeck = new Deck();
	   centerTableCard.clear();
	   lastWinner = null;
	   schedualStarted = false;
	   if (userList.size()<=1)
	   {
		   currentPlayerIndex=-1;
		   currentPlayuserName = "";
		   nextGamePlayTurn = 0;		   
		   GAME_STATUS = CardsConstants.STOPPED;
	   }
	   
	   if (userList.size()==0)
	   {
//		  arrViewers.clear();
//		  waitingUsers.clear();
//		  
//		  arrWaitingPlayerDetails.clear();
//		  arrViewers.clear();
		  
		  centerTableAmount = 0;
		  nextGamePlayTurn = 0;
		  System.out.println("Center Table Zero Now");
	   }
   }
}