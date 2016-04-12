package pokercard;

import org.omg.CORBA.NameValuePair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;


import javax.swing.table.AbstractTableModel;

import jdk.nashorn.internal.objects.NativeUint16Array;
import rummydemo.CardsConstants;

/**
 * Created by kapilbindal on 29/03/16.
 */
public class CMPokerUtils {

    //************************************************ SORT *********************************************************************

    //sortByRank
    private  static ArrayList<CMCard> sortByRankWithA(ArrayList<CMCard> arrCards)
    {
        int i, j, min_j;
        int size= arrCards.size();
    /* ---------------------------------------------------
     The selection sort algorithm
     --------------------------------------------------- */
        for ( i = 0 ; i < size ; i ++ )
        {
        /* ---------------------------------------------------
         Find array element with min. value among
         h[i], h[i+1], ..., h[n-1]
         --------------------------------------------------- */
            min_j = i;   // Assume elem i (h[i]) is the minimum

            for ( j = i+1 ; j < size ; j++ )
            {
                CMCard jCard = (CMCard)arrCards.get(j);
                CMCard minCard = (CMCard)arrCards.get(min_j);

                if ( jCard.getRank() > minCard.getRank())
                {
                    min_j = j;    // We found a smaller minimum, update min_j
                }
            }
        /* ---------------------------------------------------
         Swap a[i] and a[min_j]
         --------------------------------------------------- */
            //        Card *help = (Card*)arrCards->objectAtIndex(i);
            //        Card* minCard = (Card*)arrCards->objectAtIndex(min_j);
            //
            //        arrCards->replaceObjectAtIndex(i, minCard);
            //        arrCards->replaceObjectAtIndex(min_j, help);

            Collections.swap(arrCards, i, min_j);

        }

        ArrayList<CMCard> arrAces = new ArrayList<CMCard>();


        for (int p = arrCards.size()-1; p>=0; p--)
        {
            CMCard card = (CMCard) arrCards.get(p);
            if (card.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
            {
                arrAces.add(card);
                //            arrCards->removeObjectAtIndex(i);
                //            arrCards->insertObject(card, 0);
            }
            else
                break;
        }

        if (arrAces.size()>0)
        {
            // Sort Multiple Ace with high to low.
            for(int q =0;q<arrAces.size();q++)
            {
                min_j = q;
                for ( j = q+1 ; j < arrAces.size() ; j++ )
                {
                    CMCard jCard = (CMCard)arrAces.get(j);
                    CMCard minCard = (CMCard)arrAces.get(min_j);
                    if ( jCard.getSuit() > minCard.getSuit())
                    {
                        min_j = j;    // We found a smaller minimum, update min_j
                    }
                }
                Collections.swap(arrCards, q, min_j);

            }
            for(int r =arrAces.size()-1;r>=0;r--)
            {
                CMCard cards = (CMCard)arrAces.get(r);
                arrCards.remove(cards);
                arrCards.add(0, cards);
            }
        }

        return arrCards;
    }


    private  static ArrayList<CMCard> sortByRank(ArrayList<CMCard> arrCards)
    {
        int i, j, min_j;
        int size= arrCards.size();
    /* ---------------------------------------------------
     The selection sort algorithm
     --------------------------------------------------- */
        for ( i = 0 ; i < size ; i ++ )
        {
        /* ---------------------------------------------------
         Find array element with min. value among
         h[i], h[i+1], ..., h[n-1]
         --------------------------------------------------- */
            min_j = i;   // Assume elem i (h[i]) is the minimum

            for ( j = i+1 ; j < size ; j++ )
            {
                CMCard jCard = (CMCard)arrCards.get(j);
                CMCard minCard = (CMCard)arrCards.get(min_j);

                if ( jCard.getRank() == minCard.getRank())
                {
                    if (jCard.getSuit() > minCard.getSuit())
                    {
                        min_j = j;
                    }
                }
                else if ( jCard.getRank() > minCard.getRank())
                {
                    min_j = j;    // We found a smaller minimum, update min_j
                }
            }
        /* ---------------------------------------------------
         Swap a[i] and a[min_j]
         --------------------------------------------------- */
            //        Card *help = (Card*)arrCards->objectAtIndex(i);
            //        Card* minCard = (Card*)arrCards->objectAtIndex(min_j);
            //
            //        arrCards->replaceObjectAtIndex(i, minCard);
            //        arrCards->replaceObjectAtIndex(min_j, help);

            Collections.swap(arrCards, i, min_j);

        }

        if (arrCards.get(arrCards.size() - 1).getRank() == CardsConstants.CardRanks.ACE.getCardValue())
        {
            if (arrCards.size() >= 2)
            {
                // 4 or less card
                // here we will check last and 2nd last place for ACE , as there may chance of
                // Two Pair ACE

                switch (arrCards.size())
                {
                    case 4:
                    {
                        if ((arrCards.get(arrCards.size() - 2).getRank() == CardsConstants.CardRanks.ACE.getCardValue()))
                        {
                            // if second last is ace
                            // LAST ACE CHANGE
                            CMCard card = (CMCard)arrCards.get(arrCards.size() - 1);
                            CMCard card2 = (CMCard)arrCards.get(arrCards.size() - 2);

                            arrCards.remove(arrCards.size() - 1);

                            if (card.getSuit() > card2.getSuit())
                            {
                                arrCards.add(0, card2);
                                arrCards.remove(arrCards.size() - 1);
                                arrCards.add(0, card);
                            }
                            else
                            {
                                arrCards.add(0, card);
                                arrCards.remove(arrCards.size() - 1);
                                arrCards.add(0, card2);
                            }
                        }
                        else  if (!(arrCards.get(arrCards.size() - 2).getRank() < CardsConstants.CardRanks.TEN.getCardValue()))
                        {
                            CMCard card = arrCards.get(arrCards.size() - 1);
                            arrCards.remove(arrCards.size() - 1);
                            arrCards.add(0, card);
                        }
                    }
                    break;

                    case 3:
                    case 2:
                    {
                        if (!(arrCards.get(arrCards.size() - 2).getRank() < CardsConstants.CardRanks.TEN.getCardValue()))
                        {
                            CMCard card = arrCards.get(arrCards.size() - 1);
                            arrCards.remove(arrCards.size() - 1);
                            arrCards.add(0, card);
                        }
                    }
                    break;

                    default:
                        break;
                }
            }
        }

        return arrCards;
    }



    static ArrayList<CMCard> sortRankPairs(ArrayList<CMCard> arrCards)
    {
        HashMap<Integer, ArrayList<CMCard>> allValues = new HashMap<Integer, ArrayList<CMCard>>();
//        CCDictionary* allValues = CCDictionary::create();

        for (int i=0; i<arrCards.size(); i++)
        {
            CMCard values = (CMCard) arrCards.get(i);
            if (allValues.get(values.getRank()) != null)
            {
                ArrayList<CMCard> arrRanks = allValues.get(values.getRank());
                arrRanks.add(values);

            }
            else
            {
                ArrayList<CMCard> arrRanks = new ArrayList<CMCard>();
                arrRanks.add(values);
                allValues.put(values.getRank(),arrRanks);
            }
        }

        CMCard singleCard = null;
        Map<Integer, ArrayList<CMCard>> treeMap = new TreeMap<Integer, ArrayList<CMCard>>(allValues);
        Set<Integer> allKeys1 = treeMap.keySet();

        ArrayList<Integer> allKeys = new ArrayList<Integer>();

        Object[] array = allKeys1.toArray();

//        System.out.println("getPossibleTwoCardsHints : 4.01 array.length: " + array.length + "");

        for(int i = 0; i < array.length; i++){

            allKeys.add((Integer) array[i]);

        }
        for (int i = 0; i<allKeys.size(); i++)
        {
            int rankValue = allKeys.get(i);
            ArrayList<CMCard> arrRanks = allValues.get(rankValue);

            if (arrRanks.size()==1)
            {
                singleCard = (CMCard) arrRanks.get(0);
                break;
            }
        }

        int index = arrCards.indexOf(singleCard);
        if (index != arrCards.size()-1)
        {
            arrCards.remove(singleCard);
            arrCards.add(singleCard);
        }


        return arrCards;
    }

    static ArrayList<CMCard> sortBySuitsAndRank(ArrayList<CMCard> arrCards)
    {
        HashMap<Integer, ArrayList<CMCard>> data = new HashMap<Integer, ArrayList<CMCard>>();
//        CCDictionary* data = CCDictionary::create();

        for (int i=0; i<arrCards.size(); i++)
        {
            CMCard card = (CMCard) arrCards.get(i);
            if (data.get(card.getSuit()) != null)
            {
                ArrayList<CMCard> value = (ArrayList<CMCard>) data.get(card.getSuit());
                value.add(card);
            }
            else
            {
                ArrayList<CMCard> value  = new  ArrayList<CMCard>();
                value.add(card);
                data.put(card.getSuit(),value);
            }
        }

        Map<Integer, ArrayList<CMCard>> treeMap = new TreeMap<Integer, ArrayList<CMCard>>(data);
        Set<Integer> allKeys1 = treeMap.keySet();

        ArrayList<Integer> allKeys = new ArrayList<Integer>();

        Object[] array = allKeys1.toArray();

        System.out.println("getPossibleTwoCardsHints : 4.01 array.length: " + array.length + "");

        for(int i = 0; i < array.length; i++){

            allKeys.add((Integer)array[i]);

        }
        if (data.size() > 0)
        {
            for (int i=0; i<data.keySet().size(); i++)
            {
                Integer key = (Integer) allKeys.get(i);
                ArrayList<CMCard> values = (ArrayList<CMCard>) data.get(key);

                values = sortByRankWithA(values);
            }

            //    CCLOG("****beforeCards******");
            //    for (int i =0; i<arrCards->count(); i++)
            //    {
            //        Card* card = (Card*)arrCards->objectAtIndex(i);
            //        CCLOG("Card%d: %s",i,card->getCardName());
            //    }

            ArrayList<CMCard> newArray = new ArrayList<CMCard>();
            for (int i= CardsConstants.Suit.SPADES.getSuitsValue(); i>=CardsConstants.Suit.CLUBS.getSuitsValue(); i--)
            {
                if (data.get(i) != null)
                {
                    ArrayList<CMCard> values = (ArrayList<CMCard>) data.get(i);
                    newArray.addAll(values);
                }
            }

            arrCards.clear();
            arrCards.addAll(newArray);

        }
        return arrCards;
    }

    static  ArrayList<CMCard> sortByRankWithAHighLow(ArrayList<CMCard> arrCards)
    {
        if (arrCards.size()>2)
        {
            switch (arrCards.size())
            {
                case 3:
                {
                    CMCard card = (CMCard) arrCards.get(0);
                    if (card.getRank()== CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        CMCard card2 = (CMCard) arrCards.get(1);
                        if (card2.getRank()< CardsConstants.CardRanks.TEN.getCardValue())
                        {
                            arrCards.remove(0);
                            arrCards.add(card);
                        }
                    }
                    return arrCards;
                }

                case 4:
                {
                    CMCard card = (CMCard) arrCards.get(0);
                    if (card.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        CMCard card2 = (CMCard) arrCards.get(1);
                        if (card2.getRank()<CardsConstants.CardRanks.TEN.getCardValue() && card2.getRank()!= CardsConstants.CardRanks.ACE.getCardValue())
                        {
                            arrCards.remove(0);
                            arrCards.add(card);
                        }
                    }
                    return arrCards;
                }

                case 5:
                {
                    CMCard card = (CMCard) arrCards.get(0);
                    if (card.getRank()== CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        CMCard card2 = (CMCard) arrCards.get(1);
                        if (card2.getRank()<CardsConstants.CardRanks.TEN.getCardValue() && card2.getRank()!= CardsConstants.CardRanks.ACE.getCardValue())
                        {
                            arrCards.remove(0);
                            arrCards.add(card);
                        }
                    }
                    return arrCards;
                }


                default:
                    return null;

            }

        }
        return null;
    }

    //********************************** CHECK ******************************************************

    private static CardsConstants.CMHandType checkThreeCardPattern(ArrayList<CMCard> cardVector){
        CardsConstants.CMHandType type;
        if(CMPokerUtils.checkRoyalFlushWithNumbers(cardVector, 3))
        {
            type = CardsConstants.CMHandType.HandRoyalFlush3;
        }
        else if (CMPokerUtils.checkStraightFlush(cardVector, 3))
        {
            type = CardsConstants.CMHandType.HandStraightFlush3;
        }
        else if(CMPokerUtils.checkThreeKindWithNumber(cardVector, 3))
        {
            type = CardsConstants.CMHandType.HandKindOf3;
        }
        else if(CMPokerUtils.checkFlushWithNumbers(cardVector, 3))
        {
            type = CardsConstants.CMHandType.HandFlush3;
        }
        else if (CMPokerUtils.checkStraightWithNumbers(cardVector, 3))
        {
            type = CardsConstants.CMHandType.HandStraight3;
        }
        else
        {
            type = CardsConstants.CMHandType.HandTypeUnkown;
        }
        return type;

    }

    //    #pragma mark - Four Card Hints
    static CardsConstants.CMHandType checkFourCardPattern(ArrayList<CMCard> cardVector)
    {
        CardsConstants.CMHandType type;
        if(checkRoyalFlushWithNumbers(cardVector, 4))
        {
            type = CardsConstants.CMHandType.HandRoyalFlush4;
        }
        else if (CMPokerUtils.checkStraightFlush(cardVector, 4))
        {
            type = CardsConstants.CMHandType.HandStraightFlush4;
        }
        else if(CMPokerUtils.checkFourKind(cardVector))
        {
            type = CardsConstants.CMHandType.HandKindOf4;
        }
        else if(CMPokerUtils.checkFlushWithNumbers(cardVector, 4))
        {
            type = CardsConstants.CMHandType.HandFlush4;
        }
        else if (CMPokerUtils.checkStraightWithNumbers(cardVector, 4))
        {
            type = CardsConstants.CMHandType.HandStraight4;
        }
        else if (CMPokerUtils.checkTwoPair(cardVector))
        {
            type = CardsConstants.CMHandType.HandTwoPair;
        }
        else
        {
            type = CardsConstants.CMHandType.HandTypeUnkown;
        }
        return type;
    }

    private static CardsConstants.CMHandType checkFiveCardPattern(ArrayList<CMCard> cardVector)
    {
        CardsConstants.CMHandType type;
        if(checkRoyalFlushWithNumbers(cardVector, 5))
        {
            type = CardsConstants.CMHandType.HandRoyalFlush5;
        }
        else if (checkStraightFlush(cardVector, 5))
        {
            type = CardsConstants.CMHandType.HandStraightFlush5;
        }
        else if(checkFourKind(cardVector))
        {
            type = CardsConstants.CMHandType.HandKindOf4One;
        }
        // full house if
        else if (checkFullHouse(cardVector))
        {
            type = CardsConstants.CMHandType.HandFullHouse;
        }
        else if(checkFlushWithNumbers(cardVector,5))
        {
            type = CardsConstants.CMHandType.HandFlush5;
        }
        else if (checkStraightWithNumbers(cardVector, 5))
        {
            type = CardsConstants.CMHandType.HandStraight5;
        }
        else{
        type = CardsConstants.CMHandType.HandTypeUnkown;
    }
        return type;
    }



    static boolean checkThreeKind(ArrayList<CMCard> hand)
    {
        boolean three=false;

        int temp1[] = new int[hand.size()];
        int temp2[] = new int[hand.size()];
        int temp3[] = new int[hand.size()];

        for(int x=0;x<hand.size();x++)
        {

            temp1[x]=hand.get(x).getRank();
            temp2[x]=hand.get(x).getRank();
            temp3[x]=hand.get(x).getRank();

        }//end for

        for(int y=0;y<hand.size();y++){

            for(int z=0;z<hand.size();z++){

                for(int a=0;a<hand.size();a++)
                {

                    if(
                            (temp1[y]==temp2[z] && temp1[y]==temp3[a])
                                    &&
                                    (    (y!=z)&&(z!=a)&&(y!=a)  )
                            ){

                        three=true;

                    }//end if

                }//end for

            }//end for

        }//end for

        return three;

    }

    //    #pragma mark - Original Four Kind
    // suits for four kind , four kind +1
    static boolean checkFourKind(ArrayList<CMCard> hand)
    {
        boolean four=false;
        int numberTimes = hand.size();
        int temp1[] = new int[numberTimes];
        int temp2[] = new int[numberTimes];
        int temp3[] = new int[numberTimes];
        int temp4[] = new int[numberTimes];

        for(int x=0;x<numberTimes;x++)
        {
            temp1[x]=hand.get(x).getRank();
            temp2[x]=hand.get(x).getRank();
            temp3[x]=hand.get(x).getRank();
            temp4[x]=hand.get(x).getRank();

        }//end for

        for(int y=0;y<numberTimes;y++){

            for(int z=0;z<numberTimes;z++){

                for(int a=0;a<numberTimes;a++){

                    for(int b=0;b<numberTimes;b++)
                    {
                        if(
                                (temp1[y]==temp2[z] && temp1[y]==temp3[a] && temp1[y]==temp4[b])
                                        &&
                                        (    (y!=z)&&(z!=a)&&(y!=a)&&(y!=b)&&(z!=b)&&(a!=b)  )
                                )
                        {

                            four=true;

                        }//end if

                    }//end for

                }//end for

            }//end for

        }//end for

        return four;

    }//end function


    static  boolean checkPair(ArrayList<CMCard> hand)
    {
        boolean pair=false;

        int temp1[] = new int[hand.size()];
        int temp2[] = new int[hand.size()];

        for(int x=0;x<hand.size();x++)
        {

            temp1[x]=hand.get(x).getRank();
            temp2[x]=hand.get(x).getRank();

        }//end for

        for(int y=0;y<hand.size();y++){

            for(int z=0;z<hand.size();z++){

                if((temp1[y]==temp2[z])&&(z!=y)){

                    pair=true;

                }//end if

            }//end for

        }//end for

        return pair;

    }//end function


    static  boolean  checkTwoPair(ArrayList<CMCard> hand)
    {
        if(checkPair(hand)&&!checkThreeKind(hand))
        {
            int a[] = new int[hand.size()];

            a[0]=hand.get(0).getRank();
            a[1]=hand.get(1).getRank();
            a[2]=hand.get(2).getRank();
            a[3]=hand.get(3).getRank();
//            a[4]=hand[4]->getNumber();

            int temp = 0;

            boolean twoPair=false;

            for(int x=0;x<4;x++){

                for(int y=0;y<4;y++){

                    if(x!=y&&(a[x]==a[y])){

                        temp=a[x];

                    }//end if

                }//end for

            }//end for

            for(int x=0;x<4;x++){

                for(int y=0;y<4;y++){

                    if(
                            ((a[x]!= temp && a[y]!=temp))
                                    &&
                                    (x!=y&&(a[x]==a[y]))
                            ){

                        twoPair=true;

                    }//end if

                }//end for

            }//end for

            return twoPair;

        }else{

            return false;

        }//end if

    }//end function

    static boolean checkFullHouse(ArrayList<CMCard> hand){

        if(checkThreeKind(hand))
        {
            int[] a = new int[5];

            a[0]=hand.get(0).getRank();
            a[1]=hand.get(1).getRank();
            a[2]=hand.get(2).getRank();
            a[3]=hand.get(3).getRank();
            a[4]=hand.get(4).getRank();

            Arrays.sort(a);


            int threeTriggerCard=a[2]; // middle card has to be part of the 3 Kind, sorted

            int[] holderArray = new int[2];

            for(int b=0,c=0;b<5;b++){

                if(a[b]!=threeTriggerCard){ // get out the two others

                    holderArray[c]=a[b];
                    c++; // lol

                }//end if

            }//end for

            if(holderArray[0]==holderArray[1]){

                return true;

            }else{

                return false;

            }//end if

        }else{

            return false;

        }//end if

    }//end function

//    #pragma mark - Test Royal Flush
    private static boolean checkRoyalFlushWithNumbers(ArrayList<CMCard>hand,int numberTimes)
    {
        if(hand.size() < 3)
        {
            return false;
        }
        else
        {
            boolean hasKing=false;
            boolean hasAce=false;

            for(int x=0;x<numberTimes; x++)
            {
                if(hand.get(x).getRank()==CardsConstants.CardRanks.ACE.getCardValue())
                {
                    hasAce=true;
                }//end if
                if(hand.get(x).getRank()==CardsConstants.CardRanks.KING.getCardValue())
                {
                    hasKing=true;
                }//end if

            }//end for


            if(hasKing&&hasAce&&checkStraightFlush(hand,hand.size()))
            {
                return true;
            }else
            {
                return false;

            }//end if
        }
    }//end function

    private static boolean checkStraightFlush(ArrayList<CMCard> hand , int numberTimes)
    {
//        if(checkFlush(hand)&&checkStraight(hand))
        if(checkFlushWithNumbers(hand, numberTimes) && checkStraightWithNumbers(hand, numberTimes))
        {
            return true;
        }
        else
        {
            return false;
        }//end if

    }//end function

    static boolean checkFlushWithNumbers(ArrayList<CMCard> hand , int numberTimes)
    {
        int firstSuits = hand.get(0).getSuit();
        int firstRank  = hand.get(0).getRank();
        int counter = 0;
        int sameCount = 0;
        for (int k=1;k<numberTimes; k++)
        {
            if(firstSuits == hand.get(k).getSuit())
            {
                counter++;
            }
            if (firstRank == hand.get(k).getRank())
            {
                sameCount++;
            }
        }

        if(counter == (numberTimes-1) && sameCount==0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }//end function

    static boolean checkStraightWithNumbers(ArrayList<CMCard> hand , int numberTimes)
    {
        int[] a = new int[numberTimes];



        for(int x=0;x<numberTimes;x++)
        {
            a[x]=hand.get(x).getRank();
        }


        Arrays.sort(a);
        boolean isStraight = false;

        // we get first value(0 index) , now start with 1st index ...
        int firstValue = a[0];
        int counter = 0;
        for (int k = 1; k<numberTimes;k++)
        {
            if ((firstValue+k) == a[k])
            {
                counter++;
            }
        }
        if (counter == (numberTimes-1))
        {
            isStraight = true;
        }
        if(isStraight)
        {
            return true;
        }
        else
        {
            counter = 0;
            for(int x=0;x<numberTimes;x++)
            {
                a[x]=hand.get(x).getRank();
            }

            //check for hi straight 2-6 to J-A

            for(int b=0;b<numberTimes;b++)
            {
                if(a[b]==CardsConstants.CardRanks.ACE.getCardValue())
                {
                    a[b]=CardsConstants.CardRanks.KING.getCardValue()+1;
                }//end if
            }//end for
            Arrays.sort(a);
            firstValue = a[0];

            for (int k = 1; k<numberTimes;k++)
            {
                if ((firstValue+k) == a[k])
                {
                    counter++;
                }
            }
            if (counter == (numberTimes-1))
            {
                isStraight = true;
            }
            return isStraight;
            //end if
        }//end if
    }

    static boolean checkThreeKindWithNumber(ArrayList<CMCard> hand, int numberTimes)
    {
        boolean three=false;


        int[] temp1 = new int[numberTimes];
        int[] temp2 = new int[numberTimes];
        int[] temp3 = new int[numberTimes];


        for(int x=0;x<numberTimes;x++)
        {

            temp1[x]=hand.get(x).getRank();
            temp2[x]=hand.get(x).getRank();
            temp3[x]=hand.get(x).getRank();

        }//end for

        for(int y=0;y<numberTimes;y++){

            for(int z=0;z<numberTimes;z++)
            {

                for(int a=0;a<numberTimes;a++)
                {
                    if(
                            (temp1[y]==temp2[z] && temp1[y]==temp3[a])
                                    &&
                                    (    (y!=z)&&(z!=a)&&(y!=a)  )
                            ){

                        three=true;

                    }//end if

                }//end for

            }//end for

        }//end for

        return three;

    }//end function



    static ArrayList<Integer> checkCardSuitsForHint(ArrayList<CMCard> handCards,ArrayList<CMCard> arrTbl,ArrayList<CardsConstants.CMHandType> allKeys,CardsConstants.CMHandType tblType,HashMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>> dicKindPatterns,CardsConstants.CMHandPlayType cardPlayType)
    {
        ArrayList<Integer> arrIdexs = null;
        for (int i=0; i<allKeys.size(); i++)
        {

            CardsConstants.CMHandType key = allKeys.get(i);

            // we need to check all set here, if 1 set does not have lowest best then other may have
            // if all does not have lowest best then user does not have lowest best
            if (key == tblType)
            {
//                CCArray* arrSets = (CCArray*) dicKindPatterns->objectForKey(key->getValue());

                ArrayList<ArrayList<CMCard>> arrSets = dicKindPatterns.get(key);

                if (arrSets.size() > 1)
                {
                    // multiple sets here. do get lowest best from all sets
                    ArrayList<ArrayList<CMCard>> arrBestSets = new ArrayList<ArrayList<CMCard>>();


                    for (int j=0; j<arrSets.size(); j++)
                    {

                        ArrayList<CMCard> arrCards = arrSets.get(j);

                        int idx = CMPokerUtils.bestFromTwoHands(arrCards, arrTbl, cardPlayType.getHandPlayValue());
                        if (idx==1)
                        {
//                            arrBestSets->addObject(arrCards);
                            arrBestSets.add(arrCards);
                        }
                    }
                    if (arrBestSets.size() > 0)
                    {
                        // arrbestsets in my cards.

                        ArrayList<CMCard> arrBestCards = arrBestSets.get(0);

                        for (int j=1; j<arrBestSets.size(); j++)
                        {
                            ArrayList<CMCard> arrCards = arrBestSets.get(j);

                            int idx = CMPokerUtils.bestFromTwoHands(arrBestCards, arrCards, cardPlayType.getHandPlayValue());
                            if (idx == 1) // arrCards is lower better card then  arrBestCards so sawp it.
                            {
                                arrBestCards = arrCards;
                            }
                        }

                        ArrayList<Integer> arrIdx = new ArrayList<Integer>();

                        for (int j=0; j<arrBestCards.size(); j++)
                        {
                            CMCard card = arrBestCards.get(j);
                            int idx = handCards.indexOf(card);
                            arrIdx.add(idx);
                        }
                        arrIdexs = arrIdx;
                        break;
                    }
                }
                else
                {

                    // single set here. so get best set table and user's hand as both are same level.
                    ArrayList<CMCard> arrCards = arrSets.get(0);

                    int userId = CMPokerUtils.bestFromTwoHands(arrCards, arrTbl, cardPlayType.getHandPlayValue());
                    if (userId==1)
                    {
                        // my card best.
                        ArrayList<Integer> arrIdx = new ArrayList<Integer>();

                        for (int j=0; j<arrCards.size(); j++)
                        {
                            CMCard card = arrCards.get(j);
                            int idx = handCards.indexOf(card);
                            arrIdx.add(idx);
                        }
                        arrIdexs = arrIdx;
                        break;
                    }
                }
            }
            else if (key.getHandValue() > tblType.getHandValue())
            {
                // key card patterns are lowest best.
                ArrayList<ArrayList<CMCard>> arrSets = dicKindPatterns.get(key);
                if (arrSets.size()>1)
                {
                    // multiple sets here. do get lowest best from all sets
                    ArrayList<CMCard> arrBestCards = arrSets.get(0);
                    for (int j=1; j<arrSets.size(); j++)
                    {
                        ArrayList<CMCard> arrCards = arrSets.get(j);
                        int idx = bestFromTwoHands(arrBestCards, arrCards, cardPlayType.getHandPlayValue());

                        if (idx == 1) // arrCards is lower better card then  arrBestCards so sawp it.
                        {
                            arrBestCards = arrCards;
                        }
                    }

                    // best card idx..
                    ArrayList<Integer> arrIdx = new ArrayList<Integer>();

                    for (int j=0; j<arrBestCards.size(); j++)
                    {
                        CMCard card =(CMCard)arrBestCards.get(j);
                        int idx = handCards.indexOf(card);
                        arrIdx.add(idx);
                    }
                    arrIdexs = arrIdx;
                    break;
                }
                else
                {
                    // single set here. so its best set for user's cards.

                    ArrayList<CMCard> arrCards = arrSets.get(0);
                    ArrayList<Integer> arrIdx = new ArrayList<Integer>();

                    for (int j=0; j<arrCards.size(); j++)
                    {
                        CMCard card = arrCards.get(j);
                        int idx = handCards.indexOf(card);
                        arrIdx.add(idx);
                    }
                    arrIdexs = arrIdx;
                    break;
                }
            }
        }
        if (arrIdexs!=null)
        {
            return arrIdexs;
        }
        return null;
    }



    //********************************** COMPARE ******************************************************


    static int compareThreeTypes(ArrayList<CMCard> arrCards1 , ArrayList<CMCard> arrCards2)
    {
        ArrayList<CMCard> cardVector = new ArrayList<CMCard>();
        ArrayList<CMCard> cardVector2 = new ArrayList<CMCard>();
        CardsConstants.CMHandType user1Type,user2Type;

        for (int i=0; i<arrCards1.size(); i++)
        {
            CMCard card = (CMCard) arrCards1.get(i);
            cardVector.add(card);

            CMCard card2 = (CMCard) arrCards2.get(i);
            cardVector2.add(card2);
        }

        // user 1 type checks
        if(CMPokerUtils.checkRoyalFlushWithNumbers(cardVector, 3))
        {
            user1Type = CardsConstants.CMHandType.HandRoyalFlush3;
        }
        else if (CMPokerUtils.checkStraightFlush(cardVector, 3))
        {
            user1Type = CardsConstants.CMHandType.HandStraightFlush3;
        }
        else if(CMPokerUtils.checkThreeKindWithNumber(cardVector, 3))
        {
            user1Type = CardsConstants.CMHandType.HandKindOf3;
        }
        else if(CMPokerUtils.checkFlushWithNumbers(cardVector, 3))
        {
            user1Type = CardsConstants.CMHandType.HandFlush3;
        }
        else if (CMPokerUtils.checkStraightWithNumbers(cardVector, 3))
        {
            user1Type = CardsConstants.CMHandType.HandStraight3;
        }
        else
        {
            user1Type = CardsConstants.CMHandType.HandTypeUnkown;
        }

        // user 2 type checks
        if(CMPokerUtils.checkRoyalFlushWithNumbers(cardVector2, 3))
        {
            user2Type = CardsConstants.CMHandType.HandRoyalFlush3;
        }
        else if (CMPokerUtils.checkStraightFlush(cardVector2, 3))
        {
            user2Type = CardsConstants.CMHandType.HandStraightFlush3;
        }
        else if(CMPokerUtils.checkThreeKindWithNumber(cardVector2, 3))
        {
            user2Type = CardsConstants.CMHandType.HandKindOf3;
        }
        else if(CMPokerUtils.checkFlushWithNumbers(cardVector2, 3))
        {
            user2Type = CardsConstants.CMHandType.HandFlush3;
        }
        else if (CMPokerUtils.checkStraightWithNumbers(cardVector2, 3))
        {
            user2Type = CardsConstants.CMHandType.HandStraight3;
        }
        else
        {
            user2Type = CardsConstants.CMHandType.HandTypeUnkown;
        }

        if (user1Type == user2Type)
        {
            switch (user2Type)
            {
                case HandRoyalFlush3:
                {
//                    CCLOG("Same HandRoyalFlush3 Check");
                    CMCard usercard1 = (CMCard) arrCards1.get(0);
                    CMCard usercard2 = (CMCard) arrCards2.get(0);
                    if (usercard1.getSuit() > usercard2.getSuit())
                    {
                        return 1;
                    }
                    else
                    {
                        return 2;
                    }
                }

                case HandStraightFlush3:
                {
//                    CCLOG("Same HandStraightFlush3 Check");
                    int countCheck = 0;

                    int index = -1;
                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);

                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 1;
                            }
                            else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                    }
                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }

                case HandKindOf3:
                {
//                    CCLOG("Same HandKindOf3 Check");
                    CMCard usercard1 = (CMCard)arrCards1.get(0);
                    CMCard usercard2 = (CMCard)arrCards2.get(0);

                    if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        return 1;
                    }
                    else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        return 2;
                    }
                    else if (usercard1.getRank() > usercard2.getRank())
                    {
                        return 1;
                    }
                    else
                    {
                        return 2;
                    }
                }

                case HandFlush3:
                {
//                    CCLOG("Same HandFlush3 Check");
                    int countCheck = 0;
                    int index = -1;

                    arrCards1 = sortByRankWithA(arrCards1);
                    arrCards2 = sortByRankWithA(arrCards2);

                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);

                        //                    if (usercard1->getSuit() == usercard2->getSuit())
                        //                    {
                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 1;
                            }
                            else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                        //                    }
                        //                    else if(usercard1->getSuit() > usercard2->getSuit())
                        //                    {
                        //                        index = 1;
                        //                        break;
                        //                    }
                        //                    else
                        //                    {
                        //                        index = 2;
                        //                        break;
                        //                    }
                    }

                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }

                case HandStraight3:
                {
//                    CCLOG("Same HandStraight3 Check");
                    int countCheck = 0;
                    int index = -1;
                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);

                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 1;
                            }
                            else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                    }

                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }

                default:
                    break;
            }

        }
        else
        {
            if (user1Type.getHandValue() >user2Type.getHandValue())
            {
                return 1;
            }
            else
            {
                return 2;
            }
        }

        return -1;
    }


    static  int compareFourTypes(ArrayList<CMCard> arrCards1 , ArrayList<CMCard> arrCards2)
    {
        ArrayList<CMCard> cardVector = new ArrayList<CMCard>();
        ArrayList<CMCard> cardVector2 = new ArrayList<CMCard>();
        CardsConstants.CMHandType user1Type,user2Type;

        for (int i=0; i<arrCards1.size(); i++)
        {
            CMCard card = (CMCard) arrCards1.get(i);
            cardVector.add(card);

            CMCard card2 = (CMCard) arrCards2.get(i);
            cardVector2.add(card2);
        }

        // user 1 type checks
        if(CMPokerUtils.checkRoyalFlushWithNumbers(cardVector, 4))
        {
            user1Type = CardsConstants.CMHandType.HandRoyalFlush4;
        }
        else if (CMPokerUtils.checkStraightFlush(cardVector, 4))
        {
            user1Type = CardsConstants.CMHandType.HandStraightFlush4;
        }
        else if(CMPokerUtils.checkThreeKindWithNumber(cardVector, 4))
        {
            user1Type = CardsConstants.CMHandType.HandKindOf4;
        }
        else if(CMPokerUtils.checkFlushWithNumbers(cardVector, 4))
        {
            user1Type = CardsConstants.CMHandType.HandFlush4;
        }
        else if (CMPokerUtils.checkStraightWithNumbers(cardVector, 4))
        {
            user1Type = CardsConstants.CMHandType.HandStraight4;
        }
        else if (CMPokerUtils.checkTwoPair(cardVector))
        {
            user1Type = CardsConstants.CMHandType.HandTwoPair;
        }
        else
        {
            user1Type = CardsConstants.CMHandType.HandTypeUnkown;
        }

        // user 2 type checks
        if(CMPokerUtils.checkRoyalFlushWithNumbers(cardVector2, 4))
        {
            user2Type = CardsConstants.CMHandType.HandRoyalFlush4;
        }
        else if (CMPokerUtils.checkStraightFlush(cardVector2, 4))
        {
            user2Type = CardsConstants.CMHandType.HandStraightFlush4;
        }
        else if(CMPokerUtils.checkFourKind(cardVector2))
        {
            user2Type = CardsConstants.CMHandType.HandKindOf4;
        }
        else if(CMPokerUtils.checkFlushWithNumbers(cardVector2, 4))
        {
            user2Type = CardsConstants.CMHandType.HandFlush4;
        }
        else if (CMPokerUtils.checkStraightWithNumbers(cardVector2, 4))
        {
            user2Type = CardsConstants.CMHandType.HandStraight4;
        }
        else if (CMPokerUtils.checkTwoPair(cardVector2))
        {
            user2Type = CardsConstants.CMHandType.HandTwoPair;
        }
        else
        {
            user2Type = CardsConstants.CMHandType.HandTypeUnkown;
        }

        if (user1Type == user2Type)
        {
            switch (user2Type)
            {
                case HandRoyalFlush4:
                {
                    CMCard usercard1 = (CMCard) arrCards1.get(0);
                    CMCard usercard2 = (CMCard) arrCards2.get(0);
                    if (usercard1.getSuit() > usercard2.getSuit())
                    {
                        return 1;
                    }
                    else
                    {
                        return 2;
                    }
                }

                case HandStraightFlush4:
                {
                    int countCheck = 0;

                    int index = -1;
                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);
                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 1;
                            }
                            else if (usercard2.getRank() ==CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                    }
                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }

                case HandKindOf4:
                {
                    CMCard usercard1 = (CMCard)arrCards1.get(0);
                    CMCard usercard2 = (CMCard)arrCards2.get(0);

                    if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        return 1;
                    }
                    else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        return 2;
                    }
                    else if (usercard1.getRank() > usercard2.getRank())
                    {
                        return 1;
                    }
                    else
                    {
                        return 2;
                    }
                }

                case HandFlush4:
                {
                    int countCheck = 0;
                    int index = -1;

                    arrCards1 = CMPokerUtils.sortByRankWithA(arrCards1);
                    arrCards2 = CMPokerUtils.sortByRankWithA(arrCards2);

                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);
                        //                    if (usercard1->getSuit() == usercard2->getSuit())
                        //                    {
                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 1;
                            }
                            else if (usercard2.getRank() ==CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                        //                    }
                        //                    else if(usercard1->getSuit() > usercard2->getSuit())
                        //                    {
                        //                        index = 1;
                        //                        break;
                        //                    }
                        //                    else
                        //                    {
                        //                        index = 2;
                        //                        break;
                        //                    }
                    }

                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }

                case HandStraight4:
                {
                    int countCheck = 0;
                    int index = -1;
                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);

                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 1;
                            }
                            else if (usercard2.getRank() ==CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                    }

                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }
                case HandTwoPair:
                {
                    int countCheck = 0;
                    int index = -1;
                    for (int i = 0; i<arrCards1.size(); i+=2)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);

                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() ==CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 1;
                            }
                            else if (usercard2.getRank() ==CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                return 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                    }
                    if (countCheck == arrCards1.size()/2)
                    {
                        index = -1;
                        boolean foundSpades = false;
                        for (int i =0; i<arrCards1.size()/2; i++)
                        {
                            CMCard card =  (CMCard)arrCards1.get(i);
                            if (card.getSuit()== CardsConstants.Suit.SPADES.getSuitsValue())
                            {
                                index = 1;
                                foundSpades = true;
                                break;
                            }
                        }
                        if (!foundSpades)
                        {
                            for (int i =0; i<arrCards2.size()/2; i++)
                            {
                                CMCard card =  (CMCard)arrCards2.get(i);
                                if (card.getSuit()==CardsConstants.Suit.SPADES.getSuitsValue())
                                {
                                    index = 2;
                                    foundSpades = true;
                                    break;
                                }
                            }
                        }
                        return index;
                    }
                    else
                    {
                        return index;
                    }
                }

                default:
                    break;
            }
        }
        else
        {
            if (user1Type.getHandValue() >user2Type.getHandValue())
            {
                return 1;
            }
            else
            {
                return 2;
            }
        }

        return -1;
    }


    static int compareFiveTypes(ArrayList<CMCard> arrCards1 , ArrayList<CMCard> arrCards2)
    {
        ArrayList<CMCard>  cardVector = new ArrayList<CMCard>();
        ArrayList<CMCard> cardVector2 = new ArrayList<CMCard>();

        CardsConstants.CMHandType user1Type,user2Type;

        for (int i=0; i<arrCards1.size(); i++)
        {
            CMCard card =  arrCards1.get(i);
            cardVector.add(card);

            CMCard card2 =  arrCards2.get(i);
            cardVector2.add(card2);
        }

        // user 1 type checks
        if(CMPokerUtils.checkRoyalFlushWithNumbers(cardVector, 5))
        {
            user1Type = CardsConstants.CMHandType.HandRoyalFlush5;
        }
        else if (CMPokerUtils.checkStraightFlush(cardVector, 5))
        {
            user1Type = CardsConstants.CMHandType.HandStraightFlush5;
        }
        else if(CMPokerUtils.checkFourKind(cardVector))
        {
            user1Type = CardsConstants.CMHandType.HandKindOf4One;
        }
        else if (CMPokerUtils.checkFullHouse(cardVector))
        {
            user1Type = CardsConstants.CMHandType.HandFullHouse;
        }
        else if(CMPokerUtils.checkFlushWithNumbers(cardVector, 5))
        {
            user1Type = CardsConstants.CMHandType.HandFlush5;
        }
        else if (CMPokerUtils.checkStraightWithNumbers(cardVector, 5))
        {
            user1Type = CardsConstants.CMHandType.HandStraight5;
        }
        else
        {
            user1Type = CardsConstants.CMHandType.HandTypeUnkown;
        }

        // user 2 type checks
        if(CMPokerUtils.checkRoyalFlushWithNumbers(cardVector2, 5))
        {
            user2Type = CardsConstants.CMHandType.HandRoyalFlush5;
        }
        else if (CMPokerUtils.checkStraightFlush(cardVector2, 5))
        {
            user2Type = CardsConstants.CMHandType.HandStraightFlush5;
        }
        else if(CMPokerUtils.checkFourKind(cardVector2))
        {
            user2Type = CardsConstants.CMHandType.HandKindOf4One;
        }
        else if (CMPokerUtils.checkFullHouse(cardVector2))
        {
            user2Type = CardsConstants.CMHandType.HandFullHouse;
        }
        else if(CMPokerUtils.checkFlushWithNumbers(cardVector2, 5))
        {
            user2Type = CardsConstants.CMHandType.HandFlush5;
        }
        else if (CMPokerUtils.checkStraightWithNumbers(cardVector2, 5))
        {
            user2Type = CardsConstants.CMHandType.HandStraight5;
        }
        else
        {
            user2Type = CardsConstants.CMHandType.HandTypeUnkown;
        }

        if (user1Type == user2Type)
        {
            switch (user2Type)
            {
                case HandRoyalFlush5:
                {
                    CMCard usercard1 = (CMCard) arrCards1.get(0);
                    CMCard usercard2 = (CMCard) arrCards2.get(0);
                    if (usercard1.getSuit() > usercard2.getSuit())
                    {
                        return 1;
                    }
                    else
                    {
                        return 2;
                    }
                }
//                break;
                case HandStraightFlush5:
                {
                    int countCheck = 0;

                    int index = -1;
                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);
                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                index = 1;
                            }
                            else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                index = 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                    }
                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }
//                break;
                case HandKindOf4One:
                {
                    int countCheck = 0;
                    int index = -1;

                    arrCards1 =     sortRankPairs(arrCards1);
                    arrCards2 =     sortRankPairs(arrCards2);

                    for (int i = 0; i<arrCards1.size(); i+=2)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);

                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                index = 1;
                            }
                            else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                index = 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                    }
                    if (countCheck == 3)
                    {
                        index = -1;
                        boolean foundSpades = false;

                        for (int i =0; i<arrCards1.size()/2; i++)
                        {
                            CMCard card =  (CMCard )arrCards1.get(i);
                            if (card.getSuit()==CardsConstants.Suit.SPADES.getSuitsValue())
                            {
                                index = 1;
                                foundSpades = true;
                                break;
                            }
                        }
                        if (!foundSpades)
                        {
                            for (int i =0; i<arrCards2.size()/2; i++)
                            {
                                CMCard card =  (CMCard )arrCards2.get(i);
                                if (card.getSuit()==CardsConstants.Suit.SPADES.getSuitsValue())
                                {
                                    index = 2;
                                    foundSpades = true;
                                    break;
                                }
                            }
                        }
                        return index;
                    }
                    else
                    {
                        return index;
                    }
                }
//                break;
                case HandFullHouse:
                {
                    CMCard threecard1 = null;
                    CMCard threecard2 = null;
                    int count = 0;
                    for (int i=0; i<arrCards1.size(); i++)
                    {
                        CMCard firstCard = (CMCard) arrCards1.get(i);
                        threecard1 = firstCard;
                        count = 1;
                        for (int j=i+1; j<arrCards1.size(); j++)
                        {
                            CMCard inner1Card = (CMCard) arrCards1.get(i);
                            if (inner1Card.getRank() == firstCard.getRank())
                            {
                                threecard1 = firstCard;
                                count++;
                            }
                            else
                            {
                                break;
                            }
                        }
                        if (count==3)
                        {
                            break;
                        }
                    }

                    for (int i=0; i<arrCards2.size(); i++)
                    {
                        CMCard secondCard = (CMCard) arrCards2.get(i);
                        threecard2 = secondCard;
                        count = 1;
                        for (int j=i+1; j<arrCards2.size(); j++)
                        {
                            CMCard inner2Card = (CMCard) arrCards2.get(i);
                            if (inner2Card.getRank() == secondCard.getRank())
                            {
                                threecard2 = secondCard;
                                count++;
                            }
                            else
                            {
                                break;
                            }
                        }
                        if (count==3)
                        {
                            break;
                        }
                    }
                    if (threecard1 != null && threecard2 != null)
                    {
                        if (threecard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                        {
                            return 1;
                        }
                        else if (threecard2.getRank() ==CardsConstants.CardRanks.ACE.getCardValue())
                        {
                            return 2;
                        }
                        else if (threecard1.getRank()>threecard2.getRank())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return -1;
                    }
                }
//                break;
                case HandFlush5:
                {
                    arrCards1 = CMPokerUtils.sortByRankWithA(arrCards1);
                    arrCards2 = CMPokerUtils.sortByRankWithA(arrCards2);

                    int countCheck = 0;
                    int index = -1;
                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);
                        //                    if (usercard1->getSuit() == usercard2->getSuit())
                        //                    {
                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                index = 1;
                            }
                            else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                index = 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                        //                    }
                        //                    else if(usercard1->getSuit() > usercard2->getSuit())
                        //                    {
                        //                        index = 1;
                        //                        break;
                        //                    }
                        //                    else
                        //                    {
                        //                        index = 2;
                        //                        break;
                        //                    }
                    }

                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }
//                break;
                case HandStraight5:
                {
                    int countCheck = 0;
                    int index = -1;
                    for (int i = 0; i<arrCards1.size(); i++)
                    {
                        CMCard usercard1 = (CMCard)arrCards1.get(i);
                        CMCard usercard2 = (CMCard)arrCards2.get(i);

                        if (usercard1.getRank() == usercard2.getRank())
                        {
                            countCheck++;
                        }
                        else
                        {
                            if (usercard1.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                index = 1;
                            }
                            else if (usercard2.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                            {
                                index = 2;
                            }
                            else if (usercard1.getRank() > usercard2.getRank())
                            {
                                index = 1;
                            }
                            else
                            {
                                index = 2;
                            }
                            break;
                        }
                    }

                    if (countCheck == arrCards1.size())
                    {
                        // check with suits
                        CMCard usercard1 = (CMCard)arrCards1.get(0);
                        CMCard usercard2 = (CMCard)arrCards2.get(0);
                        if (usercard1.getSuit() > usercard2.getSuit())
                        {
                            return 1;
                        }
                        else
                        {
                            return 2;
                        }
                    }
                    else
                    {
                        return index;
                    }
                }
//                break;
                default:
                    break;
            }
        }
        else
        {
            if (user1Type.getHandValue() > user2Type.getHandValue())
            {
                return 1;
            }
            else
            {
                return 2;
            }
        }

        return -1;
    }


    static int bestFromTwoHands(ArrayList<CMCard> arrCards1 , ArrayList<CMCard> arrCards2, int type)
    {


        arrCards1 = sortBySuitsAndRank(arrCards1);
        arrCards2 = sortBySuitsAndRank(arrCards2);

//    sortByRankWithA(arrCards1);
//    sortByRankWithA(arrCards2);

        arrCards1 = sortByRankWithAHighLow(arrCards1);
        arrCards2 = sortByRankWithAHighLow(arrCards2);

        int userIndex = -1;
        switch (type)
        {
            case 1:
            {
                CMCard user1Card = (CMCard)arrCards1.get(0);
                CMCard user2Card = (CMCard)arrCards2.get(0);

                if (user1Card.getRank() == user2Card.getRank())
                {
                    if (user1Card.getSuit()>user2Card.getSuit())
                    {
                        userIndex = 1;
                    }
                    else
                    {   userIndex = 2;

                    }
                }
                else if (user1Card.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                {
                    userIndex = 1;
                }
                else if (user2Card.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                {
                    userIndex = 2;
                }
                else if (user1Card.getRank() > user2Card.getRank())
                {
                    userIndex = 1;
                }
                else
                {
                    userIndex = 2;
                }
            }
            break;
            case 2:
            {
                CMCard user1Card = (CMCard)arrCards1.get(0);
                CMCard user2Card = (CMCard)arrCards2.get(0);

                if (user1Card.getRank() == user2Card.getRank())
                {
                    boolean foundSpades = false;
                    for (int i =0; i<arrCards1.size(); i++)
                    {
                        CMCard card =  (CMCard)arrCards1.get(i);
                        if (card.getSuit() == CardsConstants.Suit.SPADES.getSuitsValue())
                        {
                            userIndex = 1;
                            foundSpades = true;
                            break;
                        }
                    }
                    if (!foundSpades)
                    {
                        for (int i =0; i<arrCards2.size(); i++)
                        {
                            CMCard card =  (CMCard )arrCards2.get(i);
                            if (card.getSuit() == CardsConstants.Suit.SPADES.getSuitsValue())
                            {
                                userIndex = 2;
                                foundSpades = true;
                                break;
                            }
                        }
                    }
                }
                else if (user1Card.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                {
                    userIndex = 1;
                }
                else if (user2Card.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                {
                    userIndex = 2;
                }
                else if (user1Card.getRank() > user2Card.getRank())
                {
                    userIndex = 1;
                }
                else
                {
                    userIndex = 2;
                }

            }
            break;
            case 3:
            {
                userIndex = CMPokerUtils.compareThreeTypes(arrCards1, arrCards2);
            }
            break;
            case 4:
            {
                userIndex = compareFourTypes(arrCards1, arrCards2);
            }
            break;
            case 5:
            {
                userIndex = compareFiveTypes(arrCards1, arrCards2);
            }
            break;
            default:
            {
//                CCLOG("Serious error! with cards");
                userIndex = -1;
            }
            break;
        }
        return userIndex;
    }



    //***************************** Hand Hints ************************************************

    public  static  ArrayList<Integer> RoyalFlushHandsHint(ArrayList<CMCard> handsCards ,int flushType)
    {
        HashMap<Integer,ArrayList<CMCard>> cardsDetailsDic = new HashMap<Integer,ArrayList<CMCard>>();
        cardsDetailsDic.put(CardsConstants.Suit.HEARTS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.DIAMONDS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.CLUBS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.SPADES.getSuitsValue(), new ArrayList<CMCard>());
        for (int i = 0; i<handsCards.size(); i++)
        {
            CMCard cardDetails = (CMCard) handsCards.get(i);
            if (cardDetails.getRank() == CardsConstants.CardRanks.ACE.getCardValue() || cardDetails.getRank() == CardsConstants.CardRanks.KING.getCardValue() || cardDetails.getRank() == CardsConstants.CardRanks.QUEEN.getCardValue() )
            {
                ArrayList<CMCard> array = (ArrayList<CMCard>)cardsDetailsDic.get(cardDetails.getSuit());
                if (cardDetails.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                {
                    array.add(0,cardDetails);
                }
                else
                {
                    array.add(cardDetails);
                }
            }
            if (flushType == 4)
            {
                if (cardDetails.getRank() == CardsConstants.CardRanks.JACK.getCardValue())
                {
                    ArrayList<CMCard>  array = (ArrayList<CMCard> )cardsDetailsDic.get(cardDetails.getSuit());
                    array.add(cardDetails);
                }
            }
            if (flushType == 5)
            {
                if (cardDetails.getRank() == CardsConstants.CardRanks.JACK.getCardValue() || cardDetails.getRank() == CardsConstants.CardRanks.TEN.getCardValue())
                {
                    ArrayList<CMCard> array = (ArrayList<CMCard>) cardsDetailsDic.get(cardDetails.getSuit());
                    array.add(cardDetails);
                }
            }
        }
        ArrayList<CMCard> arrHighestCardsDetails = new ArrayList<CMCard>();
        //    CardColor lastColor;
        Map<Integer, ArrayList<CMCard>> treeMap = new TreeMap<Integer, ArrayList<CMCard>>(cardsDetailsDic);
        Set<Integer> allKeys1 = treeMap.keySet();

        ArrayList<Integer> allKeys = new ArrayList<Integer>();

        Object[] array = allKeys1.toArray();

        for(int i = 0; i < array.length; i++){

            allKeys.add((Integer)array[i]);

        }
        int CardSuits = -1;
        for (int i=0; i<allKeys.size(); i++)
        {
            Integer keyName = (Integer) allKeys.get(i);
            ArrayList<CMCard> arr = (ArrayList<CMCard>) cardsDetailsDic.get(keyName);

            if (arr.size() == flushType)
            {
//                CCLOG("checking key for : %s",keyName->getCString());
                if (CardSuits != -1)
                {
                    int newSuits = keyName;
                    if (newSuits > CardSuits)
                    {
                        CardSuits = newSuits;
                        arrHighestCardsDetails = arr;
                    }
                }
                else
                {
                    CardSuits = keyName;
                    arrHighestCardsDetails = arr;
                }
            }
        }
        if (arrHighestCardsDetails != null && arrHighestCardsDetails.size() > 0)
        {
            ArrayList<Integer> arrIndex = new ArrayList<Integer>();

            for (int j = 0; j < arrHighestCardsDetails.size(); j++)
            {
                CMCard card = (CMCard) arrHighestCardsDetails.get(j);
                int index = handsCards.indexOf(card);
                arrIndex.add(index);
            }
            return arrIndex;
        }


        return null;
    }

    public  static  ArrayList<Integer> StraightFlushHandsHint(ArrayList<CMCard> handsCards , int flushType)
    {

        HashMap<Integer,ArrayList<CMCard>> cardsDetailsDic = new HashMap<Integer,ArrayList<CMCard>>();
        cardsDetailsDic.put(CardsConstants.Suit.HEARTS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.DIAMONDS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.CLUBS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.SPADES.getSuitsValue(), new ArrayList<CMCard>());


        for (int i = 0; i<handsCards.size(); i++)
        {
            CMCard cardDetails = handsCards.get(i);
            ArrayList<CMCard> arr = cardsDetailsDic.get(cardDetails.getSuit());
            arr.add(cardDetails);
        }

        ArrayList<CMCard> arrHighestStraight = new ArrayList<CMCard>();
        ArrayList<CMCard> arrStraightCards = new ArrayList<CMCard>();

        int CardSuits = -1;

        Map<Integer, ArrayList<CMCard>> treeMap = new TreeMap<Integer, ArrayList<CMCard>>(cardsDetailsDic);
        Set<Integer> allKeys1 = treeMap.keySet();

        ArrayList<Integer> allKeys = new ArrayList<Integer>();

        Object[] array = allKeys1.toArray();

        for(int i = 0; i < array.length; i++){

            allKeys.add((Integer)array[i]);

        }

        for (int i=0; i<allKeys.size(); i++)
        {
            int keyName = allKeys.get(i);
            ArrayList<CMCard> arr = cardsDetailsDic.get(keyName);

            if (arr.size() > 0)
            {
                for (int j =0; j<arr.size(); j++)
                {
                    CMCard first = arr.get(j);
                    arrHighestStraight.add(first);
                    int y = 1;
                    for (int k = j+1; k < arr.size(); k++)
                    {
                        CMCard second = arr.get(k);

                        if (first.getRank() == second.getRank()+y)
                        {
                            arrHighestStraight.add(second);

                            if (arrHighestStraight.size() == flushType)
                            {
                                if (arrStraightCards.size() > 0)
                                {
                                    int sameCount=0;
                                    for (int x=0; x<arrStraightCards.size(); x++)
                                    {
                                        CMCard first1 = arrStraightCards.get(x);
                                        CMCard second1 = arrStraightCards.get(x);

                                        if (second1.getRank() > first1.getRank())
                                        {
                                            arrStraightCards.clear();
                                            arrStraightCards.addAll(arrHighestStraight);
                                            CardSuits = second1.getSuit();
                                            break;
                                        }
                                        else  if (second1.getRank() == first1.getRank())
                                        {
                                            sameCount++;
                                        }
                                    }
                                    if (sameCount==flushType)
                                    {
                                        // get highest suits data
                                        CMCard first1 = arrStraightCards.get(0);
                                        CMCard second1 = arrHighestStraight.get(0);

                                        if (second1.getSuit() > first1.getSuit())
                                        {
                                            arrStraightCards.clear();
                                            arrStraightCards.addAll(arrHighestStraight);
                                            CardSuits = second1.getSuit();

                                            break;
                                        }
                                    }
                                }
                                else
                                {
                                    arrStraightCards.addAll(arrHighestStraight);
                                    CardSuits = second.getSuit();
                                    arrHighestStraight.clear();;
                                }

                                break;
                            }
                        }
                        else
                        {
                            arrHighestStraight.clear();
                            break;
                        }
                        y++;
                    }
                    arrHighestStraight.clear();
                }
                arrHighestStraight.clear();
                ///*****/
            }
            arrHighestStraight.clear();
        }

        if (arrStraightCards.size()>0 && CardSuits!=-1)
        {
            ArrayList<Integer> arrIndex = new ArrayList<Integer>();

            for (int m=0; m<arrStraightCards.size(); m++)
            {

                CMCard card = arrStraightCards.get(m);
                int index = handsCards.indexOf(card);
                arrIndex.add(index);
            }
            return arrIndex;
        }
        return null;
    }


    public  static  ArrayList<Integer> FullHouseHandHint(ArrayList<CMCard> handCards)
    {
        ArrayList<CMCard> arrHand = new ArrayList<CMCard>();
        arrHand.addAll(handCards);


        ArrayList<Integer> arrIndex = KindOfHandHint(arrHand, 3, false);
        if (arrIndex != null)
        {
            for (int i = arrIndex.size()-1; i >=0; i--)
            {
                int intValue = arrIndex.get(i);
                arrHand.remove(intValue);
            }

            ArrayList<Integer> arrIndex2 = getBestTwoCardHands(arrHand);
            if (arrIndex2 != null)
            {
                for (int i = 0; i <arrIndex2.size(); i++)
                {
                    int intValue = arrIndex2.get(i);

                    CMCard card = (CMCard)arrHand.get(intValue);
                    int index = handCards.indexOf(card);
                    arrIndex.add(index);
                }

                for (int j = 0; j < arrIndex.size(); j++)
                {
                    int intValue = arrIndex.get(j);
                    CMCard card = (CMCard)handCards.get(intValue);

                }
                return arrIndex;
            }
        }
        return null;
    }


    public  static  ArrayList<Integer> StraightHandHint(ArrayList<CMCard> handCards, int flushType)
    {
        ArrayList<CMCard> arrHighestStraight = new ArrayList<CMCard>();
        ArrayList<CMCard> arrStraightCards = new ArrayList<CMCard>();

        int cardRank = -1;
        boolean straightFound = false;
        for (int j =0; j<handCards.size(); j++)
        {
            CMCard first = (CMCard)handCards.get(j);
            arrHighestStraight.add(first);
            int y = 1;
            for (int k = j+1; k < handCards.size(); k++)
            {
                CMCard second = (CMCard) handCards.get(k);
                CMCard lastAdded = (CMCard) arrHighestStraight.get(arrHighestStraight.size() - 1);

                if (lastAdded.getRank() == second.getRank())
                {
                    continue;
                }
                if (first.getRank()==second.getRank())
                {
                    y--;
                }
                else if (first.getRank() == second.getRank()+y)
                {
                    arrHighestStraight.add(second);
                    if (arrHighestStraight.size() == flushType)
                    {
                        straightFound = true;
                        arrStraightCards.addAll(arrHighestStraight);
                        cardRank = second.getSuit();
                        arrHighestStraight.clear();
                        break;
                    }
                }
                else
                {
                    arrHighestStraight.clear();
                    break;
                }
                y++;
            }
            arrHighestStraight.clear();
            if (straightFound)
            {
                break;
            }
        }

        if (arrStraightCards != null && arrStraightCards.size() > 0 && cardRank!=-1)
        {
            ArrayList<Integer> arrIndex = new ArrayList<Integer>();

            for (int j = 0; j < arrStraightCards.size(); j++)
            {
                CMCard card = (CMCard)arrStraightCards.get(j);
                int index = handCards.indexOf(card);
                arrIndex.add(index);
            }
            return arrIndex;
        }

        return null;
    }

    public  static  ArrayList<Integer> TwoPairsHandHint(ArrayList<CMCard> handCards)
    {
        ArrayList<CMCard> arrHand = new ArrayList<CMCard>();

        arrHand.addAll(handCards);

        ArrayList<Integer> arrIndex = getBestTwoCardHands(arrHand);
        if (arrIndex != null && arrIndex.size() > 0)
        {
            ArrayList<CMCard> arrToRemove = new ArrayList<CMCard>();
            for (int i = arrIndex.size() - 1; i>=0; i--)
            {
                int intValue = arrIndex.get(i);
                arrToRemove.add(arrHand.get(intValue));

            }
            arrHand.removeAll(arrToRemove);

            ArrayList<Integer> arrIndex2  = getBestTwoCardHands(arrHand);
            if (arrIndex2 != null  && arrIndex.size() > 0)
            {
                for (int i =0; i<arrIndex2.size(); i++)
                {
                    //                arrIndex->addObject(arrIndex2->objectAtIndex(i));
                    int intValue = arrIndex2.get(i);

                    CMCard card = (CMCard)arrHand.get(intValue);
                    int index = handCards.indexOf(card);
                    arrIndex.add(index);
                }

                for (int j=0; j<arrIndex.size(); j++)
                {
                    int intIndex = arrIndex.get(j);
                    CMCard card = handCards.get(intIndex);
                    System.out.println("Cards : "+ card.getRank());
                }
                return arrIndex;
            }
        }
        return null;
    }





    //*******************************   Check Card Hint   *******************************************

    public static ArrayList<Integer> getPossibleOneCardsHints(ArrayList<CMCard> handCards,ArrayList<CMCard> tableCards){
        // First Sort cards
        ArrayList<CMCard> arrCards = new ArrayList<CMCard>();
        arrCards.addAll(handCards);



        arrCards = sortByRankWithA(arrCards);

        for(int i = 0; i < arrCards.size(); i++){

            CMCard card = arrCards.get(i);
            System.out.println("sortedCard " + (card.getSuit() * 100 + card.getRank()));
        }

        // return next highest from all cards.

        CMCard cardCompare = (CMCard)tableCards.get(0); // Hoping It has only 1 card and we are accessing directly.
        CMCard highCard = null;
        for (int i = arrCards.size()-1; i>=0; i--)
        {
            CMCard card = (CMCard)arrCards.get(i);

            if (card.getRank() == cardCompare.getRank())
            {
                if (card.getSuit() > cardCompare.getSuit())
                {
                    highCard = card;
                    break;
                }
            }
            else if (card.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
            {
                highCard = card;
                break;
            }
            else if (cardCompare.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
            {
                // other card higer ,so continue and find other best cards
                continue;
            }
            else if (card.getRank() > cardCompare.getRank())
            {
//                CCLOG("cardRank : %d",card->getRank());
//                CCLOG("cardCompareRank : %d",cardCompare->getRank());
                highCard = card;
                break;
            }
        }
        if (highCard!=null)
        {



            // return array with index of highcard in handCards
            int idx = handCards.indexOf(highCard);
            ArrayList<Integer> arrCardsToReturn = new ArrayList<Integer>();
            arrCardsToReturn.add(idx);

            System.out.println("High Card : " + (highCard.getSuit() * 100 + highCard.getRank()) + "    idx : " + idx);


//            CCArray* arrIdxs = CCArray::create();
//            arrIdxs->addObject(CCInteger::create(idx));
            return arrCardsToReturn;
        }
        return null;


    }

    public static ArrayList<Integer>  getPossibleTwoCardsHints(ArrayList<CMCard> handCards,ArrayList<CMCard> tableCards){

        // First Sort cards
        ArrayList<CMCard> arrCards = new ArrayList<CMCard>();
        arrCards.addAll(handCards);
        //Sort By Rank
        arrCards = sortByRankWithA(arrCards);

        HashMap<Integer, ArrayList<ArrayList<CMCard>>> dicCardsPairs = new HashMap<Integer, ArrayList<ArrayList<CMCard>>>();

        CMCard cardTbl = (CMCard)tableCards.get(0); // Hoping It has only 1 card and we are accessing directly.


        for (int i=0; i<arrCards.size(); i++)
        {
            CMCard card1 = (CMCard)arrCards.get(i);
            for (int j=i+1; j<arrCards.size(); j++)
            {
                CMCard card2 = (CMCard)arrCards.get(j);
                if (card1.getRank() == CardsConstants.CardRanks.ACE.getCardValue() && (card1.getRank() == card2.getRank()))
                {
                    // its Ace pairs allow it.
                    ArrayList<ArrayList<CMCard>> arrCardsInLoop = (ArrayList<ArrayList<CMCard>>) dicCardsPairs.get(card1.getRank());

                    if (arrCardsInLoop == null)
                    {
                        arrCardsInLoop = new ArrayList<ArrayList<CMCard>>();

                        dicCardsPairs.put(card1.getRank(), arrCardsInLoop);
                    }

                    ArrayList<CMCard> innerArr = new ArrayList<CMCard>();
                    innerArr.add(card1);
                    innerArr.add(card2);
                    arrCardsInLoop.add(innerArr);


                }
                else if (cardTbl.getRank()!= CardsConstants.CardRanks.ACE.getCardValue())
                {
                    if ((card1.getRank() >= cardTbl.getRank()) && (card1.getRank()==card2.getRank()))
                    {
                        // its greater or equal and pairs
                        // its Ace pairs allow it.
                        ArrayList<ArrayList<CMCard>> arrCardsInLoop = (ArrayList<ArrayList<CMCard>>) dicCardsPairs.get(card1.getRank());


                        if (arrCardsInLoop == null)
                        {
                            arrCardsInLoop = new ArrayList<ArrayList<CMCard>>();

                            dicCardsPairs.put(card1.getRank(), arrCardsInLoop);
//                            arrCardsInLoop.add(card1.getRank(),arrCardsInLoop.get(0));
                        }

                        ArrayList<CMCard> innerArr = new ArrayList<CMCard>();
                        innerArr.add(card1);
                        innerArr.add(card2);
                        arrCardsInLoop.add(innerArr);
                    }
                    else
                        break;
                }
                else
                    break;
            }
        }


        Map<Integer, ArrayList<ArrayList<CMCard>>> treeMap = new TreeMap<Integer, ArrayList<ArrayList<CMCard>>>(dicCardsPairs);
        Set<Integer> allKeys1 = treeMap.keySet();

        ArrayList<Integer> allKeys = new ArrayList<Integer>();

        Object[] array = allKeys1.toArray();


        for(int i = 0; i < array.length; i++){

            allKeys.add((Integer)array[i]);

        }

//        Object[] array = allKeys.toArray();

        ArrayList<ArrayList<CMCard>> arrPairsCards = null;
        if (allKeys != null && allKeys.size() > 0)
        {
//            std::sort(allKeys->data->arr, allKeys->data->arr+allKeys->data->num, compareSortKey);

            // Arrang ACE to LAST if its on first place.
            Integer key = allKeys.get(0);

            if(key == CardsConstants.CardRanks.ACE.getCardValue())
            {   allKeys.add(key);
                allKeys.remove(0);
            }
            // we have cards to compares.

            for (Integer objCard :allKeys)
            {
                Integer keyinLoop = (Integer)objCard;
                if(keyinLoop == CardsConstants.CardRanks.ACE.getCardValue())
                {
                    // key is greater or equal. find best from dic and return it.
                    arrPairsCards = (ArrayList<ArrayList<CMCard>>)dicCardsPairs.get(keyinLoop);
                    break;
                }
                else if (keyinLoop > cardTbl.getRank())
                {
                    // key is greater or equal. find best from dic and return it.
                    arrPairsCards = (ArrayList<ArrayList<CMCard>>)dicCardsPairs.get(keyinLoop);
                    break;
                }
                else if (keyinLoop == cardTbl.getRank())
                {
                    arrPairsCards = (ArrayList<ArrayList<CMCard>>)dicCardsPairs.get(keyinLoop);
                    ArrayList<CMCard> arrPairs = (ArrayList<CMCard>)arrPairsCards.get(0);

                    CMCard card1 = (CMCard)arrPairs.get(0);
                    CMCard card2 = (CMCard)arrPairs.get(1);

                    CMCard tblCard1 = (CMCard)tableCards.get(0);
                    CMCard tblCard2 = (CMCard)tableCards.get(1);

                    arrPairsCards = null;

                    if ((card1.getSuit() > tblCard1.getSuit()) && (card1.getSuit() > tblCard2.getSuit()))
                    {
                        arrPairsCards = (ArrayList<ArrayList<CMCard>>)dicCardsPairs.get(keyinLoop);
                    }
                    else if ((card2.getSuit() > tblCard1.getSuit()) && (card2.getSuit() > tblCard2.getSuit()))
                    {
                        arrPairsCards = (ArrayList<ArrayList<CMCard>>)dicCardsPairs.get(keyinLoop);
                    }
                }
            }
        }



        if (arrPairsCards != null)
        {
            // we found better choice in pairs lets compare n get it.
            if (arrPairsCards.size() > 2)
            {
                // may have four or three cards
                // sort card from low to high on base of suits and get first index card.

            /*
            (C8,D8),(C8,H8),(C8,S8),(D8,H8),(D8,S8),(H8,S8).
            */

                int min_j;
                for (int i=0; i<arrPairsCards.size(); i++)
                {
                    min_j = i;
                    for (int j=i+1; j<arrPairsCards.size();j++)
                    {
                        ArrayList<CMCard> newPair= (ArrayList<CMCard>)arrPairsCards.get(j);
                        ArrayList<CMCard> arrPairs = (ArrayList<CMCard>)arrPairsCards.get(min_j);

                        CMCard card11 = (CMCard) arrPairs.get(0);
                        CMCard card12 = (CMCard) arrPairs.get(1);

                        CMCard card21 = (CMCard) newPair.get(0);
                        CMCard card22 = (CMCard) newPair.get(1);

                        CMCard card1Cmp,card2Cmp;
                        boolean isiCard1,isjCard1;

                        // finding lowest from first pairs
                        if (card11.getSuit() < card12.getSuit())
                        {
                            // in 1st pair card1 is lower suit then card 2 so use card1 for compare
                            card1Cmp = card11;
                            isiCard1 = true;
                        }
                        else
                        {
                            // in 1st pair card2 is lower suit then card 1 so use card2 for compare
                            card1Cmp = card12;
                            isiCard1 = false;
                        }

                        // finding lowest from 2nd pairs
                        if (card21.getSuit() < card22.getSuit())
                        {
                            // in 1st pair card1 is lower suit then card 2 so use card1 for compare
                            card2Cmp = card21;
                            isjCard1 = true;
                        }
                        else
                        {
                            // in 1st pair card2 is lower suit then card 1 so use card2 for compare
                            card2Cmp = card22;
                            isjCard1 = false;
                        }
                        if (card1Cmp.getSuit() == card2Cmp.getSuit())
                        {
                            // highest card same user other card for lowest check.
                            if(isiCard1){
                                card1Cmp = card12;
                            }else{
                                card1Cmp = card11;
                            }
                            if(isjCard1){
                                card2Cmp = card22;
                            }else{
                                card2Cmp = card21;
                            }

                        }
                        //compare if lowest swap
                        if(card2Cmp.getSuit() < card1Cmp.getSuit() )
                        {
                            min_j = j; // We found a smaller minimum, update min_j
                        }

                    }
                    Collections.swap(arrPairsCards, min_j, i);

                }


                ArrayList<CMCard> arrCard = (ArrayList<CMCard>)arrPairsCards.get(0);

                if (arrCard != null)
                {
                    CMCard card1 = (CMCard)arrCard.get(0);
                    CMCard card2 = (CMCard)arrCard.get(1);

                    ArrayList<Integer> arrIdx = new ArrayList<Integer>();
                    Integer idx1 = new Integer(handCards.indexOf(card1));

                    Integer idx2 = new Integer(handCards.indexOf(card2));
                    arrIdx.add(idx1);
                    arrIdx.add(idx2);
                    return arrIdx;
                }
            }
            else
            {
                // have only two cards use it now.
                ArrayList<CMCard> arrPairs = (ArrayList<CMCard>)arrPairsCards.get(0); // contains single array with two cards inside innerArray
                CMCard card1 = (CMCard)arrPairs.get(0);
                CMCard card2 = (CMCard)arrPairs.get(1);

                ArrayList<Integer> arrIdx = new ArrayList<Integer>();
                Integer idx1 = new Integer(handCards.indexOf(card1));

                Integer idx2 = new Integer(handCards.indexOf(card2));
                arrIdx.add(idx1);
                arrIdx.add(idx2);
                return arrIdx;
            }
        }
        return null;

    }

    public static ArrayList<Integer>  getPossibleThreeCardsHints(ArrayList<CMCard> handCards,ArrayList<CMCard> tableCards){

        // First get sets of possible sets of 3 from hands cards.
//        // store all hands as per their types.

        ArrayList<CMCard> tableCardVector = new ArrayList<CMCard>();
        for (int i =0; i<tableCards.size(); i++)
        {
            CMCard card = (CMCard)tableCards.get(i);
            tableCardVector.add(card);
        }

        CardsConstants.CMHandType tblType = checkThreeCardPattern(tableCardVector);

        ArrayList<CMCard> arrHands = new ArrayList<CMCard>();
        arrHands.addAll(handCards);
        //Sort By Rank
        arrHands = sortByRankWithA(arrHands);

        ArrayList<CMCard> arrTbl = new ArrayList<CMCard>();
        arrTbl.addAll(tableCards);
        //Sort By Rank
        arrTbl = sortByRankWithA(arrTbl);


        HashMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>> dicKindPatterns = new HashMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>>();

        for (int i=0; i<arrHands.size(); i++)
        {
            CMCard card1 = (CMCard) arrHands.get(i);
            for (int j=i+1; j<arrHands.size(); j++)
            {
                CMCard card2 = (CMCard) arrHands.get(j);
                for (int k=j+1; k<arrHands.size(); k++)
                {
                    CMCard card3 = (CMCard) arrHands.get(k);

                    ArrayList<CMCard> cardVec = new ArrayList<CMCard>();
                    cardVec.add(card1);
                    cardVec.add(card2);
                    cardVec.add(card3);

                    CardsConstants.CMHandType userType = checkThreeCardPattern(cardVec);

                    if (userType!=CardsConstants.CMHandType.HandTypeUnkown && userType.getHandValue() >= tblType.getHandValue())
                    {
                        // user card type is known and is better then tbltype

                        ArrayList<CMCard> arr = new ArrayList<CMCard>();
                        arr.add(card1);
                        arr.add(card2);
                        arr.add(card3);


                        arr = sortByRankWithA(arr); // sorting possible set of cards here

                        ArrayList<ArrayList<CMCard>> arrType = (ArrayList<ArrayList<CMCard>> ) dicKindPatterns.get(userType);

                        if (arrType==null)
                        {
                            // Setting Type of Card Sets Array
//                            arrType = CCArray::create();

                            arrType = new ArrayList<>();

                            dicKindPatterns.put(userType, arrType);
                        }
                        arrType.add(arr);
                    }
                }
            }
        }


        Map<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>> treeMap = new TreeMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>>(dicKindPatterns);
        Set<CardsConstants.CMHandType> allKeys1 = treeMap.keySet();

        ArrayList<CardsConstants.CMHandType> allKeys = new ArrayList<CardsConstants.CMHandType>();

        Object[] array = allKeys1.toArray();

        for(int i = 0; i < array.length; i++){

            allKeys.add((CardsConstants.CMHandType)array[i]);

        }

        if (allKeys!=null && allKeys.size() > 0)
        {
            // cards exist get and find better;
            ArrayList<Integer> arrIdexs = checkCardSuitsForHint(handCards, arrTbl, allKeys, tblType, dicKindPatterns, CardsConstants.CMHandPlayType.HandPlayThree);

            if (arrIdexs!=null)
            {
                return arrIdexs;
            }
        }

        return null;

    }

    public  static  ArrayList<Integer> getPossibleFourCardsHints(ArrayList<CMCard> handCards,ArrayList<CMCard> tableCards)
    {
        // First get sets of possible sets of 4 from hands cards.
        // store all hands as per their types.
        ArrayList<CMCard> tableCardVector = new ArrayList<CMCard>() ;
        for (int i =0; i<tableCards.size(); i++)
        {
            CMCard card = (CMCard)tableCards.get(i);
            tableCardVector.add(card);
        }

        CardsConstants.CMHandType tblType = checkFourCardPattern(tableCardVector);

        ArrayList<CMCard> arrHands = new ArrayList<CMCard>();
        arrHands.addAll(handCards);
        arrHands = sortByRankWithA(arrHands);

        ArrayList<CMCard> arrTbl = new ArrayList<CMCard>();
        arrTbl.addAll(tableCards);
        arrTbl = sortByRankWithA(arrTbl);

        // getting valid sets of pattern from user's card
        HashMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>> dicKindPatterns = new HashMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>>();
        for (int i=0; i<arrHands.size(); i++)
        {
            CMCard card1 = (CMCard) arrHands.get(i);
            for (int j=i+1; j<arrHands.size(); j++)
            {
                CMCard card2 = (CMCard) arrHands.get(j);
                for (int k=j+1; k<arrHands.size(); k++)
                {
                    CMCard card3 = (CMCard) arrHands.get(k);
                    for (int l=k+1; l<arrHands.size(); l++)
                    {
                        CMCard card4 = (CMCard) arrHands.get(l);

                        // Vector set of cards.
                        ArrayList<CMCard> cardVec = new ArrayList<CMCard>();
                        cardVec.add(card1);
                        cardVec.add(card2);
                        cardVec.add(card3);
                        cardVec.add(card4);

                        CardsConstants.CMHandType userType = checkFourCardPattern(cardVec);

                        if (userType!=CardsConstants.CMHandType.HandTypeUnkown && userType.getHandValue() >= tblType.getHandValue())
                        {
                            // user card type is known and is better then table card type.
                            ArrayList<CMCard> arr = new ArrayList<CMCard>();
                            arr.add(card1);
                            arr.add(card2);
                            arr.add(card3);
                            arr.add(card4);

                            arr = sortByRankWithA(arr); // sorting possible set of cards here

                            ArrayList<ArrayList<CMCard>> arrType = (ArrayList<ArrayList<CMCard>>)dicKindPatterns.get(userType.getHandValue());
                            if (arrType == null)
                            {
                                // Setting Type of Card Sets Array
                                arrType = new ArrayList<ArrayList<CMCard>>();
                                dicKindPatterns.put(userType,arrType);
                            }
                            arrType.add(arr);

                        }
                    }
                }
            }
        }
        Map<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>> treeMap = new TreeMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>>(dicKindPatterns);
        Set<CardsConstants.CMHandType> allKeys1 = treeMap.keySet();

        ArrayList<CardsConstants.CMHandType> allKeys = new ArrayList<CardsConstants.CMHandType>();

        Object[] array = allKeys1.toArray();

        System.out.println("getPossibleTwoCardsHints : 4.01 array.length: " + array.length + "");

        for(int i = 0; i < array.length; i++){

            allKeys.add((CardsConstants.CMHandType)array[i]);

        }
        System.out.println("getPossibleTwoCardsHints : 4.1");

        if (allKeys!= null && allKeys.size() > 0)
        {

            // cards exist get and find better;
            ArrayList<Integer> arrIdexs = checkCardSuitsForHint(handCards, arrTbl, allKeys, tblType, dicKindPatterns, CardsConstants.CMHandPlayType.HandPlayFour);

            if (arrIdexs!= null)
            {
                return arrIdexs;
            }
        }
        return null;
    }

    public static ArrayList<Integer> getPossibleFiveCardsHints(ArrayList<CMCard> handCards, ArrayList<CMCard> tableCards)
    {
        // First get sets of possible sets of 5 from hands cards.
        // store all hands as per their types.
        ArrayList<CMCard> tableCardVector = new ArrayList<CMCard>();


        for (int i =0; i<tableCards.size(); i++)
        {
            CMCard card = tableCards.get(i);
            tableCardVector.add(card);
        }


        CardsConstants.CMHandType tblType = checkFiveCardPattern(tableCardVector);

        ArrayList<CMCard> arrHands = new ArrayList<CMCard>();
        arrHands.addAll(handCards);
        arrHands = sortByRankWithA(arrHands);

        ArrayList<CMCard> arrTbl = new ArrayList<CMCard>();
        arrTbl.addAll(tableCards);
        arrTbl = sortByRankWithA(arrTbl);


        // getting valid sets of pattern from user's card

        HashMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>> dicKindPatterns = new HashMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>>();


        for (int i=0; i<arrHands.size(); i++)
        {
            CMCard card1 = (CMCard) arrHands.get(i);
            for (int j=i+1; j<arrHands.size(); j++)
            {
                CMCard card2 = (CMCard) arrHands.get(j);
                for (int k=j+1; k<arrHands.size(); k++)
                {
                    CMCard card3 = (CMCard) arrHands.get(k);
                    for (int l=k+1; l<arrHands.size(); l++)
                    {
                        CMCard card4 = (CMCard) arrHands.get(l);

                        for (int m=l+1; m<arrHands.size(); m++)
                        {
                            CMCard card5 = (CMCard) arrHands.get(m);

                            // Vector set of cards.
                            ArrayList<CMCard> cardVec = new ArrayList<CMCard>();
                            cardVec.add(card1);
                            cardVec.add(card2);
                            cardVec.add(card3);
                            cardVec.add(card4);
                            cardVec.add(card5);

                            CardsConstants.CMHandType userType = checkFiveCardPattern(cardVec);

                            if (userType!= CardsConstants.CMHandType.HandTypeUnkown && userType.getHandValue() >= tblType.getHandValue())
                            {
                                // user card type is known and is better then tbltype
                                ArrayList<CMCard> arr = new ArrayList<CMCard>();
                                arr.add(card1);
                                arr.add(card2);
                                arr.add(card3);
                                arr.add(card4);
                                arr.add(card5);


                                arr = sortByRankWithA(arr); // sorting possible set of cards here
                                ArrayList<ArrayList<CMCard>> arrType = dicKindPatterns.get(userType);
                                if (arrType==null || arrType.size() == 0)
                                {
                                    // Setting Type of Card Sets Array
                                    arrType = new ArrayList<>();
                                    dicKindPatterns.put(userType, arrType);

                                }
                                arrType.add(arr);

                            }
                        }
                    }
                }
            }
        }



        Map<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>> treeMap = new TreeMap<CardsConstants.CMHandType, ArrayList<ArrayList<CMCard>>>(dicKindPatterns);
        Set<CardsConstants.CMHandType> allKeys1 = treeMap.keySet();

        ArrayList<CardsConstants.CMHandType> allKeys = new ArrayList<>();

        Object[] array = allKeys1.toArray();

        for(int i = 0; i < array.length; i++){

            allKeys.add((CardsConstants.CMHandType)array[i]);

        }


        if (allKeys!=null && allKeys.size() > 0)
        {
            // cards exist get and find better;
            ArrayList<Integer> arrIdexs = checkCardSuitsForHint(handCards, arrTbl, allKeys, tblType, dicKindPatterns, CardsConstants.CMHandPlayType.HandPlayFive);

            if (arrIdexs!=null)
            {
                return arrIdexs;
            }
        }

        return null;
    }


    // ****************************Methods For getBestCardHands*************************




    // ****************************Methods For getBestCardHandsLowest*************************

    public static ArrayList<Integer> getBestOneCardHandsLowest(ArrayList<CMCard> handCards)
    {
        CMCard card = null;
        int index = -1;
        boolean isAceFound = false;
        ArrayList<CMCard> sortNewHands = new ArrayList<CMCard>();
        sortNewHands.addAll(handCards);


//    sortByRank(sortNewHands);
        sortNewHands = sortByRankWithA(sortNewHands);
//    sortBySuitsAndRank(sortNewHands);


     
        for (int j =sortNewHands.size()-1; j>0; j--)
        {
            CMCard newCard = (CMCard) sortNewHands.get(j);
            if (newCard.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
            {
                if (card != null)
                {
                    if (newCard.getRank()== CardsConstants.CardRanks.ACE.getCardValue() && newCard.getSuit() > card.getSuit())
                    {
                        card = newCard;
                        index = j;
                        isAceFound  = true;
                    }
                }
                else
                {
                    card = newCard;
                    index = j;
                    isAceFound = true;
                }
            }
            else
            {
                break;
            }
        }
        if (!isAceFound)
        {
            index = 0;
        }
        if (index==-1)
        {
            return null;
        }

        index = handCards.indexOf(sortNewHands.get(sortNewHands.size() - 1));
        ArrayList<Integer> arrIndex = new ArrayList<Integer>();
        arrIndex.add(index);
        return arrIndex;
    }
    
    
    public static ArrayList<Integer> getBestFiveCardHandsLowest(ArrayList<CMCard> handCards, int typeValue)
    {
        ArrayList<Integer> hintArray = null;
        ArrayList<CMCard> arrNewSortHand = new ArrayList<CMCard>();
        arrNewSortHand.addAll(handCards);
        arrNewSortHand = sortByRank(arrNewSortHand);
        
        
        hintArray = RoyalFlushHandsHint(arrNewSortHand, 5);
//        System.out.println("RoyalFlushHandsHint : " + hintArray);

        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandRoyalFlush5.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }
            return hintArray;
        }


        hintArray = StraightFlushHandsHint(arrNewSortHand, 5);
        System.out.println("StraightFlushHandsHint : " + hintArray);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandStraightFlush5.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }

        hintArray = KindOfHandHint(arrNewSortHand, 5, true);
//        System.out.println("KindOfHandHint : " + hintArray);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandKindOf4One.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }
        hintArray = FullHouseHandHint(arrNewSortHand);
        System.out.println("FullHouseHandHint : " + hintArray);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandFullHouse.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }

        hintArray = FlushHandHint(arrNewSortHand, 5);
//        System.out.println("FlushHandHint : " + hintArray);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandFlush5.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }

        hintArray = StraightHandHint(arrNewSortHand, 5);
//        System.out.println("StraightHandHint : " + hintArray);

        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandStraight5.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }
        return null;
    }

        
        

    public static ArrayList<Integer> getBestFourCardHandsLowest(ArrayList<CMCard> handCards, int typeValue)
    {
        ArrayList<Integer> hintArray = null;

        ArrayList<CMCard> arrNewSortHand = new ArrayList<CMCard>();
        arrNewSortHand.addAll(handCards);

        arrNewSortHand = sortByRank(arrNewSortHand);


        hintArray = RoyalFlushHandsHint(arrNewSortHand, 4);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandRoyalFlush4.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }

        hintArray = StraightFlushHandsHint(arrNewSortHand, 4);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandStraightFlush4.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);

            }

            return hintArray;
        }

        hintArray = KindOfHandHint(arrNewSortHand, 4, false);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandKindOf4.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {

                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }

        hintArray = FlushHandHint(arrNewSortHand, 4);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandFlush4.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }

        hintArray = StraightHandHint(arrNewSortHand, 4);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandStraight4.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {

                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);

            }

            return hintArray;
        }

        hintArray = TwoPairsHandHint(arrNewSortHand);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandTwoPair.getHandValue();

            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                int value = hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i, index);
            }

            return hintArray;
        }
        return null;
    }

    public static ArrayList<Integer> getBestTwoCardHandsLowest(ArrayList<CMCard> handCards)
        {
            int index1=-1;
            int index2=-1;
            CMCard prevfirst,prevSecond;
            prevfirst = prevSecond = null;
            
        
        
        ArrayList<CMCard> arrNewSortHand = new ArrayList<CMCard>();
        arrNewSortHand.addAll(handCards);
        //    sortByRank(arrNewSortHand);
        arrNewSortHand = sortByRankWithA(arrNewSortHand);


        boolean isAceFound = false;

        if (!isAceFound)
        {
            boolean foundPair = false;
            for (int i=arrNewSortHand.size() - 1; i>=0;i--)
            {
                CMCard firstCard = (CMCard) arrNewSortHand.get(i);

                for (int j = i-1; j>=0; j--)
                {
                    CMCard secondCard = (CMCard) arrNewSortHand.get(j);
                    if (secondCard.getRank()==firstCard.getRank())
                    {
                        index1 = i;
                        index2 = j;
                        foundPair = true;
                        break;
                    }
                    else
                    {
                        break;
                    }
                }
                if (foundPair)
                {
                    break;
                }
            }
        }

        if (index1 == -1 || index2 == -1)
        {
            return null;
        }

        ArrayList<Integer> arrIndex = new ArrayList<Integer>();
        arrIndex.add(handCards.indexOf(arrNewSortHand.get(index1)));
        arrIndex.add(handCards.indexOf(arrNewSortHand.get(index2)));
        return arrIndex;
    }

    public static ArrayList<Integer> getBestThreeCardHandsLowest(ArrayList<CMCard> handCards, int typeValue)
    {
        ArrayList<Integer> hintArray = null;

        ArrayList<CMCard> arrNewSortHand =new ArrayList<CMCard>();
        arrNewSortHand.addAll(handCards);

        arrNewSortHand = sortByRank(arrNewSortHand);
//    sortByRankWithA(arrNewSortHand);

//        CCLOG("Checking for %d RoyalFlush",3);
        hintArray = RoyalFlushHandsHint(arrNewSortHand,3);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandRoyalFlush3.getHandValue();
            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                Integer value = (Integer) hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i,index);
            }
            return hintArray;
        }

//        CCLOG("Checking for %d StraightFlush",3);
        hintArray = StraightFlushHandsHint(arrNewSortHand, 3);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandStraightFlush3.getHandValue();
            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                Integer value = (Integer) hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i,index);
            }
            return hintArray;
        }

//        CCLOG("Checking for %d kind of",3);
        hintArray = KindOfHandHint(arrNewSortHand, 3 , false);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandKindOf3.getHandValue();
            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                Integer value = (Integer) hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i,index);
            }
            return hintArray;
        }

//        CCLOG("Checking Flush for %d",3);
        hintArray = FlushHandHint(arrNewSortHand, 3);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandFlush3.getHandValue();
            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                Integer value = (Integer) hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i,index);
            }
            return hintArray;
        }

//        CCLOG("Checking Straight for %d",3);
        hintArray = StraightHandHint(arrNewSortHand, 3);
        if (hintArray != null)
        {
            typeValue = CardsConstants.CMHandType.HandStraight3.getHandValue();
            // change hint array index according to old array and return
            for (int i =0; i<hintArray.size(); i++)
            {
                Integer value = (Integer) hintArray.get(i);
                int index = handCards.indexOf(arrNewSortHand.get(value));
                hintArray.set(i,index);
            }
            return hintArray;
        }
        return null;
    }





    public  static ArrayList<Integer> KindOfHandHint(ArrayList<CMCard> handsCards , int flushType , boolean isPlusOne)
    {

        HashMap<Integer,ArrayList<CMCard>> cardsDetailsDic = new HashMap<Integer,ArrayList<CMCard>>();
        for(int i =0;i<handsCards.size();i++)
        {
            CMCard card = (CMCard) handsCards.get(i);

            if (cardsDetailsDic.get(card.getRank()) != null)
            {
                ArrayList<CMCard> arr = (ArrayList<CMCard>) cardsDetailsDic.get(card.getRank());
                arr.add(card);
            }else
            {
                ArrayList<CMCard> arr = new ArrayList<CMCard>();
                arr.add(card);
                cardsDetailsDic.put(card.getRank(),arr);
            }
        }
        ArrayList<CMCard> arrHighest = null;
        int cardRank = -1;
        Map<Integer, ArrayList<CMCard>> treeMap = new TreeMap<Integer, ArrayList<CMCard>>(cardsDetailsDic);
        Set<Integer> allKeys1 = treeMap.keySet();

        ArrayList<Integer> allKeys = new ArrayList<Integer>();

        Object[] array = allKeys1.toArray();

        for(int i = 0; i < array.length; i++){

            allKeys.add((Integer)array[i]);

        }


        for (int i=0; i < allKeys.size(); i++)
        {

            Integer key = allKeys.get(i);
            ArrayList<CMCard> arrFirst = (ArrayList<CMCard>) cardsDetailsDic.get(key);

            if (arrFirst.size() == ((flushType)-(isPlusOne?1:0)))
            {
                if (arrHighest != null)
                {
                    CMCard firstCard = (CMCard) arrFirst.get(0);
                    CMCard secondCard = (CMCard) arrHighest.get(0);
                    if (secondCard.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        arrHighest = arrHighest;
                        cardRank = secondCard.getRank();
                    }
                    else if (firstCard.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                    {
                        arrHighest = arrFirst;
                        cardRank = firstCard.getRank();
                    }
                    else if (secondCard.getRank() > firstCard.getRank())
                    {
                        arrHighest = arrHighest;
                        cardRank = secondCard.getRank();
                    }
                }
                else{
                    arrHighest = arrFirst;
                }
                cardRank = ((CMCard)arrFirst.get(0)).getRank();
                for (int j=i+1; j<allKeys.size(); j++)
                {
                    Integer keyInLoop = (Integer) allKeys.get(j);
                    ArrayList<CMCard> arrSecond= (ArrayList<CMCard>) cardsDetailsDic.get(keyInLoop);

                    if (arrSecond.size() == flushType)
                    {
                        CMCard firstCard = (CMCard) arrFirst.get(0);
                        CMCard secondCard = (CMCard) arrSecond.get(0);

                        if (secondCard.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                        {
                            arrHighest = arrSecond;
                            cardRank = secondCard.getRank();
                        }
                        else if (firstCard.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                        {
                            arrHighest = arrFirst;
                            cardRank = firstCard.getRank();
                        }
                        else if (secondCard.getRank() > firstCard.getRank())
                        {
                            arrHighest = arrSecond;
                            cardRank = secondCard.getRank();
                        }
                    }
                }
            }
        }

        if (arrHighest!=null && arrHighest.size() > 0 && cardRank!=-1)
        {
            if (isPlusOne)
            {
                // five card and isPlusOne true,

                CMCard ca = (CMCard) arrHighest.get(0);
                CMCard card2 = null;

                for (int i=0; i<allKeys.size(); i++)
                {
                    Integer keyInLoop = (Integer) allKeys.get(i);
                    ArrayList<CMCard> arrFirst = (ArrayList<CMCard>) cardsDetailsDic.get(keyInLoop);

                    if (arrFirst == arrHighest)
                    {
                        continue;
                    }
                    if (card2 != null)
                    {
                        CMCard cards = (CMCard) arrFirst.get(0);

                        if (cards.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                        {
                            card2 = cards;
                        }
                        else if (cards.getRank() > card2.getRank())
                        {
                            card2 = cards;
                        }
                    }
                    else
                    {
                        card2 = (CMCard) arrFirst.get(0);
                    }

                }
                arrHighest.add(card2);
            }

            ArrayList<Integer> arrIndex = new ArrayList<Integer>();

            for (int j = 0; j < arrHighest.size(); j++)
            {
                CMCard card = (CMCard) arrHighest.get(j);
                int index = handsCards.indexOf(card);
                arrIndex.add(index);
            }
            return arrIndex;
        }

        return null;
    }


    public static  ArrayList<Integer> FlushHandHint(ArrayList<CMCard> handsCard, int flushType)
    {
        HashMap<Integer,ArrayList<CMCard>> cardsDetailsDic = new HashMap<Integer,ArrayList<CMCard>>();
        cardsDetailsDic.put(CardsConstants.Suit.HEARTS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.DIAMONDS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.CLUBS.getSuitsValue(), new ArrayList<CMCard>());
        cardsDetailsDic.put(CardsConstants.Suit.SPADES.getSuitsValue(), new ArrayList<CMCard>());

        for (int i = 0; i< handsCard.size(); i++)
        {
            CMCard cardDetails = (CMCard) handsCard.get(i);
            ArrayList<CMCard> arr = (ArrayList<CMCard>)  cardsDetailsDic.get(cardDetails.getSuit());
            arr.add(cardDetails);

        }
        ArrayList<CMCard> arrHighestCardsDetails = new ArrayList<CMCard>();
        //    CardColor lastColor;
        Map<Integer, ArrayList<CMCard>> treeMap = new TreeMap<Integer, ArrayList<CMCard>>(cardsDetailsDic);
        Set<Integer> allKeys1 = treeMap.keySet();

        ArrayList<Integer> arrAllKeys = new ArrayList<Integer>();

        Object[] array = allKeys1.toArray();

        for(int i = 0; i < array.length; i++){

            arrAllKeys.add((Integer)array[i]);

        }

        for (int i=0; i<arrAllKeys.size(); i++)
        {
            Integer key = (Integer) arrAllKeys.get(i);
            ArrayList<CMCard> arr = (ArrayList<CMCard>) cardsDetailsDic.get(key);


            for (int j = arr.size()-1; j>=0; j--)
            {
                CMCard cards = (CMCard) arr.get(j);

                if (cards.getRank() == CardsConstants.CardRanks.ACE.getCardValue())
                {
                    arr.remove(j);
                    arr.add(0,cards);
                }
                else
                    break;
            }
        }



        // check valid arr for current type

        ArrayList<CMCard> arrHighFlush = null;


        for (int i = 0; i < arrAllKeys.size(); i++)
        {
            Integer key = (Integer) arrAllKeys.get(i);
            ArrayList<CMCard> arrCards = (ArrayList<CMCard>) cardsDetailsDic.get(key);

            if (arrCards.size() >= flushType)
            {
                if (arrHighFlush != null)
                {
                    CMCard first = (CMCard) arrCards.get(0);
                    CMCard second = (CMCard) arrHighFlush.get(0);

                    if (first.getSuit() > second.getSuit())
                    {
                        arrHighFlush = arrCards;

                    }
                }
                else
                    arrHighFlush = arrCards;
                for (int j =i+1; j<arrAllKeys.size(); j++)
                {
                    Integer secKey = (Integer) arrAllKeys.get(j);
                    ArrayList<CMCard> arrSecondCards = (ArrayList<CMCard>) cardsDetailsDic.get(secKey);


                    if (arrSecondCards.size() >= flushType)
                    {
                        // check suits

                        CMCard first = (CMCard) arrCards.get(0);
                        CMCard second = (CMCard) arrSecondCards.get(0);

                        if (second.getSuit() > first.getSuit())
                        {
                            arrHighFlush = arrSecondCards;
                        }
                    }
                }
            }
        }

        if (arrHighFlush != null && arrHighFlush.size() > 0)
        {
            ArrayList<Integer> arrIndex = new ArrayList<Integer>();
            for (int j = 0; j < flushType/* arrHighFlush->count()*/; j++)
            {
                CMCard card = (CMCard) arrHighFlush.get(j);
                int index = handsCard.indexOf(card);
                arrIndex.add(index);

            }
            return arrIndex;
        }

        return null;
    }
    public static ArrayList<Integer> getBestTwoCardHands(ArrayList<CMCard> handCards)
    {
        int index1=-1;
        int index2=-1;
        CMCard prevfirst,prevSecond;
        prevfirst = prevSecond = null;


        ArrayList<CMCard> arrNewSortHand = new ArrayList<CMCard>();
        arrNewSortHand.addAll(handCards);
//    sortByRank(arrNewSortHand);
        arrNewSortHand = sortByRank(arrNewSortHand);



        boolean isAceFound = false;


        if (!isAceFound)
        {
            boolean foundPair = false;
            for (int i=arrNewSortHand.size() - 1; i>=0;i--)
            {
                CMCard firstCard = (CMCard) arrNewSortHand.get(i);

                for (int j = i-1; j>=0; j--)
                {
                    CMCard secondCard = (CMCard) arrNewSortHand.get(j);
                    if (secondCard.getRank()==firstCard.getRank())
                    {
                        index1 = i;
                        index2 = j;
                        foundPair = true;
                        break;
                    }
                    else
                    {
                        break;
                    }
                }
                if (foundPair)
                {
                    break;
                }
            }
        }

        if (index1 == -1 || index2 == -1)
        {
            return null;
        }


        ArrayList<Integer> arrIndex = new ArrayList<Integer>();
        arrIndex.add(handCards.indexOf(arrNewSortHand.get(index1)));
        arrIndex.add(handCards.indexOf(arrNewSortHand.get(index2)));

        System.out.println("getBestTwoCardHands : " + arrIndex);

        return arrIndex;
    }

}
