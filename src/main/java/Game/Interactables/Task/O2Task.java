package Game.Interactables.Task;

import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Player.PlayerView;
import org.jspace.Space;

public class O2Task extends Task {

    public O2Task() {
        super(TaskType.CLEAN_O2_FILTER);
    }

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
        return !info.isImposter;
    }

}
