/**
 * 
 */
package rummydemo;

import static pokercard.Suit.*;
import static pokercard.Rank.*;

import computerpackage.ComputerDetails;
import pokercard.*;

import com.shephertz.app42.server.domain.Room;
import com.shephertz.app42.server.domain.User;
import com.shephertz.app42.server.domain.VirtualUser;
import com.shephertz.app42.server.idomain.BaseRoomAdaptor;
import com.shephertz.app42.server.idomain.HandlingResult;
import com.shephertz.app42.server.idomain.IRoom;
import com.shephertz.app42.server.idomain.ITurnBasedRoom;
import com.shephertz.app42.server.idomain.IUser;
import com.shephertz.app42.server.idomain.IZone;
import com.shephertz.app42.server.message.WarpRequestTypeCode;
import com.shephertz.app42.server.message.WarpResponseResultCode;

import java.sql.Time;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONWriter;

import pokercard.Card;
import pokercard.Card.*;

/**
 * @author
 * 
 */
@SuppressWarnings("unused")
public class RoomExtension extends BaseRoomAdaptor {
	/*** iVars Declaration ***/
	private IZone izone;

	GameDetails currentGameDetails;
	Timer userCheckTimer;
	Timer timeoutTimer;
	long startTimer;
	String lastWinner;
	private final int MSecs = 1000;

	public long currentBetAmount;
	Timer aiTimer;

	/*** End iVars Declaration ***/

	/******** Methods *******/

	public RoomExtension(IZone izone, IRoom room) {
		this.izone = izone;
		currentGameDetails = new GameDetails(room);
		currentGameDetails.roomExtension = this;
	}

	void printAllRoomUsersList() {
		System.out.println("*** listing roomDetails.getJoinedUsers");
		for (IUser player : currentGameDetails.roomDetails.getJoinedUsers()) {
			System.out.println("* name :" + player.getName());
		}
		System.out.println("*** ending roomDetails.getJoinedUsers");
	}

	void printAllUsers() {
		System.out.println("*** listing gameUserList");
		for (PlayerDetail player : currentGameDetails.gameUserList) {
			System.out.println("* name :" + player.userName);
		}
		System.out.println("*** ending listing gameUserList");
		// for ( )
	}

	/** schedule for checking user status in both listing */

	void checkUserComparision() {
		List<IUser> joinList = currentGameDetails.roomDetails.getJoinedUsers();
		if (!(currentGameDetails.isComputerPlayerInGame())
				&& (joinList.size() != currentGameDetails.gameUserList.size())) {
			// computer player is not in game and count does not match for
			// players
			System.out
					.println(" ** checkUserComparision computer not exist and count does not match");
			printAllRoomUsersList();
			printAllUsers();
			// clear player who does not exist in game.
		} else if (currentGameDetails.isComputerPlayerInGame()
				&& joinList.size() != currentGameDetails.gameUserList.size() - 1) {
			System.out
					.println(" ** checkUserComparision computer exist and count does not match after computer deduct");

			printAllRoomUsersList();
		} else {
			System.out.println("*** user sync fine *** " + new Date());
		}

	}

	public boolean endGameWinner(String winnerName) {
		currentGameDetails.currentGameStatus = GameStatus.GameEnd;
		PlayerDetail winnerPlayer = null;
		lastWinner = winnerName;

		this.unScheduleUserTimeOut();

		ArrayList<PlayerDetail> arrWaitingUsersList = currentGameDetails
				.getPlayerWaitingList();
		ArrayList<PlayerDetail> arrPlayerDetails = currentGameDetails
				.getPlayerPlayingList();

		for (PlayerDetail playerDetails : arrPlayerDetails) {
			if (playerDetails.userName.equals(winnerName)) {
				winnerPlayer = playerDetails;
				break;
			}
		}

		if (winnerPlayer != null) {

			try {
				final String winName = winnerName;
				long amount = 0;
				long winnerRaiseAmount = winnerPlayer.raiseChipAmount;
				// increase won count of winner
				long won = Long.parseLong(winnerPlayer.totalWon);
				won++;
				winnerPlayer.totalWon = Long.toString(won);

				for (PlayerDetail player : currentGameDetails.gameUserList) {
					if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
							.getPlayerStatusValue()) {
						player.cardList.clear(); // clear cards for players if
													// any
						// player.userPlayStatus = PlayerStatus.Playing;
						player.didBet = false;
					}
					player.userActionStatus = PlayerActionStatus.PLAYActionNone;
				}

				// manage bets distribution and raise distribution.
				if (winnerPlayer.totalChipAmount == 0) {
					for (PlayerDetail player : currentGameDetails.gameUserList) {
						if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
								.getPlayerStatusValue()) {
							// manage raise distribution
							long amount1 = player.raiseChipAmount;
							if (amount1 > winnerRaiseAmount) {
								amount1 -= winnerRaiseAmount;
								player.totalChipAmount += amount;
								player.ChipsValue = Long
										.toString(player.totalChipAmount);
								amount += winnerRaiseAmount;
							} else if (amount1 <= winnerRaiseAmount) {
								amount += amount1;
							}

							// manage betting round bets distribution.
							long userBets = player.allBetRoundAmount;

							if (userBets > winnerPlayer.allBetRoundAmount) {
								long differAmount = userBets
										- winnerPlayer.allBetRoundAmount;
								player.totalChipAmount += differAmount;
								player.ChipsValue = Long
										.toString(player.totalChipAmount);

								currentGameDetails.centerTableAmount -= differAmount;
							} else if (userBets <= winnerPlayer.allBetRoundAmount) {

							}
							player.raiseChipAmount = 0;
							player.roundBetAmount = 0;
						}
					}
				} else {
					for (PlayerDetail player : currentGameDetails.gameUserList) {
						if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
								.getPlayerStatusValue()) {
							amount += player.raiseChipAmount;
							player.raiseChipAmount = 0;
							player.roundBetAmount = 0;
							player.ChipsValue = Long
									.toString(player.totalChipAmount);
							player.allBetRoundAmount = 0;
						}
					}
				}

				// System.out.println("table and winner amount "+currentGameDetails.centerTableAmount+" and chips amount "+amount);
				amount += currentGameDetails.centerTableAmount;

				winnerPlayer.totalChipAmount += amount;
				winnerPlayer.ChipsValue = Long
						.toString(winnerPlayer.totalChipAmount);

				// System.out.println("GameWinner is "+winnerPlayer.userName);
				// System.out.println("GameWinner total amount "+winnerPlayer.totalChipAmount);

				currentGameDetails.centerTableAmount = 0;
				currentGameDetails.resetGameTable();

				long roomAmount = currentGameDetails.getDefaultCallRaiseAmunt();
				for (PlayerDetail player : currentGameDetails.gameUserList) {
					if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
							.getPlayerStatusValue()) {
						if (player.totalChipAmount > 0
								&& player.totalChipAmount >= roomAmount) {
							player.userPlayStatus = PlayerStatus.Playing;
						} else {
							player.userPlayStatus = PlayerStatus.Waiting;

						}
					}
					player.allBetRoundAmount = 0;
				}

				ArrayList<IUser> playersToRemove = new ArrayList<IUser>();

				for (PlayerDetail player : currentGameDetails.gameUserList) {

					if (player.totalChipAmount > 0
							&& player.totalChipAmount >= roomAmount) {
					} else {

						if(player.isComputerPlayer){
							playersToRemove.add(player.user);

						}
					}

				}

				for(IUser user : playersToRemove){
					currentGameDetails.roomDetails.removeUser(user, true);
					onUserLeaveRequest(user);
				}

				System.out.println("arrWaitingUsersList.size() : " + arrWaitingUsersList.size());

				for (PlayerDetail player : arrWaitingUsersList) {
					System.out.println("player.totalChipAmount : " + player.totalChipAmount + "  player.username : " + player.userName);
					if (player.totalChipAmount > 0) {
						player.userPlayStatus = PlayerStatus.Playing;
						player.raiseChipAmount = 0;
						player.roundBetAmount = 0;
						player.allBetRoundAmount = 0;
					}
				}

				arrPlayerDetails = currentGameDetails.getPlayerPlayingList();

				currentGameDetails.centerTableCard.clear();

				// System.out.println("****");
				// for(PlayerDetail player : currentGameDetails.gameUserList)
				// {
				// System.out.println("****Name: "+player.playerName);
				// System.out.println("****userPlayStatus: "+player.userPlayStatus.getPlayerStatusValue());
				// System.out.println("****userActionStatus: "+player.userActionStatus.getPlayerActionValue());
				// System.out.println("****ChipsValue: "+player.ChipsValue);
				// }
				// System.out.println("**++++**");


				if (arrPlayerDetails.size() >= 2) {
					Timer newTimer = new Timer();
					TimerTask task = new TimerTask() {
						@Override
						public void run() {

							ArrayList<PlayerDetail> localarrPlayerDetails = currentGameDetails
									.getPlayerPlayingList();

							// System.out.println("****");
							// for(PlayerDetail player : localarrPlayerDetails)
							// {
							// System.out.println("**** NAMe:  "+player.playerName);
							// }
							// System.out.println("****");

							if (currentGameDetails.currentGameStatus
									.getGameStatusValue() > GameStatus.GameRunning
									.getGameStatusValue()) {
								// distributeCardsForGame(winName); // comment
								// on 19 march 2015
								if (localarrPlayerDetails.size() >= 2) {
									// added on 19 march 15
									int nextIdx = currentGameDetails
											.getNextPlayerTurn();
									PlayerDetail player = currentGameDetails.gameUserList
											.get(nextIdx);
									String name = player.userName;

									// System.out.println("//**New Game Begins with "+name+" !!");
									distributeCardsForGame(name);
									// added end on 19 march 15
								}
							}
						}
					};
					newTimer.schedule(task, 5 * MSecs);
				}




			} catch (Exception e) {

				System.out.println("endGameWinner exception");
				e.printStackTrace();

				System.out
						.println("SomeIssue in parsing End Game msg or Array>>: "
								+ e.getLocalizedMessage());
				e.printStackTrace();
			}
			return true;
		}
		return true;
	}

	public  void callDistributeCardsForGameByAI(String startingPlayer){
		distributeCardsForGame(startingPlayer);

	}

	private void distributeCardsForGame(String startingPlayer) {
		// notify to start game
		JSONObject obj = new JSONObject();
		try {
			obj.put(CardsConstants.kSuccessKey, "1");
			JSONObject objUserList = new JSONObject();
			ArrayList<PlayerDetail> userList = currentGameDetails
					.getPlayerPlayingList();
			System.out.print("currentGameDetails.getPlayerPlayingList().size : " + currentGameDetails.getPlayerPlayingList().size());

			int u = 0;
			for (PlayerDetail players : userList) {
				u++;

				JSONArray objStr = new JSONArray();
				ArrayList<Card> cardList = new ArrayList<Card>();

				long total = Long.parseLong(players.totalPlay.toString());
				total++;
				System.out.print("players.totalPlay : " + players.totalPlay);

				players.totalPlay = Long.toString(total);


				for (int i = 0; i < CardsConstants.MAX_NO_OF_CARDS; i++) {

					Card card = currentGameDetails.getNewCard();
//					if(players.userName.equalsIgnoreCase("k1")){
//						card = new Card(CardValue.values()[i], Suit.values()[1]);
//
//					}
//					else if(u == userList.size()){
//						card = new Card(CardValue.values()[i], Suit.values()[2]);
//
//					}

					int cardValue = card.getCards().getCardValue()
							+ card.getSuits().getSuitsValue();
					objStr.put(cardValue);

					cardList.add(card);
				}

				if (cardList.size() > 0) {
					players.setCardList(cardList);
				}
				objUserList.put(players.userName, objStr);
			}

			obj.put(CardsConstants.kUserListKey, objUserList);
			obj.put(CardsConstants.kTypeMessageKey,
					CardsConstants.kTypeGameStartWithCard);
			// obj.put(CardsConstants.kPlayerTurnKey,
			// userList.get(0).getName());
			obj.put(CardsConstants.kPlayerTurnKey, startingPlayer);

			ArrayList<PlayerDetail> newuserList = currentGameDetails
					.getPlayerPlayingList();
			if (newuserList.size() > 1) {

				currentGameDetails.currentGameStatus = GameStatus.GameBettingRound;

				PlayerDetail startPlayer = currentGameDetails
						.getPlayerDetails(startingPlayer);
				currentGameDetails.playStartUsername = startPlayer.userName;

				currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
						.indexOf(startPlayer);
				currentGameDetails.totalGamerounds = 1;
				currentGameDetails.currentPlayuserName = startPlayer.userName;
				currentGameDetails.passcounts = 0;
				currentGameDetails.centerTableAmount = 0;
				currentGameDetails.startPlayUserName = startPlayer.userName;
				lastWinner = null;

				currentGameDetails.roomDetails.BroadcastChat(
						CardsConstants.SERVER_NAME, obj.toString());

				this.sendInitialBetMessage(false); // get bets from all users.

				// skiping beting round if allin.
				if (currentGameDetails.allUserAllIn()) {
					currentGameDetails.currentGameStatus = GameStatus.GameRunning;

					if (currentGameDetails.totalGamerounds <= 3) {
						currentGameDetails.totalGamerounds = 4;
					}
				}

				// System.out.println("**** NEW GAME START%%%");
				// System.out.println("****");
				// for(PlayerDetail player : currentGameDetails.gameUserList)
				// {
				// System.out.println("****Name: "+player.playerName);
				// System.out.println("****userPlayStatus: "+player.userPlayStatus.getPlayerStatusValue());
				// System.out.println("****userActionStatus: "+player.userActionStatus.getPlayerActionValue());
				// System.out.println("****ChipsValue: "+player.ChipsValue);
				// }
				// System.out.println("**++++**");

				this.scheduleUserTimeOut(CardsConstants.UserTimeOut
						+ CardsConstants.GameStartDely);
			}

			final PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);
			if(myDetails.isComputerPlayer == true) {
				int delay = (int)(CardsConstants.kDealerAnimationTime * MSecs);
				System.out.println("computer turn to bet delay : " + delay);

				int delayInCardDis = 4 * currentGameDetails.getTotalPlayingPlayer();

				Timer timer = new Timer();
				TimerTask delayedThreadStartTask = new TimerTask() {
					@Override
					public void run() {

						System.out.println("computer turn to bet");

						fireEvent(CardsConstants.AIFireEvent.Bet);

					}
				};
				timer.schedule(delayedThreadStartTask, delayInCardDis * MSecs);

			}

			
			currentGameDetails.processComputerPlayerStates(10);



		} catch (JSONException e) {

			System.out.println("distributeCardsForGame Exception");

			e.printStackTrace();
		}
	}

	public void handleChatRequest(IUser sender, String message,
			HandlingResult result) {
//		 System.out.println("Sender :: "+sender.getName());
		// System.out.println("Message:: "+message);
		if (!sender.getName().equalsIgnoreCase(CardsConstants.SERVER_NAME))
		{
			try {
				JSONObject jsonObject = new JSONObject(message);
				int response = jsonObject
						.getInt(CardsConstants.kTypeMessageKey);

				String strSender = jsonObject
						.getString(CardsConstants.kUserNameKey);
				String strNextUser = null;
				PlayerDetail nextuser = null;

				if (jsonObject.has(CardsConstants.kUserTurnKey)) {
					strNextUser = jsonObject
							.getString(CardsConstants.kUserTurnKey);
					nextuser = currentGameDetails.getPlayerDetails(strNextUser);
				}

				if (currentGameDetails.arrIgnoreList.size() > 0) {
					boolean needLeave = false;
					for (String name : currentGameDetails.arrIgnoreList) {
						if (name.equalsIgnoreCase(sender.getName())) {
							needLeave = true;
							break;
						}
					}
					if (needLeave
							&& response != CardsConstants.kTypeResumeRequest) {

						result.code = WarpResponseResultCode.CONNECTION_ERROR_RECOVERABLE; // WarpResponseResultCode.BAD_REQUEST;
						result.sendResponse = false;
						// currentGameDetails.arrIgnoreList.remove(sender.getName());
						// System.out.println("***ingnore active ");
						// System.out.println("***ingnore active for player..."+sender.getName());
						return;
					} else {
						currentGameDetails.arrIgnoreList.clear();
					}
				}

				PlayerDetail playerSender = currentGameDetails
						.getPlayerDetails(strSender);
				switch (response) {
				case CardsConstants.kTypeCardDistribution:
				case CardsConstants.kTypeCardDistributionCall:
				case CardsConstants.kTypeCardDistributionRaise: {
					this.unScheduleUserTimeOut();
					this.handleClientPlayEvents(sender, message);
					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
				}
					break;
				case CardsConstants.kTypeUserTimeOut:
				case CardsConstants.kTypeUserPass: {
					this.unScheduleUserTimeOut();
					this.handleClientPassTimeout(sender, message);
					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
				}
					break;
				case CardsConstants.kTypeWinner: {
					lastWinner = sender.getName();
					// Timer newTimerTask = new Timer();
					// TimerTask task = new TimerTask() {
					// @Override
					// public void run() {
					// TODO Auto-generated method stub
					endGameWinner(lastWinner);
					// }
					// };
					// newTimerTask.schedule(task, 1*MSecs);
				}
					break;
				case CardsConstants.kTypeFold: {
					this.unScheduleUserTimeOut();
					this.handleClientFoldEvents(sender, message);
					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
				}
					break;
				case CardsConstants.kTypeUserCheck: {
					this.unScheduleUserTimeOut();
					System.out.println("handleClientCheck");
					this.handleClientCheck(sender, message);
					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
				}
					break;
				case CardsConstants.kTypeUserStatus: {
					if (playerSender != null) {
						String playerName = jsonObject
								.getString(CardsConstants.kUserName);
						String total = jsonObject
								.getString(CardsConstants.kUserTotalHands);
						String totalWon = jsonObject
								.getString(CardsConstants.kUserTotalWonHands);
						String chipsValue = jsonObject
								.getString(CardsConstants.kUserGamesChipsValue);
						String userID = jsonObject
								.getString(CardsConstants.kUserAccountID);
						String giftID = jsonObject
								.getString(CardsConstants.kUserGiftID);
						String imageUrl = jsonObject
								.getString(CardsConstants.kUserImageUrl);

						// playerSender.totalChipAmount =
						// Long.parseLong(chipsValue);
						// playerSender.ChipsValue = chipsValue;

						playerSender.playerName = playerName;
						// playerSender.totalPlay= total;
						// playerSender.totalWon= totalWon;
						playerSender.userID = userID;
						playerSender.defaultGiftID = giftID;
						playerSender.profileImageUrl = imageUrl;
					}

				}
					break;
				case CardsConstants.kTypeUserChat: {
					// System.out.println("Receive Chat Message");
				}
					break;
				case CardsConstants.kTypeSendGift: {
					// System.out.println("Receive Gift Message");
				}
					break;
				case CardsConstants.kTypeBetRoundBet: {
					this.unScheduleUserTimeOut();
					this.handleClientBettingBet(sender, message);
					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
				}
					break;
				case CardsConstants.kTypeBetRoundCheck: {
					this.handleClientBettingCheck(sender, message);
					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
				}
					break;
				case CardsConstants.kTypeBetRoundRaise: {
					this.unScheduleUserTimeOut();
					this.handleClientBettingRaise(sender, message);
					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
				}
					break;
				case CardsConstants.kTypeJoinRequest: {
					if (playerSender != null) {
						String playerName = jsonObject
								.getString(CardsConstants.kUserName);
						String total = jsonObject
								.getString(CardsConstants.kUserTotalHands);
						String totalWon = jsonObject
								.getString(CardsConstants.kUserTotalWonHands);
						String chipsValue = jsonObject
								.getString(CardsConstants.kUserGamesChipsValue);
						String userID = jsonObject
								.getString(CardsConstants.kUserAccountID);
						String giftID = jsonObject
								.getString(CardsConstants.kUserGiftID);
						String imageUrl = jsonObject
								.getString(CardsConstants.kUserImageUrl);

						playerSender.totalChipAmount = Long
								.parseLong(chipsValue);
						playerSender.playerName = playerName;
						playerSender.totalPlay = total;
						playerSender.totalWon = totalWon;
						playerSender.ChipsValue = chipsValue;
						playerSender.userID = userID;
						playerSender.defaultGiftID = giftID;
						playerSender.profileImageUrl = imageUrl;

						final ArrayList<PlayerDetail> arrPlayers = currentGameDetails
								.getPlayerPlayingList();
						if (arrPlayers.size() >= 2) {
							playerSender.userPlayStatus = PlayerStatus.Waiting;
						} else {
							playerSender.userPlayStatus = PlayerStatus.Playing;
							// added on 25 march 15
							int nextIdx = currentGameDetails
									.getNextPlayerTurn();
							PlayerDetail players = currentGameDetails.gameUserList
									.get(nextIdx);
							String name = players.userName;
							// System.out.println("//**New Game Begins with "+name+" !!");
							distributeCardsForGame(name);
							// added end on 25 march 15
						}
					}
				}
					break;

				case CardsConstants.KTypeExtendTimer: {
					long timerelapsed = System.currentTimeMillis();
					long secsRemain = (timerelapsed - startTimer) / 1000;
					// System.out.println("***Timer remain "+secsRemain);
					// System.out.println("***extends Timer with "+secsRemain+CardsConstants.ExtendTimer);
					this.scheduleUserTimeOut((secsRemain + CardsConstants.ExtendTimer));
				}
					break;
				case CardsConstants.kTypeResumeRequest: {
					// System.out.println(".. CardsConstants.kTypeResumeRequest: from "+sender.getName());
					currentGameDetails.sendResumeGameStauts(sender);
				}
					break;
				case CardsConstants.kTypeServerUserTimeOut: {
					// System.out.println("kTypeServerUserTimeOut Message receive .._.. "+message);
					// System.out.println("user info+++  "+sender.getCustomData());
				}
					break;
				case CardsConstants.kTypePingRequest: {
//					 System.out.println("**kTypePingRequest Message receive .._.. "+sender.getName());
				}
					break;
				default: {
					// System.out.println("Unknown type Message receive .._.. "+message);
					// System.out.println("user info+++  "+sender.getCustomData());
				}
					break;
				}
			} catch (Exception e) {
				// message
				System.out.println("Unknown type exception .._.. "
						+ e.getLocalizedMessage());
				System.out.println("actual message..... " + message);
			}
		}
	}

	private void handleClientPlayEvents(IUser sender, String message) {
		try {


			JSONObject jsonObject = new JSONObject(message);
			int response = jsonObject.getInt(CardsConstants.kTypeMessageKey);

			String strSender = jsonObject
					.getString(CardsConstants.kUserNameKey);
			String strNextUser = null;
			PlayerDetail nextuser = null;

			if (jsonObject.has(CardsConstants.kUserTurnKey)) {
				strNextUser = jsonObject.getString(CardsConstants.kUserTurnKey);
				nextuser = currentGameDetails.getPlayerDetails(strNextUser);

			}
			PlayerDetail playerSender = currentGameDetails
					.getPlayerDetails(strSender);

			int index = currentGameDetails.gameUserList.indexOf(nextuser);
			currentGameDetails.currentPlayIndex = index;
			currentGameDetails.currentPlayuserName = nextuser.userName;

			currentGameDetails.totalGamerounds = jsonObject
					.getInt(CardsConstants.kGameRoundKey);

			if (jsonObject.has(CardsConstants.kUserCallAmountKey)) {
				int amount = jsonObject
						.getInt(CardsConstants.kUserCallAmountKey);
				playerSender.raiseChipAmount += amount;
				playerSender.totalChipAmount -= amount;
				if (playerSender.totalChipAmount < 0) {
					// System.out.println("** Name: "+playerSender.playerName+" has chips zero > "+playerSender.totalChipAmount);
					playerSender.totalChipAmount = 0;
				}
				playerSender.ChipsValue = Long
						.toString(playerSender.totalChipAmount);
			}

			if (currentGameDetails.centerTableCard.size() > 0) {
				HashMap<String, Object> newData = new HashMap<String, Object>();
				newData.put(CardsConstants.kLastCardDetails,
						currentGameDetails.centerTableCard);
				newData.put(CardsConstants.kLastUserName,
						currentGameDetails.playStartUsername);
				currentGameDetails.arrLastCardDetails.add(newData);
			}

			if (jsonObject.has(CardsConstants.kCurrentTableAmountKey)) {
				int tblamount = jsonObject
						.getInt(CardsConstants.kCurrentTableAmountKey);

				if (tblamount != currentGameDetails.centerTableAmount) {
					System.out
							.println("*client and server side center table ammount issue");
					System.out.println("*client table ammount " + tblamount);
					System.out.println("*server table ammount "
							+ currentGameDetails.centerTableAmount);
				}
			}

			// System.out.println("****** Parsing message Type in Play******"+response);

			// Maintain Play Count;
			long totalPlay = Long.parseLong(playerSender.totalPlay);
			// totalPlay++;
			playerSender.totalPlay = Long.toString(totalPlay);

			JSONArray arrCardList = jsonObject
					.getJSONArray(CardsConstants.kUserCardsKey);
			currentGameDetails.centerTableCard = convertJsonToCardList(arrCardList);

			currentGameDetails.passcounts = 0;
//			currentGameDetails.playStartUsername = sender.getName();

			if(!sender.getName().equalsIgnoreCase(strSender)){
				System.out.println("***** problem ***** sender.getName()" + sender.getName() + "    strSender : " + strSender);

			}

			currentGameDetails.playStartUsername = strSender;


			try {
				for (int i = 0; i < currentGameDetails.centerTableCard.size(); i++) {
//					playerSender.cardList.remove(0);
					int val = currentGameDetails.centerTableCard.get(i);
					CardValue cardValue = CardValue.values()[val % 100 - 1];
					Suit suitValue = Suit.values()[val/100 - 1];
					Card card = new Card(cardValue, suitValue);
					System.out.println("playerSender.cardToRemove Rank: " + cardValue.getCardValue() + "   suitValue : " + suitValue.getSuitsValue());
					int indexToRemove = playerSender.cardList.indexOf(card);

					for (int k = 0; k < playerSender.cardList.size(); k++){
						Card cardVal = playerSender.cardList.get(k);

						if(cardVal.getCards().getCardValue() == card.getCards().getCardValue() && cardVal.getSuits().getSuitsValue() == card.getSuits().getSuitsValue()){
							playerSender.cardList.remove(cardVal);
							break;

						}


					}

//					System.out.println("playerSender.indexToRemove : " + indexToRemove);
//					playerSender.cardList.remove(card);

				}

				if(jsonObject.has(CardsConstants.kUserCallAmountKey)){
					int betAmount = jsonObject.getInt(CardsConstants.kUserCallAmountKey);

					for(int i = 0; i< currentGameDetails.gameUserList.size(); i++){
						PlayerDetail playerDetail = currentGameDetails.gameUserList.get(i);
						if(betAmount > playerDetail.previousBetValueByPlayer)
							playerDetail.previousBetValueByPlayer = betAmount;
					}

				}



			} catch (Exception e) {
				System.out.println("Error in card play remove array");
				e.printStackTrace();
			}


			if(response == CardsConstants.kTypeCardDistributionRaise){
				fireEvent(CardsConstants.AIFireEvent.Call);
			}

			else if (!currentGameDetails.isBettingRound() && playerSender.cardList.size() > 0){
				fireEvent(CardsConstants.AIFireEvent.PlayCard);
			}


			// finally
			// {
			// System.out.println("**in Did Play playerSender "+playerSender.userName+" and raiseAmount "+playerSender.raiseChipAmount);
			// System.out.println("**in Did Play playerSender "+playerSender.userName+" and tableAmount "+playerSender.totalChipAmount);
			// System.out.println("**in Did Play PlayerNext  "+nextuser.userName+" and tableAmount "+nextuser.totalChipAmount);
			// }
		} catch (Exception e) {
			System.out.println("handleClientPlayEvents exception .._.. "
					+ e.getLocalizedMessage());
		}
	}

	private void handleClientPassTimeout(IUser sender, String message) {
		try {
			JSONObject jsonObject = new JSONObject(message);
			int response = jsonObject.getInt(CardsConstants.kTypeMessageKey);

			String strSender = jsonObject
					.getString(CardsConstants.kUserNameKey);
			String strNextUser = null;
			PlayerDetail nextuser = null;

			if (jsonObject.has(CardsConstants.kUserTurnKey)) {
				strNextUser = jsonObject.getString(CardsConstants.kUserTurnKey);
				nextuser = currentGameDetails.getPlayerDetails(strNextUser);

			}
			PlayerDetail playerSender = currentGameDetails
					.getPlayerDetails(strSender);
			int index = currentGameDetails.gameUserList.indexOf(nextuser);

			currentGameDetails.currentPlayIndex = index;
			currentGameDetails.currentPlayuserName = nextuser.userName;

			// centerTableAmount =
			// jsonObject.getInt(CardsConstants.kCurrentTableAmountKey);
			currentGameDetails.passcounts++;

			currentGameDetails.totalGamerounds = jsonObject
					.getInt(CardsConstants.kGameRoundKey);

			// System.out.println("******Checking Pass Message******");
			// System.out.println("PlaystartUserName  "+currentGameDetails.playStartUsername);
			// System.out.println("currentPlayuserName  "+currentGameDetails.currentPlayuserName);

			// System.out.println("passCounts:  "+currentGameDetails.passcounts);

			playerSender.userActionStatus = PlayerActionStatus.PLAYActionPass;

			int totalActivePlayer = currentGameDetails.getTotalPlayingPlayer();
			// System.out.println("totalPlayers:  "+ totalActivePlayer);
			// System.out.println("currentGameDetails.playStartUsername... "+currentGameDetails.playStartUsername);
			// System.out.println("currentGameDetails.currentPlayuserName... "+currentGameDetails.currentPlayuserName);
			// System.out.println("(totalActivePlayer-1)... "+(totalActivePlayer-1));

			if (currentGameDetails.playStartUsername
					.equalsIgnoreCase(currentGameDetails.currentPlayuserName)
					&& (totalActivePlayer - 1) == 0) {
				// Working here on new round.....
				currentGameDetails.passcounts = 0;
				if (currentGameDetails.centerTableCard.size() > 0) {
					currentGameDetails.resetUserGameActions();
					 System.out.println(":::>>>>>New Rounds Begins...");
					currentGameDetails.playStartUsername = nextuser.userName;

					currentGameDetails.totalGamerounds++;

					if (currentGameDetails.totalGamerounds <= 3) {
						currentGameDetails.currentGameStatus = GameStatus.GameBettingRound;
					} else {
						currentGameDetails.currentGameStatus = GameStatus.GameRunning;
					}

					ArrayList<PlayerDetail> arrPlayerDetails = currentGameDetails
							.getPlayerPlayingList();

					PlayerDetail roundWinner = nextuser;
					long amount = 0;
					long raiseAmount = roundWinner.raiseChipAmount;

					if (roundWinner.totalChipAmount == 0) {
						for (PlayerDetail players : currentGameDetails.gameUserList) {
							if (players.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
									.getPlayerStatusValue()) {
								long amount1 = players.raiseChipAmount;
								if (amount1 > raiseAmount) {
									amount1 -= raiseAmount;

									amount += raiseAmount;

									players.totalChipAmount += amount1;
									players.ChipsValue = Long
											.toString(players.totalChipAmount);
								} else if (amount1 <= raiseAmount) {
									amount += amount1;
								}
								players.raiseChipAmount = 0;
								players.roundBetAmount = 0;
							}
						}

						// for (PlayerDetail players : arrPlayerDetails)
						// {
						// long amount1 = players.raiseChipAmount;
						// if (amount1 > raiseAmount)
						// {
						// amount1 -= raiseAmount;
						//
						// amount += raiseAmount;
						//
						// players.totalChipAmount += amount1;
						// players.ChipsValue =
						// Long.toString(players.totalChipAmount);
						// }
						// else if (amount1 <= raiseAmount)
						// {
						// amount += amount1;
						// }
						// players.raiseChipAmount = 0;
						// players.roundBetAmount = 0;
						// }
					} else {
						for (PlayerDetail players : currentGameDetails.gameUserList) {
							if (players.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
									.getPlayerStatusValue()) {
								amount += players.raiseChipAmount;
								players.raiseChipAmount = 0;
								players.roundBetAmount = 0;
							}
						}
						// for (PlayerDetail players : arrPlayerDetails)
						// {
						// amount += players.raiseChipAmount;
						// players.raiseChipAmount = 0;
						// players.roundBetAmount = 0;
						// }
					}

					long totalChips = roundWinner.totalChipAmount;

					totalChips += amount;
					roundWinner.totalChipAmount = totalChips;
					roundWinner.ChipsValue = Long
							.toString(roundWinner.totalChipAmount);
					// System.out.println("New round Winner chips..."+roundWinner.ChipsValue);

					// Won Count of players
					long totalWon = Long.parseLong(roundWinner.totalWon);
					// totalWon++;
					roundWinner.totalWon = Long.toString(totalWon);

					currentGameDetails.clearLastCardsData();

					currentGameDetails.centerTableCard.clear();

					// skiping beting round if allin.
					if (currentGameDetails.allUserAllIn()) {
						currentGameDetails.currentGameStatus = GameStatus.GameRunning;

						if (currentGameDetails.totalGamerounds <= 3) {
							currentGameDetails.totalGamerounds = 4;
						}
					}

					JSONObject passObj = new JSONObject();

					passObj.put(CardsConstants.kTypeMessageKey,
							CardsConstants.kTypeNewRoundBegin);
					passObj.put(CardsConstants.kUserNameKey,
							roundWinner.getUsernme());
					passObj.put(CardsConstants.kGameRoundKey,
							currentGameDetails.totalGamerounds);

					String msg = passObj.toString();
					currentGameDetails.roomDetails.BroadcastChat(
							CardsConstants.SERVER_NAME, msg);

					this.sendInitialBetMessage(true);

					int tblamount = jsonObject
							.getInt(CardsConstants.kCurrentTableAmountKey);

					if (tblamount != currentGameDetails.centerTableAmount) {
						System.out
								.println("* handleClientPassTimeout client and server side center table ammount issue");
						System.out
								.println("*client table ammount " + tblamount);
						System.out.println("*server table ammount "
								+ currentGameDetails.centerTableAmount);
					}
					currentGameDetails.resetUserGameActions();
				} else {
					// passcounts = 0;
					// System.out.println("*Not New Round YET while in side loop");
				}
			} else {

				if (jsonObject.has(CardsConstants.kCurrentTableAmountKey)) {
					int tblamount = jsonObject
							.getInt(CardsConstants.kCurrentTableAmountKey);

					if (tblamount != currentGameDetails.centerTableAmount) {
						System.out
								.println("* handleClientPassTimeout client and server side center table ammount issue");
						System.out
								.println("*client table ammount " + tblamount);
						System.out.println("*server table ammount "
								+ currentGameDetails.centerTableAmount);
					}
				}
				if (currentGameDetails.playStartUsername
						.equalsIgnoreCase(sender.getName())
						&& currentGameDetails.passcounts == 1) {
					// System.out.println("RestWith Username "+sender.getName());
					// System.out.println("ResetingPlaystartIndex  "+currentGameDetails.playStartUsername);
					// System.out.println("ResetingcurrentPlayerIndex  "+currentGameDetails.currentPlayuserName);

					currentGameDetails.playStartUsername = currentGameDetails.currentPlayuserName;
					currentGameDetails.resetUserGameActions();
					currentGameDetails.passcounts = 0;
					// System.out.println("*Reseting reseting StartIndex and pass count as Firstplayer Pass");
					// System.out.println("After Reset playStartUsername"+currentGameDetails.playStartUsername);
					// System.out.println("After Reset currentPlayuserName"+currentGameDetails.currentPlayuserName);
				} else {
					System.out.println("*Not New Round YET");
				}
			}

			System.out.println("*** handleClientPassTimeout ***");

			if(currentGameDetails.isBettingRound()){
				fireEvent(CardsConstants.AIFireEvent.Bet);
			}
			else{
				fireEvent(CardsConstants.AIFireEvent.PlayCard);
			}


		} catch (Exception e) {
			System.out.println("handleClientPassTimeout exception .._.. "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	private void handleClientFoldEvents(IUser sender, String message) {
		try {
			// System.out.println("**** Fold Message receive ****");

			JSONObject jsonObject = new JSONObject(message);
			int response = jsonObject.getInt(CardsConstants.kTypeMessageKey);

			String strSender = jsonObject
					.getString(CardsConstants.kUserNameKey);
			String strNextUser = null;
			PlayerDetail nextuser = null;

			if (jsonObject.has(CardsConstants.kUserTurnKey)) {
				strNextUser = jsonObject.getString(CardsConstants.kUserTurnKey);
				nextuser = currentGameDetails.getPlayerDetails(strNextUser);

			}

			if (jsonObject.has(CardsConstants.kCurrentTableAmountKey)) {
				int tblamount = jsonObject
						.getInt(CardsConstants.kCurrentTableAmountKey);

				if (tblamount != currentGameDetails.centerTableAmount) {
					System.out
							.println("* handleClientFoldEvents client and server side center table ammount issue");
					System.out.println("*client table ammount " + tblamount);
					System.out.println("*server table ammount "
							+ currentGameDetails.centerTableAmount);
				}
			}

			PlayerDetail playerSender = currentGameDetails
					.getPlayerDetails(strSender);
			// Original Code
			playerSender.userActionStatus = PlayerActionStatus.PLAYActionFold;
			playerSender.userPlayStatus = PlayerStatus.Folded;

			currentGameDetails.totalGamerounds = jsonObject
					.getInt(CardsConstants.kGameRoundKey);

			int index = currentGameDetails.gameUserList.indexOf(nextuser);
			currentGameDetails.currentPlayIndex = index;
			currentGameDetails.currentPlayuserName = nextuser.userName;

			// New Code Added BELOW, if any issue remove it.
			if (currentGameDetails.isBettingRound()) {
				// if its firstplayerd fold, need to reset players name...
				if (currentGameDetails.playStartUsername
						.equals(playerSender.userName)) {
					// System.out.println("** RESET PLAYERS FOLD IN BETTING ROUND");
					currentGameDetails.playStartUsername = currentGameDetails.currentPlayuserName;
					currentGameDetails.passcounts = 0;
					// currentGameDetails.resetGameRound();
				}

				int totalRemainPlayer = currentGameDetails.getFoldTotalCountPlayers();


				// nextuser
				if (currentGameDetails.allUserBetsDone()) {
					// set all bets to mainpots
					long maxBet = 0;
					ArrayList<PlayerDetail> arrPlayerDetails = currentGameDetails.getPlayerPlayingList();

					for (PlayerDetail player : arrPlayerDetails) {
						if (player.roundBetAmount > maxBet) {
							maxBet = player.roundBetAmount;
						}
					}

					this.moveRoundBetsToMainPoit();

					if(totalRemainPlayer == 1){
						System.out.println("handleClientFoldEvents : 1");
						fireEvent(CardsConstants.AIFireEvent.Won);

					}
					else{

						System.out.println("*** handleClientFoldEvents ***");
						if(maxBet == 0)
						{
							System.out.println("handleClientFoldEvents : 2");
							fireEvent(CardsConstants.AIFireEvent.Bet);
						}
						else{
							System.out.println("handleClientFoldEvents : 2.1");
							fireEvent(CardsConstants.AIFireEvent.PlayCard);
						}

					}

				} else {
					if (currentGameDetails.allUserChecked()) {
						// set all bets to mainpots
						this.moveRoundBetsToMainPoit();
						if(totalRemainPlayer == 1){
							System.out.println("handleClientFoldEvents : 3");

							fireEvent(CardsConstants.AIFireEvent.Won);

						}
						else{
							System.out.println("*** handleClientFoldEvents ***");
							System.out.println("handleClientFoldEvents : 4");

							fireEvent(CardsConstants.AIFireEvent.PlayCard);
						}

					}
					else{

						System.out.println("handleClientFoldEvents : 5");

						fireEvent(CardsConstants.AIFireEvent.Call);

					}
				}
			} else
			// New Code Added ABOVE, if any issue remove it.
			{
				if (currentGameDetails.playStartUsername
						.equals(playerSender.userName)
						&& currentGameDetails.centerTableCard.size() <= 0) {
					// System.out.println("** RESET PLAYERS FOLD IN NORMAL ROUND");
					// chance of first player fold. so need to reset
					// firstplayername.
					currentGameDetails.playStartUsername = currentGameDetails.currentPlayuserName;
					currentGameDetails.passcounts = 0;
					currentGameDetails.resetGameRound();
				} else {
					processPassFor(0);
				}

				int totalRemainPlayer = currentGameDetails.getFoldTotalCountPlayers();
				if(totalRemainPlayer == 1){
					System.out.println("handleClientFoldEvents : 6");

					fireEvent(CardsConstants.AIFireEvent.Won);

				}
				else{
					System.out.println("handleClientFoldEvents : 7");

					fireEvent(CardsConstants.AIFireEvent.PlayCard);
				}

				// int count=0;
				// ArrayList<PlayerDetail> arrPlayerDetails =
				// currentGameDetails.getPlayerPlayingList();
				// count = arrPlayerDetails.size();
				//
				// if (count >1)
				// {
				// final int newIndex = index;
				// Timer newTimer = new Timer();
				//
				// TimerTask task = new TimerTask() {
				// @Override
				// public void run()
				// {
				// processPassFor(newIndex);
				// }
				// };
				// newTimer.schedule(task, 0*MSecs);
				// }
			}
		} catch (Exception e) {
			System.out.println("handleClientFoldEvents exception .._.. "
					+ e.getLocalizedMessage());
		}
	}

	private void handleClientCheck(IUser sender, String message) {

		System.out.println("handleClientCheck");

		try {
			System.out.println("handleClientCheck");
			JSONObject jsonObject = new JSONObject(message);
			int response = jsonObject.getInt(CardsConstants.kTypeMessageKey);

			String strSender = jsonObject
					.getString(CardsConstants.kUserNameKey);
			String strNextUser = null;
			PlayerDetail nextuser = null;

			if (jsonObject.has(CardsConstants.kUserTurnKey)) {
				strNextUser = jsonObject.getString(CardsConstants.kUserTurnKey);
				nextuser = currentGameDetails.getPlayerDetails(strNextUser);

			}
			PlayerDetail playerSender = currentGameDetails
					.getPlayerDetails(strSender);

			int index = currentGameDetails.gameUserList.indexOf(nextuser);
			currentGameDetails.currentPlayIndex = index;
			currentGameDetails.currentPlayuserName = nextuser.userName;
			currentGameDetails.totalGamerounds = jsonObject
					.getInt(CardsConstants.kGameRoundKey);

			if (jsonObject.has(CardsConstants.kCurrentTableAmountKey)) {
				int tblamount = jsonObject
						.getInt(CardsConstants.kCurrentTableAmountKey);

				if (tblamount != currentGameDetails.centerTableAmount) {
					System.out
							.println("* handleClientCheck client and server side center table ammount issue");
					System.out.println("*client table ammount " + tblamount);
					System.out.println("*server table ammount "
							+ currentGameDetails.centerTableAmount);
				}
			}

			fireEvent(CardsConstants.AIFireEvent.Bet);

		} catch (Exception e) {
			System.out.println("handleClientCheck exception .._.. "
					+ e.getLocalizedMessage());
		}
	}

	private void handleClientBettingBet(IUser sender, String message) {
		try {
//			System.out.println("handleClientBettingBet : " + message);

			JSONObject jsonObject = new JSONObject(message);
			int response = jsonObject.getInt(CardsConstants.kTypeMessageKey);


			int betAmount = jsonObject.getInt(CardsConstants.kUserCallAmountKey);

			for(int i = 0; i< currentGameDetails.gameUserList.size(); i++){
				PlayerDetail playerDetail = currentGameDetails.gameUserList.get(i);
				if(betAmount > playerDetail.previousBetValueByPlayer)
					playerDetail.previousBetValueByPlayer = betAmount;
			}



			String strSender = jsonObject
					.getString(CardsConstants.kUserNameKey);
			String strNextUser = null;
			PlayerDetail nextuser = null;

			if (jsonObject.has(CardsConstants.kUserTurnKey)) {
				strNextUser = jsonObject.getString(CardsConstants.kUserTurnKey);
				nextuser = currentGameDetails.getPlayerDetails(strNextUser);
			}
			PlayerDetail playerSender = currentGameDetails
					.getPlayerDetails(strSender);

			int index = currentGameDetails.gameUserList.indexOf(nextuser);
			currentGameDetails.currentPlayIndex = index;
			currentGameDetails.currentPlayuserName = nextuser.userName;

			if (jsonObject.has(CardsConstants.kCurrentTableAmountKey)) {
				int tblamount = jsonObject
						.getInt(CardsConstants.kCurrentTableAmountKey);

				if (tblamount != currentGameDetails.centerTableAmount) {
					System.out
							.println("*handleClientBettingBet client and server side center table ammount issue");
					System.out.println("*client table ammount " + tblamount);
					System.out.println("*server table ammount "
							+ currentGameDetails.centerTableAmount);
				}
			}

			if (jsonObject.has(CardsConstants.kUserCallAmountKey)) {
				int amount = jsonObject
						.getInt(CardsConstants.kUserCallAmountKey);
				playerSender.raiseChipAmount += amount;
				playerSender.totalChipAmount -= amount;
				playerSender.roundBetAmount += amount;
				if (playerSender.totalChipAmount < 0) {
					System.out.println("** Name: " + playerSender.playerName
							+ " has chips zero > "
							+ playerSender.totalChipAmount);
					playerSender.totalChipAmount = 0;
				}
				playerSender.ChipsValue = Long
						.toString(playerSender.totalChipAmount);
				playerSender.userActionStatus = PlayerActionStatus.PLAYActionBet;

				// nextuser
				if (currentGameDetails.allUserBetsDone()) {
					// System.out.println("Betting round Completed with BETTING match");
					// set all bets to mainpots
					this.moveRoundBetsToMainPoit();
					if (!(currentGameDetails.playStartUsername
							.equals(currentGameDetails.currentPlayuserName))) {
						// changing starting point according to change of
						// start/intermediat bets.
						nextuser = currentGameDetails
								.getPlayerDetails(currentGameDetails.playStartUsername);
						index = currentGameDetails.gameUserList
								.indexOf(nextuser);
						currentGameDetails.currentPlayIndex = index;
						currentGameDetails.currentPlayuserName = nextuser.userName;
					}
					System.out.println("*** handleClientBettingBet ***");

					fireEvent(CardsConstants.AIFireEvent.PlayCard);
				}



				// else if (currentGameDetails.allUserChecked())
				// {
				// // set all bets to mainpots
				// this.moveRoundBetsToMainPoit();
				// }
				else {
					// set current play here.
					fireEvent(CardsConstants.AIFireEvent.Call);
				}
				this.currentBetAmount = jsonObject
						.getInt(CardsConstants.kUserCallAmountKey);




				int isBet = 0;

				if(jsonObject.has(CardsConstants.kGameBetOn)){
					isBet = jsonObject.getInt(CardsConstants.kGameBetOn);
				}

//				if(isBet == 1){
//					fireEvent(CardsConstants.AIFireEvent.Call);
//				}
//				else{
//					fireEvent(CardsConstants.AIFireEvent.PlayCard);
//				}


//				callByAI();
			}
		} catch (Exception e) {
			System.out.println("handleClientBettingBet exception .._.. "
					+ e.getLocalizedMessage());
		}
	}

	private void handleClientBettingRaise(IUser sender, String message) {
		try {
			JSONObject jsonObject = new JSONObject(message);
			int response = jsonObject.getInt(CardsConstants.kTypeMessageKey);

			String strSender = jsonObject
					.getString(CardsConstants.kUserNameKey);
			String strNextUser = null;
			PlayerDetail nextuser = null;

			if (jsonObject.has(CardsConstants.kUserTurnKey)) {
				strNextUser = jsonObject.getString(CardsConstants.kUserTurnKey);
				nextuser = currentGameDetails.getPlayerDetails(strNextUser);
			}

			int index = currentGameDetails.gameUserList.indexOf(nextuser);
			currentGameDetails.currentPlayIndex = index;
			currentGameDetails.currentPlayuserName = nextuser.userName;

			if (jsonObject.has(CardsConstants.kCurrentTableAmountKey)) {
				int tblamount = jsonObject
						.getInt(CardsConstants.kCurrentTableAmountKey);

				if (tblamount != currentGameDetails.centerTableAmount) {
					System.out
							.println("* handleClientBettingRaise client and server side center table ammount issue");
					System.out.println("*client table ammount " + tblamount);
					System.out.println("*server table ammount "
							+ currentGameDetails.centerTableAmount);
				}
			}

			PlayerDetail playerSender = currentGameDetails
					.getPlayerDetails(strSender);
			// System.out.println("Receive kTypeBetRoundRaise Message");
			if (jsonObject.has(CardsConstants.kUserCallAmountKey)) {
				long amount = jsonObject
						.getInt(CardsConstants.kUserCallAmountKey);
				playerSender.raiseChipAmount += amount;
				playerSender.totalChipAmount -= amount;
				playerSender.roundBetAmount += amount;

				if (playerSender.totalChipAmount < 0) {
					System.out.println("** Name: " + playerSender.playerName
							+ " has chips zero > "
							+ playerSender.totalChipAmount);
					playerSender.totalChipAmount = 0;
				}
				playerSender.ChipsValue = Long
						.toString(playerSender.totalChipAmount);
				playerSender.userActionStatus = PlayerActionStatus.PLAYActionBet;

				// nextuser
				if (currentGameDetails.allUserBetsDone()) {
					// set all bets to mainpots
					this.moveRoundBetsToMainPoit();
					if (!(currentGameDetails.playStartUsername
							.equals(currentGameDetails.currentPlayuserName))) {
						// changing starting point according to change of
						// start/intermediat bets.
						nextuser = currentGameDetails
								.getPlayerDetails(currentGameDetails.playStartUsername);
						index = currentGameDetails.gameUserList
								.indexOf(nextuser);
						currentGameDetails.currentPlayIndex = index;
						currentGameDetails.currentPlayuserName = nextuser.userName;
					}
				}
				// else if (currentGameDetails.allUserChecked())
				// {
				// // set all bets to mainpots
				// this.moveRoundBetsToMainPoit();
				// }
				else {
					// set current play here.
				}

				int betAmount = jsonObject.getInt(CardsConstants.kUserCallAmountKey);

				for(int i = 0; i< currentGameDetails.gameUserList.size(); i++){
					PlayerDetail playerDetail = currentGameDetails.gameUserList.get(i);
					if(betAmount > playerDetail.previousBetValueByPlayer)
						playerDetail.previousBetValueByPlayer = betAmount;
				}

			}

			fireEvent(CardsConstants.AIFireEvent.Call);

		} catch (Exception e) {
			System.out.println("handleClientBettingRaise exception .._.. "
					+ e.getLocalizedMessage());
		}
	}

	private void handleClientBettingCheck(IUser sender, String message) {
		try {
			 System.out.println("Receive kTypeBetRoundCheck Message");

			JSONObject jsonObject = new JSONObject(message);
			int response = jsonObject.getInt(CardsConstants.kTypeMessageKey);

			String strSender = jsonObject
					.getString(CardsConstants.kUserNameKey);
			String strNextUser = null;
			PlayerDetail nextuser = null;

			if (jsonObject.has(CardsConstants.kUserTurnKey)) {
				strNextUser = jsonObject.getString(CardsConstants.kUserTurnKey);
				nextuser = currentGameDetails.getPlayerDetails(strNextUser);
			}

			int index = currentGameDetails.gameUserList.indexOf(nextuser);
			currentGameDetails.currentPlayIndex = index;
			currentGameDetails.currentPlayuserName = nextuser.userName;

			PlayerDetail playerSender = currentGameDetails
					.getPlayerDetails(strSender);
			playerSender.userActionStatus = PlayerActionStatus.PLAYActionCheck;

			if (jsonObject.has(CardsConstants.kCurrentTableAmountKey)) {
				int tblamount = jsonObject
						.getInt(CardsConstants.kCurrentTableAmountKey);

				if (tblamount != currentGameDetails.centerTableAmount) {
					System.out
							.println("*handleClientBettingCheck client and server side center table ammount issue");
					System.out.println("*client table ammount " + tblamount);
					System.out.println("*server table ammount "
							+ currentGameDetails.centerTableAmount);
				}
			}

			if (currentGameDetails.allUserChecked()) {
				// System.out.println("Betting round Completed with check");
				// set all bets to mainpots
				this.moveRoundBetsToMainPoit();
				if (!(currentGameDetails.playStartUsername
						.equals(currentGameDetails.currentPlayuserName))) {
					// changing starting point according to change of
					// start/intermediat bets.
					nextuser = currentGameDetails
							.getPlayerDetails(currentGameDetails.playStartUsername);
					index = currentGameDetails.gameUserList.indexOf(nextuser);
					currentGameDetails.currentPlayIndex = index;
					currentGameDetails.currentPlayuserName = nextuser.userName;
				}
			}

			if(currentGameDetails.isBettingRound()){

				fireEvent(CardsConstants.AIFireEvent.Bet);
			}


		} catch (Exception e) {
			System.out
					.println("Unknown type exception .._.. " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void moveRoundBetsToMainPoit() {
		currentGameDetails.currentGameStatus = GameStatus.GameRunning;
		currentGameDetails.resetUserGameActions();
		long amounts = 0;

		for (PlayerDetail player : currentGameDetails.gameUserList) {
			if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
					.getPlayerStatusValue()) {
				amounts += player.roundBetAmount;
				player.allBetRoundAmount += player.roundBetAmount;
				player.roundBetAmount = 0;
				player.raiseChipAmount = 0;
				player.raiseCounter = 0;
			}
		}

		// ArrayList<PlayerDetail> playerList =
		// currentGameDetails.getPlayerPlayingList();
		// for (PlayerDetail player : playerList)
		// {
		// amounts+= player.roundBetAmount;
		// player.allBetRoundAmount += player.roundBetAmount;
		// player.roundBetAmount = 0;
		// player.raiseChipAmount = 0;
		// player.raiseCounter = 0;
		// }

		currentGameDetails.centerTableAmount += amounts;

		for(PlayerDetail playerDetail : currentGameDetails.getPlayerPlayingList()){

			playerDetail.previousBetValueByPlayer = 0;
		}


	}

	public void unScheduleUserTimeOut() {
		if (timeoutTimer != null) {
			timeoutTimer.cancel();
			timeoutTimer = null;
		}
	}

	public void scheduleUserTimeOut(long delaySet) {
		if (timeoutTimer != null) {
			timeoutTimer.cancel();
			timeoutTimer = null;
		}

		timeoutTimer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				userTimeOut();
			}
		};
		timeoutTimer.schedule(task, delaySet * MSecs);
		startTimer = System.currentTimeMillis();
	}

	public void userTimeOut() {
		try {
			if (currentGameDetails.currentGameStatus.getGameStatusValue() <= GameStatus.GameRunning
					.getGameStatusValue()) {
				PlayerDetail currentPlay = currentGameDetails
						.getPlayerDetails(currentGameDetails.currentPlayuserName);
				if (currentPlay != null) {
					PlayerDetail newTurnPlayer = currentGameDetails
							.nextPlayerTurn();

					currentGameDetails.arrIgnoreList.add(currentPlay.userName);

					// now performing status updates on server side for same as
					// we done on client side.
					boolean needFoldAction = false;
					if (currentGameDetails.isBettingRound()) {
						// nextuser
						long amount = currentGameDetails
								.getMaxBettingRoundAmount();
						if (amount != -1) {
							if (amount == currentPlay.roundBetAmount
									|| currentPlay.totalChipAmount == 0) {
								// i have same so i should let check
								// do check process in betting round
								String strFormat = "{\""
										+ CardsConstants.kTypeMessageKey
										+ "\":\""
										+ CardsConstants.kTypeBetRoundCheck
										+ "\",\"" + CardsConstants.kUserNameKey
										+ "\":\"" + currentPlay.userName
										+ "\",\"" + CardsConstants.kUserTurnKey
										+ "\":\"" + newTurnPlayer.userName
										+ "\",\""
										+ CardsConstants.kUserPassCount
										+ "\":\""
										+ currentGameDetails.passcounts
										+ "\",\""
										+ CardsConstants.kCurrentTableAmountKey
										+ "\":\""
										+ currentGameDetails.centerTableAmount
										+ "\"}";
								this.handleClientBettingCheck(currentPlay.user,
										strFormat);
							} else {
								// have to do fold
								needFoldAction = true;
							}
						} else {
							// do check process in betting round
							String strFormat = "{\""
									+ CardsConstants.kTypeMessageKey + "\":\""
									+ CardsConstants.kTypeBetRoundCheck
									+ "\",\"" + CardsConstants.kUserNameKey
									+ "\":\"" + currentPlay.userName + "\",\""
									+ CardsConstants.kUserTurnKey + "\":\""
									+ newTurnPlayer.userName + "\",\""
									+ CardsConstants.kUserPassCount + "\":\""
									+ currentGameDetails.passcounts + "\",\""
									+ CardsConstants.kCurrentTableAmountKey
									+ "\":\""
									+ currentGameDetails.centerTableAmount
									+ "\"}";
							this.handleClientBettingCheck(currentPlay.user,
									strFormat);
						}
					} else {
						// normal round process.
						long amount = currentGameDetails.getMaxRaiseAmount();
						if (amount != -1) {
							// ALLIN Updates
							if (amount == currentPlay.raiseChipAmount
									|| currentPlay.totalChipAmount == 0) {
								// do pass process in normal round.
								String strFormat = "{\""
										+ CardsConstants.kTypeMessageKey
										+ "\":\""
										+ CardsConstants.kTypeUserPass
										+ "\",\"" + CardsConstants.kUserNameKey
										+ "\":\"" + currentPlay.userName
										+ "\",\"" + CardsConstants.kUserTurnKey
										+ "\":\"" + newTurnPlayer.userName
										+ "\",\""
										+ CardsConstants.kGameRoundKey
										+ "\":\""
										+ currentGameDetails.totalGamerounds
										+ "\",\""
										+ CardsConstants.kCurrentTableAmountKey
										+ "\":\""
										+ currentGameDetails.centerTableAmount
										+ "\"}";
								this.handleClientPassTimeout(currentPlay.user,
										strFormat);
							} else {
								// do fold process in normal round
								needFoldAction = true;
							}
						} else {
							// do pass process in normal round.
							String strFormat = "{\""
									+ CardsConstants.kTypeMessageKey + "\":\""
									+ CardsConstants.kTypeUserPass + "\",\""
									+ CardsConstants.kUserNameKey + "\":\""
									+ currentPlay.userName + "\",\""
									+ CardsConstants.kUserTurnKey + "\":\""
									+ newTurnPlayer.userName + "\",\""
									+ CardsConstants.kGameRoundKey + "\":\""
									+ currentGameDetails.totalGamerounds
									+ "\",\""
									+ CardsConstants.kCurrentTableAmountKey
									+ "\":\""
									+ currentGameDetails.centerTableAmount
									+ "\"}";
							this.handleClientPassTimeout(currentPlay.user,
									strFormat);
						}
					}
					if (needFoldAction) {
						String strFormat = "{\""
								+ CardsConstants.kTypeMessageKey + "\":\""
								+ CardsConstants.kTypeFold + "\",\""
								+ CardsConstants.kUserNameKey + "\":\""
								+ currentPlay.userName + "\",\""
								+ CardsConstants.kUserTurnKey + "\":\""
								+ newTurnPlayer.userName + "\",\""
								+ CardsConstants.kGameRoundKey + "\":\""
								+ currentGameDetails.totalGamerounds + "\",\""
								+ CardsConstants.kCurrentTableAmountKey
								+ "\":\""
								+ currentGameDetails.centerTableAmount + "\"}";
						this.handleClientFoldEvents(currentPlay.user, strFormat);
					}
					JSONObject passObj = new JSONObject();
					passObj.put(CardsConstants.kTypeMessageKey,
							CardsConstants.kTypeServerUserTimeOut);
					// here kUserNameKey used as nextturn player key
					passObj.put(CardsConstants.kUserNameKey,
							newTurnPlayer.userName);
					// here kUserTurnKey used as Current player key
					passObj.put(CardsConstants.kUserTurnKey,
							currentPlay.userName);

					// kUserTurnKey
					String strObj = passObj.toString();
					currentGameDetails.roomDetails.BroadcastChat(
							CardsConstants.SERVER_NAME, strObj);
					// System.out.println("Server TimeOut File..... sender "+CardsConstants.SERVER_NAME);

					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
				} else {
					System.out
							.println("/* USERTIMEOUT Current Player was NULL");
				}
			} else {
				System.out.println("/* Time out fire after game End");
			}
		} catch (Exception e) {
			System.out.println(" timeoutfire issue " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void sendNewTurnPassMessage(String nextUsernameStart) {
		JSONObject passObj = new JSONObject();
		try {
			passObj.put(CardsConstants.kTypeMessageKey,
					CardsConstants.kTypeNewUserTurn);
			passObj.put(CardsConstants.kUserNameKey, nextUsernameStart);

			String strObj = passObj.toString();
			currentGameDetails.roomDetails.BroadcastChat(
					CardsConstants.SERVER_NAME, strObj);

			if (currentGameDetails.isBettingRound()) {
				if (currentGameDetails.allUserBetsDone()) {
					this.moveRoundBetsToMainPoit();
					if (!currentGameDetails.currentPlayuserName
							.equalsIgnoreCase(currentGameDetails.playStartUsername)) {
						currentGameDetails.currentPlayuserName = currentGameDetails.playStartUsername;
					}
				} else if (currentGameDetails.allUserChecked()) {
					this.moveRoundBetsToMainPoit();
					if (!currentGameDetails.currentPlayuserName
							.equalsIgnoreCase(currentGameDetails.playStartUsername)) {
						currentGameDetails.currentPlayuserName = currentGameDetails.playStartUsername;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void handleUserJoinRequest(IUser user, HandlingResult result) {
		// System.out.println("inside > handleUserJoinRequest name : "+user.getName());
	}

	public void handleUserSubscribeRequest(IUser sender, HandlingResult result) {
		
		System.out.println("***handleUserSubscribeRequest");
		
		PlayerDetail players = currentGameDetails.getPlayerDetails(sender
				.getName());
		boolean isComputer = false;
		ComputerDetails comDetails = null;

		if(currentGameDetails.gameUserList.size() == 0){
			currentGameDetails.minPlayerToStartGame = 2;
			currentGameDetails.playerChipsInitally = 0;
		}
		
		if (currentGameDetails.gameUserList.size() >0)
		{
			ArrayList<PlayerDetail> arrList = currentGameDetails.gameUserList;

			System.out.println("***join user  LIST");
			for (PlayerDetail player : arrList)
			{
				boolean needRemove = true;

				for (IUser name : currentGameDetails.roomDetails.getJoinedUsers())
				{
					if (name.getName().equalsIgnoreCase(player.userName))
					{
						needRemove = false;
						break;
					}
				}

				if (needRemove)
				{
					System.out.println("removing player from exising array and he is not in game");
					// player exist in array list but he is not in game any more so removing player from game list as it create issue
					currentGameDetails.gameUserList.remove(player);
				}
			}

		}

		if (players != null) {
			isComputer = players.isComputerPlayer;
			comDetails = players.computerDetails;

			System.out.println("OLDplayers info NAME : "+players.playerName);
			System.out.println("OLD players info userName: "+players.userName);
			System.out.println("OLD players info status : "+players.userPlayStatus.getPlayerStatusValue());
			this.onDisconnectUser2(sender);

			PlayerDetail player1 = currentGameDetails.getPlayerDetails(sender
					.getName());

			if (player1 != null) {
				java.util.Date today = new java.util.Date();

				// System.out.println("USERINFO...: Time: "+today+"... "+sender.getCustomData());
				// System.out.println("USERINFO..... "+currentGameDetails.gameUserList.toString());
			}
		} else {
			// System.out.println("user info>>  "+sender.getCustomData());
		}

		boolean needChangeIndex = false;
		int userIdxValue = currentGameDetails.getTopMostViewersIndex();
		if (userIdxValue != -1) {
			needChangeIndex = true;
		}

		ArrayList<PlayerDetail> arrPlayerList = currentGameDetails
				.getPlayerPlayingList();
		if (currentGameDetails.currentGameStatus.getGameStatusValue() > GameStatus.GameRunning
				.getGameStatusValue()) {
			if (arrPlayerList.size() > 0) {
				// send user status with game not running;
				System.out.println("sendInitialGameState");
				this.sendInitialGameState();
			} else {
				// only single player i guess.
				System.out.println("send kTypeRoomInfo");
				currentGameDetails.processComputerPlayerStates(5);

				JSONObject jsonDetails = currentGameDetails.sendRoomInfo();
				try {
					jsonDetails.put(CardsConstants.kTypeMessageKey,
							CardsConstants.kTypeRoomInfo);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				currentGameDetails.roomDetails.BroadcastChat(
						CardsConstants.SERVER_NAME, jsonDetails.toString());
			}


			PlayerDetail player = new PlayerDetail(sender);
			player.userPlayStatus = PlayerStatus.Playing;
			player.isComputerPlayer = isComputer;
			player.computerDetails = comDetails;

			if (needChangeIndex) {
				// System.out.println("** Adding USER at"+userIdxValue);
				currentGameDetails.gameUserList.add(userIdxValue, player);
			} else {
				// System.out.println("** Adding USER at end of list");
				currentGameDetails.gameUserList.add(player);
			}

			arrPlayerList.add(player);
			// System.out.println("Before GameStart: "+sender.getName()+" with size:"+arrPlayerList.size());

//			if(arrPlayerList.size() == 1){
//				System.out.println("AI");
//				currentGameDetails.processComputerPlayerStates();
//			}


			// CM check to increase min players to start the game
			if (arrPlayerList.size() >= currentGameDetails.minPlayerToStartGame) {
				currentGameDetails.currentGameStatus = GameStatus.GameBettingRound;

				Timer timer = new Timer();
				TimerTask delayedThreadStartTask = new TimerTask() {
					@Override
					public void run() {
						if (lastWinner != null) {
							int nextIdx = currentGameDetails
									.getNextPlayerTurn();
							PlayerDetail players = currentGameDetails.gameUserList
									.get(nextIdx);
							System.out.println("players.userName : "  + players.userName);
							String name = players.userName;
							// System.out.println("//**New Game Begins with "+name+" !!");
							distributeCardsForGame(name);
						} else {
							String name = currentGameDetails.gameUserList
									.get(0).userName;
							System.out.println("players.userName : "  + name);
							currentGameDetails.playStartUsername = name;
							// System.out.println("distributeCardsForGame WITH "+name);
							distributeCardsForGame(name);
						}
					}
				};
				timer.schedule(delayedThreadStartTask, 3 * MSecs); // 3 secs
			}
		} else if (currentGameDetails.currentGameStatus.getGameStatusValue() <= GameStatus.GameRunning
				.getGameStatusValue()) {
			// int totalSize = arrPlayerDetails.size() +
			// arrWaitingPlayerDetails.size();
			ArrayList<PlayerDetail> arrPlaying = currentGameDetails
					.getPlayerPlayingList();
			ArrayList<PlayerDetail> arrWaiting = currentGameDetails
					.getPlayerWaitingList();
			ArrayList<PlayerDetail> arrViewing = currentGameDetails
					.getPlayerViewingList();

			int totalSize = arrWaiting.size() + arrPlaying.size();
			PlayerStatus status;
			if (totalSize < CardsConstants.MaxPlayers) {
				status = PlayerStatus.Waiting;
			} else {
				status = PlayerStatus.Viewing;
			}

			PlayerDetail player = currentGameDetails.getPlayerDetails(sender
					.getName());
			if (player != null) {
				player.user = sender;
				System.out.println("** <= GameStatus.GameRunning currentGameDetails.gameUserList.remove "+player.playerName);
				currentGameDetails.gameUserList.remove(player);
			}
			// else

			{
				player = new PlayerDetail(sender);
				player.userPlayStatus = status;
				// System.out.println("*****new user status "+player.userPlayStatus);
				if (needChangeIndex) {
					// System.out.println("** Adding USER at"+userIdxValue);
					currentGameDetails.gameUserList.add(userIdxValue, player);
				} else {
					// System.out.println("** Adding USER at end of list");
					currentGameDetails.gameUserList.add(player);
				}
			}
			currentGameDetails.sendGameStatus();
		}
	}

	@Override
	public void onTimerTick(long time) {
		// TODO Auto-generated method stub
		super.onTimerTick(time);
		// System.out.println("** Tick timer "
		// +currentGameDetails.roomDetails.getName());
		// System.out.println("** Tick timer Time " +time);

	}

	public void handleSubscribeByAI(IUser sender){

		boolean needChangeIndex = false;
		int userIdxValue = currentGameDetails.getTopMostViewersIndex();
		if (userIdxValue != -1) {
			needChangeIndex = true;
		}

		ArrayList<PlayerDetail> arrPlayerList = currentGameDetails
				.getPlayerPlayingList();
		if (currentGameDetails.currentGameStatus.getGameStatusValue() > GameStatus.GameRunning
				.getGameStatusValue()) {
			if (arrPlayerList.size() > 0) {
				// send user status with game not running;
				System.out.println("sendInitialGameState");
				this.sendInitialGameState();
			} else {
				// only single player i guess.
				System.out.println("send kTypeRoomInfo");
				currentGameDetails.processComputerPlayerStates(5);

				JSONObject jsonDetails = currentGameDetails.sendRoomInfo();
				try {
					jsonDetails.put(CardsConstants.kTypeMessageKey,
							CardsConstants.kTypeRoomInfo);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				currentGameDetails.roomDetails.BroadcastChat(
						CardsConstants.SERVER_NAME, jsonDetails.toString());
			}



			// CM check to increase min players to start the game
			if (arrPlayerList.size() >= currentGameDetails.minPlayerToStartGame) {
				currentGameDetails.currentGameStatus = GameStatus.GameBettingRound;

				Timer timer = new Timer();
				TimerTask delayedThreadStartTask = new TimerTask() {
					@Override
					public void run() {
						if (lastWinner != null) {
							int nextIdx = currentGameDetails
									.getNextPlayerTurn();
							PlayerDetail players = currentGameDetails.gameUserList
									.get(nextIdx);
							System.out.println("players.userName : "  + players.userName);
							String name = players.userName;
							// System.out.println("//**New Game Begins with "+name+" !!");
							distributeCardsForGame(name);
						} else {
							String name = currentGameDetails.gameUserList
									.get(0).userName;
							System.out.println("players.userName : "  + name);
							currentGameDetails.playStartUsername = name;
							// System.out.println("distributeCardsForGame WITH "+name);
							distributeCardsForGame(name);
						}
					}
				};
				timer.schedule(delayedThreadStartTask, 3 * MSecs); // 3 secs
			}
		} else if (currentGameDetails.currentGameStatus.getGameStatusValue() <= GameStatus.GameRunning
				.getGameStatusValue()) {
			// int totalSize = arrPlayerDetails.size() +
			// arrWaitingPlayerDetails.size();
			ArrayList<PlayerDetail> arrPlaying = currentGameDetails
					.getPlayerPlayingList();
			ArrayList<PlayerDetail> arrWaiting = currentGameDetails
					.getPlayerWaitingList();
			ArrayList<PlayerDetail> arrViewing = currentGameDetails
					.getPlayerViewingList();

			int totalSize = arrWaiting.size() + arrPlaying.size() - 1;

			System.out.println("** arrWaiting.size() "+ arrWaiting.size() + "    arrPlaying.size() : " + arrPlaying.size());

			PlayerStatus status;
			if (totalSize < CardsConstants.MaxPlayers) {
				status = PlayerStatus.Waiting;
			} else {
				status = PlayerStatus.Viewing;
			}

			PlayerDetail player = currentGameDetails.getPlayerDetails(sender
					.getName());
//			if (player != null) {
//				player.user = sender;
//				System.out.println("** <= GameStatus.GameRunning currentGameDetails.gameUserList.remove "+player.playerName);
//				currentGameDetails.gameUserList.remove(player);
//			}
//			// else
//
			{
//				player = new PlayerDetail(sender);
				System.out.println("** player.userPlayStatus "+ status);
				player.userPlayStatus = status;
				// System.out.println("*****new user status "+player.userPlayStatus);
				if (needChangeIndex) {
					// System.out.println("** Adding USER at"+userIdxValue);
					currentGameDetails.gameUserList.remove(player);
					currentGameDetails.gameUserList.add(userIdxValue, player);
				} else {
					// System.out.println("** Adding USER at end of list");
//					currentGameDetails.gameUserList.add(player);
				}
			}
			currentGameDetails.sendGameStatus();
		}
	}

	public void onUserUnsubscribeRequest(IUser sender) {

	}

	public void onUserLeaveRequest(IUser user) {
		 System.out.println("onUserLeaveRequest. "+user.getName());
		this.onDisconnectUser(user);
	}

	/** Process to clear user from list From Any state */
	public void onDisconnectUser2(IUser sender) {

		// TODO Auto-generated method stub
		PlayerDetail players = currentGameDetails
				.getPlayerDetails(sender);
		if (players != null) {
			try {
				PlayerDetail currPlayer = null;
				if (currentGameDetails.currentPlayIndex != -1
						&& currentGameDetails.currentPlayIndex < currentGameDetails.gameUserList
								.size()) {
					currPlayer = currentGameDetails.gameUserList
							.get(currentGameDetails.currentPlayIndex);
				}

				if (players.userPlayStatus.getPlayerStatusValue() > PlayerStatus.Folded
						.getPlayerStatusValue()) {
					currentGameDetails.gameUserList.remove(players);
					if (currPlayer != null
							&& !currPlayer.userName
									.equalsIgnoreCase(sender.getName())) {
						currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
								.indexOf(currPlayer);
					}
					System.out.println("PlayerStatus.Folded Leave Users New Size... : "
							+ currentGameDetails.gameUserList.size());
					return;
				}

				currentGameDetails.processStartIndicator(players);
				StartUserleftProcess(sender.getName());

				processUserLeft(sender.getName());// process leave
													// request in ivars
				System.out.println("onMainDisconnectoin... : "+players.playerName);						
				currentGameDetails.gameUserList.remove(players);
				// 

				final int newIndex = currentGameDetails.currentPlayIndex;

				// Timer newTimer = new Timer();
				// TimerTask task = new TimerTask() {
				// @Override
				// public void run()
				// {
				processPassFor(newIndex);
				// }
				// };
				// newTimer.schedule(task, 0*MSecs);

				ArrayList<PlayerDetail> userList = currentGameDetails
						.getPlayerPlayingList();

				if (userList.size() <= 1) {
					currentGameDetails.currentGameStatus = GameStatus.GameEnd;
					clearAllUserDetails();
					lastWinner = null;

					unScheduleUserTimeOut();

					if (userList.size() <= 0) {
						ArrayList<PlayerDetail> waitingPlayers = currentGameDetails
								.getPlayerWaitingList();

						for (PlayerDetail player1 : waitingPlayers) {
							player1.userActionStatus = PlayerActionStatus.PLAYActionNone;
							player1.userPlayStatus = PlayerStatus.Playing;
							// System.out.println("****player name in play now .."
							// +player1.playerName);
						}

						userList = currentGameDetails
								.getPlayerPlayingList();
						if (userList.size() >= 2) {
							int nextIdx = currentGameDetails
									.getNextPlayerTurn();
							// System.out.println("***** idx "+nextIdx);
							// for (PlayerDetail player :
							// currentGameDetails.gameUserList)
							// {
							// System.out.println("Players in list : "+player.userName);
							// System.out.println("Players Idx : "+currentGameDetails.gameUserList.indexOf(player));
							// }
							// System.out.println("*****");
							PlayerDetail player = currentGameDetails.gameUserList
									.get(nextIdx);
							String name = player.userName;
							distributeCardsForGame(name);
						}
					} else {
						if (currPlayer != null
								&& !currPlayer.userName
										.equalsIgnoreCase(sender
												.getName())) {
							currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
									.indexOf(currPlayer);
						}
					}
				} else {
					if (currPlayer != null
							&& !currPlayer.userName
									.equalsIgnoreCase(sender.getName())) {
						currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
								.indexOf(currPlayer);
					}
				}
			} catch (Exception e) {
				System.out.println("Exception onDisconnectUser...."
						+ e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		System.out.println("Leave Users New Size... : "
				+ currentGameDetails.gameUserList.size());
	
	}
	public void onDisconnectUser(IUser sender1) {
		final IUser sender = sender1;
		// System.out.println("onDisconnectUser: "+sender1.getName());

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				PlayerDetail players = currentGameDetails
						.getPlayerDetails(sender);
				if (players != null) {
					try {
						PlayerDetail currPlayer = null;
						if (currentGameDetails.currentPlayIndex != -1
								&& currentGameDetails.currentPlayIndex < currentGameDetails.gameUserList
										.size()) {
							currPlayer = currentGameDetails.gameUserList
									.get(currentGameDetails.currentPlayIndex);
						}

						if (players.userPlayStatus.getPlayerStatusValue() > PlayerStatus.Folded
								.getPlayerStatusValue()) {
							currentGameDetails.gameUserList.remove(players);
							if (currPlayer != null
									&& !currPlayer.userName
											.equalsIgnoreCase(sender.getName())) {
								currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
										.indexOf(currPlayer);
							}
							System.out.println("PlayerStatus.Folded Leave Users New Size... : "
									+ currentGameDetails.gameUserList.size());
							return;
						}

						currentGameDetails.processStartIndicator(players);
						StartUserleftProcess(sender.getName());

						processUserLeft(sender.getName());// process leave
															// request in ivars
						System.out.println("onMainDisconnectoin... : "+players.playerName);
						System.out.println("onMainDisconnectoin... : "+players.userName);							
						currentGameDetails.gameUserList.remove(players);
						// 

						final int newIndex = currentGameDetails.currentPlayIndex;

						// Timer newTimer = new Timer();
						// TimerTask task = new TimerTask() {
						// @Override
						// public void run()
						// {
						processPassFor(newIndex);
						// }
						// };
						// newTimer.schedule(task, 0*MSecs);

						ArrayList<PlayerDetail> userList = currentGameDetails
								.getPlayerPlayingList();

						if (userList.size() <= 1) {
							currentGameDetails.currentGameStatus = GameStatus.GameEnd;
							clearAllUserDetails();
							lastWinner = null;

							unScheduleUserTimeOut();

							if (userList.size() <= 0) {
								ArrayList<PlayerDetail> waitingPlayers = currentGameDetails
										.getPlayerWaitingList();

								for (PlayerDetail player1 : waitingPlayers) {
									player1.userActionStatus = PlayerActionStatus.PLAYActionNone;
									player1.userPlayStatus = PlayerStatus.Playing;
									// System.out.println("****player name in play now .."
									// +player1.playerName);
								}

								userList = currentGameDetails
										.getPlayerPlayingList();
								if (userList.size() >= 2) {
									int nextIdx = currentGameDetails
											.getNextPlayerTurn();
									// System.out.println("***** idx "+nextIdx);
									// for (PlayerDetail player :
									// currentGameDetails.gameUserList)
									// {
									// System.out.println("Players in list : "+player.userName);
									// System.out.println("Players Idx : "+currentGameDetails.gameUserList.indexOf(player));
									// }
									// System.out.println("*****");
									PlayerDetail player = currentGameDetails.gameUserList
											.get(nextIdx);
									String name = player.userName;
									distributeCardsForGame(name);
								}
							} else {
								if (currPlayer != null
										&& !currPlayer.userName
												.equalsIgnoreCase(sender
														.getName())) {
									currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
											.indexOf(currPlayer);
								}
							}
						} else {
							if (currPlayer != null
									&& !currPlayer.userName
											.equalsIgnoreCase(sender.getName())) {
								currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
										.indexOf(currPlayer);
							}
						}
					} catch (Exception e) {
						System.out.println("Exception onDisconnectUser...."
								+ e.getLocalizedMessage());
						e.printStackTrace();
					}
				}

				System.out.println("Leave Users New Size... : "
						+ currentGameDetails.gameUserList.size());


				boolean isAllComputerRemains = true;
				for(int i = 0; i < currentGameDetails.gameUserList.size(); i++){
					PlayerDetail playerDetail = currentGameDetails.gameUserList.get(i);
					if(!playerDetail.isComputerPlayer){
						isAllComputerRemains = false;
						break;
					}

				}

				System.out.println("Leave Users New Size... isAllComputerRemains: "
						+ isAllComputerRemains);

				if (isAllComputerRemains && currentGameDetails.gameUserList.size() > 0){
						PlayerDetail playerDetail = currentGameDetails.gameUserList.get(0);
						if(playerDetail.isComputerPlayer){
							if(aiTimer != null){
								aiTimer.cancel();
								aiTimer = null;
							}
//							onUserLeaveRequest(playerDetail.user);
							currentGameDetails.roomDetails.removeUser(playerDetail.user, true);
							onUserLeaveRequest(playerDetail.user);

					}


				}

				if(!isAllComputerRemains  && currentGameDetails.gameUserList.size() > 0){

//					currentGameDetails.processComputerPlayerStates();
				}

			}
		}).start();

//		Timer addNewTimer = new Timer();
//		TimerTask comTask = new TimerTask() {
//
//			@Override
//			public void run() {
//				boolean isAllComputerRemains = true;
//				for(int i = 0; i < currentGameDetails.gameUserList.size(); i++){
//					PlayerDetail playerDetail = currentGameDetails.gameUserList.get(i);
//					if(!playerDetail.isComputerPlayer){
//						isAllComputerRemains = false;
//						break;
//					}
//
//				}
//
//				if(!isAllComputerRemains && currentGameDetails.gameUserList.size() > 0){
//
//					currentGameDetails.processComputerPlayerStates();
//
//				}
//
//			}
//		};
//		addNewTimer.schedule(comTask, 5 * MSecs);


	}

	/** sends and check new round message to all users */
	private void processPassFor(int nextInd) {
		ArrayList<PlayerDetail> arrPlayerDetails = currentGameDetails
				.getPlayerPlayingList();

		if (arrPlayerDetails.size() >= 2) {
			try {
				if (currentGameDetails.currentPlayIndex == -1) {
					return;
				}

				int totalPlayers = currentGameDetails.getTotalPlayingPlayer();

				// System.out.println("processPassFor playStartUsername: "+currentGameDetails.playStartUsername);
				// System.out.println("processPassFor currentPlayuserName: "+currentGameDetails.currentPlayuserName);
				// System.out.println("processPassFor totalPlayers: "+totalPlayers);

				if ((currentGameDetails.playStartUsername
						.equalsIgnoreCase(currentGameDetails.currentPlayuserName))
						&& (totalPlayers - 1) == 0) {
					// Working here on new round.....
					if (currentGameDetails.centerTableCard.size() > 0) {
						currentGameDetails.passcounts = 0;
						currentGameDetails.resetUserGameActions();

						// new round begins. send msg for new round.
						// System.out.println(":::>>>>>New Rounds Begins...");
						currentGameDetails.totalGamerounds++;
						if (currentGameDetails.totalGamerounds <= 3) {
							currentGameDetails.currentGameStatus = GameStatus.GameBettingRound;
						} else {
							currentGameDetails.currentGameStatus = GameStatus.GameRunning;
						}

						currentGameDetails.playStartUsername = currentGameDetails.currentPlayuserName;

						PlayerDetail roundWinner = currentGameDetails
								.getPlayerDetails(currentGameDetails.playStartUsername);
						currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
								.indexOf(roundWinner);

						long amount = 0;
						long raiseAmount = roundWinner.raiseChipAmount;

						if (roundWinner.totalChipAmount == 0) {
							for (PlayerDetail players : arrPlayerDetails) {
								long amount1 = players.raiseChipAmount;
								if (amount1 > raiseAmount) {
									amount1 -= raiseAmount;

									amount += raiseAmount;

									players.totalChipAmount += amount1;
									players.ChipsValue = Long
											.toString(players.totalChipAmount);
								} else if (amount1 <= raiseAmount) {
									amount += amount1;
								}
								players.raiseChipAmount = 0;
								players.roundBetAmount = 0;
							}
						} else {
							for (PlayerDetail players : arrPlayerDetails) {
								amount += players.raiseChipAmount;
								players.raiseChipAmount = 0;
								players.roundBetAmount = 0;
							}
						}

						long totalChips = roundWinner.totalChipAmount;
						totalChips += amount;
						roundWinner.totalChipAmount = totalChips;
						roundWinner.ChipsValue = Long.toString(totalChips);

						long won = Long.parseLong(roundWinner.totalWon);
						// won++;
						roundWinner.totalWon = Long.toString(won);

						currentGameDetails.clearLastCardsData();

						try {
							JSONObject passObj = new JSONObject();

							passObj.put(CardsConstants.kTypeMessageKey,
									CardsConstants.kTypeNewRoundBegin);
							passObj.put(CardsConstants.kUserNameKey,
									roundWinner.getUsernme());
							passObj.put(CardsConstants.kCurrentUserIndexKey,
									currentGameDetails.currentPlayIndex);
							passObj.put(CardsConstants.kGameRoundKey,
									currentGameDetails.totalGamerounds);

							String msg = passObj.toString();

							currentGameDetails.roomDetails.BroadcastChat(
									CardsConstants.SERVER_NAME, msg);
							this.sendInitialBetMessage(true);
							currentGameDetails.centerTableCard.clear();
							this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
						} catch (JSONException e) {
							e.printStackTrace();
							// System.out.println("In Pass reset>> "+e.getLocalizedMessage());
						}
					}
				}
			} catch (Exception e) {
				System.out
						.println("********************In Pass  CUrrentplay index Error.***********");
			}
		}
	}

	/** sends new turn message to all user if required changes */
	private void processUserLeft(String userName) {
		ArrayList<PlayerDetail> arrPlayerDetails = currentGameDetails
				.getPlayerPlayingList();

		if (!(arrPlayerDetails.size() > 1)) {
			// System.out.println("only 1 player leave process");
			return;
		}
		try {
			PlayerDetail player = currentGameDetails.getPlayerDetails(userName);
			int index = arrPlayerDetails.indexOf(player);
			// System.out.println("in processUserLeft currentGameDetails.currentPlayuserName "+currentGameDetails.currentPlayuserName);
			if (currentGameDetails.currentPlayuserName
					.equalsIgnoreCase(player.userName)) {
				// System.out.println("in processUserLeft currentGameplay "+player.userName+" left");
				index++;
				if (index >= arrPlayerDetails.size())
					index = 0;

				// SOME ISSUE HERE..
				PlayerDetail nextPlayer = null;
				while (index < arrPlayerDetails.size()) {
					// System.out.println("user.. idx "+index);
					PlayerDetail nextTurn = arrPlayerDetails.get(index);

					if (nextTurn.userActionStatus.getPlayerActionValue() != PlayerActionStatus.PLAYActionPass
							.getPlayerActionValue()) {
						nextPlayer = nextTurn;
						// System.out.println("LOOP BREAK.... with "+nextPlayer.playerName);
						break;
					}
					// System.out.println("USER NOT SATISFY CONDITION.. with "+nextTurn.playerName);
					index++;
					if (index >= arrPlayerDetails.size())
						index = 0;
				}

				if (nextPlayer != null) {
					// System.out.println("ProcessUserLeft playStartUsername "+currentGameDetails.playStartUsername);
					if (currentGameDetails.playStartUsername
							.equals(player.userName)
							&& currentGameDetails.centerTableCard.size() <= 0) {
						currentGameDetails.playStartUsername = nextPlayer.userName;
						currentGameDetails.passcounts = 0;
						currentGameDetails.resetUserGameActions();
						// System.out.println("Reseting ProcessUserLeft playStartUsername "+currentGameDetails.playStartUsername);
					}

					currentGameDetails.currentPlayIndex = currentGameDetails.gameUserList
							.indexOf(nextPlayer);
					currentGameDetails.currentPlayuserName = nextPlayer.userName;
					if (currentGameDetails.isBettingRound()) {
						System.out.println("** Process User currentGameDetails.gameUserList.remove "+player.playerName);
						currentGameDetails.gameUserList.remove(player);
					}
					this.sendNewTurnPassMessage(nextPlayer.userName);
					this.scheduleUserTimeOut(CardsConstants.UserTimeOut);
					currentGameDetails.centerTableAmount += player.raiseChipAmount;
				} else {
					// System.out.println("ProcessUserLeft issue with nextPlayer..nextPlayer is null");
				}
			} else {
				if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
						.getPlayerStatusValue()) {
					// System.out.println("in processUserLeft normal "+player.userName+" left");
					currentGameDetails.centerTableAmount += player.raiseChipAmount;
				}
			}
		} catch (Exception e) {
			System.out
					.println("in processUserLeft Exception " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void StartUserleftProcess(String userName) {
		try {
			if ((currentGameDetails.currentGameStatus.getGameStatusValue() > GameStatus.GameRunning
					.getGameStatusValue())) {
				// System.out.println("Game Not Started Yet.");
				return;
			}

			if (currentGameDetails.playStartUsername.equalsIgnoreCase(userName)) {
				currentGameDetails.centerTableCard.clear();

				if (currentGameDetails.arrLastCardDetails.size() > 0) {
					HashMap<String, Object> data = (HashMap<String, Object>) currentGameDetails.arrLastCardDetails
							.get(currentGameDetails.arrLastCardDetails.size() - 1);

					String uName = (String) data
							.get(CardsConstants.kLastUserName);
					@SuppressWarnings("unchecked")
					ArrayList<Integer> cards = (ArrayList<Integer>) data
							.get(CardsConstants.kLastCardDetails);
					currentGameDetails.centerTableCard = cards;

					currentGameDetails.playStartUsername = uName;
					currentGameDetails.arrLastCardDetails
							.remove(currentGameDetails.arrLastCardDetails
									.size() - 1);

					int i = 0;
					while (i < currentGameDetails.arrLastCardDetails.size()) {
						HashMap<String, Object> data1 = (HashMap<String, Object>) currentGameDetails.arrLastCardDetails
								.get(i);

						String uName1 = (String) data1
								.get(CardsConstants.kLastUserName);

						if (userName.equalsIgnoreCase(uName1)) {
							currentGameDetails.arrLastCardDetails.remove(data1);
							// break;
						}
						i++;
					}
				} else {
					// cases when first time played player leave and no old
					// cards available
					// System.out.println("... last cards stack zero...cleaning playstartUsername");
					currentGameDetails.playStartUsername = currentGameDetails.currentPlayuserName;
				}
			} else {
				if (currentGameDetails.arrLastCardDetails.size() > 0) {
					int i = 0;
					while (i < currentGameDetails.arrLastCardDetails.size()) {
						HashMap<String, Object> data = (HashMap<String, Object>) currentGameDetails.arrLastCardDetails
								.get(i);

						String uName = (String) data
								.get(CardsConstants.kLastUserName);

						if (userName.equalsIgnoreCase(uName)) {
							currentGameDetails.arrLastCardDetails.remove(data);
							// break;
						}
						i++;
					}
				}
			}
		} catch (Exception e) {
			System.out
					.println("********************In StartUserLeftProcess  CUrrentplay index Error.***********");
			System.out.println("error: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/***
	 * Check Weather minimum users available to next round
	 ***/
	private boolean checkValidUsersGamePlay() {
		ArrayList<PlayerDetail> arrPlayerDetails = currentGameDetails
				.getPlayerPlayingList();

		int count = 0;
		for (PlayerDetail player : arrPlayerDetails) {
			if (player.totalChipAmount == 0) {
				// player.isInGame = false;
				// arrPlayerDetails.remove(player);
				// arrWaitingPlayerDetails.add(player);
				count++;
				// userList.remove(player.user);
				// waitingUsers.add(player.user);
			}
		}

		if ((currentGameDetails.getTotalPlayingPlayer() - count) > 1) {
			return true;
		} else {
			// System.out.println("New Game round will not start ");
			return false;
		}
	}

	/**
	 * isNewRound indicates wheather its new round of first gamepaly
	 * 
	 * @param if TRUE, server side center tble amount will add if FALSE, server
	 *        side center table amount will add and will send msg to all clients
	 */

	private void sendInitialBetMessage(boolean isNewRound) {
		// if (checkValidUsersGamePlay())
		{
			// kTypeGetTableBet
			// Integer value =
			// (Integer)this.gameRoom.getProperties().get(CardsConstants.kRoomDefaultAmount);
			if ((currentGameDetails.totalGamerounds) <= 1)// change here added
															// condition on 17
															// March 2015
			{
				long callAmountDefault = currentGameDetails
						.getDefaultCallRaiseAmunt();
				// System.out.println("***Getting Bet Amount "+callAmountDefault);

				ArrayList<PlayerDetail> arrPlayerDetails = currentGameDetails
						.getPlayerPlayingList();

				long callAmount = callAmountDefault;// value.intValue();
				long amount = 0;
				for (PlayerDetail player : arrPlayerDetails) {
					// System.out.println("**PLAYER NAME "+player.playerName);
					// System.out.println("**PLAYER totalChipAmount "+player.totalChipAmount);
					// System.out.println("**PLAYER ChipsValue "+player.ChipsValue);
					player.totalChipAmount -= callAmount;
					amount += callAmount;
					player.ChipsValue = Long.toString(player.totalChipAmount);
					// System.out.println("**New PLAYER totalChipAmount "+player.totalChipAmount);
					// System.out.println("**New PLAYER ChipsValue "+player.ChipsValue);
				}

				if (!isNewRound) {
					// System.out.println("before value CentarTableAmount : "+currentGameDetails.centerTableAmount);
					currentGameDetails.centerTableAmount = amount;
					System.out.println("***CentarTableAmount : "
							+ currentGameDetails.centerTableAmount);
					JSONObject obj = new JSONObject();
					try {
						obj.put(CardsConstants.kTypeMessageKey,
								CardsConstants.kTypeGetTableBet);
						obj.put(CardsConstants.kCurrentTableAmountKey,
								currentGameDetails.centerTableAmount);
						String strObj = obj.toString();

						this.currentGameDetails.roomDetails.BroadcastChat(
								CardsConstants.SERVER_NAME, strObj);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void sendInitialGameState() {
		JSONObject obj = new JSONObject();
		try {
			ArrayList<PlayerDetail> arrPlayerDetails = currentGameDetails
					.getPlayerPlayingList();

			obj.put(CardsConstants.kTypeMessageKey,
					CardsConstants.kTypeInitStatus);
			obj.put(CardsConstants.kCurrentTableAmountKey,
					currentGameDetails.centerTableAmount);
			obj.put(CardsConstants.kPlayerDetailsCount, arrPlayerDetails.size());

			JSONArray playingObj = new JSONArray();
			for (PlayerDetail player : arrPlayerDetails) {
				JSONObject objplayer = new JSONObject();
				objplayer.put(CardsConstants.kUserNameKey, player.userName);

				objplayer.put(CardsConstants.kUserName, player.playerName);
				objplayer.put(CardsConstants.kUserTotalHands, player.totalPlay);
				objplayer.put(CardsConstants.kUserTotalWonHands,
						player.totalWon);
				objplayer.put(CardsConstants.kUserGamesChipsValue,
						player.ChipsValue);
				objplayer.put(CardsConstants.kUserAccountID, player.userID);
				objplayer.put(CardsConstants.kUserGiftID, player.defaultGiftID);
				objplayer.put(CardsConstants.kUserImageUrl,
						player.profileImageUrl);
				playingObj.put(objplayer);
			}
			obj.put(CardsConstants.kUserListKey, playingObj);

			JSONObject roomInfo = currentGameDetails.sendRoomInfo();
			obj.put(CardsConstants.kRoomDetails, roomInfo);

			String msg = obj.toString();
			// System.out.println("sended initi msg "+msg);
			this.currentGameDetails.roomDetails.BroadcastChat(
					CardsConstants.SERVER_NAME, msg);
		} catch (JSONException e) {
			System.out.println("error " + e.getMessage());
			e.printStackTrace();
		}
	}

	private ArrayList<Integer> convertJsonToCardList(JSONArray arrCardObj) {
		ArrayList<Integer> cardsList = new ArrayList<Integer>();
		// System.out.println("****Card Values Start**** TOTAL "+arrCardObj.length());
		for (int i = 0; i < arrCardObj.length(); i++) {
			Integer value;
			try {
				value = arrCardObj.getInt(i);
				// System.out.println("****Card Values "+value);
				cardsList.add(value);
			} catch (Exception e) {
				System.out
						.println("******\n***....Inside Card Convert error.....*****\n*****");
				e.printStackTrace();
			}
		}
		return cardsList;
	}

	private void clearAllUserDetails() {
		// System.out.println("****clearAllUserDetails*****");
		ArrayList<PlayerDetail> userList = currentGameDetails
				.getPlayerPlayingList();

		// for(PlayerDetail user : userList)
		// {
		// System.out.println("USER> : "+user.userName);
		// }

		currentGameDetails.centerTableCard.clear();
		lastWinner = null;

		if (userList.size() <= 1) {
			currentGameDetails.resetGameTable();
		}

		if (userList.size() == 0) {
			currentGameDetails.centerTableAmount = 0;
			currentGameDetails.resetNextTurn();
			currentGameDetails.resetGameTable();
			// System.out.println("Center Table Zero Now");
		}
	}

	public void userPause(IUser user) {

	}

	public void userResume(IUser user) {
		// currentGameDetails.sendResumeGameStauts(user);
	}

	/*** Computer players implementation flow. ***/


	public void fireEvent(final CardsConstants.AIFireEvent eventType){

		final PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);

//		System.out.println("fireEvent currentGameDetails.currentPlayuserName: " + currentGameDetails.currentPlayuserName + "  isComputerPlayer : " + myDetails.isComputerPlayer);

		if(myDetails.isComputerPlayer == true) {

			int randPercentTime = CMUtility.randomNumberBetween(35, 90);
			float timeMustTaken = currentGameDetails.getRoomInterval();

//			System.out.println("timeMustTaken : " + timeMustTaken);

			timeMustTaken = timeMustTaken * randPercentTime;

//			System.out.println("Random timeMustTaken : " + timeMustTaken);

//				callByAI();

			if (eventType == CardsConstants.AIFireEvent.Won) {

				Timer timer1 = new Timer();
				TimerTask delayThreadStartTask1 = new TimerTask() {
					@Override
					public void run() {

						sendAIWin(myDetails);
					}
				};
				timer1.schedule(delayThreadStartTask1, (long) (1 * MSecs));

			} else {

				aiTimer = new Timer();
				TimerTask delayedThreadStartTask = new TimerTask() {
					@Override
					public void run() {

						switch (eventType) {

							case Call: {
								callByAI();

							}
							break;

							case Pass: {
								sendPassByAi();

							}
							break;

							case Bet: {

								betByAI();
							}
							break;

							case PlayCard: {
//								if (currentGameDetails.isBettingRound()) {
//									betByAI();
//								} else {
								if(!currentGameDetails.isBettingRound())
									playCardsByAi();
//								}
							}
							break;
							case Won: {



							}
							break;
							default: {

							}
							break;
						}


//				callByAI();

					}
				};
				aiTimer.schedule(delayedThreadStartTask, (long) (timeMustTaken * MSecs));

			}
		}

	}


	//BET
	public void betByAI(){
		PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);


		if (currentGameDetails.isUserAllIn(myDetails))
		{
			checkByAI();
			return;
		}

		long max = myDetails.totalChipAmount;
		long betAmount = (long)(max * calculateAmountToBet()/100);
		if(betAmount <= 0){

			checkByAI();
			return;
		}

//		System.out.println("Username : " + myDetails.userName + "  betByAI : " + betAmount + "max : " + max + " myDetails.ChipsValue : " + myDetails.totalChipAmount);

		processBettingRoundCheckBet(betAmount, CardsConstants.kTypeBetRoundBet, true);


	}

	private  long calculateAmountToBet(){

		return (CMUtility.randomNumberBetween(10,20));
	}



	//Check
	public void checkByAI(){

		try{
			PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);

//			System.out.println("processBettingRoundCheckBet : " + betAmount);

			JSONObject passObj = new JSONObject();
//
			passObj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeBetRoundCheck);
			passObj.put(CardsConstants.kUserNameKey, currentGameDetails.currentPlayuserName);
			passObj.put(CardsConstants.kUserTurnKey, currentGameDetails.nextPlayerTurn().userName);
			passObj.put(CardsConstants.kUserPassCount, currentGameDetails.passcounts);
			passObj.put(CardsConstants.kGameRoundKey, currentGameDetails.totalGamerounds);
			passObj.put(CardsConstants.kCurrentTableAmountKey, currentGameDetails.centerTableAmount);

//
			String msg = passObj.toString();
			currentGameDetails.roomDetails.BroadcastChat(currentGameDetails.currentPlayuserName, msg);
			handleChatRequest(myDetails.user, msg, new HandlingResult());

		}
		catch (Exception e){

			e.printStackTrace();
		}

	}


	//CALL
	public void callByAI(){

//		long amountToCall = currentBetAmount;
//		System.out.println("callByAI(long amountToCall)");

		PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);
		long amountToCall = myDetails.previousBetValueByPlayer - myDetails.raiseChipAmount;

		myDetails.previousBetValueByPlayer = 0;
		if(currentGameDetails.isBettingRound()){

			long myChips = Long.parseLong(myDetails.ChipsValue);
//			System.out.println("amount To call : " + amountToCall + "  mychips : " + myChips + "   myDetails.totalChipAmount   " + myDetails.totalChipAmount);
			if(myChips == 0){

				processBettingRoundCheckBet(0,CardsConstants.kTypeBetRoundBet,false);
			}
			else{
				if(amountToCall < myChips){

					processBettingRoundCheckBet(amountToCall,CardsConstants.kTypeBetRoundBet,false);
				}
				else{

					processBettingRoundCheckBet(myChips,CardsConstants.kTypeBetRoundBet,false);
				}

			}

		}



//		currentGameDetails.createComputerPlayerInGame();


	}

	//Raise
	public void processBettingRoundCheckBet(long betAmount,int betType,boolean isBet){

		try{
			PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);

//			System.out.println("processBettingRoundCheckBet : " + betAmount);

			JSONObject passObj = new JSONObject();
//
			passObj.put(CardsConstants.kTypeMessageKey, betType);
			passObj.put(CardsConstants.kUserNameKey, currentGameDetails.currentPlayuserName);
			passObj.put(CardsConstants.kUserTurnKey, currentGameDetails.nextPlayerTurn().userName);
			passObj.put(CardsConstants.kUserCallAmountKey, betAmount);

			if(isBet)
			{
				passObj.put(CardsConstants.kGameBetOn, 1);
			}

			String msg = passObj.toString();
			currentGameDetails.roomDetails.BroadcastChat(currentGameDetails.currentPlayuserName, msg);
			handleChatRequest(myDetails.user, msg, new HandlingResult());

		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}

	//SEND PASS

	public void sendPassByAi(){

		try{



//			System.out.println("currentGameDetails.centerTableCard.get(0) : " + currentGameDetails.centerTableCard.get(0));

			PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);

//			System.out.println("sendPassByAi");
			JSONObject passObj = new JSONObject();
//
			passObj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeUserPass);
			passObj.put(CardsConstants.kUserNameKey, currentGameDetails.currentPlayuserName);
			passObj.put(CardsConstants.kUserTurnKey, currentGameDetails.nextPlayerTurn().userName);
			passObj.put(CardsConstants.kUserPassCount, currentGameDetails.passcounts);
			passObj.put(CardsConstants.kGameRoundKey, currentGameDetails.totalGamerounds);
			passObj.put(CardsConstants.kCurrentTableAmountKey, currentGameDetails.centerTableAmount);

			String msg = passObj.toString();
//			handleClientPassTimeout(myDetails.user, msg);
			System.out.println("Pass : " + msg);

			handleChatRequest(myDetails.user, msg, new HandlingResult());
			currentGameDetails.roomDetails.BroadcastChat(currentGameDetails.currentPlayuserName, msg);


		}
		catch (Exception e) {

			e.printStackTrace();
		}

	}

	//Play
	public void playCardsByAi(){

//		sendPassByAi();
//		return;

		PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);

		int cardsOnTable = currentGameDetails.centerTableCard.size();
		if(cardsOnTable == 0){
			System.out.println("playBestCardsAccordingToExperience : " + myDetails.cardList.size());
			int startNo = myDetails.computerDetails.getStartNoAccToProbAndExp();
			playBestCardsAccordingToExperience(myDetails.cardList, startNo);
		}
		else{
			System.out.println("checkNextHighestHint");
			int startNo = myDetails.computerDetails.getStartNoAccToProbAndExp();
			if(startNo >= cardsOnTable)
			{
				checkNextHighestHint(myDetails.cardList, cardsOnTable);
			}
			else{
				sendPassByAi();
			}
		}

	}

	public void playBestCardsAccordingToExperience(ArrayList<Card> arrayCards, int startIndex){

		ArrayList<Integer> arrCardIndex = null;

		ArrayList<CMCard> arrUserCards = new ArrayList<CMCard>();

		for (int i=0; i< arrayCards.size(); i++)
		{
			CMCard card = new CMCard(arrayCards.get(i));
			arrUserCards.add(card);
		}

		int startNo = startIndex;
		for(int i = startNo; i >= 0; i--){
			arrCardIndex = checkCanPlayCards(arrUserCards, i);
			if(arrCardIndex != null && arrCardIndex.size() > 0){
				break;
			}
		}





		if (arrCardIndex!=null && arrCardIndex.size() > 0){
			// may found
			System.out.println("playBestCardsAccordingToExperience sendPlayCards");
			sendPlayCards(arrCardIndex);

		}else{
			//pass
			System.out.println("playBestCardsAccordingToExperience sendPassByAi");
			sendPassByAi();
		}

	}

	public void checkNextHighestHint(ArrayList<Card> arrayCards, int cardsOnTable){


		ArrayList<Integer> arrCardIndex = null;

		ArrayList<CMCard> centerTblCards = new ArrayList<>();

		for (int i =0;i<currentGameDetails.centerTableCard.size(); i++)
		{

			CMCard card = new CMCard(currentGameDetails.centerTableCard.get(i));
			centerTblCards.add(card);
		}

		ArrayList<CMCard> arrUserCards = new ArrayList<>();

		for (int i=0; i< arrayCards.size(); i++)
		{
			CMCard card = new CMCard(arrayCards.get(i));
			arrUserCards.add(card);
		}


		switch (cardsOnTable)
		{
			case 8:
			case 7:
			case 6:
			case 5:
			{
				arrCardIndex = CMPokerUtils.getPossibleFiveCardsHints(arrUserCards, centerTblCards);
			}break;
			case 4:
			{
				arrCardIndex = CMPokerUtils.getPossibleFourCardsHints(arrUserCards, centerTblCards);
			}break;
			case 3:
			{
				arrCardIndex = CMPokerUtils.getPossibleThreeCardsHints(arrUserCards, centerTblCards);
			}break;
			case 2:
			{
				arrCardIndex = CMPokerUtils.getPossibleTwoCardsHints(arrUserCards, centerTblCards);
			}break;
			case 1:
			{
				arrCardIndex = CMPokerUtils.getPossibleOneCardsHints(arrUserCards, centerTblCards);
			}break;
		}

		if (arrCardIndex!=null && arrCardIndex.size() > 0)
		{
			// may found
//        this->showHintUserCards(arrCardIndex);

			sendPlayCards(arrCardIndex);
		}
		else
		{
			// no hint found for one cards.

			//pass
			sendPassByAi();
		}

	}

	ArrayList<Integer> checkCanPlayCards(ArrayList<CMCard>aiCards, int cardNo){
		ArrayList<Integer> arrCardIndex = null;
//    CCArray* handCards = CCArray::create();

//    for (int i =0 ; i<aiCards->count(); i++)
//    {
//        Card* card = (Card*)aiCards->objectAtIndex(i);
//        handCards->addObject(card);
//    }

//	int type1 = 0;
//		ArrayList<CMCard> arrayCards1 = new ArrayList<CMCard>();
//		for(int j = 0; j < 8; j++){
//			CMCard card = new CMCard(101 + j);
//			arrayCards1.add(card);
//
//		}
//
//		ArrayList<Integer> arrCardIndexCheck = CMPokerUtils.getBestFiveCardHandsLowest(arrayCards1, type1);
//		System.out.println("**************** TEST arrCardIndex : case 5 " + arrCardIndexCheck);

		int type = 5;
		switch (cardNo) {
			case 5:
			{
				arrCardIndex = CMPokerUtils.getBestFiveCardHandsLowest(aiCards, type);
				System.out.println("checkCanPlayCards arrCardIndex : case 5 " + arrCardIndex);
			}break;
			case 4:
			{
				arrCardIndex = CMPokerUtils.getBestFourCardHandsLowest(aiCards, type);
				System.out.println("checkCanPlayCards arrCardIndex : case 4 " + arrCardIndex);
			}break;
			case 3:
			{
				arrCardIndex = CMPokerUtils.getBestThreeCardHandsLowest(aiCards, type);
				System.out.println("checkCanPlayCards arrCardIndex : case 3 " + arrCardIndex);
			}break;
			case 2:
			{
				arrCardIndex = CMPokerUtils.getBestTwoCardHandsLowest(aiCards);
				System.out.println("checkCanPlayCards arrCardIndex : case 2 " + arrCardIndex);
			}break;
			case 1:
			{
				arrCardIndex = CMPokerUtils.getBestOneCardHandsLowest(aiCards);
				System.out.println("checkCanPlayCards arrCardIndex : case 1 " + arrCardIndex);
			}break;


			default:
				break;
		}


		System.out.println("checkCanPlayCards arrCardIndex : " + arrCardIndex);
		return arrCardIndex;
	}


	public void sendPlayCards(ArrayList<Integer> arrCardIndex){

		try {

			PlayerDetail myDetails = currentGameDetails.getPlayerDetails(currentGameDetails.currentPlayuserName);

			ArrayList<Card> cardsToCheckWin = new ArrayList<Card>();
			cardsToCheckWin.addAll(myDetails.cardList);

			JSONObject passObj = new JSONObject();
//
			passObj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeCardDistribution);
			passObj.put(CardsConstants.kUserNameKey, currentGameDetails.currentPlayuserName);
			passObj.put(CardsConstants.kUserTurnKey, currentGameDetails.nextPlayerTurn().userName);

			JSONArray cardStr = new JSONArray();
			for (int i = 0; i < arrCardIndex.size(); i++) {
				Card card = myDetails.cardList.get(arrCardIndex.get(i));
				int cardValue = card.getCards().getCardValue()
						+ card.getSuits().getSuitsValue();
				cardStr.put(cardValue);

				cardsToCheckWin.remove(card);
			}

			passObj.put(CardsConstants.kUserCardsKey, cardStr);

			passObj.put(CardsConstants.kGameRoundKey, currentGameDetails.totalGamerounds);
			passObj.put(CardsConstants.kCurrentTableAmountKey, currentGameDetails.centerTableAmount);

			String msg = passObj.toString();
//			handleClientPassTimeout(myDetails.user, msg);

			System.out.println("Play Cards : " + msg + "  myDetails.user : " + myDetails.user.getName());




//			if(!isWon){

				handleChatRequest(myDetails.user, msg, new HandlingResult());
				currentGameDetails.roomDetails.BroadcastChat(currentGameDetails.currentPlayuserName, msg);
			boolean isWon = checkAIWonProcess(cardsToCheckWin, myDetails);

//			}


		}catch (Exception e){

			e.printStackTrace();
		}


	}


	//send AI WIN

	boolean checkAIWonProcess(ArrayList<Card>  cardArrayList, final PlayerDetail myDetails){


		if (cardArrayList.size()==0)
		{
			System.out.println("AI won");

			Timer timer = new Timer();
			TimerTask delayedThreadStartTask = new TimerTask() {
				@Override
				public void run() {

					sendAIWin(myDetails);

				}
			};
			timer.schedule(delayedThreadStartTask, (long) (1 * MSecs));

			return true;
		}

		return false;
	}

	public void sendAIWin(final PlayerDetail myDetails){

		try{

			JSONObject passObj = new JSONObject();
//
			passObj.put(CardsConstants.kTypeMessageKey, CardsConstants.kTypeWinner);
			passObj.put(CardsConstants.kUserNameKey, myDetails.userName);
			passObj.put(CardsConstants.kCurrentTableAmountKey, currentGameDetails.centerTableAmount);

			String msg = passObj.toString();
//			handleClientPassTimeout(myDetails.user, msg);

			System.out.println("Play Cards : " + msg);

			currentGameDetails.roomDetails.BroadcastChat(myDetails.userName, msg);
			handleChatRequest(myDetails.user, msg, new HandlingResult());


		}catch (Exception e){

			e.printStackTrace();;
		}

	}



	//RPC CALL
	public String getLiveUserInfo(String username){
		String str = currentGameDetails.getPlayerDetails(username).user.getCustomData();

		System.out.println("getLiveUserInfo : " + str);

		return str;

	}

	public String setMinUsersToStartGame(String message){
		String str = "";
		System.out.println("setMinUsersToStartGame ");
		try{
			if(currentGameDetails.gameUserList.size() == 1){
				JSONObject jsonObject = new JSONObject(message);
				currentGameDetails.minPlayerToStartGame = jsonObject
						.getInt(CardsConstants.kMinPlayerKey);

				System.out.println("minPlayerRequire : " + currentGameDetails.minPlayerToStartGame);
			}


		}catch (Exception e){
			e.printStackTrace();
		}



		return str;
	}



	// public boolean checkMinimumPlayer()
	// {
	//
	// }
}