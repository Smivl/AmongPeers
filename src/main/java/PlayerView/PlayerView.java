package PlayerView;

import javafx.scene.Group;
import org.jspace.Space;
import utils.PlayerInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlayerView extends Group {

    private Sprite sprite = new Sprite();
    private Map<String, Sprite> otherPlayers = new HashMap<>();
    // scene = getScene();
    {
        this.getChildren().add(sprite);
    }

    public PlayerView(Space positionSpace){
        sprite.setAsMain(positionSpace);
    }

    public void update(String playerName, double[] position){
        otherPlayers.get(playerName).move(position);
    }

    public void addPlayer(String name, PlayerInfo info) {
        Sprite newPlayer = new Sprite();
        System.out.println("New player's color " + info.color);
        newPlayer.setFill(info.color);
        System.out.println("New player's position " + Arrays.toString(info.position));
        newPlayer.move(info.position);
        this.getChildren().add(newPlayer);
        otherPlayers.put(name, newPlayer);
    }

    public Sprite getSprite() {
        return sprite;
    }
}
