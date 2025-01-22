package Menu;

import Game.GameController;
import Game.GameOverMenu;
import Server.Server;
import Server.ServerScan;
import Server.Response;
import Server.ServerBroadcast;
import javafx.application.Platform;
import javafx.scene.Scene;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;


public class MenuManager {
    private final Scene scene;
    private Thread gameWaitingThread;
    private Server server;
    private GameController gameController;
    private MainMenu mainMenu;
    private HostMenu hostMenu;
    private JoinMenu joinMenu;
    private LobbyMenu lobbyMenu;

    public MenuManager(Scene scene) {
        this.scene = scene;
        initializeMenus();
    }

    private void initializeMenus() {
        mainMenu = new MainMenu(this);
        hostMenu = new HostMenu(this);
        joinMenu = new JoinMenu(this);
        lobbyMenu = new LobbyMenu(this);

        mainMenu.getStyleClass().add("menu-box");
        hostMenu.getStyleClass().add("menu-box");
        joinMenu.getStyleClass().add("menu-box");
        lobbyMenu.getStyleClass().add("menu-box");
    }

    public void transitionToMainMenu() {
        if(!Objects.isNull(gameController)){
            gameController.leave();
            gameController = null;
        }

        if (!Objects.isNull(server)){ // any server created is killed
            ServerBroadcast.stopServer();
            server.shutdown();
            server = null;
        }
        if (!Objects.isNull(gameWaitingThread)){ // if were waiting to start game, abort
            gameWaitingThread.interrupt();
            gameWaitingThread = null;
        }
        scene.setRoot(mainMenu);
    }

    public void transitionToHostMenu() {
        scene.setRoot(hostMenu);
    }

    public void transitionToJoinMenu() {
        scene.setRoot(joinMenu);
        joinMenu.hideErrorMessage();

        new Thread(joinMenu::refreshServers).start();
    }

    public void transitionToLobbyMenu(boolean isHosting, String name, String IP, int Port) {
        try {
            URI serverURI = new URI("tcp://" + IP.replace("/", "") + ":" + Port + "/?keep");
            System.out.println(serverURI);

            if (isHosting) {
                new Thread(() -> ServerBroadcast.startServer(name)).start();

                Space serverSpace = new SequentialSpace();
                server = new Server(serverURI, serverSpace);

                server.start();
            }

            gameController = new GameController(name, serverURI, this::transitionToMainMenu);
            System.out.println(serverURI);
            Response serverResponse = gameController.join();
            if (serverResponse.isSuccesful()){ // always successful for host
                lobbyMenu.setHosting(isHosting);
                lobbyMenu.setURI(serverURI.getHost(), serverURI.getPort());
                scene.setRoot(lobbyMenu);
                gameWaitingThread = new Thread(() ->
                {
                    System.out.println("waiting");
                    try {
                        gameController.waitForStart(scene);
                    } catch (InterruptedException e){
                        scene.setRoot(mainMenu);
                    }
                });
                gameWaitingThread.start();
            } else { // must be in join menu
                System.out.println(serverResponse.getErrorMessage());
                joinMenu.displayErrorMessage(serverResponse.getErrorMessage());
            }
        } catch (URISyntaxException e){
            e.printStackTrace(System.out);
            joinMenu.displayErrorMessage("Could not parse address");
        }
    }

    public void startGame() {
        // DONT STOP THIS BECAUSE WE NEED TO LET OTHERS KNOW THAT THIS IS BEING USED
        //ServerBroadcast.stopServer();
        server.startGame();
    }

    public Server getServer() {
        return server;
    }

    public GameController getGameController() {
        return gameController;
    }
}
