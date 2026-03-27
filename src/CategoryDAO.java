import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CategoryDAO {

    public void showCategories() {
        String query = "SELECT * FROM categories ORDER BY category_id";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            System.out.println("\n===== AVAILABLE CATEGORIES =====");

            while (rs.next()) {
                System.out.println(rs.getInt("category_id") + ". " + rs.getString("category_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}