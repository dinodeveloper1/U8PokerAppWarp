/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rummydemo;


public class CardsConstants 
{
    public static final float APIVersion = 0.73f;  
    public static final String SERVER_NAME = "ADMIN";
    
    public static final byte MAX_CARD = 52;
    
    public static final int MAX_NO_OF_CARDS = 8;// for each user
    
    // Message Constants
    
    public static final byte MaxPlayers = 6;
    public static final byte GameStartDely = 5;
    public static final byte ExtendTimer = 3;    
    public static final byte UserTimeOut = 28;   
    public static int MSecs = 1000;
    
    public static final byte PLAYER_HAND = 1;
    
    public static final byte RESULT_GAME_OVER = 3;
    
    public static final byte RESULT_USER_LEFT = 4;
    
    
    // error code
    public static final int SUBMIT_CARD = 111;
    public static final int INVALID_MOVE = 121;
    
    // GAME_STATUS
    
    public static final int STOPPED = 71;
    public static final int RUNNING = 72;
    public static final int PAUSED = 73;
    public static final int RESUMED = 74;
    public static final int FINISHED = 75;
    public static final int START_GAME = 80;

//     Room Properites Keys
    
//   Must Change Keys for RoomCall Amount with Final build in server and client Side.    
    /** Local and Temp Gpaas CallAmount*/
//    public static final String kRoomDefaultAmount = "RoomDefaultAmount";
    /** Final Gpaas CallAmount*/
    public static final long checkTimer = (3*60*1000); 
    public static final String kRoomDefaultAmount = "RoomCallAmount";    
    public static final String kRoomType = "RoomType";
    public static final String kRoomMinStack = "MinStack";
    public static final String kRoomMaxStack = "MaxStack";
    public static final String kRoomOwner = "createRoomOwner";
    public static final String kRoomInterval = "RoomInterval";    
   
    public static final int kMaxRoomUsers = 50;
    
    // Messages Key - Keys will use for message communication with other players
    public static final String kTypeMessageKey = "MsgPktK";
    public static final String kUserListKey = "UsrObjListK";     
    public static final String kUserNameKey = "usrNmK"; //UserNameKey
    public static final String kCurrentUserIndexKey = "crtUsrIdxK"; //currentuserIndexKey
    public static final String kPlayerTurnKey = "PlTrn"; //Playerturn
    public static final String kSuccessKey = "success"; //success
    public static final String kUserTurnKey = "nxtUsrTrnK"; //nextUserTurnKey
    public static final String kUserCardsKey = "UsrCrdK"; //UserCardsKey
    public static final String kUserPassCount = "UsrPsCntK"; //UserPassCount
    public static final String kGameRoundKey = "GmRndK"; //GameRoundKey
    public static final String kGameStatusKey = "GmStsK";
    
    public static final String kUserCallAmountKey = "UsrClAmntK"; //UserCallAmoutKey
    public static final String KDefaultRaiseAmountKey = "DftRAmtK"; //DefaultRaiseAmountKey
    public static final String kUserDefaultCallAmount = "UsrDftCAmtK"; //UserDefaultCallAmount
    public static final String kUserServerStartIndex = "UsrSIdxSrv"; //UserStartIndexServer    
    public static final String kUserInitialStackKey = "UsrStckAmt"; //UserStacKAmount
    public static final String kCurrentTableAmountKey = "CrtTblAmt"; //CurrentTableAmountKey
    public static final String kPlayerDetailsKey = "PlyrDtls"; //PlayersDetails    
    public static final String kPlayerDetailsCount = "PlyrTCnt"; //PlayersTotalCount
    public static final String kUserDetailsViewing = "viewUDtls"; //viewUDetails    
    public static final String kUserDetailsWaiting = "waitUDtls"; //waitUDetails
    public static final String kUserDetailsPlaying = "playUDtls"; //playUDetails    
    public static final String kCurrentUserTurnName = "CrtPlyUNme"; //currentPlayUserName
    public static final String kNewIndexPosKey = "idxPosUsr"; // Index position which is free to join
    public static final String kRoomDetails = "krmDtls"; // Index position which is free to join
    public static final String kUserLastPlayed = "usrLstPlyd"; // Index position which is free to join
    public static final String kUserIndicator = "usrindiName"; // Index position which is start of game
    public static final String kLastCardDetails = "UserCenterCard";
    public static final String kLastUserName  = "LastPlayerUserName";
    
    // User Details // DO NOT CHANGE FOLLOWING KEYS EVER
    public static final String kUserChipsValue = "userTotalChip";
    public static final String kUserGamesChipsValue = "userGamesChips";    
    public static final String kUserTotalHands = "userTotalPlayed";
    public static final String kUserTotalWonHands = "userTotalWon";
    public static final String kUserName = "userName";
    public static final String kUserAccountID = "userId";
    public static final String kUserGiftID = "userDefaultGiftId";
    public static final String kUserImageUrl = "userImageUrl";
    public static final String kUserRoundAmount = "usrRndAmnt";
    public static final String kUserActionStatus = "usrActSts";
    public static final String kUserStatus = "usrSts";
    public static final String kUserRaiseCounter = "usrRsCntr";

    public static final String kGameBetOn = "isBetOn";

    public static final String kMinPlayerKey = "minplyr";


    // Messages Type - type of message recieved.
    public static final int kTypeInvalideMessage = -99;
    public static final int kTypeGameStartWithCard = 10;
    public static final int kTypeCardDistribution  = 11            ;//"ChatPacket" //
    public static final int kTypeNewRoundBegin = 12;  // "kTableCardPacket" 
    public static final int kTypeGetTableBet= 13;     //"kTypeGetTableBet"
    public static final int kTypeNewUserTurn =14;     //"kTypeNewUserTurn" // in case current user left,
    																	   // so pass turn to next player
    public static final int kTypeInitStatus =15; // "kCardDistribution" // send first status before game start;
    
    public static final int kTypeUserTimeOut =16; // "kCardDistribution"
    public static final int kTypeUserPass =17; // "kCardDistribution"
    public static final int kTypeWinner =18; // "kCardDistribution"    
    
    public static final int kTypeFold =19; // "kTypeFold"
    public static final int kTypeCardDistributionCall =20; // "kCardDistribution"
    public static final int kTypeCardDistributionRaise =21; // "kTypeCardDistributionRaise"
    public static final int kTypeUserCheck =22; // "kTypeUserCheck"    
    
    public static final int kTypeUserStatus =23; // "kTypeUserStatus"
    
    public static final int kTypeGameStatus = 24; // "GameStatus"
    public static final int kTypeUserChat = 25; // "Game Chat"
    public static final int kTypeSendGift = 26; // "Game Gifts"
    
    public static final int kTypeBetRoundBet = 27;   //16 Mar 2015
    public static final int kTypeBetRoundCheck= 28; //16 Mar 2015
    public static final int kTypeBetRoundRaise = 29; //16 Mar 2015    
    public static final int kTypeJoinRequest = 30; //25 Mar 2015
    
    public static final int kTypeServerUserTimeOut = 31; //20 june 2015
    
    public static final int kTypeRoomInfo = 32; //20 june 2015    
    public static final int kTypeGameStautsResume = 33; //20 june 2015
    public static final int KTypeExtendTimer = 34;
    public static final int kTypeResumeRequest = 35;
    public static final int kTypePingRequest = 36;


    //For AI
    public static final float kDealerAnimationTime = 8 * 0.3f;


    //Enum
    public enum AIFireEvent
    {
        Bet,
        Call,
        Raise,
        Won,
        PlayCard,
        Pass
    }

    public enum CardRanks{
        ACE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13);

        private int cardValue;

        CardRanks (int value)
        {
            this.cardValue = value;
        }
        public int getCardValue() {
            return cardValue;
        }

    }
    public enum Suit
    {
        CLUBS(1),
        DIAMONDS(2),
        HEARTS(3),
        SPADES(4);

        private final int someString;
        private Suit(final int someString)
        {
            this.someString = someString;
        }

        public int getSuitsValue()
        {
            return someString;
        }
    }

    public enum CMHandType
    {
        // lowest to Highest  hand types

        HandOneCard(500),

        HandOnePair(501) , // 501

        //502
        HandStraight3(502), HandFlush3(503), HandKindOf3(504), HandStraightFlush3(505), HandRoyalFlush3(506), // 506

        //507
        HandTwoPair(507), HandStraight4(508), HandFlush4(509), HandKindOf4(510), HandStraightFlush4(511), HandRoyalFlush4(512),//512

        // 513
        HandStraight5(513), HandFlush5(514), HandFullHouse(515), HandKindOf4One(516), HandStraightFlush5(517), HandRoyalFlush5(518), //518

        HandTypeUnkown(1);

        private int handValue;

        CMHandType (int value)
        {
            this.handValue = value;
        }
        public int getHandValue() {
            return handValue;
        }

    }


    public enum CMHandPlayType
    {
        HandPlaySingle(1),HandPlayPair(2),HandPlayThree(3),HandPlayFour(4),HandPlayFive(5);

        private int handPlayValue;

        CMHandPlayType (int value)
        {
            this.handPlayValue = value;
        }
        public int getHandPlayValue() {
            return handPlayValue;
        }
    };



}
