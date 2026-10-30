
public class WorkflowEngine {
    private DependencyChecker checker = new DependencyChecker();
    private ExecutionLogger logger = new ExecutionLogger();

    public void executeWorkflow(Task[] tasks, int taskCount) {
        System.out.println("\n===== WORKFLOW EXECUTION =====");
        logger.log("Workflow execution started.");

        if (checker.hasCircularDependency(tasks, taskCount)) {
            System.out.println("Execution stopped: circular dependency detected.");
            logger.log("Execution stopped due to circular dependency.");
            return;
        }

        int completedCount = 0;
        boolean progress;

        do {
            progress = false;

            for (int i = 0; i < taskCount; i++) {
                Task task = tasks[i];

                if (task.getStatus().equals("Pending")
                        && checker.isDependencyCompleted(task, tasks, taskCount)) {

                    task.setStatus("Running");

                    boolean success = executeTask(task);

                    if (success) {
                        task.setStatus("Completed");
                        completedCount++;
                        System.out.println("Task " + task.getTaskId()
                                + " completed: " + task.getTaskName());
                        logger.log("Task " + task.getTaskId() + " completed.");
                    } else {
                        task.setStatus("Failed");
                        System.out.println("Task " + task.getTaskId() + " failed.");
                        logger.log("Task " + task.getTaskId() + " failed.");
                    }

                    progress = true;
                }
            }
        } while (progress);

        for (int i = 0; i < taskCount; i++) {
            if (tasks[i].getStatus().equals("Pending")) {
                tasks[i].setStatus("Skipped");
                System.out.println("Task " + tasks[i].getTaskId()
                        + " skipped: dependency unavailable.");
                logger.log("Task " + tasks[i].getTaskId() + " skipped.");
            }
        }

        System.out.println("\nCompleted in this execution: " + completedCount);
        logger.log("Workflow execution finished.");
    }

    private boolean executeTask(Task task) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.println("\nExecuting: " + task.getTaskName());
            System.out.println("Attempt " + attempt + " of " + maxAttempts);

            if (task.shouldSucceed()) {
                logger.log("Task " + task.getTaskId()
                        + " succeeded on attempt " + attempt + ".");
                return true;
            }

            System.out.println("Task failed.");
            logger.log("Task " + task.getTaskId()
                    + " failed on attempt " + attempt + ".");

            if (attempt < maxAttempts) {
                System.out.println("Retrying task...");
            }
        }

        return false;
    }

    public void executeSingleTask(Task task, Task[] tasks, int taskCount) {
        if (!task.getStatus().equals("Pending")
                || !checker.isDependencyCompleted(task, tasks, taskCount)) {
            return;
        }

        logger.log("Single task execution started: Task " + task.getTaskId());

        task.setStatus("Running");

        boolean success = executeTask(task);

        task.setStatus(success ? "Completed" : "Failed");

        logger.log("Task " + task.getTaskId()
                + (success ? " completed." : " failed."));
    }
}
