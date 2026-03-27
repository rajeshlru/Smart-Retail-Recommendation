import java.sql.*;
import java.util.*;

public class RandomTransactionGenerator {

    Random rand = new Random();

    public void generateSmartTransactions(int count) {

        try (Connection con = DBConnection.getConnection()) {

            for (int i = 0; i < count; i++) {

                con.setAutoCommit(false);

                String transactionQuery = "INSERT INTO transactions (customer_name) VALUES (?)";
                PreparedStatement pstTransaction = con.prepareStatement(transactionQuery,
                        Statement.RETURN_GENERATED_KEYS);

                pstTransaction.setString(1, "AutoUser_" + (i + 1));
                pstTransaction.executeUpdate();

                ResultSet rs = pstTransaction.getGeneratedKeys();
                int transactionId = 0;

                if (rs.next()) {
                    transactionId = rs.getInt(1);
                }

                List<List<Integer>> productGroups = getProductGroups();

                List<Integer> selectedGroup = productGroups.get(rand.nextInt(productGroups.size()));

                String itemQuery = "INSERT INTO transaction_items (transaction_id, product_id, quantity) VALUES (?, ?, ?)";
                PreparedStatement pstItem = con.prepareStatement(itemQuery);

                String stockQuery = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
                PreparedStatement pstStock = con.prepareStatement(stockQuery);

                for (int productId : selectedGroup) {

                    int quantity = 1 + rand.nextInt(2);

                    pstItem.setInt(1, transactionId);
                    pstItem.setInt(2, productId);
                    pstItem.setInt(3, quantity);
                    pstItem.executeUpdate();

                    pstStock.setInt(1, quantity);
                    pstStock.setInt(2, productId);
                    pstStock.executeUpdate();
                }

                con.commit();
            }

            System.out.println("\n🔥 Smart random transactions generated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==============================
    // REALISTIC PRODUCT GROUPS
    // ==============================
    private List<List<Integer>> getProductGroups() {

        List<List<Integer>> groups = new ArrayList<>();

        groups.add(Arrays.asList(1, 6, 7, 11));

        groups.add(Arrays.asList(3, 9, 8, 12, 13));

        groups.add(Arrays.asList(4, 9, 13, 15));

        groups.add(Arrays.asList(21, 22, 28, 29));

        groups.add(Arrays.asList(16, 17, 18, 19, 20));

        groups.add(Arrays.asList(31, 32, 33, 34, 35));

        groups.add(Arrays.asList(46, 47, 48, 50));

        return groups;
    }
}