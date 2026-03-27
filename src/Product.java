public class Product {
    private int productId;
    private String productName;
    private int categoryId;
    private String subcategory;
    private double price;
    private int stock;
    private String brand;
    private double rating;
    private String description;

    public Product(int productId, String productName, int categoryId, String subcategory,
            double price, int stock, String brand, double rating, String description) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.subcategory = subcategory;
        this.price = price;
        this.stock = stock;
        this.brand = brand;
        this.rating = rating;
        this.description = description;
    }

    public Product(int productId, String productName, int categoryId, String subcategory,
            double price, int stock) {
        this(productId, productName, categoryId, subcategory, price, stock, "Generic", 4.0, "No description available");
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getBrand() {
        return brand;
    }

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }
}