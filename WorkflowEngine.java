
import java.util.Scanner;

public class WorkflowEngine {
    private DependencyChecker checker;
    private ExecutionLogger logger;
    private Scanner input;

    public WorkflowEngine() {
        checker = new DependencyChecker();
        logger = new ExecutionLogger();
        input = new Scanner(System.in);
    }

    public void executeWorkflow(Task[] tasks, int taskCount) {
        System.out.println("\n===== WORKFLOW EXECUTION =====");
        logger.log("Workflow execution started.");

        if (checker.hasCircularDependency(tasks, taskCount)) {
            System.out.println("Execution stopped: circular dependency detected.");
            logger.log("Circular dependency detected.");
            return;
        }

        boolean progress;

        do {
            progress = false;

            for (int i = 0; i < taskCount; i++) {
                Task task = tasks[i];

                if (task.getStatus().equals("Completed")
                        || task.getStatus().equals("Failed")) {
                    continue;
                }

                if (checker.isDependencyCompleted(task, tasks, taskCount)) {
                    task.setStatus("Running");

                    boolean success = executeTask(task);

                    if (success) {
                        task.setStatus("Completed");
                        System.out.println("Task " + task.getTaskId()
                                + " completed successfully.");
                        logger.log("Task " + task.getTaskId() + " completed.");
                        progress = true;
                    } else {
                        task.setStatus("Failed");
                        System.out.println("Task " + task.getTaskId()
                                + " failed after all retries.");
                        logger.log("Task " + task.getTaskId() + " failed.");
                    }
                }
            }
        } while (progress);

        for (int i = 0; i < taskCount; i++) {
            if (!tasks[i].getStatus().equals("Completed")
                    && !tasks[i].getStatus().equals("Failed")) {
                tasks[i].setStatus("Skipped");
                System.out.println("Task " + tasks[i].getTaskId()
                        + " skipped because its dependency is unavailable.");
                logger.log("Task " + tasks[i].getTaskId() + " skipped.");
            }
        }

        logger.log("Workflow execution finished.");
    }

    private boolean executeTask(Task task) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.println("\nExecuting: " + task.getTaskName());
            System.out.println("Attempt " + attempt + " of " + maxAttempts);

            System.out.print("Did this task succeed? (yes/no): ");
            String answer = input.nextLine();

            if (answer.equalsIgnoreCase("yes")) {
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
}
