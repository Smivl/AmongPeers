package Game.Player;

import Game.GameCharacter.GameCharacter;
import Game.GameCharacter.GameCharacterView;
import Game.GameMap.GameMap;
import Server.Request;
import Server.Response;
import Server.ServerUpdate;
import javafx.scene.input.KeyEvent;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.net.URI;
import java.security.Key;

public class Player {
    private GameCharacter character;

    private PlayerView view;
    private PlayerInfo playerInfo;
    private String name;

    private Space playerSpace;
    private Space serverSpace;

    private URI serverURI;


    public PlayerInfo getInfo() { return playerInfo; }
    public PlayerView getPlayerView() { return view; }
    public GameCharacter getCharacter() { return character; }



    public Player(String name, Space serverSpace, URI serverURI){

        this.view = new PlayerView();

        this.name = name;

        this.serverSpace = serverSpace;
        this.serverURI = serverURI;
    }

    public void join() {
        try{
            serverSpace.put(Request.JOIN);
            serverSpace.put(Request.JOIN, name);

            Object[] response = serverSpace.get(new ActualField(name), new FormalField(Response.class));

            switch ((Response) response[1]){
                case SUCCESS:
                case ACCEPTED:{
                    playerSpace = new RemoteSpace(
                            serverURI.getScheme() + "://" +
                                    serverURI.getHost() + ":" +
                                    serverURI.getPort() + "/" +
                                    name + "?" +
                                    serverURI.getQuery()
                    );

                    break;
                }
                case CONFLICT:{
                    System.out.println("Player name already exists! Pick another name.");
                    break;
                }
                case PERMISSION_DENIED:{
                    System.out.println("Permission denied to join server!");
                    break;
                }
                case ERROR:
                case FAILURE:{
                    System.out.println("Server does not exist!");
                    break;
                }
            }


        }catch (Exception e){
            System.out.println("Error in join");
            System.out.println(e.getMessage());
        }

    }

    // Call init before starting after join
    public void init(){
        try{
            Object[] playerInfo = playerSpace.get(new ActualField(ServerUpdate.PLAYER_INIT), new FormalField(PlayerInfo.class));
            PlayerInfo info = (PlayerInfo) playerInfo[1];

            this.playerInfo = info;

            this.character = new GameCharacter(name, info);
            this.character.setPlayerSpace(playerSpace);

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void onKilled() {
        playerInfo.isAlive = false;
        character.onKilled();
    }

    public void onUpdate(double delta, GameMap map){
        character.onUpdate(delta);
    }

    public void handleKeyPressed(KeyEvent event){
        this.character.handleKeyPressed(event);
    }

    public void handleKeyReleased(KeyEvent event){
        this.character.handleKeyReleased(event);
    }

}
