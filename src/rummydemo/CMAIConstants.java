package rummydemo;

/**
 * Created by kapilbindal on 29/03/16.
 */
public class CMAIConstants {

            public static final int kAddSecondAiAfterTimeMin  =  60;  //time in sec



            public static final int kAddSecondAiAfterTimeMax  = 240; //time in sec

            public static final int kStartAiAfterPingsMax = 14;
            public static final int kStartAiAfterPingsMin  = 8;

            public static final int kReconnectAIAfterMinMax = 16;
            public static final int kReconnectAIAfterMinMin = 12;

            public static final int kMatchPlayedFactorMax =  120;
            public static final int kMatchPlayedFactorMin  = 50;

            public static final int kWinPercentageNoviceMax = 40;
            public static final int kWinPercentageNoviceMin = 15;

            public static final int kWinPercentageIntermendiateMax = 70;
            public static final int kWinPercentageIntermendiateMin = 41;

            public static final int kWinPercentageExpertMax = 90;
            public static final int kWinPercentageExpertMin = 71;


            // #pragma mark -----Play card probability----

//Expert
            public static final int kExpertStartWithFiveCards = 40;
            public static final int kExpertStartWithFourCards = 50;


//Intermediate
            public static final int kIntermediateStartWithFiveCards = 15;
            public static final int kIntermediateStartWithFourCards = 30;
            public static final int kIntermediateStartWithThreeCards = 50;


//Novice
            public static final int kNoviceStartWithFiveCards = 5;
            public static final int kNoviceStartWithFourCards = 10;
            public static final int kNoviceStartWithThreeCards = 25;
            public static final int kNoviceStartWithTwoCards = 50;

//            #pragma mark -----Betting amount probability----

//Expert
            public static final int kExpertFiveCardAmountMax = 70;
            public static final int kExpertFiveCardAmountMin = 40;

            public static final int kExpertFourCardAmountMax = 60;
            public static final int kExpertFourCardAmountMin = 30;

            public static final int kExpertThreeCardAmountMax = 50;
            public static final int kExpertThreeCardAmountMin = 25;

            public static final int kExpertTwoCardAmountMax = 35;
            public static final int kExpertTwoCardAmountMin = 15;

            public static final int kExpertOneCardAmountMax = 20;
            public static final int kExpertOneCardAmountMin = 05;

            public static final int kExpertCheckAmountMax = 30;
            public static final int kExpertCheckAmountMin = 15;

//Intermediate
            public static final int kIntermediateFiveCardAmountMax = 80;
            public static final int kIntermediateFiveCardAmountMin = 55;

            public static final int kIntermediateFourCardAmountMax = 65;
            public static final int kIntermediateFourCardAmountMin = 40;

            public static final int kIntermediateThreeCardAmountMax = 55;
            public static final int kIntermediateThreeCardAmountMin = 30;

            public static final int kIntermediateTwoCardAmountMax = 40;
            public static final int kIntermediateTwoCardAmountMin = 15;

            public static final int kIntermediateOneCardAmountMax = 25;
            public static final int kIntermediateOneCardAmountMin = 10;

            public static final int kIntermediateCheckAmountMax = 25;
            public static final int kIntermediateCheckAmountMin = 10;


//Novice
            public static final int kNoviceFiveCardAmountMax = 80;
            public static final int kNoviceFiveCardAmountMin = 55;

            public static final int kNoviceFourCardAmountMax = 65;
            public static final int kNoviceFourCardAmountMin = 40;

            public static final int kNoviceThreeCardAmountMax = 55;
            public static final int kNoviceThreeCardAmountMin = 30;

            public static final int kNoviceTwoCardAmountMax = 40;
            public static final int kNoviceTwoCardAmountMin = 15;

            public static final int kNoviceOneCardAmountMax = 25;
            public static final int kNoviceOneCardAmountMin = 10;

            public static final int kNoviceCheckAmountMax = 20;
            public static final int kNoviceCheckAmountMin = 5;


    //Intervals

    public static final float TURNINTERVAL = 0.15f;

    public static final String kTypeNormal = "NORMAL";
    public static final String  kTypeFast = "FAST";
    public static final String  kTypeExpert = "EXPERT";

    public static final int  NormalDuration = 20;
    public static final int  FastDuration = 15;
    public static final int  ExpertDuration = 10;

}
