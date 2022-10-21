package edu.cornell.mannlib.vitro.webapp.dynapi.access;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class GroupAccessWhitelist implements AccessWhitelist {

    private static final Log log = LogFactory.getLog(GroupAccessWhitelist.class);
	private Map<String, UserGroup> groups = new HashMap<String, UserGroup>();
	private String actionName = "action name not provided";
	
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#userGroup")
    public void addAccessFilter(UserGroup userGroup) {
    	groups.put(userGroup.getName(), userGroup);
    }
	
	@Override
	public boolean isAuthorized(UserAccount user) {
		Set<String> uris = user.getPermissionSetUris();
		for (String uri : uris) {
			if (groups.containsKey(uri)) {
				UserGroup userGroup = groups.get(uri);
				log.debug("Group '" + userGroup.getLabel() + "' member '" + user.getEmailAddress() + "' is allowed to access action '" + actionName + "'");
				return true;
			}
		}
		log.debug("Whitelist is doesn't allow user '" + user.getEmailAddress() + "' to access action '" + actionName + "'");
		return false;
	}

	@Override
	public void setActionName(String name) {
		this.actionName  = name;
	}

}
