package Game.Interactables.Task;

import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Player.PlayerView;
import org.jspace.Space;

public class ChuteTask extends Task {

    private double progress;

    public ChuteTask() {
        super(TaskType.EMPTY_CHUTE);
        this.progress = 0.0;
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
