import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActionTracker {

    public static void log(String user, String action) {
        FileWriter writer = null;
        try {
            if (user == null || user.trim().isEmpty()) {
                user = "Unknown";
            }
            if (action == null || action.trim().isEmpty()) {
                action = "No action specified";
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logEntry = timestamp + " | " + user + " | " + action + "\n";
            
            writer = new FileWriter("action_log.txt", true); // true for append mode
            writer.write(logEntry);
            
        } catch (IOException e) {
            // Silently fail for logging - don't crash the main application
            System.err.println("Logging error: " + e.getMessage());
        } catch (Exception e) {
            // Silently fail for logging - don't crash the main application
            System.err.println("Unexpected logging error: " + e.getMessage());
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                // Ignore close errors for logging
            }
        }
    }

    public static void viewLog() {
        BufferedReader reader = null;
        try {
            File logFile = new File("action_log.txt");
            if (!logFile.exists()) {
                CLIUtils.printInfo("No action log found.");
                return;
            }
            
            reader = new BufferedReader(new FileReader(logFile));
            String line;
            
            CLIUtils.printInfo("\n📋 Action Log:");
            CLIUtils.printInfo("=".repeat(80));
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                CLIUtils.printInfo(line);
            }
            
        } catch (IOException e) {
            CLIUtils.printError("Error reading action log: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Unexpected error reading log: " + e.getMessage());
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                CLIUtils.printError("Error closing log file: " + e.getMessage());
            }
        }
    }

    public static void clearLog() {
        try {
            File logFile = new File("action_log.txt");
            if (logFile.exists()) {
                if (logFile.delete()) {
                    CLIUtils.printSuccess("✅ Action log cleared successfully.");
                } else {
                    CLIUtils.printError("Failed to clear action log.");
                }
            } else {
                CLIUtils.printInfo("No action log found to clear.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error clearing action log: " + e.getMessage());
        }
    }
}
