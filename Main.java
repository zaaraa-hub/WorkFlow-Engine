
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

        Task[] tasks = { task1, task2, task3 };

        FileManager fileManager = new FileManager();

        fileManager.saveTasks(tasks, 3);
        fileManager.loadTasks();
    }
}
