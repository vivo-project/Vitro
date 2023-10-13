package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

public class PasswordChangeRequestSpamMitigation {

    private static final Map<String, LocalDateTime> requestHistory = new HashMap<>();

    private static final Map<String, Integer> requestFrequency = new HashMap<>();

    private static final long INTERVAL_INCREASE_MINUTES = 1;

    private static boolean initializeHistoryRequestDataIfNotExists(String emailAddress) {
        if (requestHistory.containsKey(emailAddress)) {
            return false;
        }

        requestHistory.put(emailAddress, LocalDateTime.now());
        requestFrequency.put(emailAddress, 0);
        return true;
    }

    public static PasswordChangeRequestSpamMitigationResponse isPasswordResetRequestable(UserAccount userAccount) {
        boolean justInitialised = initializeHistoryRequestDataIfNotExists(userAccount.getEmailAddress());

        Integer numberOfSuccessiveRequests = requestFrequency.get(userAccount.getEmailAddress());
        LocalDateTime momentOfFirstRequest = requestHistory.get(userAccount.getEmailAddress());
        LocalDateTime nextRequestAvailableAt =
            momentOfFirstRequest.plusMinutes(numberOfSuccessiveRequests * INTERVAL_INCREASE_MINUTES);

        if (nextRequestAvailableAt.isAfter(LocalDateTime.now())) {
            String[] dateTimeTokens = nextRequestAvailableAt.toString().split("T");
            String dateString = dateTimeTokens[0];
            String timeString = dateTimeTokens[1].split("\\.")[0];
            return new PasswordChangeRequestSpamMitigationResponse(false, dateString, timeString);
        }

        if (numberOfSuccessiveRequests > 0) {
            requestHistory.put(userAccount.getEmailAddress(), LocalDateTime.now());
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
