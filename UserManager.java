import java.sql.*;

public class UserManager {

    public static Citizen registerUser(String username, String password, String name, String countryCode, String phone,
                                       String state, String city, String landmark, String houseNo, int age, String email) {
        int userId = -1;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    name == null || name.trim().isEmpty()) {
                System.out.println("❌ Username, password, and name are required.");
                return null;
            }

            con = DBConnection.connect();
            String checkSql = "SELECT user_id FROM users WHERE username = ?";
            ps = con.prepareStatement(checkSql);
            ps.setString(1, username.trim());
            rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("❌ Username already exists.");
                return null;
            }

            String sql = "INSERT INTO users (username, password, name, country_code, phone, state, city, landmark, house_no, age, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, username.trim());
            ps.setString(2, password.trim()); // Store plain-text password
            ps.setString(3, name.trim());
            ps.setString(4, countryCode.trim());
            ps.setString(5, phone.trim());
            ps.setString(6, state.trim());
            ps.setString(7, city.trim());
            ps.setString(8, landmark.trim());
            ps.setString(9, houseNo.trim());
            ps.setInt(10, age);
            ps.setString(11, email.trim());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error registering user: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) { }
        }

        if (userId != -1) {
            return new Citizen(userId, username, password, name, countryCode, phone, state, city, landmark, houseNo, age, email);
        }
        return null;
    }

    public static Citizen getUser(String username, String password) {
        Citizen c = null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, username.trim());
            ps.setString(2, password.trim());
            rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String name = rs.getString("name");
                String countryCode = rs.getString("country_code");
                String phone = rs.getString("phone");
                String state = rs.getString("state");
                String city = rs.getString("city");
                String landmark = rs.getString("landmark");
                String houseNo = rs.getString("house_no");
                int age = rs.getInt("age");
                String email = rs.getString("email");
                c = new Citizen(userId, username, password, name, countryCode, phone, state, city, landmark, houseNo, age, email);
            }

        } catch (Exception e) {
            System.out.println("❌ Error fetching user: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {}
        }
        return c;
    }

    public static int getCitizenCount() {
        int count = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.connect();
            String sql = "SELECT COUNT(*) FROM users";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) {
            System.out.println("❌ Error counting citizens: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {}
        }
        return count;
    }

    public static void viewAllUsers() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = DBConnection.connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT user_id, username, name, city FROM users");

            CLIUtils.printInfo("\n--- All Registered Citizens ---");
            while(rs.next()) {
                String userInfo = String.format("ID: %-5d | Username: %-15s | Name: %-20s | City: %s",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("city"));
                CLIUtils.printInfo(userInfo);
            }
            CLIUtils.printInfo("-----------------------------");
            ActionTracker.log("Admin", "Viewed all citizen users");

        } catch (Exception e) {
            CLIUtils.printError("Error viewing users: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {}
        }
    }

    public static void deleteUser(int userId) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.connect();
            ps = con.prepareStatement("DELETE FROM complaints WHERE user_id = ?");
            ps.setInt(1, userId);
            ps.executeUpdate();

            ps = con.prepareStatement("DELETE FROM suggestions WHERE user_id = ?");
            ps.setInt(1, userId);
            ps.executeUpdate();

            ps = con.prepareStatement("DELETE FROM users WHERE user_id = ?");
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                CLIUtils.printSuccess("✅ User " + userId + " and their associated records have been deleted.");
                ActionTracker.log("Admin", "Deleted user with ID: " + userId);
            } else {
                CLIUtils.printError("User with ID " + userId + " not found.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error deleting user: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {}
        }
    }

    public static void updateUserProfile(int userId, int choice, String newValue) {
        Connection con = null;
        PreparedStatement ps = null;
        String fieldName = "";

        switch (choice) {
            case 1:
                fieldName = "phone";
                break;
            case 2:
                fieldName = "email";
                break;
            case 3:
                fieldName = "city";
                break;
            default:
                CLIUtils.printError("Invalid field choice.");
                return;
        }

        String sql = "UPDATE users SET " + fieldName + " = ? WHERE user_id = ?";

        try {
            con = DBConnection.connect();
            ps = con.prepareStatement(sql);
            ps.setString(1, newValue);
            ps.setInt(2, userId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                CLIUtils.printSuccess("✅ Profile updated successfully!");
                ActionTracker.log("Citizen_" + userId, "Updated profile field: " + fieldName);
            } else {
                CLIUtils.printError("Profile update failed. User not found.");
            }

        } catch (SQLException e) {
            CLIUtils.printError("Database error updating profile: " + e.getMessage());
        } catch (Exception e) {
            CLIUtils.printError("An unexpected error occurred: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {}
        }
    }
}
