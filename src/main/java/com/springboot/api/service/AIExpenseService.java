package com.springboot.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIExpenseService {
    private static final Map<String,String> rules = Map.of(
            "uber","Travel",
            "ola","Travel",
            "pizza","Food",
            "restaurant","Food",
            "netflix","Entertainment",
            "hotstar","Entertainment",
            "amazon","Shopping",
            "flipkart","Shopping"
    );

    public String predictCategory(String title) {
        title = title.toLowerCase();
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            if (title.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Others";
    }
}