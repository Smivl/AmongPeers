package Player;

import PlayerView.PlayerView;
import javafx.scene.paint.Color;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import utils.Dummy;
import utils.PlayerInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Player implements Runnable{
    // control stuff
    protected Space privateSpace;
    protected Space serverSpace;

    protected String name;
    protected URI myURI;

    // view stuff
    protected PlayerView view;

    abstract public void addUri(String uri);

    public boolean tryJoin(String name) {
        try {
            serverSpace.put("JoinRequest");
            serverSpace.put("JoinRequest", name);
            Object[] t = serverSpace.get(new ActualField(name), new FormalField(String.class));
            switch ((String) t[1]) {
                case ("ACCEPTED"): {
                    this.name = name;
                    System.out.println(myURI);
                    System.out.println(myURI.getScheme() + "://" +
                            myURI.getHost() + ":" +
                            myURI.getPort() + "/" +
                            name + "?" +
                            myURI.getQuery()); // TODO: DELETE AS IS DEBUG
                    privateSpace = new RemoteSpace(
                            myURI.getScheme() + "://" +
                                    myURI.getHost() + ":" +
                                    myURI.getPort() + "/" +
                                    name + "?" +
                                    myURI.getQuery()
                    );
                    initializeView();
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

    @Override
    public void run(){
        System.out.println(name + " is Running");
        while (true){
            try {
                Object[] t = privateSpace.get(new FormalField(String.class));
                switch ((String)t[0]){
                    case ("NewPlayer") : {
                        handleNewPlayer();
                    }
                    case ("NewPosition") : {
                        handleNewPosition();
                    }
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
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
            Object[] t = privateSpace.get(new FormalField(String.class), new FormalField(Object.class));
            view.update((String) t[0], (double[]) t[1]);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public PlayerView getView() {
        return view;
    }

    protected void initializeView() {
        try {
            // get own info
            privateSpace.put(1);
            privateSpace.get(new ActualField(1));
            List<Double> dummyList = new ArrayList<>(Arrays.asList(1.0,2.0,3.0));

            // BUG IN THE LINE BELOW: problem with color class
            privateSpace.put(new Color(1,1,1,1));
            privateSpace.get(new FormalField(Color.class));
            System.out.println(1);


            Object[] infoTuple = privateSpace.get(new ActualField("PlayerInfo"), new FormalField(PlayerInfo.class));
            PlayerInfo info = (PlayerInfo) infoTuple[1];
            view = new PlayerView(privateSpace);
            view.getSprite().setFill(info.color);
            view.getSprite().move(info.position);

            // get other players info
            privateSpace.get(new ActualField("OtherPlayersStart"));
            while (true){
                String message = (String)(privateSpace.get(new FormalField(String.class))[0]);
                switch (message){
                    case "NewPlayer" : handleNewPlayer();
                    case "OtherPlayersEnd" : break;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public URI getURI() {
        return myURI;
    }
}
