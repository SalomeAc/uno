package org.example.eiscuno.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.designPattern.Observer;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

/**
 * Controller class for the Uno game.
 */
public class GameUnoController implements Observer {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private Button barajaButton;

    @FXML
    private Button unoButton;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        initVariables();

        setBackground();
        Image barajaImage = new Image(getClass().getResourceAsStream("/org/example/eiscuno/cards-uno/deck_of_cards.png"));
        BackgroundSize backgroundSize = new BackgroundSize(120, 169, false, false, true, true);
        BackgroundImage backgroundImage = new BackgroundImage(barajaImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        Background background = new Background(backgroundImage);
        barajaButton.setBackground(background);

        Image unoImage = new Image(getClass().getResourceAsStream("/org/example/eiscuno/images/button_uno.png"));
        BackgroundSize backgroundSizeUnoImage = new BackgroundSize(150, 200, false, false, true, false);
        BackgroundImage backgroundUnoImage = new BackgroundImage(unoImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSizeUnoImage);
        Background backgroundUnoButton = new Background(backgroundUnoImage);
        unoButton.setBackground(backgroundUnoButton);

        this.gameUno.startGame();
        this.gameUno.addObserver(this);

        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView, this.deck, this.gameUno);
        threadPlayMachine.start();
        printCardsHumanPlayer();
        printCardsMachinePlayer();

    }

    /**
     * Sets the background image for the game.
     */

    private void setBackground() {
        Image backgroundImage = new Image(getClass().getResource("/org/example/eiscuno/images/background_uno.png").toExternalForm());

        double width = rootBorderPane.getWidth();
        double height = rootBorderPane.getHeight();

        BackgroundImage backgroundImg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(width, height, true, true, true, true)
        );

        rootBorderPane.setBackground(new Background(backgroundImg));
    }

    /**
     * Initializes the variables for the game.
     */
    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
    }

    /**
     * Prints the human player's cards on the grid pane.
     */
    private void printCardsHumanPlayer() {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {

                gameUno.playCard(card, humanPlayer);
                tableImageView.setImage(card.getImage());
                humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                threadPlayMachine.setHasPlayerPlayed(true);
                printCardsHumanPlayer();
                printCardsMachinePlayer(); // Refresh machine cards



            });

            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    /**
     * Displays the machine player's cards on the grid pane.
     * This method clears the current grid pane and adds an ImageView for each card
     * in the machine player's hand. The cards are displayed face down.
     */
    private void printCardsMachinePlayer() {
        // Clear existing cards from the grid pane
        this.gridPaneCardsMachine.getChildren().clear();

        // Get the current cards of the machine player as an array
        Card[] currentVisibleCardsMachinePlayer = this.machinePlayer.getCardsPlayer().toArray(new Card[0]);

        // Determine the number of cards to display (max 4)
        int cardsToDisplay = Math.min(currentVisibleCardsMachinePlayer.length, 4);

        // Iterate through each card and create an ImageView for it
        for (int i = 0; i < cardsToDisplay; i++) {
            // Create a new ImageView for the card with the placeholder image
            ImageView newCardImageView = new ImageView(new Image(getClass().getResourceAsStream(
                    "/org/example/eiscuno/cards-uno/card_uno.png")));

            // Set the size and preserve ratio of the card image
            newCardImageView.setFitHeight(90);
            newCardImageView.setFitWidth(70);
            newCardImageView.setPreserveRatio(true);

            // Add the card image to the grid pane at the appropriate position
            this.gridPaneCardsMachine.add(newCardImageView, i, 0);


        }
        System.out.println("Cartas de la máquina: " + machinePlayer.getCardsPlayer());

    }


    /**
     * Finds the position of a specific card in the human player's hand.
     *
     * @param card the card to find
     * @return the position of the card, or -1 if not found
     */
    private Integer findPosCardsHumanPlayer(Card card) {
        for (int i = 0; i < this.humanPlayer.getCardsPlayer().size(); i++) {
            if (this.humanPlayer.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }



    /**
     * Handles the "Back" button action to show the previous set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printCardsHumanPlayer();
        }
    }

    /**
     * Handles the "Next" button action to show the next set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printCardsHumanPlayer();
        }
    }

    /**
     * Handles the action of taking a card.
     *
     * @param event the action event
     */
    @FXML
    void onHandleTakeCard(ActionEvent event) {
        if (!threadPlayMachine.isHasPlayerPlayed()) {
            Card newCard = deck.takeCard();
            this.humanPlayer.addCard(newCard);
            System.out.println("Se añadió la carta " + newCard);
            System.out.println("Cartas del jugador: " + humanPlayer.getCardsPlayer());
            printCardsHumanPlayer();
            if (!gameUno.isCardPlayable(newCard, table.getCurrentCardOnTheTable())) {
                // Ceder el turno a la máquina
                threadPlayMachine.setHasPlayerPlayed(true);
            }
        } else {
            Card newCard = deck.takeCard();
            this.machinePlayer.addCard(newCard);
            System.out.println("Se añadió la carta " + newCard);
            printCardsMessageMachinePlayer();
            printCardsMachinePlayer();
            if (!gameUno.isCardPlayable(newCard, table.getCurrentCardOnTheTable())) {
                // La máquina toma una carta y no es jugable, no hacer nada especial
                // El hilo de la máquina seguirá intentando jugar

            }
        }
        System.out.println("Botón Baraja");
    }

    /**
     * Prints the machine player's cards to the console.
     * This method outputs a message to the console listing the cards currently held by the machine player.
     */
    public void printCardsMessageMachinePlayer() {
        System.out.println("Cartas del jugador máquina: " + machinePlayer.getCardsPlayer());
    }



    /**
     * Handles the action of saying "Uno".
     *
     * @param event the action event
     */
    @FXML
    void onHandleUno(ActionEvent event) {
        System.out.println("Pressed Uno button");
    }

    /**
     * Updates the game state when the machine plays a card.
     * This method should be called whenever the machine player plays a card to
     * update the display of the machine player's cards.
     */
    private void updateMachinePlayerCards() {
        printCardsMachinePlayer();
        printCardsHumanPlayer(); // Optional: Refresh human cards if needed
    }


    /**
     * Update method required by Observer interface.
     */
    @Override
    public void update() {
        printCardsHumanPlayer();
    }


}
