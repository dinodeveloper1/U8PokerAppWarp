package rummydemo;

import java.util.Random;

/**
 * Created by kapilbindal on 29/03/16.
 */
public class CMUtility {

    static public int randomNumberBetween(int minValue, int maxValue){
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(maxValue - minValue);
        index += minValue;

        return index;
    }

    static public float getPercentage(int value, int maxValue){
        if(maxValue == 0){
            return 0;
        }
        return (value/maxValue * 100);

    }

    static public String getRandomName(){

        int size = randomNumberBetween(2,5);

        String val = "abkuecukidbefkghuiebeukgaijklimbukeneiabokbkpgeqiriustueagueuviwixeyzabkuecukidbefkghuiebeukgaijklimbukeneiabokbkpgeqiriustueagueuviwixeyzabkuecukidbefkghuiebeukgaijklimbukeneiabokbkpgeqiriustueagueuviwixeyz";
        int sizeStr = val.length() - size -2;

        int index = randomNumberBetween(1,sizeStr);

        String subStr = val.substring(index, index + size);

        int randomNo = CMUtility.randomNumberBetween(1000,9999);
        subStr = subStr.concat(randomNo + "");

        return subStr;

    }

}
