package com.michielboekhoff.starlingtest.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michielboekhoff.starlingtest.domain.Transaction;
import com.michielboekhoff.starlingtest.domain.Transaction.TransactionDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionDeserializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("it should deserialize a transaction")
    void shouldDeserialize() throws JsonProcessingException {
        String json = "{\n" +
                "      \"feedItemUid\": \"75d7b8ca-36c2-4f76-9887-8996924606c2\",\n" +
                "      \"categoryUid\": \"e1b49313-eb7f-4f1f-808a-fcc729e8c01e\",\n" +
                "      \"amount\": {\n" +
                "        \"currency\": \"GBP\",\n" +
                "        \"minorUnits\": 60000\n" +
                "      },\n" +
                "      \"sourceAmount\": {\n" +
                "        \"currency\": \"GBP\",\n" +
                "        \"minorUnits\": 60000\n" +
                "      },\n" +
                "      \"direction\": \"IN\",\n" +
                "      \"updatedAt\": \"2020-01-09T21:43:14.721Z\",\n" +
                "      \"transactionTime\": \"2020-01-09T21:43:14.000Z\",\n" +
                "      \"settlementTime\": \"2020-01-09T21:43:14.000Z\",\n" +
                "      \"source\": \"FASTER_PAYMENTS_IN\",\n" +
                "      \"status\": \"SETTLED\",\n" +
                "      \"counterPartyType\": \"SENDER\",\n" +
                "      \"counterPartyName\": \"MR CUSTOMER\",\n" +
                "      \"counterPartySubEntityName\": \"\",\n" +
                "      \"counterPartySubEntityIdentifier\": \"203002\",\n" +
                "      \"counterPartySubEntitySubIdentifier\": \"79155677\",\n" +
                "      \"reference\": \"Test deposit\",\n" +
                "      \"country\": \"GB\",\n" +
                "      \"spendingCategory\": \"INCOME\"\n" +
                "    }";

        Transaction transaction = objectMapper.readValue(json, Transaction.class);

        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("600.00"));
        assertThat(transaction.getTransactionDirection()).isEqualTo(TransactionDirection.IN);
    }

    @Test
    @DisplayName("it should throw a JsonProcessingException when the amount is missing")
    void cannotDeserializeWithoutAmount() {
        String json = "{\n" +
                "      \"feedItemUid\": \"75d7b8ca-36c2-4f76-9887-8996924606c2\",\n" +
                "      \"categoryUid\": \"e1b49313-eb7f-4f1f-808a-fcc729e8c01e\",\n" +
                "      \"amount\": {\n" +
                "        \"currency\": \"GBP\"\n" +
                "      },\n" +
                "      \"sourceAmount\": {\n" +
                "        \"currency\": \"GBP\",\n" +
                "        \"minorUnits\": 60000\n" +
                "      },\n" +
                "      \"direction\": \"IN\",\n" +
                "      \"updatedAt\": \"2020-01-09T21:43:14.721Z\",\n" +
                "      \"transactionTime\": \"2020-01-09T21:43:14.000Z\",\n" +
                "      \"settlementTime\": \"2020-01-09T21:43:14.000Z\",\n" +
                "      \"source\": \"FASTER_PAYMENTS_IN\",\n" +
                "      \"status\": \"SETTLED\",\n" +
                "      \"counterPartyType\": \"SENDER\",\n" +
                "      \"counterPartyName\": \"MR CUSTOMER\",\n" +
                "      \"counterPartySubEntityName\": \"\",\n" +
                "      \"counterPartySubEntityIdentifier\": \"203002\",\n" +
                "      \"counterPartySubEntitySubIdentifier\": \"79155677\",\n" +
                "      \"reference\": \"Test deposit\",\n" +
                "      \"country\": \"GB\",\n" +
                "      \"spendingCategory\": \"INCOME\"\n" +
                "    }";

        assertThatThrownBy(() -> objectMapper.readValue(json, Transaction.class))
                .isInstanceOf(JsonProcessingException.class)
                .hasMessageStartingWith("Cannot construct instance of `com.michielboekhoff.starlingtest.domain.Transaction`: A numeric value for $.amount.minorUnits is required");
    }

    @Test
    void shouldNotDeserializeWithoutDirection() {
        String json = "{\n" +
                "      \"feedItemUid\": \"75d7b8ca-36c2-4f76-9887-8996924606c2\",\n" +
                "      \"categoryUid\": \"e1b49313-eb7f-4f1f-808a-fcc729e8c01e\",\n" +
                "      \"amount\": {\n" +
                "        \"currency\": \"GBP\",\n" +
                "        \"minorUnits\": 60000\n" +
                "      },\n" +
                "      \"sourceAmount\": {\n" +
                "        \"currency\": \"GBP\",\n" +
                "        \"minorUnits\": 60000\n" +
                "      },\n" +
                "      \"updatedAt\": \"2020-01-09T21:43:14.721Z\",\n" +
                "      \"transactionTime\": \"2020-01-09T21:43:14.000Z\",\n" +
                "      \"settlementTime\": \"2020-01-09T21:43:14.000Z\",\n" +
                "      \"source\": \"FASTER_PAYMENTS_IN\",\n" +
                "      \"status\": \"SETTLED\",\n" +
                "      \"counterPartyType\": \"SENDER\",\n" +
                "      \"counterPartyName\": \"MR CUSTOMER\",\n" +
                "      \"counterPartySubEntityName\": \"\",\n" +
                "      \"counterPartySubEntityIdentifier\": \"203002\",\n" +
                "      \"counterPartySubEntitySubIdentifier\": \"79155677\",\n" +
                "      \"reference\": \"Test deposit\",\n" +
                "      \"country\": \"GB\",\n" +
                "      \"spendingCategory\": \"INCOME\"\n" +
                "    }";

        assertThatThrownBy(() -> objectMapper.readValue(json, Transaction.class))
                .isInstanceOf(JsonProcessingException.class)
                .hasMessageStartingWith("Cannot construct instance of `com.michielboekhoff.starlingtest.domain.Transaction`: A value has to be provided for $.direction");
    }

    @Test
    void shouldNotDeserializeWithInvalidDirection() {
        String json = "{\n" +
                "      \"feedItemUid\": \"75d7b8ca-36c2-4f76-9887-8996924606c2\",\n" +
                "      \"categoryUid\": \"e1b49313-eb7f-4f1f-808a-fcc729e8c01e\",\n" +
                "      \"amount\": {\n" +
                "        \"currency\": \"GBP\",\n" +
                "        \"minorUnits\": 60000\n" +
                "      },\n" +
                "      \"sourceAmount\": {\n" +
                "        \"currency\": \"GBP\",\n" +
                "        \"minorUnits\": 60000\n" +
                "      },\n" +
                "      \"direction\": \"INVALID\",\n" +
                "      \"updatedAt\": \"2020-01-09T21:43:14.721Z\",\n" +
                "      \"transactionTime\": \"2020-01-09T21:43:14.000Z\",\n" +
                "      \"settlementTime\": \"2020-01-09T21:43:14.000Z\",\n" +
                "      \"source\": \"FASTER_PAYMENTS_IN\",\n" +
                "      \"status\": \"SETTLED\",\n" +
                "      \"counterPartyType\": \"SENDER\",\n" +
                "      \"counterPartyName\": \"MR CUSTOMER\",\n" +
                "      \"counterPartySubEntityName\": \"\",\n" +
                "      \"counterPartySubEntityIdentifier\": \"203002\",\n" +
                "      \"counterPartySubEntitySubIdentifier\": \"79155677\",\n" +
                "      \"reference\": \"Test deposit\",\n" +
                "      \"country\": \"GB\",\n" +
                "      \"spendingCategory\": \"INCOME\"\n" +
                "    }";

        assertThatThrownBy(() -> objectMapper.readValue(json, Transaction.class))
                .isInstanceOf(JsonProcessingException.class)
                .hasMessageStartingWith("Cannot deserialize value of type `com.michielboekhoff.starlingtest.domain.Transaction$TransactionDirection` from String \"INVALID\": not one of the values accepted for Enum class: [IN, OUT]");
    }
}