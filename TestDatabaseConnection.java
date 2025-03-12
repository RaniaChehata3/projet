import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String password = ""; // Default XAMPP has empty password
        
        // First test connection to MySQL server
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Successfully connected to MySQL server!");
            
            // Now try to create the sahacare database if it doesn't exist
            try {
                conn.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS sahacare");
                System.out.println("✅ sahacare database exists or was created");
                
                // Test connection to the specific database
                Connection dbConn = DriverManager.getConnection(url + "sahacare", user, password);
                System.out.println("✅ Successfully connected to sahacare database!");
                dbConn.close();
            } catch (SQLException e) {
                System.out.println("❌ Error accessing/creating sahacare database: " + e.getMessage());
            }
            
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ Error connecting to MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 