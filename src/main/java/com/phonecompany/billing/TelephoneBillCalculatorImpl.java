package com.phonecompany.billing;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator{

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static class Call {
        LocalDateTime start;
        LocalDateTime end;

        Call(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }

    @Override
    public BigDecimal calculate(String phoneLog) {

        Map<String, List<Call>> callsPerNumber = new HashMap<>();
        String[] lines = phoneLog.split("\\r?\\n");

        for (String line : lines) {
            String[] parts = line.split(",");
            String number = parts[0];
            var start = LocalDateTime.parse(parts[1], FORMATTER);
            var end = LocalDateTime.parse(parts[2], FORMATTER);
            callsPerNumber.computeIfAbsent(number, k -> new ArrayList<>()).add(new Call(start, end));
        }

        String freeNumber = callsPerNumber.entrySet().stream()
                .sorted((e1, e2) -> {
                    int cmp = Integer.compare(e2.getValue().size(), e1.getValue().size());
                    if (cmp == 0) {
                        return e2.getKey().compareTo(e1.getKey());
                    }
                    return cmp;
                }).map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        BigDecimal total = BigDecimal.ZERO;

        for (var entry : callsPerNumber.entrySet()) {
            String number = entry.getKey();
            if (number.equals(freeNumber)) {
                continue;
            }
            for (Call call : entry.getValue()) {
                total = total.add(calculateCallCost(call));
            }
        }
        return total;
    }

    private BigDecimal calculateCallCost(Call call) {
        var start = call.start;
        var end = call.end;

        int minutes = (int) ChronoUnit.MINUTES.between(start, end);
        if (start.plusMinutes(minutes).isBefore(end)) {
            minutes++;
        }
        if (minutes == 0) {
            minutes = 1;
        }

        BigDecimal cost = BigDecimal.ZERO;

        for (int i = 0; i < minutes; i++) {
            LocalDateTime minuteStart = start.plusMinutes(i);
            if (i >= 5) {
                cost = cost.add(BigDecimal.valueOf(0.20));
            } else {
                if (minuteStart.getHour() >= 8 && minuteStart.getHour() < 16) {
                    cost = cost.add(BigDecimal.valueOf(1.0));
                } else {
                    cost = cost.add(BigDecimal.valueOf(0.5));
                }
            }
        }
        return cost;
    }
}
