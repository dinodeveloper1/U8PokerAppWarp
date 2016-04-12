package pokercard;

/**
 * Created by kapilbindal on 29/03/16.
 */
public class CMCard {

    int rank;
    int suit;

    public CMCard(int value){

        this.suit = value / 100;
        this.rank = value % 100;
    }

    public CMCard(Card card){

        int cardValue = card.getCards().getCardValue()
                + card.getSuits().getSuitsValue();
        this.suit = cardValue / 100;
        this.rank = cardValue % 100;
    }

    public int getRank(){

        return rank;
    }

    public int getSuit(){

        return suit;
    }

}
