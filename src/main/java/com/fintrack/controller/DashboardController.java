package com.fintrack.controller;

import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.model.User;
import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.AccountService;
import com.fintrack.service.CategoryService;
import com.fintrack.service.TransactionService;
import com.fintrack.session.SessionManager;
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
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    // ── UI Elements ────────────────────────────────────────────────────────

    @FXML private Label lblWelcome;
    @FXML private Label lblTotalBalance;
    @FXML private Label lblMonthlyIncome;
    @FXML private Label lblMonthlyExpense;
    @FXML private Label lblNetSavings;

    @FXML private Label lblBalanceError;
    @FXML private Label lblIncomeError;
    @FXML private Label lblExpenseError;

    @FXML private BarChart<String, Number> barChartCashFlow;
    @FXML private Label lblBarChartError;

    @FXML private PieChart pieChartSpending;
    @FXML private Label lblPieChartError;

    @FXML private TableView<Transaction> tableRecent;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colDesc;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private Label lblTableError;

    // ── Services ───────────────────────────────────────────────────────────

    private AccountService accountService;
    private TransactionService transactionService;
    private CategoryService categoryService;
    private User currentUser;

    /**
     * Required no-arg constructor for FXMLLoader.
     * We explicitly declare it to track exact instantiation timing.
     */
    public DashboardController() {
        System.out.println("[DEBUG-DASH-LIFECYCLE] 1. DashboardController constructor called (Instantiated by FXMLLoader)");
    }

    /**
     * Called automatically by FXMLLoader after all @FXML fields are injected.
     */
    @FXML
    public void initialize() {
        System.out.println("[DEBUG-DASH-LIFECYCLE] 2. initialize() called. @FXML fields injected.");
        
        // 1. Hard Validation of FXML Injections
        if (!validateFXMLInjections()) {
            System.err.println("[FATAL-DASH] FXML Injection failed! Mismatched fx:id between FXML and Controller.");
            return;
        }

        // 2. Validate Session State
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("[FATAL-DASH] SessionManager.getCurrentUser() is NULL! Redirecting to login.");
            Platform.runLater(() -> SceneNavigator.navigateTo("login.fxml"));
            return;
        }
        
        lblWelcome.setText("Welcome back, " + currentUser.getUsername() + "!");

        // 3. Initialize Services Defensively
        // This MUST not hit the database in the constructor of the services!
        try {
            System.out.println("[DEBUG-DASH-LIFECYCLE] 3. Initializing Services...");
            accountService = new AccountService();
            transactionService = new TransactionService();
            categoryService = new CategoryService();
        } catch (Exception e) {
            System.err.println("[FATAL-DASH] Service initialization crashed (Check DAO constructors!)");
            e.printStackTrace(System.err);
            return;
        }

        // 4. Detach UI rendering from Data Loading
        System.out.println("[DEBUG-DASH-LIFECYCLE] 4. Offloading Data Fetching to Platform.runLater...");
        Platform.runLater(() -> {
            System.out.println("[DEBUG-DASH-LIFECYCLE] 5. Starting Isolated Widget Loading...");
            setupTableSafely();
            loadSummaryCardsSafely();
            loadPieChartSafely();
            loadBarChartSafely();
            loadRecentTransactionsSafely();
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), lblTotalBalance.getParent());
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
            System.out.println("[DEBUG-DASH-LIFECYCLE] 6. Dashboard Fully Rendered!");
        });
    }

    private boolean validateFXMLInjections() {
        boolean valid = true;
        if (lblTotalBalance == null) { System.err.println("lblTotalBalance is null"); valid = false; }
        if (barChartCashFlow == null) { System.err.println("barChartCashFlow is null"); valid = false; }
        if (pieChartSpending == null) { System.err.println("pieChartSpending is null"); valid = false; }
        if (tableRecent == null) { System.err.println("tableRecent is null"); valid = false; }
        if (colDate == null) { System.err.println("colDate is null"); valid = false; }
        return valid;
    }

    private void setupTableSafely() {
        try {
            colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
            colDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
            
            colCategory.setCellValueFactory(data -> {
                try {
                    int catId = data.getValue().getCategoryId();
                    if (catId > 0) {
                        Category cat = categoryService.getCategoryById(catId);
                        return new SimpleStringProperty(cat != null ? cat.getName() : "None");
                    }
                } catch (Exception e) {
                    System.err.println("[WARN-DASH] Failed to load category name for row.");
                }
                return new SimpleStringProperty("None");
            });

            colAmount.setCellValueFactory(data -> {
                Transaction t = data.getValue();
                String formatted = CurrencyUtil.formatSimple(t.getAmount());
                return new SimpleStringProperty(t.getType().equals("EXPENSE") ? "-" + formatted : "+" + formatted);
            });
        } catch (Exception e) {
            System.err.println("[ERROR-DASH] setupTableSafely crashed!");
            e.printStackTrace(System.err);
        }
    }

    private void loadSummaryCardsSafely() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        double netIncome = 0;
        double netExpense = 0;

        try {
            double totalBalance = accountService.getTotalBalance(currentUser.getId());
            lblTotalBalance.setText(CurrencyUtil.formatSimple(totalBalance));
        } catch (Exception e) {
            System.err.println("[ERROR-DASH] Total Balance failed to load.");
            lblBalanceError.setVisible(true);
            lblBalanceError.setManaged(true);
        }

        try {
            netIncome = transactionService.getTotalIncome(currentUser.getId(), startOfMonth, endOfMonth);
            lblMonthlyIncome.setText("+" + CurrencyUtil.formatSimple(netIncome));
        } catch (Exception e) {
            System.err.println("[ERROR-DASH] Monthly Income failed to load.");
            lblIncomeError.setVisible(true);
            lblIncomeError.setManaged(true);
        }

        try {
            netExpense = transactionService.getTotalExpense(currentUser.getId(), startOfMonth, endOfMonth);
            lblMonthlyExpense.setText("-" + CurrencyUtil.formatSimple(netExpense));
        } catch (Exception e) {
            System.err.println("[ERROR-DASH] Monthly Expense failed to load.");
            lblExpenseError.setVisible(true);
            lblExpenseError.setManaged(true);
        }

        double netSavings = netIncome - netExpense;
        lblNetSavings.setText(CurrencyUtil.formatSimple(netSavings));
        lblNetSavings.setStyle(netSavings < 0 ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #4F46E5;");
    }

    private void loadPieChartSafely() {
        try {
            LocalDate now = LocalDate.now();
            List<Transaction> monthTxns = transactionService.getTransactionsByDateRange(
                    currentUser.getId(), now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()));

            Map<Integer, Double> expensesByCategory = monthTxns.stream()
                    .filter(t -> t.getType().equals("EXPENSE") && t.getCategoryId() > 0)
                    .collect(Collectors.groupingBy(
                            Transaction::getCategoryId,
                            Collectors.summingDouble(Transaction::getAmount)
                    ));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<Integer, Double> entry : expensesByCategory.entrySet()) {
                Category cat = categoryService.getCategoryById(entry.getKey());
                if (cat != null && entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(cat.getName(), entry.getValue()));
                }
            }
            pieChartSpending.setData(pieData);
        } catch (Exception e) {
            System.err.println("[ERROR-DASH] Pie Chart failed to load.");
            pieChartSpending.setVisible(false);
            pieChartSpending.setManaged(false);
            lblPieChartError.setVisible(true);
            lblPieChartError.setManaged(true);
        }
    }

    private void loadBarChartSafely() {
        try {
            barChartCashFlow.getData().clear();
            XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
            incomeSeries.setName("Income");
            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Expense");

            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");

            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = now.minusMonths(i);
                LocalDate start = monthDate.withDayOfMonth(1);
                LocalDate end = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
                
                double inc = transactionService.getTotalIncome(currentUser.getId(), start, end);
                double exp = transactionService.getTotalExpense(currentUser.getId(), start, end);
                
                incomeSeries.getData().add(new XYChart.Data<>(monthDate.format(formatter), inc));
                expenseSeries.getData().add(new XYChart.Data<>(monthDate.format(formatter), exp));
            }
            barChartCashFlow.getData().addAll(incomeSeries, expenseSeries);
        } catch (Exception e) {
            System.err.println("[ERROR-DASH] Bar Chart failed to load.");
            barChartCashFlow.setVisible(false);
            barChartCashFlow.setManaged(false);
            lblBarChartError.setVisible(true);
            lblBarChartError.setManaged(true);
        }
    }

    private void loadRecentTransactionsSafely() {
        try {
            List<Transaction> recent = transactionService.getTransactionsByUser(currentUser.getId())
                    .stream().limit(5).collect(Collectors.toList());
            tableRecent.setItems(FXCollections.observableArrayList(recent));
        } catch (Exception e) {
            System.err.println("[ERROR-DASH] Recent Transactions failed to load.");
            tableRecent.setVisible(false);
            tableRecent.setManaged(false);
            lblTableError.setVisible(true);
            lblTableError.setManaged(true);
        }
    }

    @FXML private void navDashboard(ActionEvent event) { SceneNavigator.navigateTo("dashboard.fxml"); }
    @FXML private void navTransactions(ActionEvent event) { SceneNavigator.navigateTo("transactions.fxml"); }
    @FXML private void navAccounts(ActionEvent event) { SceneNavigator.navigateTo("accounts.fxml"); }
    @FXML private void navBudgets(ActionEvent event) { SceneNavigator.navigateTo("budgets.fxml"); }
    @FXML private void navReports(ActionEvent event) { SceneNavigator.navigateTo("reports.fxml"); }
    @FXML private void navSettings(ActionEvent event) { SceneNavigator.navigateTo("settings.fxml"); }
    @FXML private void handleLogout(ActionEvent event) { SessionManager.getInstance().logout(); }
}
