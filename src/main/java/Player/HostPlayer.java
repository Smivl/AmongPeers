package Player;

import PlayerView.PlayerView;
import javafx.scene.paint.Color;
import org.jspace.*;
import utils.PlayerInfo;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HostPlayer extends Player{
    private Random random = new Random();
    private SpaceRepository repo = new SpaceRepository();
    private Map<String, Space> playerSpaces = new HashMap<>();
    private Map<String, PlayerInfo> playerInfos = new HashMap<>();


    { // initialization
        serverSpace = new SequentialSpace();
        repo.add("server", serverSpace);
    }

    public HostPlayer(){
        (new Thread(this::runServer)).start();
    }

    public void addUri(String uri) {
        try {
            myURI = new URI(uri);
            repo.addGate(uri);
            System.out.println(repo.get("server"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runServer(){
        System.out.println(name + "is running the server");
        while (true){
            try {
                Object[] t = serverSpace.get(new FormalField(String.class));
                switch ((String)t[0]){
                    case ("JoinRequest") : {
                        handleJoinRequest();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void handleJoinRequest() {
        try {
            // read request
            String nameRequest = (String)(serverSpace.get(new ActualField("JoinRequest"), new FormalField(String.class))[1]);

            if (playerSpaces.containsKey(nameRequest)){ // player name exists already
                serverSpace.put(nameRequest, "REJECTED");
            }
            else { // player name does not exist
                // create new channel
                Space privateChannel = new SequentialSpace();
                playerSpaces.put(nameRequest, privateChannel);
                repo.add(nameRequest, privateChannel);

                // accept request
                serverSpace.put(nameRequest, "ACCEPTED");

                initializePlayer(nameRequest);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initializePlayer(String nameRequest){
        try {
        // initialize random data for the player
            Color randomColor = new Color(random.nextDouble(),random.nextDouble(),random.nextDouble(), 1.0);
            double[] randomPosition = new double[]{random.nextDouble()*500, random.nextDouble()*500};
            PlayerInfo newPlayerInfo = new PlayerInfo(randomColor, randomPosition);

        // inform the player of its data and other players
            playerSpaces.get(nameRequest).put( "PlayerInfo", newPlayerInfo);
            playerSpaces.get(nameRequest).put("OtherPlayersStart"); // inform to start a loop

            System.out.println("Current players: " + playerInfos.keySet());

            for (String playerName : playerInfos.keySet()){
                System.out.println("Dealing with " + playerName);

                // tell existing players there is a new player
                playerSpaces.get(playerName).put("NewPlayer");
                playerSpaces.get(playerName).put("NewPlayer", nameRequest, newPlayerInfo);

                // tell new player there are existing players
                playerSpaces.get(nameRequest).put("NewPlayer");
                playerSpaces.get(nameRequest).put("NewPlayer", playerName, playerInfos.get(playerName));
            }

            // update players data
            playerInfos.put(nameRequest, newPlayerInfo);

            System.out.println("New players: " + playerInfos.keySet());

            playerSpaces.get(nameRequest).put("OtherPlayersEnd"); // end the loop

        } catch (Exception e ){
            e.printStackTrace();
        }
    }
}