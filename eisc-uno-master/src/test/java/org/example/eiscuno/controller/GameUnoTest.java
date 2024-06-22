package org.example.eiscuno.controller;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.model.game.GameUno;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameUnoTest extends ApplicationTest {

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;

    @Override
    public void start(Stage stage) {
        humanPlayer = new Player("HUMAN_PLAYER");
        machinePlayer = new Player("MACHINE_PLAYER");
        deck = new Deck();
        table = new Table();
        gameUno = new GameUno(humanPlayer, machinePlayer, deck, table);
        gameUno.startGame();

        Scene scene = new Scene(new javafx.scene.layout.StackPane(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void machineShouldEat2Cards() {

        String imageUrl = "/org/example/eiscuno/cards-uno/2_wild_draw_red.png";
        String cardValue = "+2";
        String cardColor = "RED";
        Card plus2Card = new Card(imageUrl, cardValue, cardColor);

        gameUno.playCard(plus2Card, humanPlayer);

        assertEquals(7, machinePlayer.getCardsPlayer().size());
    }
}
