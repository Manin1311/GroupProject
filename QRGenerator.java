import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;

public class QRGenerator {

    public static void generateQRCode(int complaintId) {
        try {
            if (complaintId <= 0) {
                CLIUtils.printError("Invalid complaint ID. Must be a positive number.");
                return;
            }

            String qrContent = "RapidResolve Complaint ID: " + complaintId + "\n" +
                    "Status: Check at rapidresolve.gov.in\n" +
                    "Generated: " + java.time.LocalDate.now();

            String filename = "Complaint_" + complaintId + "_QR.png";

            generateQRFile(filename, qrContent);

            CLIUtils.printSuccess("✅ QR Code generated successfully: " + filename);
            CLIUtils.printInfo("QR Code contains complaint tracking information.");

            openFileInWindows(filename);

        } catch (Exception e) {
            CLIUtils.printError("Error generating QR code: " + e.getMessage());
        }
    }

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
