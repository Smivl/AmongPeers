package Server;

import Game.Player.PlayerInfo;
import javafx.scene.paint.Color;
import org.jspace.*;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private Map<String, Space> playerSpaces = new HashMap<>();
    private Map<String, PlayerInfo> playerInfos = new HashMap<>();

    private SpaceRepository spaceRepository = new SpaceRepository();
    private Space serverSpace;
    private URI serverURI;

    private Thread serverThread;

    public Server(URI uri, Space serverSpace){
        this.serverURI = uri;
        this.serverSpace = serverSpace;
        spaceRepository.add("server", serverSpace);
        spaceRepository.addGate(serverURI);
    }

    public void start(){
        serverThread = new Thread(this::server);
        serverThread.start();

        System.out.println("Server now online:\naddress:" + serverURI);
    }

    public void shutdown(){
        serverThread.interrupt();
        try{
            serverThread.join();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        System.out.println("Server offline.");
    }

    private void server(){
        while (true){
            try {
                Object[] request = serverSpace.get(new FormalField(Request.class));
                switch ((Request) request[0]){
                    case JOIN: {
                        handleJoinRequest();
                        break;
                    }
                    case LEAVE:
                    case KICK: {
                        System.out.println("Unsupported request!");
                        break;
                    }
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    private void handleJoinRequest() {
        try {
            // read request
            String nameRequest = (String)(serverSpace.get(new ActualField(Request.JOIN), new FormalField(String.class))[1]);

            if (playerSpaces.containsKey(nameRequest)){
                // player name exists already
                serverSpace.put(nameRequest, Response.CONFLICT);
            }
            else {
                // player name does not exist

                // create new channel
                Space privateChannel = new SequentialSpace();
                playerSpaces.put(nameRequest, privateChannel);
                spaceRepository.add(nameRequest, privateChannel);

                new Thread(() -> handlePlayer(nameRequest)).start();

                // accept request
                serverSpace.put(nameRequest, Response.ACCEPTED);

                initializePlayer(nameRequest);
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void initializePlayer(String nameRequest){
        try{

            PlayerInfo newPlayerInfo = new PlayerInfo(Color.GREEN, new double[]{4900, 1500}, new double[]{0,0});
            playerSpaces.get(nameRequest).put(ServerUpdate.PLAYER_INIT, newPlayerInfo);

            for (String playerName : playerInfos.keySet()){
                playerSpaces.get(playerName).put(ServerUpdate.PLAYER_JOINED);
                playerSpaces.get(playerName).put(ServerUpdate.PLAYER_JOINED, nameRequest, newPlayerInfo);

                playerSpaces.get(nameRequest).put(ServerUpdate.PLAYER_JOINED);
                playerSpaces.get(nameRequest).put(ServerUpdate.PLAYER_JOINED, playerName, playerInfos.get(playerName));
            }

            playerInfos.put(nameRequest, newPlayerInfo);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void handlePlayer(String playerName) {
        while (true){
            try{

                /*
                    Note:
                        It could be a good idea to run broadcastUpdate after the switch case (to show every player the new update).
                        Then use the switch case to perform server actions based on the update (for example to handle leaving of a player).
                 */

                Object[] update = playerSpaces.get(playerName).get(new ActualField("SERVER"), new FormalField(ClientUpdate.class));
                switch ((ClientUpdate) update[1]){
                    case POSITION:{
                        broadcastClientUpdate(playerName);
                        break;
                    }
                }


            }catch (Exception e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    // todo: Make this more modular so it can broadcast all types of updates
    private void broadcastClientUpdate(String playerName){
        try {
            Object[] update = playerSpaces.get(playerName).get(new ActualField(ClientUpdate.POSITION), new FormalField(Object.class), new FormalField(Object.class));

            playerInfos.get(playerName).position = (double[]) update[1];
            playerInfos.get(playerName).velocity = (double[]) update[2];

            for(String name : playerSpaces.keySet()){
                if (!name.equals(playerName)){
                    playerSpaces.get(name).put(ServerUpdate.POSITION);
                    playerSpaces.get(name).put(ServerUpdate.POSITION, playerName, update[1], update[2]);
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}
