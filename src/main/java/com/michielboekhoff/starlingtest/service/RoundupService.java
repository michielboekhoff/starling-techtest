package com.michielboekhoff.starlingtest.service;

import com.michielboekhoff.starlingtest.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class RoundupService {
    public BigDecimal roundUp(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .map(this::getAmountToSave)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getAmountToSave(BigDecimal transactionAmount) {
        BigDecimal fractionalPart = transactionAmount.remainder(BigDecimal.ONE);
        return BigDecimal.ONE.subtract(fractionalPart);
    }
}
