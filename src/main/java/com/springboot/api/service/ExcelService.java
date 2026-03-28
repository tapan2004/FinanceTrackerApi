package com.springboot.api.service;

import com.springboot.api.entity.users.User;
import com.springboot.api.entity.transactions.Transaction;
import com.springboot.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

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
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Title");
            header.createCell(1).setCellValue("Amount");
            header.createCell(2).setCellValue("Type");
            header.createCell(3).setCellValue("Date");

            int rowIdx = 1;

            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getTitle());
                row.createCell(1).setCellValue(t.getAmount());
                row.createCell(2).setCellValue(t.getType().name());
                row.createCell(3).setCellValue(t.getCreatedAt().toString());
            }
            workbook.write(out);
            workbook.close();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}