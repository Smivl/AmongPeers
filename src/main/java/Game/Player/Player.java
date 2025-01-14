package Game.Player;

import Game.GameCharacter.GameCharacter;
import Game.GameMap.GameMap;
import org.jspace.Space;

import java.net.URI;

public class Player {
    private GameCharacter character;
    private PlayerView view;
    private String name;

    public PlayerView getPlayerView() { return view; }
    public GameCharacter getCharacter() { return character; }

    public Player(String name, Space serverSpace, URI uri){

        this.view = new PlayerView();

        this.name = name;
        this.character = new GameCharacter(name);

        character.setServerSpace(serverSpace);
        character.setServerURI(uri);

        // Get player to join server and then initialize the player
        character.join();
        character.init();
    }


    public void onUpdate(double delta, GameMap map){
        character.onUpdate(delta);
    }
}
