/**
 * 
 */
package pokercard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rummydemo.CMAIConstants;
import rummydemo.CMUtility;
import rummydemo.CardsConstants;

import com.shephertz.app42.server.idomain.HandlingResult;
import com.shephertz.app42.server.idomain.IRoom;
import com.shephertz.app42.server.idomain.IUser;
import computerpackage.*;
import rummydemo.RoomExtension;

/**
 * @author Rahul Singh
 */

public class GameDetails {
	/** holds next play turn */
	int nextGamePlayTurn;

	/** current room informatoin */
	public IRoom roomDetails;

	/** store current game status */
	public GameStatus currentGameStatus;

	/** contains all user list information */
	public ArrayList<PlayerDetail> gameUserList;

	/** store total game rounds */
	public int totalGamerounds;

	/** total pass counts */
	public int passcounts;

	/** currentPlayIndex */
	public int currentPlayIndex;

	/** play start user name */
	public String playStartUsername;

	/** current play username */
	public String currentPlayuserName;

	/** Start Player indicator */
	public String startPlayUserName;

	/** holds center table cards details */
	public ArrayList<Integer> centerTableCard;

	/** holds previous users cards details */
	public ArrayList<HashMap<String, Object>> arrLastCardDetails;

	/** game table amounts */
	public long centerTableAmount;

	/** card deck information */
	Deck cardDeck = new Deck();

	// Computer player updates
	ComputerHelper computerhelperDetails;
	ComputerDetails computerPlayerInGame;
	Timer computerPlayerTimer;

	//Room extension Ref
	public RoomExtension roomExtension;

	public int minPlayerToStartGame = 2;
	public long playerChipsInitally = 0;

	private final int MSecs = 1000;

	public ArrayList<String> arrIgnoreList;

	public GameDetails(IRoom roomInfo) {
		this.roomDetails = roomInfo;
		currentGameStatus = GameStatus.GameEnd;
		gameUserList = new ArrayList<PlayerDetail>();
		centerTableCard = new ArrayList<Integer>();
		arrLastCardDetails = new ArrayList<HashMap<String, Object>>();
		centerTableAmount = 0;
		nextGamePlayTurn = 0;
		currentPlayIndex = -1;
		playStartUsername = "";
		currentPlayuserName = "";
		arrIgnoreList = new ArrayList<String>();
		startPlayUserName = "";
		computerhelperDetails = new ComputerHelper(this);
		roomExtension = null;
        
	}

	/** deafult table call amount */
	public long getDefaultCallRaiseAmunt() {
		Object callValue = roomDetails.getProperties().get(
				CardsConstants.kRoomDefaultAmount);
		Long longValue = (long) 100;
		if (callValue instanceof String) {
			String str = callValue.toString();
			// System.out.println("value inside room "+str);
			longValue = Long.parseLong(str);
		} else if (callValue instanceof Integer) {
			longValue = ((Integer) callValue).longValue();
		}

		return longValue.longValue();
	}

	/** reset nextplayer turn index */
	public void resetNextTurn() {
		nextGamePlayTurn = 0;
	}

	/** reset game flags at end of game */
	public void resetGameTable() {
		totalGamerounds = 0;
		passcounts = 0;
		cardDeck = null;
		cardDeck = new Deck();
		playStartUsername = "";
		currentPlayuserName = "";

		this.clearLastCardsData();

		for (PlayerDetail players : gameUserList) {
			players.userActionStatus = PlayerActionStatus.PLAYActionNone;
		}
		currentPlayIndex = -1;
		currentGameStatus = GameStatus.GameEnd;
	}

	/** reset game flags at end of game rounds */
	public void resetGameRound() {
		// totalGamerounds = 0;
		passcounts = 0;

		for (PlayerDetail players : gameUserList) {
			players.userActionStatus = PlayerActionStatus.PLAYActionNone;
		}
	}

	/**
	 * This method return last element of TOTAL_CARDS In case of empty list
	 * again shuffle cards
	 */
	public Card getNewCard() {
		if (cardDeck.hasMoreCards()) {
			Card cardis = cardDeck.dealCard();
			return cardis;
			// Integer cardValue = cardis.getCardValue().getCardValue()
			// +cardis.getSuit().getSuitsValue();
			// return cardValue;
		} else {
			cardDeck = null;
			cardDeck = new Deck();
			Card cardis = cardDeck.dealCard();
			return cardis;
			// Integer cardValue = cardis.getCardValue().getCardValue()
			// +cardis.getSuit().getSuitsValue();
			// return cardValue;
		}
	}

	/** check all user bets status */
	public boolean allUserBetsDone() {
		long maxBet = 0;
		int count, totalPlayers;
		totalPlayers = count = 0;
		ArrayList<PlayerDetail> arrPlayerDetails = this.getPlayerPlayingList();

		for (PlayerDetail player : arrPlayerDetails) {
			if (player.roundBetAmount > maxBet) {
				maxBet = player.roundBetAmount;
			}
		}

		for (PlayerDetail player : arrPlayerDetails) {
			totalPlayers++;
			if (player.roundBetAmount == maxBet /*
												 * ALL IN Case condition add
												 * herer
												 */
					|| player.totalChipAmount == 0) {
				count++;
			}
		}

		// for(PlayerDetail player : arrPlayerDetails)
		// {
		// // if(player.isInGame == true)
		// {
		// totalPlayers++;
		// if (maxBet!=0)
		// {
		// if (player.roundBetAmount == maxBet /*ALL IN Case condition add
		// herer*/ || player.totalChipAmount ==0)
		// {
		// count++;
		// }
		// }
		// else
		// {
		// if (player.roundBetAmount !=0)
		// {
		// maxBet = player.roundBetAmount;
		// count++;
		// }
		// }
		// }
		// }

		if (count == totalPlayers) {
			return true;
		}
		return false;
	}

	/** check all user checked status */
	public boolean allUserChecked() {
		int check = 0;
		int totalPlayer = 0;
		ArrayList<PlayerDetail> arrPlayerDetails = this.getPlayerPlayingList();

		for (PlayerDetail player : arrPlayerDetails) {
			// if(player.isInGame == true)
			{
				totalPlayer++;
				if (player.userActionStatus.getPlayerActionValue() == PlayerActionStatus.PLAYActionCheck
						.getPlayerActionValue()
						/* ALL IN Case condition add herer */|| player.totalChipAmount == 0) {
					check++;
				}
			}
		}

		if (check == totalPlayer) {
			return true;
		}
		return false;
	}

	/** get nextplayer turn */
	public int getNextPlayerTurn() {
		int totalPlayers = this.getTotalPlayingPlayers();
		PlayerDetail player = null;
		do {
			nextGamePlayTurn++;
			if (nextGamePlayTurn >= totalPlayers) {
				nextGamePlayTurn = 0;
			}
			player = gameUserList.get(nextGamePlayTurn);

		} while (player.userPlayStatus.getPlayerStatusValue() > PlayerStatus.Playing
				.getPlayerStatusValue());

		return nextGamePlayTurn;
	}

	public PlayerDetail nextPlayerTurn() {
		int totalPlayer = gameUserList.size();

		PlayerDetail next;

		// int nextIdx = currentPlayIndex+1;
		//
		int nextIdx = getPlayerIndexWithUsername(currentPlayuserName) + 1;

		if (nextIdx >= totalPlayer) {
			nextIdx = 0;
		}

		if (this.isBettingRound()) {
			do {
				next = gameUserList.get(nextIdx);
				// &&
				// next.userActionStatus.getPlayerActionValue()!=PlayerActionStatus.PLAYActionPass.getPlayerActionValue()
				if (next.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Playing
						.getPlayerStatusValue()

				&& next.totalChipAmount != 0) {
					break;
				}
				nextIdx++;
				if (nextIdx >= totalPlayer) {
					nextIdx = 0;
				}
			} while (true);
		} else {
			do {
				next = gameUserList.get(nextIdx);
				if (next.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Playing
						.getPlayerStatusValue()
						&& next.userActionStatus.getPlayerActionValue() != PlayerActionStatus.PLAYActionPass
								.getPlayerActionValue()) {
					break;
				}
				nextIdx++;
				if (nextIdx >= totalPlayer) {
					nextIdx = 0;
				}
			} while (true);
		}

		return next;
	}

	/** get Playing Player counts */
	public int getTotalPlayingPlayers() {
		int n = 0;
		for (PlayerDetail player : gameUserList) {
			if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Playing
					.getPlayerStatusValue()) {
				n++;
			}
		}
		return n;
	}

	/** get Fold Player counts */
	public int getFoldTotalCountPlayers()
	{
		int n = 0;

		for (PlayerDetail player : gameUserList) {
			if ((player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Playing
					.getPlayerStatusValue()) && (player.userActionStatus != PlayerActionStatus.PLAYActionBet.PLAYActionPass || player.totalChipAmount == 0)) {
				n++;
			}
		}
		return n;
	}

	/** check betting round on / other rounds */
	public boolean isBettingRound() {
		if (totalGamerounds <= 3
				&& currentGameStatus.getGameStatusValue() <= GameStatus.GameBettingRound
						.getGameStatusValue()) {
			return true;
		}
		return false;
	}

	/** clear last card details */
	public void clearLastCardsData() {
		arrLastCardDetails.clear();
	}

	/**
	 * check user exist or not in list, return 0 if does not exist, or 1 : if in
	 * playing list / or folded 2 : if in waiting list 3 : if in viewing list 4
	 * : if in other mode
	 */

	public int checkUserExist(String username) {
		int retValue = 0;
		for (PlayerDetail player : gameUserList) {
			if (player.userName.equals(username)) {
				if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
						.getPlayerStatusValue()) {
					retValue = 1;
				} else if (player.userPlayStatus.getPlayerStatusValue() == PlayerStatus.Waiting
						.getPlayerStatusValue()) {
					retValue = 2;
				} else if (player.userPlayStatus.getPlayerStatusValue() == PlayerStatus.Viewing
						.getPlayerStatusValue()) {
					retValue = 3;
				} else {
					retValue = 4;
				}
			}
		}
		return retValue;
	}

	/** get player details from username */
	public PlayerDetail getPlayerDetails(String username) {

		PlayerDetail foundPlayer = null;
		for (PlayerDetail player : gameUserList) {
			if (player.userName.equals(username)) {
				foundPlayer = player;
			}
		}
		return foundPlayer;
	}

	/** return player details at specified index */
	public PlayerDetail getPlayersAtIdx(int idx) {
		PlayerDetail players = null;
		try {
			players = gameUserList.get(idx);
		} catch (Exception e) {
			System.out.println(" getPlayersAtIdx out... bount "
					+ e.getLocalizedMessage());
			e.printStackTrace();
		}
		return players;
	}

	/** get player details from IUser user */
	public PlayerDetail getPlayerDetails(IUser iuser) {
		return this.getPlayerDetails(iuser.getName());
	}

	/** get Player list of Waiting status */
	public ArrayList<PlayerDetail> getPlayerWaitingList() {
		ArrayList<PlayerDetail> userList = new ArrayList<PlayerDetail>();
		for (PlayerDetail player : gameUserList) {
			if (player.userPlayStatus.getPlayerStatusValue() == PlayerStatus.Waiting
					.getPlayerStatusValue()) {
				userList.add(player);
			}
		}
		return userList;
	}

	/** get Player list of Viewing status */
	public ArrayList<PlayerDetail> getPlayerViewingList() {
		ArrayList<PlayerDetail> userList = new ArrayList<PlayerDetail>();
		for (PlayerDetail player : gameUserList) {
			if (player.userPlayStatus.getPlayerStatusValue() == PlayerStatus.Viewing
					.getPlayerStatusValue()) {
				userList.add(player);
			}
		}
		return userList;
	}

	/** get Player list of Playing status */
	public ArrayList<PlayerDetail> getPlayerPlayingList() {
		ArrayList<PlayerDetail> userList = new ArrayList<PlayerDetail>();
		for (PlayerDetail player : gameUserList) {
			if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Playing
					.getPlayerStatusValue()) {
				userList.add(player);
			}
		}
		return userList;
	}

	/** get Player list of Playing status */
	public ArrayList<PlayerDetail> getPlayerPlayingListForGameStatus() {
		ArrayList<PlayerDetail> userList = new ArrayList<PlayerDetail>();
		for (PlayerDetail player : gameUserList) {
			if (player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Folded
					.getPlayerStatusValue()) {
				userList.add(player);
			}
		}
		return userList;
	}

	/** get total players playing counts */
	public int getTotalPlayingPlayer() {
		int n = 0;
		for (PlayerDetail player : gameUserList) {
			if ((player.userPlayStatus.getPlayerStatusValue() <= PlayerStatus.Playing
					.getPlayerStatusValue())
					&& (player.userActionStatus.getPlayerActionValue() != PlayerActionStatus.PLAYActionPass
							.getPlayerActionValue())) {
				n++;
			}
		}
		return n;
	}

	/** reset user's game actions */
	public void resetUserGameActions() {
		ArrayList<PlayerDetail> playerList = this.getPlayerPlayingList();
		for (PlayerDetail players : playerList) {
			players.userActionStatus = PlayerActionStatus.PLAYActionNone;
		}
	}

	public int getPlayerIndexWithUsername(String userName) {
		PlayerDetail player = getPlayerDetails(userName);
		int idx = -1;
		if (player != null) {
			idx = gameUserList.indexOf(player);
		}
		return idx;
	}

	/** get top most viewers index in gameuser list */
	public int getTopMostViewersIndex() {
		int idx = -1;
		for (PlayerDetail player : gameUserList) {
			if (player.userPlayStatus.getPlayerStatusValue() > PlayerStatus.Waiting
					.getPlayerStatusValue()) {
				idx = gameUserList.indexOf(player);
				break;
			}
		}
		return idx;
	}

	/** send user game room info */
	public JSONObject sendRoomInfo() {
		try {
			JSONObject jsonDetails = new JSONObject(
					this.roomDetails.getProperties());

			JSONArray jsonArray = new JSONArray();

			for (IUser user : this.roomDetails.getJoinedUsers()) {
				jsonArray.put(user.getName());
			}

			jsonDetails.put(CardsConstants.kUserListKey, jsonArray);

			// System.out.println("room info values.... "+jsonDetails.toString());

			return jsonDetails;
		} catch (Exception e) {
			return null;
		}
	}
	public boolean isUserExistInJoinList(String name)
	{
		if (roomDetails.getJoinedUsers().size()>0)
		{
			for (int i =0;i<roomDetails.getJoinedUsers().size() ;i++)
			{
				IUser player = roomDetails.getJoinedUsers().get(i);
				if (player.getName().equalsIgnoreCase(name))
				{
					return true;
				}
			}
		}
		return false;
	}
	/** send user game status when user resume. */
	public void sendResumeGameStauts(IUser userResume)
	{
		if (roomDetails.getJoinedUsers().size() != gameUserList.size())
		{
			System.out.println("*** Count not same***");
			for (int i =0;i<gameUserList.size();i++)
			{
				PlayerDetail player = gameUserList.get(i);
				System.out.println("name>> "+player.playerName);
				System.out.println("IDname>> "+player.userName);				
			}
			for (int i =0;i<roomDetails.getJoinedUsers().size() ;i++)
			{
				IUser player = roomDetails.getJoinedUsers().get(i);
				System.out.println("getName()>> "+player.getName());
			}
		}
		else
		{
			System.out.println("*** Count  same***");
			for (int i =0;i<gameUserList.size();i++)
			{
				PlayerDetail player = gameUserList.get(i);
				System.out.println("name>> "+player.playerName);
				System.out.println("IDname>> "+player.userName);	
				
				IUser iUser= roomDetails.getJoinedUsers().get(i);
				System.out.println("getName()>> "+iUser.getName());
			}
		}
		System.out.println("*** END USER INFO***");		
		// notify to start game
		JSONObject obj = new JSONObject();
		try {
			obj.put(CardsConstants.kTypeMessageKey,
					CardsConstants.kTypeGameStautsResume);
			obj.put(CardsConstants.kCurrentTableAmountKey,
					this.centerTableAmount);
			obj.put(CardsConstants.kCurrentUserTurnName,
					this.currentPlayuserName);
			obj.put(CardsConstants.kUserLastPlayed, this.playStartUsername);
			obj.put(CardsConstants.kGameStatusKey,
					this.currentGameStatus.getGameStatusValue());
			obj.put(CardsConstants.kGameRoundKey,
					Integer.toString(this.totalGamerounds));
			obj.put(CardsConstants.kUserPassCount,
					Integer.toString(this.passcounts));
			obj.put(CardsConstants.kUserIndicator, startPlayUserName);

			JSONObject playingObj = new JSONObject();

			ArrayList<PlayerDetail> arrPlayerDetails = this.gameUserList;

			for (PlayerDetail player : arrPlayerDetails)
			{
				if (!isUserExistInJoinList(player.userName))
				{
					System.out.println("*** /// *** user is in ARRAY but is not in JOIN LIST");		
					continue;
				}
				
				JSONObject objplayer = new JSONObject();

				objplayer.put(CardsConstants.kUserCallAmountKey,
						player.raiseChipAmount);
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
				objplayer.put(CardsConstants.kUserRoundAmount,
						player.roundBetAmount);
				objplayer.put(CardsConstants.kUserActionStatus,
						player.userActionStatus.getPlayerActionValue());
				objplayer.put(CardsConstants.kUserStatus,
						player.userPlayStatus.getPlayerStatusValue());
				objplayer.put(CardsConstants.kUserRaiseCounter,
						player.raiseCounter);

				JSONArray cardList = new JSONArray();
				for (Card card : player.cardList) {
					int cardValue = card.getCards().getCardValue()
							+ card.getSuits().getSuitsValue();
					cardList.put(cardValue);
				}

				objplayer.put(CardsConstants.kUserCardsKey, cardList);
				// obj.put(player.userName, objplayer);
				playingObj.put(player.userName, objplayer);
			}
			obj.put(CardsConstants.kUserDetailsPlaying, playingObj);

			JSONArray cardList = new JSONArray();
			for (Integer value : this.centerTableCard) {
				cardList.put(value.intValue());
			}

			obj.put(CardsConstants.kUserCardsKey, cardList);

			// JSONArray lastCardDetails = new
			// JSONArray(this.arrLastCardDetails);
			// obj.put(CardsConstants.kLastCardDetails, lastCardDetails);

			JSONObject roomInfo = this.sendRoomInfo();
			obj.put(CardsConstants.kRoomDetails, roomInfo);

			String strObj = obj.toString();
			userResume.SendChatNotification(CardsConstants.SERVER_NAME, strObj,
					userResume.getLocation());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** send user game status when join. */
	public void sendGameStatus() {
		// notify to start game
		JSONObject obj = new JSONObject();
		try {
			obj.put(CardsConstants.kTypeMessageKey,
					CardsConstants.kTypeGameStatus);
			obj.put(CardsConstants.kCurrentTableAmountKey,
					this.centerTableAmount);
			obj.put(CardsConstants.kCurrentUserTurnName,
					this.currentPlayuserName);
			obj.put(CardsConstants.kGameStatusKey,
					this.currentGameStatus.getGameStatusValue());
			obj.put(CardsConstants.kGameRoundKey,
					Integer.toString(this.totalGamerounds));
			obj.put(CardsConstants.kUserPassCount,
					Integer.toString(this.passcounts));
			obj.put(CardsConstants.kUserLastPlayed, this.playStartUsername);
			obj.put(CardsConstants.kUserIndicator, startPlayUserName);

			JSONObject playingObj = new JSONObject();
			ArrayList<PlayerDetail> arrPlayerDetails = this
					.getPlayerPlayingListForGameStatus();
			for (PlayerDetail player : arrPlayerDetails) {
				JSONObject objplayer = new JSONObject();
				objplayer.put(CardsConstants.kUserCallAmountKey,
						player.raiseChipAmount);

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

				/* ... Updated status values.... */
				objplayer.put(CardsConstants.kUserRoundAmount,
						player.roundBetAmount);
				objplayer.put(CardsConstants.kUserActionStatus,
						player.userActionStatus.getPlayerActionValue());
				objplayer.put(CardsConstants.kUserStatus,
						player.userPlayStatus.getPlayerStatusValue());
				objplayer.put(CardsConstants.kUserRaiseCounter,
						player.raiseCounter);

				JSONArray cardList = new JSONArray();
				for (Card card : player.cardList) {
					int cardValue = card.getCards().getCardValue()
							+ card.getSuits().getSuitsValue();
					cardList.put(cardValue);
				}
				objplayer.put(CardsConstants.kUserCardsKey, cardList);
				// obj.put(player.userName, objplayer);
				playingObj.put(player.userName, objplayer);
			}
			obj.put(CardsConstants.kUserDetailsPlaying, playingObj);

			JSONObject waitingObj = new JSONObject();

			ArrayList<PlayerDetail> arrWaitingPlayerDetails = this
					.getPlayerWaitingList();

			for (PlayerDetail player : arrWaitingPlayerDetails) {
				JSONObject objplayer = new JSONObject();
				objplayer.put(CardsConstants.kUserCallAmountKey,
						player.raiseChipAmount);

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

				objplayer.put(CardsConstants.kUserRoundAmount,
						player.roundBetAmount);
				objplayer.put(CardsConstants.kUserActionStatus,
						player.userActionStatus.getPlayerActionValue());
				objplayer.put(CardsConstants.kUserStatus,
						player.userPlayStatus.getPlayerStatusValue());
				objplayer.put(CardsConstants.kUserRaiseCounter,
						player.raiseCounter);

				waitingObj.put(player.userName, objplayer);
			}
			obj.put(CardsConstants.kUserDetailsWaiting, waitingObj);
			JSONArray cardList = new JSONArray();
			for (Integer value : this.centerTableCard) {
				cardList.put(value.intValue());
			}
			obj.put(CardsConstants.kUserCardsKey, cardList);

			// JSONArray lastCardDetails = new
			// JSONArray(this.arrLastCardDetails);
			// System.out.println(".. last cards .. "+lastCardDetails.toString());
			// obj.put(CardsConstants.kLastCardDetails, lastCardDetails);

			JSONObject roomInfo = this.sendRoomInfo();
			obj.put(CardsConstants.kRoomDetails, roomInfo);

			String strObj = obj.toString();
			 System.out.println("GameStatus : "+strObj);
			this.roomDetails.BroadcastChat(CardsConstants.SERVER_NAME, strObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** check betting occurs in betting round or not */
	public boolean checkBetsInBettingRound() {
		boolean didBetOccurs = false;
		ArrayList<PlayerDetail> arrPlayingList = this.getPlayerPlayingList();
		for (PlayerDetail players : arrPlayingList) {
			if (players.roundBetAmount > 0) {
				didBetOccurs = true;
				break;
			}
		}
		return didBetOccurs;
	}

	/** check betting occurs in betting round or not */
	public boolean checkBetsInNormalRound() {
		boolean didBetOccurs = false;
		ArrayList<PlayerDetail> arrPlayingList = this.getPlayerPlayingList();
		for (PlayerDetail players : arrPlayingList) {
			if (players.raiseChipAmount > 0) {
				didBetOccurs = true;
				break;
			}
		}
		return didBetOccurs;
	}

	public long getMaxBettingRoundAmount() {
		long amount = -1;
		ArrayList<PlayerDetail> arrPlayingList = this.getPlayerPlayingList();
		for (PlayerDetail players : arrPlayingList) {
			if (players.roundBetAmount != 0 && players.roundBetAmount > amount) {
				amount = players.roundBetAmount;
			}
		}
		return amount;
	}

	public long getMaxRaiseAmount() {
		long amount = -1;
		ArrayList<PlayerDetail> arrPlayingList = this.getPlayerPlayingList();
		for (PlayerDetail players : arrPlayingList) {
			if (players.raiseChipAmount != 0
					&& players.raiseChipAmount > amount) {
				amount = players.raiseChipAmount;
			}
		}
		return amount;
	}

	/** check All user All-In return true of all users are all in else false */
	public boolean allUserAllIn() {
		boolean isAllIn = false;
		ArrayList<PlayerDetail> arrPlayingList = this.getPlayerPlayingList();
		int count = 0;
		for (PlayerDetail players : arrPlayingList) {
			if (this.isUserAllIn(players)) {
				count++;
			}
		}

		if ((count == arrPlayingList.size())
				|| (count == arrPlayingList.size() - 1)) {
			isAllIn = true;
		}

		return isAllIn;
	}

	public boolean isUserAllIn(PlayerDetail player) {
		if (player.totalChipAmount == 0) {
			return true;
		}
		return false;
	}

	/** Process start indicator and move as per requirement */
	public void processStartIndicator(PlayerDetail player) {
		try {

			if (player != null) {
				if (startPlayUserName.equalsIgnoreCase(player.userName))
				{
					// currentGameDetails.startPlayUserName =
					// startPlayer.userName;

					int total = gameUserList.size();
					int idx = gameUserList.indexOf(player);

					PlayerDetail nextPlayer = null;

					do {
						idx++;
						if (idx >= total) {
							idx = 0;
						}
						nextPlayer = gameUserList.get(idx);

					} while (nextPlayer.userPlayStatus.getPlayerStatusValue() > PlayerStatus.Folded
							.getPlayerStatusValue()
							&& nextPlayer.userActionStatus
									.getPlayerActionValue() < PlayerActionStatus.PLAYActionPass
									.getPlayerActionValue());

					startPlayUserName = nextPlayer.userName;
				}
			} else {
				System.out
						.println("processStartIndicator player is null coming... ");
			}
		} catch (Exception e) {
			// TODO: handle exception

			System.out.println(" error in processStartIndicator "
					+ e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	/** ************** Compulter Player methods ****************** */
	public boolean checkComputerPlayerRequirement() {
		return computerhelperDetails.checkComputerPlayerRequirement();
	}

	public void createComputerPlayerInGame() {
		final ComputerDetails comPlayer = computerhelperDetails
				.getComputername();
		comPlayer.name = CMUtility.getRandomName();
		comPlayer.setComputerDifficulty(gameUserList.get(0));


		IUser computerPlayerUser = new IUser() {

			String customData = "";
			String name = comPlayer.name;

			@Override
			public void setCustomData(String arg0) {
				// TODO Auto-generated method stub

				System.out.println("setCustomData : " + arg0);
				customData = arg0;
			}

			@Override
			public boolean isPaused() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
//				System.out.println("***,getting name from instance name : " +name);
				return name;
			}

			@Override
			public IRoom getLocation() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getIPAddress() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getCustomData() {
				// TODO Auto-generated method stub
				return customData;
			}

			@Override
			public void SendUpdatePeersNotification(byte[] arg0, boolean arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void SendChatNotification(String arg0, String arg1,
					IRoom arg2) {
				// TODO Auto-generated method stub

			}
		};

		PlayerDetail newComputerPlayer = new PlayerDetail(true,
				computerPlayerUser);


//		newComputerPlayer.setDefaultValuesforComputer();

		newComputerPlayer.setRandomValuesForComputerInRef(gameUserList.get(0));
		Object obj = roomDetails.getProperties().get(
				CardsConstants.kRoomMaxStack);
		if (obj instanceof String) {
			newComputerPlayer.totalChipAmount = Long.parseLong((String) obj);
		} else if (obj instanceof Integer) {
			newComputerPlayer.totalChipAmount = ((Integer) obj).longValue();
		}

		if(playerChipsInitally == 0){
			playerChipsInitally = Long.parseLong(gameUserList.get(0).ChipsValue);
		}
		newComputerPlayer.ChipsValue = "" + playerChipsInitally;
		newComputerPlayer.totalChipAmount = playerChipsInitally;

		System.out.println("newComputerPlayer.getPlayerDetailsJson() : " + newComputerPlayer.getPlayerDetailsJson());

		computerPlayerUser.setCustomData(newComputerPlayer.getPlayerDetailsJson());

//		if(this.currentGameStatus == GameStatus.GameEnd) {
//			System.out.println("Computer Player Type : Playing");
			newComputerPlayer.userPlayStatus = PlayerStatus.Playing;
//		}
//		else{
//			System.out.println("Computer Player Type : Waiting");
//			newComputerPlayer.userPlayStatus = PlayerStatus.Waiting;
//		}


		newComputerPlayer.computerDetails = comPlayer;


		gameUserList.add(newComputerPlayer);

		System.out.println("computerPlayerUser.getName() "
				+ computerPlayerUser.getName());
		computerPlayerInGame = comPlayer;

//		roomExtension.handleUserSubscribeRequest(newComputerPlayer.user, new HandlingResult());

		// here send message for computer player and its details to rooms;
//		 roomDetails.BroadcastChat(CardsConstants.SERVER_NAME, "");
//		 roomDetails.BroadcastChat(computerPlayerUser.getName(), "");

//		this.sendGameStatus();
//		this.sendInitialGameState();
		roomDetails.addUser(computerPlayerUser, true);

//		roomExtension.handleUserSubscribeRequest(computerPlayerUser, new HandlingResult());

		roomExtension.handleSubscribeByAI(computerPlayerUser);

		PlayerDetail player = this.getPlayerDetails(computerPlayerUser.getName());

		System.out.println("computerPlayerUser.getName() : " + computerPlayerUser.getName() + "   player.userPlayStatus :  " + player.userPlayStatus);

	}

	public void startGameWithAI(){

		this.sendGameStatus();
		this.sendInitialGameState();



		ArrayList<PlayerDetail> arrPlayerList = this
				.getPlayerPlayingList();
		if (arrPlayerList.size() >= 2) {
			this.currentGameStatus = GameStatus.GameBettingRound;

			Timer timer = new Timer();
			TimerTask delayedThreadStartTask = new TimerTask() {
				@Override
				public void run() {
					String name = gameUserList
							.get(0).userName;
					playStartUsername = name;
					// System.out.println("distributeCardsForGame WITH "+name);
					roomExtension.callDistributeCardsForGameByAI(name);

				}
			};
			timer.schedule(delayedThreadStartTask, 0 * MSecs); // 3 secs
		}

	}

	public void removeComputerPlayerFromGame() {
		PlayerDetail computerPlayer = null;
		for (PlayerDetail player : gameUserList) {
			if (player.isComputerPlayer) {
				computerPlayer = player;
				break;
			}
		}

		if (computerPlayer != null) {
			gameUserList.remove(computerPlayer);
			// here send message for computer player to remove from game and its
			// details from rooms;
			// roomDetails.BroadcastChat(CardsConstants.SERVER_NAME, "");
		}
		computerPlayerInGame = null;
	}

	public void processComputerPlayerStates(int timeSec) {

        System.out.println("processComputerPlayerStates gameUserList.size() : " + gameUserList.size());


        
//		if (computerPlayerInGame == null
//				&& computerhelperDetails.checkComputerPlayerRequirement()) {
//		if (gameUserList.size() < 5 && gameUserList.size() > 0) {
			System.out.println("processComputerPlayerStates : 1");
			// add computer player
			if (computerPlayerTimer != null) {
				computerPlayerTimer.cancel();
				computerPlayerTimer = null;
			}

//			while (getPlayerPlayingList().size() < 6){
//				System.out.println("createComputerPlayerInGame");
//				createComputerPlayerInGame();
//			}

			computerPlayerTimer = new Timer();
			TimerTask comTask = new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
                    System.out.println("run");
//					while (getPlayerPlayingList().size() < 6){
//						System.out.println("createComputerPlayerInGame");
//						createComputerPlayerInGame();
//
//					}
//					createComputerPlayerInGame();
					System.out.println("***************** getPlayerPlayingList().size() *****************" + getPlayerPlayingList().size());
					if(gameUserList.size() < minPlayerToStartGame && gameUserList.size() > 0)
					createComputerPlayerInGame();
					else{
//						startGameWithAI();
						computerPlayerTimer.cancel();
					}

//					sendInitialGameState();



				}
			};
			computerPlayerTimer.schedule(comTask, timeSec * MSecs, 5 * MSecs);


//		} else {
//			// computer player already in game or no requirement of it.
//
//		}
	}

	/** return true if computer player exist in game. */
	public boolean isComputerPlayerInGame() {
		if (computerPlayerInGame != null) {
			return true;
		}
		return false;
	}
	
	//Check
	private void sendInitialGameState() {
		JSONObject obj = new JSONObject();
		try {
			ArrayList<PlayerDetail> arrPlayerDetails = this
					.getPlayerPlayingList();

			obj.put(CardsConstants.kTypeMessageKey,
					CardsConstants.kTypeInitStatus);
			obj.put(CardsConstants.kCurrentTableAmountKey,
					this.centerTableAmount);
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

			JSONObject roomInfo = this.sendRoomInfo();
			obj.put(CardsConstants.kRoomDetails, roomInfo);

			String msg = obj.toString();
			 System.out.println("sended initi msg "+msg);
			this.roomDetails.BroadcastChat(
					CardsConstants.SERVER_NAME, msg);
		} catch (JSONException e) {
			System.out.println("error " + e.getMessage());
			e.printStackTrace();
		}
	}


	public float getRoomInterval()
	{
		Object obj = roomDetails.getProperties().get(
				CardsConstants.kRoomInterval);
		String RoomInterval = (String) obj;
		if (RoomInterval.compareToIgnoreCase("-1") == 0)
		{
			return CMAIConstants.TURNINTERVAL;
		}

//    CCString* str = CCString::create(RoomInterval);
//    CCLog("***return room interval value %s",str->getCString());
//    return (str->floatValue()/100.0);

		if (RoomInterval.compareToIgnoreCase(CMAIConstants.kTypeExpert) == 0)
		{
			return CMAIConstants.ExpertDuration/100.0f;
		}
		else if (RoomInterval.compareToIgnoreCase(CMAIConstants.kTypeFast) == 0)
		{
			return CMAIConstants.FastDuration/100.0f;
		}
		else
		{
			return CMAIConstants.NormalDuration/100.0f;
		}
	}


}