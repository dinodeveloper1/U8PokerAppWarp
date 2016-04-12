package pokercard;

public enum GameStatus 
{
  GameBettingRound(1200),
  GameRunning(1201),
  GameEnd(1202);

  private int cardValue;
 
  GameStatus (int value)
  {
    this.cardValue = value;
  }
 
  public int getGameStatusValue() {
    return cardValue;
  }
}