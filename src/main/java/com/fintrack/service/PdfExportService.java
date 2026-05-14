package com.fintrack.service;

import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.util.CurrencyUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PdfExportService {

    private final CategoryService categoryService;

    public PdfExportService() {
        this.categoryService = new CategoryService();
    }

    public void exportFinancialReport(List<Transaction> transactions, LocalDate from, LocalDate to, String filePath) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        // Header
        Paragraph title = new Paragraph("FinTrack Financial Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph dateRange = new Paragraph("Period: " + from.toString() + " to " + to.toString(), subtitleFont);
        dateRange.setAlignment(Element.ALIGN_CENTER);
        dateRange.setSpacingAfter(20);
        document.add(dateRange);

        // Summary Calculations
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) totalIncome += t.getAmount();
            else if ("EXPENSE".equals(t.getType())) totalExpense += t.getAmount();
        }
        double netSavings = totalIncome - totalExpense;

        // Summary Section
        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(30);
        
        summaryTable.addCell(createSummaryCell("Total Income", CurrencyUtil.formatSimple(totalIncome), new BaseColor(16, 185, 129))); // Emerald
        summaryTable.addCell(createSummaryCell("Total Expenses", CurrencyUtil.formatSimple(totalExpense), new BaseColor(239, 68, 68))); // Red
        summaryTable.addCell(createSummaryCell("Net Savings", CurrencyUtil.formatSimple(netSavings), new BaseColor(59, 130, 246))); // Blue
        
        document.add(summaryTable);

        // Transactions Table
        Paragraph tableTitle = new Paragraph("Transaction Details", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY));
        tableTitle.setSpacingAfter(10);
        document.add(tableTitle);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2f, 3f, 2f, 2f});

        // Headers
        String[] headers = {"Date", "Type", "Description", "Category", "Amount"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new BaseColor(15, 23, 42)); // Slate 900
            cell.setPadding(8);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }

        // Data
        boolean alternate = false;
        for (Transaction t : transactions) {
            String catName = "None";
            if (t.getCategoryId() > 0) {
                Category cat = categoryService.getCategoryById(t.getCategoryId());
                if (cat != null) catName = cat.getName();
            }

            table.addCell(createCell(t.getDate().toString(), cellFont, alternate));
            table.addCell(createCell(t.getType(), cellFont, alternate));
            table.addCell(createCell(t.getDescription(), cellFont, alternate));
            table.addCell(createCell(catName, cellFont, alternate));
            
            String formattedAmt = CurrencyUtil.formatSimple(t.getAmount());
            if (t.getType().equals("EXPENSE")) formattedAmt = "-" + formattedAmt;
            else if (t.getType().equals("INCOME")) formattedAmt = "+" + formattedAmt;
            
            table.addCell(createCell(formattedAmt, cellFont, alternate));
            alternate = !alternate;
        }

        document.add(table);
        document.close();
    }

    private PdfPCell createSummaryCell(String label, String value, BaseColor valueColor) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(15);
        cell.setBorderColor(new BaseColor(226, 232, 240)); // Slate 200
        
        Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
        pLabel.setAlignment(Element.ALIGN_CENTER);
        Paragraph pValue = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, valueColor));
        pValue.setAlignment(Element.ALIGN_CENTER);
        
        cell.addElement(pLabel);
        cell.addElement(pValue);
        return cell;
    }

    private PdfPCell createCell(String text, Font font, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(8);
        cell.setBorderColor(new BaseColor(226, 232, 240));
        if (alternate) {
            cell.setBackgroundColor(new BaseColor(248, 250, 252)); // Slate 50
        }
        return cell;
    }
}
