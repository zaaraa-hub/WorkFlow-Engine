
public class Main {
    public static void main(String[] args) {
        Task task1 = new Task(1, "Design Website");
        Task task2 = new Task(2, "Develop Website");
        Task task3 = new Task(3, "Test Website");

        task2.setDependencyId(1);
        task3.setDependencyId(2);

        Task[] tasks = { task1, task2, task3 };

        WorkflowEngine engine = new WorkflowEngine();
        engine.executeWorkflow(tasks, 3);

        ExecutionLogger logger = new ExecutionLogger();
        logger.displayLogs();
    }
}
