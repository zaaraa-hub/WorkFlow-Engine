
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        Task[] tasks = new Task[10];
        int taskCount = 0;

        Workflow workflow = new Workflow(10);
        WorkflowEngine engine = new WorkflowEngine();
        DependencyChecker checker = new DependencyChecker();
        FileManager fileManager = new FileManager();
        ExecutionLogger logger = new ExecutionLogger();

        int choice;

        do {
            System.out.println("\n===== TASKFLOW WORKFLOW ENGINE =====");
            System.out.println("1. Add Task");
            System.out.println("2. Display Tasks");
            System.out.println("3. Execute Workflow");
            System.out.println("4. Save Tasks");
            System.out.println("5. Load Saved Tasks");
            System.out.println("6. View Execution Logs");
            System.out.println("7. Check Circular Dependencies");
            System.out.println("8. Test Task Retry");
            System.out.println("9. Reset Workflow");
            System.out.println("10. Workflow Summary");
            System.out.println("11. Clear Execution Logs");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    if (taskCount < tasks.length) {
                        System.out.print("Enter task ID: ");
                        int id = input.nextInt();
                        input.nextLine();

                        boolean duplicate = false;

                        for (int i = 0; i < taskCount; i++) {
                            if (tasks[i].getTaskId() == id) {
                                duplicate = true;
                                break;
                            }
                        }

                        if (duplicate) {
                            System.out.println("Task ID already exists.");
                            break;
                        }

                        System.out.print("Enter task name: ");
                        String name = input.nextLine();

                        Task task = new Task(id, name);

                        System.out.print("Enter dependency ID (-1 for none): ");
                        int dependencyId = input.nextInt();
                        input.nextLine();

                        task.setDependencyId(dependencyId);

                        if (!checker.isDependencyValid(
                                task, tasks, taskCount)) {
                            System.out.println("Invalid dependency.");
                            break;
                        }

                        tasks[taskCount] = task;
                        taskCount++;
                        workflow.addTask(task);

                    } else {
                        System.out.println("Task limit reached.");
                    }
                    break;

                case 2:
                    workflow.displayTasks();
                    break;

                case 3:
                    engine.executeWorkflow(tasks, taskCount);
                    break;

                case 4:
                    fileManager.saveTasks(tasks, taskCount);
                    break;

                case 5:
                    taskCount = fileManager.loadTasks(tasks);

                    workflow.clearTasks();

                    for (int i = 0; i < taskCount; i++) {
                        workflow.addTask(tasks[i]);
                    }

                    break;

                case 6:
                    logger.displayLogs();
                    break;

                case 7:
                    if (taskCount >= 2) {
                        int originalDependency = tasks[0].getDependencyId();

                        tasks[0].setDependencyId(tasks[1].getTaskId());

                        if (checker.hasCircularDependency(tasks, taskCount)) {
                            System.out.println("Circular dependency detected!");
                        } else {
                            System.out.println("No circular dependency found.");
                        }

                        tasks[0].setDependencyId(originalDependency);
                    } else {
                        System.out.println("Add at least 2 tasks first.");
                    }
                    break;

                case 8:
                    if (taskCount == 0) {
                        System.out.println("Add a task first!");
                        break;
                    }

                    System.out.println("\n===== RETRY TEST =====");
                    System.out.println("1. Test successful task");
                    System.out.println("2. Test failed task and retries");
                    System.out.print("Choose test: ");

                    int testChoice = input.nextInt();

                    if (testChoice == 1) {
                        tasks[0].setShouldSucceed(true);
                    } else if (testChoice == 2) {
                        tasks[0].setShouldSucceed(false);
                    } else {
                        System.out.println("Invalid choice.");
                        break;
                    }

                    for (int i = 0; i < taskCount; i++) {
                        tasks[i].resetStatus();
                    }

                    engine.executeWorkflow(tasks, taskCount);

                    tasks[0].setShouldSucceed(true);
                    break;
                case 9:
                    for (int i = 0; i < taskCount; i++) {
                        tasks[i].resetStatus();
                    }

                    System.out.println("Workflow statuses reset successfully!");
                    break;

                case 10:
                    int completed = 0;
                    int failed = 0;
                    int pending = 0;
                    int skipped = 0;
                    int running = 0;

                    for (int i = 0; i < taskCount; i++) {
                        String status = tasks[i].getStatus();

                        if (status.equals("Completed")) {
                            completed++;
                        } else if (status.equals("Failed")) {
                            failed++;
                        } else if (status.equals("Pending")) {
                            pending++;
                        } else if (status.equals("Skipped")) {
                            skipped++;
                        } else if (status.equals("Running")) {
                            running++;
                        }
                    }

                    System.out.println("\n===== WORKFLOW SUMMARY =====");
                    System.out.println("Total tasks: " + taskCount);
                    System.out.println("Completed: " + completed);
                    System.out.println("Failed: " + failed);
                    System.out.println("Pending: " + pending);
                    System.out.println("Skipped: " + skipped);
                    System.out.println("Running: " + running);
                    break;
                case 11:
                    logger.clearLogs();
                    break;

                case 0:
                    System.out.println("Exiting TaskFlow. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 0);

        input.close();
    }
}
