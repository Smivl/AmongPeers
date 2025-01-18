package Menu;

import Game.GameController;
import Server.Server;
import Server.Response;
import javafx.scene.Scene;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.net.URI;
import java.net.URISyntaxException;
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
    }

    public void transitionToMainMenu() {
        if (!Objects.isNull(server)){ // any server created is killed
            server.shutdown();
        }
        if (!Objects.isNull(gameWaitingThread)){ // if were waiting to start game, abort
            gameWaitingThread.interrupt();
        }
        scene.setRoot(mainMenu);
    }

    public void transitionToHostMenu() {
        scene.setRoot(hostMenu);
    }

    public void transitionToJoinMenu() {
        scene.setRoot(joinMenu);
        joinMenu.hideErrorMessage();
    }

    public void transitionToLobbyMenu(boolean isHosting, String name, String IP) {
        try {
            URI serverURI = new URI("tcp://" + IP + ":9001/?keep");
            if (isHosting) {
                Space serverSpace = new SequentialSpace();
                server = new Server(serverURI, serverSpace);

                server.start();
            }

            gameController = new GameController(name, serverURI);
            Response serverResponse = gameController.join();
            if (serverResponse.isSuccesful()){ // always successful for host
                lobbyMenu.setHosting(isHosting);
                scene.setRoot(lobbyMenu);
                gameWaitingThread = new Thread(() ->
                {
                    System.out.println("waiting");
                    gameController.waitForStart(scene);
                });
                gameWaitingThread.start();
            } else { // must be in join menu
                System.out.println(serverResponse);
                joinMenu.displayErrorMessage(serverResponse.getErrorMessage());
            }
        } catch (URISyntaxException e){
            e.printStackTrace(System.out);
            joinMenu.displayErrorMessage("Could not parse address");
        }
    }

    public void startGame() {
        server.startGame();
    }
}
