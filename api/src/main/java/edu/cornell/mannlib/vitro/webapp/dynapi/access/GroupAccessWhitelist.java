/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.access;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GroupAccessWhitelist implements AccessWhitelist {

    private static final Log log = LogFactory.getLog(GroupAccessWhitelist.class);
    private Map<String, UserGroup> groups = new HashMap<String, UserGroup>();

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#userGroup")
    public void addAccessFilter(UserGroup userGroup) {
        groups.put(userGroup.getName(), userGroup);
    }

    @Override
    public boolean isAuthorized(UserAccount user, String procedureName) {
        Set<String> uris = user.getPermissionSetUris();
        for (String uri : uris) {
            UserGroup userGroup = groups.get(uri);
            if (groups.containsKey(uri)) {
                log.debug("Group '" +
                        userGroup.getLabel() +
                        "' membership allows '" +
                        user.getEmailAddress() +
                        "' to access procedure '" +
                        procedureName +
                        "'");
                return true;
            }
            log.debug("Group '" +
                    userGroup.getLabel() +
                    "' membership doesn't allow '" +
                    user.getEmailAddress() +
                    "' to access procedure '" +
                    procedureName +
                    "'");
        }
        log.debug("Whitelist is doesn't allow user '" +
                user.getEmailAddress() +
                "' to access procedure '" +
                procedureName +
                "'");
        return false;
    }
}
