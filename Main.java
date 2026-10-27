
public class Main {
    public static void main(String[] args) {
        Workflow workflow = new Workflow(5);

        Task task1 = new Task(1, "Design Website");
        Task task2 = new Task(2, "Develop Website");
        Task task3 = new Task(3, "Test Website");

        System.out.println("===== TASKFLOW =====");

        workflow.addTask(task1);
        workflow.addTask(task2);
        workflow.addTask(task3);

        System.out.println("\n===== ALL TASKS =====");
        workflow.displayTasks();
    }
}
