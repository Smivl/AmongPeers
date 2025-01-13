package PlayerView;

import javafx.scene.Group;
import org.jspace.Space;
import Game.Player.PlayerInfo;

import java.util.HashMap;
import java.util.Map;

public class PlayerView extends Group {

    private Sprite sprite = new Sprite();
    private Map<String, Sprite> otherPlayers = new HashMap<>();
    // scene = getScene();
    {
        this.getChildren().add(sprite);
    }

    public void initialize(Space positionSpace){
        sprite.setAsMain(positionSpace);
    }

    public void update(String playerName, double[] position){
        otherPlayers.get(playerName).move(position);
    }

    public void addPlayer(String name, PlayerInfo info) {
        Sprite newPlayer = new Sprite();

        // add to collections
        this.getChildren().add(newPlayer);
        otherPlayers.put(name, newPlayer);

        // initialize sprite with properties
        newPlayer.setFill(info.color);
        newPlayer.move(info.position);
    }

    public Sprite getSprite() {
        return sprite;
    }
}
