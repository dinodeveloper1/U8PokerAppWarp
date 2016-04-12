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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import pokercard.Card.CardValue;

/**
 * Standard deck of cards for poker. 52 Cards. 13 Clubs, Diamonds, Spades, and Hearts.
 * 
 * @author jacobhyphenated
 */

public class Deck 
{ 
	private ArrayList<Card> deck;
	static final int cardsInDeck = 52;

	public Deck ()
	{
		this.deck = new ArrayList<Card>();
		for (int i=0; i<13; i++)
		{
			CardValue value = CardValue.values()[i];
			for (int j=0; j<4; j++)
			{
				Card card = new Card(value, Suit.values()[j]);
				this.deck.add(card);
			}
		}
		Collections.shuffle(deck);
		this.shuffle();
		Collections.shuffle(deck);
		this.shuffle();
		Collections.shuffle(deck);
		this.shuffle();
	}
	/**
	 * Shuffles the deck (i.e. randomly reorders the cards in the deck). 
	 */
	public void shuffle() {
		int newI;
		Card temp;
		Random randIndex = new Random();

		for (int i = 0; i < cardsInDeck; i++) 
		{
			// pick a random index between 0 and cardsInDeck - 1
			newI = randIndex.nextInt(cardsInDeck);
			// swap cards[i] and cards[newI]
			temp = deck.get(i);
			deck.set(i, deck.get(newI));
			deck.set(newI, temp);
		}
		
//		for (int i = 0; i < cardsInDeck; i++)
//		{
//			Card temp12 = deck.get(i);
//			System.out.println("Card Value "+temp12.getCards() +" Card Suits "+temp12.getSuits());
//		}
	}

	/**
	 * Returns the top card from the deck.  Removes the card from the deck.
	 * @return {@link Card}
	 */
	
	public boolean hasMoreCards()
	{
		if (deck.size()>0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public Card dealCard()
	{
		return deck.remove(0);
	}
}
