
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class ExecutionLogger {

    public void log(String message) {
        try {
            PrintWriter writer = new PrintWriter(
                    new FileWriter("execution.log", true));

            writer.println(LocalDateTime.now() + " - " + message);
            writer.close();

        } catch (Exception e) {
            System.out.println("Error writing execution log.");
        }
    }

    public void displayLogs() {
        try {
            java.io.File file = new java.io.File("execution.log");
            java.util.Scanner reader = new java.util.Scanner(file);

            System.out.println("\n===== EXECUTION LOGS =====");

            while (reader.hasNextLine()) {
                System.out.println(reader.nextLine());
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("No execution logs found.");
        }
    }
}
