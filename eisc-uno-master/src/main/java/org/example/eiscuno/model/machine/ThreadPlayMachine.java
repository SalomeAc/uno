package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;
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

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, Deck deck, GameUno gameUno) {
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
                        Thread.currentThread().interrupt(); // Restablecer el estado de interrupción
                        return;
                    }
                    // Lógica para colocar la carta
                    putCardOnTheTable();
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
                break; // Romper el bucle si se ha jugado una carta
            } else {
                System.out.println("No hay cartas jugables en la mano del jugador máquina.");
                // La máquina toma una carta del mazo
                Card newCard = deck.takeCard();
                if (newCard != null) {
                    machinePlayer.addCard(newCard);
                    System.out.println("La máquina toma una carta: " + newCard);
                    if (!gameUno.isCardPlayable(newCard, cardOnTable)) {
                        System.out.println("La carta tomada no es jugable. Cediendo el turno.");
                        setHasPlayerPlayed(false); // Ceder el turno al jugador
                        break; // Romper el bucle si la carta no es jugable
                    }
                } else {
                    System.out.println("El mazo está vacío. No se puede tomar una carta.");
                    break; // Romper el bucle si el mazo está vacío
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