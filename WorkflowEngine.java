
public class WorkflowEngine {
    private DependencyChecker checker;

    public WorkflowEngine() {
        checker = new DependencyChecker();
    }

    public void executeWorkflow(Task[] tasks, int taskCount) {
        System.out.println("\n===== WORKFLOW EXECUTION =====");

        for (int i = 0; i < taskCount; i++) {
            Task currentTask = tasks[i];

            if (checker.isDependencyCompleted(
                    currentTask, tasks, taskCount)) {

                currentTask.setStatus("Completed");

                System.out.println(
                        "Task " + currentTask.getTaskId()
                                + " completed successfully.");

            } else {
                System.out.println(
                        "Task " + currentTask.getTaskId()
                                + " skipped: dependency not completed.");
            }
        }
    }
}
