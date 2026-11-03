
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        Workflow workflow = new Workflow();
        WorkflowEngine engine = new WorkflowEngine();
        DependencyChecker checker = new DependencyChecker();
        FileManager fileManager = new FileManager();
        ExecutionLogger logger = new ExecutionLogger();

        int choice;

        do {
            System.out.println("\n===== TASKFLOW WORKFLOW ENGINE =====");
            System.out.println("1. Add Task");
            System.out.println("2. Display Tasks");
            System.out.println("3. Execute Selected Task");
            System.out.println("4. Execute Workflow");
            System.out.println("5. Save Tasks");
            System.out.println("6. Load Saved Tasks");
            System.out.println("7. View Execution Logs");
            System.out.println("8. Check Circular Dependencies");
            System.out.println("9. Test Task Retry");
            System.out.println("10. Reset Workflow");
            System.out.println("11. Workflow Summary");
            System.out.println("12. Clear Execution Logs");
            System.out.println("13. Delete Task");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            choice = input.nextInt();
            input.nextLine();

            switch (choice) {

                // ADD TASK
                case 1:

                    System.out.print("Enter task ID: ");
                    int id = input.nextInt();
                    input.nextLine();

                    System.out.print("Enter task name: ");
                    String name = input.nextLine();

                    System.out.print("Enter dependency ID (-1 for none): ");
                    int dependencyId = input.nextInt();
                    input.nextLine();

                    Task task = new Task(id, name);
                    task.setDependencyId(dependencyId);

                    Task[] currentTasks = workflow.getTasks();

                    if (!checker.isDependencyValid(
                            task, currentTasks, workflow.getTaskCount())) {
                        System.out.println("Invalid dependency.");
                        break;
                    }

                    try {
                        workflow.addTask(task);
                        System.out.println("Task added successfully!");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }

                    break;

                // DISPLAY TASKS
                case 2:

                    workflow.displayTasks();
                    break;

                // EXECUTE SELECTED TASK
                case 3:

                    if (workflow.getTaskCount() == 0) {
                        System.out.println("No tasks available.");
                        break;
                    }

                    System.out.print("Enter Task ID to execute: ");
                    int selectedId = input.nextInt();
                    input.nextLine();

                    Task selectedTask = workflow.getTaskById(selectedId);

                    if (selectedTask == null) {
                        System.out.println("Task not found.");
                    } else {

                        Task[] tasks = workflow.getTasks();

                        engine.executeSingleTask(
                                selectedTask,
                                tasks,
                                workflow.getTaskCount());
                    }

                    break;

                // EXECUTE WORKFLOW
                case 4:

                    if (workflow.getTaskCount() == 0) {
                        System.out.println("No tasks available.");
                        break;
                    }

                    Task[] executionTasks = workflow.getTasks();

                    if (checker.hasCircularDependency(
                            executionTasks, workflow.getTaskCount())) {
                        System.out.println(
                                "Execution stopped: circular dependency detected!");
                        break;
                    }

                    engine.executeWorkflow(
                            executionTasks,
                            workflow.getTaskCount());

                    break;

                // SAVE TASKS
                case 5:

                    if (workflow.getTaskCount() == 0) {
                        System.out.println("No tasks to save.");
                        break;
                    }

                    fileManager.saveTasks(
                            workflow.getTasks(),
                            workflow.getTaskCount());

                    break;

                // LOAD TASKS
                case 6:

                    Task[] loadedTasks = fileManager.loadTasks();

                    workflow.clearTasks();

                    try {
                        for (Task loadedTask : loadedTasks) {
                            workflow.addTask(loadedTask);
                        }

                        System.out.println(
                                "Loaded " + workflow.getTaskCount()
                                        + " task(s) successfully!");

                    } catch (IllegalArgumentException e) {
                        System.out.println(
                                "Error loading tasks: " + e.getMessage());
                    }

                    break;

                // VIEW EXECUTION LOGS
                case 7:

                    logger.displayLogs();
                    break;

                // CHECK CIRCULAR DEPENDENCIES
                case 8:

                    int taskCount = workflow.getTaskCount();

                    if (taskCount >= 2) {

                        Task firstTask = workflow.getTask(0);

                        int originalDependency = firstTask.getDependencyId();

                        firstTask.setDependencyId(
                                workflow.getTask(1).getTaskId());

                        if (checker.hasCircularDependency(
                                workflow.getTasks(), taskCount)) {
                            System.out.println(
                                    "Circular dependency detected!");
                        } else {
                            System.out.println(
                                    "No circular dependency found.");
                        }

                        firstTask.setDependencyId(originalDependency);

                    } else {
                        System.out.println(
                                "Add at least 2 tasks first.");
                    }

                    break;

                // TEST TASK RETRY
                case 9:

                    if (workflow.getTaskCount() == 0) {
                        System.out.println("Add a task first!");
                        break;
                    }

                    System.out.println("\n===== RETRY TEST =====");
                    System.out.println("1. Test successful task");
                    System.out.println("2. Test failed task and retries");
                    System.out.print("Choose test: ");

                    int testChoice = input.nextInt();
                    input.nextLine();

                    if (testChoice == 1) {
                        workflow.getTask(0).setShouldSucceed(true);
                    } else if (testChoice == 2) {
                        workflow.getTask(0).setShouldSucceed(false);
                    } else {
                        System.out.println("Invalid choice.");
                        break;
                    }

                    for (int i = 0; i < workflow.getTaskCount(); i++) {
                        workflow.getTask(i).resetStatus();
                    }

                    engine.executeWorkflow(
                            workflow.getTasks(),
                            workflow.getTaskCount());

                    workflow.getTask(0).setShouldSucceed(true);

                    break;

                // RESET WORKFLOW
                case 10:

                    for (int i = 0; i < workflow.getTaskCount(); i++) {
                        workflow.getTask(i).resetStatus();
                    }

                    System.out.println(
                            "Workflow statuses reset successfully!");

                    break;

                // WORKFLOW SUMMARY
                case 11:

                    int completed = 0;
                    int failed = 0;
                    int pending = 0;
                    int skipped = 0;
                    int running = 0;

                    for (int i = 0; i < workflow.getTaskCount(); i++) {

                        String status = workflow.getTask(i).getStatus();

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
                    System.out.println(
                            "Total tasks: " + workflow.getTaskCount());
                    System.out.println("Completed: " + completed);
                    System.out.println("Failed: " + failed);
                    System.out.println("Pending: " + pending);
                    System.out.println("Skipped: " + skipped);
                    System.out.println("Running: " + running);

                    break;

                // CLEAR EXECUTION LOGS
                case 12:

                    logger.clearLogs();
                    break;

                // DELETE TASK
                case 13:

                    if (workflow.getTaskCount() == 0) {
                        System.out.println(
                                "No tasks available to delete.");
                        break;
                    }

                    System.out.print("Enter Task ID to delete: ");
                    int deleteId = input.nextInt();
                    input.nextLine();

                    int deleteIndex = -1;
                    boolean hasDependent = false;

                    for (int i = 0; i < workflow.getTaskCount(); i++) {

                        Task current = workflow.getTask(i);

                        if (current.getTaskId() == deleteId) {
                            deleteIndex = i;
                        }

                        if (current.getDependencyId() == deleteId) {
                            hasDependent = true;
                        }
                    }

                    if (deleteIndex == -1) {
                        System.out.println("Task not found.");

                    } else if (hasDependent) {
                        System.out.println(
                                "Cannot delete! Other tasks depend on this task.");

                    } else {
                        workflow.deleteTask(deleteIndex);

                        System.out.println(
                                "Task deleted successfully!");
                    }

                    break;

                // EXIT
                case 0:

                    System.out.println(
                            "Exiting TaskFlow. Goodbye!");

                    break;

                default:

                    System.out.println(
                            "Invalid choice. Try again.");
            }

        } while (choice != 0);

        input.close();
    }
}
