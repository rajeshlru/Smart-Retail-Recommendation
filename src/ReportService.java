import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReportService {

    public void showTopSellingProducts() {
        String query = """
                    SELECT p.product_name, SUM(ti.quantity) AS total_sold
                    FROM transaction_items ti
                    JOIN products p ON ti.product_id = p.product_id
                    GROUP BY p.product_name
                    ORDER BY total_sold DESC
                    LIMIT 5
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            System.out.println("\n===== TOP SELLING PRODUCTS =====");

            int rank = 1;
            while (rs.next()) {
                System.out.println(rank + ". " + rs.getString("product_name") +
                        " | Sold: " + rs.getInt("total_sold"));
                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTopCategories() {
        String query = """
                    SELECT c.category_name, SUM(ti.quantity) AS total_sold
                    FROM transaction_items ti
                    JOIN products p ON ti.product_id = p.product_id
                    JOIN categories c ON p.category_id = c.category_id
                    GROUP BY c.category_name
                    ORDER BY total_sold DESC
                    LIMIT 5
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            System.out.println("\n===== TOP CATEGORIES =====");

            int rank = 1;
            while (rs.next()) {
                System.out.println(rank + ". " + rs.getString("category_name") +
                        " | Items Sold: " + rs.getInt("total_sold"));
                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMostBoughtTogether() {
        String query = """
                    SELECT p1.product_name AS product1, p2.product_name AS product2, COUNT(*) AS pair_count
                    FROM transaction_items t1
                    JOIN transaction_items t2
                        ON t1.transaction_id = t2.transaction_id
                        AND t1.product_id < t2.product_id
                    JOIN products p1 ON t1.product_id = p1.product_id
                    JOIN products p2 ON t2.product_id = p2.product_id
                    GROUP BY p1.product_name, p2.product_name
                    ORDER BY pair_count DESC
                    LIMIT 5
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            System.out.println("\n===== MOST FREQUENTLY BOUGHT TOGETHER =====");

            int rank = 1;
            while (rs.next()) {
                System.out.println(rank + ". " +
                        rs.getString("product1") + " + " +
                        rs.getString("product2") +
                        " | Count: " + rs.getInt("pair_count"));
                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTrendingProducts() {
        String query = """
                    SELECT p.product_name, COUNT(*) AS trend_count
                    FROM transaction_items ti
                    JOIN products p ON ti.product_id = p.product_id
                    GROUP BY p.product_name
                    ORDER BY trend_count DESC
                    LIMIT 5
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            System.out.println("\n===== TRENDING PRODUCTS =====");

            int rank = 1;
            while (rs.next()) {
                System.out.println(rank + ". " +
                        rs.getString("product_name") +
                        " | Trend Score: " + rs.getInt("trend_count"));
                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTotalRevenue() {
        String query = """
                    SELECT SUM(p.price * ti.quantity) AS total_revenue
                    FROM transaction_items ti
                    JOIN products p ON ti.product_id = p.product_id
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            System.out.println("\n===== TOTAL SALES REVENUE =====");

            if (rs.next()) {
                System.out.println("Total Revenue: Rs." + rs.getDouble("total_revenue"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}