
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

        if (checker.hasCircularDependency(tasks, taskCount)) {
            System.out.println("Execution stopped: circular dependency detected.");
            logger.log("Execution stopped due to circular dependency.");
            return;
        }

        boolean progress;
        int completedCount = 0;

        do {
            progress = false;

            for (int i = 0; i < taskCount; i++) {
                Task task = tasks[i];

                if (task.getStatus().equals("Completed")
                        || task.getStatus().equals("Failed")) {
                    continue;
                }

                if (checker.isDependencyCompleted(task, tasks, taskCount)) {
                    task.setStatus("Completed");
                    completedCount++;
                    progress = true;

                    System.out.println("Task " + task.getTaskId()
                            + " completed: " + task.getTaskName());

                    logger.log("Task " + task.getTaskId() + " completed.");
                }
            }

        } while (progress);

        for (int i = 0; i < taskCount; i++) {
            if (!tasks[i].getStatus().equals("Completed")
                    && !tasks[i].getStatus().equals("Failed")) {
                tasks[i].setStatus("Skipped");

                System.out.println("Task " + tasks[i].getTaskId()
                        + " skipped: dependency unavailable.");

                logger.log("Task " + tasks[i].getTaskId() + " skipped.");
            }
        }

        System.out.println("\nCompleted in this execution: " + completedCount);

        logger.log("Workflow execution finished.");
    }
}
