package com.michielboekhoff.starlingtest.service;

import com.michielboekhoff.starlingtest.client.ApiClient;
import com.michielboekhoff.starlingtest.client.Interval;
import com.michielboekhoff.starlingtest.domain.Account;
import com.michielboekhoff.starlingtest.domain.Transaction;
import com.michielboekhoff.starlingtest.domain.Transaction.TransactionDirection;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

public class RoundupService {

    private static final Interval LAST_WEEK_INTERVAL = Interval.lastWeek(Clock.systemUTC());

    private final ApiClient apiClient;

    public RoundupService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void roundUpTransactionsFromLastWeekIntoSavingsGoal(String savingsGoalUid) {
        List<Account> accounts = apiClient.getAllAccounts();

        for (Account account : accounts) {
            BigDecimal totalToSave = getAmountToSaveForAccount(account);
            apiClient.transferIntoSavingsGoalForAccount(account, savingsGoalUid, totalToSave);
        }
    }

    private BigDecimal getAmountToSaveForAccount(Account account) {
        return apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(account, LAST_WEEK_INTERVAL)
                .stream()
                .filter(transaction -> TransactionDirection.OUT.equals(transaction.getTransactionDirection()))
                .map(Transaction::getAmount)
                .map(this::getAmountToSave)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getAmountToSave(BigDecimal transactionAmount) {
        BigDecimal fractionalPart = transactionAmount.remainder(BigDecimal.ONE);
        return BigDecimal.ONE.subtract(fractionalPart);
    }
}
