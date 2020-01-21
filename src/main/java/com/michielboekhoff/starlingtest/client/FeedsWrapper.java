package com.michielboekhoff.starlingtest.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.michielboekhoff.starlingtest.domain.Transaction;

import java.util.List;

class FeedsWrapper {
    private final List<Transaction> transactions;

    public FeedsWrapper(@JsonProperty(value = "feedItems", required = true) List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
