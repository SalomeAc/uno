package org.example.eiscuno.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import org.example.eiscuno.view.ColorSelectionDialog;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    private boolean isHumanTurn = true;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> unoTask;

    @FXML
    public void initialize() {
        initVariables();
        this.gameUno.playCard(deck.takeCard(), humanPlayer);
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
        this.gameUno.addObserver(this);

        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        threadPlayMachine = new ThreadPlayMachine(this, this.table, this.machinePlayer, this.tableImageView, this.deck, this.gameUno);
        threadPlayMachine.start();
    }

    public GameUnoController(GameUno gameUno) {
        this.gameUno = gameUno;
    }

    public GameUnoController() {}

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

    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
    }

    public void validateSpecialCard(Card card, Player player) {
        gameUno.validateSpecialCard(card, player);
    }

    private void printCardsHumanPlayer() {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                if (gameUno.isCardPlayable(card, table.getCurrentCardOnTheTable())) {
                    if (card.isWild()) {
                        showColorSelectionDialog(card);
                    } else {
                        playCard(card);
                    }
                }
            });

            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
        System.out.println("Cartas de la máquina: " + humanPlayer.getCardsPlayer());
    }
    private void showColorSelectionDialog(Card card) {
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog();
        Optional<ButtonType> result = colorSelectionDialog.showAndWait();

        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selectedColor = colorSelectionDialog.getSelectedColor();
                card.setColor(selectedColor);
                playCard(card);
            }
        });
    }
    private void playCard(Card card) {
        gameUno.playCard(card, humanPlayer);
        tableImageView.setImage(card.getImage());
        humanPlayer.removeCard(findPosCardsHumanPlayer(card));
        printCardsHumanPlayer();
        printCardsMachinePlayer();
        checkForWinner();

        if (card.getValue().equals("SKIP")) {
            System.out.println("Played SKIP card. Turn continues for the same player.");
            if (isHumanTurn) {
                // Human player plays again
                startUnoTimerIfNeeded();
            } else {
                // Machine player plays again
                threadPlayMachine.setHasPlayerPlayed(true);
            }
        } else {
            if (isHumanTurn) {
                isHumanTurn = false;
                threadPlayMachine.setHasPlayerPlayed(true);
            } else {
                isHumanTurn = true;
            }
        }

        startUnoTimerIfNeeded();
    }
    private void startUnoTimerIfNeeded() {
        if (isHumanTurn && humanPlayer.getCardsPlayer().size() == 1) {
            startUnoTimer();
        }
    }



    public void printCardsMachinePlayer() {
        this.gridPaneCardsMachine.getChildren().clear();
        Card[] currentVisibleCardsMachinePlayer = this.machinePlayer.getCardsPlayer().toArray(new Card[0]);

        for (int i = 0; i < currentVisibleCardsMachinePlayer.length; i++) {
            Card card = currentVisibleCardsMachinePlayer[i];
            ImageView cardImageViewMachine = card.getCardImageViewMachine();
            cardImageViewMachine.setFitHeight(90);
            cardImageViewMachine.setFitWidth(70);
            cardImageViewMachine.setPreserveRatio(true);
            this.gridPaneCardsMachine.add(cardImageViewMachine, i, 0);
        }
        System.out.println("Cartas de la máquina: " + machinePlayer.getCardsPlayer());
    }

    private Integer findPosCardsHumanPlayer(Card card) {
        for (int i = 0; i < this.humanPlayer.getCardsPlayer().size(); i++) {
            if (this.humanPlayer.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printCardsHumanPlayer();
        }
    }

    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printCardsHumanPlayer();
        }
    }

    @FXML
    void onHandleTakeCard(ActionEvent event) {
        if (!threadPlayMachine.isHasPlayerPlayed()) {
            Card newCard = deck.takeCard();
            this.humanPlayer.addCard(newCard);
            System.out.println("Se añadió la carta " + newCard);
            System.out.println("Cartas del jugador: " + humanPlayer.getCardsPlayer());
            printCardsHumanPlayer();
            if (!gameUno.isCardPlayable(newCard, table.getCurrentCardOnTheTable())) {
                threadPlayMachine.setHasPlayerPlayed(true);
            }
        } else {
            Card newCard = deck.takeCard();
            this.machinePlayer.addCard(newCard);
            System.out.println("Se añadió la carta " + newCard);
            printCardsMessageMachinePlayer();
            printCardsMachinePlayer();
            if (!gameUno.isCardPlayable(newCard, table.getCurrentCardOnTheTable())) {
            }
        }
        System.out.println("Botón Baraja");
    }

    public void printCardsMessageMachinePlayer() {
        System.out.println("Cartas del jugador máquina: " + machinePlayer.getCardsPlayer());
    }
    @FXML
    void onHandleUno(ActionEvent event) {
        System.out.println("Pressed Uno button");
        if (unoTask != null && !unoTask.isDone()) {
            unoTask.cancel(true);
            System.out.println("UNO button pressed in time!");
        }
    }

    private void startUnoTimer() {
        unoTask = scheduler.schedule(() -> {
            Platform.runLater(() -> {
                System.out.println("UNO button not pressed in time. Adding penalty cards.");
                addPenaltyCards(humanPlayer, 2);
            });
        }, 2, TimeUnit.SECONDS);
    }

    //Método penaliza sino toca el botón max en 2s
    private void addPenaltyCards(Player player, int numberOfCards) {
        for (int i = 0; i < numberOfCards; i++) {
            Card newCard = deck.takeCard();
            if (newCard != null) {
                player.addCard(newCard);
            }
        }
        printCardsHumanPlayer(); // Actualizar la interfaz gráfica
    }


    @Override
    public void update() {
        printCardsHumanPlayer();
    }

    private void checkForWinner() {
        if (humanPlayer.getCardsPlayer().isEmpty()) {
            showAlert("Felicidades", "¡Ganaste el juego!");
        } else if (machinePlayer.getCardsPlayer().isEmpty()) {
            showAlert("Lo siento", "La máquina ganó.");
        }
    }


    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


}