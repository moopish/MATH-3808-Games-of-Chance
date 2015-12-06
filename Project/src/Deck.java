import java.util.ArrayList;
import java.util.Collections;

/**
 * class representing a deck of cards. Can shuffle or deal and has various get methods. creates instances of the card class
 */
public class Deck
{
  public static int cardsPerDeck = 52;
  
  private int deckSize;
  private ArrayList<Card> deck;
  private ArrayList<Card> cardsDealt;
  
  /**
   *Constructor method for a Deck.
   *pre: none
   *post: initialises instance variables. Creates 52 different cards, 13 per suit.
   */
  public Deck()
  {
    deckSize = cardsPerDeck;
    deck = new ArrayList<Card>(cardsPerDeck);
    cardsDealt = new ArrayList<Card>(cardsPerDeck * 2);
    
    char suit = Card.CLUBS; // Initialises the card to a suit
    for(byte suitCount = 0; suitCount < 4; suitCount++)
    {
      switch(suitCount) // Statement that determines the suit of the cards
      {
        case 0: suit = Card.CLUBS;
        break;
        
        case 1: suit = Card.DIAMONDS;
        break;
        
        case 2: suit = Card.HEARTS;
        break;
        
        case 3: suit = Card.SPADES;
        break;
      }
      
      for(byte count = 2; count < 15; count++) //Adds all 13 cards of the current suit
      {
        deck.add(new Card(count, suit));
      }
    }
    
    Collections.shuffle(deck);
  }
  
  /**
   * Overloaded constructor method for the deck, allowing for multiple decks
   * pre: NumOfDecks > 0
   * post: initialises instance variables. Creates 52 different cards, per deck
   * @param NumOfDecks the number of decks
   */
  public Deck(int NumOfDecks)
  {
    deckSize = cardsPerDeck * NumOfDecks;
    deck = new ArrayList<Card>(cardsPerDeck);
    cardsDealt = new ArrayList<Card>(cardsPerDeck * 2);
    
    char suit = Card.CLUBS;
    for(byte decks = 0; decks < NumOfDecks; decks++)
    {
      for(byte suitCount = 0; suitCount < 4; suitCount++)
      {
        switch(suitCount)
        {
          case 0: suit = Card.CLUBS;
          break;
          
          case 1: suit = Card.DIAMONDS;
          break;
          
          case 2: suit = Card.HEARTS;
          break;
          
          case 3: suit = Card.SPADES;
          break;
        }
        
        for(byte count = 2; count < 15; count++)
        {
          deck.add(new Card(count, suit));
        }
      }
    }
    
    Collections.shuffle(deck);
  }
  
  // NOTE: For simulation purposes; does not modify deck.
  public ArrayList<Card> sample(int numCards)
  {
    Collections.shuffle(deck);
    return new ArrayList<Card>(deck.subList(0, numCards));
  }
  
  /**
   *Deals cards from deck equal to numCards
   *pre: numCards is positive
   *post: ArrayList with dealt cards is returned
   */
  public ArrayList<Card> deal(int numCards)
  {
    Collections.shuffle(deck);
    
    int toIndex = deck.size();
    int fromIndex = toIndex - numCards;
    
    ArrayList<Card> hand = new ArrayList<Card>(deck.subList(fromIndex, toIndex));
    cardsDealt.addAll(hand);
    deck.removeAll(hand);
    
    return hand;
  }
  
  /**
   *Deals a card from the Deck
   *pre: deck is non empty
   *post: return a random card from the deck
   */ 
  public Card deal()
  {
    Collections.shuffle(deck);
    
    if(!deck.isEmpty()) {
      Card card = deck.remove(deck.size() - 1);
      cardsDealt.add(card);
      return card;
    }
    
    return null;
  }
  
  // NOTE: Restores the deck; avoids having to create a new deck.
  public boolean restore()
  {
    boolean result = deck.addAll(cardsDealt);
    cardsDealt.clear();
    return result;
  }
  
  /**
   * Gets the number of cards remaining in the current deck.
   * pre: none
   * post: returns the size of the current deck
   */
  public int getCurrentDeckSize()
  {
    return deck.size();
  }
  
  /**
   * Gets the number of cards the deck had when it was initialised.
   * pre: none
   * post: returns the number of cards the deck had when initialised.
   */
  public int getDeckSize()
  {
    return deckSize;
  }
}