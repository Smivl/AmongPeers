package Game.Interactables.Sabotage;

import Game.Interactables.Interactable;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import org.jspace.Space;

public class OxygenSabotage extends Sabotage {
    @Override
    public void setPlayerName(String name) {

    }

    @Override
    public void setPlayerSpace(Space playerSpace) {

    }

    @Override
    public void interact(Player player) {

    }

    @Override
    public void stopInteraction(Player player) {

    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isAlive;
    }

}
