package com.fintrack.controller;

import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.model.User;
import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.AccountService;
import com.fintrack.service.CategoryService;
import com.fintrack.service.TransactionService;
import com.fintrack.session.SessionManager;
import com.fintrack.util.AlertUtil;
import com.fintrack.util.AsyncUtil;
import com.fintrack.util.CurrencyUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardController {

    // ── UI Elements ────────────────────────────────────────────────────────
    
    @FXML private SidebarController sidebarController;

    @FXML private Label lblWelcome;
    @FXML private Label lblDateTime;
    @FXML private Label lblTotalBalance;
    @FXML private Label lblMonthlyIncome;
    @FXML private Label lblMonthlyExpense;
    @FXML private Label lblNetSavings;

    @FXML private BarChart<String, Number> barChartCashFlow;
    @FXML private PieChart pieChartSpending;

    @FXML private TableView<Transaction> tableRecent;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colDesc;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, String> colStatus;
    
    @FXML private Label lblTopCategoryInsight;

    // ── Services ───────────────────────────────────────────────────────────

    private AccountService accountService;
    private TransactionService transactionService;
    private CategoryService categoryService;
    private User currentUser;

    public DashboardController() {}

    @FXML
    public void initialize() {
        if (!validateFXMLInjections()) {
            System.err.println("[FATAL-DASH] FXML Injection failed! Mismatched fx:id between FXML and Controller.");
            return;
        }

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> SceneNavigator.navigateTo("login.fxml"));
            return;
        }
        
        lblWelcome.setText("Welcome back, " + currentUser.getUsername());
        lblDateTime.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));

        try {
            accountService = new AccountService();
            transactionService = new TransactionService();
            categoryService = new CategoryService();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return;
        }

        if (sidebarController != null) {
            sidebarController.setActive("dashboard");
        }

        setupTable();
        loadDashboardAsync();
    }

    private boolean validateFXMLInjections() {
        return lblTotalBalance != null && barChartCashFlow != null && pieChartSpending != null && tableRecent != null;
    }

    private void setupTable() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        colDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        
        colCategory.setCellValueFactory(data -> {
            try {
                int catId = data.getValue().getCategoryId();
                if (catId > 0) {
                    Category cat = categoryService.getCategoryById(catId);
                    return new SimpleStringProperty(cat != null ? cat.getName() : "None");
                }
            } catch (Exception ignored) {}
            return new SimpleStringProperty("None");
        });

        colAmount.setCellValueFactory(data -> {
            Transaction t = data.getValue();
            String formatted = CurrencyUtil.formatSimple(t.getAmount());
            return new SimpleStringProperty(t.getType().equals("EXPENSE") ? "-" + formatted : "+" + formatted);
        });
        
        colStatus.setCellValueFactory(data -> new SimpleStringProperty("Completed"));
    }
    
    private void loadDashboardAsync() {
        tableRecent.setPlaceholder(new Label("Loading recent activity..."));
        
        AsyncUtil.runAsync(() -> {
            // Background Data Fetch
            DashboardData data = new DashboardData();
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

            data.totalBalance = accountService.getTotalBalance(currentUser.getId());
            data.monthlyIncome = transactionService.getTotalIncome(currentUser.getId(), startOfMonth, endOfMonth);
            data.monthlyExpense = transactionService.getTotalExpense(currentUser.getId(), startOfMonth, endOfMonth);
            
            data.recentTransactions = transactionService.getTransactionsByUser(currentUser.getId())
                    .stream()
                    .sorted(Comparator.comparing(Transaction::getDate).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            // Build Cash Flow Chart Data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
            data.incomeSeries = new XYChart.Series<>();
            data.incomeSeries.setName("Income");
            data.expenseSeries = new XYChart.Series<>();
            data.expenseSeries.setName("Expense");

            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = now.minusMonths(i);
                LocalDate start = monthDate.withDayOfMonth(1);
                LocalDate end = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
                
                double inc = transactionService.getTotalIncome(currentUser.getId(), start, end);
                double exp = transactionService.getTotalExpense(currentUser.getId(), start, end);
                
                data.incomeSeries.getData().add(new XYChart.Data<>(monthDate.format(formatter), inc));
                data.expenseSeries.getData().add(new XYChart.Data<>(monthDate.format(formatter), exp));
            }

            // Build Pie Chart Data
            List<Transaction> monthTxns = transactionService.getTransactionsByDateRange(currentUser.getId(), startOfMonth, endOfMonth);
            Map<Integer, Double> expensesByCategory = monthTxns.stream()
                    .filter(t -> t.getType().equals("EXPENSE") && t.getCategoryId() > 0)
                    .collect(Collectors.groupingBy(Transaction::getCategoryId, Collectors.summingDouble(Transaction::getAmount)));

            data.pieData = FXCollections.observableArrayList();
            data.topCategoryName = "None";
            data.topCategoryAmount = 0.0;

            for (Map.Entry<Integer, Double> entry : expensesByCategory.entrySet()) {
                Category cat = categoryService.getCategoryById(entry.getKey());
                if (cat != null && entry.getValue() > 0) {
                    data.pieData.add(new PieChart.Data(cat.getName(), entry.getValue()));
                    if (entry.getValue() > data.topCategoryAmount) {
                        data.topCategoryAmount = entry.getValue();
                        data.topCategoryName = cat.getName();
                    }
                }
            }
            
            return data;
            
        }, data -> {
            // Update UI Thread
            lblTotalBalance.setText(CurrencyUtil.formatSimple(data.totalBalance));
            lblMonthlyIncome.setText("+" + CurrencyUtil.formatSimple(data.monthlyIncome));
            lblMonthlyExpense.setText("-" + CurrencyUtil.formatSimple(data.monthlyExpense));
            lblNetSavings.setText(CurrencyUtil.formatSimple(data.monthlyIncome - data.monthlyExpense));

            barChartCashFlow.getData().clear();
            barChartCashFlow.getData().addAll(data.incomeSeries, data.expenseSeries);
            
            pieChartSpending.setData(data.pieData);
            if (data.topCategoryAmount > 0) {
                lblTopCategoryInsight.setText(data.topCategoryName + " (" + CurrencyUtil.formatSimple(data.topCategoryAmount) + ")");
            } else {
                lblTopCategoryInsight.setText("No spending yet this month.");
            }

            tableRecent.setItems(FXCollections.observableArrayList(data.recentTransactions));
            if (data.recentTransactions.isEmpty()) {
                tableRecent.setPlaceholder(new Label("No recent transactions."));
            }
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), lblTotalBalance.getParent());
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
            
        }, error -> {
            System.err.println("[ERROR-DASH] Failed to load dashboard async.");
            error.printStackTrace();
        });
    }

    // Helper class to ferry data between background thread and UI thread
    private static class DashboardData {
        double totalBalance;
        double monthlyIncome;
        double monthlyExpense;
        List<Transaction> recentTransactions;
        XYChart.Series<String, Number> incomeSeries;
        XYChart.Series<String, Number> expenseSeries;
        ObservableList<PieChart.Data> pieData;
        String topCategoryName;
        double topCategoryAmount;
    }

    // ── Action Handlers ─────────────────────────────────────────────────────

    @FXML private void navTransactions(ActionEvent event) { SceneNavigator.navigateTo("transactions.fxml"); }

    @FXML private void handleAddTransaction(ActionEvent event) {
        SceneNavigator.navigateTo("transactions.fxml");
    }

    @FXML private void handleAddBudget(ActionEvent event) {
        SceneNavigator.navigateTo("budgets.fxml");
    }

    @FXML private void handleExport(ActionEvent event) {
        SceneNavigator.navigateTo("reports.fxml");
    }
}
