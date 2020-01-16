package com.michielboekhoff.starlingtest.client;

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
        HttpUrl httpUrl = getRelativePathToBaseUrl(ACCOUNTS_API_PATH);
        Request request = new Request.Builder()
                .url(httpUrl)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            // the `response.body()` carries a warning that this could be null
            // but according to the documentation, this is never the case for `Call.execute`.
            AccountsWrapper accountsWrapper = objectMapper.readValue(response.body().string(), AccountsWrapper.class);
            return accountsWrapper.getAccounts();
        } catch (IOException e) {
            throw new ApiException("Could not get accounts data from Accounts API", e);
        }
    }

    private HttpUrl getRelativePathToBaseUrl(String path) {
        return Optional.ofNullable(HttpUrl.parse(baseUrl))
                .map(url -> url.resolve(path))
                .orElseThrow(() -> new IllegalStateException("Base URL could not be parsed"));
    }
}
