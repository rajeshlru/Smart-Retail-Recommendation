import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class RecommendationEngine {
    private void printDetailedProduct(ResultSet rs, String reason) throws Exception {
        System.out.println(
                rs.getInt("product_id") + ". " +
                        rs.getString("product_name") +
                        " | Rs." + rs.getDouble("price") +
                        " | Brand: " + rs.getString("brand") +
                        " | Rating: " + rs.getDouble("rating"));
        System.out.println("👉 Reason: " + reason);
        System.out.println("👉 Description: " + rs.getString("description"));
        System.out.println("-----------------------------------------------------------------------------");
    }

    // =====================================================
    // PRODUCT-LEVEL SMART RECOMMENDATION
    // =====================================================
    public void showRecommendations(int productId) {
        System.out.println("\n====================================================");
        System.out.println("        SMART PRODUCT RECOMMENDATIONS");
        System.out.println("====================================================");

        boolean crossSellFound = showCrossSellRecommendations(productId);
        boolean comboFound = false;
        boolean upsellFound = showUpsellRecommendation(productId);

        if (!crossSellFound) {
            System.out.println("\nNo strong direct cross-sell found. Showing smart combo suggestions...");
            comboFound = showComboRecommendations(productId);
        }

        if (!crossSellFound && !comboFound && !upsellFound) {
            showFallbackRecommendations(productId);
        }
    }

    // =====================================================
    // 1. CROSS-SELL USING REAL TRANSACTION HISTORY
    // =====================================================

    public boolean showCrossSellRecommendations(int productId) {
        boolean found = false;

        System.out.println("\n🛒 Cross-Sell Recommendations (Frequently Bought Together):");

        Set<Integer> shown = new LinkedHashSet<>();

        try (Connection con = DBConnection.getConnection()) {

            // =====================================================
            // 1. FIRST: PRODUCT RELATIONS (MANUAL / BUSINESS LOGIC)
            // =====================================================
            String relationQuery = """
                    SELECT p.product_id, p.product_name, p.category_id, p.subcategory, p.price, p.stock,
                           p.brand, p.rating, p.description, pr.score
                    FROM product_relations pr
                    JOIN products p ON pr.related_product_id = p.product_id
                    WHERE pr.base_product_id = ?
                    ORDER BY pr.score DESC, p.rating DESC
                    LIMIT 5
                    """;

            try (PreparedStatement pst = con.prepareStatement(relationQuery)) {
                pst.setInt(1, productId);
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    found = true;
                    shown.add(rs.getInt("product_id"));
                    printDetailedProduct(rs,
                            "Highly relevant accessory / complementary product");
                }
            }

            // =====================================================
            // 2. SECOND: REAL TRANSACTION HISTORY
            // =====================================================
            String transactionQuery = """
                    SELECT p.product_id, p.product_name, p.category_id, p.subcategory, p.price, p.stock,
                           p.brand, p.rating, p.description,
                           COUNT(*) AS frequency
                    FROM transaction_items t1
                    JOIN transaction_items t2
                        ON t1.transaction_id = t2.transaction_id
                        AND t1.product_id != t2.product_id
                    JOIN products p ON t2.product_id = p.product_id
                    WHERE t1.product_id = ?
                    GROUP BY p.product_id, p.product_name, p.category_id, p.subcategory, p.price, p.stock,
                             p.brand, p.rating, p.description
                    HAVING COUNT(*) >= 1
                    ORDER BY frequency DESC, p.rating DESC
                    LIMIT 5
                    """;

            try (PreparedStatement pst = con.prepareStatement(transactionQuery)) {
                pst.setInt(1, productId);
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    int pid = rs.getInt("product_id");

                    if (shown.contains(pid))
                        continue;

                    found = true;
                    shown.add(pid);

                    printDetailedProduct(rs,
                            "Customers often buy this together");
                }
            }

            if (!found) {
                System.out.println("No strong related cross-sell data found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return found;
    }

    // =====================================================
    // 2. SMART COMBO RECOMMENDATIONS (FALLBACK BOOST)
    // =====================================================
    public boolean showComboRecommendations(int productId) {
        System.out.println("\n✨ Smart Combo Suggestions:");

        List<Product> comboProducts = getComboProducts(productId);

        if (comboProducts.isEmpty()) {
            System.out.println("No predefined combo suggestions available.");
            return false;
        }

        for (Product p : comboProducts) {
            System.out.println(
                    p.getProductId() + ". " +
                            p.getProductName() +
                            " | Rs." + p.getPrice() +
                            " | Brand: " + p.getBrand() +
                            " | Rating: " + p.getRating());
            System.out.println("👉 Reason: Common combo recommendation");
            System.out.println("👉 Description: " + p.getDescription());
            System.out.println("-----------------------------------------------------------------------------");
        }

        return true;
    }

    // =====================================================
    // 3. UPSSELL - ONLY SAME SUBCATEGORY
    // =====================================================
    public boolean showUpsellRecommendation(int productId) {
        System.out.println("\n⬆️ Upsell Recommendation (Better Alternative):");

        boolean found = false;

        String sameSubcategoryQuery = """
                    SELECT p2.*
                    FROM products p1
                    JOIN products p2
                        ON p1.subcategory = p2.subcategory
                        AND p1.product_id != p2.product_id
                    WHERE p1.product_id = ?
                      AND p2.price > p1.price
                      AND p2.price <= p1.price * 2.5
                    ORDER BY p2.price ASC
                    LIMIT 1
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sameSubcategoryQuery)) {

            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                found = true;
                printUpsellProduct(rs, "Better option from same product type.");
            } else {
                System.out.println("No suitable upsell option available.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return found;
    }

    private void printUpsellProduct(ResultSet rs, String reason) throws Exception {
        System.out.println(
                rs.getInt("product_id") + ". " +
                        rs.getString("product_name") +
                        " | Rs." + rs.getDouble("price") +
                        " | Brand: " + rs.getString("brand") +
                        " | Rating: " + rs.getDouble("rating"));
        System.out.println("Reason: " + reason);
        System.out.println("Description: " + rs.getString("description"));
    }

    // =====================================================
    // 4. FALLBACK SIMILAR PRODUCTS
    // =====================================================
    public void showFallbackRecommendations(int productId) {
        System.out.println("\n🔁 Similar Product Suggestions:");

        String query = """
                    SELECT p2.*
                    FROM products p1
                    JOIN products p2 ON p1.category_id = p2.category_id
                    WHERE p1.product_id = ?
                      AND p2.product_id != ?
                      AND p2.price BETWEEN p1.price * 0.5 AND p1.price * 2
                    ORDER BY ABS(p2.price - p1.price), p2.rating DESC
                    LIMIT 3
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, productId);
            pst.setInt(2, productId);

            ResultSet rs = pst.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;
                printDetailedProduct(rs, "Similar product in same category and price range");
            }

            if (!found) {
                System.out.println("No similar products available.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // 5. CART-LEVEL RECOMMENDATIONS
    // =====================================================
    public List<Product> getCartRecommendations(List<CartItem> cart) {
        List<Product> recommendations = new ArrayList<>();
        Set<Integer> cartProductIds = new HashSet<>();
        Map<Integer, Integer> scoreMap = new HashMap<>();
        Map<Integer, Product> productMap = new HashMap<>();

        for (CartItem item : cart) {
            cartProductIds.add(item.getProduct().getProductId());
        }

        String transactionQuery = """
                    SELECT p.product_id, p.product_name, p.category_id, p.subcategory, p.price, p.stock,
                           p.brand, p.rating, p.description,
                           COUNT(*) AS frequency
                    FROM transaction_items t1
                    JOIN transaction_items t2
                        ON t1.transaction_id = t2.transaction_id
                        AND t1.product_id != t2.product_id
                    JOIN products p ON t2.product_id = p.product_id
                    WHERE t1.product_id = ?
                    GROUP BY p.product_id, p.product_name, p.category_id, p.subcategory, p.price, p.stock,
                             p.brand, p.rating, p.description
                    ORDER BY frequency DESC
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(transactionQuery)) {

            for (CartItem item : cart) {
                int currentProductId = item.getProduct().getProductId();
                int currentCategoryId = item.getProduct().getCategoryId();
                String currentSubcategory = item.getProduct().getSubcategory();

                // ----------------------------
                // A. Product Relations Scoring
                // ----------------------------
                String relationQuery = """
                        SELECT p.product_id, p.product_name, p.category_id, p.subcategory, p.price, p.stock,
                               p.brand, p.rating, p.description, pr.score
                        FROM product_relations pr
                        JOIN products p ON pr.related_product_id = p.product_id
                        WHERE pr.base_product_id = ?
                        ORDER BY pr.score DESC
                        """;

                try (PreparedStatement relationPst = con.prepareStatement(relationQuery)) {
                    relationPst.setInt(1, currentProductId);
                    ResultSet relRs = relationPst.executeQuery();

                    while (relRs.next()) {
                        int recommendedId = relRs.getInt("product_id");

                        if (cartProductIds.contains(recommendedId))
                            continue;

                        int score = relRs.getInt("score") + 20;

                        scoreMap.put(recommendedId, scoreMap.getOrDefault(recommendedId, 0) + score);

                        productMap.put(recommendedId, new Product(
                                relRs.getInt("product_id"),
                                relRs.getString("product_name"),
                                relRs.getInt("category_id"),
                                relRs.getString("subcategory"),
                                relRs.getDouble("price"),
                                relRs.getInt("stock"),
                                relRs.getString("brand"),
                                relRs.getDouble("rating"),
                                relRs.getString("description")));
                    }
                }

                // ----------------------------
                // B. Transaction-based scoring
                // ----------------------------
                pst.setInt(1, currentProductId);
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    int recommendedId = rs.getInt("product_id");

                    if (cartProductIds.contains(recommendedId))
                        continue;

                    int score = rs.getInt("frequency");

                    if (rs.getInt("category_id") == currentCategoryId) {
                        score += 5;
                    }

                    if (rs.getString("subcategory").equalsIgnoreCase(currentSubcategory)) {
                        score += 10;
                    }

                    scoreMap.put(recommendedId, scoreMap.getOrDefault(recommendedId, 0) + score);

                    productMap.put(recommendedId, new Product(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("category_id"),
                            rs.getString("subcategory"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("brand"),
                            rs.getDouble("rating"),
                            rs.getString("description")));
                }

                // ----------------------------
                // C. Combo-based scoring boost
                // ----------------------------
                List<Product> comboProducts = getComboProducts(currentProductId);

                for (Product comboProduct : comboProducts) {
                    int recommendedId = comboProduct.getProductId();

                    if (cartProductIds.contains(recommendedId))
                        continue;

                    scoreMap.put(recommendedId, scoreMap.getOrDefault(recommendedId, 0) + 15);
                    productMap.put(recommendedId, comboProduct);
                }
            }

            List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(scoreMap.entrySet());
            sorted.sort((a, b) -> b.getValue() - a.getValue());

            for (Map.Entry<Integer, Integer> entry : sorted) {
                recommendations.add(productMap.get(entry.getKey()));
                if (recommendations.size() >= 5)
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return recommendations;
    }

    // =====================================================
    // 6. DISPLAY CART RECOMMENDATIONS
    // =====================================================
    public void showCartRecommendations(List<CartItem> cart) {
        List<Product> recommendations = getCartRecommendations(cart);

        System.out.println("\n===== CART-BASED SMART RECOMMENDATIONS =====");

        if (recommendations.isEmpty()) {
            System.out.println("No additional recommendations available.");
            return;
        }

        for (Product p : recommendations) {
            System.out.println(
                    p.getProductId() + ". " +
                            p.getProductName() +
                            " | Rs." + p.getPrice() +
                            " | Brand: " + p.getBrand() +
                            " | Rating: " + p.getRating());
            System.out.println("👉 Description: " + p.getDescription());
            System.out.println("-----------------------------------------------------------------------------");
        }
    }

    // =====================================================
    // 7. COMBO PRODUCT FETCHER (INTERNAL HELPER)
    // =====================================================
    private List<Product> getComboProducts(int productId) {
        List<Product> comboProducts = new ArrayList<>();
        List<String> comboSubcategories = new ArrayList<>();
        String subcategory = "";

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(
                    "SELECT subcategory FROM products WHERE product_id = ?");
            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                subcategory = rs.getString("subcategory").toLowerCase();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // =====================================================
        // SMART COMBO MAPPING
        // =====================================================
        if (subcategory.contains("laptop")) {
            comboSubcategories = List.of("mouse", "keyboards", "bags");
        } else if (subcategory.contains("mobile")) {
            comboSubcategories = List.of("chargers", "power banks", "earbuds");
        } else if (subcategory.contains("tablet")) {
            comboSubcategories = List.of("chargers", "earbuds", "power banks");
        } else if (subcategory.contains("smart tv")) {
            comboSubcategories = List.of("power banks"); // optional simple fallback
        } else if (subcategory.contains("mouse")) {
            comboSubcategories = List.of("keyboards", "earbuds");
        } else if (subcategory.contains("keyboard")) {
            comboSubcategories = List.of("mouse", "earbuds");
        } else if (subcategory.contains("earbuds")) {
            comboSubcategories = List.of("chargers", "power banks");
        } else if (subcategory.contains("charger")) {
            comboSubcategories = List.of("power banks", "earbuds");
        } else if (subcategory.contains("rice")) {
            comboSubcategories = List.of("oil", "salt", "pulses");
        } else if (subcategory.contains("oil")) {
            comboSubcategories = List.of("rice", "salt", "pulses");
        } else if (subcategory.contains("sugar")) {
            comboSubcategories = List.of("tea", "coffee");
        } else if (subcategory.contains("salt")) {
            comboSubcategories = List.of("rice", "oil", "pulses");
        } else if (subcategory.contains("pulses")) {
            comboSubcategories = List.of("rice", "oil", "salt");
        } else if (subcategory.contains("tea")) {
            comboSubcategories = List.of("sugar", "biscuits");
        } else if (subcategory.contains("coffee")) {
            comboSubcategories = List.of("sugar", "biscuits");
        } else if (subcategory.contains("shampoo")) {
            comboSubcategories = List.of("soap", "hair care", "skin care");
        } else if (subcategory.contains("soap")) {
            comboSubcategories = List.of("oral care", "skin care");
        } else if (subcategory.contains("oral care")) {
            comboSubcategories = List.of("soap", "skin care");
        } else if (subcategory.contains("hair care")) {
            comboSubcategories = List.of("shampoo", "soap");
        } else if (subcategory.contains("bottle")) {
            comboSubcategories = List.of("lunch boxes", "storage");
        } else if (subcategory.contains("lunch")) {
            comboSubcategories = List.of("bottles", "storage");
        } else if (subcategory.contains("cookware")) {
            comboSubcategories = List.of("kitchen tools", "storage");
        } else if (subcategory.contains("storage")) {
            comboSubcategories = List.of("bottles", "lunch boxes");
        } else if (subcategory.contains("pens")) {
            comboSubcategories = List.of("notebooks", "markers", "books");
        } else if (subcategory.contains("notebook")) {
            comboSubcategories = List.of("pens", "markers", "books");
        } else if (subcategory.contains("marker")) {
            comboSubcategories = List.of("pens", "notebooks");
        } else if (subcategory.contains("book")) {
            comboSubcategories = List.of("pens", "notebooks");
        } else if (subcategory.contains("cricket")) {
            comboSubcategories = List.of("fitness");
        } else if (subcategory.contains("yoga")) {
            comboSubcategories = List.of("fitness", "gym");
        } else if (subcategory.contains("gym")) {
            comboSubcategories = List.of("fitness", "yoga");
        } else if (subcategory.contains("fitness")) {
            comboSubcategories = List.of("gym", "yoga");
        }

        if (comboSubcategories.isEmpty()) {
            return comboProducts;
        }

        String sql = """
                    SELECT product_id, product_name, category_id, subcategory, price, stock, brand, rating, description
                    FROM products
                    WHERE LOWER(subcategory) = ?
                    LIMIT 2
                """;

        try (Connection con = DBConnection.getConnection()) {
            Set<Integer> addedIds = new HashSet<>();

            for (String sub : comboSubcategories) {
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, "%" + sub.toLowerCase() + "%");
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    int pid = rs.getInt("product_id");

                    if (addedIds.contains(pid) || pid == productId)
                        continue;

                    comboProducts.add(new Product(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("category_id"),
                            rs.getString("subcategory"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("brand"),
                            rs.getDouble("rating"),
                            rs.getString("description")));

                    addedIds.add(pid);

                    if (comboProducts.size() >= 5)
                        return comboProducts;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return comboProducts;
    }
}
