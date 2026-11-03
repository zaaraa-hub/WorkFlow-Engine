import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class FileManager {

    public void saveTasks(Task[] tasks, int taskCount) {
        try (PrintWriter writer = new PrintWriter(
                new FileWriter("tasks.txt"))) {

            for (int i = 0; i < taskCount; i++) {
                writer.println(
                        tasks[i].getTaskId() + "," +
                                tasks[i].getTaskName() + "," +
                                tasks[i].getStatus() + "," +
                                tasks[i].getDependencyId());
            }

            System.out.println("Tasks saved successfully!");

        } catch (Exception e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    // New dynamic loading method
    public Task[] loadTasks() {
        ArrayList<Task> loadedTasks = new ArrayList<>();

        try {
            File file = new File("tasks.txt");

            if (!file.exists()) {
                System.out.println("No saved tasks file found.");
                return new Task[0];
            }

            try (Scanner reader = new Scanner(file)) {

                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    String[] data = line.split(",", 4);

                    if (data.length == 4) {
                        int id = Integer.parseInt(data[0]);
                        String name = data[1];
                        String status = data[2];
                        int dependencyId = Integer.parseInt(data[3]);

                        Task task = new Task(id, name);
                        task.setStatus(status);
                        task.setDependencyId(dependencyId);

                        loadedTasks.add(task);
                    }
                }
            }

            System.out.println("Tasks loaded successfully!");

        } catch (Exception e) {
            System.out.println("Error loading tasks: " + e.getMessage());
            return new Task[0];
        }

        return loadedTasks.toArray(new Task[0]);
    }

    // Old method retained for compatibility
    public int loadTasks(Task[] tasks) {
        Task[] loadedTasks = loadTasks();
        int count = Math.min(loadedTasks.length, tasks.length);

        for (int i = 0; i < count; i++) {
            tasks[i] = loadedTasks[i];
        }

        return count;
    }
}
