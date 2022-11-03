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
			UserGroup userGroup = groups.get(uri);
			if (groups.containsKey(uri)) {
				log.debug("Group '" + userGroup.getLabel() + "' membership allows '" + user.getEmailAddress() + "' to access action '" + actionName + "'");
				return true;
			}
			log.debug("Group '" + userGroup.getLabel() + "' membership doesn't allow '" + user.getEmailAddress() + "' to access action '" + actionName + "'");
		}
		log.debug("Whitelist is doesn't allow user '" + user.getEmailAddress() + "' to access action '" + actionName + "'");
		return false;
	}

	@Override
	public void setActionName(String name) {
		this.actionName  = name;
	}

}
