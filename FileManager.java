
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

public class FileManager {

    public void saveTasks(Task[] tasks, int taskCount) {
        try {
            PrintWriter writer = new PrintWriter(
                    new FileWriter("tasks.txt"));

            for (int i = 0; i < taskCount; i++) {
                writer.println(
                        tasks[i].getTaskId() + "," +
                                tasks[i].getTaskName() + "," +
                                tasks[i].getStatus() + "," +
                                tasks[i].getDependencyId());
            }

            writer.close();
            System.out.println("Tasks saved successfully!");

        } catch (Exception e) {
            System.out.println("Error saving tasks.");
        }
    }

    public void loadTasks() {
        try {
            File file = new File("tasks.txt");
            Scanner reader = new Scanner(file);

            System.out.println("\n===== SAVED TASKS =====");

            while (reader.hasNextLine()) {
                System.out.println(reader.nextLine());
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("No saved tasks found.");
        }
    }
}
