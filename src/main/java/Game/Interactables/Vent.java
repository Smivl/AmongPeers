package Game.Interactables;

import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Player.PlayerView;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.List;

public class Vent implements Interactable{

    List<Vent> neighbors = new ArrayList<>();

    @Override
    public void setPlayerName(String name) {
    }

    @Override
    public void setPlayerSpace(Space playerSpace) {
    }

    @Override
    public void interact(Player view) {
    }

    @Override
    public void stopInteraction(Player view) {

    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isImposter && info.isAlive;
    }
}
