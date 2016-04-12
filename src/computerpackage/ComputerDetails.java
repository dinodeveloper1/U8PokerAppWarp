package computerpackage;

import java.util.Random;

import pokercard.PlayerDetail;
import rummydemo.CMAIConstants;
import rummydemo.CMUtility;
import rummydemo.CardsConstants;

public class ComputerDetails {

	public String name;
	public String userId;
	public ComputerTypeLevel computerLevel;
	
	public ComputerDetails(String name, ComputerTypeLevel type,String newUserId)
	{
		this.name = name;
		this.computerLevel = type;		
		this.userId = newUserId;
	}
	public ComputerDetails(String name, ComputerTypeLevel type)
	{
		this.name = name;
		this.computerLevel = type;		
		this.userId = name;
	}
	public ComputerDetails()
	{
		this.name = "";
		this.computerLevel = ComputerTypeLevel.ComputerTypeLevelEasy;		
	}

	public void setComputerDifficulty(PlayerDetail refPlayer){

		long refTotalPlay = Long.parseLong(refPlayer.totalPlay);
		long refTotalWin = Long.parseLong(refPlayer.totalWon);

		if(refTotalPlay < 10){
			refTotalPlay = 50;
		}
		if(refTotalWin < 10){
			refTotalWin = 30;
		}

		float percentageWin = CMUtility.getPercentage((int)refTotalWin, (int)refTotalPlay);
		if(percentageWin > 60.0){
			this.computerLevel = ComputerTypeLevel.ComputerTypeLevelHard;
		}
		else if (percentageWin > 30.0){
			this.computerLevel = ComputerTypeLevel.ComputerTypeLevelMedium;
		}
		else{
			this.computerLevel = ComputerTypeLevel.ComputerTypeLevelEasy;
		}

	}

	public int canPlayCardsUptoAccToExp(){
		final ComputerTypeLevel level = computerLevel;
		if(level == ComputerTypeLevel.ComputerTypeLevelHard){
			return 5;
		}
		else if(level == ComputerTypeLevel.ComputerTypeLevelMedium){
			return 4;
		}
		else{

			return 3;
		}


	}

	public int getStartNoAccToProbAndExp(){

		Random randomGenerator = new Random();
		int probabilityFactor = randomGenerator.nextInt(100);
		final ComputerTypeLevel level = computerLevel;
		int startNo = 0;

		if(level == ComputerTypeLevel.ComputerTypeLevelHard){

			startNo = 3;

			if(probabilityFactor <= CMAIConstants.kExpertStartWithFiveCards){
				startNo = 5;
			}
			else if(probabilityFactor <= CMAIConstants.kExpertStartWithFiveCards + CMAIConstants.kExpertStartWithFourCards){
				startNo = 4;
			}

		}
		else if(level == ComputerTypeLevel.ComputerTypeLevelMedium){

			startNo = 2;

			if(probabilityFactor <= CMAIConstants.kIntermediateStartWithFiveCards){
				startNo = 5;
			}
			else if(probabilityFactor <= CMAIConstants.kIntermediateStartWithFiveCards + CMAIConstants.kIntermediateStartWithFourCards){
				startNo = 4;
			}
			else if(probabilityFactor <= CMAIConstants.kIntermediateStartWithFiveCards + CMAIConstants.kIntermediateStartWithFourCards + CMAIConstants.kIntermediateStartWithThreeCards){
				startNo = 3;
			}


		}
		else{

			startNo = 1;

			if(probabilityFactor <= CMAIConstants.kNoviceStartWithFiveCards){
				startNo = 5;
			}
			else if(probabilityFactor <= CMAIConstants.kNoviceStartWithFiveCards + CMAIConstants.kNoviceStartWithFourCards){
				startNo = 4;
			}
			else if(probabilityFactor <= CMAIConstants.kNoviceStartWithFiveCards + CMAIConstants.kNoviceStartWithFourCards + CMAIConstants.kNoviceStartWithThreeCards){
				startNo = 3;
			}
			else if(probabilityFactor <= CMAIConstants.kNoviceStartWithFiveCards + CMAIConstants.kNoviceStartWithFourCards + CMAIConstants.kNoviceStartWithThreeCards + CMAIConstants.kNoviceStartWithTwoCards){
				startNo = 2;
			}

		}



		return startNo;

	}

}
