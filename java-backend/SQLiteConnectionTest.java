import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLiteConnectionTest {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:../atm.db";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT account_number, balance FROM accounts")) {

            while (rs.next()) {
                System.out.println(
                    "Account: " + rs.getString("account_number")
                    + ", Balance: Rs. " + rs.getInt("balance")
                );
            }
            System.out.println("SQLite JDBC connection successful!");
        }
    }
}
