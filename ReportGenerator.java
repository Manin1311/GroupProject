import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportGenerator {

    public static void generateAllComplaintsReport() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        PrintWriter writer = null;

        try {
            con = DBConnection.connect();
            String sql = "SELECT c.*, u.name as user_name, u.phone as user_phone FROM complaints c " +
                    "LEFT JOIN users u ON c.user_id = u.user_id " +
                    "ORDER BY c.filed_on DESC";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            String filename = "All_Complaints_Report.txt";
            writer = new PrintWriter(new FileWriter(filename));

            writer.println("=".repeat(80));
            writer.println("                    RAPIDRESOLVE - ALL COMPLAINTS REPORT");
            writer.println("=".repeat(80));
            writer.println("Generated on: " + LocalDate.now());
            writer.println("Total Complaints: " + ComplaintManager.getTotalComplaintCount());
            writer.println("-".repeat(80));

            while (rs.next()) {
                writer.println("Complaint ID: " + rs.getInt("complaint_id"));
                writer.println("Filed by: " + rs.getString("user_name") + " (" + rs.getString("user_phone") + ")");
                writer.println("Area: " + rs.getString("area"));
                writer.println("Type: " + rs.getString("type"));
                writer.println("Description: " + rs.getString("description"));
                writer.println("Status: " + rs.getString("status"));
                writer.println("Officer ID: " + rs.getInt("officer_id"));
                writer.println("Filed on: " + rs.getString("filed_on"));
                writer.println("-".repeat(40));
            }

            writer.close();
            CLIUtils.printSuccess("✅ Report generated successfully: " + filename);

        } catch (Exception e) {
            CLIUtils.printError("Error generating report: " + e.getMessage());
        } finally {
            try {
                if (writer != null) writer.close();
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) {}
        }
    }

    public static void generateOfficerComplaintsReport(int officerId, String officerName) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        PrintWriter writer = null;
        try {
            con = DBConnection.connect();
            String sql = "SELECT * FROM complaints WHERE officer_id = ? ORDER BY filed_on DESC";
            ps = con.prepareStatement(sql);
            ps.setInt(1, officerId);
            rs = ps.executeQuery();

            String filename = "Officer_" + officerId + "_Report.txt";
            writer = new PrintWriter(new FileWriter(filename));

            writer.println("=".repeat(60));
            writer.println("           RAPIDRESOLVE - OFFICER COMPLAINTS REPORT");
            writer.println("=".repeat(60));
            writer.println("Officer ID: " + officerId);
            writer.println("Officer Name: " + officerName);
            writer.println("Generated on: " + LocalDate.now());
            writer.println("-".repeat(60));

            boolean hasComplaints = false;
            while (rs.next()) {
                hasComplaints = true;
                writer.println("Complaint ID: " + rs.getInt("complaint_id"));
                writer.println("Area: " + rs.getString("area"));
                writer.println("Type: " + rs.getString("type"));
                writer.println("Description: " + rs.getString("description"));
                writer.println("Status: " + rs.getString("status"));
                writer.println("Filed on: " + rs.getString("filed_on"));
                writer.println("-".repeat(30));
            }

            if (!hasComplaints) {
                writer.println("No complaints assigned to this officer.");
            }

            writer.close();
            CLIUtils.printSuccess("✅ Report generated successfully: " + filename);
        } catch (Exception e) {
            CLIUtils.printError("Database error generating officer report: " + e.getMessage());
        } finally {
            try {
                if (writer != null) writer.close();
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) {}
        }
    }

    // REMOVED the exportComplaintsToTXT() method from this file.

    public static void archiveOldComplaints() {
        Connection con = null;
        PreparedStatement psSelect = null;
        ResultSet rs = null;
        PrintWriter writer = null;
        List<Integer> idsToDelete = new ArrayList<>();

        try {
            con = DBConnection.connect();
            con.setAutoCommit(false); // Start transaction

            String selectSql = "SELECT * FROM complaints WHERE (status = 'RESOLVED' OR status = 'CLOSED') AND filed_on <= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)";
            psSelect = con.prepareStatement(selectSql);
            rs = psSelect.executeQuery();

            if (!rs.isBeforeFirst()) { // Check if there are any results
                CLIUtils.printInfo("No complaints older than one year found to archive.");
                con.rollback();
                return;
            }

            String dateStamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String filename = "Archived_Complaints_" + dateStamp + ".txt";
            writer = new PrintWriter(new FileWriter(filename));
            writer.println("Complaints Archived on: " + dateStamp);
            writer.println("========================================\n");

            while (rs.next()) {
                int complaintId = rs.getInt("complaint_id");
                idsToDelete.add(complaintId); // Add ID to our list for deletion

                // Write details to the archive file
                writer.println("Complaint ID: " + complaintId);
                writer.println("User ID     : " + rs.getInt("user_id"));
                writer.println("Status      : " + rs.getString("status"));
                writer.println("Filed On    : " + rs.getString("filed_on"));
                writer.println("Description : " + rs.getString("description"));
                writer.println("------------------------------------");
            }

            if (!idsToDelete.isEmpty()) {
                String deleteSql = "DELETE FROM complaints WHERE complaint_id IN (" +
                        idsToDelete.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
                Statement stmtDelete = con.createStatement();
                int deletedRows = stmtDelete.executeUpdate(deleteSql);
                stmtDelete.close();

                con.commit(); // Commit the transaction
                CLIUtils.printSuccess("✅ Successfully archived and deleted " + deletedRows + " old complaints.");
                CLIUtils.printInfo("Archive file created: " + filename);
            } else {
                con.rollback(); // Rollback if nothing to delete
                CLIUtils.printInfo("No complaints were archived.");
            }

        } catch (Exception e) {
            CLIUtils.printError("Error archiving complaints: " + e.getMessage());
            try {
                if (con != null) con.rollback(); // Rollback on error
            } catch (Exception ex) {
                CLIUtils.printError("Error rolling back transaction: " + ex.getMessage());
            }
        } finally {
            try {
                if(writer != null) writer.close();
                if (rs != null) rs.close();
                if (psSelect != null) psSelect.close();
                if (con != null) con.close();
            } catch (Exception e) {}
        }
    }
}
