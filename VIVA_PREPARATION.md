# FINTRACK — COMPLETE VIVA PREPARATION GUIDE

## Quick Navigation
- [Section 1: Project Overview](#section-1-project-overview)
- [Section 2: Architecture](#section-2-architecture)
- [Section 3: Java Concepts](#section-3-java-concepts)
- [Section 4: JavaFX](#section-4-javafx)
- [Section 5: Database](#section-5-database)
- [Section 6: Rapid Fire Q&A](#section-6-rapid-fire-qa)
- [Section 7: Tough Questions](#section-7-tough-questions)

---

# SECTION 1: PROJECT OVERVIEW

## 1.1 What is FinTrack?

**Simple Answer:**
FinTrack is a **Desktop Personal Finance Manager** that helps users track income, expenses, budgets, and analyze spending patterns.

**Technical Answer:**
A JavaFX-based desktop application with SQLite backend that implements MVC architecture with service and DAO layers for managing personal finances locally.

## 1.2 Problem Statement

| Problem | How FinTrack Solves It |
|---------|----------------------|
| No spending visibility | Dashboard shows balance, income, expense at a glance |
| Difficult budget management | Create budgets per category with visual progress bars |
| Can't see spending patterns | 6-month charts show income vs expense trends |
| Need multiple tools | Single integrated app for accounts, transactions, budgets |
| Privacy concerns | All data stored locally; no cloud required |

## 1.3 Key Features

### Dashboard
- Welcome message with current date
- 4 summary cards: Total Balance, Monthly Income, Monthly Expense, Net Savings
- 6-month cash flow chart (Income vs Expense bars)
- Category-wise spending pie chart
- Last 5 transactions table
- Top spending category insight

### Transaction Management
- ✅ Add/Edit/Delete transactions
- ✅ Filter by: Type, Category, Date Range, Amount Range, Keyword
- ✅ Sort by: Date, Amount, Description
- ✅ Pagination (15 per page)
- ✅ Advanced search

### Budget Tracking
- ✅ Create budgets per expense category
- ✅ Set period: Monthly, Weekly, Yearly
- ✅ Visual progress bars with color coding
- ✅ Budget health metrics
- ✅ Alert when budget exceeded

### Accounts
- ✅ Multiple account types: Checking, Savings, Credit Card, Cash, Investment
- ✅ Real-time balance tracking
- ✅ Net worth calculation (sum of all accounts)

### Reports & Analytics
- ✅ 6-month trend analysis
- ✅ Category-wise spending breakdown
- ✅ Budget health overview
- ✅ CSV export
- ✅ PDF export

### Security
- ✅ User registration with bcrypt password hashing
- ✅ Login authentication
- ✅ Session management
- ✅ Per-user data isolation

## 1.4 Technology Stack

```
┌─────────────────────────────────────────────────┐
│ PRESENTATION LAYER                              │
│ JavaFX 21 + FXML + CSS                         │
│ Controllers, Views, UI Components               │
└─────────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────────┐
│ BUSINESS LOGIC LAYER                            │
│ Services: Auth, Transaction, Budget, Account    │
│ Business rules & validation                     │
└─────────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────────┐
│ DATA ACCESS LAYER                               │
│ DAOs + Models: User, Transaction, Budget, etc.  │
│ JDBC with PreparedStatements                    │
└─────────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────────┐
│ DATABASE LAYER                                  │
│ SQLite 3.45 (fintrack.db)                      │
│ 6 tables with relationships                     │
└─────────────────────────────────────────────────┘
```

## 1.5 Why Each Technology?

### JavaFX (Not Swing)
```
✅ Modern CSS styling
✅ Built-in charts (BarChart, PieChart, LineChart)
✅ Scene graph architecture
✅ Property binding & ObservableList
✅ FXML for declarative UI
✅ Active development
```

### SQLite (Not MySQL)
```
✅ Zero configuration
✅ Single file database
✅ No server process needed
✅ Perfect for desktop apps
✅ ACID compliance
✅ Easy backup & deployment
```

### JDBC (Not JPA/Hibernate)
```
✅ Direct query control
✅ Minimal overhead
✅ Custom DAO implementation
✅ Better debugging
✅ No ORM complexity
```

### bcrypt (Not Plain Text)
```
✅ Industry-standard hashing
✅ Auto-salt generation
✅ Rainbow table resistant
✅ Adaptive work factor
✅ Simple API
```

### Maven (Not Gradle)
```
✅ XML configuration (familiar)
✅ Standardized project structure
✅ Works with JavaFX plugin
✅ Team-friendly conventions
```

## 1.6 Project Statistics

```
Language Composition:
├─ Java: 86.1%
└─ CSS: 13.9%

File Structure:
src/main/
├─ java/
│  ├─ controller/ (9 controllers)
│  ├─ service/ (10 services)
│  ├─ dao/ (6 DAOs)
│  ├─ model/ (6 models)
│  ├─ config/ (database config)
│  ├─ navigation/ (scene navigator)
│  ├─ session/ (session manager)
│  ├─ util/ (utilities)
│  ├─ exception/ (custom exceptions)
│  └─ module-info.java
├─ resources/
│  ├─ fxml/ (FXML files)
│  └─ css/ (stylesheets)
```

---

# SECTION 2: ARCHITECTURE

## 2.1 MVC Pattern Explained

```
MODEL (Data)
├─ Location: com.fintrack.model
├─ Classes: User, Transaction, Account, Budget, Category
├─ Purpose: Represent data, no business logic
└─ Example:
   public class Transaction {
       private int id;
       private double amount;
       public double getAmount() { return amount; }  // Simple getter
       public void setAmount(double amt) { this.amount = amt; }  // Simple setter
   }

VIEW (Presentation)
├─ Location: src/main/resources/fxml/ and css/
├─ Files: dashboard.fxml, transactions.fxml, styles.css
├─ Purpose: Display data, capture user input
└─ Example:
   <Label fx:id="lblBalance" text="$0.00" style="-fx-font-size: 24;"/>

CONTROLLER (Interaction)
├─ Location: com.fintrack.controller
├─ Classes: DashboardController, TransactionController, etc.
├─ Purpose: Handle events, call services, update UI
└─ Example:
   @FXML private void handleLogin(ActionEvent event) {
       user = authService.login(username, password);
       SceneNavigator.navigateTo("dashboard.fxml");
   }
```

## 2.2 4-Layer Architecture

```
┌──────────────────────────────────────────────┐
│ LAYER 1: PRESENTATION (UI/Controllers)       │
│ What: User interacts here                     │
│ Who: DashboardController, LoginController    │
│ Calls: Service layer methods                 │
└──────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────┐
│ LAYER 2: BUSINESS LOGIC (Services)           │
│ What: Application rules & workflows          │
│ Who: TransactionService, BudgetService       │
│ Calls: DAO methods                           │
└──────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────┐
│ LAYER 3: DATA ACCESS (DAOs + Models)         │
│ What: Query database & map to objects        │
│ Who: TransactionDAO, UserDAO                 │
│ Calls: JDBC / SQL                            │
└──────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────┐
│ LAYER 4: DATABASE (SQLite)                   │
│ What: Persistent storage                     │
│ Who: fintrack.db file                        │
│ Contains: Tables with data                   │
└──────────────────────────────────────────────┘
```

### Why 4 Layers?

| Benefit | Example |
|---------|---------|
| **Separation of Concerns** | UI changes don't affect database queries |
| **Testability** | Test service without UI; test DAO without service |
| **Maintainability** | Bug in business logic? Fix in service layer only |
| **Scalability** | Change SQLite to MySQL? Only modify DAO layer |
| **Reusability** | Same service used by multiple controllers |

## 2.3 Data Flow Example: Adding a Transaction

```
1. USER CLICKS "ADD TRANSACTION"
   └─ Input: Amount=100, Type="EXPENSE", Description="Groceries"

2. CONTROLLER RECEIVES EVENT
   └─ TransactionController.handleSave()
   └─ Validates input (not empty, amount > 0)

3. CALLS SERVICE LAYER
   └─ transactionService.addTransaction(transaction)
   └─ Service does business logic validation

4. SERVICE CALLS DAO
   └─ transactionDAO.insert(transaction)

5. DAO EXECUTES SQL
   └─ INSERT INTO transactions (amount, type, description) VALUES (100, 'EXPENSE', 'Groceries')

6. DATABASE STORES
   └─ Row added to fintrack.db

7. CONTROLLER REFRESHES UI
   └─ loadTransactions() called
   └─ DAO fetches all user transactions
   └─ TableView updates with new data
   └─ User sees new transaction in table
```

## 2.4 Navigation System

### How Scene Switching Works

```
App starts
├─ DatabaseConfig.initialize()  ← Create database
├─ SceneNavigator.init(primaryStage)  ← Setup navigation
└─ SceneNavigator.navigateTo("login.fxml")  ← Load first scene

User logs in
├─ handleLogin() called
├─ AuthService validates credentials
├─ SessionManager.getInstance().login(user)  ← Store user
└─ SceneNavigator.navigateTo("dashboard.fxml")  ← Switch scene

User clicks "Transactions"
├─ handleTransactions() called
└─ SceneNavigator.navigateTo("transactions.fxml")  ← Switch scene
```

### SceneNavigator Code Pattern

```java
public class SceneNavigator {
    private static Stage primaryStage;
    private static Map<String, Parent> sceneCache = new HashMap<>();
    
    public static void navigateTo(String fxmlFileName) {
        // Load FXML if not cached
        if (!sceneCache.containsKey(fxmlFileName)) {
            FXMLLoader loader = new FXMLLoader(
                SceneNavigator.class.getResource("/fxml/" + fxmlFileName)
            );
            Parent root = loader.load();
            sceneCache.put(fxmlFileName, root);
        }
        
        // Get from cache and display
        Parent root = sceneCache.get(fxmlFileName);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

**Benefits:**
- ✅ FXML parsed only once (performance)
- ✅ Controllers stay in memory (state preserved)
- ✅ Faster navigation

## 2.5 Session Management

### How User State is Maintained

```
LOGIN
└─ User enters credentials
└─ AuthService.login() returns User object
└─ SessionManager.getInstance().login(user)  ← Store in memory

NOW THROUGHOUT APP
├─ DashboardController: int userId = SessionManager.getInstance().getCurrentUserId()
├─ TransactionController: User user = SessionManager.getInstance().getCurrentUser()
└─ Any controller can access current user

LOGOUT
└─ SessionManager.getInstance().logout()  ← Clear from memory
└─ Navigate to login.fxml
```

### SessionManager Code

```java
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    
    private SessionManager() {}  // Private constructor
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void login(User user) {
        this.currentUser = user;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
```

---

# SECTION 3: JAVA CONCEPTS

## 3.1 OOP: The 4 Pillars

### 1. ENCAPSULATION (Hiding Details)

```java
// Example: Transaction Model
public class Transaction {
    // Private variables — hidden from outside
    private int id;
    private double amount;
    private String description;
    
    // Public methods — controlled access
    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;  // Validation enforced
    }
    
    public double getAmount() {
        return amount;
    }
}

// Usage
Transaction t = new Transaction();
t.setAmount(100);    // ✅ OK
t.setAmount(-50);    // ❌ Exception thrown — validation enforced
```

**Why?**
- ✅ Data protection
- ✅ Validation enforced
- ✅ Can change internal implementation without breaking external code

---

### 2. INHERITANCE (Extending Classes)

```java
// Base Exception
public class FinTrackException extends RuntimeException {
    public FinTrackException(String message) {
        super(message);
    }
}

// Specific Exceptions
public class AuthenticationException extends FinTrackException {
    public AuthenticationException(String message) {
        super(message);
    }
}

public class ValidationException extends FinTrackException {
    public ValidationException(String message) {
        super(message);
    }
}

// Usage
try {
    user = authService.login(username, password);
} catch (AuthenticationException e) {
    // Handle authentication error
    AlertUtil.showWarning(e.getMessage());
} catch (FinTrackException e) {
    // Handle any FinTrack error
    ErrorDialogUtil.showFriendlyError(e);
}
```

**Why?**
- ✅ Code reuse
- ✅ Hierarchy of exceptions
- ✅ Can catch specific or general errors

---

### 3. POLYMORPHISM (Multiple Forms)

```java
// Same method call, different behavior based on exception type
FinTrackException error = new ValidationException("Invalid input");
// or
FinTrackException error = new AuthenticationException("Wrong password");

// At runtime, correct error handling executes
```

**Example with Services:**
```java
// Controllers don't know HOW services work, just that they work
public class TransactionService {
    private TransactionDAO dao;  // Could be SQLiteDAO, MySQLDAO, etc.
    
    public List<Transaction> getTransactions(int userId) {
        return dao.selectByUser(userId);  // Same call, different implementations
    }
}
```

**Why?**
- ✅ Flexibility
- ✅ Easy to swap implementations
- ✅ Reduces coupling

---

### 4. ABSTRACTION (Showing Only What Matters)

```java
// User doesn't see SQL complexity
public class TransactionService {
    public List<Transaction> filterTransactions(
        int userId, String type, Integer categoryId,
        LocalDate from, LocalDate to, String keyword,
        Double minAmount, Double maxAmount,
        String sortBy, String sortOrder, int offset, int limit
    ) {
        // Complex SQL construction hidden inside DAO
        return transactionDAO.selectFiltered(...);
    }
}

// Consumer code
List<Transaction> expenses = transactionService.filterTransactions(
    userId, "EXPENSE", null, fromDate, toDate, 
    null, null, null, "amount", "DESC", 0, 20
);
// Don't care HOW it queries — just works!
```

**Why?**
- ✅ Simplicity
- ✅ User sees only essential details
- ✅ Implementation can change

---

## 3.2 Collections Framework

### ArrayList (Most Used)

```java
// Store transactions
List<Transaction> transactions = new ArrayList<>();

// Add
transactions.add(new Transaction(...));

// Get
Transaction first = transactions.get(0);

// Iterate
for (Transaction t : transactions) {
    System.out.println(t.getDescription());
}

// Filter with Stream API
List<Transaction> expenses = transactions.stream()
    .filter(t -> t.getType().equals("EXPENSE"))
    .collect(Collectors.toList());

// Sort
List<Transaction> sorted = transactions.stream()
    .sorted(Comparator.comparing(Transaction::getDate).reversed())
    .collect(Collectors.toList());
```

### ObservableList (For JavaFX Tables)

```java
// Create observable list
ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

// Bind to TableView
tableTransactions.setItems(transactionList);

// Add item — table AUTOMATICALLY updates
transactionList.add(newTransaction);

// Remove item — table AUTOMATICALLY updates
transactionList.remove(selectedTransaction);

// Replace all — table AUTOMATICALLY updates
transactionList.setAll(newList);
```

### Map (Key-Value Pairs)

```java
// Cache category names for fast lookup
Map<Integer, String> categoryMap = new HashMap<>();

// Populate
List<Category> categories = categoryService.getCategories();
for (Category c : categories) {
    categoryMap.put(c.getId(), c.getName());  // ID → Name
}

// Fast lookup
String categoryName = categoryMap.getOrDefault(categoryId, "None");

// Grouping transactions by category
Map<Integer, Double> byCategory = transactions.stream()
    .collect(Collectors.groupingBy(
        Transaction::getCategoryId,
        Collectors.summingDouble(Transaction::getAmount)
    ));

// Iterate
for (Map.Entry<Integer, Double> entry : byCategory.entrySet()) {
    int categoryId = entry.getKey();
    double amount = entry.getValue();
}
```

### Stream API (Functional Programming)

```java
// Chain operations
List<Transaction> results = transactions.stream()
    .filter(t -> t.getType().equals("EXPENSE"))
    .filter(t -> t.getAmount() > 100)
    .sorted(Comparator.comparing(Transaction::getDate).reversed())
    .limit(5)
    .collect(Collectors.toList());

// Map (transform)
List<String> descriptions = transactions.stream()
    .map(Transaction::getDescription)
    .collect(Collectors.toList());

// Sum
double total = transactions.stream()
    .mapToDouble(Transaction::getAmount)
    .sum();

// Group and aggregate
Map<String, Double> byType = transactions.stream()
    .collect(Collectors.groupingBy(
        Transaction::getType,
        Collectors.summingDouble(Transaction::getAmount)
    ));
```

---

## 3.3 Exception Handling

### Try-Catch Pattern

```java
try {
    // Code that might throw exception
    user = authService.login(username, password);
    SessionManager.getInstance().login(user);
    
} catch (AuthenticationException e) {
    // Handle specific error
    AlertUtil.showWarning("Wrong username or password");
    
} catch (ValidationException e) {
    // Handle validation error
    AlertUtil.showWarning("Username and password required");
    
} catch (Exception e) {
    // Catch-all for unexpected errors
    ErrorDialogUtil.showFriendlyError(e);
    
} finally {
    // Always execute (cleanup)
    // Not used in FinTrack much, but good practice
}
```

### Try-With-Resources (Auto-Close)

```java
// Automatically closes PreparedStatement and ResultSet
try (PreparedStatement stmt = connection.prepareStatement(query)) {
    stmt.setInt(1, userId);
    ResultSet rs = stmt.executeQuery();
    
    while (rs.next()) {
        // Process result
    }
    // stmt and rs AUTOMATICALLY closed here
    
} catch (SQLException e) {
    throw new DataAccessException("Query failed", e);
}
```

### Custom Exception Hierarchy

```java
// Base exception
public class FinTrackException extends RuntimeException {
    public FinTrackException(String message) {
        super(message);
    }
    
    public FinTrackException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Specific exceptions
public class AuthenticationException extends FinTrackException { }
public class ValidationException extends FinTrackException { }
public class DataAccessException extends FinTrackException { }

// Usage
throw new ValidationException("Username required");
throw new AuthenticationException("Invalid password");
throw new DataAccessException("Database error", sqlException);
```

---

## 3.4 Design Patterns

### Singleton Pattern

```java
// Only ONE instance exists
public class SessionManager {
    private static SessionManager instance;
    
    private SessionManager() {}  // Private — can't create new instances
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;  // Always same instance
    }
}

// Usage
SessionManager.getInstance().login(user);  // Same instance everywhere
SessionManager.getInstance().getCurrentUser();  // Same instance
```

**Why?**
- ✅ Global access
- ✅ Single source of truth
- ✅ No parameter passing needed

---

### DAO Pattern

```java
// Data Access Object — abstracts database queries

public class TransactionDAO {
    // Create
    public void insert(Transaction t) { }
    
    // Read
    public Transaction selectById(int id) { }
    public List<Transaction> selectByUser(int userId) { }
    
    // Update
    public void update(Transaction t) { }
    
    // Delete
    public void delete(int id) { }
}

// Service uses DAO
public class TransactionService {
    private TransactionDAO dao = new TransactionDAO();
    
    public void addTransaction(Transaction t) {
        // Business logic validation
        if (t.getAmount() <= 0) {
            throw new ValidationException("Invalid amount");
        }
        
        dao.insert(t);  // DAO handles SQL
    }
}

// Controller uses Service
public class TransactionController {
    private TransactionService service = new TransactionService();
    
    @FXML private void handleSave() {
        service.addTransaction(transaction);  // Abstracted
    }
}
```

**Why?**
- ✅ SQL centralized in one place
- ✅ Easy to test (mock DAO)
- ✅ Database independent

---

### Service Layer Pattern

```java
// Business logic layer between Controller and DAO

public class BudgetService {
    private BudgetDAO budgetDAO = new BudgetDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    
    public double getSpendingPercentage(Budget budget) {
        // Business logic
        double spent = getSpentAmount(budget);
        return (spent / budget.getAmount()) * 100;
    }
    
    public double getSpentAmount(Budget budget) {
        // Combine data from multiple sources
        List<Transaction> txns = transactionDAO.selectByDateRange(...);
        return txns.stream()
            .filter(t -> t.getCategoryId() == budget.getCategoryId())
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
}
```

**Why?**
- ✅ Orchestrates multiple DAOs
- ✅ Centralizes business rules
- ✅ Easy to test

---

## 3.5 Generics

```java
// Type-safe collections

List<Transaction> transactions = new ArrayList<>();  // Specify type
transactions.add(new Transaction(...));  // ✅ OK
transactions.add(new Account(...));      // ❌ Compile error

// No casting needed
Transaction t = transactions.get(0);  // Compiler knows it's Transaction

// Generic streams
List<Transaction> expenses = transactions.stream()
    .filter(t -> t.getType().equals("EXPENSE"))
    .collect(Collectors.toList());  // Type parameter: <Transaction>

// ComboBox with types
ComboBox<String> cmbType = new ComboBox<>();
String selected = cmbType.getValue();  // Compiler knows it's String

ComboBox<Category> cmbCategory = new ComboBox<>();
Category selected = cmbCategory.getValue();  // Compiler knows it's Category
```

**Why?**
- ✅ Compile-time type checking
- ✅ No casting needed
- ✅ Cleaner code

---

# SECTION 4: JAVAFX

## 4.1 What is FXML?

**FXML** = FX Markup Language (XML for building UIs)

### Advantages

```
✅ Separates UI from logic
✅ Looks like HTML
✅ Easy to understand
✅ No Java code needed for UI
✅ SceneBuilder integration
```

### Example: dashboard.fxml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.fxml.FXML?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.control.*?>
<?import javafx.scene.chart.*?>

<BorderPane xmlns="http://javafx.com/javafx"
            fx:controller="com.fintrack.controller.DashboardController">
    
    <!-- LEFT: Sidebar Navigation -->
    <left>
        <fx:include source="sidebar.fxml" fx:id="sidebarController"/>
    </left>
    
    <!-- CENTER: Main Content -->
    <center>
        <VBox spacing="15" style="-fx-padding: 20;">
            
            <!-- Welcome Section -->
            <Label fx:id="lblWelcome" style="-fx-font-size: 24; -fx-font-weight: bold;"/>
            
            <!-- Summary Cards -->
            <HBox spacing="15">
                <VBox>
                    <Label text="Total Balance"/>
                    <Label fx:id="lblTotalBalance" text="$0.00" style="-fx-font-size: 24;"/>
                </VBox>
                <VBox>
                    <Label text="Monthly Income"/>
                    <Label fx:id="lblMonthlyIncome" text="+$0.00"/>
                </VBox>
                <VBox>
                    <Label text="Monthly Expense"/>
                    <Label fx:id="lblMonthlyExpense" text="-$0.00"/>
                </VBox>
            </HBox>
            
            <!-- Charts -->
            <BarChart fx:id="barChartCashFlow" title="6-Month Cash Flow"/>
            <PieChart fx:id="pieChartSpending" title="Spending by Category"/>
            
            <!-- Recent Transactions -->
            <TableView fx:id="tableRecent">
                <columns>
                    <TableColumn fx:id="colDate" text="Date"/>
                    <TableColumn fx:id="colDesc" text="Description"/>
                    <TableColumn fx:id="colAmount" text="Amount"/>
                </columns>
            </TableView>
        </VBox>
    </center>
</BorderPane>
```

### How FXML Injection Works

```
1. FXMLLoader reads FXML file
2. Creates all UI nodes (Labels, Buttons, etc.)
3. Looks for @FXML fields in controller
4. Matches fx:id with field name
5. Sets the field value
6. Calls initialize() method

FXML                          Java Controller
<Label fx:id="lblWelcome"/> → @FXML private Label lblWelcome;
<TextField fx:id="txtUsername"/> → @FXML private TextField txtUsername;
<Button onAction="#handleLogin"/> → @FXML private void handleLogin(ActionEvent e)
```

---

## 4.2 Event Handling

### Button Click

```xml
<Button text="Login" onAction="#handleLogin"/>
```

```java
@FXML
private void handleLogin(ActionEvent event) {
    // Handle button click
    user = authService.login(username, password);
}
```

### Text Change Listener

```java
// Whenever text changes, load transactions
txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
    currentPage = 1;
    loadTransactions();
});
```

### ComboBox Selection Change

```java
// When user selects type, filter categories
cmbType.valueProperty().addListener((obs, oldVal, newVal) -> {
    handleTypeChange();
});
```

### DatePicker Change

```java
// When user picks date, reload data
dpFromDate.valueProperty().addListener((obs, oldVal, newVal) -> {
    loadTransactions();
});
```

### Key Press

```java
// Allow Enter key to submit
txtPassword.setOnKeyPressed(event -> {
    if (event.getCode() == KeyCode.ENTER) {
        handleLogin(null);
    }
});
```

---

## 4.3 Observable Collections & Data Binding

### Display Data in TableView

```java
// Create observable list
ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

// Bind to table
tableTransactions.setItems(transactionList);

// Add data
transactionList.add(newTransaction);  // Table AUTOMATICALLY updates!
transactionList.remove(selected);     // Table AUTOMATICALLY updates!

// Replace all
transactionList.setAll(newList);  // Table refreshes automatically
```

### Bind Labels to Properties

```java
// Create property
SimpleStringProperty balanceProperty = new SimpleStringProperty("$0.00");

// Bind label to property
lblBalance.textProperty().bind(balanceProperty);

// When property changes, label updates
balanceProperty.setValue("$1000.00");  // Label text changes automatically!

// Number with formatting
SimpleDoubleProperty amountProperty = new SimpleDoubleProperty(100.0);
lblAmount.textProperty().bind(amountProperty.asString("$%.2f"));
// Label shows: "$100.00"
```

---

## 4.4 TableView & Columns

### Setup Table Columns

```java
// Date column
colDate.setCellValueFactory(data -> 
    new SimpleStringProperty(data.getValue().getDate().toString())
);

// Amount column (formatted)
colAmount.setCellValueFactory(data -> {
    Transaction t = data.getValue();
    String formatted = CurrencyUtil.formatSimple(t.getAmount());
    String sign = t.getType().equals("EXPENSE") ? "-" : "+";
    return new SimpleStringProperty(sign + formatted);
});

// Category column (from map lookup)
colCategory.setCellValueFactory(data -> 
    new SimpleStringProperty(categoryMap.getOrDefault(data.getValue().getCategoryId(), "None"))
);

// Bind table data
tableTransactions.setItems(transactionList);
```

### Add Action Buttons to Table

```java
// Edit and Delete buttons in each row
colActions.setCellFactory(param -> new TableCell<>() {
    private final Button btnEdit = new Button("Edit");
    private final Button btnDelete = new Button("Delete");
    private final HBox pane = new HBox(8, btnEdit, btnDelete);
    
    {
        btnEdit.setOnAction(event -> {
            Transaction selected = getTableView().getItems().get(getIndex());
            handlePrepareEdit(selected);
        });
        
        btnDelete.setOnAction(event -> {
            Transaction selected = getTableView().getItems().get(getIndex());
            handleDelete(selected);
        });
    }
    
    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : pane);
    }
});
```

---

## 4.5 Charts

### Bar Chart (6-Month Cash Flow)

```java
// Create series
XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
incomeSeries.setName("Income");

XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
expenseSeries.setName("Expense");

// Add data points
for (int i = 5; i >= 0; i--) {
    LocalDate monthDate = now.minusMonths(i);
    double income = transactionService.getTotalIncome(..., monthDate, ...);
    double expense = transactionService.getTotalExpense(..., monthDate, ...);
    
    incomeSeries.getData().add(new XYChart.Data<>("Jan", income));
    expenseSeries.getData().add(new XYChart.Data<>("Jan", expense));
}

// Add to chart
barChartCashFlow.getData().clear();
barChartCashFlow.getData().addAll(incomeSeries, expenseSeries);
```

### Pie Chart (Spending by Category)

```java
ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

// Group transactions by category
Map<Integer, Double> byCategory = transactions.stream()
    .filter(t -> t.getType().equals("EXPENSE"))
    .collect(Collectors.groupingBy(
        Transaction::getCategoryId,
        Collectors.summingDouble(Transaction::getAmount)
    ));

// Add to pie chart
for (Map.Entry<Integer, Double> entry : byCategory.entrySet()) {
    Category cat = categoryService.getCategoryById(entry.getKey());
    if (cat != null) {
        pieData.add(new PieChart.Data(cat.getName(), entry.getValue()));
    }
}

pieChartSpending.setData(pieData);
```

### Line Chart (Trends)

```java
// Similar to BarChart
XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
incomeSeries.setName("Income");

for (int i = 5; i >= 0; i--) {
    LocalDate monthDate = now.minusMonths(i);
    double income = transactionService.getTotalIncome(...);
    incomeSeries.getData().add(new XYChart.Data<>(
        monthDate.format(DateTimeFormatter.ofPattern("MMM")), 
        income
    ));
}

lineChart.getData().add(incomeSeries);
```

---

## 4.6 CSS Styling

### Global Styles

```css
.root {
    -fx-font-family: "Segoe UI", Arial, sans-serif;
    -fx-font-size: 12px;
    -fx-background-color: #f5f5f5;
}
```

### Buttons

```css
.button {
    -fx-padding: 10px 20px;
    -fx-border-radius: 4px;
    -fx-font-size: 13px;
}

.button:hover {
    -fx-opacity: 0.9;
}

.button-primary {
    -fx-background-color: #2563eb;
    -fx-text-fill: white;
}

.button-danger {
    -fx-background-color: #ef4444;
    -fx-text-fill: white;
}
```

### Cards

```css
.card {
    -fx-border-width: 1;
    -fx-border-color: #e5e7eb;
    -fx-border-radius: 8;
    -fx-background-color: #ffffff;
    -fx-padding: 15;
}
```

### Budget Progress Bar

```css
.budget-bar-track {
    -fx-background-color: #e5e7eb;
    -fx-border-radius: 4;
}

.budget-bar-fill {
    -fx-background-color: #3b82f6;
}

.budget-bar-fill.warning {
    -fx-background-color: #f59e0b;  /* Yellow when 85%+ spent */
}

.budget-bar-fill.danger {
    -fx-background-color: #ef4444;  /* Red when 100%+ spent */
}
```

### Applying CSS in Code

```java
// Apply style to specific node
lblAtRisk.setStyle("-fx-text-fill: #EF4444;");

// Add style class
button.getStyleClass().add("button-danger");

// Remove style class
button.getStyleClass().remove("button-primary");

// Toggle style
if (isWarning) {
    fill.getStyleClass().add("warning");
} else {
    fill.getStyleClass().remove("warning");
}
```

---

# SECTION 5: DATABASE

## 5.1 Database Schema

### 6 Tables

```sql
USERS TABLE
├─ id (PRIMARY KEY)
├─ username (UNIQUE)
├─ password_hash (bcrypt)
└─ email

ACCOUNTS TABLE
├─ id (PRIMARY KEY)
├─ user_id (FOREIGN KEY → users)
├─ name
├─ type (Checking, Savings, Credit Card, Cash, Investment)
└─ balance

CATEGORIES TABLE
├─ id (PRIMARY KEY)
├─ user_id (FOREIGN KEY → users)
├─ name
└─ type (INCOME or EXPENSE)

TRANSACTIONS TABLE
├─ id (PRIMARY KEY)
├─ user_id (FOREIGN KEY → users)
├─ account_id (FOREIGN KEY → accounts)
├─ category_id (FOREIGN KEY → categories, nullable)
├─ amount
├─ type (INCOME, EXPENSE, TRANSFER)
├─ description
└─ transaction_date

BUDGETS TABLE
├─ id (PRIMARY KEY)
├─ user_id (FOREIGN KEY → users)
├─ category_id (FOREIGN KEY → categories)
├─ amount
├─ period (Monthly, Weekly, Yearly)
├─ start_date
└─ end_date
```

### Relationships

```
One User → Many Accounts
One User → Many Categories
One User → Many Transactions → One Account
                          → One Category
One User → Many Budgets → One Category
```

---

## 5.2 JDBC Flow

```
1. LOAD DRIVER
   └─ Class.forName("org.sqlite.JDBC");

2. CREATE CONNECTION
   └─ Connection conn = DriverManager.getConnection("jdbc:sqlite:fintrack.db");

3. CREATE STATEMENT
   └─ PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");

4. SET PARAMETERS
   └─ stmt.setInt(1, userId);

5. EXECUTE QUERY
   └─ ResultSet rs = stmt.executeQuery();

6. PROCESS RESULTS
   while (rs.next()) {
       int id = rs.getInt("id");
       String username = rs.getString("username");
   }

7. CLOSE RESOURCES
   └─ rs.close(); stmt.close(); conn.close();
```

---

## 5.3 PreparedStatement (SQL Injection Prevention)

### ❌ VULNERABLE (Don't Do This)

```java
String username = txtUsername.getText();
String password = txtPassword.getText();

// DANGEROUS! SQL Injection possible
String query = "SELECT * FROM users WHERE username = '" + username + 
               "' AND password = '" + password + "'";

// If user enters: ' OR '1'='1
// Query becomes: SELECT * FROM users WHERE username = '' OR '1'='1' AND password = ''
// Returns ALL users!
```

### ✅ SAFE (Use This)

```java
String query = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

try (PreparedStatement stmt = connection.prepareStatement(query)) {
    stmt.setString(1, username);      // Parameter 1
    stmt.setString(2, passwordHash);   // Parameter 2
    
    ResultSet rs = stmt.executeQuery();
    // Values NEVER interpreted as SQL code
}
```

**Why PreparedStatement?**
- ✅ SQL and parameters separated
- ✅ Values treated as data, not code
- ✅ Pre-compiled for performance

---

## 5.4 ResultSet Mapping

### Extract Data from Query Results

```java
ResultSet rs = stmt.executeQuery();

while (rs.next()) {
    // Extract columns
    int id = rs.getInt("id");
    String username = rs.getString("username");
    double balance = rs.getDouble("balance");
    java.sql.Date sqlDate = rs.getDate("created_at");
    
    // Convert to Java objects
    LocalDate date = sqlDate.toLocalDate();
    
    // Create model object
    User u = new User();
    u.setId(id);
    u.setUsername(username);
    u.setCreatedAt(date);
}
```

### Mapper Pattern

```java
private User mapResultSetToUser(ResultSet rs) throws SQLException {
    User u = new User();
    u.setId(rs.getInt("id"));
    u.setUsername(rs.getString("username"));
    u.setEmail(rs.getString("email"));
    
    java.sql.Date sqlDate = rs.getDate("created_at");
    if (sqlDate != null) {
        u.setCreatedAt(sqlDate.toLocalDate());
    }
    
    return u;
}

// Usage in DAO
List<User> users = new ArrayList<>();
while (rs.next()) {
    users.add(mapResultSetToUser(rs));
}
```

---

## 5.5 CRUD Operations

### CREATE (Insert)

```java
public void insert(Transaction t) {
    String query = "INSERT INTO transactions " +
                   "(user_id, account_id, category_id, amount, type, description, transaction_date) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, t.getUserId());
        stmt.setInt(2, t.getAccountId());
        stmt.setInt(3, t.getCategoryId());
        stmt.setDouble(4, t.getAmount());
        stmt.setString(5, t.getType());
        stmt.setString(6, t.getDescription());
        stmt.setDate(7, java.sql.Date.valueOf(t.getDate()));
        
        stmt.executeUpdate();  // Execute INSERT
    }
}
```

### READ (Select)

```java
public Transaction selectById(int id) {
    String query = "SELECT * FROM transactions WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return mapResultSetToTransaction(rs);
        }
    }
    
    return null;
}

public List<Transaction> selectByUser(int userId) {
    String query = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC";
    List<Transaction> transactions = new ArrayList<>();
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            transactions.add(mapResultSetToTransaction(rs));
        }
    }
    
    return transactions;
}
```

### UPDATE (Modify)

```java
public void update(Transaction t) {
    String query = "UPDATE transactions SET " +
                   "amount = ?, type = ?, description = ?, transaction_date = ? " +
                   "WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setDouble(1, t.getAmount());
        stmt.setString(2, t.getType());
        stmt.setString(3, t.getDescription());
        stmt.setDate(4, java.sql.Date.valueOf(t.getDate()));
        stmt.setInt(5, t.getId());
        
        stmt.executeUpdate();  // Execute UPDATE
    }
}
```

### DELETE (Remove)

```java
public void delete(int id) {
    String query = "DELETE FROM transactions WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, id);
        stmt.executeUpdate();  // Execute DELETE
    }
}
```

---

# SECTION 6: RAPID FIRE Q&A

## Quick Question & Answer Reference

### JAVA

**Q1: What's the difference between List and ArrayList?**
A: List is interface; ArrayList is implementation. Use List in code for flexibility.

**Q2: What are generics?**
A: Type parameters that make collections type-safe. `List<Transaction>` vs `List` (raw type).

**Q3: What's try-with-resources?**
A: `try (Resource r = new Resource()) { }` auto-closes resource.

**Q4: Explain encapsulation**
A: Bundle data (private fields) + methods (public getters/setters) to hide implementation.

**Q5: What's inheritance?**
A: Create new classes extending existing ones to reuse code. Example: `AuthenticationException extends FinTrackException`.

**Q6: What's polymorphism?**
A: Same method call behaves differently based on object type. Example: Exception handling.

**Q7: What's an interface?**
A: Contract defining methods classes must implement. Allows loose coupling.

**Q8: What's a stream?**
A: Functional approach to process collections. `list.stream().filter(...).collect(...)`.

**Q9: Difference between checked and unchecked exceptions?**
A: Checked must be caught (SQLException); Unchecked don't (RuntimeException).

**Q10: What's the Singleton pattern?**
A: Only one instance of class exists globally. Example: SessionManager.

---

### JAVAFX

**Q1: What is FXML?**
A: XML-based language for building UIs; separates UI from Java code.

**Q2: How does @FXML work?**
A: FXMLLoader injects UI components into @FXML fields based on fx:id matching.

**Q3: What's ObservableList?**
A: List that notifies UI when data changes; TableView auto-updates.

**Q4: How to handle button click?**
A: Add `onAction="#handleClick"` in FXML or `button.setOnAction(e -> { })` in Java.

**Q5: How to populate a TableView?**
A: Create columns, set cell factories, bind ObservableList via `setItems()`.

**Q6: What's data binding?**
A: Automatically sync property changes to UI. `label.textProperty().bind(property)`.

**Q7: How to add a chart?**
A: Create XYChart.Series, add data points, add to chart.

**Q8: How to style UI?**
A: Use CSS files or `setStyle("-fx-background-color: red;")`.

**Q9: How to navigate between scenes?**
A: Use SceneNavigator to load different FXML files into primary stage.

**Q10: How to make UI responsive?**
A: Use `AsyncUtil.runAsync()` to load data on background thread, update UI on JavaFX thread.

---

### DATABASE

**Q1: What's JDBC?**
A: Java Database Connectivity API for connecting to databases.

**Q2: What's PreparedStatement?**
A: Pre-compiled SQL query with parameters; prevents SQL injection.

**Q3: What's ResultSet?**
A: Container holding query results; iterate with `next()`, extract with `getInt()`, etc.

**Q4: What's a PRIMARY KEY?**
A: Unique identifier for each row; auto-increment by default.

**Q5: What's a FOREIGN KEY?**
A: Reference to PRIMARY KEY in another table; maintains relationships.

**Q6: What's normalization?**
A: Organize data to reduce redundancy; FinTrack: separate tables for users, accounts, transactions.

**Q7: How to prevent SQL injection?**
A: Use PreparedStatement with parameters instead of string concatenation.

**Q8: What's ACID compliance?**
A: Atomicity, Consistency, Isolation, Durability; SQLite provides this.

**Q9: What's a transaction?**
A: Group of SQL statements executed as one unit; all succeed or all fail.

**Q10: Why JDBC over Hibernate?**
A: Direct query control, less overhead, perfect for desktop app.

---

### ARCHITECTURE

**Q1: What's MVC?**
A: Model (data) + View (UI) + Controller (logic); separates concerns.

**Q2: Explain 4-layer architecture**
A: Presentation (UI) → Business Logic (Services) → Data Access (DAOs) → Database.

**Q3: What's a DAO?**
A: Data Access Object; abstracts database queries from business logic.

**Q4: What's a Service?**
A: Business logic layer; orchestrates DAOs and validates data.

**Q5: Why layered architecture?**
A: Separation of concerns, testability, maintainability, scalability.

**Q6: How to test layered architecture?**
A: Mock DAOs to test services; mock services to test controllers.

**Q7: What's dependency injection?**
A: Pass objects to constructors instead of creating inside; improves testability.

**Q8: Why singleton for SessionManager?**
A: Global access to current user without parameter passing.

**Q9: How does SceneNavigator work?**
A: Cache FXML files, load once, switch scenes without reloading.

**Q10: What's the flow when adding transaction?**
A: Controller → Service → DAO → SQL → Database → DAO → Service → Controller → UI update.

---

# SECTION 7: TOUGH QUESTIONS

## Examiner's Difficult Questions & Strong Answers

### Q1: Why JavaFX over Swing?

**Weak Answer:** "JavaFX is newer than Swing."

**Strong Answer:**
- Modern CSS styling support (not possible in Swing)
- Built-in chart components (BarChart, PieChart, LineChart)
- Observable collections that auto-update UI
- Scene graph architecture (better performance)
- Property binding (reduce boilerplate)
- FXML for declarative UI (clean separation)
- Active development; Swing is legacy
- Better animation support
- In FinTrack: CSS for professional styling, built-in charts for analytics, ObservableList for TableViews

---

### Q2: Why SQLite over MySQL/PostgreSQL?

**Weak Answer:** "SQLite is simpler."

**Strong Answer:**
- Zero configuration (MySQL needs server setup)
- Single file database (easy backup, portability)
- No server process (desktop app doesn't need it)
- Embedded in Java (pure Java JDBC driver)
- ACID compliance (transactions, reliability)
- Perfect for single-user offline application
- Sufficient for expected data volume (thousands of transactions)
- Reduced complexity and dependencies
- If need to scale: only DAO layer changes, services/controllers unchanged

---

### Q3: Why DAO pattern instead of direct SQL in controllers?

**Weak Answer:** "It's cleaner."

**Strong Answer:**
- Separation of concerns: SQL in one place, not scattered across controllers
- Testability: Mock DAO for unit testing services without database
- Database independence: Change SQLite to MySQL? Only modify DAOs
- Reusability: Same DAO used by multiple services
- Maintainability: SQL bug? Fix in DAO, not multiple places
- Consistency: All queries follow same pattern
- Query optimization: Central place to optimize SQL

Example:
```
Without DAO:
├─ TransactionController.loadTransactions() has SQL query
├─ BudgetService.getSpentAmount() has similar SQL query
├─ ReportService.getAnalytics() has another SQL query
Bug in query? Fix in 3 places!

With DAO:
├─ TransactionDAO.selectByUser() has SQL query
├─ BudgetService calls DAO
├─ ReportService calls DAO
Bug in query? Fix in one place!
```

---

### Q4: How would you scale FinTrack to multi-user cloud application?

**Architecture Changes:**

```
Current (Desktop, Single User, Local DB):
┌─────────────────┐
│ JavaFX Frontend │
└────────┬────────┘
         │
    ┌────▼────┐
    │ SQLite  │
    └─────────┘

Scaled (Web, Multi-User, Cloud):
┌─────────────────────────────────┐
│ Web Frontend (React/Vue)         │ (Multiple users)
│ Desktop Client (JavaFX)          | (Different language but same logic)
└────────┬────────────────────────┘
         │ HTTP/REST/GraphQL
    ┌────▼──────────────────┐
    │ Spring Boot Backend    │ (Business logic)
    └─────┬──────────────────┘
          │ JPA/Hibernate
    ┌─────▼──────────────────┐
    │ PostgreSQL             │ (Multi-user database)
    │ Redis Cache            │ (Performance)
    └────────────────────────┘

Code Changes:
├─ Controllers: Same logic (separated already with services)
├─ Services: Same business logic (no changes needed)
├─ DAO: Replace with Repository interfaces (Spring Data JPA)
│       Could keep same queries
└─ Models: Add @Entity annotations for JPA
```

**Why FinTrack Architecture Makes This Easy:**
- Layered architecture: Presentation independent of Data Access
- Services isolated: Business logic reusable
- DAOs abstracted: Easy to replace JDBC with JPA
- Models simple: Easy to convert to entities

---

### Q5: What are security weaknesses in FinTrack?

**Be Honest:**

1. **Session Management**
   - Currently in-memory: Lost on app restart
   - Solution: Save to secured file or database with encryption

2. **Database Not Encrypted**
   - SQLite file readable if someone accesses disk
   - Solution: SQLCipher for encrypted SQLite

3. **Password Storage**
   - Using bcrypt (good!)
   - But could add: password strength requirements, 2FA

4. **No Audit Trail**
   - Can't track who changed what when
   - Solution: Add audit table logging all changes

5. **No Input Validation on Amount**
   - Could allow extremely large numbers
   - Solution: Add validation: `amount > 0 && amount < 999999999`

6. **FXML Hardcoded Paths**
   - String paths could break
   - Solution: Use ResourceBundle for paths

7. **No Rate Limiting**
   - If web version: no protection against brute force login
   - Solution: Rate limiting middleware

**Strong Answer:**
"FinTrack uses bcrypt for password hashing (best practice), but for production:
1. Encrypt database with SQLCipher
2. Add persistent encrypted session tokens
3. Implement 2FA for important accounts
4. Add comprehensive input validation
5. Create audit logs for changes
6. Implement rate limiting if web-based
All feasible within the layered architecture."

---

### Q6: How would you implement a dark mode?

**Approach:**

```java
// Create CSS for both themes
styles.css           // Light theme
styles-dark.css      // Dark theme

// Store preference
public class PreferenceManager {
    private static final String THEME_KEY = "theme";
    
    public static void setTheme(String theme) {
        Preferences.userRoot().put(THEME_KEY, theme);
    }
    
    public static String getTheme() {
        return Preferences.userRoot().get(THEME_KEY, "light");
    }
}

// Switch themes
public void switchTheme(String theme) {
    Scene scene = primaryStage.getScene();
    
    // Remove old stylesheet
    scene.getStylesheets().remove(0);
    
    // Add new stylesheet
    String css = theme.equals("dark") ? 
        "/css/styles-dark.css" : 
        "/css/styles.css";
    scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    
    // Save preference
    PreferenceManager.setTheme(theme);
}

// Load saved theme on startup
@Override
public void start(Stage stage) {
    String theme = PreferenceManager.getTheme();
    
    Scene scene = new Scene(root);
    String css = theme.equals("dark") ? 
        "/css/styles-dark.css" : 
        "/css/styles.css";
    scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    
    stage.setScene(scene);
}
```

---

### Q7: What metrics would you use to measure app performance?

**Key Metrics:**

```
1. LOAD TIME
   ├─ Time to load dashboard: target < 2 seconds
   ├─ Time to load transactions table: target < 1 second
   └─ Measurement: System.nanoTime()

2. MEMORY USAGE
   ├─ Baseline: target < 150MB
   ├─ After loading 1000 transactions: target < 300MB
   └─ Measurement: Runtime.getRuntime().totalMemory()

3. UI RESPONSIVENESS
   ├─ No UI freeze during data loading
   ├─ Transactions per second: handle 100+ queries/sec
   └─ Measurement: FPS counter in JavaFX

4. DATABASE PERFORMANCE
   ├─ Query time for 1000 transactions: target < 500ms
   ├─ Index on user_id (foreign key lookup)
   └─ Measurement: System.nanoTime() around DAO calls

5. ERROR RECOVERY
   ├─ Handle database disconnect gracefully
   ├─ Retry logic for network issues
   └─ User-friendly error messages
```

**How to Implement:**

```java
public class PerformanceUtil {
    public static void measureTime(String operation, Runnable r) {
        long start = System.nanoTime();
        r.run();
        long elapsed = (System.nanoTime() - start) / 1_000_000;  // ms
        System.out.println(operation + " took " + elapsed + "ms");
    }
}

// Usage
PerformanceUtil.measureTime("Load Dashboard", () -> {
    loadDashboardAsync();
});

// Output: "Load Dashboard took 1250ms"
```

---

### Q8: How would you implement offline-sync for a mobile version?

**Approach:**

```
ONLINE MODE
├─ Update local SQLite immediately
├─ Send changes to server (queue if offline)
└─ Sync when connection restored

OFFLINE MODE
├─ All operations work locally
├─ Changes queued in "pending_changes" table
└─ Show "offline mode" indicator

SYNC STRATEGY
├─ Track: INSERT, UPDATE, DELETE in sync_queue
├─ When online: Send batched changes to server
├─ Conflict resolution: Last-write-wins
└─ Keep timestamps for version control

Implementation:
1. Add sync_queue table
   ├─ operation (INSERT/UPDATE/DELETE)
   ├─ table_name (transactions, budgets, etc.)
   ├─ record_id
   ├─ data (JSON)
   └─ is_synced (boolean)

2. Intercept DAO operations
   ├─ After insert: Add to sync_queue
   ├─ After update: Add to sync_queue
   ├─ After delete: Add to sync_queue

3. Background sync service
   ├─ Check connectivity
   ├─ If online: Upload pending changes
   ├─ If conflict: Merge or ask user
   └─ Mark as synced
```

**Code Example:**

```java
public class SyncQueue {
    public static void addPendingChange(String operation, String tableName, int recordId, String data) {
        String query = "INSERT INTO sync_queue (operation, table_name, record_id, data, is_synced) " +
                       "VALUES (?, ?, ?, ?, 0)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, operation);
            stmt.setString(2, tableName);
            stmt.setInt(3, recordId);
            stmt.setString(4, data);
            stmt.executeUpdate();
        }
    }
    
    public static List<SyncItem> getPendingChanges() {
        String query = "SELECT * FROM sync_queue WHERE is_synced = 0";
        // ... execute and return
    }
    
    public static void markAsSynced(int id) {
        String query = "UPDATE sync_queue SET is_synced = 1 WHERE id = ?";
        // ... execute
    }
}

// In TransactionDAO
public void insert(Transaction t) {
    // Insert locally
    transactionDAO.insert(t);
    
    // Queue for sync
    SyncQueue.addPendingChange("INSERT", "transactions", t.getId(), 
        new JSONObject(t).toString());
}
```

---

### Q9: Explain a complex feature: Advanced Transaction Filtering

**Feature Implementation:**

```
User selects filters:
├─ Type: EXPENSE
├─ Category: Groceries
├─ Date: Jan 1 - Jan 31
├─ Amount: $50 - $200
├─ Search: "store"
└─ Sort: Amount (Highest First)

Dynamic SQL Construction:

String query = "SELECT * FROM transactions WHERE user_id = ?";
List<Object> params = new ArrayList<>();
params.add(userId);

if (!"All".equals(type)) {
    query += " AND type = ?";
    params.add(type);
}

if (categoryId != null) {
    query += " AND category_id = ?";
    params.add(categoryId);
}

if (fromDate != null) {
    query += " AND transaction_date >= ?";
    params.add(fromDate);
}

if (toDate != null) {
    query += " AND transaction_date <= ?";
    params.add(toDate);
}

if (minAmount != null) {
    query += " AND amount >= ?";
    params.add(minAmount);
}

if (maxAmount != null) {
    query += " AND amount <= ?";
    params.add(maxAmount);
}

if (keyword != null && !keyword.isEmpty()) {
    query += " AND description LIKE ?";
    params.add("%" + keyword + "%");
}

// Sorting
query += " ORDER BY " + sortBy + " " + sortOrder;

// Pagination
query += " LIMIT ? OFFSET ?";
params.add(pageSize);
params.add(offset);

// Execute with all parameters
PreparedStatement stmt = connection.prepareStatement(query);
for (int i = 0; i < params.size(); i++) {
    stmt.setObject(i + 1, params.get(i));
}

ResultSet rs = stmt.executeQuery();
// ... process results
```

**Why This Works:**
- Dynamic query: Only add filters user selected
- No SQL injection: All parameters via PreparedStatement
- Pagination: Load page_size + 1 to detect next page
- Sorting: User chooses sort field
- Async: Background thread loads data

---

### Q10: What if database grows to 100,000 transactions?

**Performance Issues:**

```
1. SLOW QUERIES
   ├─ SELECT * FROM transactions takes 5+ seconds
   ├─ Solution: Add indexes on frequently queried columns
   │   CREATE INDEX idx_user_date ON transactions(user_id, transaction_date);
   │   CREATE INDEX idx_category ON transactions(category_id);
   └─ Measure: Before index, after index

2. MEMORY BLOAT
   ├─ Loading all transactions at once: OutOfMemoryError
   ├─ Solution: Pagination (always load page_size only)
   │   SELECT * FROM transactions WHERE user_id = ? LIMIT 15 OFFSET 0;
   └─ Currently: Load 15 per page (scalable)

3. CHART GENERATION
   ├─ Building 6-month chart from 100k transactions: slow
   ├─ Solution: Pre-aggregate data
   │   CREATE TABLE monthly_summary (
   │       user_id, month, total_income, total_expense
   │   );
   └─ Update nightly via scheduled job

4. LARGE TABLE OPERATIONS
   ├─ Delete all transactions for a user (cascade): slow
   ├─ Solution: Soft delete (add deleted_at timestamp)
   │   SELECT * FROM transactions WHERE deleted_at IS NULL;
   └─ Faster than physical delete

Implementation Strategy:
├─ Add indexes: 1 day
├─ Implement pagination: Already done!
├─ Create summary tables: 2 days
├─ Cache aggregated results: 1 day
└─ Test with 100k rows
```

---

## Final Tips for Viva

### 1. Structure Your Answers

**Good Structure:**
1. **Definition** — What is it?
2. **Example** — Show in FinTrack code
3. **Benefits** — Why use it?
4. **Implementation** — How does it work?

### 2. Reference the Code

Always say: "In FinTrack, in `TransactionController.java` line 250, we..."

### 3. Don't Memorize Syntax

Examiners care about understanding, not perfect syntax. Say: "We use a try-with-resources statement to auto-close the PreparedStatement."

### 4. Be Honest About Limitations

Say: "FinTrack currently stores sessions in memory, which is lost on restart. For production, we'd use encrypted persistent sessions."

### 5. Think Big Picture

Relate questions back to project layers:
- Why pattern X? "Because it separates concerns in our layered architecture"
- Why technology Y? "Because it fits the desktop app requirements"

### 6. Ask for Clarification

If unclear: "Are you asking about how transactions are filtered in the UI, or how the DAO constructs the SQL?"

### 7. Use Analogies

"MVC is like a restaurant: Model = food, View = plate presentation, Controller = waiter connecting kitchen to customer."

---

## Most Likely Viva Questions (Priority)

### MUST KNOW

1. ✅ Explain MVC architecture in FinTrack
2. ✅ How does login/authentication work?
3. ✅ Explain DAO pattern and PreparedStatement
4. ✅ How are transactions filtered and displayed?
5. ✅ How do budgets work (progress bars, overspending detection)?
6. ✅ Database schema and relationships
7. ✅ How is SessionManager implemented (Singleton)?
8. ✅ Explain exception handling strategy
9. ✅ How does Dashboard load async?
10. ✅ Why JavaFX + SQLite + JDBC?

### SHOULD KNOW

1. ✅ FXML injection and @FXML annotation
2. ✅ ObservableList and data binding
3. ✅ Chart generation (BarChart, PieChart)
4. ✅ Layered architecture benefits
5. ✅ Stream API and collections
6. ✅ CSS styling and property binding
7. ✅ Try-with-resources and exception handling
8. ✅ SceneNavigator caching strategy
9. ✅ Service layer validation
10. ✅ Pagination implementation

### NICE TO KNOW

1. ✅ Design patterns (Singleton, DAO, Service, Factory)
2. ✅ How to scale to multi-user web app
3. ✅ Security improvements
4. ✅ Performance optimization
5. ✅ Testing strategy
6. ✅ Offline sync capability
7. ✅ Dark mode implementation
8. ✅ Encrypted database
9. ✅ Audit logging
10. ✅ Index optimization

---

**Good Luck with Your Viva! 🎓**

Remember:
- Know your code deeply
- Explain concepts clearly with examples
- Be honest about what you know and don't know
- Ask clarifying questions
- Show understanding over memorization
