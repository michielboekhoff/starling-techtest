package com.michielboekhoff.starlingtest.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.michielboekhoff.starlingtest.domain.Account;

import java.util.List;

class AccountsWrapper {
    private final List<Account> accounts;

    public AccountsWrapper(@JsonProperty(value = "accounts", required = true) List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}
