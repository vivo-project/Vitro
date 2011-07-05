/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import static edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource.INTERNAL;
import static edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader.URI_CURATOR;
import static edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader.URI_DBA;
import static edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader.URI_EDITOR;
import static edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader.URI_SELF_EDITOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import stubs.edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.IndividualDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

/**
 * This test class is written to make it easy to create dozens of tests with
 * minimal code duplication. That way we can test that Individual A can be
 * viewed by Curators and DBAs but not by Editors or Self-Editors, etc., etc.,
 * ad infinitum.
 * 
 * The class is implemented as a Parameterized JUnit test class. The parameters
 * are TestData objects, and the class will invoke its single test for one.
 * 
 * Actually, each parameter is an instance of a TestData sub-class, and the test
 * method will decide how to handle the data based on the type.
 * 
 * This gets a little clearer if you start by looking at the section labeled
 * "the actual test class".
 */
@RunWith(value = Parameterized.class)
public class HiddenFromDisplayBelowRoleLevelFilterTest extends
		AbstractTestClass {
	private static final Log log = LogFactory
			.getLog(HiddenFromDisplayBelowRoleLevelFilterTest.class);

	// ----------------------------------------------------------------------
	// the "framework"
	// ----------------------------------------------------------------------

	/**
	 * Each test is driven by an instance of this (actually, of a sub-class).
	 */
	private static abstract class TestData {
		LoginStatusBean loginStatus;

		boolean expectedResult;

		public String getUserUri() {
			if (loginStatus == null) {
				return "nobody";
			} else {
				return loginStatus.getUserURI();
			}
		}

		public RoleLevel getRoleLevel() {
			return HiddenFromDisplayBelowRoleLevelFilterTest
					.getRoleLevel(loginStatus);
		}

		/** A detailed description of the test in case something goes wrong. */
		public abstract String describeTest();
	}

	// ----------------------------------------------------------------------
	// data elements
	// ----------------------------------------------------------------------

	private static final String NS = "http://someDomain/individual/";

	private static final UserAccount USER_SELF = userAccount("userSelf",
			"self_editor", URI_SELF_EDITOR);
	private static final UserAccount USER_EDITOR = userAccount("userEditor",
			"editor", URI_EDITOR);
	private static final UserAccount USER_CURATOR = userAccount("userCurator",
			"curator", URI_CURATOR);
	private static final UserAccount USER_DBA = userAccount(NS + "userDba",
			"dba", URI_DBA);

	/** Create a User */
	private static UserAccount userAccount(String uri, String emailAddress,
			String roleUri) {
		UserAccount user = new UserAccount();
		user.setUri(NS + uri);
		user.setEmailAddress(emailAddress);
		user.setPermissionSetUris(Collections.singleton(roleUri));
		return user;
	}

	private static final UserAccountsDaoStub DAO_USER_ACCOUNT = userAccountsDao(USER_SELF, USER_EDITOR,
			USER_CURATOR, USER_DBA);

	/** Create the UserAccountsDao */
	private static UserAccountsDaoStub userAccountsDao(UserAccount... users) {
		UserAccountsDaoStub dao = new UserAccountsDaoStub();
		for (UserAccount user : users) {
			dao.addUser(user);
		}
		return dao;
	}

	private static final LoginStatusBean LOGIN_NONE = null;
	private static final LoginStatusBean LOGIN_SELF = loginStatusBean(
			USER_SELF, INTERNAL);
	private static final LoginStatusBean LOGIN_EDITOR = loginStatusBean(
			USER_EDITOR, INTERNAL);
	private static final LoginStatusBean LOGIN_CURATOR = loginStatusBean(
			USER_CURATOR, INTERNAL);
	private static final LoginStatusBean LOGIN_DBA = loginStatusBean(USER_DBA,
			INTERNAL);

	private static LoginStatusBean loginStatusBean(UserAccount user,
			AuthenticationSource auth) {
		return new LoginStatusBean(user.getUri(), auth);
	}

	private static final LoginStatusBean[] LOGINS = new LoginStatusBean[] {
			LOGIN_NONE, LOGIN_SELF, LOGIN_EDITOR, LOGIN_CURATOR, LOGIN_DBA };

	private static final Individual PUBLIC_INDIVIDUAL = individual(
			"PUBLIC_individual", RoleLevel.PUBLIC);
	private static final Individual SELF_INDIVIDUAL = individual(
			"SELF_individual", RoleLevel.SELF);
	private static final Individual EDITOR_INDIVIDUAL = individual(
			"EDITOR_individual", RoleLevel.EDITOR);
	private static final Individual CURATOR_INDIVIDUAL = individual(
			"CURATOR_individual", RoleLevel.CURATOR);
	private static final Individual DBA_INDIVIDUAL = individual(
			"DBA_individual", RoleLevel.DB_ADMIN);
	private static final Individual HIDDEN_INDIVIDUAL = individual(
			"HIDDEN_individual", RoleLevel.NOBODY);

	private static final Individual[] INDIVIDUALS = new Individual[] {
			PUBLIC_INDIVIDUAL, SELF_INDIVIDUAL, EDITOR_INDIVIDUAL,
			CURATOR_INDIVIDUAL, DBA_INDIVIDUAL, HIDDEN_INDIVIDUAL };

	/** Create an Individual */
	private static Individual individual(String moniker,
			RoleLevel displayThreshhold) {
		Individual i = new IndividualImpl();
		i.setURI("uri:" + moniker);
		i.setHiddenFromDisplayBelowRoleLevel(displayThreshhold);
		return i;
	}

	private static final VClass PUBLIC_VCLASS = vClass("PUBLIC_vclass",
			RoleLevel.PUBLIC);
	private static final VClass SELF_VCLASS = vClass("SELF_vclass",
			RoleLevel.SELF);
	private static final VClass EDITOR_VCLASS = vClass("EDITOR_vclass",
			RoleLevel.EDITOR);
	private static final VClass CURATOR_VCLASS = vClass("CURATOR_vclass",
			RoleLevel.CURATOR);
	private static final VClass DBA_VCLASS = vClass("DBA_vclass",
			RoleLevel.DB_ADMIN);
	private static final VClass HIDDEN_VCLASS = vClass("HIDDEN_vclass",
			RoleLevel.NOBODY);

	/** Create a VClass */
	private static VClass vClass(String label, RoleLevel displayThreshhold) {
		VClass dp = new VClass();
		dp.setName(label);
		dp.setHiddenFromDisplayBelowRoleLevel(displayThreshhold);
		return dp;
	}

	private static final DataProperty PUBLIC_DATA_PROPERTY = dataProperty(
			"PUBLIC_data_property", RoleLevel.PUBLIC);
	private static final DataProperty SELF_DATA_PROPERTY = dataProperty(
			"SELF_data_property", RoleLevel.SELF);
	private static final DataProperty EDITOR_DATA_PROPERTY = dataProperty(
			"EDITOR_data_property", RoleLevel.EDITOR);
	private static final DataProperty CURATOR_DATA_PROPERTY = dataProperty(
			"CURATOR_data_property", RoleLevel.CURATOR);
	private static final DataProperty DBA_DATA_PROPERTY = dataProperty(
			"DBA_data_property", RoleLevel.DB_ADMIN);
	private static final DataProperty HIDDEN_DATA_PROPERTY = dataProperty(
			"HIDDEN_data_property", RoleLevel.NOBODY);

	private static final DataProperty[] DATA_PROPERTIES = new DataProperty[] {
			PUBLIC_DATA_PROPERTY, SELF_DATA_PROPERTY, EDITOR_DATA_PROPERTY,
			CURATOR_DATA_PROPERTY, DBA_DATA_PROPERTY, HIDDEN_DATA_PROPERTY };

	/** Create a DataProperty */
	private static DataProperty dataProperty(String label,
			RoleLevel displayThreshhold) {
		DataProperty dp = new DataProperty();
		dp.setLabel(label);
		dp.setURI("uri:" + label);
		dp.setHiddenFromDisplayBelowRoleLevel(displayThreshhold);
		return dp;
	}

	private static final ObjectProperty PUBLIC_OBJECT_PROPERTY = objectProperty(
			"PUBLIC_object_property", RoleLevel.PUBLIC);
	private static final ObjectProperty SELF_OBJECT_PROPERTY = objectProperty(
			"SELF_object_property", RoleLevel.SELF);
	private static final ObjectProperty EDITOR_OBJECT_PROPERTY = objectProperty(
			"EDITOR_object_property", RoleLevel.EDITOR);
	private static final ObjectProperty CURATOR_OBJECT_PROPERTY = objectProperty(
			"CURATOR_object_property", RoleLevel.CURATOR);
	private static final ObjectProperty DBA_OBJECT_PROPERTY = objectProperty(
			"DBA_object_property", RoleLevel.DB_ADMIN);
	private static final ObjectProperty HIDDEN_OBJECT_PROPERTY = objectProperty(
			"HIDDEN_object_property", RoleLevel.NOBODY);

	private static final ObjectProperty[] OBJECT_PROPERTIES = new ObjectProperty[] {
			PUBLIC_OBJECT_PROPERTY, SELF_OBJECT_PROPERTY,
			EDITOR_OBJECT_PROPERTY, CURATOR_OBJECT_PROPERTY,
			DBA_OBJECT_PROPERTY, HIDDEN_OBJECT_PROPERTY };

	/** Create a ObjectProperty */
	private static ObjectProperty objectProperty(String label,
			RoleLevel displayThreshhold) {
		ObjectProperty op = new ObjectProperty();
		op.setLabel(label);
		op.setURI("uri:" + label);
		op.setHiddenFromDisplayBelowRoleLevel(displayThreshhold);
		return op;
	}

	// ----------------------------------------------------------------------
	// the "tests"
	// ----------------------------------------------------------------------

	@Parameters
	public static Collection<Object[]> testData() {
		List<Object[]> tests = new ArrayList<Object[]>();

		/*
		 * tests for Individual filter
		 */
		// TODO:
		// HiddenFromDisplayBelowRoleLevelFilter.FILTER_ON_INDIVIDUAL_VCLASSES
		// is set to false. If it were true, we would need more tests:
		//
		// get the list of direct VClasses
		// if the list is null (maybe filtered out by a previous filter)
		// - test the VClass from individual.getVClassUri using the Class filter
		// else
		// - test each VClass from the list using the Class filter

		tests.add(testIndividual(PUBLIC_INDIVIDUAL, LOGIN_NONE, true));
		tests.add(testIndividual(PUBLIC_INDIVIDUAL, LOGIN_SELF, true));
		tests.add(testIndividual(PUBLIC_INDIVIDUAL, LOGIN_EDITOR, true));
		tests.add(testIndividual(PUBLIC_INDIVIDUAL, LOGIN_CURATOR, true));
		tests.add(testIndividual(PUBLIC_INDIVIDUAL, LOGIN_DBA, true));

		tests.add(testIndividual(SELF_INDIVIDUAL, LOGIN_NONE, false));
		tests.add(testIndividual(SELF_INDIVIDUAL, LOGIN_SELF, true));
		tests.add(testIndividual(SELF_INDIVIDUAL, LOGIN_EDITOR, true));
		tests.add(testIndividual(SELF_INDIVIDUAL, LOGIN_CURATOR, true));
		tests.add(testIndividual(SELF_INDIVIDUAL, LOGIN_DBA, true));

		tests.add(testIndividual(EDITOR_INDIVIDUAL, LOGIN_NONE, false));
		tests.add(testIndividual(EDITOR_INDIVIDUAL, LOGIN_SELF, false));
		tests.add(testIndividual(EDITOR_INDIVIDUAL, LOGIN_EDITOR, true));
		tests.add(testIndividual(EDITOR_INDIVIDUAL, LOGIN_CURATOR, true));
		tests.add(testIndividual(EDITOR_INDIVIDUAL, LOGIN_DBA, true));

		tests.add(testIndividual(CURATOR_INDIVIDUAL, LOGIN_NONE, false));
		tests.add(testIndividual(CURATOR_INDIVIDUAL, LOGIN_SELF, false));
		tests.add(testIndividual(CURATOR_INDIVIDUAL, LOGIN_EDITOR, false));
		tests.add(testIndividual(CURATOR_INDIVIDUAL, LOGIN_CURATOR, true));
		tests.add(testIndividual(CURATOR_INDIVIDUAL, LOGIN_DBA, true));

		tests.add(testIndividual(DBA_INDIVIDUAL, LOGIN_NONE, false));
		tests.add(testIndividual(DBA_INDIVIDUAL, LOGIN_SELF, false));
		tests.add(testIndividual(DBA_INDIVIDUAL, LOGIN_EDITOR, false));
		tests.add(testIndividual(DBA_INDIVIDUAL, LOGIN_CURATOR, false));
		tests.add(testIndividual(DBA_INDIVIDUAL, LOGIN_DBA, true));

		tests.add(testIndividual(HIDDEN_INDIVIDUAL, LOGIN_NONE, false));
		tests.add(testIndividual(HIDDEN_INDIVIDUAL, LOGIN_SELF, false));
		tests.add(testIndividual(HIDDEN_INDIVIDUAL, LOGIN_EDITOR, false));
		tests.add(testIndividual(HIDDEN_INDIVIDUAL, LOGIN_CURATOR, false));
		tests.add(testIndividual(HIDDEN_INDIVIDUAL, LOGIN_DBA, false));

		tests.add(testIndividual(null, LOGIN_NONE, false));
		tests.add(testIndividual(null, LOGIN_SELF, false));
		tests.add(testIndividual(null, LOGIN_EDITOR, false));
		tests.add(testIndividual(null, LOGIN_CURATOR, false));
		tests.add(testIndividual(null, LOGIN_DBA, true));

		/*
		 * tests for Class Filter
		 */
		tests.add(testVClass(PUBLIC_VCLASS, LOGIN_NONE, true));
		tests.add(testVClass(PUBLIC_VCLASS, LOGIN_SELF, true));
		tests.add(testVClass(PUBLIC_VCLASS, LOGIN_EDITOR, true));
		tests.add(testVClass(PUBLIC_VCLASS, LOGIN_CURATOR, true));
		tests.add(testVClass(PUBLIC_VCLASS, LOGIN_DBA, true));

		tests.add(testVClass(SELF_VCLASS, LOGIN_NONE, false));
		tests.add(testVClass(SELF_VCLASS, LOGIN_SELF, true));
		tests.add(testVClass(SELF_VCLASS, LOGIN_EDITOR, true));
		tests.add(testVClass(SELF_VCLASS, LOGIN_CURATOR, true));
		tests.add(testVClass(SELF_VCLASS, LOGIN_DBA, true));

		tests.add(testVClass(EDITOR_VCLASS, LOGIN_NONE, false));
		tests.add(testVClass(EDITOR_VCLASS, LOGIN_SELF, false));
		tests.add(testVClass(EDITOR_VCLASS, LOGIN_EDITOR, true));
		tests.add(testVClass(EDITOR_VCLASS, LOGIN_CURATOR, true));
		tests.add(testVClass(EDITOR_VCLASS, LOGIN_DBA, true));

		tests.add(testVClass(CURATOR_VCLASS, LOGIN_NONE, false));
		tests.add(testVClass(CURATOR_VCLASS, LOGIN_SELF, false));
		tests.add(testVClass(CURATOR_VCLASS, LOGIN_EDITOR, false));
		tests.add(testVClass(CURATOR_VCLASS, LOGIN_CURATOR, true));
		tests.add(testVClass(CURATOR_VCLASS, LOGIN_DBA, true));

		tests.add(testVClass(DBA_VCLASS, LOGIN_NONE, false));
		tests.add(testVClass(DBA_VCLASS, LOGIN_SELF, false));
		tests.add(testVClass(DBA_VCLASS, LOGIN_EDITOR, false));
		tests.add(testVClass(DBA_VCLASS, LOGIN_CURATOR, false));
		tests.add(testVClass(DBA_VCLASS, LOGIN_DBA, true));

		tests.add(testVClass(HIDDEN_VCLASS, LOGIN_NONE, false));
		tests.add(testVClass(HIDDEN_VCLASS, LOGIN_SELF, false));
		tests.add(testVClass(HIDDEN_VCLASS, LOGIN_EDITOR, false));
		tests.add(testVClass(HIDDEN_VCLASS, LOGIN_CURATOR, false));
		tests.add(testVClass(HIDDEN_VCLASS, LOGIN_DBA, false));

		tests.add(testVClass(null, LOGIN_NONE, false));
		tests.add(testVClass(null, LOGIN_SELF, false));
		tests.add(testVClass(null, LOGIN_EDITOR, false));
		tests.add(testVClass(null, LOGIN_CURATOR, false));
		tests.add(testVClass(null, LOGIN_DBA, true));

		/*
		 * tests for DataProperty filter
		 */
		tests.add(testDataProp(PUBLIC_DATA_PROPERTY, LOGIN_NONE, true));
		tests.add(testDataProp(PUBLIC_DATA_PROPERTY, LOGIN_SELF, true));
		tests.add(testDataProp(PUBLIC_DATA_PROPERTY, LOGIN_EDITOR, true));
		tests.add(testDataProp(PUBLIC_DATA_PROPERTY, LOGIN_CURATOR, true));
		tests.add(testDataProp(PUBLIC_DATA_PROPERTY, LOGIN_DBA, true));

		tests.add(testDataProp(SELF_DATA_PROPERTY, LOGIN_NONE, false));
		tests.add(testDataProp(SELF_DATA_PROPERTY, LOGIN_SELF, true));
		tests.add(testDataProp(SELF_DATA_PROPERTY, LOGIN_EDITOR, true));
		tests.add(testDataProp(SELF_DATA_PROPERTY, LOGIN_CURATOR, true));
		tests.add(testDataProp(SELF_DATA_PROPERTY, LOGIN_DBA, true));

		tests.add(testDataProp(EDITOR_DATA_PROPERTY, LOGIN_NONE, false));
		tests.add(testDataProp(EDITOR_DATA_PROPERTY, LOGIN_SELF, false));
		tests.add(testDataProp(EDITOR_DATA_PROPERTY, LOGIN_EDITOR, true));
		tests.add(testDataProp(EDITOR_DATA_PROPERTY, LOGIN_CURATOR, true));
		tests.add(testDataProp(EDITOR_DATA_PROPERTY, LOGIN_DBA, true));

		tests.add(testDataProp(CURATOR_DATA_PROPERTY, LOGIN_NONE, false));
		tests.add(testDataProp(CURATOR_DATA_PROPERTY, LOGIN_SELF, false));
		tests.add(testDataProp(CURATOR_DATA_PROPERTY, LOGIN_EDITOR, false));
		tests.add(testDataProp(CURATOR_DATA_PROPERTY, LOGIN_CURATOR, true));
		tests.add(testDataProp(CURATOR_DATA_PROPERTY, LOGIN_DBA, true));

		tests.add(testDataProp(DBA_DATA_PROPERTY, LOGIN_NONE, false));
		tests.add(testDataProp(DBA_DATA_PROPERTY, LOGIN_SELF, false));
		tests.add(testDataProp(DBA_DATA_PROPERTY, LOGIN_EDITOR, false));
		tests.add(testDataProp(DBA_DATA_PROPERTY, LOGIN_CURATOR, false));
		tests.add(testDataProp(DBA_DATA_PROPERTY, LOGIN_DBA, true));

		tests.add(testDataProp(HIDDEN_DATA_PROPERTY, LOGIN_NONE, false));
		tests.add(testDataProp(HIDDEN_DATA_PROPERTY, LOGIN_SELF, false));
		tests.add(testDataProp(HIDDEN_DATA_PROPERTY, LOGIN_EDITOR, false));
		tests.add(testDataProp(HIDDEN_DATA_PROPERTY, LOGIN_CURATOR, false));
		tests.add(testDataProp(HIDDEN_DATA_PROPERTY, LOGIN_DBA, false));

		tests.add(testDataProp(null, LOGIN_NONE, false));
		tests.add(testDataProp(null, LOGIN_SELF, false));
		tests.add(testDataProp(null, LOGIN_EDITOR, false));
		tests.add(testDataProp(null, LOGIN_CURATOR, false));
		tests.add(testDataProp(null, LOGIN_DBA, true));

		/*
		 * tests for ObjectProperty filter
		 */
		tests.add(testObjectProp(PUBLIC_OBJECT_PROPERTY, LOGIN_NONE, true));
		tests.add(testObjectProp(PUBLIC_OBJECT_PROPERTY, LOGIN_SELF, true));
		tests.add(testObjectProp(PUBLIC_OBJECT_PROPERTY, LOGIN_EDITOR, true));
		tests.add(testObjectProp(PUBLIC_OBJECT_PROPERTY, LOGIN_CURATOR, true));
		tests.add(testObjectProp(PUBLIC_OBJECT_PROPERTY, LOGIN_DBA, true));

		tests.add(testObjectProp(SELF_OBJECT_PROPERTY, LOGIN_NONE, false));
		tests.add(testObjectProp(SELF_OBJECT_PROPERTY, LOGIN_SELF, true));
		tests.add(testObjectProp(SELF_OBJECT_PROPERTY, LOGIN_EDITOR, true));
		tests.add(testObjectProp(SELF_OBJECT_PROPERTY, LOGIN_CURATOR, true));
		tests.add(testObjectProp(SELF_OBJECT_PROPERTY, LOGIN_DBA, true));

		tests.add(testObjectProp(EDITOR_OBJECT_PROPERTY, LOGIN_NONE, false));
		tests.add(testObjectProp(EDITOR_OBJECT_PROPERTY, LOGIN_SELF, false));
		tests.add(testObjectProp(EDITOR_OBJECT_PROPERTY, LOGIN_EDITOR, true));
		tests.add(testObjectProp(EDITOR_OBJECT_PROPERTY, LOGIN_CURATOR, true));
		tests.add(testObjectProp(EDITOR_OBJECT_PROPERTY, LOGIN_DBA, true));

		tests.add(testObjectProp(CURATOR_OBJECT_PROPERTY, LOGIN_NONE, false));
		tests.add(testObjectProp(CURATOR_OBJECT_PROPERTY, LOGIN_SELF, false));
		tests.add(testObjectProp(CURATOR_OBJECT_PROPERTY, LOGIN_EDITOR, false));
		tests.add(testObjectProp(CURATOR_OBJECT_PROPERTY, LOGIN_CURATOR, true));
		tests.add(testObjectProp(CURATOR_OBJECT_PROPERTY, LOGIN_DBA, true));

		tests.add(testObjectProp(DBA_OBJECT_PROPERTY, LOGIN_NONE, false));
		tests.add(testObjectProp(DBA_OBJECT_PROPERTY, LOGIN_SELF, false));
		tests.add(testObjectProp(DBA_OBJECT_PROPERTY, LOGIN_EDITOR, false));
		tests.add(testObjectProp(DBA_OBJECT_PROPERTY, LOGIN_CURATOR, false));
		tests.add(testObjectProp(DBA_OBJECT_PROPERTY, LOGIN_DBA, true));

		tests.add(testObjectProp(HIDDEN_OBJECT_PROPERTY, LOGIN_NONE, false));
		tests.add(testObjectProp(HIDDEN_OBJECT_PROPERTY, LOGIN_SELF, false));
		tests.add(testObjectProp(HIDDEN_OBJECT_PROPERTY, LOGIN_EDITOR, false));
		tests.add(testObjectProp(HIDDEN_OBJECT_PROPERTY, LOGIN_CURATOR, false));
		tests.add(testObjectProp(HIDDEN_OBJECT_PROPERTY, LOGIN_DBA, false));

		tests.add(testObjectProp(null, LOGIN_NONE, false));
		tests.add(testObjectProp(null, LOGIN_SELF, false));
		tests.add(testObjectProp(null, LOGIN_EDITOR, false));
		tests.add(testObjectProp(null, LOGIN_CURATOR, false));
		tests.add(testObjectProp(null, LOGIN_DBA, true));

		/*
		 * tests for DataPropertyStatementFilter
		 * 
		 * Generate most of these tests algorithmically. Use all combinations of
		 * roles for login, Individual and DataProperty.
		 */
		// TODO:
		// HiddenFromDisplayBelowRoleLevelFilter.FILTER_ON_INDIVIDUAL_VCLASSES
		// is set to false. If it were true, we would need more tests to check
		// whether the VClasses of both the subject and the predicate are
		// viewable.

		for (LoginStatusBean login : LOGINS) {
			for (Individual subject : INDIVIDUALS) {
				for (DataProperty predicate : DATA_PROPERTIES) {
					tests.add(testDataPropStmt(
							login,
							subject,
							predicate,
							expectedResultForDataPropertyStatement(login,
									subject, predicate)));
				}
			}
		}

		tests.add(testDataPropStmt(LOGIN_NONE, null, PUBLIC_DATA_PROPERTY,
				false));
		tests.add(testDataPropStmt(LOGIN_SELF, null, PUBLIC_DATA_PROPERTY,
				false));
		tests.add(testDataPropStmt(LOGIN_EDITOR, null, PUBLIC_DATA_PROPERTY,
				false));
		tests.add(testDataPropStmt(LOGIN_CURATOR, null, PUBLIC_DATA_PROPERTY,
				false));
		tests.add(testDataPropStmt(LOGIN_DBA, null, PUBLIC_DATA_PROPERTY, true));

		tests.add(testDataPropStmt(LOGIN_NONE, PUBLIC_INDIVIDUAL, null, false));
		tests.add(testDataPropStmt(LOGIN_SELF, PUBLIC_INDIVIDUAL, null, false));
		tests.add(testDataPropStmt(LOGIN_EDITOR, PUBLIC_INDIVIDUAL, null, false));
		tests.add(testDataPropStmt(LOGIN_CURATOR, PUBLIC_INDIVIDUAL, null,
				false));
		tests.add(testDataPropStmt(LOGIN_DBA, PUBLIC_INDIVIDUAL, null, true));

		/*
		 * tests for ObjectPropertyStatementFilter
		 * 
		 * Generate most of these tests algorithmically. Use all combinations of
		 * roles for login, Individual (twice) and DataProperty.
		 */
		// TODO:
		// HiddenFromDisplayBelowRoleLevelFilter.FILTER_ON_INDIVIDUAL_VCLASSES
		// is set to false. If it were true, we would need more tests to check
		// whether the VClasses of the subject, the predicate and the object are
		// viewable.
		for (LoginStatusBean login : LOGINS) {
			for (Individual subject : INDIVIDUALS) {
				for (Individual object : INDIVIDUALS) {
					for (ObjectProperty predicate : OBJECT_PROPERTIES) {
						boolean expectedResult = expectedResultForObjectPropertyStatement(
								login, subject, predicate, object);

						tests.add(testObjectPropStmt(login, subject, predicate,
								object, expectedResult));
					}
				}
			}
		}

		tests.add(testObjectPropStmt(LOGIN_NONE, null, PUBLIC_OBJECT_PROPERTY,
				PUBLIC_INDIVIDUAL, false));
		tests.add(testObjectPropStmt(LOGIN_SELF, null, PUBLIC_OBJECT_PROPERTY,
				PUBLIC_INDIVIDUAL, false));
		tests.add(testObjectPropStmt(LOGIN_EDITOR, null,
				PUBLIC_OBJECT_PROPERTY, PUBLIC_INDIVIDUAL, false));
		tests.add(testObjectPropStmt(LOGIN_CURATOR, null,
				PUBLIC_OBJECT_PROPERTY, PUBLIC_INDIVIDUAL, false));
		tests.add(testObjectPropStmt(LOGIN_DBA, null, PUBLIC_OBJECT_PROPERTY,
				PUBLIC_INDIVIDUAL, true));

		tests.add(testObjectPropStmt(LOGIN_NONE, PUBLIC_INDIVIDUAL, null,
				PUBLIC_INDIVIDUAL, false));
		tests.add(testObjectPropStmt(LOGIN_SELF, PUBLIC_INDIVIDUAL, null,
				PUBLIC_INDIVIDUAL, false));
		tests.add(testObjectPropStmt(LOGIN_EDITOR, PUBLIC_INDIVIDUAL, null,
				PUBLIC_INDIVIDUAL, false));
		tests.add(testObjectPropStmt(LOGIN_CURATOR, PUBLIC_INDIVIDUAL, null,
				PUBLIC_INDIVIDUAL, false));
		tests.add(testObjectPropStmt(LOGIN_DBA, PUBLIC_INDIVIDUAL, null,
				PUBLIC_INDIVIDUAL, true));

		tests.add(testObjectPropStmt(LOGIN_NONE, PUBLIC_INDIVIDUAL,
				PUBLIC_OBJECT_PROPERTY, null, false));
		tests.add(testObjectPropStmt(LOGIN_SELF, PUBLIC_INDIVIDUAL,
				PUBLIC_OBJECT_PROPERTY, null, false));
		tests.add(testObjectPropStmt(LOGIN_EDITOR, PUBLIC_INDIVIDUAL,
				PUBLIC_OBJECT_PROPERTY, null, false));
		tests.add(testObjectPropStmt(LOGIN_CURATOR, PUBLIC_INDIVIDUAL,
				PUBLIC_OBJECT_PROPERTY, null, false));
		tests.add(testObjectPropStmt(LOGIN_DBA, PUBLIC_INDIVIDUAL,
				PUBLIC_OBJECT_PROPERTY, null, true));

		return tests;
	}

	/**
	 * Assemble the test data to test the Individual filter.
	 */
	private static Object[] testIndividual(Individual individual,
			LoginStatusBean loginStatus, boolean expectedResult) {
		IndividualTestData data = new IndividualTestData();
		data.loginStatus = loginStatus;
		data.individual = individual;
		data.expectedResult = expectedResult;
		return new Object[] { data };
	}

	private static class IndividualTestData extends TestData {
		Individual individual;

		@Override
		public String describeTest() {
			String message = "IndividualTest, login=" + getRoleLevel() + "("
					+ getUserUri() + ")";
			if (individual == null) {
				message += ", individual=null";
			} else {
				message += ", individual=" + individual.getLocalName();
			}
			return message;
		}
	}

	/**
	 * Assemble the test data to test the VClass filter.
	 */
	private static Object[] testVClass(VClass dp, LoginStatusBean loginStatus,
			boolean expectedResult) {
		VClassTestData data = new VClassTestData();
		data.loginStatus = loginStatus;
		data.vClass = dp;
		data.expectedResult = expectedResult;
		return new Object[] { data };
	}

	private static class VClassTestData extends TestData {
		VClass vClass;

		@Override
		public String describeTest() {
			String message = "VClassTest, login=" + getRoleLevel() + "("
					+ getUserUri() + ")";
			if (vClass == null) {
				message += ", vClass=null";
			} else {
				message += ", vClass=" + vClass.getName();
			}
			return message;
		}
	}

	/**
	 * Assemble the test data to test the DataProperty filter.
	 */
	private static Object[] testDataProp(DataProperty dp,
			LoginStatusBean loginStatus, boolean expectedResult) {
		DataPropertyTestData data = new DataPropertyTestData();
		data.loginStatus = loginStatus;
		data.dataProperty = dp;
		data.expectedResult = expectedResult;
		return new Object[] { data };
	}

	private static class DataPropertyTestData extends TestData {
		DataProperty dataProperty;

		@Override
		public String describeTest() {
			String message = "DataPropertyTest, login=" + getRoleLevel() + "("
					+ getUserUri() + ")";
			if (dataProperty == null) {
				message += ", dataProperty=null";
			} else {
				message += ", dataProperty=" + dataProperty.getLabel();
			}
			return message;
		}
	}

	/**
	 * Assemble the test data to test the ObjectProperty filter.
	 */
	private static Object[] testObjectProp(ObjectProperty dp,
			LoginStatusBean loginStatus, boolean expectedResult) {
		ObjectPropertyTestData data = new ObjectPropertyTestData();
		data.loginStatus = loginStatus;
		data.objectProperty = dp;
		data.expectedResult = expectedResult;
		return new Object[] { data };
	}

	private static class ObjectPropertyTestData extends TestData {
		ObjectProperty objectProperty;

		@Override
		public String describeTest() {
			String message = "ObjectPropertyTest, login=" + getRoleLevel()
					+ "(" + getUserUri() + ")";
			if (objectProperty == null) {
				message += ", objectProperty=null";
			} else {
				message += ", objectProperty=" + objectProperty.getLabel();
			}
			return message;
		}
	}

	/**
	 * Assemble the test data to test the DataPropertyStatement filter.
	 */
	private static Object[] testDataPropStmt(LoginStatusBean login,
			Individual subject, DataProperty predicate, boolean expectedResult) {
		DataPropertyStatementTestData data = new DataPropertyStatementTestData();
		data.loginStatus = login;
		data.subject = subject;
		data.predicate = predicate;
		data.expectedResult = expectedResult;
		return new Object[] { data };
	}

	private static boolean expectedResultForDataPropertyStatement(
			LoginStatusBean login, Individual subject, DataProperty predicate) {
		RoleLevel loginRole = getRoleLevel(login);
		RoleLevel subjectRole = subject.getHiddenFromDisplayBelowRoleLevel();
		RoleLevel predicateRole = predicate
				.getHiddenFromDisplayBelowRoleLevel();

		// Is the effective login role at least as high as the threshhold roles
		// for both subject and predicate?
		return (loginRole.compareTo(subjectRole) >= 0)
				&& (loginRole.compareTo(predicateRole) >= 0);
	}

	private static class DataPropertyStatementTestData extends TestData {
		Individual subject;
		DataProperty predicate;

		@Override
		public String describeTest() {
			String message = "DataPropertyStatementTest, login="
					+ getRoleLevel() + "(" + getUserUri() + ")";

			if (subject == null) {
				message += ", subject=null";
			} else {
				message += ", subject=" + subject.getLocalName();
			}

			if (predicate == null) {
				message += ", predicate=null";
			} else {
				message += ", predicate=" + predicate.getLabel();
			}

			return message;
		}
	}

	/**
	 * Assemble the test data to test the ObjectPropertyStatement filter.
	 */
	private static Object[] testObjectPropStmt(LoginStatusBean login,
			Individual subject, ObjectProperty predicate, Individual object,
			boolean expectedResult) {
		ObjectPropertyStatementTestData data = new ObjectPropertyStatementTestData();
		data.loginStatus = login;
		data.subject = subject;
		data.predicate = predicate;
		data.object = object;
		data.expectedResult = expectedResult;
		return new Object[] { data };
	}

	private static boolean expectedResultForObjectPropertyStatement(
			LoginStatusBean login, Individual subject,
			ObjectProperty predicate, Individual object) {
		RoleLevel loginRole = getRoleLevel(login);
		RoleLevel subjectRole = subject.getHiddenFromDisplayBelowRoleLevel();
		RoleLevel predicateRole = predicate
				.getHiddenFromDisplayBelowRoleLevel();
		RoleLevel objectRole = object.getHiddenFromDisplayBelowRoleLevel();

		// Is the effective login role at least as high as the threshhold roles
		// for subject, object, and predicate?
		return (loginRole.compareTo(subjectRole) >= 0)
				&& (loginRole.compareTo(objectRole) >= 0)
				&& (loginRole.compareTo(predicateRole) >= 0);
	}

	private static class ObjectPropertyStatementTestData extends TestData {
		Individual subject;
		ObjectProperty predicate;
		Individual object;

		@Override
		public String describeTest() {
			String message = "ObjectPropertyStatementTest, login="
					+ getRoleLevel() + "(" + getUserUri() + ")";

			if (subject == null) {
				message += ", subject=null";
			} else {
				message += ", subject=" + subject.getLocalName();
			}

			if (predicate == null) {
				message += ", predicate=null";
			} else {
				message += ", predicate=" + predicate.getLabel();
			}

			if (object == null) {
				message += ", object=null";
			} else {
				message += ", object=" + object.getLocalName();
			}

			return message;
		}
	}

	public static RoleLevel getRoleLevel(LoginStatusBean loginStatus) {
		if (loginStatus == null) {
			return RoleLevel.PUBLIC;
		}

		String userUri = loginStatus.getUserURI();
		if (userUri == null) {
			return RoleLevel.PUBLIC;
		}

		UserAccount user = DAO_USER_ACCOUNT.getUserAccountByUri(userUri);
		if (user == null) {
			return RoleLevel.PUBLIC;
		}

		Set<String> roleUris = user.getPermissionSetUris();
		if (roleUris.contains(URI_DBA)) {
			return RoleLevel.DB_ADMIN; 
		} else 		if (roleUris.contains(URI_CURATOR)) {
			return RoleLevel.CURATOR; 
		} else 		if (roleUris.contains(URI_EDITOR)) {
			return RoleLevel.EDITOR; 
		} else 		if (roleUris.contains(URI_SELF_EDITOR)) {
			return RoleLevel.SELF; 
 		} else {
			return RoleLevel.PUBLIC;
		}
	}

	// ----------------------------------------------------------------------
	// the actual test class
	// ----------------------------------------------------------------------

	private final TestData testData;

	private WebappDaoFactoryStub wdf;
	private IndividualDaoStub indDao;
	private DataPropertyDaoStub dpDao;
	private ObjectPropertyDaoStub opDao;
	private HiddenFromDisplayBelowRoleLevelFilter filter;

	public HiddenFromDisplayBelowRoleLevelFilterTest(TestData testData) {
		this.testData = testData;
	}

	@Before
	public void setLoggingLevel() {
		// setLoggerLevel(this.getClass(), org.apache.log4j.Level.DEBUG);
		// setLoggerLevel(HiddenFromDisplayBelowRoleLevelFilter.class,
		// org.apache.log4j.Level.DEBUG);
	}

	@Before
	public void createFilter() {
		wdf = new WebappDaoFactoryStub();

		indDao = new IndividualDaoStub();
		wdf.setIndividualDao(indDao);

		dpDao = new DataPropertyDaoStub();
		wdf.setDataPropertyDao(dpDao);

		opDao = new ObjectPropertyDaoStub();
		wdf.setObjectPropertyDao(opDao);

		filter = new HiddenFromDisplayBelowRoleLevelFilter(
				this.testData.getRoleLevel(), wdf);
	}

	@Test
	public void runTest() throws Exception {
		try {
			if (testData instanceof IndividualTestData) {
				runIndividualTest();
			} else if (testData instanceof VClassTestData) {
				runVClassTest();
			} else if (testData instanceof DataPropertyTestData) {
				runDataPropertyTest();
			} else if (testData instanceof ObjectPropertyTestData) {
				runObjectPropertyTest();
			} else if (testData instanceof DataPropertyStatementTestData) {
				runDataPropertyStatementTest();
			} else if (testData instanceof ObjectPropertyStatementTestData) {
				runObjectPropertyStatementTest();
			} else {
				fail("Unrecognized test data: " + testData);
			}
		} catch (Exception e) {
			fail("Exception " + e + " in test: " + testData.describeTest());
		}
	}

	private void runIndividualTest() {
		IndividualTestData data = (IndividualTestData) this.testData;
		boolean expected = data.expectedResult;
		boolean actual = filter.getIndividualFilter().fn(data.individual);

		log.debug("expect '" + expected + "' for " + data.describeTest());
		assertEquals(data.describeTest(), expected, actual);
	}

	private void runVClassTest() {
		VClassTestData data = (VClassTestData) this.testData;
		boolean expected = data.expectedResult;
		boolean actual = filter.getClassFilter().fn(data.vClass);

		log.debug("expect '" + expected + "' for " + data.describeTest());
		assertEquals(data.describeTest(), expected, actual);
	}

	private void runDataPropertyTest() {
		DataPropertyTestData data = (DataPropertyTestData) this.testData;
		boolean expected = data.expectedResult;
		boolean actual = filter.getDataPropertyFilter().fn(data.dataProperty);

		log.debug("expect '" + expected + "' for " + data.describeTest());
		assertEquals(data.describeTest(), expected, actual);
	}

	private void runObjectPropertyTest() {
		ObjectPropertyTestData data = (ObjectPropertyTestData) this.testData;
		boolean expected = data.expectedResult;
		boolean actual = filter.getObjectPropertyFilter().fn(
				data.objectProperty);

		log.debug("expect '" + expected + "' for " + data.describeTest());
		assertEquals(data.describeTest(), expected, actual);
	}

	private void runDataPropertyStatementTest() {
		DataPropertyStatementTestData data = (DataPropertyStatementTestData) this.testData;

		DataPropertyStatement dps = new DataPropertyStatementImpl();
		dps.setIndividual(data.subject);
		if (data.subject != null) {
			dps.setIndividualURI(data.subject.getURI());
			indDao.addIndividual(data.subject);
		}

		if (data.predicate != null) {
			dps.setDatapropURI(data.predicate.getURI());
			dpDao.addDataProperty(data.predicate);
		}

		dps.setData("bogus data");

		boolean expected = data.expectedResult;
		boolean actual = filter.getDataPropertyStatementFilter().fn(dps);

		log.debug("expect '" + expected + "' for " + data.describeTest());
		assertEquals(data.describeTest(), expected, actual);
	}

	private void runObjectPropertyStatementTest() {
		ObjectPropertyStatementTestData data = (ObjectPropertyStatementTestData) this.testData;

		ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
		ops.setSubject(data.subject);
		if (data.subject != null) {
			ops.setSubjectURI(data.subject.getURI());
			indDao.addIndividual(data.subject);
		}

		ops.setProperty(data.predicate);
		if (data.predicate != null) {
			ops.setPropertyURI(data.predicate.getURI());
			opDao.addObjectProperty(data.predicate);
		}

		ops.setObject(data.object);
		if (data.object != null) {
			ops.setObjectURI(data.object.getURI());
			indDao.addIndividual(data.object);
		}

		boolean expected = data.expectedResult;
		boolean actual = filter.getObjectPropertyStatementFilter().fn(ops);

		log.debug("expect '" + expected + "' for " + data.describeTest());
		assertEquals(data.describeTest(), expected, actual);
	}
}
