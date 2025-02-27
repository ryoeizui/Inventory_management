package com.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

class Product {
    private Long id;
    private String name;
    private String description;
    private int quantity;
    private BigDecimal price;
    private String category;
    private int minimumStock;
    private LocalDateTime lastUpdated;

    public Product (Long id, String name, String description, int quantity,
                    BigDecimal price, String category, int minimumStock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.minimumStock = minimumStock;
        this.lastUpdated = LocalDateTime.now();
    }

    //Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public int getMinimumStock() { return minimumStock; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("""
                ID: %d
                Name: %s
                Description: %s
                Quantity: %d
                Price: $%.2f
                Category: %s
                Minimum Stock: %d
                Last Updated: %s
                """,
                id, name, description, quantity, price, category,
                minimumStock, lastUpdated.toString());
    }
}

class Transaction {
    private Long id;
    private Long productId;
    private String type; // "IN" or "OUT"
    private int quantity;
    private LocalDateTime timestamp;
    private String notes;

    public Transaction(Long id, Long productId, String type,
                       int quantity, String notes) {
        this.id = id;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
        this.notes = notes;
    }

    @Override
    public String toString() {
        return String.format("""
                Transaction ID: %d
                Product ID: %d
                Type: %s
                Quantity: %d
                Time: %s
                Notes: %s
                """,
                id, productId, type, quantity, timestamp,toString(), notes);
    }
}

class InventoryManager {
    private Map<Long, Product> products;
    private List<Transaction> transactions;
    private Long nextProductId;
    private Long nextTransactionId;
    private Scanner scanner;

    public InventoryManager() {
        this.products = new HashMap<>();
        this.transactions = new ArrayList<>();
        this.nextProductId = 1L;
        this.nextTransactionId = 1L;
        this.scanner = new Scanner(System.in);
    }

    public void addProduct() {
        System.out.println("\n=== Add New Product ===\n");

        System.out.println("Enter product name: ");
        String name = scanner.nextLine();

        System.out.println("\nEnter product description: ");
        String description = scanner.nextLine();

        System.out.println("\nEnter initial quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());

        System.out.println("\nEnter price: ");
        BigDecimal price = new BigDecimal(scanner.nextLine());

        System.out.println("\nEnter category: ");
        String category = scanner.nextLine();

        System.out.println("\nEnter minimum stock level: ");
        int minimumStock = Integer.parseInt(scanner.nextLine());

        Product product = new Product(nextProductId++, name, description,
                quantity, price, category, minimumStock);
        products.put(product.getId(), product);

        //Record transaction
        Transaction transaction = new Transaction(
                nextTransactionId++, product.getId(), "IN", quantity, "Initial Stock"
        );
        transactions.add(transaction);

        System.out.println("Product added successfully!!!");
    }

    public void updateStock() {
        System.out.println("\n=== Update Stock ===");

        System.out.print("\nEnter product ID: ");
        Long productId = Long.parseLong(scanner.nextLine());

        Product product = products.get(productId);
        if (product == null) {
            System.out.println("Product not found! Please go 1 to enter a new product if needed");
            return;
        }

        System.out.print("Current stock: " + product.getQuantity());
        System.out.print("Enter stock change (Positive for IN, negative for OUT): ");
        int change = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter notes");
        String notes = scanner.nextLine();

        int newQuantity = product.getQuantity() + change;
        if (newQuantity < 0) {
            System.out.println("Error: Stock cannot be negative!!");
            return;
        }
        product.setQuantity(newQuantity);

        //Transaction record
        Transaction transaction = new Transaction(
                nextTransactionId++, productId,
                change > 0 ? "IN" : "OUT",
                Math.abs(change), notes
        );
        transactions.add(transaction);

        System.out.println("Stock Updated Successfully!!");
        checkLowStock(product);
    }

    private void checkLowStock(Product product) {
        if (product.getQuantity() <= product.getMinimumStock()) {
            System.out.println("\n***!!! LOW STOCK ALERT!!!***");
            System.out.printf("Product '%s' is at or below minimum stock level!\n",
                    product.getName());
            System.out.printf("Current stock: %d, Minimum stock: %d\n",
                    product.getQuantity(), product.getMinimumStock());
        }
    }

    public void viewProducts() {
        if (products.isEmpty()) {
            System.out.println("\nNo product found.");
            return;
        }

        System.out.println("\n=== Product List ===");
        products.values().forEach(product -> {
            System.out.println("\n" + product);
            System.out.println("-".repeat(40));
        });
    }

    public void viewTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("\nNo transactions found.");
            return;
        }

        System.out.println("\n=== Transaction History ===");
        transactions.forEach(transaction -> {
            System.out.println("\n" + transaction);
            System.out.println("-".repeat(40));
        });
    }

    public void generateReport() {
        System.out.println("\n=== Inventory Report ===");

        BigDecimal totalValue = BigDecimal.ZERO;
        int lowStockItems = 0;

        System.out.println("\nLow Stock Items:");
        for (Product product : products.values()) {
            totalValue = totalValue.add(
                    product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity()))
            );

            if (product.getQuantity() <= product.getMinimumStock()) {
                lowStockItems++;
                System.out.printf("- %s (Current: %d, Minimum: %d",
                        product.getName(), product.getQuantity(),
                        product.getMinimumStock());
            }
        }

        System.out.println("\n********************");
        System.out.println("Summary:");

        System.out.println("\nTotal number of products: " + products.size());
        System.out.println("\nTotal inventory value: " + totalValue);
        System.out.println("\nNumber of low stock items: " + lowStockItems);
        System.out.println("\nTotal transactions: " + transactions.size());
        System.out.println("**********************");
    }



    public void showMenu() {
        while (true) {
            System.out.println("\n***************************");
            System.out.println("Inventory Management System");
            System.out.println("***************************\n");
            System.out.println("1. Add New Product");
            System.out.println("2. Update Stock");
            System.out.println("3. View Product");
            System.out.println("4. View Transactions");
            System.out.println("5. Generate Report");
            System.out.println("6. Exit\n");
            System.out.print("Choose an option (1 - 6): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> addProduct();
                case "2" -> updateStock();
                case "3" -> viewProducts();
                case "4" -> viewTransactions();
                case "5" -> generateReport();
                case "6" -> {
                    System.out.println("Goodbye");
                    return;
                }
                default -> System.out.println("Invalid option. Please enter a number through 1 - 6.");
            }
        }
    }

    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();
        manager.showMenu();
    }
}
