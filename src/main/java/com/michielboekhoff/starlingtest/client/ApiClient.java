package com.michielboekhoff.starlingtest.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michielboekhoff.starlingtest.domain.Account;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String ACCOUNTS_API_PATH = "/api/v2/accounts";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    private final String baseUrl;
    private final String accessToken;

    public ApiClient(String baseUrl, String accessToken) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
    }

    public List<Account> getAllAccounts() {
        HttpUrl httpUrl = getAccountsUrl();
        Request request = new Request.Builder()
                .url(httpUrl)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            // the `response.body()` carries a warning that this could be null
            // but according to the documentation, this is never the case for `Call.execute`.
            return deserializeAccountsFromJson(response.body());
        } catch (IOException e) {
            throw new ApiException("Could not get accounts data from Accounts API", e);
        }
    }

    private List<Account> deserializeAccountsFromJson(ResponseBody responseBody) throws IOException {
        AccountsWrapper accountsWrapper = objectMapper.readValue(responseBody.string(), AccountsWrapper.class);
        return accountsWrapper.getAccounts();
    }

    private HttpUrl getAccountsUrl() {
        return Optional.ofNullable(HttpUrl.parse(baseUrl))
                .map(url -> url.resolve(ACCOUNTS_API_PATH))
                .orElseThrow(() -> new IllegalStateException("Base URL could not be parsed"));
    }

    private static class AccountsWrapper {
        private final List<Account> accounts;

        public AccountsWrapper(@JsonProperty(value = "accounts", required = true) List<Account> accounts) {
            this.accounts = accounts;
        }

        public List<Account> getAccounts() {
            return accounts;
        }
    }
}
