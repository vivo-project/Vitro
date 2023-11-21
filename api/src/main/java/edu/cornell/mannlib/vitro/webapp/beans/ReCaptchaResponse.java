package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Date;

public class ReCaptchaResponse {

    private boolean success;

    private Date challenge_ts;

    private String hostname;

    public ReCaptchaResponse() {
    }

    public ReCaptchaResponse(boolean success, Date challenge_ts, String hostname) {
        this.success = success;
        this.challenge_ts = challenge_ts;
        this.hostname = hostname;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Date getChallenge_ts() {
        return challenge_ts;
    }

    public void setChallenge_ts(Date challenge_ts) {
        this.challenge_ts = challenge_ts;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "ReCaptchaResponse{" +
            "success=" + success +
            ", challenge_ts=" + challenge_ts +
            ", hostname='" + hostname + '\'' +
            '}';
    }
}
