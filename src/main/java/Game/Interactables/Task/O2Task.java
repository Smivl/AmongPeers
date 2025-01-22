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
    public void interact(Player player) {
        TaskView taskView = new TaskView("Cleaning O2 Filter...", 6, () -> { stopInteraction(player); });
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
