
public class DependencyChecker {

    public boolean isDependencyCompleted(
            Task task, Task[] tasks, int taskCount) {

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

    public boolean isDependencyValid(
            Task task, Task[] tasks, int taskCount) {

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

    public boolean hasCircularDependency(
            Task[] tasks, int taskCount) {

        for (int i = 0; i < taskCount; i++) {
            int currentId = tasks[i].getTaskId();
            int dependencyId = tasks[i].getDependencyId();
            int steps = 0;

            while (dependencyId != -1) {
                if (dependencyId == currentId) {
                    return true;
                }

                int nextDependency = -1;

                for (int j = 0; j < taskCount; j++) {
                    if (tasks[j].getTaskId() == dependencyId) {
                        nextDependency = tasks[j].getDependencyId();
                        break;
                    }
                }

                if (nextDependency == -1) {
                    break;
                }

                dependencyId = nextDependency;
                steps++;

                if (steps > taskCount) {
                    return true;
                }
            }
        }

        return false;
    }
}
