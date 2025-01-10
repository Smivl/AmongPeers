package Player;

import org.jspace.RemoteSpace;

import java.net.URI;

public class ClientPlayer extends Player{

    @Override
    public void addUri(String uri) {
        try {
            myURI = new URI(uri);
            serverSpace = new RemoteSpace(
                            myURI.getScheme() + "://" +
                                    myURI.getHost() + ":" +
                                    myURI.getPort() + "/" +
                                    "server" + "?" +
                                    myURI.getQuery()
                    );

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }



}
