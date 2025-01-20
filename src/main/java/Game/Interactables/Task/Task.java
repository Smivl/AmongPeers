package Game.Interactables.Task;

import Game.Interactables.Interactable;

public abstract class Task implements Interactable {

    private final TaskType taskType;
    private boolean completed;

    protected Task(TaskType taskType) {
        this.taskType = taskType;
        this.completed = false;
    }

    protected TaskType getTaskType(){
        return taskType;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean value){
        completed = value;
    }


}
