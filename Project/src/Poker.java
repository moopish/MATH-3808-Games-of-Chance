import java.util.ArrayList;

public class Poker
{
    public static String[] handNames = { "Nothing", "Jacks or better", "Two pair",
                                         "Three of a kind", "Straight", "Flush",
                                         "Full house", "Four of a kind",
                                         "Straight flush", "Royal flush" };

    // NOTE: To qualify for the 800 to 1 payout on a royal flush, the player
    // must bet five coins.
    public static int[] payoutTable = { 0, 1, 2, 3, 4, 6, 9, 25, 50, 800 };
    public static int[] preDrawFrequencies = { 2062860, 337920, 123552, 54912, 10200,
                                               5108, 3744, 624, 36, 4 };

    public static int numIter = 3500000;
    public static int numPlays = 7;
    public static float winnings = 0;

    public static int numHands = 2598960;
    public static int numRanks = 10;
    public static int handSize = 5;

    private static class HandValue {
        private final ArrayList<Card> hand;
        private final float value;

        private HandValue(ArrayList<Card> hand, float value) {
            this.hand = hand;
            this.value = value;
        }
    }

    public static void main(String[] args)
    {
        numIter = (args.length > 0) ? Integer.parseInt(args[0]) : numIter;
        numPlays = (args.length > 1) ? Integer.parseInt(args[1]) : numPlays;

        Player player = new Player();
        Deck deck = new Deck();

        test(player, deck);
    }

    private static void test(Player player, Deck deck)
    {
        int[] counts = observed(player, deck);
        float[] expectedCounts = expected();

        handDistribution(counts);
        chiSquareTest(counts, expectedCounts);

        for(int i = 0; i < numPlays; ++i)
            playthrough(player, deck);
    }

    private static int[] observed(Player player, Deck deck)
    {
        int[] counts = new int[numRanks];

        for(int i = 0; i < numIter; ++i) {
            player.setHand(deck.sample(handSize));
            counts[player.getHandRank()] += 1;
        }

        return counts;
    }

    private static float[] expected()
    {
        float[] counts = new float[numRanks];

        for(int i = 0; i < numRanks; ++i)
            counts[i] = (float)numIter*preDrawFrequencies[i] / numHands;

        return counts;
    }

    private static void handDistribution(int[] counts)
    {
        System.out.println("\nHand distribution:\n");

        for(int i = numRanks-1; i >= 0; --i)
            System.out.format("%17s: %9.6f %% %n", handNames[i],
                              100.0*counts[i] / numIter);
    }

    private static void chiSquareTest(int[] observed, float[] expected)
    {
        float result = 0;
        float deviation = 0;

        System.out.println("\nChi-square test:");
        System.out.format("%n%27s\t%s\t%s %n", "Observed", "Expected", "Deviation");

        for(int i = numRanks-1; i >= 0; --i) {
            deviation = (float)Math.pow(observed[i] - expected[i], 2) / expected[i];
            result += deviation;
            System.out.format("%17s: %-7d\t%-10.2f\t%-5.3f %n",
                              handNames[i], observed[i], expected[i], deviation);
        }

        System.out.format("%n%46s: %5.3f %n%n", "Total", result);
    }

    private static void playthrough(Player player, Deck deck)
    {
        player.setHand(deck.deal(handSize));

        System.out.format("%17s: ", "Hand dealt");
        printHand(player.getHand());

        HandValue bestChoice = optimalStrategy(player, deck);
        winnings += (bestChoice.value - 1);

        System.out.format("%17s: ", "Optimal strategy");
        printHand(bestChoice.hand);
        System.out.format("%17s: %4.2f (%4.2f) %n%n", "Expected payout",
                          bestChoice.value, winnings);

        deck.restore();
    }

    public static HandValue optimalStrategy(Player player, Deck deck)
    {
        int rank = 0;
        int bestChoice = 0;
        int numChoices = 32;
        int numCardsHeld = 0;
        int sampleSize = 766969;
        float expectedPayout = 0;

        // NOTE: Massage these parameters to adjust the speed/accuracy ratio.
        // Each entry corresponds to the number of cards held. E.g., if 4 cards
        // are held, sample 752 times.
        int[] sampleSizes = { sampleSize, 356730, 64860, 8648, 752, 1 };

        int[][] payoutFrequencies = new int[numChoices][numRanks];
        float[] expectedPayouts = new float[numChoices];

        ArrayList<ArrayList<Card>> cardsHeld = new ArrayList<ArrayList<Card>>(numChoices);
        ArrayList<Card> cardsDrawn;

        for(int i = 0; i < (1 << handSize); ++i) {
            ArrayList<Card> hand = new ArrayList<Card>(handSize);

            for(int j = 0; j < handSize; j++)
                if(((i >> j) & 1) == 1)
                    hand.add(player.getCard(j));

            cardsHeld.add(hand);
        }

        for(int i = 0; i < numChoices; ++i) {
            player.setHand(cardsHeld.get(i));
            numCardsHeld = player.handSize();
            sampleSize = sampleSizes[numCardsHeld];

            for(int j = 0; j < sampleSize; ++j) {
                cardsDrawn = deck.sample(handSize - numCardsHeld);
                player.addCards(cardsDrawn);
                rank = player.getHandRank();
                payoutFrequencies[i][rank] += payoutTable[rank];
                player.removeCards(cardsDrawn);
            }

            for(int k = 0; k < numRanks; ++k)
                expectedPayout += payoutFrequencies[i][k];

            expectedPayouts[i] = expectedPayout / sampleSize;
            expectedPayout = 0;
        }

        for(int i = 0; i < numChoices; ++i)
            if(expectedPayouts[i] > expectedPayouts[bestChoice])
                bestChoice = i;

        return new HandValue(cardsHeld.get(bestChoice), expectedPayouts[bestChoice]);
    }

    public static void printHand(ArrayList<Card> hand) {
        int numCards = hand.size();

        System.out.print("{");

        if(numCards > 0) {
            for(int i = 0; i < numCards-1; ++i)
                System.out.print(hand.get(i) + ", ");

            System.out.print(hand.get(numCards - 1));
        }

        System.out.println("}");
    }
}