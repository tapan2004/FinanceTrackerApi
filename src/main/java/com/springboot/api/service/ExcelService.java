package com.springboot.api.service;

import com.springboot.api.entity.users.User;
import com.springboot.api.entity.transactions.Transaction;
import com.springboot.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public ByteArrayInputStream exportToExcel(String email) throws Exception {
        User user = userService.findByEmail(email);
        List<Transaction> transactions =
                transactionRepository.findByUser_UserId(user.getUserId());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Transactions");

            // Header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"Title", "Amount", "Type", "Category", "Note", "Date"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowIdx++);

                // Null-safe cell generation
                row.createCell(0).setCellValue(t.getTitle() != null ? t.getTitle() : "");
                row.createCell(1).setCellValue(t.getAmount() != null ? t.getAmount() : 0.0);
                row.createCell(2).setCellValue(t.getType() != null ? t.getType().name() : "UNKNOWN");
                row.createCell(3).setCellValue(
                        t.getCategory() != null && t.getCategory().getName() != null
                                ? t.getCategory().getName() : "Uncategorized"
                );
                row.createCell(4).setCellValue(t.getNote() != null ? t.getNote() : "");
                row.createCell(5).setCellValue(
                        t.getCreatedAt() != null ? t.getCreatedAt().toString() : ""
                );
            }

            // Auto-size columns for readability
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}