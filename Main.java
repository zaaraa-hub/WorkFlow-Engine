
public class Main {
    public static void main(String[] args) {
        Workflow workflow = new Workflow(5);

        Task task1 = new Task(1, "Design Website");
        Task task2 = new Task(2, "Develop Website");
        Task task3 = new Task(3, "Test Website");

        task2.setDependencyId(1);
        task3.setDependencyId(2);

        workflow.addTask(task1);
        workflow.addTask(task2);
        workflow.addTask(task3);

        System.out.println("\n===== TASKS =====");
        workflow.displayTasks();

        Task[] tasks = { task1, task2, task3 };
        DependencyChecker checker = new DependencyChecker();

        System.out.println("\n===== DEPENDENCY CHECK =====");
        System.out.println("Task 1 valid: "
                + checker.isDependencyValid(task1, tasks, 3));
        System.out.println("Task 2 valid: "
                + checker.isDependencyValid(task2, tasks, 3));
        System.out.println("Task 2 dependency completed: "
                + checker.isDependencyCompleted(task2, tasks, 3));
    }
}
