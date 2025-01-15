package Game.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private List<Task> tasks;

    public TaskManager() {
        this.tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void startTask(Task task) {
        task.startTask();
        // Possibly notify the UI: "Task X started"
    }

    public void updateTask(Task task, double progress) {
        task.updateTask(progress);
        // Possibly notify the UI: "Task X updated"
    }

    public void completeTask(Task task) {
        task.completeTask();
        // Possibly notify the UI: "Task X completed"
    }
}
