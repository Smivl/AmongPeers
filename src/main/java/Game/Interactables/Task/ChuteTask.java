package Game.Interactables.Task;

import Game.Player.PlayerInfo;
import org.jspace.Space;

public class ChuteTask implements Task {
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
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public void startTask() {

    }

    @Override
    public void updateTask(double progress) {

    }

    @Override
    public void completeTask() {

    }
}
