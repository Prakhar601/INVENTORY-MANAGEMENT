package com.fintrack.controller;

import com.fintrack.exception.ValidationException;
import com.fintrack.model.Account;
import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.AccountService;
import com.fintrack.session.SessionManager;
import com.fintrack.util.AlertUtil;
import com.fintrack.util.AsyncUtil;
import com.fintrack.util.CurrencyUtil;
import com.fintrack.util.ValidationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class AccountController {

    @FXML private SidebarController sidebarController;

    // Table Elements
    @FXML private TableView<Account> tableAccounts;
    @FXML private TableColumn<Account, String> colName;
    @FXML private TableColumn<Account, String> colType;
    @FXML private TableColumn<Account, String> colBalance;
    @FXML private TableColumn<Account, Void> colActions;

    // Metrics
    @FXML private Label lblTotalNetWorth;

    // Form Elements
    @FXML private VBox formCard;
    @FXML private Label lblFormTitle;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtBalance;
    @FXML private Label lblCurrencySymbol;

    private AccountService accountService;
    private final ObservableList<Account> accountList = FXCollections.observableArrayList();
    private Account currentEditAccount = null;
    private int currentUserId;

    public AccountController() {}

    @FXML
    public void initialize() {
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == -1) {
            Platform.runLater(() -> SceneNavigator.navigateTo("login.fxml"));
            return;
        }

        if (sidebarController != null) {
            sidebarController.setActive("accounts");
        }

        try {
            accountService = new AccountService();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        setupTable();

        // Set currency symbol safely from Java (not FXML) to avoid expression parsing
        if (lblCurrencySymbol != null) {
            lblCurrencySymbol.setText(CurrencyUtil.getSymbol());
        }
        
        AsyncUtil.runAsync(() -> {
            loadDropdownDataSync();
            return null;
        }, result -> {
            loadAccounts();
        }, error -> AlertUtil.showError("Failed to initialize accounts view."));
    }

    private void setupTable() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        
        colBalance.setCellValueFactory(data -> 
            new SimpleStringProperty(CurrencyUtil.formatSimple(data.getValue().getBalance()))
        );

        setupActionColumn();
        tableAccounts.setItems(accountList);
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
        Platform.runLater(() -> {
            cmbType.setItems(FXCollections.observableArrayList("Checking", "Savings", "Credit Card", "Cash", "Investment"));
        });
    }

    private void loadAccounts() {
        tableAccounts.setPlaceholder(new Label("Loading accounts..."));
        
        AsyncUtil.runAsync(() -> {
            AccountData data = new AccountData();
            data.accounts = accountService.getAccountsByUser(currentUserId);
            data.netWorth = accountService.getTotalBalance(currentUserId);
            return data;
            
        }, data -> {
            accountList.setAll(data.accounts);
            lblTotalNetWorth.setText(CurrencyUtil.formatSimple(data.netWorth));
            
            if (data.accounts.isEmpty()) {
                tableAccounts.setPlaceholder(new Label("No accounts found. Add one to get started."));
            }
        }, error -> {
            System.err.println("[ERROR-ACC] Failed to load accounts.");
            tableAccounts.setPlaceholder(new Label("Failed to load accounts."));
        });
    }

    private static class AccountData {
        List<Account> accounts;
        double netWorth;
    }

    @FXML private void handlePrepareAdd() {
        currentEditAccount = null;
        lblFormTitle.setText("New Account");
        clearForm();
        formCard.setVisible(true);
        formCard.setManaged(true);
    }

    private void handlePrepareEdit(Account selected) {
        if (selected == null) return;
        
        currentEditAccount = selected;
        lblFormTitle.setText("Edit Account");
        
        txtName.setText(selected.getName());
        cmbType.setValue(selected.getType());
        txtBalance.setText(String.valueOf(selected.getBalance()));
        
        formCard.setVisible(true);
        formCard.setManaged(true);
    }

    @FXML private void handleSave() {
        try {
            ValidationUtil.requireNotBlank(txtName.getText(), "Account Name");
            ValidationUtil.requireNonNull(cmbType.getValue(), "Account Type");
            ValidationUtil.requireNotBlank(txtBalance.getText(), "Starting Balance");
            
            double balance = Double.parseDouble(txtBalance.getText().trim());

            if (currentEditAccount == null) {
                Account a = new Account(currentUserId, txtName.getText().trim(), cmbType.getValue());
                a.setBalance(balance);
                accountService.createAccount(a);
                AlertUtil.showSuccess("Account created successfully.");
            } else {
                currentEditAccount.setName(txtName.getText().trim());
                currentEditAccount.setType(cmbType.getValue());
                currentEditAccount.setBalance(balance);
                
                accountService.updateAccount(currentEditAccount);
                AlertUtil.showSuccess("Account updated.");
            }

            handleCancel();
            loadAccounts();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Balance must be a valid number.");
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
        currentEditAccount = null;
    }

    private void clearForm() {
        txtName.clear();
        cmbType.getSelectionModel().clearSelection();
        txtBalance.clear();
    }

    private void handleDelete(Account selected) {
        if (selected == null) return;
        if (AlertUtil.showConfirm("Delete Account", "Are you sure you want to delete this account? All associated transactions will be affected.")) {
            accountService.deleteAccount(selected.getId());
            loadAccounts();
        }
    }
}
