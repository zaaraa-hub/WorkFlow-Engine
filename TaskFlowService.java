
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TaskFlowService {

    public static final int MAX_TASKS = 10;

    public static final String PENDING = "Pending";
    public static final String RUNNING = "Running";
    public static final String COMPLETED = "Completed";
    public static final String FAILED = "Failed";
    public static final String SKIPPED = "Skipped";

    private static final Path LOG_PATH = Paths.get("execution.log").toAbsolutePath();

    public static class Result {

        private final boolean success;
        private final String message;
        private final String consoleOutput;

        Result(boolean success, String message, String consoleOutput) {
            this.success = success;
            this.message = message;
            this.consoleOutput = consoleOutput == null ? "" : consoleOutput;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getConsoleOutput() {
            return consoleOutput;
        }
    }

    // Single source of truth
    private final Workflow workflow = new Workflow();

    private final DependencyChecker checker = new DependencyChecker();
    private final WorkflowEngine engine = new WorkflowEngine();
    private final FileManager fileManager = new FileManager();
    private final ExecutionLogger logger = new ExecutionLogger();

    // Reading tasks

    public int getTaskCount() {
        return workflow.getTaskCount();
    }

    public Task getTask(int index) {
        if (index < 0 || index >= workflow.getTaskCount()) {
            return null;
        }

        return workflow.getTask(index);
    }

    public int countByStatus(String status) {
        int count = 0;

        for (int i = 0; i < workflow.getTaskCount(); i++) {
            if (status.equals(workflow.getTask(i).getStatus())) {
                count++;
            }
        }

        return count;
    }

    public String getDependencyLabel(int index) {
        Task task = getTask(index);

        if (task == null) {
            return "";
        }

        int dependencyId = task.getDependencyId();

        if (dependencyId == -1) {
            return "None";
        }

        Task prerequisite = workflow.getTaskById(dependencyId);

        if (prerequisite == null) {
            return "Task " + dependencyId + " (missing)";
        }

        return "Task " + dependencyId
                + " (" + prerequisite.getTaskName() + ")";
    }

    // Adding tasks

    public Result addTask(int id, String name, int dependencyId) {

        if (id < 1) {
            return fail("Task ID must be 1 or higher.");
        }

        String cleanName = name == null ? "" : name.trim();

        if (cleanName.isEmpty()) {
            return fail("Task name cannot be empty.");
        }

        if (cleanName.contains(",")
                || cleanName.contains("\n")
                || cleanName.contains("\r")) {
            return fail("Task name cannot contain a comma or a line break.");
        }

        if (workflow.getTaskById(id) != null) {
            return fail("Task ID " + id + " already exists.");
        }

        Task task = new Task(id, cleanName);
        task.setDependencyId(dependencyId);

        Task[] currentTasks = workflow.getTasks();

        if (!checker.isDependencyValid(
                task, currentTasks, workflow.getTaskCount())) {
            return fail("Invalid dependency.");
        }

        try {
            workflow.addTask(task);
        } catch (IllegalArgumentException e) {
            return fail(e.getMessage());
        }

        return ok("Task " + id + " added.");
    }

    // Workflow execution

    public Result executeWorkflow() {

        int taskCount = workflow.getTaskCount();

        if (taskCount == 0) {
            return fail("No tasks to execute. Add a task first.");
        }

        Task[] currentTasks = workflow.getTasks();

        if (checker.hasCircularDependency(currentTasks, taskCount)) {
            return fail("Execution stopped: circular dependency detected.");
        }

        String output = capture(
                () -> engine.executeWorkflow(currentTasks, taskCount));

        String summary = "Workflow executed: "
                + countByStatus(COMPLETED) + " completed, "
                + countByStatus(FAILED) + " failed, "
                + countByStatus(SKIPPED) + " skipped.";

        return new Result(true, summary, output);
    }

    public Result executeSelectedTask(int index) {

        String reason = getExecutionBlockReason(index);

        if (reason != null) {
            return fail(reason);
        }

        Task task = workflow.getTask(index);

        Task[] currentTasks = workflow.getTasks();
        int taskCount = workflow.getTaskCount();

        String output = capture(
                () -> engine.executeSingleTask(
                        task, currentTasks, taskCount));

        String label = "Task " + task.getTaskId()
                + " (" + task.getTaskName() + ")";

        if (COMPLETED.equals(task.getStatus())) {
            return new Result(true, label + " completed.", output);
        }

        if (FAILED.equals(task.getStatus())) {
            return new Result(false,
                    label + " failed after its retries. See Execution Logs.",
                    output);
        }

        return new Result(false, label + " was not executed.", output);
    }

    public String getExecutionBlockReason(int index) {

        Task task = getTask(index);

        if (task == null) {
            return "Select a task first.";
        }

        String label = "Task " + task.getTaskId()
                + " (" + task.getTaskName() + ")";

        if (!PENDING.equals(task.getStatus())) {
            return "Cannot run " + label + ": its status is "
                    + task.getStatus()
                    + ". Only Pending tasks can run - use Reset Statuses to run it again.";
        }

        int dependencyId = task.getDependencyId();

        if (dependencyId != -1) {

            Task prerequisite = workflow.getTaskById(dependencyId);

            if (prerequisite == null) {
                return "Cannot run " + label
                        + ": its prerequisite Task "
                        + dependencyId + " does not exist.";
            }

            if (!COMPLETED.equals(prerequisite.getStatus())) {
                return "Cannot run " + label
                        + ": prerequisite Task " + dependencyId
                        + " (" + prerequisite.getTaskName() + ") is "
                        + prerequisite.getStatus() + ".";
            }
        }

        Task[] currentTasks = workflow.getTasks();

        if (!checker.isDependencyCompleted(
                task, currentTasks, workflow.getTaskCount())) {
            return "Cannot run " + label
                    + ": its prerequisites are not completed.";
        }

        return null;
    }

    // Deleting tasks

    public Result deleteTask(int index) {

        Task task = getTask(index);

        if (task == null) {
            return fail("Select a task first.");
        }

        String reason = getDeleteBlockReason(index);

        if (reason != null) {
            return fail(reason);
        }

        String label = "Task " + task.getTaskId()
                + " (" + task.getTaskName() + ")";

        workflow.deleteTask(index);

        return ok(label
                + " deleted. Press Save Tasks to keep this change in tasks.txt.");
    }

    public String getDeleteBlockReason(int index) {

        Task task = getTask(index);

        if (task == null) {
            return "Select a task first.";
        }

        StringBuilder dependents = new StringBuilder();
        int count = 0;

        for (int i = 0; i < workflow.getTaskCount(); i++) {

            Task current = workflow.getTask(i);

            if (i != index
                    && current.getDependencyId() == task.getTaskId()) {

                if (count > 0) {
                    dependents.append(", ");
                }

                dependents.append("Task ")
                        .append(current.getTaskId())
                        .append(" (")
                        .append(current.getTaskName())
                        .append(")");

                count++;
            }
        }

        if (count > 0) {
            return "Cannot delete Task " + task.getTaskId()
                    + " (" + task.getTaskName() + "): "
                    + dependents
                    + (count == 1 ? " depends" : " depend")
                    + " on it. Delete the dependent task first.";
        }

        return null;
    }

    // Reset and dependency checking

    public Result resetStatuses() {

        if (workflow.getTaskCount() == 0) {
            return fail("No tasks to reset.");
        }

        for (int i = 0; i < workflow.getTaskCount(); i++) {
            workflow.getTask(i).resetStatus();
        }

        return ok("All task statuses reset to Pending.");
    }

    public Result checkCircularDependencies() {

        if (workflow.getTaskCount() == 0) {
            return fail("No tasks to check. Add a task first.");
        }

        Task[] currentTasks = workflow.getTasks();

        if (checker.hasCircularDependency(
                currentTasks, workflow.getTaskCount())) {
            return fail("Circular dependency detected!");
        }

        return ok("No circular dependency found.");
    }

    // Saving and loading

    public Result saveTasks() {

        int taskCount = workflow.getTaskCount();

        if (taskCount == 0) {
            return fail("No tasks to save.");
        }

        Task[] currentTasks = workflow.getTasks();

        String output = capture(
                () -> fileManager.saveTasks(currentTasks, taskCount));

        if (output.contains("saved successfully")) {
            return new Result(true,
                    "Saved " + taskCount + " task(s) to tasks.txt.",
                    output);
        }

        return new Result(false, "Saving failed. See Run Output.", output);
    }

    public Result loadTasks() {

        String output = capture(() -> {
            Task[] loadedTasks = fileManager.loadTasks();

            workflow.clearTasks();

            for (Task task : loadedTasks) {
                workflow.addTask(task);
            }
        });

        if (output.contains("Error loading tasks")) {
            return new Result(false, "Loading failed. See Run Output.", output);
        }

        return new Result(true,
                "Loaded " + workflow.getTaskCount()
                        + " task(s) from tasks.txt.",
                output);
    }

    // Logs

    public String readLogs() {

        if (!Files.exists(LOG_PATH)) {
            return "No execution logs found.";
        }

        try {
            List<String> lines = Files.readAllLines(LOG_PATH, StandardCharsets.UTF_8);

            if (lines.isEmpty()) {
                return "(The log is empty.)";
            }

            return String.join("\n", lines);

        } catch (IOException e) {
            return "Could not read " + LOG_PATH + ": " + e.getMessage();
        }
    }

    public String getLogFilePath() {
        return LOG_PATH.toString();
    }

    public Result clearLogs() {

        String output = capture(logger::clearLogs);

        boolean cleared = false;

        try {
            cleared = Files.notExists(LOG_PATH)
                    || Files.size(LOG_PATH) == 0;
        } catch (IOException e) {
            cleared = false;
        }

        if (cleared) {
            return new Result(true, "Execution logs cleared.", output);
        }

        return new Result(false,
                "Could not clear " + LOG_PATH
                        + " - it may be open or locked by another program. See Run Output.",
                output);
    }

    // Helper methods

    private String capture(Runnable action) {

        PrintStream original = System.out;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        PrintStream temporary = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        try {
            System.setOut(temporary);
            action.run();

        } finally {
            System.setOut(original);
            temporary.flush();
            temporary.close();
        }

        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    private static Result ok(String message) {
        return new Result(true, message, "");
    }

    private static Result fail(String message) {
        return new Result(false, message, "");
    }
}
