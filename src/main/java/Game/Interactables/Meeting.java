package Game.Interactables;

import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Player.PlayerView;
import Server.ClientUpdate;
import org.jspace.Space;

public class Meeting implements Interactable {
    Space playerSpace;
    String playerName;

    @Override
    public void setPlayerName(String name) {
        this.playerName = name;
    }

    @Override
    public void setPlayerSpace(Space playerSpace) {
        this.playerSpace = playerSpace;
    }

    @Override
    public void interact(Player view) {
        System.out.println("Meeting Called!");

        try {
            playerSpace.put(ClientUpdate.MEETING);
            playerSpace.put(ClientUpdate.MEETING, playerName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopInteraction(Player view) {

    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isAlive;
    }
}
