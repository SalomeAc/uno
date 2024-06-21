package org.example.eiscuno.model.card;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Represents a card in the Uno game.
 */
public class Card {
    public Image getImage;
    private String url;
    private String value;
    private String color;
    private Image image;
    private ImageView cardImageView;
    private ImageView cardImageViewMachine;
    private ImageView imageViewMachine;

    /**
     * Constructs a Card with the specified image URL and name.
     *
     * @param url   the URL of the card image
     * @param value of the card
     */
    public Card(String url, String value, String color) {
        this.url = url;
        this.value = value;
        this.color = color;
        this.image = new Image(String.valueOf(getClass().getResource(url)));
        this.cardImageView = createCardImageView();
    }


    public ImageView getCardImageViewMachine() {
        if (this.imageViewMachine == null) {
            this.imageViewMachine = new ImageView(new Image(getClass().getResourceAsStream("/org/example/eiscuno/cards-uno/card_uno.png")));
            this.imageViewMachine.setFitHeight(90);
            this.imageViewMachine.setFitWidth(70);
            this.imageViewMachine.setPreserveRatio(true);
        }
        return this.imageViewMachine;
    }

    @Override
    public String toString() {
        return value + " de " + color;
    }

    /**
     * Creates and configures the ImageView for the card.
     *
     * @return the configured ImageView of the card
     */
    private ImageView createCardImageView() {
        ImageView card = new ImageView(this.image);
        card.setY(16);
        card.setFitHeight(90);
        card.setFitWidth(70);
        return card;
    }


    public Card(String imagePath) {
        this.cardImageViewMachine = new ImageView(new Image(getClass().getResourceAsStream("/org/example/eiscuno/cards-uno/card_uno.png")));
    }

    public ImageView getCardImageView() {
        return this.cardImageViewMachine;
    }

    /**
     * Gets the ImageView representation of the card.
     *
     * @return the ImageView of the card
     */
    public ImageView getCard() {
        return cardImageView;
    }

    public boolean isDrawTwo() {
        return "+2".equals(value);
    }

    public boolean isDrawFour() {
        return "+4".equals(value);
    }

    /**
     * Gets the image of the card.
     *
     * @return the Image of the card
     */
    public Image getImage() {
        return image;
    }

    public String getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }
    public String getUrl(){
        return url;
    }
}
