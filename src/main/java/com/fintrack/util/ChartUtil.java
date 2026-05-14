package com.fintrack.util;

import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class ChartUtil {

    /**
     * Applies a modern SaaS-style tooltip to a PieChart slice.
     */
    public static void setupPieChartTooltip(PieChart pieChart) {
        for (PieChart.Data data : pieChart.getData()) {
            Node node = data.getNode();
            if (node != null) {
                Tooltip tooltip = new Tooltip(data.getName() + ": " + CurrencyUtil.formatSimple(data.getPieValue()));
                styleTooltip(tooltip);
                Tooltip.install(node, tooltip);

                // Hover effect
                node.setOnMouseEntered(e -> node.setStyle("-fx-opacity: 0.8; -fx-cursor: hand;"));
                node.setOnMouseExited(e -> node.setStyle("-fx-opacity: 1.0; -fx-cursor: default;"));
            }
        }
    }

    /**
     * Applies a modern SaaS-style tooltip to BarChart/LineChart data nodes.
     */
    public static void setupXYChartTooltip(XYChart<String, Number> chart) {
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    Tooltip tooltip = new Tooltip(series.getName() + " (" + data.getXValue() + "): " 
                            + CurrencyUtil.formatSimple(data.getYValue().doubleValue()));
                    styleTooltip(tooltip);
                    Tooltip.install(node, tooltip);

                    // Hover effect
                    node.setOnMouseEntered(e -> node.setStyle("-fx-opacity: 0.7; -fx-cursor: hand;"));
                    node.setOnMouseExited(e -> node.setStyle("-fx-opacity: 1.0; -fx-cursor: default;"));
                }
            }
        }
    }

    private static void styleTooltip(Tooltip tooltip) {
        tooltip.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-padding: 8px 12px; -fx-background-radius: 6px;");
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setHideDelay(Duration.millis(100));
        tooltip.setShowDuration(Duration.seconds(5));
    }
}
