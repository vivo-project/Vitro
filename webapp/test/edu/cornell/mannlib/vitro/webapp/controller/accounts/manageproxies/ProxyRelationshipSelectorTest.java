/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import static edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelectionCriteria.ProxyRelationshipView.DEFAULT_VIEW;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelectionCriteria.ProxyRelationshipView;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelector.Context;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner;

/**
 * TODO
 */
public class ProxyRelationshipSelectorTest extends AbstractTestClass {
	/**
	 * 
	 */
	private static final String URL_PROFILE_IMAGE = "http://mydomain.edu/profileImage.jpg";
	/**
	 * 
	 */
	private static final String URL_SELF_IMAGE = "http://mydomain.edu/selfImage.jpg";
	private static final String USER_ACCOUNT_DATA_FILENAME = "ProxyRelationshipSelectorTest_UserAccountsModel.n3";
	private static final String UNION_DATA_FILENAME = "ProxyRelationshipSelectorTest_UnionModel.n3";

	private static final String NS_MINE = "http://vivo.mydomain.edu/individual/";
	private static final String MATCHING_PROPERTY = NS_MINE + "matching";

	private static OntModel userAccountsModel;
	private static OntModel unionModel;
	private static Context context;

	private ProxyRelationshipSelection selection;
	private ProxyRelationshipSelectionCriteria criteria;

	@BeforeClass
	public static void setupModel() throws IOException {
		userAccountsModel = prepareModel(USER_ACCOUNT_DATA_FILENAME);
		unionModel = prepareModel(UNION_DATA_FILENAME);
		context = new Context(userAccountsModel, unionModel, MATCHING_PROPERTY);
	}

	private static OntModel prepareModel(String filename) throws IOException {
		InputStream stream = ProxyRelationshipSelectorTest.class
				.getResourceAsStream(filename);
		Model model = ModelFactory.createDefaultModel();
		model.read(stream, null, "N3");
		stream.close();

		OntModel ontModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_DL_MEM, model);
		ontModel.prepare();
		return ontModel;
	}

	// ----------------------------------------------------------------------
	// exceptions tests
	// ----------------------------------------------------------------------

	@Test(expected = NullPointerException.class)
	public void contextIsNull() {
		ProxyRelationshipSelector.select(null,
				criteria(10, 1, DEFAULT_VIEW, ""));
	}

	@Test(expected = NullPointerException.class)
	public void userAccountsModelIsNull_select_nullPointerException() {
		Context brokenContext = new Context(null, unionModel, MATCHING_PROPERTY);
		ProxyRelationshipSelector.select(brokenContext,
				criteria(10, 1, DEFAULT_VIEW, ""));
	}

	@Test(expected = NullPointerException.class)
	public void unionModelIsNull_select_nullPointerException() {
		Context brokenContext = new Context(userAccountsModel, null,
				MATCHING_PROPERTY);
		ProxyRelationshipSelector.select(brokenContext,
				criteria(10, 1, DEFAULT_VIEW, ""));
	}

	@Test(expected = NullPointerException.class)
	public void criteriaIsNull() {
		ProxyRelationshipSelector.select(context, null);
	}

	// ----------------------------------------------------------------------
	// fields tests
	// ----------------------------------------------------------------------

	@Test
	public void checkAllFieldsOnFirstRelationship() {
		setLoggerLevel(SparqlQueryRunner.class, Level.DEBUG);
		selectOnCriteria(1, 1, DEFAULT_VIEW, "");
		System.out.println("SELECTION: " + selection);
		assertExpectedCounts(7, counts(1, 1));

		ProxyRelationship pr = selection.getProxyRelationships().get(0);
		assertEquals(
				"proxy",
				item(NS_MINE + "userFirstProxy", "AAAA, FirstProxy", "Self",
						URL_SELF_IMAGE), pr.getProxyInfos().get(0));
		assertEquals(
				"profile",
				item(NS_MINE + "firstProfile", "AAAA, FirstProfile", "Profile",
						URL_PROFILE_IMAGE), pr.getProfileInfos().get(0));
	}

	/**
	 * test plan:
	 * 
	 * <pre>
	 * pagination tests: (repeat both views?)
	 *   page 1 of several
	 *   page 1 of 1
	 *   page 2 of several
	 *   page out of range (zero results)
	 *   last page divides evenly
	 *   last page divides unevenly
	 *   
	 * search tests: (repeat both views)
	 *   some results
	 *   no results
	 *   special REGEX characters
	 *   
	 *   profile w/no proxies
	 * profile w/proxies
	 * 	no associated profile
	 * 	profile w/no classLabel
	 * 	profile w/no imageUrl
	 * 	profile w/neither
	 * 	profile w/both
	 * 	
	 * proxy w/no profiles
	 * proxy w profiles:
	 * 	no classLabel
	 * 	no imageUrl
	 * 	neither
	 * 	both
	 * </pre>
	 */

	// ----------------------------------------------------------------------
	// pagination tests
	// ----------------------------------------------------------------------

	// @Test
	// public void showFirstPageOfFifteen() {
	// selectOnCriteria(15, 1, DEFAULT_ORDERING, "", "");
	// assertSelectedUris(10, "user01", "user02", "user03", "user04",
	// "user05", "user06", "user07", "user08", "user09", "user10");
	// }
	//
	// @Test
	// public void showFirstPageOfOne() {
	// selectOnCriteria(1, 1, DEFAULT_ORDERING, "", "");
	// assertSelectedUris(10, "user01");
	// }
	//
	// @Test
	// public void showFirstPageOfFive() {
	// selectOnCriteria(5, 1, DEFAULT_ORDERING, "", "");
	// assertSelectedUris(10, "user01", "user02", "user03", "user04", "user05");
	// }
	//
	// @Test
	// public void showSecondPageOfSeven() {
	// selectOnCriteria(7, 2, DEFAULT_ORDERING, "", "");
	// assertSelectedUris(10, "user08", "user09", "user10");
	// }
	//
	// @Test
	// public void showTenthPageOfThree() {
	// selectOnCriteria(3, 10, DEFAULT_ORDERING, "", "");
	// assertSelectedUris(10);
	// }

	// ----------------------------------------------------------------------
	// search tests
	// ----------------------------------------------------------------------
	//
	// @Test
	// public void searchTermFoundInAllThreeFields() {
	// selectOnCriteria(20, 1, DEFAULT_ORDERING, "", "bob");
	// assertSelectedUris(3, "user02", "user05", "user10");
	// }
	//
	// @Test
	// public void searchTermNotFound() {
	// selectOnCriteria(20, 1, DEFAULT_ORDERING, "", "bogus");
	// assertSelectedUris(0);
	// }
	//
	// /**
	// * If the special characters were allowed into the Regex, this would have
	// 3
	// * matches. If they are escaped properly, it will have none.
	// */
	// @Test
	// public void searchTermContainsSpecialRegexCharacters() {
	// selectOnCriteria(20, 1, DEFAULT_ORDERING, "", "b.b");
	// assertSelectedUris(0);
	// }
	//
	// // ----------------------------------------------------------------------
	// // combination tests
	// // ----------------------------------------------------------------------
	//
	// @Test
	// public void searchWithFilter() {
	// selectOnCriteria(20, 1, DEFAULT_ORDERING, NS_MINE + "role1", "bob");
	// assertSelectedUris(2, "user02", "user05");
	// }
	//
	// @Test
	// public void searchWithFilterPaginatedWithFunkySortOrder() {
	// selectOnCriteria(1, 2, new UserAccountsOrdering(Field.STATUS,
	// Direction.ASCENDING), NS_MINE + "role1", "bob");
	// assertSelectedUris(2, "user02");
	// }
	//
	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	/** Create a new criteria object */
	private ProxyRelationshipSelectionCriteria criteria(int accountsPerPage,
			int pageIndex, ProxyRelationshipView view, String searchTerm) {
		return new ProxyRelationshipSelectionCriteria(accountsPerPage,
				pageIndex, view, searchTerm);
	}

	/** Create a criteria object and select against it. */
	private void selectOnCriteria(int relationshipsPerPage, int pageIndex,
			ProxyRelationshipView viewBy, String searchTerm) {
		criteria = new ProxyRelationshipSelectionCriteria(relationshipsPerPage,
				pageIndex, viewBy, searchTerm);
		selection = ProxyRelationshipSelector.select(context, criteria);
	}

	private int[] counts(int proxyCount, int profileCount) {
		return new int[] { proxyCount, profileCount };
	}

	private ProxyItemInfo item(String uri, String label, String classLabel,
			String imageUrl) {
		return new ProxyItemInfo(uri, label, classLabel, imageUrl);
	}

	private void assertExpectedCounts(int total, int[]... counts) {
		assertEquals("total result count", total,
				selection.getTotalResultCount());

		List<ProxyRelationship> relationships = selection
				.getProxyRelationships();
		assertEquals("number of returns", counts.length, relationships.size());

		for (int i = 0; i < counts.length; i++) {
			ProxyRelationship r = relationships.get(i);
			assertEquals("number of proxies in result " + i, counts[i][0], r
					.getProxyInfos().size());
			assertEquals("number of profiles in result " + i, counts[i][1], r
					.getProfileInfos().size());
		}
	}

}
