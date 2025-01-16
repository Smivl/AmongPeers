package Game.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TaskFactory {

    private static final Map<TaskType, Supplier<Task>> TASK_CREATORS = new HashMap<>();

    static {
        TASK_CREATORS.put(TaskType.UPLOAD_DATA, UploadTask::new);
        TASK_CREATORS.put(TaskType.WIRING, WiringTask::new);
        // etc.
    }

    public static Task createTask(TaskType type) {
        return TASK_CREATORS.get(type).get();
    }
}