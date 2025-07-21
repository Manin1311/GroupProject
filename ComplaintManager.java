import java.sql.*;
import java.util.Scanner;

public class ComplaintManager {

    // ✅ 1. File a new complaint
    public static void fileComplaint(int userId) {
        Scanner sc = new Scanner(System.in);

        CLIUtils.printPrompt("Enter Area: ");
        String area = sc.nextLine().trim();
        if (area.isEmpty()) {
            CLIUtils.printError("Area cannot be empty. Please try again.");
            return;
        }
        if (area.length() < 2) {
            CLIUtils.printError("Area must be at least 2 characters long.");
            return;
        }

        CLIUtils.printPrompt("Enter Complaint Type: ");
        String type = sc.nextLine().trim();
        if (type.isEmpty()) {
            CLIUtils.printError("Complaint type cannot be empty. Please try again.");
            return;
        }
        if (type.length() < 3) {
            CLIUtils.printError("Complaint type must be at least 3 characters long.");
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

        String status = "NEW";
        String filedOn = java.time.LocalDate.now().toString();

        int assignedOfficerId = OfficerManager.assignOfficer(area);

        if (assignedOfficerId == -1) {
            CLIUtils.printError("Cannot file complaint: No officer available for this area.");
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }

            String sql = "INSERT INTO complaints (user_id, area, type, description, status, officer_id, filed_on) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setString(2, area);
            ps.setString(3, type);
            ps.setString(4, desc);
            ps.setString(5, status);
            ps.setInt(6, assignedOfficerId);
            ps.setString(7, filedOn);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                CLIUtils.printSuccess("Complaint filed successfully!");
                CLIUtils.printInfo("Assigned Officer ID: " + assignedOfficerId);
                ActionTracker.log("Citizen_" + userId, "Filed complaint in " + area + " assigned to Officer ID: " + assignedOfficerId);
            } else {
                CLIUtils.printError("Failed to file complaint.");
            }

        } catch (SQLException e) {
            CLIUtils.printError("Database error: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
    }

    // Overloaded fileComplaint for type
    public static void fileComplaint(int userId, String type) {
        Scanner sc = new Scanner(System.in);

        CLIUtils.printPrompt("Enter Area: ");
        String area = sc.nextLine().trim();
        if (area.isEmpty()) {
            CLIUtils.printError("Area cannot be empty. Please try again.");
            return;
        }
        if (area.length() < 2) {
            CLIUtils.printError("Area must be at least 2 characters long.");
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

        String status = "OPEN";
        String filedOn = java.time.LocalDate.now().toString();

        int assignedOfficerId = OfficerManager.assignOfficer(area);

        if (assignedOfficerId == -1) {
            CLIUtils.printError("Cannot file complaint: No officer available for this area.");
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }

            String sql = "INSERT INTO complaints (user_id, area, type, description, status, officer_id, filed_on) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setString(2, area);
            ps.setString(3, type);
            ps.setString(4, desc);
            ps.setString(5, status);
            ps.setInt(6, assignedOfficerId);
            ps.setString(7, filedOn);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                CLIUtils.printSuccess("Complaint filed successfully!");
                CLIUtils.printInfo("Assigned Officer ID: " + assignedOfficerId);
                ActionTracker.log("Citizen_" + userId, "Filed " + type + " complaint in " + area + " assigned to Officer ID: " + assignedOfficerId);
            } else {
                CLIUtils.printError("Failed to file complaint.");
            }

        } catch (SQLException e) {
            CLIUtils.printError("Database error: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
    }

    // ✅ 2. Citizen: View complaints filed by them
    public static void viewComplaintsByUser(int userId) {
        ComplaintBST tree = new ComplaintBST();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            
            String sql = "SELECT * FROM complaints WHERE user_id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("complaint_id");
                String area = rs.getString("area");
                String type = rs.getString("type");
                String desc = rs.getString("description");
                String status = rs.getString("status");
                int officerId = rs.getInt("officer_id");
                String date = rs.getString("filed_on");

                Complaint c = new Complaint(id, userId, area, type, desc, status, officerId, date);
                tree.insert(c);
            }

            CLIUtils.printInfo("\n📄 Your Complaints (Sorted by ID):");
            tree.inOrder();
            ActionTracker.log("Citizen_" + userId, "Viewed own complaint history");

        } catch (SQLException e) {
            CLIUtils.printError("Database error while fetching complaints: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error while fetching complaints: " + e.getMessage());
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

    // ✅ 3. Officer: View complaints assigned to them
    public static void viewComplaintsByOfficer(int officerId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            
            String sql = "SELECT * FROM complaints WHERE officer_id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, officerId);
            rs = ps.executeQuery();

            CLIUtils.printInfo("\n📋 Complaints Assigned to You:");
            while (rs.next()) {
                CLIUtils.printInfo("Complaint ID : " + rs.getInt("complaint_id"));
                CLIUtils.printInfo("Area         : " + rs.getString("area"));
                CLIUtils.printInfo("Type         : " + rs.getString("type"));
                CLIUtils.printInfo("Description  : " + rs.getString("description"));
                CLIUtils.printInfo("Status       : " + rs.getString("status"));
                CLIUtils.printInfo("Filed On     : " + rs.getString("filed_on"));
                CLIUtils.printInfo("--------------------------------------");
            }

            ActionTracker.log("Officer_" + officerId, "Viewed assigned complaints");
            
        } catch (SQLException e) {
            CLIUtils.printError("Database error fetching officer complaints: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error fetching officer complaints: " + e.getMessage());
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

    // ✅ 4. Officer: Update status of a complaint
    public static void updateComplaintStatus(int officerId) {
        Scanner sc = new Scanner(System.in);
        Connection con = null;
        PreparedStatement ps = null;

        try {
            CLIUtils.printPrompt("Enter Complaint ID to update: ");
            String complaintIdInput = sc.nextLine().trim();
            
            if (complaintIdInput.isEmpty()) {
                CLIUtils.printError("Complaint ID cannot be empty.");
                return;
            }
            
            int cid;
            try {
                cid = Integer.parseInt(complaintIdInput);
                if (cid <= 0) {
                    CLIUtils.printError("Complaint ID must be a positive number.");
                    return;
                }
            } catch (NumberFormatException e) {
                CLIUtils.printError("Please enter a valid complaint ID (number).");
                return;
            }

            CLIUtils.printPrompt("Enter new status (OPEN/IN_PROGRESS/RESOLVED/CLOSED): ");
            String newStatus = sc.nextLine().trim().toUpperCase();
            
            if (newStatus.isEmpty()) {
                CLIUtils.printError("Status cannot be empty.");
                return;
            }
            
            // Validate status
            if (!newStatus.matches("^(OPEN|IN_PROGRESS|RESOLVED|CLOSED|CONDITIONAL_CLOSED)$")) {
                CLIUtils.printError("Invalid status. Please use: OPEN, IN_PROGRESS, RESOLVED, CLOSED, or CONDITIONAL_CLOSED");
                return;
            }

            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            
            String sql = "UPDATE complaints SET status = ? WHERE complaint_id = ? AND officer_id = ?";
            ps = con.prepareStatement(sql);

            ps.setString(1, newStatus);
            ps.setInt(2, cid);
            ps.setInt(3, officerId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                CLIUtils.printSuccess("Complaint status updated successfully.");
                ActionTracker.log("Officer_" + officerId, "Updated status of Complaint ID " + cid + " to " + newStatus);
            } else {
                CLIUtils.printError("No complaint found with that ID for your account.");
            }

        } catch (SQLException e) {
            CLIUtils.printError("Database error updating status: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error updating status: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
    }

    // ✅ 5. Admin: View all complaints
    public static void viewAllComplaints() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            
            String sql = "SELECT * FROM complaints";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            CLIUtils.printInfo("\n📋 All Complaints in System:");
            while (rs.next()) {
                CLIUtils.printInfo("Complaint ID: " + rs.getInt("complaint_id"));
                CLIUtils.printInfo("User ID     : " + rs.getInt("user_id"));
                CLIUtils.printInfo("Area        : " + rs.getString("area"));
                CLIUtils.printInfo("Type        : " + rs.getString("type"));
                CLIUtils.printInfo("Status      : " + rs.getString("status"));
                CLIUtils.printInfo("Officer ID  : " + rs.getInt("officer_id"));
                CLIUtils.printInfo("Filed On    : " + rs.getString("filed_on"));
                CLIUtils.printInfo("----------------------------------");
            }

            ActionTracker.log("Admin", "Viewed all complaints in the system");
            
        } catch (SQLException e) {
            CLIUtils.printError("Database error fetching all complaints: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error fetching all complaints: " + e.getMessage());
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

    // Show complaint tracker for a user
    public static void showComplaintTracker(int userId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return;
            }
            
            String sql = "SELECT complaint_id, status FROM complaints WHERE user_id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int cid = rs.getInt("complaint_id");
                String status = rs.getString("status");
                CLIUtils.printInfo("Complaint ID: " + cid + " | Status: ");
                switch (status) {
                    case "OPEN":
                        CLIUtils.printInfo("🟢 Open");
                        break;
                    case "IN_PROGRESS":
                        CLIUtils.printInfo("🟡 Under Process");
                        break;
                    case "CLOSED":
                        CLIUtils.printInfo("✅ Closed");
                        break;
                    case "CONDITIONAL_CLOSED":
                        CLIUtils.printInfo("🟠 Conditional Closed");
                        break;
                    default:
                        CLIUtils.printInfo(status);
                }
            }
            
        } catch (SQLException e) {
            CLIUtils.printError("Database error fetching complaint tracker: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error fetching complaint tracker: " + e.getMessage());
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

    public static int getLatestComplaintId(int userId) {
        int id = -1;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed.");
                return -1;
            }
            
            String sql = "SELECT complaint_id FROM complaints WHERE user_id = ? ORDER BY complaint_id DESC LIMIT 1";
            ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                id = rs.getInt("complaint_id");
            }

        } catch (SQLException e) {
            CLIUtils.printError("Database error fetching latest complaint ID: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error fetching latest complaint ID: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                CLIUtils.printError("Error closing database resources: " + e.getMessage());
            }
        }
        return id;
    }

    public static int getTotalComplaintCount() {
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
            
            String sql = "SELECT COUNT(*) FROM complaints";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            
        } catch (SQLException e) {
            CLIUtils.printError("Database error counting complaints: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("Error counting complaints: " + e.getMessage());
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
