package com.michielboekhoff.starlingtest.domain;

import java.math.BigDecimal;

public class Transaction {

    private final BigDecimal amount;

    public Transaction(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
