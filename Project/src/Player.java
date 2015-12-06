import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 *class representing a player in a game of video poker
 */
public class Player
{
    private ArrayList<Card> hand;  //the players hand
    private int money;  //the amount of money that the player has
    private int net;    //current total net gain/loss of player
    private int rank; //rank of hand

    /**
     *Constructor method of the Player class.
     *pre: none
     *post: initialises instance variables.
     */
    public Player()
    {
        this.money = 0;
        this.net = 0;
        this.hand = new ArrayList<Card>(5);
        this.rank = -1;
    }

    public ArrayList<Card> getHand()
    {
        return hand;
    }

    public void setHand(ArrayList<Card> cards)
    {
        hand.clear();
        hand.addAll(cards);
    }

    public Card getCard(int index)
    {
        return hand.get(index);
    }

    public Card setCard(int index, Card card)
    {
        return hand.set(index, card);
    }

    public void setCards(boolean[] cardsHeld, ArrayList<Card> cards)
    {
        for(int i = 0; i < cardsHeld.length; ++i)
            if(!cardsHeld[i])
                hand.set(i, cards.remove(cards.size() - 1));
    }

    public boolean addCard(Card card)
    {
        return hand.add(card);
    }

    public boolean addCards(ArrayList<Card> cards)
    {
        return hand.addAll(cards);
    }

    public Card removeCard(int index)
    {
        return hand.remove(index);
    }

    public boolean removeCards(ArrayList<Card> cards)
    {
        return hand.removeAll(cards);
    }

    public void discard()
    {
        hand.clear();
    }

    public int handSize()
    {
        return hand.size();
    }

    public int getHandRank()
    {
        rankHand();
        return rank;
    }

     /**
     *Gets the rank of players poker hand
     *pre: hand contains 5 cards
     *post: hand rank is determined 
     */
    private void rankHand()
    {
        int highPair = 0;
        boolean straight = true;
        boolean flush = true;
        boolean pair = false;
        boolean twoPair = false;
        boolean three = false;
        boolean four = false;

        boolean pairDone = true;

        ArrayList<Card> sortedHand = sortHand();

        for(int i = 1; i < 5; i++)
        {
            if(flush && sortedHand.get(i).getSuit() != sortedHand.get(i-1).getSuit())
            {
                flush = false;
            }
            if(straight &&
               (sortedHand.get(i).getRank() - sortedHand.get(i-1).getRank() != 1
                && sortedHand.get(i).getRank() - sortedHand.get(i-1).getRank() != -12))
            {
                straight = false;
            }
            if(!straight && !four && sortedHand.get(i).getRank() == sortedHand.get(i-1).getRank())
            {
                if(pairDone)
                {
                    if(pair)
                    {
                        twoPair = true;
                    }
                    else
                    {
                        pair = true;
                        highPair = sortedHand.get(i).getRank();
                    }
                }
                else
                {
                    if(twoPair)
                    {
                        twoPair = false;
                        three = true;
                    }
                    else if(three)
                    {
                        three = false;
                        four = true;
                    }
                    else
                    {
                        pair = false;
                        three = true;
                    }
                }
                pairDone = false;
            }
            else
            {
                pairDone = true;
            }
        }

        if(straight && flush && sortedHand.get(4).getRank() == 14)
            this.rank =9;
        else if(straight && flush)
            this.rank =8;
        else if(four)
            this.rank=7;
        else if(three && pair)
            this.rank=6;
        else if(flush)
            this.rank=5;
        else if(straight)
            this.rank=4;
        else if(three)
            this.rank=3;
        else if(twoPair)
            this.rank=2;
        else if(pair && highPair >= 11)
            this.rank=1;
        else
            this.rank=0;
    }

    /**
     *Sorts the players hand
     *pre: hand contains 5 cards
     *post: returns a copy of the players hand that has been sorted
     */
    private ArrayList<Card> sortHand() {
        ArrayList<Card> result = new ArrayList<Card>(hand);
        Card temp;

        //sort based on rank
        for(int current = 0; current < 4; current++)
        {
            for(int i = current + 1; i < 5; i++)
            {
                if(result.get(current).getRank() > result.get(i).getRank())
                {
                    temp = result.get(current);
                    result.set(current, result.get(i));
                    result.set(i, temp);
                }
            }
        }

        //check for low straight with ace
        if(result.get(0).getRank() == 2 && result.get(1).getRank() == 3 &&
           result.get(2).getRank() == 4 && result.get(3).getRank() == 5 &&
           result.get(4).getRank() == 14)
        {
            result.add(0, result.get(4));
            result.remove(5);
        }

        return result;
    }

    /**
     *Gets the images associated with the cards in hand.
     *pre: hand contains 5 cards
     *post: returns an array of ImageIcon corresponding to each card in the hand.
     */
    public ImageIcon[] getCardImages()
    {
        ImageIcon[] cards;
        int count = 0;
        cards = new ImageIcon[5];
        for (Card i : this.hand)
        {
            cards[count] = i.getCardImage();
            count++;
        }
        return cards;

    }

    /**
     *Gets the number of cards in a player's hand.
     *pre: none
     *post: returns the number of cards in  hand
     */
    public int getSizeOfHand()
    {
        return hand.size();
    }

    /**
     *Adds a specified amount of money to the player's bank.
     *pre: value >= 0
     *post: value is added to the player's money.
     */
    public void addMoney(int value)
    {
        this.money += value;
    }

    /**
     *Removes a specified amount of money from the player's bank.
     *pre: value >= 0
     *post: retuens bet amount or -1 if insufficient funds.
     */
    public int makeBet (int value)
    {
        if(value <= this.money)
        {
            this.money -= value;
            this.net -= value;
            return value;
        }
        else
        {
            return -1;
        }
    }

    /**
     *Adds a specified amount of money to the player's bank as winning.
     *pre: value >= 0
     *post: value is added to the player's money.
     */
    public void win(int value)
    {
        this.money += value;
        this.net += value;
    }

    /**
     *Show the current net gain/loss of player
     *pre: none
     *post: returns total  gain/loss of player
     */
    public int getNet()
    {
        return this.net;
    }

    /**
     *Shows the amount of money currently left in the player's bank.
     *pre: none
     *post: returns the amount of money the player has.
     */
    public int currentBank()
    {
        return this.money;
    }
}