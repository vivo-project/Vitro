/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import static edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.DEFAULT_ORDERING;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Direction;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Field;

public class UserAccountsSelectorTest extends AbstractTestClass {
	/**
	 * Where the model statements are stored for this test.
	 */
	private static final String N3_DATA_FILENAME = "UserAccountsSelectorTest.n3";

	private static final String NS_MINE = "http://vivo.mydomain.edu/individual/";

	private static OntModel ontModel;

	@BeforeClass
	public static void setupModel() throws IOException {
		InputStream stream = UserAccountsSelectorTest.class
				.getResourceAsStream(N3_DATA_FILENAME);
		Model model = ModelFactory.createDefaultModel();
		model.read(stream, null, "N3");
		stream.close();

		ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,
				model);
		ontModel.prepare();
	}

	private UserAccountsSelection selection;
	private UserAccountsSelectionCriteria criteria;

	// ----------------------------------------------------------------------
	// exceptions tests
	// ----------------------------------------------------------------------

	@Test(expected = NullPointerException.class)
	public void modelIsNull() {
		UserAccountsSelector.select(null,
				criteria(10, 1, DEFAULT_ORDERING, "", ""));
	}

	@Test(expected = NullPointerException.class)
	public void criteriaIsNull() {
		UserAccountsSelector.select(ontModel, null);
	}

	// ----------------------------------------------------------------------
	// fields tests
	// ----------------------------------------------------------------------

	@Test
	public void checkAllFields() {
		selectOnCriteria(1, 10, DEFAULT_ORDERING, "", "");
		assertSelectedUris(10, "user10");

		UserAccount acct = selection.getUserAccounts().get(0);
		assertEquals("uri", "http://vivo.mydomain.edu/individual/user10",
				acct.getUri());
		assertEquals("email", "email@jones.edu", acct.getEmailAddress());
		assertEquals("firstName", "Bob", acct.getFirstName());
		assertEquals("lastName", "Caruso", acct.getLastName());
		assertEquals("password", "garbage", acct.getMd5Password());
		assertEquals("expires", 1100234965897L, acct.getPasswordLinkExpires());
		assertEquals("loginCount", 50, acct.getLoginCount());
		assertEquals("lastLogin", 1020304050607080L, acct.getLastLoginTime());
		assertEquals("status", UserAccount.Status.ACTIVE, acct.getStatus());
		assertEqualSets(
				"permissions",
				Collections
						.singleton("http://vivo.mydomain.edu/individual/role2"),
				acct.getPermissionSetUris());
		assertEquals("rootUser", false, acct.isRootUser());
	}

	@Test
	public void checkFieldsForRootUser() {
		selectOnCriteria(1, 8, DEFAULT_ORDERING, "", "");
		assertSelectedUris(10, "user08");

		UserAccount acct = selection.getUserAccounts().get(0);
		assertEquals("uri", "http://vivo.mydomain.edu/individual/user08",
				acct.getUri());
		assertEquals("email", "email@henry.edu", acct.getEmailAddress());
		assertEquals("firstName", "Mary", acct.getFirstName());
		assertEquals("lastName", "McInerney", acct.getLastName());
		assertEquals("password", "garbage", acct.getMd5Password());
		assertEquals("expires", 0L, acct.getPasswordLinkExpires());
		assertEquals("loginCount", 7, acct.getLoginCount());
		assertEquals("lastLogin", 1122334455667788L, acct.getLastLoginTime());
		assertEquals("status", UserAccount.Status.ACTIVE, acct.getStatus());
		assertEqualSets("permissions", Collections.<String> emptySet(),
				acct.getPermissionSetUris());
		assertEquals("rootUser", true, acct.isRootUser());
	}

	// ----------------------------------------------------------------------
	// pagination tests
	// ----------------------------------------------------------------------

	@Test
	public void showFirstPageOfFifteen() {
		selectOnCriteria(15, 1, DEFAULT_ORDERING, "", "");
		assertSelectedUris(10, "user01", "user02", "user03", "user04",
				"user05", "user06", "user07", "user08", "user09", "user10");
	}

	@Test
	public void showFirstPageOfOne() {
		selectOnCriteria(1, 1, DEFAULT_ORDERING, "", "");
		assertSelectedUris(10, "user01");
	}

	@Test
	public void showFirstPageOfFive() {
		selectOnCriteria(5, 1, DEFAULT_ORDERING, "", "");
		assertSelectedUris(10, "user01", "user02", "user03", "user04", "user05");
	}

	@Test
	public void showSecondPageOfSeven() {
		selectOnCriteria(7, 2, DEFAULT_ORDERING, "", "");
		assertSelectedUris(10, "user08", "user09", "user10");
	}

	@Test
	public void showTenthPageOfThree() {
		selectOnCriteria(3, 10, DEFAULT_ORDERING, "", "");
		assertSelectedUris(10);
	}

	// ----------------------------------------------------------------------
	// sorting tests
	// ----------------------------------------------------------------------

	@Test
	public void sortByEmailAscending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(Field.EMAIL,
				Direction.ASCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		assertSelectedUris(10, "user01", "user02", "user03");
	}

	@Test
	public void sortByEmailDescending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(Field.EMAIL,
				Direction.DESCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		assertSelectedUris(10, "user10", "user09", "user08");
	}

	@Test
	public void sortByFirstNameAscending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(
				Field.FIRST_NAME, Direction.ASCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		// user02 has no first name: collates as least value.
		assertSelectedUris(10, "user02", "user10", "user09");
	}

	@Test
	public void sortByFirstNameDescending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(
				Field.FIRST_NAME, Direction.DESCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		// user02 has no first name: collates as least value.
		assertSelectedUris(10, "user01", "user03", "user04");
	}

	@Test
	public void sortByLastNameAscending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(
				Field.LAST_NAME, Direction.ASCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		// user03 has no last name: collates as least value.
		assertSelectedUris(10, "user03", "user05", "user09");
	}

	@Test
	public void sortByLastNameDescending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(
				Field.LAST_NAME, Direction.DESCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		assertSelectedUris(10, "user06", "user07", "user01");
	}

	@Test
	public void sortByStatusAscending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(Field.STATUS,
				Direction.ASCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		// user07 has no status: collates as least value.
		assertSelectedUris(10, "user07", "user01", "user04");
	}

	@Test
	public void sortByStatusDescending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(Field.STATUS,
				Direction.DESCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		assertSelectedUris(10, "user02", "user03", "user06");
	}

	@Test
	public void sortByLoginCountAscending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(
				Field.LOGIN_COUNT, Direction.ASCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		// user06 has no login count: reads as 0.
		assertSelectedUris(10, "user06", "user03", "user07");
	}

	@Test
	public void sortByLoginCountDescending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(
				Field.LOGIN_COUNT, Direction.DESCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		assertSelectedUris(10, "user10", "user04", "user08");
	}

	@Test
	public void sortByLastLoginTimeAscending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(
				Field.LAST_LOGIN_TIME, Direction.ASCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		// user06 has no login count: reads as 0.
		assertSelectedUris(10, "user07", "user03", "user06");
	}
	
	@Test
	public void sortByLastLoginTimeDescending() {
		UserAccountsOrdering orderBy = new UserAccountsOrdering(
				Field.LAST_LOGIN_TIME, Direction.DESCENDING);
		selectOnCriteria(3, 1, orderBy, "", "");
		assertSelectedUris(10, "user08", "user10", "user09");
	}
	
	// ----------------------------------------------------------------------
	// filtering tests
	// ----------------------------------------------------------------------

	@Test
	public void filterAgainstRole1() {
		selectOnCriteria(20, 1, DEFAULT_ORDERING, NS_MINE + "role1", "");
		assertSelectedUris(6, "user01", "user02", "user03", "user05", "user06",
				"user09");
	}

	@Test
	public void filterAgainstRole2() {
		selectOnCriteria(20, 1, DEFAULT_ORDERING, NS_MINE + "role2", "");
		assertSelectedUris(2, "user03", "user10");
	}
	
	@Test
	public void filterAgainstNoSuchRole() {
		selectOnCriteria(20, 1, DEFAULT_ORDERING, "BogusRole", "");
		assertSelectedUris(0);
	}

	// ----------------------------------------------------------------------
	// search tests
	// ----------------------------------------------------------------------

	@Test
	public void searchTermFoundInAllThreeFields() {
		selectOnCriteria(20, 1, DEFAULT_ORDERING, "", "bob");
		assertSelectedUris(3, "user02", "user05", "user10");
	}

	@Test
	public void searchTermNotFound() {
		selectOnCriteria(20, 1, DEFAULT_ORDERING, "", "bogus");
		assertSelectedUris(0);
	}

	/**
	 * If the special characters were allowed into the Regex, this would have 3
	 * matches. If they are escaped properly, it will have none.
	 */
	@Test
	public void searchTermContainsSpecialRegexCharacters() {
		selectOnCriteria(20, 1, DEFAULT_ORDERING, "", "b.b");
		assertSelectedUris(0);
	}

	// ----------------------------------------------------------------------
	// combination tests
	// ----------------------------------------------------------------------

	@Test
	public void searchWithFilter() {
		selectOnCriteria(20, 1, DEFAULT_ORDERING, NS_MINE + "role1", "bob");
		assertSelectedUris(2, "user02", "user05");
	}

	@Test
	public void searchWithFilterPaginatedWithFunkySortOrder() {
		selectOnCriteria(1, 2, new UserAccountsOrdering(Field.STATUS,
				Direction.ASCENDING), NS_MINE + "role1", "bob");
		assertSelectedUris(2, "user02");
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	/** Create a new criteria object */
	private UserAccountsSelectionCriteria criteria(int accountsPerPage,
			int pageIndex, UserAccountsOrdering orderBy, String roleFilterUri,
			String searchTerm) {
		return new UserAccountsSelectionCriteria(accountsPerPage, pageIndex,
				orderBy, roleFilterUri, searchTerm);
	}

	/** Create a criteria object and select against it. */
	private void selectOnCriteria(int accountsPerPage, int pageIndex,
			UserAccountsOrdering orderBy, String roleFilterUri,
			String searchTerm) {
		criteria = new UserAccountsSelectionCriteria(accountsPerPage,
				pageIndex, orderBy, roleFilterUri, searchTerm);
		selection = UserAccountsSelector.select(ontModel, criteria);
	}

	/** How many URIs should we expect, and which ones (local names only). */
	private void assertSelectedUris(int resultCount, String... uris) {
		assertEquals("result count", resultCount, selection.getResultCount());

		List<String> expectedList = Arrays.asList(uris);
		List<String> actualList = new ArrayList<String>();
		for (UserAccount a : selection.getUserAccounts()) {
			String[] uriParts = a.getUri().split("/");
			actualList.add(uriParts[uriParts.length - 1]);
		}
		assertEquals("uris", expectedList, actualList);
	}

}
