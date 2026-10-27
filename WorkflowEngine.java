
public class WorkflowEngine {
    private DependencyChecker checker;
    private ExecutionLogger logger;

    public WorkflowEngine() {
        checker = new DependencyChecker();
        logger = new ExecutionLogger();
    }

    public void executeWorkflow(Task[] tasks, int taskCount) {
        System.out.println("\n===== WORKFLOW EXECUTION =====");

        logger.log("Workflow execution started.");

        for (int i = 0; i < taskCount; i++) {
            Task currentTask = tasks[i];

            if (currentTask.getStatus().equals("Failed")) {
                System.out.println("Task " + currentTask.getTaskId()
                        + " has failed.");
                logger.log("Task " + currentTask.getTaskId()
                        + " failed.");
                continue;
            }

            if (checker.isDependencyCompleted(
                    currentTask, tasks, taskCount)) {

                currentTask.setStatus("Completed");

                System.out.println("Task " + currentTask.getTaskId()
                        + " completed successfully.");

                logger.log("Task " + currentTask.getTaskId()
                        + " completed.");

            } else {
                currentTask.setStatus("Skipped");

                System.out.println("Task " + currentTask.getTaskId()
                        + " skipped because its dependency is not completed.");

                logger.log("Task " + currentTask.getTaskId()
                        + " skipped.");
            }
        }

        logger.log("Workflow execution finished.");
    }
}
