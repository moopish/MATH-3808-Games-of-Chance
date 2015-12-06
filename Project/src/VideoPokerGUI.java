import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 *
 * @author Michael van Dyk
 * <p>
 * Date : Mar 24, 2015
 * <p>
 *  The GUI for the video poker game
 */
@SuppressWarnings("serial")
public class VideoPokerGUI extends JFrame {



    public static final String TITLE = "Video Poker";   // The window title

    /*
     * The dimensions of the game window
     */
    private static final int GAME_WIDTH     = 800;
    private static final int GAME_HEIGHT    = 600;

    /*
     * Exit states
     */
    public static final int EXIT_CANNOT_LOAD_IMAGES     = 1; // Fail if the images could not be loaded
    public static final int EXIT_DEAL_FAIL              = 2; // Fail when deal is in progress
    public static final int EXIT_UPDATE_FAIL            = 3; // Fail during update

    /*
     * The Potential game states
     */
    private static final int STATE_BET  = 1; // The user makes their bet at this state
    private static final int STATE_HOLD     = 2; // The user selects which cards to hold at this state

    /*
     * The back card image, all other images are stored in the card class
     */
    private static BufferedImage CARD_BACK_IMAGE;

    /*
     * Colours
     */
    private static final Color AREA_BACKGROUND      = new Color(0, 0, 255);
    private static final Color CHART_BET_COLOUR     = new Color(208, 0, 0);
    private static final Color CHART_BACKGROUND     = new Color(64, 64, 64);
    private static final Color HOLD_BACKGROUND      = new Color(0, 0, 0, 192);
    private static final Color TEXT_COLOUR          = new Color(255, 255, 0);
    private static final Color TEXT_SELECTED        = new Color(255, 255, 255);

    /*
     * Fonts
     */
    private static final Font PAYBACK_FONT  = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    private static final Font CHART_FONT    = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    /*
     * Rank constants
     */
    private static final String[] RANK_NAME = {"LOWER THAN JACKS", "JACKS OR BETTER", "TWO PAIR",
            "THREE OF A KIND", "STRAIGHT", "FLUSH", "FULL HOUSE",
            "FOUR OF A KIND", "STRAIGHT FLUSH", "ROYAL FLUSH"};

    private static final int[]  RANK_PAYBACK                    = {0, 1, 2, 3, 4, 6, 9, 25, 50, 250};
    private static final int    RANK_ROYAL_FLUSH_BIG_PAYBACK    = 800; // Multi'd by 5 when used (fits into the formula un-multi'd)

    /*
     * Image dimensions
     */
    private static final int DRAW_IMAGE_WIDTH   = 640;
    private static final int DRAW_IMAGE_HEIGHT  = 480;

    /*
     * Card constants
     */
    private static final int CARD_WIDTH         = DRAW_IMAGE_WIDTH/6;
    private static final int CARD_HEIGHT        = 7*CARD_WIDTH/5;
    private static final int CARD_SEPARATION    = CARD_WIDTH/8;
    private static final int CARD_Y             = 5*DRAW_IMAGE_HEIGHT/9;

    /*
     * Chart constants
     */
    private static final int CHART_EDGE_DIST        = 10;
    private static final int CHART_HEIGHT   = 200;
    private static final int CHART_DIFF             = 18;
    private static final int CHART_PAYBACK_WIDTH    = (DRAW_IMAGE_WIDTH - CHART_EDGE_DIST * 2)/8;

    /*
     * Button constants
     */
    private static final int DEFAULT_BUTTON_HEIGHT = 25; // The default height of a button


    /*
     * Attributes
     */
    private final VideoPoker            game_logic;     // The logic of the game is handled with this class
    private final DrawArea              drawArea;  // Where the images are drawn

    private final HoldButton[]          holdButtons; // The buttons to determine cards held
    private final boolean[]             hold;   // Stores which buttons are held
    private final boolean[]             face_up;     // Stores which cards are face up

    private final JButton               betOneButton; // The button to bet one credit
    private final JButton               betMaxButton; // The button to bet the max credits
    private final JButton               dealButton;  // The button to deal the cards
    private final JComboBox<Integer> betDropDown; // Allows the user to pick the bet via drop down
    private final JLabel    netGainLoss; // The total amount the player has won or lost

    private final JTextField   fundsAmount; // The amount of funds the user wishes to enter
    private final JButton    fundsButton; // Confirm the add of funds with this button

    private boolean                     do_update;  // Used to determine if an update is needed
    private int                         bet_amount;  // The current betting amount
    private int                         game_state;  // The current state of the game

    /**
     * Constructor for the game's GUI, sets everything up.
     * Use start() to begin the game after creation.
     */
    public VideoPokerGUI() {
        super(TITLE);

        game_logic = new VideoPoker();

        if (CARD_BACK_IMAGE == null)
            CARD_BACK_IMAGE = loadImage("Cards/hidden.png");

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.getContentPane().setBackground(Color.BLACK);
        this.setSize(GAME_WIDTH, GAME_HEIGHT);
        this.setResizable(false);

        // Sets up the game area (everything on screen)
        JPanel gameArea = new JPanel();
        gameArea.setLayout(null);
        gameArea.setBackground(this.getContentPane().getBackground());
        gameArea.setLocation(5, 5);
        gameArea.setSize(GAME_WIDTH, GAME_HEIGHT);

        // Adds the drawArea so things can be drawn to screen
        gameArea.add(drawArea = new DrawArea(DRAW_IMAGE_WIDTH, DRAW_IMAGE_HEIGHT));

        /*
         * Setting up the hold buttons and hold and face_up values
         */
        hold = new boolean [5];
        face_up = new boolean[5];
        holdButtons = new HoldButton[5];
        for (int i=0; i<5; ++i) {
            hold[i] = face_up[i] = false;
            holdButtons[i] = new HoldButton(i);
            holdButtons[i].setLocation((CARD_WIDTH + CARD_SEPARATION) * i + 2 * CARD_SEPARATION, DRAW_IMAGE_HEIGHT + DEFAULT_BUTTON_HEIGHT/5);

            gameArea.add(holdButtons[i]);
        }

        /*
         * Setting up the betting panel and its contents
         */
        JPanel bettingPanel = new JPanel(null);

        int bet_buttons_width = 130;

        JLabel bet_label = new JLabel("Bet:  ", SwingConstants.RIGHT);
        bet_label.setFont(CHART_FONT);
        bet_label.setSize(bet_buttons_width / 2, DEFAULT_BUTTON_HEIGHT);
        bet_label.setLocation(0, 0);
        bet_label.setForeground(TEXT_COLOUR);
        bettingPanel.add(bet_label);

        //The combo box that you can explicitly choose your bet amount
        betDropDown = new JComboBox<Integer>(new Integer[]{0, 1, 2, 3, 4, 5});
        betDropDown.setSize(bet_buttons_width/2, DEFAULT_BUTTON_HEIGHT);
        betDropDown.setLocation(bet_buttons_width / 2, 0);
        betDropDown.setFont(CHART_FONT);
        betDropDown.setSelectedIndex(0);
        betDropDown.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                bet_amount = betDropDown.getItemAt(betDropDown.getSelectedIndex());
                if (bet_amount > game_logic.getCurrentBank())
                    betDropDown.setSelectedIndex(bet_amount = game_logic.getCurrentBank());
                dealButton.setEnabled(bet_amount != 0);
                drawArea.draw(-1);
            }

        });
        bettingPanel.add(betDropDown);

        // Button to increase your bet by one
        betOneButton = new JButton("Bet One");
        betOneButton.setSize(bet_buttons_width, DEFAULT_BUTTON_HEIGHT);
        betOneButton.setLocation(0, 7 * DEFAULT_BUTTON_HEIGHT / 5);
        betOneButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                bet_amount = (bet_amount % 5) + 1;
                dealButton.setEnabled(true);
                betDropDown.setSelectedIndex(bet_amount);
                drawArea.draw(-1);
            }

        });
        bettingPanel.add(betOneButton);

        // Button to get the maximum bet amount
        betMaxButton = new JButton("Bet Max");
        betMaxButton.setSize(bet_buttons_width, DEFAULT_BUTTON_HEIGHT);
        betMaxButton.setLocation(0, 14 * DEFAULT_BUTTON_HEIGHT / 5);
        betMaxButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                bet_amount = 5;
                dealButton.setEnabled(true);
                betDropDown.setSelectedIndex(bet_amount);
                drawArea.draw(-1);
            }

        });
        bettingPanel.add(betMaxButton);

        JLabel gainLossHeader = new JLabel("Net Gain/Loss:", SwingConstants.CENTER);
        gainLossHeader.setFont(CHART_FONT);
        gainLossHeader.setForeground(TEXT_COLOUR);
        gainLossHeader.setSize(bet_buttons_width, DEFAULT_BUTTON_HEIGHT);
        gainLossHeader.setLocation(0, 21 * DEFAULT_BUTTON_HEIGHT / 5);
        bettingPanel.add(gainLossHeader);

        // Shows the player the total amount they have won or lost
        netGainLoss = new JLabel("0", SwingConstants.CENTER);
        netGainLoss.setFont(CHART_FONT);
        netGainLoss.setForeground(TEXT_COLOUR);
        netGainLoss.setSize(bet_buttons_width, DEFAULT_BUTTON_HEIGHT);
        netGainLoss.setLocation(0, 28 * DEFAULT_BUTTON_HEIGHT / 5);
        bettingPanel.add(netGainLoss);

        bettingPanel.setBackground(this.getContentPane().getBackground());
        bettingPanel.setLocation(DRAW_IMAGE_WIDTH + 10, 50);
        bettingPanel.setSize(bet_buttons_width, 34 * DEFAULT_BUTTON_HEIGHT / 5);
        gameArea.add(bettingPanel);

        // Adding the deal button so the player can progress the game
        dealButton = new JButton("Deal");
        dealButton.setSize(bet_buttons_width, 3*DEFAULT_BUTTON_HEIGHT);
        dealButton.setLocation(bettingPanel.getX(), CARD_Y + CARD_HEIGHT / 2 - 3 * DEFAULT_BUTTON_HEIGHT / 2);
        dealButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                do_update = true;
            }

        });
        gameArea.add(dealButton);

        /*
         * The adding of funds to the players bank so they can play
         */
        JPanel fundsPanel = new JPanel(null);
        fundsPanel.setSize(3*bet_buttons_width, DEFAULT_BUTTON_HEIGHT);
        fundsPanel.setBackground(gameArea.getBackground());
        fundsPanel.setLocation(DRAW_IMAGE_WIDTH / 2 - fundsPanel.getWidth() / 2, GAME_HEIGHT - 15 * DEFAULT_BUTTON_HEIGHT / 5);

        JLabel addFunds = new JLabel("Enter Funds:  ", SwingConstants.RIGHT);
        addFunds.setFont(CHART_FONT);
        addFunds.setForeground(TEXT_COLOUR);
        addFunds.setSize(bet_buttons_width, DEFAULT_BUTTON_HEIGHT);
        addFunds.setLocation(0, 0);
        fundsPanel.add(addFunds);

        fundsAmount = new JTextField();
        fundsAmount.setHorizontalAlignment(SwingConstants.RIGHT);
        fundsAmount.setSize(bet_buttons_width, DEFAULT_BUTTON_HEIGHT);
        fundsAmount.setLocation(bet_buttons_width, 0);
        fundsPanel.add(fundsAmount);

        fundsButton = new JButton("Add Funds");
        fundsButton.setSize(bet_buttons_width, DEFAULT_BUTTON_HEIGHT);
        fundsButton.setLocation(2 * bet_buttons_width, 0);
        fundsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String textField = fundsAmount.getText().trim();
                fundsAmount.setText("");

                if (textField.equals("")) {
                    JOptionPane.showMessageDialog(VideoPokerGUI.this, "Funds field is empty!", "Entering Incorrect Funds", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (isNumeric(textField)) {
                    game_logic.addFunds(Integer.valueOf(textField));
                    betOneButton.setEnabled(game_logic.getCurrentBank() > 0);
                    betMaxButton.setEnabled(game_logic.getCurrentBank() >= 5);
                    drawArea.draw(-1);
                } else {
                    JOptionPane.showMessageDialog(VideoPokerGUI.this, "Funds must be a positive integral value. No decimal or text!", "Entering Incorrect Funds", JOptionPane.WARNING_MESSAGE);
                }
            }

        });
        fundsPanel.add(fundsButton);

        gameArea.add(fundsPanel);

        this.add(gameArea);
        this.setIconImage(loadImage("Cards/icon.png"));

        bet_amount = 0;
        game_state = STATE_BET;
        do_update = false;
        drawArea.draw(-1);
        enableWidgets();
    }

    /**
     * Disables the widgets from being clicked
     */
    private void disableWidgets() {
        for (HoldButton hb : holdButtons)
            hb.setEnabled(false);

        fundsAmount.setEnabled(false);
        fundsButton.setEnabled(false);
        betDropDown.setEnabled(false);
        betOneButton.setEnabled(false);
        betMaxButton.setEnabled(false);
        dealButton.setEnabled(false);
    }

    /**
     * Enables the widgets that are allowed to be clicked
     * during the current game state
     */
    private void enableWidgets() {
        boolean betState = game_state == STATE_BET;

        for (HoldButton hb : holdButtons)
            hb.setEnabled(!betState);

        fundsAmount.setEnabled(betState);
        fundsButton.setEnabled(betState);
        betDropDown.setEnabled(betState);
        betOneButton.setEnabled(betState && game_logic.getCurrentBank() > 0);
        betMaxButton.setEnabled(betState && game_logic.getCurrentBank() >= 5);
        dealButton.setEnabled(bet_amount != 0);
    }

    /**
     * Flips the next card that has not yet been flipped
     * @return true if a card was flipped, false if no card was flipped
     */
    private boolean flipNextCard() {
        for (int i=0; i<5; ++i) {
            if (!face_up[i])
                return (face_up[i] = true);
        }
        return (false);
    }

    /**
     * Gets the name of the rank
     * @param  rank the rank of the hand
     * @return      the name of the rank
     */
    public static String getRankName(final int rank) {
        return ((rank >= 0 && rank < RANK_NAME.length) ? RANK_NAME[rank]: "");
    }

    /**
     * Gets the base payback for a ranked hand
     * @param  rank the rank of the hand
     * @return      the base payback
     */
    public static int getRankPayback(final int rank) {
        return ((rank >= 0 && rank < RANK_PAYBACK.length) ? RANK_PAYBACK[rank]: 0);
    }

    /**
     * Gets the payback for the rank with the specified bet
     * @param  rank the rank of the hand
     * @param  bet  the amount bet
     * @return      the payback of the bet
     */
    public static int getRankPayback(final int rank, final int bet) {
        if (bet <= 0 || bet > 5)
            return (0);
        return ((rank == 9 && bet == 5) ? RANK_ROYAL_FLUSH_BIG_PAYBACK * bet : getRankPayback(rank) * bet);
    }

    /**
     * Determines if a string is numeric, so it can be safely converted to an integer
     * @param  str the string to check
     * @return     true if it is numeric, false if not
     */
    public static boolean isNumeric(String str) {
        for (int i=0; i<str.length(); ++i) {
            char at_i = str.charAt(i);
            if (at_i < '0' || at_i > '9')
                return (false);
        }
        return (true);
    }

    /**
     * Opens the image with the specified file name
     * @param  file_name the image to open
     * @return           the specified image
     */
    public BufferedImage loadImage(final String file_name) {
        try {
            return (ImageIO.read(this.getClass().getResourceAsStream(file_name)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(VideoPokerGUI.EXIT_CANNOT_LOAD_IMAGES);
        }
        return (null);
    }

    /**
     * Starts the game
     */
    public void start() {
        setVisible(true);
        while(this.isVisible()) {
            try {
                if (do_update) {
                    update();
                    do_update = false;
                }
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the state of the game, progresses play
     */
    private void update() {
        disableWidgets();
        int payback = -1;

        switch (game_state) {
            case STATE_BET:
                if (bet_amount == 0) {
                    JOptionPane.showMessageDialog(VideoPokerGUI.this, "Bet cannot be zero!", "Make a bet", JOptionPane.WARNING_MESSAGE);
                    enableWidgets();
                    do_update = false;
                    return;
                }

                for (int i=0; i<5; ++i) {
                    face_up[i] = false;
                    hold[i] = false;
                }

                if (game_logic.deal(bet_amount) == -1)
                    break;


                break;
            case STATE_HOLD:
                for (int i=0; i<5; ++i)
                    if (!hold[i])
                        face_up[i] = false;

                if (game_logic.mulligan(hold) == -1)
                    return;

                    for (int i=0; i<5; ++i) {
                    hold[i] = false;
                    }

                payback = game_logic.payBack();

                if (game_logic.getCurrentBank() < bet_amount) {
                    bet_amount = game_logic.getCurrentBank();
                    betDropDown.setSelectedIndex(bet_amount);
                }

                break;
            default:
                System.err.println("Something went wrong when deal was clicked");
                System.exit(VideoPokerGUI.EXIT_DEAL_FAIL);
        }

        drawArea.draw(payback);
        while (flipNextCard()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println(EXIT_UPDATE_FAIL);
            }

            drawArea.draw(payback);
        }

        netGainLoss.setText(String.valueOf(game_logic.getPlayerNet()));
        game_state = (game_state == STATE_BET) ? STATE_HOLD : STATE_BET;
        enableWidgets();
    }

    /**
     *
     * @author Michael van Dyk
     * <p>
     * Date : Apr 3, 2015
     * <p>
     * Specially made for the hold buttons. Used
     * to change the information on whether a
     * card is held for the next deal.
     */
    private final class HoldButton extends JButton {

        private final int hold_index;   // The held index

        /**
         * The constructor for the HoldButton
         * @param index the index in the hold array that is affected when clicked
         */
        public HoldButton(int index) {
            super("Hold");

            this.setSize(CARD_WIDTH, DEFAULT_BUTTON_HEIGHT);
            this.addActionListener(button_clicked);

            hold_index = index;
        }

        /**
         * Switches the state of the hold. True becomes false,
         * false becomes true.
         */
        public void flip() {
            setHold(!isHeld());
        }

        /**
         * Gets the hold state
         * @return the state of the hold (true if the card
         * is being held for the next draw)
         */
        public boolean isHeld() {
            return (hold[hold_index]);
        }

        /**
         * Sets the state of the hold
         * @param val the state that it is to be set to
         */
        public void setHold(boolean val) {
            hold[hold_index] = val;
        }

        /**
         * When clicked, the button flips state and the screen is redrawn
         */
        private ActionListener button_clicked = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                flip();
                drawArea.draw(-1);
            }

        };
    }

    /**
     *
     * @author Michael van Dyk
     * <p>
     * Date : Apr 6, 2015
     * <p>
     * The draw area is what is drawn, including cards, payback chart, winnings, etc.
     */
    private final class DrawArea extends JLabel {

        private final BufferedImage     display; // The displayed image
        private final Graphics2D        draw;  // The tool for drawing

        private final RenderingHints REND_HINTS;

        /**
         * The constructor of the DrawArea class
         * @param width    the width of the area that the end image is drawn to
         * @param height   the height of the area that the end image is drawn to
         */
        public DrawArea(final int width, final int height) {
            this.setSize(width, height);
            this.setLocation(0, 0);

            this.display = new BufferedImage(DRAW_IMAGE_WIDTH, DRAW_IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            this.draw = display.createGraphics();
            REND_HINTS = this.draw.getRenderingHints();
            REND_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            REND_HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            this.draw.setRenderingHints(REND_HINTS);
        }

        /**
         * Draws the images to the screen
         * @param payback value the player wins if, -1 not win drawing
         */
        public void draw(int payback) {
            draw.setColor(AREA_BACKGROUND);
            draw.fillRoundRect(0, 0, DRAW_IMAGE_WIDTH, DRAW_IMAGE_HEIGHT, 30, 30);

            boolean allFaceUp = true;

            // Checks to see if all the cards are face up
            for (int i=0; i< face_up.length && allFaceUp; ++i)
                if (!face_up[i])
                    allFaceUp = false;

            drawBetChart(game_logic.getHandRank(), allFaceUp);

            // draw cards
            ImageIcon cards[] = game_logic.getCardImages();
            for (int i=0; i<5; ++i) {
                if (cards[i] != null) {
                    BufferedImage sendImg = new BufferedImage(cards[i].getIconWidth(), cards[i].getIconHeight(), BufferedImage.TYPE_INT_RGB);
                    sendImg.createGraphics().drawImage(cards[i].getImage(), 0, 0, null);
                    drawCard(sendImg, i);
                } else {
                    drawCard(null, i);
                }
            }

            draw.setColor(TEXT_COLOUR);
            draw.setFont(PAYBACK_FONT);
            draw.drawString("BET " + bet_amount, CHART_EDGE_DIST, DRAW_IMAGE_HEIGHT - PAYBACK_FONT.getSize() / 2);

            String temp = String.valueOf(game_logic.getCurrentBank());

            if (temp.length() > 6)
                temp = "999999";
            else
                while (temp.length() < 6)
                    temp = "0" + temp;

            FontMetrics fontMetrics = getFontMetrics(PAYBACK_FONT);
            temp = "CREDITS " + temp;
            int width = fontMetrics.stringWidth(temp);
            draw.drawString(temp, DRAW_IMAGE_WIDTH - width - CHART_EDGE_DIST, DRAW_IMAGE_HEIGHT - PAYBACK_FONT.getSize()/2);

            if (allFaceUp && payback != -1) {
                draw.setFont(PAYBACK_FONT);
                draw.drawString(getRankName(game_logic.getHandRank()), CHART_EDGE_DIST, CHART_HEIGHT + CHART_EDGE_DIST * 3 + PAYBACK_FONT.getSize() / 2);
                temp =  "PAYBACK " + getRankPayback(game_logic.getHandRank(), bet_amount);
                width = fontMetrics.stringWidth(temp);
                draw.drawString(temp, DRAW_IMAGE_WIDTH - width - CHART_EDGE_DIST, CHART_HEIGHT + CHART_EDGE_DIST * 3 + PAYBACK_FONT.getSize()/2);
            }

            // Updates the image to screen
            setIcon(new ImageIcon(display.getScaledInstance(getWidth(), getHeight(), Image.SCALE_REPLICATE)));
        }

        /**
         * Draws the card to the screen at a certain position.
         * @param card     the card to be drawn
         * @param position the position of the card, 0 being the first position
         */
        private void drawCard(final Image card, final int position) {
            int x = (CARD_WIDTH + CARD_SEPARATION) * position + 2 * CARD_SEPARATION;   // The x position of the card on screen

            if (face_up[position]) {   // If the card face value is shown to the user
                draw.drawImage(card.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH), x, CARD_Y, null);

                // If the card is being held draws something to show that
                //fact to the user
                if (hold[position]) {
                    draw.setColor(HOLD_BACKGROUND);
                    draw.fillRect(x, CARD_Y + 3*CARD_HEIGHT/7, CARD_WIDTH, CARD_HEIGHT/7);

                    draw.setColor(TEXT_COLOUR);
                    draw.drawString("HOLD", x + CARD_WIDTH/5, CARD_Y + 11*CARD_HEIGHT/20);
                }
            } else {
                // The card back is shown since the values are not yet shown to the player
                draw.drawImage(CARD_BACK_IMAGE.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH), x, CARD_Y, null);
            }
        }

        /**
         * Draws the payback chart of the game and shows
         * what the player has if all the cards are face up.
         * @param rank      the rank of the players hand
         * @param allFaceUp used to determine if all the cards are face up
         */
        private void drawBetChart(final int rank, final boolean allFaceUp) {
            draw.setFont(CHART_FONT);
            FontMetrics fontMetrics = getFontMetrics(CHART_FONT);

            // Draws the outer line of the chart
            draw.setColor(CHART_BACKGROUND);
            draw.fillRect(CHART_EDGE_DIST, CHART_EDGE_DIST, DRAW_IMAGE_WIDTH - CHART_EDGE_DIST * 2, CHART_HEIGHT);

            // If the bet is greater than 0 then highlight the potential paybacks
            //based on that bet
            if (bet_amount > 0) {
                draw.setColor(CHART_BET_COLOUR);
                draw.fillRect(CHART_PAYBACK_WIDTH * (bet_amount + 2) + CHART_EDGE_DIST + 2, CHART_EDGE_DIST, CHART_PAYBACK_WIDTH, CHART_HEIGHT);
            }

            // Filling out the top row of the chart
            draw.setColor(TEXT_COLOUR);
            draw.setStroke(new BasicStroke(3));
            draw.drawRect(CHART_EDGE_DIST, CHART_EDGE_DIST, DRAW_IMAGE_WIDTH - CHART_EDGE_DIST * 2, CHART_HEIGHT);
            draw.drawString("HAND RANK", CHART_EDGE_DIST * 2, CHART_EDGE_DIST * 3 );
            draw.drawLine(CHART_EDGE_DIST, CHART_EDGE_DIST * 2 + 5*CHART_DIFF/6, DRAW_IMAGE_WIDTH - CHART_EDGE_DIST, CHART_EDGE_DIST * 2 + 5*CHART_DIFF/6);

            String temp;
            int width;

            for (int i=3; i<8; ++i) {
                temp = "BET " + (i - 2);
                width = fontMetrics.stringWidth(temp);
                draw.drawString(temp, CHART_PAYBACK_WIDTH * (i + 1) - width, CHART_EDGE_DIST * 3);
                draw.drawLine(CHART_PAYBACK_WIDTH * i + CHART_EDGE_DIST, CHART_EDGE_DIST, CHART_PAYBACK_WIDTH * i + CHART_EDGE_DIST, CHART_HEIGHT + CHART_EDGE_DIST);
            }

            final int start_x = CHART_PAYBACK_WIDTH * 4;

            // Goes through and fills in the hand rank name and potential
            //paybacks for each bet level
            for (int i=9; i>=1; --i) {
                if (i == rank && allFaceUp)
                    draw.setColor(TEXT_SELECTED);
                else
                    draw.setColor(TEXT_COLOUR);

                draw.drawString(getRankName(i), CHART_EDGE_DIST * 2, CHART_HEIGHT - CHART_DIFF * (i - 1));

                for (int j=1; j<=5; ++j) {
                    temp = String.valueOf(getRankPayback(i, j));
                    width = fontMetrics.stringWidth(temp);
                    draw.drawString(temp, start_x + CHART_PAYBACK_WIDTH * (j-1) - width, CHART_HEIGHT - CHART_DIFF * (i - 1));
                }
            }
        }
    }

    /**
     * The main function of the application
     * @param args arguments passed to the application (ignored)
     */
    public static void main(String[] args) {
        VideoPokerGUI game = new VideoPokerGUI();
        game.start();
    }

}