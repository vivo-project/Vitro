package edu.cornell.mannlib.vitro.webapp.dynapi.access;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserAccessWhitelist implements AccessWhitelist {

    private static final Log log = LogFactory.getLog(UserAccessWhitelist.class);
    private Set<String> users = new HashSet<String>();

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#userEmail")
    public void addUserEmail(String email) {
        users.add(email);
    }

    @Override
    public boolean isAuthorized(UserAccount user, String procedureName) {
        String email = user.getEmailAddress();
        if (users.contains(email)) {
            log.debug("User '" + user.getEmailAddress() + "' allowed to access procedure '" + procedureName + "'");
            return true;
        }
        log.debug("User acccess whitelist is doesn't allow user '" +
                user.getEmailAddress() +
                "' to access procedure '" +
                procedureName +
                "'");
        return false;
    }
}
