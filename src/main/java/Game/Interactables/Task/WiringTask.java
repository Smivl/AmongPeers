package Game.Interactables.Task;

import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Player.PlayerView;
import org.jspace.Space;

public class WiringTask extends Task{

    private double completedSubTasks;
    private final double totalSubTasks = 4;

    public WiringTask() {
        super(TaskType.WIRING);
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
        TaskView taskView = new TaskView("Fixing wiring...", 5, () -> { stopInteraction(player); });
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
            player.completeSubTask(getTaskType());
            player.getPlayerView().updateTaskProgress(getTaskType(), completedSubTasks/totalSubTasks);
        }
    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return !info.isImposter;
    }

}
