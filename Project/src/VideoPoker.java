import javax.swing.ImageIcon;

public class VideoPoker
{
  private Player player;
  private Deck deck;
  private int betPool;
  private int handRank;
  
  private int state; //0 - pre deal(initial state) 
                     //1 - pre mulligan, post deal
                     //2 - pre payback, post mulligan
  
  private static final int PAYBACK[] = {0, 1, 2, 3, 4, 6, 9, 25, 50, 250};
  private static final int MAX_PAYBACK = 4000;
  
  public VideoPoker()
  {
    this.deck = new Deck();
    this.player = new Player();
    this.betPool = 0;
    this.handRank = 0;
    this.state = 0;
     addFunds(100); //start player with 100 money
  }
  
  //original deal returns rank of hand
  public int deal(int bet)
  {
    if(this.state == 0 && this.makeBet(bet))
    {
      this.deck.restore();
      this.player.setHand(this.deck.deal(5));
      this.state = 1;
      this.handRank = this.player.getHandRank();
      return this.handRank;
    }
    else
    {
      return -1;
    }
  }
  
  /**
     *Second deal allowing player to redraw any number of cards in their hand
     *pre: hold is a boolean array of length 5
     *post: deals a new card for every false element in hold array and returns new hand rank
     */
  public int mulligan(boolean[] hold)
  {
    if(this.state == 1 && hold.length == 5)
    {
      this.player.setCards(hold, this.deck.deal(5));
      this.handRank = this.player.getHandRank();
      this.state = 2;
      return this.handRank;
    }
    else
    {
      return -1;
    }
  }
  
  /**
     *Pays player based on their hand rank
     *pre: none
     *post: add any winnings to players bank
     */
  public int payBack()
  {
    int winnings;
    if(this.state == 2)
    {
      winnings = (this.handRank == 9 && this.betPool == 5 ? MAX_PAYBACK : (PAYBACK[this.handRank] * this.betPool));
      this.player.win(winnings);
      this.state = 0;
      return winnings;
    }
    else
    {
      return -1;
    }
  }
  
  public int getHandRank()
  {
    return this.handRank;
  }
  
  public int getCurrentBet()
  {
    return this.betPool;
  }
  
  //adds money
  public void addFunds(int amount)
  {
    player.addMoney(amount);
  }
  
  /**
     *Show the current net gain/loss of player
     *pre: none
     *post: returns total  gain/loss of player
     */
    public int getPlayerNet()
    {
        return this.player.getNet();
    }

    /**
     *Shows the amount of money currently left in the player's bank.
     *pre: none
     *post: returns the amount of money the player has.
     */
    public int getCurrentBank()
    {
        return this.player.currentBank();
    }
    
      /**
     *Gets the images associated with the cards in hand.
     *pre: hand contains 5 cards
     *post: returns an array of ImageIcon corresponding to each card in the hand.
     */
    public ImageIcon[] getCardImages()
    {
        return this.player.getCardImages();
    }
  
  //makes a bet if sufficient funds else returns -1
  private boolean makeBet(int amount)
  {
    int temp;
    if(amount >= 1 && amount <= 5)
    {
      temp = this.player.makeBet(amount);
      if(temp != -1)
      {
        this.betPool = temp;
        return true;
      }
      else
      {
        return false;
      }
    }  
    else
    {
      return false;
    }
  }
  
  
  
}