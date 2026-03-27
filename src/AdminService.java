import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class AdminService {

    // ==============================
    // SHOW ALL TRANSACTIONS SUMMARY
    // ==============================

    public void viewAllTransactions(Scanner sc) {

        while (true) {

            String summaryQuery = """
                        SELECT t.transaction_id, t.customer_name, t.transaction_date,
                               SUM(p.price * ti.quantity) AS total_amount
                        FROM transactions t
                        JOIN transaction_items ti ON t.transaction_id = ti.transaction_id
                        JOIN products p ON ti.product_id = p.product_id
                        GROUP BY t.transaction_id, t.customer_name, t.transaction_date
                        ORDER BY t.transaction_id ASC
                    """;

            try (Connection con = DBConnection.getConnection();
                    PreparedStatement pst = con.prepareStatement(summaryQuery);
                    ResultSet rs = pst.executeQuery()) {

                System.out.println("\n================ ALL TRANSACTIONS ================");
                System.out.printf("%-5s %-15s %-22s %-15s%n", "ID", "Customer", "Date", "Total");
                System.out.println("--------------------------------------------------");

                boolean found = false;

                while (rs.next()) {
                    found = true;
                    System.out.printf(
                            "%-5d %-15s %-22s Rs.%-15.2f%n",
                            rs.getInt("transaction_id"),
                            rs.getString("customer_name"),
                            rs.getString("transaction_date"),
                            rs.getDouble("total_amount"));
                }

                if (!found) {
                    System.out.println("No transactions found.");
                    return;
                }

                System.out.print("\nEnter Transaction ID (0 to go back): ");
                int transactionId = sc.nextInt();
                sc.nextLine();

                if (transactionId == 0) {
                    return;
                }

                showTransactionDetails(transactionId);

                System.out.println("\nPress Enter to go back to transaction list...");
                sc.nextLine();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ==============================
    // SHOW FULL DETAILS OF ONE TRANSACTION
    // ==============================
    public void showTransactionDetails(int transactionId) {
        String transactionQuery = "SELECT * FROM transactions WHERE transaction_id = ?";
        String itemQuery = """
                    SELECT p.product_name, ti.quantity, p.price
                    FROM transaction_items ti
                    JOIN products p ON ti.product_id = p.product_id
                    WHERE ti.transaction_id = ?
                """;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement pst1 = con.prepareStatement(transactionQuery);
            pst1.setInt(1, transactionId);
            ResultSet rs1 = pst1.executeQuery();

            if (!rs1.next()) {
                System.out.println("Invalid Transaction ID!");
                return;
            }

            System.out.println("\n================ TRANSACTION DETAILS ================");
            System.out.println("Transaction ID : " + rs1.getInt("transaction_id"));
            System.out.println("Customer Name  : " + rs1.getString("customer_name"));
            System.out.println("Date           : " + rs1.getString("transaction_date"));
            System.out.println("\nItems:");

            PreparedStatement pst2 = con.prepareStatement(itemQuery);
            pst2.setInt(1, transactionId);
            ResultSet rs2 = pst2.executeQuery();

            double total = 0;

            while (rs2.next()) {
                String productName = rs2.getString("product_name");
                int quantity = rs2.getInt("quantity");
                double price = rs2.getDouble("price");
                double subtotal = quantity * price;
                total += subtotal;

                System.out.println("- " + productName + " x" + quantity + " | Rs." + subtotal);
            }

            System.out.println("\nTotal Amount   : Rs." + total);
            System.out.println("====================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}