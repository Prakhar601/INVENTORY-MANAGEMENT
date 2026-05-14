package com.fintrack.controller;

import com.fintrack.model.Budget;
import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.model.User;
import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.BudgetService;
import com.fintrack.service.CategoryService;
import com.fintrack.service.TransactionService;
import com.fintrack.service.CsvExportService;
import com.fintrack.service.PdfExportService;
import com.fintrack.session.SessionManager;
import com.fintrack.util.AlertUtil;
import com.fintrack.util.AsyncUtil;
import com.fintrack.util.ChartUtil;
import com.fintrack.util.CurrencyUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportController {

    @FXML private SidebarController sidebarController;

    // Summary Widgets
    @FXML private Label lblAvgSpending;
    @FXML private Label lblAvgSpendingTrend;
    @FXML private Label lblSavingsRate;
    @FXML private Label lblSavingsRateTrend;
    @FXML private Label lblActiveBudgets;
    @FXML private Label lblExceededBudgets;
    @FXML private Label lblBudgetHealth;
    @FXML private Label lblTopCategory;
    @FXML private Label lblTopCategoryAmount;

    // Filters
    @FXML private DatePicker dpReportFrom;
    @FXML private DatePicker dpReportTo;

    // Charts
    @FXML private LineChart<String, Number> lineChartCashFlow;
    @FXML private PieChart pieChartCategory;
    @FXML private BarChart<String, Number> barChartCashFlow;

    // Services
    private TransactionService transactionService;
    private CategoryService categoryService;
    private BudgetService budgetService;
    private CsvExportService csvExportService;
    private PdfExportService pdfExportService;
    private User currentUser;

    public ReportController() {}

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> SceneNavigator.navigateTo("login.fxml"));
            return;
        }

        if (sidebarController != null) {
            sidebarController.setActive("reports");
        }

        try {
            transactionService = new TransactionService();
            categoryService = new CategoryService();
            budgetService = new BudgetService();
            csvExportService = new CsvExportService();
            pdfExportService = new PdfExportService();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return;
        }

        LocalDate now = LocalDate.now();
        dpReportFrom.setValue(now.withDayOfMonth(1));
        dpReportTo.setValue(now.withDayOfMonth(now.lengthOfMonth()));

        Platform.runLater(this::refreshReports);
    }

    @FXML
    private void handleUpdateReport() {
        refreshReports();
    }

    private void refreshReports() {
        LocalDate start = dpReportFrom.getValue();
        LocalDate end = dpReportTo.getValue();

        AsyncUtil.runAsync(() -> {
            ReportData data = new ReportData();
            
            // Summary Data
            data.netExpense = transactionService.getTotalExpense(currentUser.getId(), start, end);
            data.netIncome = transactionService.getTotalIncome(currentUser.getId(), start, end);
            
            // Pie Chart Data
            List<Transaction> monthTxns = transactionService.getTransactionsByDateRange(currentUser.getId(), start, end);
            Map<Integer, Double> expensesByCategory = monthTxns.stream()
                    .filter(t -> t.getType().equals("EXPENSE") && t.getCategoryId() > 0)
                    .collect(Collectors.groupingBy(Transaction::getCategoryId, Collectors.summingDouble(Transaction::getAmount)));

            data.pieData = FXCollections.observableArrayList();
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

            // Line Chart Data
            data.incomeSeries = new XYChart.Series<>();
            data.incomeSeries.setName("Income");
            data.expenseSeries = new XYChart.Series<>();
            data.expenseSeries.setName("Expense");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
            LocalDate now = LocalDate.now();

            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = now.minusMonths(i);
                LocalDate mStart = monthDate.withDayOfMonth(1);
                LocalDate mEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
                
                double inc = transactionService.getTotalIncome(currentUser.getId(), mStart, mEnd);
                double exp = transactionService.getTotalExpense(currentUser.getId(), mStart, mEnd);
                
                data.incomeSeries.getData().add(new XYChart.Data<>(monthDate.format(formatter), inc));
                data.expenseSeries.getData().add(new XYChart.Data<>(monthDate.format(formatter), exp));
            }

            // Bar Chart Data
            data.cashFlowSeries = new XYChart.Series<>();
            data.cashFlowSeries.setName("Net Flow");
            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = now.minusMonths(i);
                LocalDate mStart = monthDate.withDayOfMonth(1);
                LocalDate mEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
                
                double inc = transactionService.getTotalIncome(currentUser.getId(), mStart, mEnd);
                double exp = transactionService.getTotalExpense(currentUser.getId(), mStart, mEnd);
                data.cashFlowSeries.getData().add(new XYChart.Data<>(monthDate.format(formatter), inc - exp));
            }

            // Budget Data
            List<Budget> activeBudgets = budgetService.getActiveBudgets(currentUser.getId());
            data.budgetCount = activeBudgets.size();
            for (Budget b : activeBudgets) {
                double spent = budgetService.getSpentAmount(b);
                double pct = (spent / b.getAmount()) * 100;
                if (pct >= 100) data.exceededCount++;
                else if (pct < 85) data.healthyCount++;
            }

            return data;
            
        }, data -> {
            // Update UI Thread
            lblAvgSpending.setText(CurrencyUtil.formatSimple(data.netExpense));
            lblAvgSpendingTrend.setText("Tracked in period");

            if (data.netIncome > 0) {
                double savingsRate = ((data.netIncome - data.netExpense) / data.netIncome) * 100;
                lblSavingsRate.setText(String.format("%.1f%%", savingsRate));
            } else {
                lblSavingsRate.setText("0.0%");
            }
            lblSavingsRateTrend.setText("Income vs Expense");

            pieChartCategory.setData(data.pieData);
            if (data.topCategoryAmount > 0) {
                lblTopCategory.setText(data.topCategoryName);
                lblTopCategoryAmount.setText(CurrencyUtil.formatSimple(data.topCategoryAmount));
            } else {
                lblTopCategory.setText("N/A");
                lblTopCategoryAmount.setText("$0.00");
            }

            lineChartCashFlow.getData().clear();
            lineChartCashFlow.getData().addAll(data.incomeSeries, data.expenseSeries);
            
            barChartCashFlow.getData().clear();
            barChartCashFlow.getData().add(data.cashFlowSeries);

            lblActiveBudgets.setText(String.valueOf(data.budgetCount));
            lblExceededBudgets.setText(String.valueOf(data.exceededCount));
            if (data.exceededCount > 0) {
                lblExceededBudgets.setStyle("-fx-text-fill: #EF4444;");
            } else {
                lblExceededBudgets.setStyle("-fx-text-fill: -fx-text-primary;");
            }
            
            double healthScore = data.budgetCount == 0 ? 0 : ((double) data.healthyCount / data.budgetCount) * 100;
            lblBudgetHealth.setText(String.format("%.0f%%", healthScore));

        }, error -> {
            System.err.println("[ERROR-REP] Failed to load reports async.");
            error.printStackTrace();
        });
    }

    private static class ReportData {
        double netExpense;
        double netIncome;
        ObservableList<PieChart.Data> pieData;
        String topCategoryName = "None";
        double topCategoryAmount = 0.0;
        XYChart.Series<String, Number> incomeSeries;
        XYChart.Series<String, Number> expenseSeries;
        XYChart.Series<String, Number> cashFlowSeries;
        int budgetCount = 0;
        int exceededCount = 0;
        int healthyCount = 0;
    }

    @FXML
    private void handleExportCsv() {
        LocalDate start = dpReportFrom.getValue();
        LocalDate end = dpReportTo.getValue();
        if (start == null || end == null) {
            AlertUtil.showWarning("Please select a valid date range first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Transactions as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("fintrack_report_" + start + "_to_" + end + ".csv");
        
        java.io.File file = fileChooser.showSaveDialog(dpReportFrom.getScene().getWindow());
        if (file != null) {
            try {
                List<Transaction> txns = transactionService.getTransactionsByDateRange(currentUser.getId(), start, end);
                csvExportService.exportTransactionsToCsv(txns, file.getAbsolutePath());
                AlertUtil.showSuccess("Exported successfully to " + file.getName());
            } catch (Exception e) {
                AlertUtil.showError("Failed to export CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportPdf() {
        LocalDate start = dpReportFrom.getValue();
        LocalDate end = dpReportTo.getValue();
        if (start == null || end == null) {
            AlertUtil.showWarning("Please select a valid date range first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Financial Report as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
        fileChooser.setInitialFileName("fintrack_financial_report_" + start + "_to_" + end + ".pdf");
        
        java.io.File file = fileChooser.showSaveDialog(dpReportFrom.getScene().getWindow());
        if (file != null) {
            try {
                List<Transaction> txns = transactionService.getTransactionsByDateRange(currentUser.getId(), start, end);
                pdfExportService.exportFinancialReport(txns, start, end, file.getAbsolutePath());
                AlertUtil.showSuccess("Exported successfully to " + file.getName());
            } catch (Exception e) {
                AlertUtil.showError("Failed to export PDF: " + e.getMessage());
            }
        }
    }
}
