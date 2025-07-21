import java.sql.*;

public class DBConnection {
    public static void main(String[] args) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/rapidresolve", "root", "");
        System.out.println((con != null) ? "success" : "failed");

    }
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/rapidresolve", "root", "");
    }

    }
