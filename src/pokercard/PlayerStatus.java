/**
 * 
 */
package pokercard;

/**

 *
 */
public enum PlayerStatus 
{
	isUserTurn(1000),
	Playing(1001),
	Folded(1002),
	Waiting(1003),
	Viewing(1004),
	Lobby(1005),
	Offline(1006);
	
	private int cardValue;
	PlayerStatus (int value){
		this.cardValue = value;
	}
	public int getPlayerStatusValue() {
		return cardValue;
	}
}
