/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import static edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelectionCriteria.ProxyRelationshipView.BY_PROFILE;
import static edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelectionCriteria.ProxyRelationshipView.BY_PROXY;
import static edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelectionCriteria.ProxyRelationshipView.DEFAULT_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
	private static final Log log = LogFactory
			.getLog(ProxyRelationshipSelectorTest.class);

	private static final String USER_ACCOUNT_DATA_FILENAME = "ProxyRelationshipSelectorTest_UserAccountsModel.n3";
	private static final String UNION_DATA_FILENAME = "ProxyRelationshipSelectorTest_UnionModel.n3";

	private static final String NS_MINE = "http://vivo.mydomain.edu/individual/";
	private static final String MATCHING_PROPERTY = NS_MINE + "matching";

	private static final String URL_PROFILE_IMAGE = "http://mydomain.edu/profileImage.jpg";
	private static final String URL_SELF_IMAGE = "http://mydomain.edu/selfImage.jpg";

	private static OntModel userAccountsModel;
	private static OntModel unionModel;
	private static Context context;

	/** 1, when sorted by proxy */
	private static final Relation RELATION_1 = relation(
			list(mydomain("userFirstProxy")), list(mydomain("firstProfile")));

	/** 2, when sorted by proxy */
	private static final Relation RELATION_2 = relation(
			list(mydomain("userProxyWithSelfWithBoth")),
			list(mydomain("popularProfile")));

	/** 3, when sorted by proxy */
	private static final Relation RELATION_3 = relation(
			list(mydomain("userProxyWithSelfWithNeither")),
			list(mydomain("popularProfile")));

	/** 4, when sorted by proxy */
	private static final Relation RELATION_4 = relation(
			list(mydomain("userProxyWithSelfWithNoClassLabel")),
			list(mydomain("popularProfile")));

	/** 5, when sorted by proxy */
	private static final Relation RELATION_5 = relation(
			list(mydomain("userProxyWithSelfWithNoImageUrl")),
			list(mydomain("popularProfile")));

	/** 6, when sorted by proxy */
	private static final Relation RELATION_6 = relation(
			list(mydomain("userProxyWithNoSelf")),
			list(mydomain("popularProfile")));

	/** 7, when sorted by proxy */
	private static final Relation RELATION_7 = relation(
			list(mydomain("userPopularProxy")),
			list(mydomain("profileWithBoth"), mydomain("profileWithNeither"),
					mydomain("profileWithNoImageUrl"),
					mydomain("profileWithNoClassLabel")));

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
	public void checkAllFieldsOnFirstRelationshipByProxy() {
		selectOnCriteria(1, 1, BY_PROXY, "");
		log.debug("SELECTION: " + selection);
		assertExpectedCounts(7, counts(1, 1));

		ProxyRelationship pr = selection.getProxyRelationships().get(0);
		assertEquals(
				"proxy",
				item(mydomain("userFirstProxy"), "AAAA, FirstProxy", "Self",
						URL_SELF_IMAGE), pr.getProxyInfos().get(0));
		assertEquals(
				"profile",
				item(mydomain("firstProfile"), "AAAA, FirstProfile", "Profile",
						URL_PROFILE_IMAGE), pr.getProfileInfos().get(0));
	}

	@Test
	@Ignore
	public void checkAllFieldsOnFirstRelationshipByProfile() {
		selectOnCriteria(1, 1, BY_PROFILE, "");
		assertExpectedCounts(7, counts(1, 1));

		ProxyRelationship pr = selection.getProxyRelationships().get(0);
		assertEquals(
				"proxy",
				item(mydomain("userFirstProxy"), "AAAA, FirstProxy", "Self",
						URL_SELF_IMAGE), pr.getProxyInfos().get(0));
		assertEquals(
				"profile",
				item(mydomain("firstProfile"), "AAAA, FirstProfile", "Profile",
						URL_PROFILE_IMAGE), pr.getProfileInfos().get(0));
	}

	// ----------------------------------------------------------------------
	// pagination tests
	// TODO -- also by profile
	// ----------------------------------------------------------------------

	@Test
	public void paginationFirstOfSeveralByProxy() {
		selectOnCriteria(3, 1, BY_PROXY, "");
		assertExpectedRelations(7, RELATION_1, RELATION_2, RELATION_3);
	}

	@Test
	public void paginationOnlyPageByProxy() {
		selectOnCriteria(10, 1, BY_PROXY, "");
		assertExpectedRelations(7, RELATION_1, RELATION_2, RELATION_3,
				RELATION_4, RELATION_5, RELATION_6, RELATION_7);
	}

	@Test
	public void paginationSecondOfSeveralByProxy() {
		selectOnCriteria(3, 2, BY_PROXY, "");
		assertExpectedRelations(7, RELATION_4, RELATION_5, RELATION_6);
	}

	@Test
	public void paginationOutOfRangeTooHighByProxy() {
		selectOnCriteria(3, 7, BY_PROXY, "");
		assertExpectedRelations(7);
	}

	@Test
	@Ignore
	public void paginationLastFullPageByProxy() {
		fail("paginationLastFullPageByProxy not implemented");
	}

	@Test
	public void paginationLastPartialPageByProxy() {
		selectOnCriteria(3, 3, BY_PROXY, "");
		assertExpectedRelations(7, RELATION_7);
	}

	/**
	 * test plan:
	 * 
	 * <pre>
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
	// search tests
	// TODO search by Profile also
	// ----------------------------------------------------------------------

	@Test
	public void searchFirstProxy() {
		selectOnCriteria(10, 1, BY_PROXY, "AA");
		assertExpectedRelations(1, RELATION_1);
	}

	@Test
	public void searchAccountWithNoProxy() {
		selectOnCriteria(10, 1, BY_PROXY, "None");
		assertExpectedRelations(0);
	}

	@Test
	public void searchMultipleProxies() {
		selectOnCriteria(10, 1, BY_PROXY, "No");
		assertExpectedRelations(3, RELATION_4, RELATION_5, RELATION_6);
	}

	// ----------------------------------------------------------------------
	// combination tests
	// ----------------------------------------------------------------------

	@Test
	public void searchPopularWithPagination() {
		selectOnCriteria(2, 2, BY_PROXY, "No");
		assertExpectedRelations(3, RELATION_6);
	}

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

	private static List<String> list(String... uris) {
		return Arrays.asList(uris);
	}

	private static Relation relation(List<String> proxyUris,
			List<String> profileUris) {
		return new Relation(proxyUris, profileUris);
	}

	private static String mydomain(String localName) {
		return NS_MINE + localName;
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

	private void assertExpectedRelations(int total, Relation... relations) {
		assertEquals("total result count", total,
				selection.getTotalResultCount());
		assertEquals("page result count", relations.length, selection
				.getProxyRelationships().size());

		for (int i = 0; i < relations.length; i++) {
			assertEqualUris(i, relations[i], selection.getProxyRelationships()
					.get(i));
		}
	}

	private void assertEqualUris(int i, Relation relation,
			ProxyRelationship proxyRelationship) {
		List<String> expectedProxyUris = relation.proxyUris;
		List<String> actualProxyUris = new ArrayList<String>();
		for (ProxyItemInfo proxyInfo : proxyRelationship.getProxyInfos()) {
			actualProxyUris.add(proxyInfo.getUri());
		}
		assertEquals("proxies for relationship " + i, expectedProxyUris,
				actualProxyUris);

		List<String> expectedProfileUris = relation.profileUris;
		List<String> actualProfileUris = new ArrayList<String>();
		for (ProxyItemInfo profileInfo : proxyRelationship.getProfileInfos()) {
			actualProfileUris.add(profileInfo.getUri());
		}
		assertEquals("profiles for relationship " + i, expectedProfileUris,
				actualProfileUris);
	}

	private static class Relation {
		final List<String> proxyUris;
		final List<String> profileUris;

		public Relation(List<String> proxyUris, List<String> profileUris) {
			this.proxyUris = proxyUris;
			this.profileUris = profileUris;
		}
	}
}
