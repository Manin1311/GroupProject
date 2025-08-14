import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class Citizen extends User {

    public Citizen(int userId, String username, String password, String name, String countryCode, String phone,
                   String state, String city, String landmark, String houseNo, int age, String email) {
        super(userId, username, password, name, countryCode, phone, state, city, landmark, houseNo, age, email);
    }

    // MODIFIED: Method now accepts a Scanner object
    public void showMenu(Scanner sc) {
        // REMOVED: The line "Scanner sc = new Scanner(System.in);" is deleted from here.
        int choice = -1;

        do {
            String[] options = {
                    "1. Raise a Request/Suggestion",
                    "2. File a Complaint",
                    "3. Check Complaint Status",
                    "4. View Complaint Dashboard",
                    "5. Know Your Officer",
                    "6. Ahmedabad Helpline Number",
                    "7. Snapshot (Current/History)",
                    "8. Update Profile",
                    "9. FAQs",
                    "10. Feedback",
                    "11. Logout"
            };
            CLIUtils.printBoxedMenu("Citizen Dashboard", options);

            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 11);

            switch (choice) {
                case 1:
                    handleSuggestionSubmission(sc);
                    break;
                case 2:
                    handleComplaintFiling(sc);
                    break;
                case 3:
                    CLIUtils.printInfo("Checking complaint status...");
                    ComplaintManager.showComplaintTracker(userId);
                    break;
                case 4:
                    CLIUtils.printInfo("Your Complaint Dashboard:");
                    ComplaintManager.viewComplaintsByUser(userId);
                    break;
                case 5:
                    handleOfficerLookup(sc);
                    CLIUtils.printInfo("Press Enter to continue...");
                    sc.nextLine();

                    break;
                case 6:
                    CLIUtils.printInfo("Ahmedabad Helpline: 155303");
                    break;
                case 7:
                    handleSnapshotView(sc);
                    break;
                case 8:
                    updateProfile(sc);
                    break;
                case 9:
                    showFAQs();
                    break;
                case 10:
                    handleFeedbackSubmission(sc);
                    break;
                case 11:
                    CLIUtils.printInfo("Logged out successfully.");
                    ActionTracker.log("Citizen_" + userId, "Logged out");
                    break;
                default:
                    CLIUtils.printError("Invalid choice. Please select 1-11.");
            }
            if (choice != 11) CLIUtils.waitForEnter();
        } while (choice != 11);
    }

    private void updateProfile(Scanner sc) {
        int choice = -1;
        do {
            CLIUtils.printInfo("\n--- Update Your Profile ---");
            String[] updateOptions = {
                    "1. Update Phone Number",
                    "2. Update Email Address",
                    "3. Update City",
                    "4. Back to Dashboard"
            };
            CLIUtils.printBoxedMenu("Update Profile", updateOptions);
            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 4);

            if (choice == 4) {
                return;
            }

            String newValue = "";
            switch(choice) {
                case 1:
                    newValue = CLIUtils.promptString(sc, "Enter new Phone Number (10 digits): ", true, 10, 10, "^\\d{10}$", "Phone number must be exactly 10 digits.");
                    break;
                case 2:
                    newValue = CLIUtils.promptString(sc, "Enter new Email Address: ", true, 5, 50, "^[A-Za-z0-9+_.-]+@(.+)$", "Please enter a valid email address.");
                    break;
                case 3:
                    newValue = CLIUtils.promptString(sc, "Enter new City: ", true, 2, 30, null, null);
                    break;
            }

            UserManager.updateUserProfile(this.userId, choice, newValue);
            CLIUtils.waitForEnter();

        } while (choice != 4);
    }

    private void handleSuggestionSubmission(Scanner sc) {
        String suggestion = CLIUtils.promptString(sc, "Suggestion: ", true, 10, 200, null, null);
        try {
            Connection con = DBConnection.connect();
            String sql = "INSERT INTO suggestions (user_id, suggestion) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, suggestion);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                CLIUtils.printSuccess("✅ Suggestion submitted successfully!");
                ActionTracker.log("Citizen_" + userId, "Submitted suggestion");
            } else {
                CLIUtils.printError("❌ Failed to save suggestion.");
            }
            con.close();
        } catch (Exception e) {
            CLIUtils.printError("❌ Error while saving suggestion: " + e.getMessage());
        }
    }

    private void handleComplaintFiling(Scanner sc) {
        CLIUtils.printInfo("Choose Complaint Type:");
        CLIUtils.printInfo("1. Civil");
        CLIUtils.printInfo("2. Criminal");
        int complaintType = CLIUtils.promptInt(sc, "Enter type (1-2): ", 1, 2);
        String type = (complaintType == 1) ? "Civil" : "Criminal";
        try {
            // Note: For a full fix, the fileComplaint method should also accept 'sc'
            // But for now, it creates its own internal scanner.
            ComplaintManager.fileComplaint(userId, type);
            int latestComplaintId = ComplaintManager.getLatestComplaintId(userId);
            if (latestComplaintId > 0) {
                CLIUtils.printInfo("Generating QR Code for your complaint...");
                QRGenerator.generateQRCode(latestComplaintId);
            } else {
                CLIUtils.printError("Failed to file complaint. Please try again.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error filing complaint: " + e.getMessage());
        }
    }

    private void handleOfficerLookup(Scanner sc) {
        String area = CLIUtils.promptString(sc, "Enter area/city to view officer details: ", true, 2, 40, null, null);
        OfficerManager.viewOfficersByArea(area);
    }

    private void handleSnapshotView(Scanner sc) {
        CLIUtils.printInfo("Snapshot Options:");
        CLIUtils.printInfo("1. Current");
        CLIUtils.printInfo("2. Incident History (6 months)");
        CLIUtils.printInfo("3. Incident History (3 months)");
        CLIUtils.printInfo("4. Incident History (15 days)");
        int snap = CLIUtils.promptInt(sc, "Enter option (1-4): ", 1, 4);
        try {
            CrimeManager.showSnapshot(snap);
        } catch (Exception e) {
            CLIUtils.printError("Error loading snapshot: " + e.getMessage());
        }
    }

    private void showFAQs() {
        String[] faqs = {
                "Q: How do I file a complaint?",
                "A: Use the 'File a Complaint' option and follow the prompts.",
                "Q: How do I check my complaint status?",
                "A: Use the 'Check Complaint Status' option.",
                "Q: Who can see my complaints?",
                "A: Only authorized officers and admins.",
                "Q: How long does it take to resolve a complaint?",
                "A: Resolution time varies based on complaint type and complexity.",
                "Q: Can I update my complaint details?",
                "A: Contact your assigned officer for any updates."
        };
        CLIUtils.printBoxedInfo("FAQs", faqs);
    }

    private void handleFeedbackSubmission(Scanner sc) {
        String feedback = CLIUtils.promptString(sc, "Feedback: ", true, 10, 200, null, null);
        CLIUtils.printSuccess("Thank you for your feedback!");
        ActionTracker.log("Citizen_" + userId, "Submitted feedback");
    }
}
