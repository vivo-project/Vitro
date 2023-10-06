package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

public class PasswordChangeRequestSpamMitigationResponse {

    private Boolean canBeRequested;

    private String nextRequestAvailableAtDate;

    private String nextRequestAvailableAtTime;


    public PasswordChangeRequestSpamMitigationResponse(Boolean canBeRequested, String nextRequestAvailableAtDate,
                                                       String nextRequestAvailableAtTime) {
        this.canBeRequested = canBeRequested;
        this.nextRequestAvailableAtDate = nextRequestAvailableAtDate;
        this.nextRequestAvailableAtTime = nextRequestAvailableAtTime;
    }

    public PasswordChangeRequestSpamMitigationResponse(Boolean canBeRequested) {
        this.canBeRequested = canBeRequested;
    }

    public Boolean getCanBeRequested() {
        return canBeRequested;
    }

    public void setCanBeRequested(Boolean canBeRequested) {
        this.canBeRequested = canBeRequested;
    }

    public String getNextRequestAvailableAtDate() {
        return nextRequestAvailableAtDate;
    }

    public void setNextRequestAvailableAtDate(String nextRequestAvailableAtDate) {
        this.nextRequestAvailableAtDate = nextRequestAvailableAtDate;
    }

    public String getNextRequestAvailableAtTime() {
        return nextRequestAvailableAtTime;
    }

    public void setNextRequestAvailableAtTime(String nextRequestAvailableAtTime) {
        this.nextRequestAvailableAtTime = nextRequestAvailableAtTime;
    }
}
