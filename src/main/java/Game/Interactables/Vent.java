package Game.Interactables;

import Game.Player.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class Vent implements Interactable{

    List<Vent> neighbors = new ArrayList<>();

    @Override
    public void interact() {
    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isImposter && info.isAlive;
    }
}
