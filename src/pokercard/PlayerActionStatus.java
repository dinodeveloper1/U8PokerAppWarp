package pokercard;

public enum PlayerActionStatus 
{
	PLAYActionCardPlay(1100),
	PLAYActionCardPlayRaise(1101),
	PLAYActionCardPlayCall(1102),
	PLAYActionPass(1103),
	PLAYActionFold(1104),
	PLAYActionCheck(1105),
	PLAYActionBet(1106),
	PLAYActionRaise(1107),
	PLAYActionCall(1108),
	PLAYActionNone(1109);
	
	private int cardValue;
	PlayerActionStatus (int value){
		this.cardValue = value;
	}
	public int getPlayerActionValue() {
		return cardValue;
	}
}