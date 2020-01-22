package com.michielboekhoff.starlingtest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.michielboekhoff.starlingtest.domain.Account;
import com.michielboekhoff.starlingtest.domain.Transaction;
import com.michielboekhoff.starlingtest.domain.Transaction.TransactionDirection;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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

    @BeforeEach
    public void beforeEach() {
        wireMock.resetAll();
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
        @DisplayName("it should throw an IllegalArgumentException if the baseUrl cannot be parsed")
        void invalidBaseUrl() {
            ApiClient apiClient = new ApiClient("foo", ACCESS_TOKEN);

            assertThatThrownBy(apiClient::getAllAccounts)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("URI with undefined scheme");
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

        @Test
        @DisplayName("it should throw an ApiException when the HTTP status code is not successful")
        void nonSuccessfulStatusCode() {
            stubFor(get("/api/v2/accounts").willReturn(aResponse().withStatus(404)));

            assertThatThrownBy(apiClient::getAllAccounts)
                    .isInstanceOf(ApiException.class)
                    .hasMessageMatching("Status code 404 returned by http://.*/api/v2/accounts");
        }
    }

    @Nested
    @DisplayName("getAllTransactionsForAccountAndDefaultCategoryInInterval")
    class GetAllDebitTransactionsTests {

        private final Account account = new Account("accountUid", "defaultCategory");
        private final Clock clock = Clock.fixed(Instant.parse("2020-01-21T10:15:30Z"), ZoneId.of("Z"));
        private final Interval interval = Interval.lastWeek(clock);

        public final List<Transaction> allTransactions = List.of(
                new Transaction(new BigDecimal("600.00"), TransactionDirection.IN),
                new Transaction(new BigDecimal("37.65"), TransactionDirection.OUT),
                new Transaction(new BigDecimal("22.21"), TransactionDirection.OUT)
        );

        @DisplayName("it should get all transactions")
        @Test
        void getAllTransactions() {
            stubFor(
                    get(urlPathEqualTo("/api/v2/feed/account/accountUid/category/defaultCategory/transactions-between"))
                            .withHeader("Authorization", equalTo("Bearer token"))
                            .withQueryParam("minTransactionTimestamp", equalTo("2020-01-14T10:15:30Z"))
                            .withQueryParam("maxTransactionTimestamp", equalTo("2020-01-21T10:15:30Z"))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withBodyFile("transaction_feed.json")
                            )
            );

            List<Transaction> transactions = apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(account, interval);

            // Transaction does not provide an equals method, and I am not a fan of writing production code for tests.
            // AssertJ provides a method usingRecursiveFieldByFieldElementComparator that allows for to comparing
            // objects based on their fields.
            assertThat(transactions)
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactlyInAnyOrderElementsOf(allTransactions);
        }

        @Test
        @DisplayName("it should throw an IllegalArgumentException if the baseUrl cannot be parsed")
        void invalidBaseUrl() {
            ApiClient apiClient = new ApiClient("foo", ACCESS_TOKEN);

            assertThatThrownBy(() -> apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(account, interval))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("URI with undefined scheme");
        }

        @Test
        @DisplayName("it should throw an ApiException when a connection cannot be established")
        void cannotConnect() {
            stubFor(
                    get(urlPathEqualTo("/api/v2/feed/account/accountUid/category/defaultCategory/transactions-between"))
                            .willReturn(
                                    aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)
                            )
            );

            assertThatThrownBy(() -> apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(account, interval))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Could not get accounts data from Transaction Feed API")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("it should throw an ApiException when the received JSON is invalid")
        void invalidJson() {
            stubFor(
                    get(urlPathEqualTo("/api/v2/feed/account/accountUid/category/defaultCategory/transactions-between"))
                            .willReturn(aResponse().withStatus(200).withBody("not json"))
            );

            assertThatThrownBy(() -> apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(account, interval))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Could not get accounts data from Transaction Feed API")
                    .hasCauseInstanceOf(JsonProcessingException.class);
        }

        @Test
        @DisplayName("it should throw an ApiException when the HTTP status code is not successful")
        void nonSuccessfulStatusCode() {
            stubFor(get("/api/v2/feed/account/accountUid/category/defaultCategory")
                    .willReturn(aResponse().withStatus(404)));

            assertThatThrownBy(() -> apiClient.getAllTransactionsForAccountAndDefaultCategoryInInterval(account, interval))
                    .isInstanceOf(ApiException.class)
                    .hasMessageMatching("Status code 404 returned by http://.*/api/v2/feed/account/accountUid/category/defaultCategory.*");
        }
    }

    @Nested
    @DisplayName("transferAmountToSavingsGoal")
    class TransferAmountToSavingsGoalTest {

        private final Account account = new Account("accountUid", "defaultCategory");

        private final String savingsGoalUid = "savingsGoalUid";

        private final BigDecimal transferAmount = new BigDecimal("1.00");

        @DisplayName("it should call the transfer endpoint")
        @Test
        void callsTheTransferEndpoint() {
            stubFor(
                    put(urlPathMatching("^/api/v2/account/accountUid/savings-goals/savingsGoalUid/add-money/.*"))
                            .willReturn(aResponse().withStatus(200).withBodyFile("savings_goal.json"))
            );

            apiClient.transferIntoSavingsGoalForAccount(account, savingsGoalUid, transferAmount);

            verify(
                    putRequestedFor(urlPathMatching("^/api/v2/account/accountUid/savings-goals/savingsGoalUid/add-money/[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}"))
                            .withHeader("Authorization", equalTo("Bearer token"))
                            .withHeader("Content-Type", equalTo("application/json"))
                            .withRequestBody(matchingJsonPath("$.amount.currency", equalTo("GBP")))
                            .withRequestBody(matchingJsonPath("$.amount.minorUnits", equalTo("100")))
            );
        }

        @Test
        @DisplayName("it should throw an ApiException when the HTTP status code is not successful")
        void nonSuccessfulStatusCode() {
            stubFor(put(urlPathMatching("^/api/v2/account/accountUid/savings-goals/savingsGoalUid/add-money/.*"))
                    .willReturn(aResponse().withStatus(404)));

            assertThatThrownBy(() -> apiClient.transferIntoSavingsGoalForAccount(account, savingsGoalUid, transferAmount))
                    .isInstanceOf(ApiException.class)
                    .hasMessageMatching("Status code 404 returned by http://.*/api/v2/account/accountUid/savings-goals/savingsGoalUid/add-money/.*");
        }

        @Test
        @DisplayName("it should throw an IllegalArgumentException if the baseUrl cannot be parsed")
        void invalidBaseUrl() {
            ApiClient apiClient = new ApiClient("foo", ACCESS_TOKEN);

            assertThatThrownBy(() -> apiClient.transferIntoSavingsGoalForAccount(account, savingsGoalUid, transferAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("URI with undefined scheme");
        }

        @Test
        @DisplayName("it should throw an ApiException when a connection cannot be established")
        void cannotConnect() {
            stubFor(put(urlPathMatching("^/api/v2/account/accountUid/savings-goals/savingsGoalUid/add-money/.*"))
                    .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

            assertThatThrownBy(() -> apiClient.transferIntoSavingsGoalForAccount(account, savingsGoalUid, transferAmount))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Could not transfer savings via Savings Goals API")
                    .hasCauseInstanceOf(IOException.class);
        }
    }
}