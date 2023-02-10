package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.GroupAccessWhitelist;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.UserAccessWhitelist;
import edu.cornell.mannlib.vitro.webapp.dynapi.access.UserGroup;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.BooleanParam;

public class ProcedureTest {

   private static final String OUTPUT = "output";
private static final String INPUT = "input";
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
   
   @Test
   public void simpleParamTest() {
       try (Procedure procedure = new Procedure()) {
           OperationalStep step = new OperationalStep();
           BooleanParam inputParam = new BooleanParam(INPUT);
           BooleanParam outputParam = new BooleanParam(OUTPUT);
           AssignOperation ao = new AssignOperation();
           ao.addInputParameter(inputParam);
           ao.addOutputParameter(outputParam);
           procedure.setStep(step);
           procedure.addProvidedParameter(outputParam);           
           step.setOperation(ao);
           procedure.setUri("test_rpc");
           procedure.setPublicAccess(true);
           DataStore store = new DataStore();
           Data inputData = new Data(inputParam);
           TestView.setObject(inputData, true);
           store.addData(INPUT, inputData);
           assertTrue(procedure.isValid());
           assertEquals(OperationResult.ok(),procedure.run(store));
           assertTrue(store.contains(OUTPUT));
           Data outputData = store.getData(OUTPUT);
           assertEquals("true",outputData.getSerializedValue());
       }
   }
   
   @Test
   public void optionalParamTest() throws ConversionException {
       try (Procedure procedure = new Procedure()) {
           OperationalStep step = new OperationalStep();
           BooleanParam inputParam = new BooleanParam(INPUT);
           inputParam.setOptional(true);
           BooleanParam outputParam = new BooleanParam(OUTPUT);
           AssignOperation ao = new AssignOperation();
           ao.addInputParameter(inputParam);
           ao.addOutputParameter(outputParam);
           procedure.setStep(step);
           procedure.addProvidedParameter(outputParam);           
           step.setOperation(ao);
           procedure.setUri("test_rpc");
           procedure.setPublicAccess(true);
           DataStore store = new DataStore();
           assertTrue(procedure.isValid());
           Converter.convertInternalParams(procedure.getInternalParams(), store);
           assertEquals(1, procedure.getOptionalParams().size());
           assertEquals(OperationResult.ok(),procedure.run(store));
           assertTrue(store.contains(OUTPUT));
           Data outputData = store.getData(OUTPUT);
           assertEquals("false",outputData.getSerializedValue());
           
           //Provide optional param
           store = new DataStore();
           Data inputData = new Data(inputParam);
           TestView.setObject(inputData, true);
           store.addData(INPUT, inputData);
           assertEquals(OperationResult.ok(),procedure.run(store));
           assertTrue(store.contains(OUTPUT));
           outputData = store.getData(OUTPUT);
           assertEquals("true",outputData.getSerializedValue());
       }
   }
}
