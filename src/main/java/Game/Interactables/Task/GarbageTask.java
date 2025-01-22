package Game.Interactables.Task;

import Game.Player.Player;
import Game.Player.PlayerInfo;
import org.jspace.Space;

public class GarbageTask extends Task{

    public GarbageTask() {
        super(TaskType.EMPTY_GARBAGE);
    }

    @Override
    public void setPlayerName(String name) {

    }

    @Override
    public void setPlayerSpace(Space playerSpace) {

    }

    @Override
    public void interact(Player player) {
        TaskView taskView = new TaskView("Emptying garbage...", 5, () -> { stopInteraction(player); });
        player.getPlayerView().setCenter(taskView);
        player.setInputLocked(true);
    }

    @Override
    public void stopInteraction(Player player) {
        player.setInputLocked(false);
        player.getPlayerView().setCenter(null);
        setCompleted(true);
        player.completeTask(getTaskType());
    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return !info.isImposter;
    }

}
