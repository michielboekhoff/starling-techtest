package com.michielboekhoff.starlingtest.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SavingsGoalTransfer {

    @JsonProperty("amount")
    private final Amount amount;

    public SavingsGoalTransfer(Amount amount) {
        this.amount = amount;
    }

    public static class Amount {
        @JsonProperty("currency")
        private final String currency;

        @JsonProperty("minorUnits")
        private final long minorUnits;

        public Amount(String currency, long minorUnits) {
            this.currency = currency;
            this.minorUnits = minorUnits;
        }
    }
}
