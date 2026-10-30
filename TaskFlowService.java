import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * TaskFlowService - the "adapter" (middleman) between the Swing window and the backend.
 *
 * The window (TaskFlowGUI) only talks to this class. This class calls the existing
 * backend classes (Task, DependencyChecker, WorkflowEngine, FileManager, ExecutionLogger)
 * WITHOUT changing them. If the backend changes later, only this file needs updating.
 *
 * It owns the task list, because the backend keeps its list inside Main.main()
 * where no other class can reach it.
 */
public class TaskFlowService {

    /** UI-side task limit. Matches the console version's limit of 10 (see findings log F-11). */
    public static final int MAX_TASKS = 10;

    /** Status names used by the backend. They are compared exactly (case-sensitive). */
    public static final String PENDING = "Pending";
    public static final String RUNNING = "Running";
    public static final String COMPLETED = "Completed";
    public static final String FAILED = "Failed";
    public static final String SKIPPED = "Skipped";

    /** Same relative name the backend uses, resolved once to a full path so the window can show it. */
    private static final Path LOG_PATH = Paths.get("execution.log").toAbsolutePath();

    /** Result of an action: did it work, a short message for the user, and any console text the backend printed. */
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

    private final Task[] tasks = new Task[MAX_TASKS];
    private int taskCount = 0;

    private final DependencyChecker checker = new DependencyChecker();
    private final WorkflowEngine engine = new WorkflowEngine();
    private final FileManager fileManager = new FileManager();
    private final ExecutionLogger logger = new ExecutionLogger();

    // ------------------------------------------------------------------
    // Reading the task list
    // ------------------------------------------------------------------

    public int getTaskCount() {
        return taskCount;
    }

    /** Returns the task at the given row, or null if the index is out of range. */
    public Task getTask(int index) {
        if (index < 0 || index >= taskCount) {
            return null;
        }
        return tasks[index];
    }

    /** How many tasks currently have this exact status, e.g. "Completed". */
    public int countByStatus(String status) {
        int count = 0;
        for (int i = 0; i < taskCount; i++) {
            if (status.equals(tasks[i].getStatus())) {
                count++;
            }
        }
        return count;
    }

    /** Text for the "Depends on" column, e.g. "None" or "Task 1 (Book venue)". */
    public String getDependencyLabel(int index) {
        Task task = getTask(index);
        if (task == null) {
            return "";
        }
        int dependencyId = task.getDependencyId();
        if (dependencyId == -1) {
            return "None";
        }
        int dependencyIndex = findIndexById(dependencyId);
        if (dependencyIndex == -1) {
            return "Task " + dependencyId + " (missing)";
        }
        return "Task " + dependencyId + " (" + tasks[dependencyIndex].getTaskName() + ")";
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    /** Adds a task after checking it. dependencyId = -1 means "no dependency". */
    public Result addTask(int id, String name, int dependencyId) {
        if (taskCount >= MAX_TASKS) {
            return fail("Task limit reached (" + MAX_TASKS + ").");
        }
        if (id < 1) {
            return fail("Task ID must be 1 or higher.");
        }

        String cleanName = (name == null) ? "" : name.trim();
        if (cleanName.isEmpty()) {
            return fail("Task name cannot be empty.");
        }
        // A comma would corrupt tasks.txt because the backend saves with commas (findings log F-04).
        if (cleanName.contains(",") || cleanName.contains("\n") || cleanName.contains("\r")) {
            return fail("Task name cannot contain a comma or a line break.");
        }
        if (findIndexById(id) != -1) {
            return fail("Task ID " + id + " already exists.");
        }

        Task task = new Task(id, cleanName);
        task.setDependencyId(dependencyId);

        if (!checker.isDependencyValid(task, tasks, taskCount)) {
            return fail("Invalid dependency.");
        }

        tasks[taskCount] = task;
        taskCount++;
        return ok("Task " + id + " added.");
    }

    /** Runs the workflow using the backend engine. */
    public Result executeWorkflow() {
        if (taskCount == 0) {
            return fail("No tasks to execute. Add a task first.");
        }

        boolean hasCycle = checker.hasCircularDependency(tasks, taskCount);
        String output = capture(() -> engine.executeWorkflow(tasks, taskCount));

        if (hasCycle) {
            return new Result(false, "Execution stopped: circular dependency detected.", output);
        }

        String summary = "Workflow executed: "
                + countByStatus(COMPLETED) + " completed, "
                + countByStatus(FAILED) + " failed, "
                + countByStatus(SKIPPED) + " skipped.";
        return new Result(true, summary, output);
    }

    /**
     * Runs ONLY the task at this row, using the backend's executeSingleTask.
     * The backend method silently does nothing when the task is not allowed to run,
     * so we check the rules first (getExecutionBlockReason) and tell the user why.
     */
    public Result executeSelectedTask(int index) {
        String reason = getExecutionBlockReason(index);
        if (reason != null) {
            return fail(reason);
        }

        Task task = tasks[index];
        String output = capture(() -> engine.executeSingleTask(task, tasks, taskCount));

        String label = "Task " + task.getTaskId() + " (" + task.getTaskName() + ")";
        if (COMPLETED.equals(task.getStatus())) {
            return new Result(true, label + " completed.", output);
        }
        if (FAILED.equals(task.getStatus())) {
            return new Result(false, label + " failed after its retries. See Execution Logs.", output);
        }
        return new Result(false, label + " was not executed.", output);
    }

    /** null = this task may run now. Otherwise the reason it is blocked, written for the user. */
    public String getExecutionBlockReason(int index) {
        Task task = getTask(index);
        if (task == null) {
            return "Select a task first.";
        }
        String label = "Task " + task.getTaskId() + " (" + task.getTaskName() + ")";

        if (!PENDING.equals(task.getStatus())) {
            return "Cannot run " + label + ": its status is " + task.getStatus()
                    + ". Only Pending tasks can run - use Reset Statuses to run it again.";
        }

        int dependencyId = task.getDependencyId();
        if (dependencyId != -1) {
            int dependencyIndex = findIndexById(dependencyId);
            if (dependencyIndex == -1) {
                return "Cannot run " + label + ": its prerequisite Task " + dependencyId + " does not exist.";
            }
            Task prerequisite = tasks[dependencyIndex];
            if (!COMPLETED.equals(prerequisite.getStatus())) {
                return "Cannot run " + label + ": prerequisite Task " + dependencyId
                        + " (" + prerequisite.getTaskName() + ") is " + prerequisite.getStatus() + ".";
            }
        }

        // Final guard: use the backend's own rule too, so we never disagree with it.
        if (!checker.isDependencyCompleted(task, tasks, taskCount)) {
            return "Cannot run " + label + ": its prerequisites are not completed.";
        }
        return null;
    }

    /**
     * Removes the task at this row from the list. Same rule as the console version:
     * a task that other tasks depend on cannot be deleted. Nothing is written to
     * tasks.txt until the user presses Save.
     */
    public Result deleteTask(int index) {
        Task task = getTask(index);
        if (task == null) {
            return fail("Select a task first.");
        }
        String reason = getDeleteBlockReason(index);
        if (reason != null) {
            return fail(reason);
        }

        String label = "Task " + task.getTaskId() + " (" + task.getTaskName() + ")";
        for (int i = index; i < taskCount - 1; i++) {
            tasks[i] = tasks[i + 1];
        }
        tasks[taskCount - 1] = null;
        taskCount--;
        return ok(label + " deleted. Press Save Tasks to keep this change in tasks.txt.");
    }

    /** null = this task may be deleted. Otherwise the reason it cannot be. */
    public String getDeleteBlockReason(int index) {
        Task task = getTask(index);
        if (task == null) {
            return "Select a task first.";
        }
        StringBuilder dependents = new StringBuilder();
        int count = 0;
        for (int i = 0; i < taskCount; i++) {
            if (i != index && tasks[i].getDependencyId() == task.getTaskId()) {
                if (count > 0) {
                    dependents.append(", ");
                }
                dependents.append("Task ").append(tasks[i].getTaskId())
                        .append(" (").append(tasks[i].getTaskName()).append(")");
                count++;
            }
        }
        if (count > 0) {
            return "Cannot delete Task " + task.getTaskId() + " (" + task.getTaskName() + "): "
                    + dependents + (count == 1 ? " depends" : " depend")
                    + " on it. Delete the dependent task first.";
        }
        return null;
    }

    /** Sets every task back to Pending so the workflow can run again. */
    public Result resetStatuses() {
        if (taskCount == 0) {
            return fail("No tasks to reset.");
        }
        for (int i = 0; i < taskCount; i++) {
            tasks[i].resetStatus();
        }
        return ok("All task statuses reset to Pending.");
    }

    /** Checks the real task list for circular dependencies. success = true means NO cycle found. */
    public Result checkCircularDependencies() {
        if (taskCount == 0) {
            return fail("No tasks to check. Add a task first.");
        }
        if (checker.hasCircularDependency(tasks, taskCount)) {
            return fail("Circular dependency detected!");
        }
        return ok("No circular dependency found.");
    }

    /** Saves tasks to tasks.txt using the backend FileManager. */
    public Result saveTasks() {
        if (taskCount == 0) {
            return fail("No tasks to save.");
        }
        String output = capture(() -> fileManager.saveTasks(tasks, taskCount));
        // The backend does not return success or failure, so we read its console message (findings log F-15).
        if (output.contains("saved successfully")) {
            return new Result(true, "Saved " + taskCount + " task(s) to tasks.txt.", output);
        }
        return new Result(false, "Saving failed. See Run Output.", output);
    }

    /**
     * Loads tasks from tasks.txt. The file is read into a temporary list first, so a failed
     * load never wipes the tasks already in the window (findings log F-05).
     */
    public Result loadTasks() {
        Task[] temp = new Task[MAX_TASKS];
        int[] loaded = new int[1];
        String output = capture(() -> loaded[0] = fileManager.loadTasks(temp));

        if (loaded[0] <= 0) {
            return new Result(false,
                    "Nothing loaded (tasks.txt is missing, empty or unreadable). Current tasks were kept.",
                    output);
        }

        for (int i = 0; i < MAX_TASKS; i++) {
            tasks[i] = (i < loaded[0]) ? temp[i] : null;
        }
        taskCount = loaded[0];

        String message = "Loaded " + taskCount + " task(s) from tasks.txt.";
        if (taskCount == MAX_TASKS) {
            message += " (Limit of " + MAX_TASKS + " reached; any extra tasks in the file were not loaded.)";
        }
        return new Result(true, message, output);
    }

    // ------------------------------------------------------------------
    // Logs
    // ------------------------------------------------------------------

    /** Reads execution.log directly, because the backend can only print logs to the console. */
    public String readLogs() {
        Path file = LOG_PATH;
        if (!Files.exists(file)) {
            return "No execution logs found.";
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return "(The log is empty.)";
            }
            return String.join("\n", lines);
        } catch (IOException e) {
            return "Could not read " + LOG_PATH + ": " + e.getMessage();
        }
    }

    /** Full path of the log file this program reads and clears. */
    public String getLogFilePath() {
        return LOG_PATH.toString();
    }

    public Result clearLogs() {
        String output = capture(logger::clearLogs);

        // Do not trust the printed message alone (findings log F-21): check the file itself.
        boolean cleared = false;
        try {
            cleared = Files.notExists(LOG_PATH) || Files.size(LOG_PATH) == 0;
        } catch (IOException e) {
            cleared = false;
        }

        if (cleared) {
            return new Result(true, "Execution logs cleared.", output);
        }
        return new Result(false, "Could not clear " + LOG_PATH
                + " - it may be open or locked by another program. See Run Output.", output);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private int findIndexById(int id) {
        for (int i = 0; i < taskCount; i++) {
            if (tasks[i].getTaskId() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Runs a backend call and returns whatever it printed with System.out.
     * Many backend methods print instead of returning a value (findings log F-14),
     * so this lets the window show that text without changing the backend.
     */
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