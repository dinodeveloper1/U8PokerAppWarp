/**
 * 
 */
package computerpackage;

import java.util.ArrayList;
import java.util.Random;

import pokercard.GameDetails;

/**
 * @author 
 *
 */

public class ComputerHelper 
{
	ArrayList<ComputerDetails> computerList;
	public GameDetails currentGameDetails;
	
	public ComputerHelper(GameDetails gameDetails)
	{
		this.generateComputerPlayers();
		currentGameDetails = gameDetails;
	}
	
	public ComputerHelper()
	{
		this.generateComputerPlayers();
	}
	private void generateComputerPlayers()
	{
		computerList = new  ArrayList<ComputerDetails>();
		
		computerList.add(new ComputerDetails("Depp", ComputerTypeLevel.ComputerTypeLevelEasy));
		computerList.add(new ComputerDetails("Ryan", ComputerTypeLevel.ComputerTypeLevelEasy));
		computerList.add(new ComputerDetails("Bradly", ComputerTypeLevel.ComputerTypeLevelMedium));
		computerList.add(new ComputerDetails("Scarlet", ComputerTypeLevel.ComputerTypeLevelHard));
	}
	
	public ComputerDetails getComputername()
	{
		Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(computerList.size());
        ComputerDetails playerName = computerList.get(index);
		return playerName;
	}
	
	public boolean checkComputerPlayerRequirement()
	{
		int totalPlaying= currentGameDetails.getTotalPlayingPlayers();
		int totalWaiting = currentGameDetails.getPlayerWaitingList().size();
		int totalPlayers = totalPlaying+totalWaiting;
		if (totalPlayers <=2 )
		{
			return true;
		}
		return false;
	}
}
