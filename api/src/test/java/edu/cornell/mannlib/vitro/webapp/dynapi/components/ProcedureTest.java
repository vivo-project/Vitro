package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.GroupAccessWhitelist;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.UserAccessWhitelist;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.UserGroup;

public class ProcedureTest {

   private static final String USER_GROUP_URI = "user_group_uri";

   @Test
   public void testDefaultAccess() {
       try(Procedure procedure = new Procedure()){
           UserAccount user = new UserAccount();
           assertFalse(procedure.hasPermissions(user));
           user.setRootUser(true);
           assertTrue(procedure.hasPermissions(user));
       }
   }

   @Test
   public void testPublicAccess() {
       try (Procedure procedure = new Procedure()){
           procedure.setPublicAccess(true);
           UserAccount user = new UserAccount();
           assertTrue(procedure.hasPermissions(user));
       }
   }

   @Test
   public void testGroupAccess() {
       try (Procedure procedure = new Procedure()){
           procedure.setUri("test_rpc");
           GroupAccessWhitelist groupFilter = new GroupAccessWhitelist();
           UserAccount user = new UserAccount();
           UserGroup group = new UserGroup();
           group.setLabel("Users");
           group.setName(USER_GROUP_URI);
           groupFilter.addAccessFilter(group);
           procedure.addAccessFilter(groupFilter);
           assertFalse(procedure.hasPermissions(user));
           user.setPermissionSetUris(Collections.singletonList(USER_GROUP_URI));
           assertTrue(procedure.hasPermissions(user));
       }
   }
   
   @Test
   public void testUserAccess() {
       try (Procedure procedure = new Procedure()) {
           procedure.setUri("test_rpc");
           UserAccessWhitelist userFilter = new UserAccessWhitelist();
           userFilter.addUserEmail("user_email@example.com");
           UserAccount user = new UserAccount();
           procedure.addAccessFilter(userFilter);
           assertFalse(procedure.hasPermissions(user));
           user.setEmailAddress("user_email@example.com");
           assertTrue(procedure.hasPermissions(user));
       }
   }
}
