package Game.Interactables.Task;

import Game.Player.PlayerInfo;
import org.jspace.Space;

public class ScanTask implements Task{

    private final TaskType taskType;
    private boolean completed;
    private double progress;

    public ScanTask() {
        this.taskType = TaskType.WIRING;
        this.completed = false;
        this.progress = 0.0;
    }

    @Override
    public String getName() {
        return taskType.getDisplayName();
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void startTask() {

    }

    @Override
    public void updateTask(double progress) {

    }

    @Override
    public void completeTask() {
        this.completed = true;
    }

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
        return !info.isImposter;
    }
}
