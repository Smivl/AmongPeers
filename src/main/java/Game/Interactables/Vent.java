package Game.Interactables;

import Game.Player.PlayerInfo;
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
    public void interact() {
    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isImposter && info.isAlive;
    }
}
