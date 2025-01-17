package Player;

import PlayerView.PlayerView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import Game.Player.PlayerInfo;

import java.net.URI;

public abstract class Player implements Runnable{
    // control stuff
    protected Space privateSpace;
    protected Space serverSpace;

    protected String name;
    protected URI myURI;

    // view stuff
    protected PlayerView view = new PlayerView();

    abstract public void addUri(String uri);

    // try to join a game with username. Returns false if fails
    public boolean tryJoin(String name) {
        try {
            serverSpace.put("JoinRequest");
            serverSpace.put("JoinRequest", name);
            Object[] t = serverSpace.get(new ActualField(name), new FormalField(String.class));
            switch ((String) t[1]) {
                case ("ACCEPTED"): {
                    this.name = name;
                    privateSpace = new RemoteSpace(
                            myURI.getScheme() + "://" +
                                    myURI.getHost() + ":" +
                                    myURI.getPort() + "/" +
                                    name + "?" +
                                    myURI.getQuery()
                    );
                    return true;
                }
                case ("REJECTED"): {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();;
        }
        return false;
    }

    // private channel process
    public void run() {
        // Scene has been initialized. Inform the view
        initializeView();

        // JavaFX thread syntax
        Task<Void> tupleSpaceTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    Object[] t = privateSpace.get(new FormalField(String.class));
                    if (((String) t[0]).equals("NewPlayer")){
                        Platform.runLater(Player.this::handleNewPlayer);
                    } else if (((String) t[0]).equals("NewPosition")){
                        Platform.runLater(Player.this::handleNewPosition);
                    }
                }
            }
        };

        new Thread(tupleSpaceTask).start();
    }


    protected void handleNewPlayer(){
        try {
            Object[] t = privateSpace.get(new ActualField("NewPlayer"), new FormalField(String.class), new FormalField(PlayerInfo.class));
            view.addPlayer((String)t[1], (PlayerInfo) t[2]);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleNewPosition(){
        try {
            Object[] t = privateSpace.get(new ActualField("NewPosition"), new FormalField(String.class), new FormalField(Object.class), new FormalField(Object.class));
            view.update((String) t[1], (double[]) t[2]);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public PlayerView getView() {
        return view;
    }

    protected void initializeView() {
        try {
            view.initialize(privateSpace);
            Object[] infoTuple = privateSpace.get(new ActualField("PlayerInfo"), new FormalField(PlayerInfo.class));
            PlayerInfo info = (PlayerInfo) infoTuple[1];
            view.getSprite().setFill(Color.RED);
            view.getSprite().move(info.position);

            // get other players info
            privateSpace.get(new ActualField("OtherPlayersStart"));
            System.out.println("Getting other players...");
            while (true){
                String message = (String)(privateSpace.get(new FormalField(String.class))[0]);
                if (message.equals("NewPlayer")) {
                    handleNewPlayer();
                } else if (message.equals("OtherPlayersEnd")) {
                    break;
                }
            }
            outs:
            System.out.println("Got other players!");

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public URI getURI() {
        return myURI;
    }
}
