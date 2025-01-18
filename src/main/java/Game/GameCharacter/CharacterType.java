package Game.GameCharacter;

import javafx.scene.paint.Color;

public enum CharacterType {
    RED(Color.RED),
    BLACK(Color.BLACK),
    BLUE(Color.BLUE),
    GREEN(Color.GREEN),
    ORANGE(Color.ORANGE),
    GREY(Color.GREY),
    WHITE(Color.WHITE),
    BROWN(Color.BROWN),
    YELLOW(Color.YELLOW),
    PURPLE(Color.PURPLE),
    CYAN(Color.CYAN);

    final Color color;

    CharacterType(Color color){
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
