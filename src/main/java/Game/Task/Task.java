package Game.Task;

public interface Task {
    String getName();
    boolean isCompleted();
    void startTask();
    void updateTask(double progress);
    void completeTask();
}
