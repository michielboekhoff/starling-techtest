package com.michielboekhoff.starlingtest.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michielboekhoff.starlingtest.domain.Account;
import com.michielboekhoff.starlingtest.domain.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.UUID;

public class ApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String ACCOUNTS_API_PATH = "/api/v2/accounts";
    private static final String TRANSACTIONS_FEED_API_PATH_FORMAT = "/api/v2/feed/account/%s/category/%s/transactions-between?minTransactionTimestamp=%s&maxTransactionTimestamp=%s";
    private static final String SAVINGS_GOALS_TRANSFER_API_PATH_FORMAT = "/api/v2/account/%s/savings-goals/%s/add-money/%s";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final String baseUrl;
    private final String accessToken;

    public ApiClient(String baseUrl, String accessToken) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
    }

    public List<Account> getAllAccounts() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(resolveRelativeToBaseUrl(ACCOUNTS_API_PATH))
                .header("Authorization", bearerToken())
                .build();

        try {
            AccountsWrapper accountsWrapper = executeRequest(request, AccountsWrapper.class);
            return accountsWrapper.getAccounts();
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Could not get accounts data from Accounts API", e);
        }
    }

    public List<Transaction> getAllTransactionsForAccountAndDefaultCategoryInInterval(Account account,
                                                                                      Interval interval) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(getFeedUrlForAccountAndInterval(account, interval))
                .header("Authorization", bearerToken())
                .build();

        try {
            FeedsWrapper feedsWrapper = executeRequest(request, FeedsWrapper.class);
            return feedsWrapper.getTransactions();
        } catch (InterruptedException | IOException e) {
            throw new ApiException("Could not get accounts data from Transaction Feed API", e);
        }
    }

    public void transferIntoSavingsGoalForAccount(Account account, String savingsGoalUid, BigDecimal amount) {
        SavingsGoalTransfer savingsGoalTransfer = new SavingsGoalTransfer(
                new SavingsGoalTransfer.Amount("GBP", toMinorUnits(amount))
        );

        try {
            String json = objectMapper.writeValueAsString(savingsGoalTransfer);
            HttpRequest request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .uri(getSavingsGoalUrlForAccountAndSavingsGoal(account, savingsGoalUid))
                    .header("Authorization", bearerToken())
                    .header("Content-Type", "application/json")
                    .build();
            executeRequest(request);
        } catch (InterruptedException | IOException e) {
            throw new ApiException("Could not transfer savings via Savings Goals API", e);
        }
    }

    private String bearerToken() {
        return "Bearer " + accessToken;
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount.movePointRight(2).longValue();
    }

    private <T> T executeRequest(HttpRequest request, Class<T> klass) throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

        if (isNotSuccessful(response)) {
            throw new ApiException(String.format("Status code %d returned by %s", response.statusCode(), response.uri().toString()));
        }

        return objectMapper.readValue(response.body(), klass);
    }

    private void executeRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<Void> response = HTTP_CLIENT.send(request, BodyHandlers.discarding());

        if (isNotSuccessful(response)) {
            throw new ApiException(String.format("Status code %d returned by %s", response.statusCode(), response.uri().toString()));
        }
    }

    private boolean isNotSuccessful(HttpResponse<?> response) {
        return response.statusCode() < 200 || response.statusCode() > 299;
    }

    private URI getFeedUrlForAccountAndInterval(Account account, Interval interval) {
        String uriString = String.format(
                TRANSACTIONS_FEED_API_PATH_FORMAT,
                account.getAccountUid(),
                account.getDefaultCategory(),
                interval.getBegin(),
                interval.getEnd()
        );

        return resolveRelativeToBaseUrl(uriString);
    }

    private URI getSavingsGoalUrlForAccountAndSavingsGoal(Account account, String savingsGoalUid) {
        String uriString = String.format(
                SAVINGS_GOALS_TRANSFER_API_PATH_FORMAT,
                account.getAccountUid(),
                savingsGoalUid,
                UUID.randomUUID().toString()
        );

        return resolveRelativeToBaseUrl(uriString);
    }

    private URI resolveRelativeToBaseUrl(String path) {
        return URI.create(baseUrl).resolve(path);
    }

}
