package com.michielboekhoff.starlingtest.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.michielboekhoff.starlingtest.domain.Transaction;
import com.michielboekhoff.starlingtest.domain.Transaction.TransactionDirection;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

public class TransactionDeserializer extends StdDeserializer<Transaction> {

    protected TransactionDeserializer() {
        super(Transaction.class);
    }

    @Override
    public Transaction deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        long amount = Optional.ofNullable(node.get("amount"))
                .flatMap(getMinorUnits())
                .map(JsonNode::asLong)
                .orElseThrow(() -> ctxt.instantiationException(Transaction.class, "A numeric value for $.amount.minorUnits is required"));

        JsonNode field = Optional.ofNullable(node.get("direction"))
                .orElseThrow(() -> ctxt.instantiationException(Transaction.class, "A value has to be provided for $.direction"));

        // Allow Jackson to deserialize the enum.
        TransactionDirection transactionDirection = field.traverse(parser.getCodec())
                .readValueAs(TransactionDirection.class);
        return new Transaction(BigDecimal.valueOf(amount).movePointLeft(2), transactionDirection);
    }

    private Function<JsonNode, Optional<JsonNode>> getMinorUnits() {
        return node -> Optional.ofNullable(node.get("minorUnits"));
    }
}
