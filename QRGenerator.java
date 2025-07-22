import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
// Import necessary SQL classes
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QRGenerator {

    public static void generateQRCode(int complaintId) {
        String qrContent = "";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Step 1: Connect to the database to fetch details.
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed. Cannot generate QR code details.");
                return;
            }

            // SQL query to join complaints and users tables to get all required info
            String sql = "SELECT c.description, c.filed_on, u.user_id, u.name " +
                    "FROM complaints c " +
                    "JOIN users u ON c.user_id = u.user_id " +
                    "WHERE c.complaint_id = ?";

            ps = con.prepareStatement(sql);
            ps.setInt(1, complaintId);
            rs = ps.executeQuery();

            if (rs.next()) {
                // Step 2: Retrieve the details from the database result.
                String citizenName = rs.getString("name");
                int userId = rs.getInt("user_id");
                String description = rs.getString("description");
                String filedOn = rs.getString("filed_on");

                // Step 3: Build the new content string for the QR code.
                qrContent = "--- RapidResolve Complaint Details ---\n" +
                        "Complaint ID: " + complaintId + "\n" +
                        "Citizen Name: " + citizenName + "\n" +
                        "Citizen ID: " + userId + "\n" +
                        "Description: " + description + "\n" +
                        "Filed On: " + filedOn;

            } else {
                CLIUtils.printError("Could not find details for complaint ID: " + complaintId);
                return;
            }

            // --- The rest of the QR generation logic remains the same ---

            String filename = "Complaint_" + complaintId + "_QR.png";
            int width = 400;
            int height = 400;

            BitMatrix matrix = new MultiFormatWriter().encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            Path path = new File(filename).toPath();
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);

            CLIUtils.printSuccess("✅ QR Code generated successfully: " + filename);
            openFileInWindows(filename);

        } catch (Exception e) {
            CLIUtils.printError("Error generating QR code: " + e.getMessage());
        } finally {
            // Step 4: Close all database resources.
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) {
                CLIUtils.printError("Error closing DB resources in QRGenerator: " + e.getMessage());
            }
        }
    }

    // This method for generating a user QR code remains unchanged.
    public static void generateQRForUser(int userId) {
        try {
            if (userId <= 0) {
                CLIUtils.printError("Invalid user ID. Must be a positive number.");
                return;
            }

            String qrContent = "RapidResolve User ID: " + userId + "\n" +
                    "Access your complaints at rapidresolve.gov.in\n" +
                    "Generated: " + java.time.LocalDate.now();

            String filename = "User_" + userId + "_QR.png";

            generateQRFile(filename, qrContent);

            CLIUtils.printSuccess("✅ User QR Code generated successfully: " + filename);
            CLIUtils.printInfo("QR Code contains user access information.");

            openFileInWindows(filename);

        } catch (Exception e) {
            CLIUtils.printError("Error generating user QR code: " + e.getMessage());
        }
    }

    private static void generateQRFile(String filename, String content) {
        try {
            int width = 300;
            int height = 300;

            BitMatrix matrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            Path path = new File(filename).toPath();
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        } catch (Exception e) {
            CLIUtils.printError("QR Code generation failed: " + e.getMessage());
        }
    }

    private static void openFileInWindows(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                CLIUtils.printError("QR file does not exist to open.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error opening QR file: " + e.getMessage());
        }
    }
}
