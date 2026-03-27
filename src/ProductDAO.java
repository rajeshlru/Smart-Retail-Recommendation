
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProductDAO {

    public void showProductsByCategory(int categoryId) {
        String query = "SELECT * FROM products WHERE category_id = ? ORDER BY product_id";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, categoryId);
            ResultSet rs = pst.executeQuery();

            System.out.println(
                    "\n====================================================================================================");
            System.out.println("                                 PRODUCTS IN SELECTED CATEGORY");
            System.out.println(
                    "====================================================================================================");
            System.out.printf("%-6s %-35s %-18s %-14s %-8s%n",
                    "ID", "Product Name", "Subcategory", "Price", "Stock");
            System.out.println(
                    "----------------------------------------------------------------------------------------------------");

            boolean found = false;

            while (rs.next()) {
                found = true;

                String productName = rs.getString("product_name");
                if (productName.length() > 33) {
                    productName = productName.substring(0, 30) + "...";
                }

                System.out.printf("%-6d %-35s %-18s Rs.%-11.2f %-8d%n",
                        rs.getInt("product_id"),
                        productName,
                        rs.getString("subcategory"),
                        rs.getDouble("price"),
                        rs.getInt("stock"));
            }

            System.out.println(
                    "====================================================================================================");

            if (!found) {
                System.out.println("No products found in this category.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Product getProductById(int productId) {
        String query = "SELECT * FROM products WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("category_id"),
                        rs.getString("subcategory"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("brand"),
                        rs.getDouble("rating"),
                        rs.getString("description"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isProductInCategory(int productId, int categoryId) {
        String query = "SELECT * FROM products WHERE product_id = ? AND category_id = ?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, productId);
            pst.setInt(2, categoryId);

            ResultSet rs = pst.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}