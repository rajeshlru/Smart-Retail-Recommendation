
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void pauseScreen(Scanner sc) {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        CategoryDAO categoryDAO = new CategoryDAO();
        ProductDAO productDAO = new ProductDAO();
        CartService cartService = new CartService();
        RecommendationEngine recommendationEngine = new RecommendationEngine();
        TransactionDAO transactionDAO = new TransactionDAO();
        ReportService reportService = new ReportService();
        AdminService adminService = new AdminService();
        RandomTransactionGenerator generator = new RandomTransactionGenerator();

        boolean running = true;

        while (running) {

            System.out.println("\n====================================================");
            System.out.println("        SMART RETAIL RECOMMENDATION ENGINE");
            System.out.println("====================================================");
            System.out.println("1. Customer Shopping");
            System.out.println("2. Admin Panel");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int mainChoice = sc.nextInt();
            sc.nextLine(); // ⭐ IMPORTANT FIX

            switch (mainChoice) {
                case 1:
                    customerShopping(sc, categoryDAO, productDAO, cartService, recommendationEngine, transactionDAO);
                    break;

                case 2:
                    System.out.print("Enter Admin Password: ");
                    String adminPassword = sc.nextLine();

                    if (adminPassword.equals("admin@123")) {
                        adminPanel(sc, adminService, reportService, generator);
                    } else {
                        System.out.println("\n❌ Incorrect password! Access denied.");
                        pauseScreen(sc);
                    }
                    break;

                case 3:
                    System.out.println("Exiting system... Thank you!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice! Please try again.");
                    pauseScreen(sc);
            }
        }
        sc.close();
    }

    // ================= CUSTOMER SHOPPING =================
    public static void customerShopping(Scanner sc, CategoryDAO categoryDAO, ProductDAO productDAO,
            CartService cartService, RecommendationEngine recommendationEngine,
            TransactionDAO transactionDAO) {

        boolean customerMenu = true;

        while (customerMenu) {
            System.out.println("\n---------------- CUSTOMER SHOPPING ----------------");
            System.out.println("1. Browse Categories & Shop");
            System.out.println("2. View Cart");
            System.out.println("3. Checkout");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int customerChoice = sc.nextInt();

            switch (customerChoice) {

                case 1:
                    boolean continueShopping = true;

                    while (continueShopping) {
                        categoryDAO.showCategories();

                        System.out.print("\nEnter category ID to view products: ");
                        int categoryId = sc.nextInt();

                        boolean sameCategoryShopping = true;

                        while (sameCategoryShopping) {
                            productDAO.showProductsByCategory(categoryId);

                            System.out.print("\nEnter product ID to add to cart: ");
                            int productId = sc.nextInt();

                            // ✅ CATEGORY VALIDATION
                            if (!productDAO.isProductInCategory(productId, categoryId)) {
                                System.out.println("Invalid product! Please select only from the displayed category.");
                            } else {
                                Product selectedProduct = productDAO.getProductById(productId);

                                if (selectedProduct != null) {
                                    System.out.print("Enter quantity: ");
                                    int quantity = sc.nextInt();

                                    if (quantity <= 0) {
                                        System.out.println("Invalid quantity!");
                                    } else if (quantity > selectedProduct.getStock()) {
                                        System.out.println("Not enough stock available!");
                                        System.out.println("Available stock: " + selectedProduct.getStock());
                                    } else {
                                        cartService.addToCart(selectedProduct, quantity);

                                        recommendationEngine.showRecommendations(productId);

                                        Main.pauseScreen(sc);
                                    }
                                }
                            }
                            System.out.print(
                                    "\nYou can continue shopping in this category by entering 'yes' or switch to another category by entering 'no': ");
                            String sameCategoryChoice = sc.next();

                            if (!sameCategoryChoice.equalsIgnoreCase("yes")) {
                                sameCategoryShopping = false;
                            }
                        }

                        System.out.print("\nDo you want to shop from ANOTHER category? (yes/no): ");
                        String anotherCategoryChoice = sc.next();

                        if (!anotherCategoryChoice.equalsIgnoreCase("yes")) {
                            continueShopping = false;

                            System.out.println("\n====================================================");
                            System.out.println(" Shopping session completed for now!");
                            System.out.println(" 👉 To view your cart, choose option 2");
                            System.out.println(" 👉 To checkout / place order, choose option 3");
                            System.out.println("====================================================");

                            Main.pauseScreen(sc);
                        }
                    }
                    break;

                case 2:
                    cartService.showCart();
                    Main.pauseScreen(sc);
                    break;

                case 3:
                    if (cartService.getCart().isEmpty()) {
                        System.out.println("Your cart is empty! Add products before checkout.");
                    } else {
                        cartService.showCart();

                        // Get cart-based recommendations
                        List<Product> cartRecommendations = recommendationEngine
                                .getCartRecommendations(cartService.getCart());

                        System.out.println("\n===== CART-BASED RECOMMENDATIONS =====");

                        if (cartRecommendations.isEmpty()) {
                            System.out.println("No additional recommendations available.");
                        } else {
                            for (Product p : cartRecommendations) {
                                System.out.println(
                                        p.getProductId() + ". " + p.getProductName() + " | Price: Rs." + p.getPrice());
                            }

                            boolean addMoreRecommendations = true;

                            while (addMoreRecommendations && !cartRecommendations.isEmpty()) {

                                System.out.print("\nDo you want to add any recommended products? (yes/no): ");
                                String addChoice = sc.next();

                                if (!addChoice.equalsIgnoreCase("yes")) {
                                    break;
                                }

                                System.out.print("Enter recommended product ID to add: ");
                                int recommendedId = sc.nextInt();

                                Product selectedRecommendation = null;

                                for (Product p : cartRecommendations) {
                                    if (p.getProductId() == recommendedId) {
                                        selectedRecommendation = p;
                                        break;
                                    }
                                }

                                if (selectedRecommendation == null) {
                                    System.out.println(
                                            "Invalid choice! Please select only from displayed recommended products.");
                                    continue;
                                }

                                System.out.print("Enter quantity: ");
                                int quantity = sc.nextInt();

                                if (quantity <= 0) {
                                    System.out.println("Invalid quantity!");
                                    continue;
                                }

                                if (quantity > selectedRecommendation.getStock()) {
                                    System.out.println("Not enough stock available!");
                                    System.out.println("Available stock: " + selectedRecommendation.getStock());
                                    continue;
                                }

                                cartService.addToCart(selectedRecommendation, quantity);

                                cartRecommendations.remove(selectedRecommendation);

                                if (cartRecommendations.isEmpty()) {
                                    System.out.println("\nAll recommended products have been processed.");
                                    break;
                                }

                                System.out.println("\nRemaining Recommendations:");
                                for (Product p : cartRecommendations) {
                                    System.out.println(p.getProductId() + ". " + p.getProductName() + " | Price: Rs."
                                            + p.getPrice());
                                }
                            }
                        }

                        System.out.print("\nDo you want to place the order? (yes/no): ");
                        String placeOrder = sc.next();

                        if (placeOrder.equalsIgnoreCase("yes")) {
                            sc.nextLine();
                            System.out.print("Enter customer name: ");
                            String customerName = sc.nextLine();

                            transactionDAO.saveTransaction(customerName, cartService.getCart());
                            cartService.printFinalBill(customerName);
                            cartService.clearCart();

                            Main.pauseScreen(sc);
                        } else {
                            System.out.println("Order cancelled.");
                        }
                    }
                    break;
                case 4:
                    customerMenu = false;

                    break;

                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    // ================= ADMIN PANEL =================
    public static void adminPanel(Scanner sc, AdminService adminService, ReportService reportService,
            RandomTransactionGenerator generator) {

        boolean adminMenu = true;

        while (adminMenu) {
            System.out.println("\n---------------- ADMIN PANEL ----------------");
            System.out.println("1. View All Transactions");
            System.out.println("2. View Top Selling Products");
            System.out.println("3. View Top Categories");
            System.out.println("4. View Frequently Bought Together");
            System.out.println("5. View Trending Products");
            System.out.println("6. View Total Revenue");
            System.out.println("7. Generate Sample Data");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int adminChoice = sc.nextInt();
            sc.nextLine();

            switch (adminChoice) {
                case 1:
                    adminService.viewAllTransactions(sc);
                    pauseScreen(sc);
                    break;

                case 2:
                    reportService.showTopSellingProducts();
                    pauseScreen(sc);
                    break;

                case 3:
                    reportService.showTopCategories();
                    pauseScreen(sc);
                    break;

                case 4:
                    reportService.showMostBoughtTogether();
                    pauseScreen(sc);
                    break;

                case 5:
                    reportService.showTrendingProducts();
                    pauseScreen(sc);
                    break;

                case 6:
                    reportService.showTotalRevenue();
                    pauseScreen(sc);
                    break;

                case 7:
                    System.out.print("Enter number of sample transactions to generate: ");
                    int count = sc.nextInt();
                    sc.nextLine();
                    generator.generateSmartTransactions(count);
                    pauseScreen(sc);
                    break;

                case 8:
                    adminMenu = false;
                    break;

                default:
                    System.out.println("Invalid choice! Please try again.");
                    pauseScreen(sc);
            }
        }
    }

}