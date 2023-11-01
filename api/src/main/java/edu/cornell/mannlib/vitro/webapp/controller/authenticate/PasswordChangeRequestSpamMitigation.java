package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PasswordChangeRequestSpamMitigation {

    private static final Map<String, LocalDateTime> requestHistory = new HashMap<>();

    private static final Map<String, Integer> requestFrequency = new HashMap<>();

    private static final long INTERVAL_INCREASE_MINUTES = 10;


    private static void initializeHistoryRequestDataIfNotExists(String emailAddress) {
        if (requestHistory.containsKey(emailAddress)) {
            return;
        }

        requestHistory.put(emailAddress, LocalDateTime.now());
        requestFrequency.put(emailAddress, 0);
    }

    public static PasswordChangeRequestSpamMitigationResponse isPasswordResetRequestable(String emailAddress) {
        initializeHistoryRequestDataIfNotExists(emailAddress);

        Integer numberOfSuccessiveRequests = requestFrequency.get(emailAddress);
        LocalDateTime momentOfFirstRequest = requestHistory.get(emailAddress);
        LocalDateTime nextRequestAvailableAt =
            momentOfFirstRequest.plusMinutes(numberOfSuccessiveRequests * INTERVAL_INCREASE_MINUTES);

        if (nextRequestAvailableAt.isAfter(LocalDateTime.now())) {
            String[] dateTimeTokens = nextRequestAvailableAt.toString().split("T");
            String dateString = dateTimeTokens[0];
            String timeString = dateTimeTokens[1].split("\\.")[0];
            return new PasswordChangeRequestSpamMitigationResponse(false, dateString, timeString);
        }

        if (numberOfSuccessiveRequests > 0) {
            requestHistory.put(emailAddress, LocalDateTime.now());
        }

        return new PasswordChangeRequestSpamMitigationResponse(true);
    }

    public static void requestSuccessfullyHandledAndUserIsNotified(String email) {
        requestFrequency.computeIfPresent(email, (key, value) -> ++value);
    }

    public static void requestSuccessfullyHandledAndUserPasswordUpdated(String email) {
        requestHistory.remove(email);
        requestFrequency.remove(email);
    }
}
