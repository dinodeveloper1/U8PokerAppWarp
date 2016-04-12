package pokercard;

import java.util.ArrayList;

import org.json.JSONObject;

import rummydemo.CMAIConstants;
import rummydemo.CMUtility;
import rummydemo.CardsConstants;

import com.shephertz.app42.server.domain.User;
import com.shephertz.app42.server.idomain.IUser;
import computerpackage.ComputerDetails;

public class PlayerDetail 
{
	public PlayerActionStatus userActionStatus;
	public PlayerStatus userPlayStatus;
	
	public IUser user;
	public String userName;
	public String userID;
	public String defaultGiftID;
	public String profileImageUrl;
	public String playerName,totalWon,totalPlay,ChipsValue;
	
	public ArrayList<Card> cardList;
	public long raiseChipAmount;
	public long totalChipAmount;
	public long roundBetAmount;
	public int raiseCounter;
	/** all bet amount store*/
	public long allBetRoundAmount;
	
	public boolean isComputerPlayer;
	public ComputerDetails computerDetails;
	public long previousBetValueByPlayer;
	
	/**
	 * Will Use to know wheather user is in game or Fold.
	 * */
	
	public boolean isInGame; 
	public boolean didPass;
	public boolean didBet;
	
	public PlayerDetail(IUser tempUser)
	{
		this.user = tempUser;
		this.userName= tempUser.getName(); 
		cardList = new ArrayList<Card>();
		isInGame = true;
		didPass = false;
		this.userActionStatus = PlayerActionStatus.PLAYActionNone;
		this.userPlayStatus = PlayerStatus.Offline;
		this.storeUserDetails();
		isComputerPlayer = false;
		computerDetails = null;
	}
	public PlayerDetail(boolean computerPlayer,IUser tempUser)
	{
		this.user = tempUser;
		this.userName= tempUser.getName(); 
		cardList = new ArrayList<Card>();
		isInGame = true;
		didPass = false;
		this.userActionStatus = PlayerActionStatus.PLAYActionNone;
		this.userPlayStatus = PlayerStatus.Offline;
		
		isComputerPlayer = computerPlayer;
		computerDetails = null;
		if (!isComputerPlayer) {
			this.storeUserDetails();	
		}
	}
	
	void storeUserDetails()
	{
		try
		{
			JSONObject jsonObject = new JSONObject(this.user.getCustomData());
			String playerName = jsonObject.getString(CardsConstants.kUserName);
			String total = jsonObject.getString(CardsConstants.kUserTotalHands);
			String totalWon = jsonObject.getString(CardsConstants.kUserTotalWonHands);						
			String chipsValue = jsonObject.getString(CardsConstants.kUserGamesChipsValue);
			String userID = jsonObject.getString(CardsConstants.kUserAccountID);
			String giftID = jsonObject.getString(CardsConstants.kUserGiftID);
			String imageUrl = jsonObject.getString(CardsConstants.kUserImageUrl);
			
			this.totalChipAmount = Long.parseLong(chipsValue);			
			this.playerName = playerName;
			this.totalPlay= total;
			this.totalWon= totalWon;
			this.ChipsValue = chipsValue;
			this.userID = userID;		
			this.defaultGiftID = giftID;
			this.profileImageUrl = imageUrl;
		}
		catch(Exception e)
		{
			
		}
	}
	public void setDefaultValuesforComputer()
	{
		this.totalPlay = "0";
		this.totalWon= "0";
		this.defaultGiftID = "0";
		this.profileImageUrl = "";
		this.ChipsValue = Long.toString(totalChipAmount);
		raiseChipAmount = 0;
		totalChipAmount = 0;
		roundBetAmount = 0;
		userID = "" + CMUtility.randomNumberBetween(1,100);
		
	}

	public void setRandomValuesForComputerInRef(PlayerDetail playerDetail){

		int refTotalPlay = Integer.parseInt(playerDetail.totalPlay);
		int refTotalWin = Integer.parseInt(playerDetail.totalWon);

		if(refTotalPlay < 10){
			refTotalPlay = 50;
		}
		if(refTotalWin < 10){
			refTotalWin = 30;
		}

		float percentageWin = CMUtility.getPercentage(refTotalWin, refTotalPlay);

		int totalPlayFactor = CMUtility.randomNumberBetween(CMAIConstants.kMatchPlayedFactorMin, CMAIConstants.kMatchPlayedFactorMax);
		int totalPlayed = (int)(refTotalPlay * totalPlayFactor/100);

		this.totalPlay =  "" + totalPlayed;

		int winPercentage =  CMUtility.randomNumberBetween(CMAIConstants.kWinPercentageIntermendiateMin, CMAIConstants.kWinPercentageIntermendiateMax);

		int won =  totalPlayed * winPercentage/100;

		this.totalWon = "" + won;
		this.defaultGiftID = "0";
		this.profileImageUrl = "";
		this.ChipsValue = playerDetail.ChipsValue;
		this.ChipsValue = "" + CMUtility.randomNumberBetween(1,Integer.parseInt(playerDetail.ChipsValue));


		System.out.println("playerDetail.ChipsValue : " + playerDetail.ChipsValue);

		raiseChipAmount = 0;
//		totalChipAmount = Long.parseLong(playerDetail.ChipsValue);
		totalChipAmount = Long.parseLong(this.ChipsValue);

		roundBetAmount = 0;
//		this.userName = CMUtility.getRandomName();
		this.playerName = this.userName.substring(0, this.userName.length() - 4);

		this.userID = "" + CMUtility.randomNumberBetween(100000,150000);

	}

	public String getPlayerDetailsJson(){

		String str = "";

		try{
			JSONObject objplayer = new JSONObject();
			objplayer.put(CardsConstants.kUserNameKey, this.userName);

			objplayer.put(CardsConstants.kUserName, this.playerName);
			objplayer.put(CardsConstants.kUserTotalHands, this.totalPlay);
			objplayer.put(CardsConstants.kUserTotalWonHands,
					this.totalWon);
			objplayer.put(CardsConstants.kUserGamesChipsValue,
					this.ChipsValue);
			objplayer.put(CardsConstants.kUserAccountID, this.userID);
			objplayer.put(CardsConstants.kUserGiftID, this.defaultGiftID);
			objplayer.put(CardsConstants.kUserImageUrl,
					this.profileImageUrl);
			objplayer.put(CardsConstants.kUserChipsValue, "" + this.totalChipAmount);

			str = objplayer.toString();

		}catch (Exception e){

			e.printStackTrace();
		}

		return str;

	}

     /**
	  * Returns the IUser of Player
	  */
//	public IUser getUser()
//	{
//		return user;
//	}
	/**
	  * Returns the Username of Player
	  */	
	public String getUsernme()
	{
		return userName;
	}
	/**
	  * set CardList for Player
	  * @param ArrayList<Card> typeData
	  */	
	public void setCardList(ArrayList<Card> tempcardList)
	{
		cardList.addAll(tempcardList);		
	}
}
///**
//* Returns the runtime class of this {@code Object}. The returned
//* {@code Class} object is the object that is locked by {@code
//* static synchronized} methods of the represented class.
//*
//* <p><b>The actual result type is {@code Class<? extends |X|>}
//* where {@code |X|} is the erasure of the static type of the
//* expression on which {@code getClass} is called.</b> For
//* example, no cast is required in this code fragment:</p>
//*
//* <p>
//* {@code Number n = 0;                             }<br>
//* {@code Class<? extends Number> c = n.getClass(); }
//* </p>
//*
//* @return The {@code Class} object that represents the runtime
//*         class of this object.
//* @jls 15.8.2 Class Literals
//*/
