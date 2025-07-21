import java.sql.*;
import java.util.Scanner;

public class CrimeManager {

    // ✅ 1. Officer files a new crime record
    public static void fileCrime(int officerId) {
        Scanner sc = new Scanner(System.in);
        CLIUtils.printPrompt("Enter Crime Title: ");
        String title = sc.nextLine().trim();
        if (title.isEmpty()) {
            CLIUtils.printError("Crime title cannot be empty. Please try again.");
            return;
        }
        if (title.length() < 5) {
            CLIUtils.printError("Crime title must be at least 5 characters long.");
            return;
        }

        CLIUtils.printPrompt("Enter Description: ");
        String desc = sc.nextLine().trim();
        if (desc.isEmpty()) {
            CLIUtils.printError("Description cannot be empty. Please try again.");
            return;
        }
        if (desc.length() < 10) {
            CLIUtils.printError("Description must be at least 10 characters long.");
            return;
        }

        CLIUtils.printPrompt("Enter Location: ");
        String location = sc.nextLine().trim();
        if (location.isEmpty()) {
            CLIUtils.printError("Location cannot be empty. Please try again.");
            return;
        }
        if (location.length() < 3) {
            CLIUtils.printError("Location must be at least 3 characters long.");
            return;
        }

        String date = java.time.LocalDate.now().toString();

        try {
            Connection con = DBConnection.connect();
            String sql = "INSERT INTO crime_records (officer_id, title, description, location, date_reported) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, officerId);
            ps.setString(2, title);
            ps.setString(3, desc);
            ps.setString(4, location);
            ps.setString(5, date);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                CLIUtils.printSuccess("✅ Crime record filed successfully!");
                ActionTracker.log("Officer_" + officerId, "Filed crime: " + title + " at " + location);
            } else {
                CLIUtils.printError("Failed to file record.");
            }

            con.close();
        } catch (Exception e) {
            CLIUtils.printError("Error: " + e.getMessage());
        }
    }

    // ✅ 2. Officer views all crimes filed by them
    public static void viewCrimesByOfficer(int officerId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            
            String sql = "SELECT * FROM crime_records WHERE officer_id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, officerId);
            rs = ps.executeQuery();

            CLIUtils.printInfo("\n📂 Crimes Filed by You:");
            while (rs.next()) {
                CLIUtils.printInfo("Crime ID     : " + rs.getInt("crime_id"));
                CLIUtils.printInfo("Title        : " + rs.getString("title"));
                CLIUtils.printInfo("Description  : " + rs.getString("description"));
                CLIUtils.printInfo("Location     : " + rs.getString("location"));
                CLIUtils.printInfo("Date Reported: " + rs.getString("date_reported"));
                CLIUtils.printInfo("-----------------------------");
            }

            ActionTracker.log("Officer_" + officerId, "Viewed all crimes filed by self");
            
        } catch (SQLException e) {
            CLIUtils.printError("Database error viewing crimes: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error viewing crimes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
    }

    // ✅ 3. Admin views all crimes
    public static void viewAllCrimes() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            
            String sql = "SELECT * FROM crime_records";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            CLIUtils.printInfo("\n📁 All Crime Records in System:");
            while (rs.next()) {
                CLIUtils.printInfo("Crime ID     : " + rs.getInt("crime_id"));
                CLIUtils.printInfo("Officer ID   : " + rs.getInt("officer_id"));
                CLIUtils.printInfo("Title        : " + rs.getString("title"));
                CLIUtils.printInfo("Description  : " + rs.getString("description"));
                CLIUtils.printInfo("Location     : " + rs.getString("location"));
                CLIUtils.printInfo("Date Reported: " + rs.getString("date_reported"));
                CLIUtils.printInfo("----------------------------------");
            }

            ActionTracker.log("Admin", "Viewed all crime records");
            
        } catch (SQLException e) {
            CLIUtils.printError("Database error viewing all crimes: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error viewing all crimes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
    }

    // Show snapshot/history
    public static void showSnapshot(int option) {
        String sql = "SELECT * FROM crime_records";
        String label = "All Crime Records";
        if (option == 2) {
            sql += " WHERE date_reported >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)";
            label = "Incident History (Last 6 Months)";
        } else if (option == 3) {
            sql += " WHERE date_reported >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)";
            label = "Incident History (Last 3 Months)";
        } else if (option == 4) {
            sql += " WHERE date_reported >= DATE_SUB(CURDATE(), INTERVAL 15 DAY)";
            label = "Incident History (Last 15 Days)";
        }
        
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            CLIUtils.printInfo("\n" + label + ":");
            while (rs.next()) {
                CLIUtils.printInfo("Crime ID     : " + rs.getInt("crime_id"));
                CLIUtils.printInfo("Officer ID   : " + rs.getInt("officer_id"));
                CLIUtils.printInfo("Title        : " + rs.getString("title"));
                CLIUtils.printInfo("Description  : " + rs.getString("description"));
                CLIUtils.printInfo("Location     : " + rs.getString("location"));
                CLIUtils.printInfo("Date Reported: " + rs.getString("date_reported"));
                CLIUtils.printInfo("-----------------------------");
            }
            
        } catch (SQLException e) {
            CLIUtils.printError("Database error fetching snapshot: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error fetching snapshot: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
    }

    public static int getTotalCrimeCount() {
        int count = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return 0;
            }
            
            String sql = "SELECT COUNT(*) FROM crime_records";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            
        } catch (SQLException e) {
            CLIUtils.printError("Database error counting crimes: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error counting crimes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
        return count;
    }
}
