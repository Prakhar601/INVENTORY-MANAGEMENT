package com.fintrack.controller;

import com.fintrack.exception.ValidationException;
import com.fintrack.model.Budget;
import com.fintrack.model.Category;
import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.BudgetService;
import com.fintrack.service.CategoryService;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetController {

    @FXML private SidebarController sidebarController;

    // Table Elements
    @FXML private TableView<Budget> tableBudgets;
    @FXML private TableColumn<Budget, String> colCategory;
    @FXML private TableColumn<Budget, String> colPeriod;
    @FXML private TableColumn<Budget, String> colAmount;
    @FXML private TableColumn<Budget, String> colSpent;
    @FXML private TableColumn<Budget, Void> colProgress;
    @FXML private TableColumn<Budget, Void> colActions;

    // Metrics
    @FXML private Label lblTotalAllocated;
    @FXML private Label lblAtRisk;

    // Form Elements
    @FXML private VBox formCard;
    @FXML private Label lblFormTitle;
    @FXML private ComboBox<CategoryItem> cmbCategory;
    @FXML private TextField txtAmount;
    @FXML private ComboBox<String> cmbPeriod;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Label lblCurrencySymbol;

    private BudgetService budgetService;
    private CategoryService categoryService;
    
    private final ObservableList<Budget> budgetList = FXCollections.observableArrayList();
    private final Map<Integer, String> categoryMap = new HashMap<>();

    private Budget currentEditBudget = null;
    private int currentUserId;

    public BudgetController() {}

    @FXML
    public void initialize() {
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == -1) {
            Platform.runLater(() -> SceneNavigator.navigateTo("login.fxml"));
            return;
        }

        if (sidebarController != null) {
            sidebarController.setActive("budgets");
        }

        try {
            budgetService = new BudgetService();
            categoryService = new CategoryService();
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
            loadBudgets();
        }, error -> AlertUtil.showError("Failed to initialize budgets view."));
    }

    private void setupTable() {
        colCategory.setCellValueFactory(data -> 
            new SimpleStringProperty(categoryMap.getOrDefault(data.getValue().getCategoryId(), "Unknown"))
        );
        colPeriod.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPeriod()));
        
        colAmount.setCellValueFactory(data -> 
            new SimpleStringProperty(CurrencyUtil.formatSimple(data.getValue().getAmount()))
        );

        // Needs to call DB per row for spent amount, this is ok for a small dataset in a desktop app
        colSpent.setCellValueFactory(data -> {
            double spent = budgetService.getSpentAmount(data.getValue());
            return new SimpleStringProperty(CurrencyUtil.formatSimple(spent));
        });

        setupProgressColumn();
        setupActionColumn();
        tableBudgets.setItems(budgetList);
    }

    private void setupProgressColumn() {
        colProgress.setCellFactory(param -> new TableCell<>() {
            private final Region track = new Region();
            private final Region fill = new Region();
            private final StackPane pane = new StackPane(track, fill);

            {
                track.getStyleClass().add("budget-bar-track");
                track.setMaxWidth(Double.MAX_VALUE);
                
                fill.getStyleClass().add("budget-bar-fill");
                pane.setAlignment(Pos.CENTER_LEFT);
                pane.setPrefHeight(20);
                pane.setStyle("-fx-padding: 0 10px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Budget b = (Budget) getTableRow().getItem();
                    double percentage = budgetService.getSpendingPercentage(b);
                    
                    double fillWidth = Math.min(percentage, 100.0);
                    fill.prefWidthProperty().bind(track.widthProperty().multiply(fillWidth / 100.0));

                    fill.getStyleClass().removeAll("warning", "danger");
                    if (percentage >= 100) {
                        fill.getStyleClass().add("danger");
                    } else if (percentage >= 85) {
                        fill.getStyleClass().add("warning");
                    }

                    setGraphic(pane);
                }
            }
        });
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
        List<Category> categories = categoryService.getCategoriesByUser(currentUserId);
        
        Platform.runLater(() -> {
            cmbPeriod.setItems(FXCollections.observableArrayList("Monthly", "Weekly", "Yearly"));
            cmbPeriod.getSelectionModel().selectFirst();

            ObservableList<CategoryItem> categoryItems = FXCollections.observableArrayList();
            for (Category c : categories) {
                if (c.getType().equals("EXPENSE")) {
                    categoryMap.put(c.getId(), c.getName());
                    categoryItems.add(new CategoryItem(c.getId(), c.getName()));
                }
            }
            cmbCategory.setItems(categoryItems);
        });
    }

    private void loadBudgets() {
        tableBudgets.setPlaceholder(new Label("Loading budgets..."));
        
        AsyncUtil.runAsync(() -> {
            BudgetData data = new BudgetData();
            data.budgets = budgetService.getActiveBudgets(currentUserId);
            
            for (Budget b : data.budgets) {
                data.totalAllocated += b.getAmount();
                if (budgetService.getSpendingPercentage(b) >= 85) {
                    data.riskCount++;
                }
            }
            return data;
            
        }, data -> {
            budgetList.setAll(data.budgets);
            lblTotalAllocated.setText(CurrencyUtil.formatSimple(data.totalAllocated));
            lblAtRisk.setText(String.valueOf(data.riskCount));
            
            if (data.riskCount > 0) {
                lblAtRisk.setStyle("-fx-text-fill: #EF4444;");
            } else {
                lblAtRisk.setStyle("-fx-text-fill: -fx-text-primary;");
            }

            if (data.budgets.isEmpty()) {
                tableBudgets.setPlaceholder(new Label("No active budgets. Create one to start tracking."));
            }
            
            tableBudgets.refresh(); // Ensure progress bars recalculate
            
        }, error -> {
            System.err.println("[ERROR-BGT] Failed to load budgets.");
            tableBudgets.setPlaceholder(new Label("Failed to load budgets."));
        });
    }

    private static class BudgetData {
        List<Budget> budgets;
        double totalAllocated = 0;
        int riskCount = 0;
    }

    @FXML private void handlePrepareAdd() {
        currentEditBudget = null;
        lblFormTitle.setText("New Budget");
        clearForm();
        formCard.setVisible(true);
        formCard.setManaged(true);
        
        // Default to current month
        LocalDate now = LocalDate.now();
        dpStart.setValue(now.withDayOfMonth(1));
        dpEnd.setValue(now.withDayOfMonth(now.lengthOfMonth()));
    }

    private void handlePrepareEdit(Budget selected) {
        if (selected == null) return;
        
        currentEditBudget = selected;
        lblFormTitle.setText("Edit Budget");
        
        txtAmount.setText(String.valueOf(selected.getAmount()));
        cmbPeriod.setValue(selected.getPeriod());
        
        cmbCategory.getItems().stream()
                .filter(c -> c.id == selected.getCategoryId())
                .findFirst().ifPresent(cmbCategory.getSelectionModel()::select);
        
        dpStart.setValue(selected.getStartDate());
        dpEnd.setValue(selected.getEndDate());
        
        formCard.setVisible(true);
        formCard.setManaged(true);
    }

    @FXML private void handleSave() {
        try {
            ValidationUtil.requireNonNull(cmbCategory.getValue(), "Category");
            ValidationUtil.requireNotBlank(txtAmount.getText(), "Amount");
            ValidationUtil.requireNonNull(cmbPeriod.getValue(), "Period");
            ValidationUtil.requireNonNull(dpStart.getValue(), "Start Date");
            ValidationUtil.requireNonNull(dpEnd.getValue(), "End Date");
            
            double amount = Double.parseDouble(txtAmount.getText().trim());

            if (currentEditBudget == null) {
                Budget b = new Budget();
                b.setUserId(currentUserId);
                b.setCategoryId(cmbCategory.getValue().id);
                b.setAmount(amount);
                b.setPeriod(cmbPeriod.getValue());
                b.setStartDate(dpStart.getValue());
                b.setEndDate(dpEnd.getValue());
                
                budgetService.createBudget(b);
                AlertUtil.showSuccess("Budget created successfully.");
            } else {
                currentEditBudget.setCategoryId(cmbCategory.getValue().id);
                currentEditBudget.setAmount(amount);
                currentEditBudget.setPeriod(cmbPeriod.getValue());
                currentEditBudget.setStartDate(dpStart.getValue());
                currentEditBudget.setEndDate(dpEnd.getValue());
                
                budgetService.updateBudget(currentEditBudget);
                AlertUtil.showSuccess("Budget updated.");
            }

            handleCancel();
            loadBudgets();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Budget amount must be a valid number.");
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
        currentEditBudget = null;
    }

    private void clearForm() {
        cmbCategory.getSelectionModel().clearSelection();
        txtAmount.clear();
        cmbPeriod.getSelectionModel().clearSelection();
        dpStart.setValue(null);
        dpEnd.setValue(null);
    }

    private void handleDelete(Budget selected) {
        if (selected == null) return;
        if (AlertUtil.showConfirm("Delete Budget", "Are you sure you want to delete this budget?")) {
            budgetService.deleteBudget(selected.getId());
            loadBudgets();
        }
    }

    private static class CategoryItem {
        int id; String name;
        CategoryItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
