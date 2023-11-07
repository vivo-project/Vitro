package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PasswordChangeRequestSpamMitigation {

    private static final Map<String, LocalDateTime> requestHistory = new ConcurrentHashMap<>();

    private static final Map<String, Integer> requestFrequency = new ConcurrentHashMap<>();

    private static final long INTERVAL_INCREASE_MINUTES = 10;


    /**
     * Initializes request history and frequency data for a given email address if it does not exist.
     *
     * @param emailAddress The email address for which to initialize request history and frequency data.
     */
    private static void initializeHistoryRequestDataIfNotExists(String emailAddress) {
        requestHistory.putIfAbsent(emailAddress, LocalDateTime.now());
        requestFrequency.putIfAbsent(emailAddress, 0);
    }

    /**
     * Determines whether a password reset request is allowed based on spam mitigation criteria.
     *
     * @param emailAddress The email address associated with the password reset request.
     * @return A PasswordChangeRequestSpamMitigationResponse indicating if the request is allowed.
     */
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

    /**
     * Updates request frequency and timestamp when a password reset request is successfully handled,
     * and the user is notified.
     *
     * @param email The email address for which the request was successfully handled.
     */
    public static void requestSuccessfullyHandledAndUserIsNotified(String email) {
        requestFrequency.merge(email, 1, Integer::sum);
    }


    /**
     * Removes request history and frequency data when a password reset request is successfully handled,
     * and the user's password is updated.
     *
     * @param email The email address for which the request was successfully handled.
     */
    public static void requestSuccessfullyHandledAndUserPasswordUpdated(String email) {
        requestHistory.remove(email);
        requestFrequency.remove(email);
    }
}
