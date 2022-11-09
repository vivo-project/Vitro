package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.GroupAccessWhitelist;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.UserAccessWhitelist;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.UserGroup;

public class ActionTest {

	private static final String USER_GROUP_URI = "user_group_uri";

	@Test
	public void testDefaultAccess() {
		Action action = new Action();
		UserAccount user = new UserAccount();
		assertFalse(action.hasPermissions(user));
		user.setRootUser(true);
		assertTrue(action.hasPermissions(user));
	}

	@Test
	public void testPublicAccess() {
		Action action = new Action();
		action.setPublicAccess(true);
		UserAccount user = new UserAccount();
		assertTrue(action.hasPermissions(user));
	}

	@Test
	public void testGroupAccess() {
		Action action = new Action();
		RPC rpc = new RPC();
		rpc.setName("test_rpc");
		action.setRPC(rpc);
		GroupAccessWhitelist groupFilter = new GroupAccessWhitelist();
		UserAccount user = new UserAccount();
		UserGroup group = new UserGroup();
		group.setLabel("Users");
		group.setName(USER_GROUP_URI);
		groupFilter.addAccessFilter(group);
		action.addAccessFilter(groupFilter);
		assertFalse(action.hasPermissions(user));
		user.setPermissionSetUris(Collections.singletonList(USER_GROUP_URI));
		assertTrue(action.hasPermissions(user));
	}
	
	@Test
	public void testUserAccess() {
		Action action = new Action();
		RPC rpc = new RPC();
		rpc.setName("test_rpc");
		action.setRPC(rpc);
		UserAccessWhitelist userFilter = new UserAccessWhitelist();
		userFilter.addUserEmail("user_email@example.com");
		UserAccount user = new UserAccount();
		action.addAccessFilter(userFilter);
		assertFalse(action.hasPermissions(user));
		user.setEmailAddress("user_email@example.com");
		assertTrue(action.hasPermissions(user));
	}
}
