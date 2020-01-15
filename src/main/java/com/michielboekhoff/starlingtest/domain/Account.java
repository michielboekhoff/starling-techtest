package com.michielboekhoff.starlingtest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Account {

    private final String accountUid;
    private final String defaultCategory;

    public Account(@JsonProperty(value = "accountUid", required = true) String accountUid,
                   @JsonProperty(value = "defaultCategory", required = true) String defaultCategory) {
        this.accountUid = accountUid;
        this.defaultCategory = defaultCategory;
    }

    public String getAccountUid() {
        return accountUid;
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }
}
