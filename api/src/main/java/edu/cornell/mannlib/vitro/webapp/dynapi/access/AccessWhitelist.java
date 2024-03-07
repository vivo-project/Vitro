package edu.cornell.mannlib.vitro.webapp.dynapi.access;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

public interface AccessWhitelist {

    public boolean isAuthorized(UserAccount user, String procedureUri);

}
