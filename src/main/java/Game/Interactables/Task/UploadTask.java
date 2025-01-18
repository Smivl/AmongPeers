package Game.Interactables.Task;

import Game.Player.PlayerInfo;

public class UploadTask implements Task {
    private final TaskType taskType;
    private boolean completed;
    private double progress;

    public UploadTask() {
        this.taskType = TaskType.UPLOAD_DATA;
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
    public void interact() {
    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return !info.isImposter;
    }


}
