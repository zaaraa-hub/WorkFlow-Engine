
public class Task {
    private int taskId;
    private String taskName;
    private String status;
    private int dependencyId;
    private boolean shouldSucceed;

    public Task(int taskId, String taskName) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.status = "Pending";
        this.dependencyId = -1;
        this.shouldSucceed = true;
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

    public boolean shouldSucceed() {
        return shouldSucceed;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDependencyId(int dependencyId) {
        this.dependencyId = dependencyId;
    }

    public void setShouldSucceed(boolean shouldSucceed) {
        this.shouldSucceed = shouldSucceed;
    }

    public void displayTask() {
        System.out.println("Task ID: " + taskId);
        System.out.println("Task Name: " + taskName);
        System.out.println("Status: " + status);
        System.out.println("Dependency ID: " + dependencyId);
    }

    public void resetStatus() {
        this.status = "Pending";
    }
}
