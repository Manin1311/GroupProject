import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class ActionTracker {


    public static void log(String user, String action) {
        // SQL query to insert a new log entry. The timestamp is set automatically by the database.
        String sql = "INSERT INTO actions_log (username, action) VALUES (?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.connect();
            if (con == null) return; // Silently fail if DB connection fails

            ps = con.prepareStatement(sql);
            ps.setString(1, user);
            ps.setString(2, action);
            ps.executeUpdate();

        } catch (SQLException e) {
            // Silently fail for logging - don't crash the main application
            System.err.println("Database logging error: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                // Ignore close errors for logging
            }
        }
    }

    /**
     * Retrieves and displays all log entries from the 'actions_log' database table.
     */
    public static void viewLog() {
        String sql = "SELECT * FROM actions_log ORDER BY timestamp DESC";
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }

            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            CLIUtils.printInfo("\n📋 Action Log (from Database):");
            CLIUtils.printInfo("=".repeat(80));

            boolean foundLogs = false;
            while (rs.next()) {
                foundLogs = true;
                String timestamp = rs.getTimestamp("timestamp").toString();
                String username = rs.getString("username");
                String action = rs.getString("action");
                String logEntry = String.format("%s | %-15s | %s", timestamp, username, action);
                CLIUtils.printInfo(logEntry);
            }

            if (!foundLogs) {
                CLIUtils.printInfo("No action log found in the database.");
            }

        } catch (SQLException e) {
            CLIUtils.printError("Error reading action log from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
    }

    /**
     * Deletes all records from the 'actions_log' database table.
     */
    public static void clearLog() {
        // TRUNCATE is faster than DELETE for clearing all rows in a table.
        String sql = "TRUNCATE TABLE actions_log";
        Connection con = null;
        Statement stmt = null;

        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            stmt = con.createStatement();
            stmt.execute(sql);
            CLIUtils.printSuccess("✅ Database action log cleared successfully.");

        } catch (SQLException e) {
            CLIUtils.printError("Error clearing action log from database: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
    }
}
