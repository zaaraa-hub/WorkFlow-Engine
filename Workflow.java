
public class Workflow {
    private Task[] tasks;
    private int taskCount;

    public Workflow(int capacity) {
        tasks = new Task[capacity];
        taskCount = 0;
    }

    public void addTask(Task task) {
        if (taskCount < tasks.length) {
            tasks[taskCount] = task;
            taskCount++;
            System.out.println("Task added successfully!");
        } else {
            System.out.println("Workflow is full!");
        }
    }

    public void displayTasks() {
        if (taskCount == 0) {
            System.out.println("No tasks available.");
            return;
        }

        for (int i = 0; i < taskCount; i++) {
            System.out.println("--------------------");
            tasks[i].displayTask();
        }
    }

    public void clearTasks() {
        for (int i = 0; i < taskCount; i++) {
            tasks[i] = null;
        }

        taskCount = 0;
    }

}
