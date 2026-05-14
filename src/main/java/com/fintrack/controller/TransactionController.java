package com.fintrack.controller;

import com.fintrack.exception.ValidationException;
import com.fintrack.model.Account;
import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.AccountService;
import com.fintrack.service.CategoryService;
import com.fintrack.service.TransactionService;
import com.fintrack.session.SessionManager;
import com.fintrack.util.AlertUtil;
import com.fintrack.util.AsyncUtil;
import com.fintrack.util.CurrencyUtil;
import com.fintrack.util.ValidationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionController {

    @FXML private SidebarController sidebarController;

    // Filters
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterType;
    @FXML private ComboBox<CategoryItem> cmbFilterCategory;
    @FXML private DatePicker dpFilterFrom;
    @FXML private DatePicker dpFilterTo;
    @FXML private TextField txtMinAmount;
    @FXML private TextField txtMaxAmount;
    @FXML private ComboBox<String> cmbSortBy;

    // Table Elements
    @FXML private TableView<Transaction> tableTransactions;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colDesc;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colAccount;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, Void> colActions;

    // Pagination
    @FXML private Label lblPaginationInfo;
    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;

    // Form Elements
    @FXML private VBox formCard;
    @FXML private Label lblFormTitle;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtAmount;
    @FXML private TextField txtDescription;
    @FXML private ComboBox<CategoryItem> cmbCategory;
    @FXML private ComboBox<AccountItem> cmbAccount;
    @FXML private DatePicker dpDate;

    // Services
    private TransactionService transactionService;
    private AccountService accountService;
    private CategoryService categoryService;
    
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final Map<Integer, String> accountMap = new HashMap<>();
    private final Map<Integer, String> categoryMap = new HashMap<>();

    private int currentUserId;
    private Transaction currentEditTransaction = null;
    
    // Pagination State
    private int currentPage = 1;
    private final int PAGE_SIZE = 15;

    public TransactionController() {}

    @FXML
    public void initialize() {
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == -1) {
            Platform.runLater(() -> SceneNavigator.navigateTo("login.fxml"));
            return;
        }

        if (sidebarController != null) {
            sidebarController.setActive("transactions");
        }

        try {
            transactionService = new TransactionService();
            accountService = new AccountService();
            categoryService = new CategoryService();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        setupTable();
        
        AsyncUtil.runAsync(() -> {
            loadDropdownDataSync();
            return null;
        }, result -> {
            loadTransactions();
        }, error -> AlertUtil.showError("Failed to initialize transactions view."));
    }

    private void setupFilters() {
        txtSearch.textProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            loadTransactions();
        });
        txtMinAmount.textProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            loadTransactions();
        });
        txtMaxAmount.textProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            loadTransactions();
        });
        
        cmbFilterType.setOnAction(e -> { currentPage = 1; loadTransactions(); });
        cmbFilterCategory.setOnAction(e -> { currentPage = 1; loadTransactions(); });
        dpFilterFrom.setOnAction(e -> { currentPage = 1; loadTransactions(); });
        dpFilterTo.setOnAction(e -> { currentPage = 1; loadTransactions(); });
        cmbSortBy.setOnAction(e -> { currentPage = 1; loadTransactions(); });
    }

    private void setupTable() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        colDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        // Note: You can add CellFactory to colType to render custom styled badges
        
        colAmount.setCellValueFactory(data -> {
            Transaction t = data.getValue();
            String formatted = CurrencyUtil.formatSimple(t.getAmount());
            return new SimpleStringProperty(t.getType().equals("EXPENSE") ? "-" + formatted : "+" + formatted);
        });
        
        colAccount.setCellValueFactory(data -> 
            new SimpleStringProperty(accountMap.getOrDefault(data.getValue().getAccountId(), "Unknown"))
        );

        colCategory.setCellValueFactory(data -> 
            new SimpleStringProperty(categoryMap.getOrDefault(data.getValue().getCategoryId(), "None"))
        );

        setupActionColumn();
        tableTransactions.setItems(transactionList);
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.setGraphic(new FontIcon("fas-pen"));
                btnEdit.getStyleClass().addAll("action-icon-button", "text-muted");
                btnEdit.setStyle("-fx-border-width: 0;");
                btnEdit.setOnAction(event -> handlePrepareEdit(getTableView().getItems().get(getIndex())));

                btnDelete.setGraphic(new FontIcon("fas-trash"));
                btnDelete.getStyleClass().addAll("action-icon-button", "text-danger");
                btnDelete.setStyle("-fx-border-width: 0;");
                btnDelete.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
                
                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadDropdownDataSync() {
        // Runs on background thread
        List<Account> accounts = accountService.getAccountsByUser(currentUserId);
        List<Category> categories = categoryService.getCategoriesByUser(currentUserId);
        
        Platform.runLater(() -> {
            cmbType.setItems(FXCollections.observableArrayList("EXPENSE", "INCOME", "TRANSFER"));
            cmbFilterType.setItems(FXCollections.observableArrayList("All", "EXPENSE", "INCOME", "TRANSFER"));
            cmbFilterType.getSelectionModel().selectFirst();

            ObservableList<AccountItem> accountItems = FXCollections.observableArrayList();
            for (Account a : accounts) {
                accountMap.put(a.getId(), a.getName());
                accountItems.add(new AccountItem(a.getId(), a.getName()));
            }
            cmbAccount.setItems(accountItems);

            ObservableList<CategoryItem> categoryItems = FXCollections.observableArrayList();
            ObservableList<CategoryItem> filterCatItems = FXCollections.observableArrayList();
            filterCatItems.add(new CategoryItem(-1, "All Categories"));

            for (Category c : categories) {
                categoryMap.put(c.getId(), c.getName());
                CategoryItem item = new CategoryItem(c.getId(), c.getName(), c.getType());
                categoryItems.add(item);
                filterCatItems.add(item);
            }
            cmbCategory.setItems(categoryItems);
            cmbFilterCategory.setItems(filterCatItems);
            cmbFilterCategory.getSelectionModel().selectFirst();

            cmbSortBy.setItems(FXCollections.observableArrayList(
                    "Date (Newest First)", 
                    "Date (Oldest First)", 
                    "Amount (Highest First)", 
                    "Amount (Lowest First)",
                    "Description (A-Z)"
            ));
            cmbSortBy.getSelectionModel().selectFirst();
        });
    }

    private void loadTransactions() {
        int offset = (currentPage - 1) * PAGE_SIZE;
        
        String type = cmbFilterType.getValue();
        Integer catId = cmbFilterCategory.getValue() != null && cmbFilterCategory.getValue().id != -1 
                        ? cmbFilterCategory.getValue().id : null;
        LocalDate from = dpFilterFrom.getValue();
        LocalDate to = dpFilterTo.getValue();
        String keyword = txtSearch.getText();

        Double minAmount = null;
        if (txtMinAmount.getText() != null && !txtMinAmount.getText().isEmpty()) {
            try { minAmount = Double.parseDouble(txtMinAmount.getText().trim()); } catch (Exception ignored) {}
        }

        Double maxAmount = null;
        if (txtMaxAmount.getText() != null && !txtMaxAmount.getText().isEmpty()) {
            try { maxAmount = Double.parseDouble(txtMaxAmount.getText().trim()); } catch (Exception ignored) {}
        }

        String sortSelection = cmbSortBy.getValue();
        String sortBy = "transaction_date";
        String sortOrder = "DESC";
        
        if (sortSelection != null) {
            if (sortSelection.contains("Oldest")) sortOrder = "ASC";
            else if (sortSelection.contains("Amount")) {
                sortBy = "amount";
                sortOrder = sortSelection.contains("Highest") ? "DESC" : "ASC";
            } else if (sortSelection.contains("Description")) {
                sortBy = "description";
                sortOrder = "ASC";
            }
        }

        // Table visual state indicator
        tableTransactions.setPlaceholder(new Label("Loading data..."));

        final Double finalMinAmount = minAmount;
        final Double finalMaxAmount = maxAmount;
        final String finalSortBy = sortBy;
        final String finalSortOrder = sortOrder;

        AsyncUtil.runAsync(() -> {
            // Background Thread Fetch
            return transactionService.filterTransactions(
                    currentUserId, type, catId, from, to, keyword, finalMinAmount, finalMaxAmount, finalSortBy, finalSortOrder, offset, PAGE_SIZE + 1
            );
        }, filtered -> {
            // UI Thread Update
            boolean hasNext = filtered.size() > PAGE_SIZE;
            if (hasNext) {
                filtered.remove(filtered.size() - 1);
            }

            transactionList.setAll(filtered);
            
            btnNextPage.setDisable(!hasNext);
            btnPrevPage.setDisable(currentPage == 1);
            
            int startCount = filtered.isEmpty() ? 0 : offset + 1;
            int endCount = offset + filtered.size();
            lblPaginationInfo.setText("Showing " + startCount + "-" + endCount);
            
            if (filtered.isEmpty()) {
                tableTransactions.setPlaceholder(new Label("No transactions found."));
            }
            
        }, error -> {
            System.err.println("[ERROR-TXN] Failed to load transactions.");
            tableTransactions.setPlaceholder(new Label("Failed to load data."));
        });
    }

    @FXML private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadTransactions();
        }
    }

    @FXML private void handleNextPage() {
        currentPage++;
        loadTransactions();
    }

    @FXML private void handleClearFilter() {
        txtSearch.clear();
        txtMinAmount.clear();
        txtMaxAmount.clear();
        cmbFilterType.getSelectionModel().selectFirst();
        cmbFilterCategory.getSelectionModel().selectFirst();
        cmbSortBy.getSelectionModel().selectFirst();
        dpFilterFrom.setValue(null);
        dpFilterTo.setValue(null);
        currentPage = 1;
        loadTransactions();
    }

    @FXML private void handleTypeChange() {
        String type = cmbType.getValue();
        if (type != null) {
            List<Category> categories = categoryService.getCategoriesByUser(currentUserId);
            ObservableList<CategoryItem> filtered = FXCollections.observableArrayList();
            for (Category c : categories) {
                if (c.getType().equals(type)) {
                    filtered.add(new CategoryItem(c.getId(), c.getName(), c.getType()));
                }
            }
            cmbCategory.setItems(filtered);
        }
    }

    @FXML private void handlePrepareAdd() {
        currentEditTransaction = null;
        lblFormTitle.setText("New Transaction");
        clearForm();
        formCard.setVisible(true);
        formCard.setManaged(true);
    }

    private void handlePrepareEdit(Transaction selected) {
        if (selected == null) return;
        
        currentEditTransaction = selected;
        lblFormTitle.setText("Edit Transaction");
        
        txtDescription.setText(selected.getDescription());
        txtAmount.setText(String.valueOf(selected.getAmount()));
        cmbType.setValue(selected.getType());
        handleTypeChange();
        
        cmbCategory.getItems().stream()
                .filter(c -> c.id == selected.getCategoryId())
                .findFirst().ifPresent(cmbCategory.getSelectionModel()::select);
        
        cmbAccount.getItems().stream()
                .filter(a -> a.id == selected.getAccountId())
                .findFirst().ifPresent(cmbAccount.getSelectionModel()::select);
        
        dpDate.setValue(selected.getDate());
        
        formCard.setVisible(true);
        formCard.setManaged(true);
    }

    @FXML private void handleSave() {
        try {
            ValidationUtil.requireNotBlank(txtDescription.getText(), "Description");
            ValidationUtil.requireNotBlank(txtAmount.getText(), "Amount");
            ValidationUtil.requireNonNull(cmbType.getValue(), "Type");
            ValidationUtil.requireNonNull(cmbAccount.getValue(), "Account");
            ValidationUtil.requireNonNull(dpDate.getValue(), "Date");
            
            double amount = Double.parseDouble(txtAmount.getText().trim());
            ValidationUtil.requirePositive(amount, "Amount");

            int catId = cmbCategory.getValue() != null ? cmbCategory.getValue().id : -1;
            
            if (currentEditTransaction == null) {
                Transaction t = new Transaction(
                        currentUserId, cmbAccount.getValue().id, catId, 
                        amount, cmbType.getValue(), txtDescription.getText().trim(), dpDate.getValue()
                );
                transactionService.addTransaction(t);
                AlertUtil.showSuccess("Transaction saved successfully.");
            } else {
                currentEditTransaction.setAccountId(cmbAccount.getValue().id);
                currentEditTransaction.setCategoryId(catId);
                currentEditTransaction.setAmount(amount);
                currentEditTransaction.setType(cmbType.getValue());
                currentEditTransaction.setDescription(txtDescription.getText().trim());
                currentEditTransaction.setDate(dpDate.getValue());
                
                transactionService.updateTransaction(currentEditTransaction);
                AlertUtil.showSuccess("Transaction updated.");
            }

            handleCancel();
            loadTransactions(); 
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Amount must be a valid number.");
        } catch (ValidationException e) {
            AlertUtil.showWarning(e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("An error occurred: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() {
        clearForm();
        formCard.setVisible(false);
        formCard.setManaged(false);
        currentEditTransaction = null;
    }

    private void clearForm() {
        txtDescription.clear();
        txtAmount.clear();
        cmbType.getSelectionModel().clearSelection();
        cmbCategory.getSelectionModel().clearSelection();
        cmbAccount.getSelectionModel().clearSelection();
        dpDate.setValue(null);
    }

    private void handleDelete(Transaction selected) {
        if (selected == null) return;
        if (AlertUtil.showConfirm("Delete Transaction", "Are you sure you want to delete this transaction? This will impact your budget balances.")) {
            transactionService.deleteTransaction(selected.getId());
            loadTransactions();
        }
    }

    @FXML private void handleExport(ActionEvent event) {
        AlertUtil.showSuccess("Export functionality will be enabled soon.");
    }

    // Helper Wrappers for ComboBoxes
    private static class AccountItem {
        int id; String name;
        AccountItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private static class CategoryItem {
        int id; String name; String type;
        CategoryItem(int id, String name, String type) { this.id = id; this.name = name; this.type = type; }
        CategoryItem(int id, String name) { this(id, name, ""); }
        @Override public String toString() { return name; }
    }
}
