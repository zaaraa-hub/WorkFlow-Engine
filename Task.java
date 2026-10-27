
public class Task {
    private int taskId;
    private String taskName;
    private String status;
    private int dependencyId;

    public Task(int taskId, String taskName) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.status = "Pending";
        this.dependencyId = -1;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getStatus() {
        return status;
    }

    public int getDependencyId() {
        return dependencyId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDependencyId(int dependencyId) {
        this.dependencyId = dependencyId;
    }

    public void displayTask() {
        System.out.println("Task ID: " + taskId);
        System.out.println("Task Name: " + taskName);
        System.out.println("Status: " + status);

        if (dependencyId == -1) {
            System.out.println("Dependency: None");
        } else {
            System.out.println("Dependency ID: " + dependencyId);
        }
    }
}
