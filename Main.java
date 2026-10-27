
public class Main {
    public static void main(String[] args) {
        Task task1 = new Task(1, "Design Website");
        Task task2 = new Task(2, "Develop Website");
        Task task3 = new Task(3, "Test Website");

        // Create a circular dependency:
        // Task 1 depends on Task 2
        // Task 2 depends on Task 1
        task1.setDependencyId(2);
        task2.setDependencyId(1);

        Task[] tasks = { task1, task2, task3 };

        DependencyChecker checker = new DependencyChecker();

        System.out.println("===== CIRCULAR DEPENDENCY CHECK =====");

        if (checker.hasCircularDependency(tasks, 3)) {
            System.out.println("Circular dependency detected!");
        } else {
            System.out.println("No circular dependency found.");
        }
    }
}
