import java.sql.*;

public class OfficerManager {

    public static Officer getOfficer(String username, String password) {
        Officer officer = null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.connect();
            String sql = "SELECT * FROM officers WHERE username = ? AND password = ?";
            ps = con.prepareStatement(sql);

            ps.setString(1, username.trim());
            ps.setString(2, password.trim());
            rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("officer_id");
                // Removed reading 'name' from result set
                int assignedCount = rs.getInt("assigned_count");
                String area = rs.getString("area");
                // This call now matches the updated Officer constructor
                officer = new Officer(userId, username, password, assignedCount, area);
            }
        } catch (Exception e) {
            System.out.println("Error fetching officer: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
        return officer;
    }

    public static int assignOfficer(String area) {
        int officerId = -1;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (area == null || area.trim().isEmpty()) {
                System.out.println("❌ Area cannot be null or empty.");
                return -1;
            }

            con = DBConnection.connect();
            if (con == null) {
                System.out.println("❌ Database connection failed.");
                return -1;
            }

            String sql = "SELECT o.officer_id, COUNT(c.complaint_id) as complaint_count " +
                    "FROM officers o " +
                    "LEFT JOIN complaints c ON o.officer_id = c.officer_id " +
                    "GROUP BY o.officer_id " +
                    "ORDER BY complaint_count ASC " +
                    "LIMIT 1";

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                officerId = rs.getInt("officer_id");
            }

        } catch (SQLException e) {
            System.out.println("❌ Database error assigning officer: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error assigning officer: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("❌ Error closing database resources: " + e.getMessage());
            }
        }

        return officerId;
    }

    public static int getOfficerCount() {
        int count = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            String sql = "SELECT COUNT(*) FROM officers";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);

        } catch (Exception e) {
            System.out.println("❌ Error counting officers: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) { }
        }
        return count;
    }

    public static void viewAllOfficers() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            String sql = "SELECT * FROM officers";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            System.out.println("\n👮 All Officers:");
            while (rs.next()) {
                System.out.println("Officer ID: " + rs.getInt("officer_id"));
                System.out.println("Username: " + rs.getString("username")); // Changed from Name
                System.out.println("Assigned Count: " + rs.getInt("assigned_count"));
                System.out.println("-----------------------------");
            }

        } catch (Exception e) {
            System.out.println("❌ Error viewing officers: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) { }
        }
    }

    public static void viewOfficersByArea(String area) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean found = false;
        try {
            con = DBConnection.connect();
            String sql = "SELECT * FROM officers WHERE LOWER(area) LIKE ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + area.toLowerCase() + "%");
            rs = ps.executeQuery();
            System.out.println("\n👮 Officers in area/city: " + area);
            while (rs.next()) {
                found = true;
                System.out.println("Officer ID: " + rs.getInt("officer_id"));
                System.out.println("Username: " + rs.getString("username")); // Changed from Name
                System.out.println("Assigned Count: " + rs.getInt("assigned_count"));
                System.out.println("Area: " + rs.getString("area"));
                System.out.println("-----------------------------");
            }
            if (!found) {
                System.out.println("No officers found for area/city: " + area);
            }
        } catch (Exception e) {
            CLIUtils.printError("Error viewing officers: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) { }
        }
    }
}