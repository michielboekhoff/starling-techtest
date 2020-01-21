package com.michielboekhoff.starlingtest.service;

import com.michielboekhoff.starlingtest.domain.Transaction;
import com.michielboekhoff.starlingtest.domain.Transaction.TransactionDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class RoundupServiceTest {

    private final RoundupService roundupService = new RoundupService();

    @Test
    @DisplayName("roundUp should calculate the amount to be saved")
    void roundUpShouldCalculateSavings() {
        List<Transaction> transactions = List.of(
                new Transaction(new BigDecimal("1.88"), TransactionDirection.OUT),
                new Transaction(new BigDecimal("5.27"), TransactionDirection.OUT)
        );

        BigDecimal savingAmount = roundupService.roundUp(transactions);

        assertThat(savingAmount).isEqualTo(new BigDecimal("0.85"));
    }

    @Test
    @DisplayName("roundUp should return zero if there are no transactions")
    void roundUpWithoutTransactions() {
        BigDecimal savingAmount = roundupService.roundUp(emptyList());

        assertThat(savingAmount).isEqualTo(BigDecimal.ZERO);
    }
}