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
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

/**
 * Controller class for the Uno game.
 */
public class GameUnoController {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;
    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private Button barajaButton;

    @FXML
    private Button unoButton;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        initVariables();
        this.gameUno.playCard(deck.takeCard());
        this.tableImageView.setImage(this.table.getCurrentCardOnTheTable().getImage());

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
        printCardsHumanPlayer();
        printCardsMachinePlayer();


        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        threadPlayMachine = new ThreadPlayMachine(this,this.table, this.machinePlayer, this.tableImageView, this.deck, this.gameUno);
        threadPlayMachine.start();
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
                // Aqui deberian verificar si pueden en la tabla jugar esa carta
                gameUno.playCard(card);
                tableImageView.setImage(card.getImage());
                humanPlayer.removeCard(findPosCardsHumanPlayer(card));


                threadPlayMachine.setHasPlayerPlayed(true);
                printCardsHumanPlayer();
            });
            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }
    public void printCardsMachinePlayer() {
        this.gridPaneCardsMachine.getChildren().clear();
        Card[] currentVisibleCardsMachinePlayer = this.machinePlayer.getCardsPlayer().toArray(new Card[0]);

        for (int i = 0; i < currentVisibleCardsMachinePlayer.length; i++) {
            Card card = currentVisibleCardsMachinePlayer[i];
            ImageView cardImageViewMachine = card.getCardImageViewMachine(); // Asegúrate de tener un método para obtener el ImageView de la carta

            cardImageViewMachine.setFitHeight(90);
            cardImageViewMachine.setFitWidth(70);
            cardImageViewMachine.setPreserveRatio(true);

            this.gridPaneCardsMachine.add(cardImageViewMachine, i, 0);
        }
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
        // Implement logic to handle Uno event here
    }
}