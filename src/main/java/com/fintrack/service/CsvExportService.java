package com.fintrack.service;

import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExportService {

    private final CategoryService categoryService;

    public CsvExportService() {
        this.categoryService = new CategoryService();
    }

    public void exportTransactionsToCsv(List<Transaction> transactions, String filePath) throws IOException {
        try (FileWriter out = new FileWriter(filePath);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(
                     "ID", "Date", "Type", "Category", "Amount", "Description"))) {
            
            for (Transaction t : transactions) {
                String catName = "None";
                if (t.getCategoryId() > 0) {
                    Category cat = categoryService.getCategoryById(t.getCategoryId());
                    if (cat != null) catName = cat.getName();
                }

                printer.printRecord(
                        t.getId(),
                        t.getDate().toString(),
                        t.getType(),
                        catName,
                        t.getAmount(),
                        t.getDescription()
                );
            }
        }
    }
}
