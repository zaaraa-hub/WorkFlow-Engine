
public class DependencyChecker {

    public boolean isDependencyCompleted(Task task, Task[] tasks, int taskCount) {
        int dependencyId = task.getDependencyId();

        if (dependencyId == -1) {
            return true;
        }

        for (int i = 0; i < taskCount; i++) {
            if (tasks[i].getTaskId() == dependencyId) {
                return tasks[i].getStatus().equals("Completed");
            }
        }

        return false;
    }

    public boolean isDependencyValid(Task task, Task[] tasks, int taskCount) {
        int dependencyId = task.getDependencyId();

        if (dependencyId == -1) {
            return true;
        }

        if (dependencyId == task.getTaskId()) {
            return false;
        }

        for (int i = 0; i < taskCount; i++) {
            if (tasks[i].getTaskId() == dependencyId) {
                return true;
            }
        }

        return false;
    }
}
