import javax.swing.ImageIcon;

/**
 *Class that represents a playing card has methods representing the various actions that can be made with a card
 */
public class Card
{
    private final int rank; // rank of card 2-14(ace is 14)
    private final char suit;  //suit of card

    //The card image
    private final ImageIcon pic;


    //The possible suits
    public final static char HEARTS = 'h';
    public final static char CLUBS = 'c';
    public final static char DIAMONDS = 'd';
    public final static char SPADES = 's';

    /**
     *Constructor method for the Card class.
     *pre: none
     *post: variables are initialised, card is assigned an image indicative of its value.
     */
    public Card(int rank, char suit)
    {
        this.rank = rank;
        this.suit = suit;
        this.pic = new ImageIcon(this.getClass().getResource("Cards/" + suit + rank + ".png"));  //Find the image of the card
    }


    /**
     *Gets the image associated with a card depending on whether or not the card is face-up or face-down.
     *pre: none
     *post: returns the image of the card or the card face-down image
     */
    public ImageIcon getCardImage()
    {
        return this.pic;
    }

    /**
     *Gets the rank of the card
     *pre: none
     *post: returns rank
     */
    public int getRank()
    {
        return this.rank;
    }

    /**
     *Gets the suit of the card
     *pre: none
     *post: returns suit
     */
    public char getSuit()
    {
        return this.suit;
    }

    /**
     *Overloaded equals method. Compares another object with this card to see if they are the same.
     *pre: obj is a valid Object
     *post: returns true if obj is the exact same as this card, otherwise false
     */
    @Override
    public boolean equals(Object obj)
    {
        Card a = (Card)obj;
        return a.rank == this.rank && a.suit == this.suit;
    }

    @Override
    public String toString() {
        return Integer.toString(rank) + suit;
    }

}