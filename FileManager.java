
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

    public int loadTasks(Task[] tasks) {
        int count = 0;

        try {
            File file = new File("tasks.txt");
            Scanner reader = new Scanner(file);

            while (reader.hasNextLine() && count < tasks.length) {
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

                    tasks[count] = task;
                    count++;
                }
            }

            reader.close();
            System.out.println("Tasks loaded successfully!");

        } catch (Exception e) {
            System.out.println("Error loading tasks. Check tasks.txt.");
            return 0;
        }

        return count;
    }
}
