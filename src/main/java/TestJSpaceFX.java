import Player.Player;
import Player.HostPlayer;
import Player.ClientPlayer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.ColorAdapter;

import java.util.*;


public class TestJSpaceFX extends Application {
    final private int WIDTH = 500;
    final private int HEIGHT = 500;
    private static Player me = new ClientPlayer(); // assume player is not hosting

    public static void main(String[] args) throws InterruptedException {
        ColorAdapter.init();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Are you hosting? (y/n)");
        // Player is hosting
        if (Objects.equals(scanner.nextLine(), "y")){
            me = new HostPlayer();
            System.out.println("You are hosting.");
        }

        System.out.println("Please insert URI of the server (enter for default):");
        String uri = scanner.nextLine();
        if (uri.isEmpty()){
            uri = "tcp://localhost:9002/?keep";
        }
        me.addUri(uri);
        System.out.println("the uri is " + me.getURI());

        System.out.println("Please insert your name:");
        String name = scanner.nextLine();

        boolean b = me.tryJoin(name);;
        while (!b){
            System.out.println("This name is already taken, please choose another name:");
            name = scanner.nextLine();
            b = me.tryJoin(name);
        }

        System.out.println("Get ready!");
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Among Peers");

        Scene scene = new Scene(me.getView(), WIDTH, HEIGHT);

        me.run();

        primaryStage.setScene(scene);
        primaryStage.show();

    }
}