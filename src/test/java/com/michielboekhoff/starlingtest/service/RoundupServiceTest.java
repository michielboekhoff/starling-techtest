package com.michielboekhoff.starlingtest.service;

import com.michielboekhoff.starlingtest.client.ApiClient;
import com.michielboekhoff.starlingtest.domain.Account;
import com.michielboekhoff.starlingtest.domain.Transaction;
import com.michielboekhoff.starlingtest.domain.Transaction.TransactionDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RoundupServiceTest {

    private static final String SAVINGS_GOAL_UID = "savingsGoalUid";

    private static final Account FIRST_ACCOUNT = new Account("accountOne", "defaultCategory");
    private static final Account SECOND_ACCOUNT = new Account("accountTwo", "defaultCategory");

    private final ApiClient apiClient = mock(ApiClient.class);
    private final RoundupService roundupService = new RoundupService(apiClient);

    private final List<Transaction> transactions = List.of(
            new Transaction(new BigDecimal("4.35"), TransactionDirection.OUT),
            new Transaction(new BigDecimal("5.20"), TransactionDirection.OUT),
            new Transaction(new BigDecimal("0.87"), TransactionDirection.OUT)
    );

    @Test
    @DisplayName("it should round up all transactions into a savings goal for an account")
    void shouldRoundUpTransactionsForAnAccount() {
        when(apiClient.getAllAccounts()).thenReturn(List.of(FIRST_ACCOUNT));
        when(apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(eq(FIRST_ACCOUNT), any()))
                .thenReturn(transactions);

        roundupService.roundUpTransactionsFromLastWeekIntoSavingsGoal(SAVINGS_GOAL_UID);

        verify(apiClient)
                .transferIntoSavingsGoalForAccount(any(), eq(SAVINGS_GOAL_UID), eq(new BigDecimal("1.58")));
    }

    @Test
    @DisplayName("it should round up the transactions into a savings goal per account")
    void shouldRoundUpTransactionsPerAccount() {
        when(apiClient.getAllAccounts()).thenReturn(List.of(FIRST_ACCOUNT, SECOND_ACCOUNT));
        when(apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(eq(FIRST_ACCOUNT), any()))
                .thenReturn(List.of(new Transaction(new BigDecimal("1.58"), TransactionDirection.OUT)));
        when(apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(eq(SECOND_ACCOUNT), any()))
                .thenReturn(List.of(new Transaction(new BigDecimal("0.84"), TransactionDirection.OUT)));

        roundupService.roundUpTransactionsFromLastWeekIntoSavingsGoal(SAVINGS_GOAL_UID);

        verify(apiClient).transferIntoSavingsGoalForAccount(FIRST_ACCOUNT, SAVINGS_GOAL_UID, new BigDecimal("0.42"));
        verify(apiClient).transferIntoSavingsGoalForAccount(SECOND_ACCOUNT, SAVINGS_GOAL_UID, new BigDecimal("0.16"));
    }
}