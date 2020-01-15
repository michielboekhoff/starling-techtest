package com.michielboekhoff.starlingtest.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.michielboekhoff.starlingtest.domain.Account;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountsClientTest {

    private static final WireMockServer wireMock = new WireMockServer(options());
    private static final String ACCESS_TOKEN = "token";

    private final AccountsClient accountsClient = new AccountsClient(wireMock.baseUrl(), ACCESS_TOKEN);

    @BeforeAll
    public static void setUp() {
        wireMock.start();
    }

    @AfterAll
    public static void tearDown() {
        wireMock.stop();
    }

    @Test
    @DisplayName("getAllAccounts should get all accounts from the API")
    void getAllAccountsGetsAllAccounts() {
        stubFor(
                get("/api/v2/accounts")
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBodyFile("accounts.json")
                        )
        );

        List<Account> accounts = accountsClient.getAllAccounts();

        assertThat(accounts)
                .hasOnlyOneElementSatisfying(account -> assertThat(account.getAccountUid()).isEqualTo("bbccbbcc-bbcc-bbcc-bbcc-bbccbbccbbcc"));
        assertThat(accounts)
                .hasOnlyOneElementSatisfying(account -> assertThat(account.getDefaultCategory()).isEqualTo("ccddccdd-ccdd-ccdd-ccdd-ccddccddccdd"));
    }

    @Test
    @DisplayName("getAllAccounts should throw an IllegalStateException if the baseUrl cannot be parsed")
    void getAllAccountsInvalidBaseUrl() {
        AccountsClient accountsClient = new AccountsClient("foo", ACCESS_TOKEN);

        assertThatThrownBy(accountsClient::getAllAccounts)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Base URL could not be parsed");
    }
}