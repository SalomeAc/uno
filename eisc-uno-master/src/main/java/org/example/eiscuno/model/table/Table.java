package org.example.eiscuno.model.table;
import org.example.eiscuno.model.card.Card;

import java.util.ArrayList;

public class Table {
    private ArrayList<Card> cardsTable;

    public Table() {
        cardsTable = new ArrayList<>();
    }

    public void addCardOnTheTable(Card card) {
        cardsTable.add(card);
    }

    public Card getCurrentCardOnTheTable() {
        if (cardsTable.isEmpty()) {
            System.err.println("Error: No hay cartas en la mesa");
            return null;
        }
        return cardsTable.get(cardsTable.size() - 1);
    }

    public ArrayList<Card> getCardsTable() {
        return cardsTable;
    }

}