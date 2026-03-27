import java.sql.*;
import java.util.ArrayList;

public class TransactionDAO {

    public void saveTransaction(String customerName, ArrayList<CartItem> cart) {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty. Cannot save transaction.");
            return;
        }

        Connection con = null;
        PreparedStatement pstTransaction = null;
        PreparedStatement pstItem = null;
        PreparedStatement pstStock = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String transactionQuery = "INSERT INTO transactions (customer_name) VALUES (?)";
            pstTransaction = con.prepareStatement(transactionQuery, Statement.RETURN_GENERATED_KEYS);
            pstTransaction.setString(1, customerName);
            pstTransaction.executeUpdate();

            rs = pstTransaction.getGeneratedKeys();
            int transactionId = 0;

            if (rs.next()) {
                transactionId = rs.getInt(1);
            }

            String itemQuery = "INSERT INTO transaction_items (transaction_id, product_id, quantity) VALUES (?, ?, ?)";
            pstItem = con.prepareStatement(itemQuery);

            String stockQuery = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
            pstStock = con.prepareStatement(stockQuery);

            for (CartItem item : cart) {
                pstItem.setInt(1, transactionId);
                pstItem.setInt(2, item.getProduct().getProductId());
                pstItem.setInt(3, item.getQuantity());
                pstItem.executeUpdate();

                pstStock.setInt(1, item.getQuantity());
                pstStock.setInt(2, item.getProduct().getProductId());
                pstStock.executeUpdate();
            }

            con.commit();
            System.out.println("\n==============================================");
            System.out.println(" Order placed successfully!");
            System.out.println(" Transaction ID: " + transactionId);
            System.out.println(" Customer Name: " + customerName);
            System.out.println("==============================================");

        } catch (Exception e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }

            e.printStackTrace();

        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstTransaction != null)
                    pstTransaction.close();
                if (pstItem != null)
                    pstItem.close();
                if (pstStock != null)
                    pstStock.close();
                if (con != null)
                    con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}