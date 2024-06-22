package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;

import javafx.application.Platform;

import javafx.scene.control.Alert;
import org.example.eiscuno.controller.GameUnoController;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

public class ThreadPlayMachine extends Thread {
    private final Table table;
    private final Player machinePlayer;
    private final ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private final Deck deck;
    private final GameUno gameUno;
    private GameUnoController controller;

    public ThreadPlayMachine(GameUnoController controller, Table table, Player machinePlayer, ImageView tableImageView, Deck deck, GameUno gameUno) {
        this.controller = controller;
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.deck = deck;
        this.gameUno = gameUno;
        this.hasPlayerPlayed = false;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (hasPlayerPlayed) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    putCardOnTheTable();
                    Platform.runLater(() -> controller.printCardsMachinePlayer());
                    hasPlayerPlayed = false;
                }
            }
        }
    }

    private void putCardOnTheTable() {
        while (true) {
            if (machinePlayer.getCardsPlayer().isEmpty()) {
                System.out.println("No hay cartas en la mano del jugador máquina.");
                return;
            }

            Card cardOnTable = table.getCurrentCardOnTheTable();

            System.out.println("Carta en la mesa: " + cardOnTable.getValue() + " de " + cardOnTable.getColor());

            // Buscar una carta jugable
            Card selectedCard = null;
            int selectedIndex = -1;
            for (int i = 0; i < machinePlayer.getCardsPlayer().size(); i++) {
                Card card = machinePlayer.getCard(i);

                // Verificar si la carta es válida (no nula)
                if (card.getValue() == null || card.getColor() == null) {
                    System.out.println("Carta inválida detectada: " + card);
                    continue;
                }

                if (card.getValue() == "WILD"){
                    selectedCard = card;

                }

                if (gameUno.isCardPlayable(card, cardOnTable)) {
                    selectedCard = card;
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedCard != null) {
                System.out.println("Carta seleccionada por la máquina: " + selectedCard.getValue() + " de " + selectedCard.getColor());
                table.addCardOnTheTable(selectedCard);
                tableImageView.setImage(selectedCard.getImage());
                machinePlayer.getCardsPlayer().remove(selectedIndex);
                System.out.println("Carta añadida a la mesa: " + selectedCard.getValue() + " de " + selectedCard.getColor());
                break;
            } else {
                System.out.println("No hay cartas jugables en la mano del jugador máquina.");

                Card newCard = deck.takeCard();

                if (newCard != null) {
                    machinePlayer.addCard(newCard);
                    System.out.println("La máquina toma una carta: " + newCard);
                    if (!gameUno.isCardPlayable(newCard, cardOnTable)) {
                        System.out.println("La carta tomada no es jugable. Cediendo el turno.");
                        setHasPlayerPlayed(false);
                        break;
                    }
                } else {
                    System.out.println("El mazo está vacío. No se puede tomar una carta.");
                    break;
                }


            }
        }
    }
    public synchronized void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
    }

    public boolean isHasPlayerPlayed() {
        return hasPlayerPlayed;
    }
}