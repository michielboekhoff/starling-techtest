package com.michielboekhoff.starlingtest.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.michielboekhoff.starlingtest.deserializer.TransactionDeserializer;

import java.math.BigDecimal;

@JsonDeserialize(using = TransactionDeserializer.class)
public class Transaction {

    private final BigDecimal amount;

    private final TransactionDirection transactionDirection;

    public Transaction(BigDecimal amount, TransactionDirection transactionDirection) {
        this.amount = amount;
        this.transactionDirection = transactionDirection;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionDirection getTransactionDirection() {
        return transactionDirection;
    }

    public enum TransactionDirection {
        OUT, IN
    }
}
