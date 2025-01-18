package Game.Interactables.Task;

import Game.Interactables.Interactable;

public interface Task extends Interactable {
    String getName();
    boolean isCompleted();
    void startTask();
    void updateTask(double progress);
    void completeTask();
}
