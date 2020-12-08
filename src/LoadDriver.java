import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// Don't import com.mysql.jdbc.* or you'll have problems
public class LoadDriver {
    public static void main(String[] args) {
        System.out.println("Loading the MySQL instance...");
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Load successful.");
        } catch (Exception ex) {
            // handle the error
            System.out.println("Error when loading the instance.");
            System.out.println(ex);
        }
    }
}