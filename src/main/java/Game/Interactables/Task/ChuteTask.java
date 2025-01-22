package Game.Interactables.Task;

import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Player.PlayerView;
import org.jspace.Space;

public class ChuteTask extends Task {

    private double completedSubTasks;
    private final double totalSubTasks = 2;

    public ChuteTask() {
        super(TaskType.EMPTY_CHUTE);
        this.completedSubTasks = 0.0;
    }

    @Override
    public void setPlayerName(String name) {

    }

    @Override
    public void setPlayerSpace(Space playerSpace) {

    }

    @Override
    public void interact(Player player) {
        TaskView taskView = new TaskView("Emptying chute...", 2, () -> { stopInteraction(player); });
        player.getPlayerView().setCenter(taskView);
        player.setInputLocked(true);
    }

    @Override
    public void stopInteraction(Player player) {
        player.setInputLocked(false);
        player.getPlayerView().setCenter(null);
        if(++completedSubTasks == totalSubTasks) {
            setCompleted(true);
            player.completeTask(getTaskType());
        }else{
            player.getPlayerView().updateTaskProgress(getTaskType(), completedSubTasks/totalSubTasks);
            player.completeSubTask(getTaskType());
        }
    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return !info.isImposter;
    }

}
