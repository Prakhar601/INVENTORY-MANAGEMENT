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
import com.fintrack.util.CurrencyUtil;
import com.fintrack.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionController {

    // Form Elements
    @FXML private VBox formCard;
    @FXML private Label lblFormTitle;
    @FXML private TextField txtDescription;
    @FXML private TextField txtAmount;
    @FXML private ComboBox<String> cmbType;
    @FXML private ComboBox<CategoryItem> cmbCategory;
    @FXML private ComboBox<AccountItem> cmbAccount;
    @FXML private DatePicker dpDate;

    // Filters
    @FXML private ComboBox<String> cmbFilterType;
    @FXML private ComboBox<CategoryItem> cmbFilterCategory;
    @FXML private DatePicker dpFilterFrom;
    @FXML private DatePicker dpFilterTo;

    // Table Elements
    @FXML private TableView<Transaction> tableTransactions;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colAccount;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colAmount;

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final Map<Integer, String> accountMap = new HashMap<>();
    private final Map<Integer, String> categoryMap = new HashMap<>();

    private Transaction currentEditTransaction = null;
    private int currentUserId;

    public TransactionController() {
        this.transactionService = new TransactionService();
        this.accountService = new AccountService();
        this.categoryService = new CategoryService();
    }

    @FXML
    public void initialize() {
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == -1) {
            SceneNavigator.navigateTo("login.fxml");
            return;
        }

        setupTable();
        loadDropdownData();
        loadTransactions();
    }

    private void setupTable() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colAmount.setCellValueFactory(data -> new SimpleStringProperty(CurrencyUtil.formatSimple(data.getValue().getAmount())));
        
        colAccount.setCellValueFactory(data -> {
            String name = accountMap.getOrDefault(data.getValue().getAccountId(), "Unknown");
            return new SimpleStringProperty(name);
        });

        colCategory.setCellValueFactory(data -> {
            String name = categoryMap.getOrDefault(data.getValue().getCategoryId(), "None");
            return new SimpleStringProperty(name);
        });

        tableTransactions.setItems(transactionList);
    }

    private void loadDropdownData() {
        // Types
        cmbType.setItems(FXCollections.observableArrayList("EXPENSE", "INCOME", "TRANSFER"));
        cmbFilterType.setItems(FXCollections.observableArrayList("All", "EXPENSE", "INCOME", "TRANSFER"));
        cmbFilterType.getSelectionModel().selectFirst();

        // Accounts
        List<Account> accounts = accountService.getAccountsByUser(currentUserId);
        ObservableList<AccountItem> accountItems = FXCollections.observableArrayList();
        for (Account a : accounts) {
            accountMap.put(a.getId(), a.getName());
            accountItems.add(new AccountItem(a.getId(), a.getName()));
        }
        cmbAccount.setItems(accountItems);

        // Categories
        List<Category> categories = categoryService.getCategoriesByUser(currentUserId);
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
    }

    private void loadTransactions() {
        transactionList.clear();
        transactionList.addAll(transactionService.getTransactionsByUser(currentUserId));
    }

    @FXML
    private void handleTypeChange() {
        String type = cmbType.getValue();
        if (type != null) {
            // Filter category dropdown based on selected type
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

    @FXML
    private void handlePrepareAdd() {
        currentEditTransaction = null;
        lblFormTitle.setText("New Transaction");
        clearForm();
        formCard.setVisible(true);
        formCard.setManaged(true);
    }

    @FXML
    private void handlePrepareEdit() {
        Transaction selected = tableTransactions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Please select a transaction to edit.");
            return;
        }

        currentEditTransaction = selected;
        lblFormTitle.setText("Edit Transaction");
        
        txtDescription.setText(selected.getDescription());
        txtAmount.setText(String.valueOf(selected.getAmount()));
        cmbType.setValue(selected.getType());
        handleTypeChange(); // Filter categories
        
        // Select matching Category
        cmbCategory.getItems().stream()
                .filter(c -> c.id == selected.getCategoryId())
                .findFirst().ifPresent(cmbCategory.getSelectionModel()::select);
        
        // Select matching Account
        cmbAccount.getItems().stream()
                .filter(a -> a.id == selected.getAccountId())
                .findFirst().ifPresent(cmbAccount.getSelectionModel()::select);
        
        dpDate.setValue(selected.getDate());
        
        formCard.setVisible(true);
        formCard.setManaged(true);
    }

    @FXML
    private void handleSave() {
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
                // Add
                Transaction t = new Transaction(
                        currentUserId, 
                        cmbAccount.getValue().id, 
                        catId, 
                        amount, 
                        cmbType.getValue(), 
                        txtDescription.getText().trim(), 
                        dpDate.getValue()
                );
                transactionService.addTransaction(t);
                AlertUtil.showSuccess("Transaction added.");
            } else {
                // Edit
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
            handleFilter(); // Reload current view
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Amount must be a valid number.");
        } catch (ValidationException e) {
            AlertUtil.showWarning(e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
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

    @FXML
    private void handleDelete() {
        Transaction selected = tableTransactions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Please select a transaction to delete.");
            return;
        }

        if (AlertUtil.showConfirm("Delete Transaction", "Are you sure you want to delete this transaction?")) {
            transactionService.deleteTransaction(selected.getId());
            handleFilter(); // Reload
        }
    }

    @FXML
    private void handleFilter() {
        List<Transaction> filtered;
        
        LocalDate from = dpFilterFrom.getValue();
        LocalDate to = dpFilterTo.getValue();
        
        if (from != null && to != null) {
            filtered = transactionService.getTransactionsByDateRange(currentUserId, from, to);
        } else {
            filtered = transactionService.getTransactionsByUser(currentUserId);
        }

        String typeFilter = cmbFilterType.getValue();
        if (typeFilter != null && !typeFilter.equals("All")) {
            filtered = filtered.stream().filter(t -> t.getType().equals(typeFilter)).collect(Collectors.toList());
        }

        CategoryItem catFilter = cmbFilterCategory.getValue();
        if (catFilter != null && catFilter.id != -1) {
            filtered = filtered.stream().filter(t -> t.getCategoryId() == catFilter.id).collect(Collectors.toList());
        }

        transactionList.setAll(filtered);
    }

    @FXML
    private void handleClearFilter() {
        cmbFilterType.getSelectionModel().selectFirst();
        cmbFilterCategory.getSelectionModel().selectFirst();
        dpFilterFrom.setValue(null);
        dpFilterTo.setValue(null);
        loadTransactions();
    }

    // Navigation Methods
    @FXML private void navDashboard(ActionEvent event) { SceneNavigator.navigateTo("dashboard.fxml"); }
    @FXML private void navTransactions(ActionEvent event) { SceneNavigator.navigateTo("transactions.fxml"); }
    @FXML private void navAccounts(ActionEvent event) { SceneNavigator.navigateTo("accounts.fxml"); }
    @FXML private void navBudgets(ActionEvent event) { SceneNavigator.navigateTo("budgets.fxml"); }
    @FXML private void navReports(ActionEvent event) { SceneNavigator.navigateTo("reports.fxml"); }
    @FXML private void navSettings(ActionEvent event) { SceneNavigator.navigateTo("settings.fxml"); }
    
    @FXML 
    private void handleLogout(ActionEvent event) { 
        SessionManager.getInstance().logout(); 
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
