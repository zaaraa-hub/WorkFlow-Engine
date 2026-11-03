
public class Workflow {

    private Task[] tasks;
    private int taskCount;

    public Workflow() {
        tasks = new Task[10];
        taskCount = 0;
    }

    // Add a task
    public void addTask(Task task) {

        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        if (getTaskById(task.getTaskId()) != null) {
            throw new IllegalArgumentException("Duplicate task ID.");
        }

        if (taskCount == tasks.length) {
            expandCapacity();
        }

        tasks[taskCount] = task;
        taskCount++;
    }

    // Expand array capacity
    private void expandCapacity() {

        Task[] newTasks = new Task[tasks.length * 2];

        for (int i = 0; i < taskCount; i++) {
            newTasks[i] = tasks[i];
        }

        tasks = newTasks;
    }

    // Get task by index
    public Task getTask(int index) {

        if (index < 0 || index >= taskCount) {
            throw new IndexOutOfBoundsException("Invalid task index.");
        }

        return tasks[index];
    }

    // Find task by ID
    public Task getTaskById(int taskId) {

        for (int i = 0; i < taskCount; i++) {

            if (tasks[i].getTaskId() == taskId) {
                return tasks[i];
            }
        }

        return null;
    }

    // Get number of tasks
    public int getTaskCount() {
        return taskCount;
    }

    // Delete task by index
    public void deleteTask(int index) {

        if (index < 0 || index >= taskCount) {
            throw new IndexOutOfBoundsException("Invalid task index.");
        }

        for (int i = index; i < taskCount - 1; i++) {
            tasks[i] = tasks[i + 1];
        }

        tasks[taskCount - 1] = null;
        taskCount--;
    }

    // Display tasks
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

    // Clear workflow
    public void clearTasks() {

        for (int i = 0; i < taskCount; i++) {
            tasks[i] = null;
        }

        taskCount = 0;
    }

    public Task[] getTasks() {
        Task[] currentTasks = new Task[taskCount];

        for (int i = 0; i < taskCount; i++) {
            currentTasks[i] = tasks[i];
        }

        return currentTasks;
    }
}
