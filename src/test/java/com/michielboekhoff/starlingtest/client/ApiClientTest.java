package com.michielboekhoff.starlingtest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.michielboekhoff.starlingtest.domain.Account;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiClientTest {

    private static final WireMockServer wireMock = new WireMockServer(options());
    private static final String ACCESS_TOKEN = "token";

    private final ApiClient apiClient = new ApiClient(wireMock.baseUrl(), ACCESS_TOKEN);

    @BeforeAll
    public static void setUp() {
        wireMock.start();
    }

    @AfterAll
    public static void tearDown() {
        wireMock.stop();
    }

    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccountsTests {
        @Test
        @DisplayName("it should get all accounts from the API")
        void getsAllAccounts() {
            stubFor(
                    get("/api/v2/accounts")
                            .withHeader("Authorization", equalTo("Bearer token"))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withBodyFile("accounts.json")
                            )
            );

            List<Account> accounts = apiClient.getAllAccounts();

            assertThat(accounts)
                    .hasOnlyOneElementSatisfying(account -> assertThat(account.getAccountUid()).isEqualTo("bbccbbcc-bbcc-bbcc-bbcc-bbccbbccbbcc"));
            assertThat(accounts)
                    .hasOnlyOneElementSatisfying(account -> assertThat(account.getDefaultCategory()).isEqualTo("ccddccdd-ccdd-ccdd-ccdd-ccddccddccdd"));
        }

        @Test
        @DisplayName("it should throw an IllegalStateException if the baseUrl cannot be parsed")
        void invalidBaseUrl() {
            ApiClient apiClient = new ApiClient("foo", ACCESS_TOKEN);

            assertThatThrownBy(apiClient::getAllAccounts)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Base URL could not be parsed");
        }

        @Test
        @DisplayName("it should throw an ApiException when a connection cannot be established")
        void cannotConnect() {
            stubFor(get("/api/v2/accounts").willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

            assertThatThrownBy(apiClient::getAllAccounts)
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Could not get accounts data from Accounts API")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("it should throw an ApiException when the received JSON is invalid")
        void invalidJson() {
            stubFor(get("/api/v2/accounts").willReturn(aResponse().withStatus(200).withBody("not json")));

            assertThatThrownBy(apiClient::getAllAccounts)
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Could not get accounts data from Accounts API")
                    .hasCauseInstanceOf(JsonProcessingException.class);
        }
    }
}