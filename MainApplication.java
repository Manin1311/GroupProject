import java.util.Scanner;

public class MainApplication {
    public static void main(String[] args) {
        Scanner sc = null;
        try {
            sc = new Scanner(System.in);
            CLIUtils.clearScreen();
            CLIUtils.printSingleBlockHeading();
            boolean running = true;
            while (running) {
                try {
                    String[] options = {
                            "1. Sign Up as Citizen",
                            "2. Login as Citizen",
                            "3. Login as Officer",
                            "4. Login as Admin",
                            "5. View System Statistics",
                            "6. About RapidResolve",
                            "7. Help / Instructions",
                            "8. Exit"
                    };
                    CLIUtils.printBoxedMenu("Main Menu", options);

                    int choice = CLIUtils.promptInt(sc, "Enter choice: ", 1, 8);

                    switch (choice) {
                        case 1:
                            signUpFlow(sc);
                            break;
                        case 2:
                            loginCitizenFlow(sc);
                            break;
                        case 3:
                            loginOfficerFlow(sc);
                            break;
                        case 4:
                            loginAdminFlow(sc);
                            break;
                        case 5:
                            showSystemStats();
                            break;
                        case 6:
                            showAbout();
                            break;
                        case 7:
                            showHelp();
                            break;
                        case 8:
                            CLIUtils.printInfo("Thank you for using RapidResolve. Goodbye!");
                            running = false;
                            break;
                        default:
                            CLIUtils.printError("Invalid choice. Please select 1-8.");
                    }
                    if (running) CLIUtils.waitForEnter();
                } catch (Exception e) {
                    CLIUtils.printError("An unexpected error occurred: " + e.getMessage());
                    CLIUtils.printInfo("Please try again.");
                    CLIUtils.waitForEnter();
                }
            }
        } catch (Exception e) {
            CLIUtils.printError("Critical error: " + e.getMessage());
        } finally {
            if (sc != null) {
                try {
                    sc.close();
                } catch (Exception e) {
                    // Ignore scanner close errors
                }
            }
        }
    }

    private static void signUpFlow(Scanner sc) {
        try {
            CLIUtils.printInfo("Sign Up - Enter your details:");

            String username = CLIUtils.promptString(sc, "Username: ", true, 3, 20, ".*[a-zA-Z].*", "Username must contain at least one letter and cannot be purely numeric.");
            String password = CLIUtils.promptString(sc, "Password: ", true, 6, 30, ".*[a-zA-Z].*", "Password must contain at least one letter and cannot be purely numeric.");
            String name = CLIUtils.promptString(sc, "Full Name: ", true, 2, 40, null, null);
            String countryCode = CLIUtils.promptString(sc, "Country Code (+91, etc): ", true, 2, 5, "^\\+\\d+$", "Country code must start with + and be numeric (e.g., +91). ");
            String phone = CLIUtils.promptString(sc, "Phone Number (10 digits): ", true, 10, 10, "^\\d{10}$", "Phone number must be exactly 10 digits.");

            // OTP Verification
            int otp = 1000 + new java.util.Random().nextInt(9000);
            CLIUtils.printInfo("Your OTP is: " + otp);
            int enteredOtp = CLIUtils.promptInt(sc, "Enter OTP: ", 1000, 9999);
            if (enteredOtp != otp) {
                CLIUtils.printError("Invalid OTP! Please try again.");
                return;
            }

            String state = CLIUtils.promptString(sc, "State: ", true, 2, 30, null, null);
            String city = CLIUtils.promptString(sc, "City: ", true, 2, 30, null, null);
            String landmark = CLIUtils.promptString(sc, "Landmark: ", true, 2, 30, null, null);
            String houseNo = CLIUtils.promptString(sc, "House No: ", true, 1, 10, null, null);
            int age = CLIUtils.promptInt(sc, "Age: ", 1, 120);
            String email = CLIUtils.promptString(sc, "Email: ", true, 5, 50, "^[A-Za-z0-9+_.-]+@(.+)$", "Please enter a valid email address.");

            Citizen citizen = UserManager.registerUser(username, password, name, countryCode, phone, state, city, landmark, houseNo, age, email);
            if (citizen == null) {
                CLIUtils.printError("Registration failed! Username may already exist or there was a database error.");
            } else {
                CLIUtils.printSuccess("Registration successful! Please login to continue.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error during registration: " + e.getMessage());
        }
    }

    private static void loginCitizenFlow(Scanner sc) {
        try {
            String username = CLIUtils.promptString(sc, "Username: ", true, 3, 20, ".*[a-zA-Z].*", "Username must contain at least one letter and cannot be purely numeric.");
            String password = CLIUtils.promptString(sc, "Password: ", true, 6, 30, ".*[a-zA-Z].*", "Password must contain at least one letter and cannot be purely numeric.");

            Citizen citizen = UserManager.getUser(username, password);
            if (citizen == null) {
                CLIUtils.printError("Invalid credentials or user not found.");
                return;
            }
            citizen.showMenu(sc);
        } catch (Exception e) {
            CLIUtils.printError("Error during login: " + e.getMessage());
        }
    }

    private static void loginOfficerFlow(Scanner sc) {
        try {
            String username = CLIUtils.promptString(sc, "Officer Username: ", true, 3, 20, ".*[a-zA-Z].*", "Username must contain at least one letter and cannot be purely numeric.");
            String password = CLIUtils.promptString(sc, "Officer Password: ", true, 6, 30, ".*[a-zA-Z].*", "Password must contain at least one letter and cannot be purely numeric.");

            Officer officer = OfficerManager.getOfficer(username, password);
            if (officer != null) {
                officer.showMenu(sc);
            } else {
                CLIUtils.printError("Invalid officer credentials.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error during officer login: " + e.getMessage());
        }
    }

    private static void loginAdminFlow(Scanner sc) {
        try {
            String username = CLIUtils.promptString(sc, "Admin Username: ", true, 3, 20, ".*[a-zA-Z].*", "Username must contain at least one letter.");
            String password = CLIUtils.promptString(sc, "Admin Password: ", true, 6, 30, ".*[a-zA-Z].*", "Password must contain at least one letter.");

            Admin admin = AdminManager.getAdmin(username, password);

            if (admin != null) {
                CLIUtils.printSuccess("✅ Admin login successful!");
                admin.showMenu(sc);
            } else {
                CLIUtils.printError("Invalid admin credentials.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error during admin login: " + e.getMessage());
        }
    }

    private static void showSystemStats() {
        try {
            String[] lines = {
                    "Total Registered Citizens: " + UserManager.getCitizenCount(),
                    "Total Officers: " + OfficerManager.getOfficerCount(),
                    "Total Complaints: " + ComplaintManager.getTotalComplaintCount(),
                    "Total Crimes: " + CrimeManager.getTotalCrimeCount()
            };
            CLIUtils.printBoxedInfo("System Statistics", lines);
        } catch (Exception e) {
            CLIUtils.printError("Error loading system statistics: " + e.getMessage());
        }
    }

    private static void showAbout() {
        try {
            String[] lines = {
                    "RapidResolve is a professional Complaint & Crime Management System.",
                    "It enables citizens to file complaints, officers to manage cases,",
                    "and admins to oversee the system with advanced reporting.",
                    "Developed for smart, efficient, and transparent governance."
            };
            CLIUtils.printBoxedInfo("About RapidResolve", lines);
        } catch (Exception e) {
            CLIUtils.printError("Error displaying about information: " + e.getMessage());
        }
    }

    private static void showHelp() {
        try {
            String[] lines = {
                    "1. Sign Up as Citizen: Register yourself to use the system.",
                    "2. Login as Citizen: Access your dashboard and file complaints.",
                    "3. Login as Officer: Officers can manage assigned complaints.",
                    "4. Login as Admin: Admins can view all data and generate reports.",
                    "5. System Stats: View live statistics of the platform.",
                    "6. About: Learn about RapidResolve.",
                    "7. Help: View instructions for using the system.",
                    "8. Exit: Close the application."
            };
            CLIUtils.printBoxedInfo("Help / Instructions", lines);
        } catch (Exception e) {
            CLIUtils.printError("Error displaying help: " + e.getMessage());
        }
    }
}
