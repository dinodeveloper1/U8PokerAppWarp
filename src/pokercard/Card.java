/*
The MIT License (MIT)

Copyright (c) 2013 Jacob Kanipe-Illig

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package pokercard;

import static pokercard.Suit.*;
import static pokercard.Rank.*;
import pokercard.Suit;
import pokercard.Rank;

/**
 * Enumeration of all cards used in Texas Hold'em.
 */
//Serialize a member of this enum to JSON using the enum's toString method

/**
 * An object of type Card represents a playing card from a
 * standard Poker deck, including Jokers.  The card has a suit, which
 * can be spades, hearts, diamonds, clubs, or joker.  A spade, heart,
 * diamond, or club has one of the 13 values: ace, 2, 3, 4, 5, 6, 7,
 * 8, 9, 10, jack, queen, or king.  Note that "ace" is considered to be
 * the smallest value.  A joker can also have an associated value; 
 * this value can be anything and can be used to keep track of several
 * different jokers.
 */

public class Card {
	
	public enum CardValue
	{
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
	 
	  CardValue (int value)
	  {
	    this.cardValue = value;
	  }
	 
	  public int getCardValue() {
	    return cardValue;
	  }
	}
	private Suit suit;
	private CardValue cardValue;
	public Card (CardValue cardValue, Suit suit)
	{
		this.cardValue = cardValue;
		this.suit = suit;
	}
	public Suit getSuits()
	{
		return suit;
	}
	public void setSuit(Suit suit)
	{
		this.suit = suit;
	}
	public CardValue getCards()
	{
		return cardValue;
	}
	public void setCardValue(CardValue cardValue)
	{
		this.cardValue = cardValue;
	}
} // end class Card
