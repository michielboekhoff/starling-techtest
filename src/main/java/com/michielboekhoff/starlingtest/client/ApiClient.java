package com.michielboekhoff.starlingtest.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michielboekhoff.starlingtest.domain.Account;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

public class ApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String ACCOUNTS_API_PATH = "/api/v2/accounts";
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
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            AccountsWrapper accountsWrapper = objectMapper.readValue(response.body(), AccountsWrapper.class);
            return accountsWrapper.getAccounts();
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Could not get accounts data from Accounts API", e);
        }
    }

    private URI resolveRelativeToBaseUrl(String path) {
        return URI.create(baseUrl).resolve(path);
    }
}
