package com.michielboekhoff.starlingtest;

import com.michielboekhoff.starlingtest.client.ApiClient;
import com.michielboekhoff.starlingtest.client.ApiException;
import com.michielboekhoff.starlingtest.service.RoundupService;

public class Main {

    private static final String BASE_URL = "https://api-sandbox.starlingbank.com";

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: java -jar starling.jar <API TOKEN> <SAVINGS GOAL UID>");
        }

        String apiToken = args[0];
        String savingsGoalUid = args[1];
        ApiClient apiClient = new ApiClient(BASE_URL, apiToken);
        RoundupService roundupService = new RoundupService(apiClient);

        try {
            roundupService.roundUpTransactionsFromLastWeekIntoSavingsGoal(savingsGoalUid);
        } catch (ApiException apiException) {
            System.out.println("Could not retrieve information from Starling API, cause: " + apiException.getMessage());
        }
    }
}
