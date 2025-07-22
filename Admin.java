import java.util.Scanner;

public class Admin extends User {

    public Admin(int userId, String username, String password) {
        super(userId, username, password);
    }

    @Override
    public void showMenu(Scanner sc) {
        int choice = -1;

        do {
            // MODIFIED: "Export Data" removed, other options re-numbered.
            String[] options = {
                    "1. View All Complaints",
                    "2. View All Crime Reports",
                    "3. Generate .txt Report",
                    "4. View Logs",
                    "5. Manage Users",
                    "6. System Settings",
                    "7. FAQs",
                    "8. Logout"
            };
            // MODIFIED: The choice range is now 1-8.
            CLIUtils.printBoxedMenu("Admin Menu", options);
            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 8);

            // MODIFIED: Switch cases re-numbered.
            switch (choice) {
                case 1:
                    CLIUtils.printInfo("Showing all complaints...");
                    ComplaintManager.viewAllComplaints();
                    break;
                case 2:
                    CLIUtils.printInfo("Showing all crime reports...");
                    CrimeManager.viewAllCrimes();
                    break;
                case 3:
                    handleReportGeneration();
                    break;
                case 4:
                    CLIUtils.printInfo("Viewing system logs...");
                    ActionTracker.viewLog();
                    ActionTracker.log("Admin", "Viewed logs");
                    break;
                case 5:
                    manageUsers(sc);
                    break;
                case 6: // Was previously case 7
                    systemSettings(sc);
                    break;
                case 7: // Was previously case 8
                    showFAQs();
                    break;
                case 8: // Was previously case 9
                    CLIUtils.printInfo("Logged out successfully.");
                    ActionTracker.log("Admin", "Logged out");
                    break;
                default:
                    CLIUtils.printError("Invalid choice. Please select 1-8.");
            }
            if (choice != 8) CLIUtils.waitForEnter(sc);
        } while (choice != 8);
    }

    private void manageUsers(Scanner sc) {
        int choice = -1;
        do {
            String[] options = {
                    "1. View All Citizens",
                    "2. Delete a Citizen",
                    "3. Back to Admin Menu"
            };
            CLIUtils.printBoxedMenu("User Management", options);
            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 3);

            switch(choice) {
                case 1:
                    UserManager.viewAllUsers();
                    break;
                case 2:
                    int userIdToDelete = CLIUtils.promptInt(sc, "Enter Citizen User ID to delete: ", 1, Integer.MAX_VALUE);
                    UserManager.deleteUser(userIdToDelete);
                    break;
                case 3:
                    return;
            }
            if (choice != 3) CLIUtils.waitForEnter(sc);
        } while(choice != 3);
    }

    // REMOVED the exportData(Scanner sc) method.

    private void systemSettings(Scanner sc) {
        int choice = -1;
        do {
            String[] options = {
                    "1. Clear Action Log",
                    "2. Archive Old Complaints",
                    "3. View System Paths",
                    "4. Back to Admin Menu"
            };
            CLIUtils.printBoxedMenu("System Settings", options);
            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 4);

            switch(choice) {
                case 1:
                    CLIUtils.printInfo("Clearing action log...");
                    ActionTracker.clearLog();
                    ActionTracker.log("Admin", "Cleared the action log");
                    break;
                case 2:
                    CLIUtils.printInfo("Archiving old complaints...");
                    ReportGenerator.archiveOldComplaints();
                    ActionTracker.log("Admin", "Archived old complaints");
                    break;
                case 3:
                    viewSystemPaths();
                    break;
                case 4:
                    return; // Go back
            }
            if (choice != 4) CLIUtils.waitForEnter(sc);
        } while(choice != 4);
    }

    private void viewSystemPaths() {
        try {
            String path = System.getProperty("user.dir");
            CLIUtils.printInfo("All reports, exports, and logs are saved to:");
            CLIUtils.printInfo(path);
        } catch (Exception e) {
            CLIUtils.printError("Could not retrieve system path: " + e.getMessage());
        }
    }

    private void handleReportGeneration() {
        CLIUtils.printInfo("Generating report file...");
        try {
            ReportGenerator.generateAllComplaintsReport();
            ActionTracker.log("Admin", "Generated system-wide .txt report");
        } catch (Exception e) {
            CLIUtils.printError("Error generating report: " + e.getMessage());
        }
    }

    private void showFAQs() {
        String[] faqs = {
                "Q: How do I generate a report?",
                "A: Use the 'Generate .txt Report' option.",
                "Q: How do I manage users?",
                "A: Use the 'Manage Users' option to view or delete citizens.",
                "Q: How do I export data?",
                "A: Use the 'Export Data' option to get a TXT file of all complaints.",
                "Q: How do I view system logs?",
                "A: Use the 'View Logs' option to see all system activities.",
                "Q: What are System Settings?",
                "A: This area contains maintenance tasks like clearing logs or archiving old data."
        };
        CLIUtils.printBoxedInfo("Admin FAQs", faqs);
    }
}
