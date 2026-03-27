import java.util.ArrayList;

public class CartService {
    private ArrayList<CartItem> cart = new ArrayList<>();

    public void addToCart(Product product, int quantity) {
        cart.add(new CartItem(product, quantity));
        System.out.println(product.getProductName() + " added to cart successfully!");
    }

    public void showCart() {
        if (cart.isEmpty()) {
            System.out.println("\nCart is empty.");
            return;
        }

        System.out.println("\n========== YOUR CART ==========");
        double grandTotal = 0;

        for (CartItem item : cart) {
            System.out.println(
                    item.getProduct().getProductId() + ". " +
                            item.getProduct().getProductName() +
                            " | Qty: " + item.getQuantity() +
                            " | Price: ₹" + item.getProduct().getPrice() +
                            " | Total: ₹" + item.getTotalPrice());

            grandTotal += item.getTotalPrice();
        }

        System.out.println("--------------------------------");
        System.out.println("Grand Total: ₹" + grandTotal);
    }

    public double calculateGrandTotal() {
        double grandTotal = 0;

        for (CartItem item : cart) {
            grandTotal += item.getTotalPrice();
        }

        return grandTotal;
    }

    public void printFinalBill(String customerName) {
        if (cart.isEmpty()) {
            System.out.println("\nNo items in cart. Bill cannot be generated.");
            return;
        }

        System.out.println("\n==============================================");
        System.out.println("              FINAL BILL / RECEIPT            ");
        System.out.println("==============================================");
        System.out.println("Customer Name: " + customerName);
        System.out.println("----------------------------------------------");

        for (CartItem item : cart) {
            System.out.println(
                    item.getProduct().getProductName() +
                            " | Qty: " + item.getQuantity() +
                            " | Price: ₹" + item.getProduct().getPrice() +
                            " | Total: ₹" + item.getTotalPrice());
        }

        System.out.println("----------------------------------------------");
        System.out.println("Grand Total: ₹" + calculateGrandTotal());
        System.out.println("==============================================");
        System.out.println("Thank you for shopping with us!");
    }

    public ArrayList<CartItem> getCart() {
        return cart;
    }

    public void clearCart() {
        cart.clear();
    }
}