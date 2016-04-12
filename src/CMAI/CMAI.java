package CMAI;

import com.shephertz.app42.server.idomain.IUser;

import java.util.ArrayList;

import pokercard.Card;
import pokercard.GameDetails;
import pokercard.PlayerActionStatus;
import pokercard.PlayerDetail;
import pokercard.PlayerStatus;
import rummydemo.CMAIConstants;
import rummydemo.CMUtility;

/**
 * Created by kapilbindal on 29/03/16.
 */
public class CMAI {

    public PlayerDetail playerDetail;
    public GameDetails gameDetails;

    public CMAI(GameDetails gameDetails)
    {
        this.gameDetails = gameDetails;
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

        playerDetail.totalPlay =  "" + totalPlayed;

        int winPercentage =  CMUtility.randomNumberBetween(CMAIConstants.kWinPercentageIntermendiateMin, CMAIConstants.kWinPercentageIntermendiateMax);

        int won =  totalPlayed * winPercentage/100;

        playerDetail.totalWon = "" + won;
        playerDetail.defaultGiftID = "0";
        playerDetail.profileImageUrl = "";
        playerDetail.ChipsValue = playerDetail.ChipsValue;
        playerDetail.raiseChipAmount = 0;
        playerDetail.totalChipAmount = 0;
        playerDetail.roundBetAmount = 0;
//		this.userName = CMUtility.getRandomName();
        playerDetail.playerName = playerDetail.userName;

    }

}
