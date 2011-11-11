/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Field;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.QueryParser;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryUtils;

/**
 * Pull some UserAccounts from the model, based on a set of criteria.
 */
public class UserAccountsSelector {
	private static final Log log = LogFactory
			.getLog(UserAccountsSelector.class);

	private static final String PREFIX_LINES = ""
			+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> \n"
			+ "PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n";

	private static final String ALL_VARIABLES = "?uri ?email ?firstName "
			+ "?lastName ?pwd ?expire ?count ?lastLogin ?status ?isRoot";

	private static final String COUNT_VARIABLE = "?uri";

	private static final String MAIN_QUERY_TEMPLATE = "" //
			+ "%prefixes% \n" //
			+ "SELECT DISTINCT %variables% \n" //
			+ "WHERE {\n" //
			+ "    %requiredClauses% \n" //
			+ "    %optionalClauses% \n" //
			+ "    %filterClauses% \n" //
			+ "} \n" //
			+ "ORDER BY %ordering% \n" //
			+ "LIMIT %limit% \n" //
			+ "OFFSET %offset% \n";

	private static final String COUNT_QUERY_TEMPLATE = "" //
			+ "%prefixes% \n" //
			+ "SELECT count(DISTINCT %countVariable%) \n" //
			+ "WHERE {\n" //
			+ "    %requiredClauses% \n" //
			+ "    %optionalClauses% \n" //
			+ "    %filterClauses% \n" //
			+ "} \n";

	private static final String PERMISSIONS_QUERY_TEMPLATE = "" //
			+ "%prefixes% \n" //
			+ "SELECT ?ps \n" //
			+ "WHERE {\n" //
			+ "    <%uri%> auth:hasPermissionSet ?ps \n" //
			+ "} \n";

	/**
	 * Convenience method.
	 */
	public static UserAccountsSelection select(OntModel userAccountsModel,
			UserAccountsSelectionCriteria criteria) {
		return new UserAccountsSelector(userAccountsModel, criteria).select();
	}

	private final OntModel model;
	private final UserAccountsSelectionCriteria criteria;

	public UserAccountsSelector(OntModel userAccountsModel,
			UserAccountsSelectionCriteria criteria) {
		if (userAccountsModel == null) {
			throw new NullPointerException("userAccountsModel may not be null.");
		}
		this.model = userAccountsModel;

		if (criteria == null) {
			throw new NullPointerException("criteria may not be null.");
		}
		this.criteria = criteria;
	}

	public UserAccountsSelection select() {
		List<UserAccount> accounts = queryForAccounts();
		int resultCount = queryForCount();
		queryToPopulatePermissionSets(accounts);
		return new UserAccountsSelection(criteria, accounts, resultCount);
	}

	private List<UserAccount> queryForAccounts() {
		String qString = MAIN_QUERY_TEMPLATE
				.replace("%prefixes%", PREFIX_LINES)
				.replace("%variables%", ALL_VARIABLES)
				.replace("%requiredClauses%", requiredClauses())
				.replace("%optionalClauses%", optionalClauses())
				.replace("%filterClauses%", filterClauses())
				.replace("%ordering%", ordering()).replace("%limit%", limit())
				.replace("%offset%", offset());
		log.debug("main query: " + qString);

		List<UserAccount> accounts = new SparqlQueryRunner<List<UserAccount>>(
				model, new MainQueryParser()).executeQuery(qString);
		log.debug("query returns: " + accounts);
		return accounts;
	}

	private int queryForCount() {
		String qString = COUNT_QUERY_TEMPLATE
				.replace("%prefixes%", PREFIX_LINES)
				.replace("%countVariable%", COUNT_VARIABLE)
				.replace("%requiredClauses%", requiredClauses())
				.replace("%optionalClauses%", optionalClauses())
				.replace("%filterClauses%", filterClauses());
		log.debug("count query: " + qString);

		int count = new SparqlQueryRunner<Integer>(model,
				new CountQueryParser()).executeQuery(qString);
		log.debug("result count: " + count);
		return count;
	}

	private void queryToPopulatePermissionSets(List<UserAccount> accounts) {
		for (UserAccount account : accounts) {
			String uri = account.getUri();
			String qString = PERMISSIONS_QUERY_TEMPLATE.replace("%prefixes%",
					PREFIX_LINES).replace("%uri%", uri);
			log.debug("permissions query: " + qString);

			Set<String> permissions = new SparqlQueryRunner<Set<String>>(model,
					new PermissionsQueryParser()).executeQuery(qString);
			log.debug("permissions for '" + uri + "': " + permissions);
			account.setPermissionSetUris(permissions);
		}
	}

	/** If the result doesn't have URI and Email Address, we don't want it. */
	private String requiredClauses() {
		return "?uri a auth:UserAccount ; \n"
				+ "         auth:emailAddress ?email .";
	}

	/** If any of these fields are missing, show the result anyway. */
	private String optionalClauses() {
		return "OPTIONAL { ?uri auth:firstName ?firstName } \n"
				+ "    OPTIONAL { ?uri auth:lastName ?lastName } \n"
				+ "    OPTIONAL { ?uri auth:md5password ?pwd } \n"
				+ "    OPTIONAL { ?uri auth:passwordChangeExpires ?expire } \n"
				+ "    OPTIONAL { ?uri auth:loginCount ?count } \n"
				+ "    OPTIONAL { ?uri auth:lastLoginTime ?lastLogin } \n"
				+ "    OPTIONAL { ?uri auth:status ?status } \n"
				+ "    OPTIONAL { ?uri ?isRoot auth:RootUserAccount }";
	}

	private String filterClauses() {
		String filters = "";

		String roleFilterUri = criteria.getRoleFilterUri();
		String searchTerm = criteria.getSearchTerm();

		if (!roleFilterUri.isEmpty()) {
			String clean = SparqlQueryUtils.escapeForRegex(roleFilterUri);
			filters += "OPTIONAL { ?uri auth:hasPermissionSet ?role } \n"
					+ "    FILTER (REGEX(str(?role), '^" + clean + "$'))";
		}

		if ((!roleFilterUri.isEmpty()) && (!searchTerm.isEmpty())) {
			filters += " \n    ";
		}

		if (!searchTerm.isEmpty()) {
			String clean = SparqlQueryUtils.escapeForRegex(searchTerm);
			filters += "FILTER ("
					+ ("REGEX(?email, '" + clean + "', 'i')")
					+ " || "
					+ ("REGEX(fn:concat(?firstName, ' ', ?lastName), '" + clean + "', 'i')")
					+ ")";
		}

		return filters;
	}

	/** Sort as desired, and within ties, sort by EMail address. */
	private String ordering() {
		UserAccountsOrdering orderBy = criteria.getOrderBy();
		String keyword = orderBy.getDirection().keyword;
		String variable = orderBy.getField().name;
		if (orderBy.getField() == Field.EMAIL) {
			return keyword + "(?" + variable + ")";
		} else {
			return keyword + "(?" + variable + ") ?" + Field.EMAIL.name;
		}
	}

	private String limit() {
		return String.valueOf(criteria.getAccountsPerPage());
	}

	private String offset() {
		int offset = criteria.getAccountsPerPage()
				* (criteria.getPageIndex() - 1);
		return String.valueOf(offset);
	}

	private static class MainQueryParser extends QueryParser<List<UserAccount>> {
		@Override
		protected List<UserAccount> defaultValue() {
			return Collections.emptyList();
		}

		@Override
		protected List<UserAccount> parseResults(String queryStr,
				ResultSet results) {
			List<UserAccount> accounts = new ArrayList<UserAccount>();
			while (results.hasNext()) {
				try {
					QuerySolution solution = results.next();
					UserAccount user = parseSolution(solution);
					accounts.add(user);
				} catch (Exception e) {
					log.warn("Failed to parse the query result: " + queryStr, e);
				}
			}
			return accounts;
		}

		private UserAccount parseSolution(QuerySolution solution) {
			UserAccount user = new UserAccount();
			user.setUri(getUriFromSolution(solution));
			user.setEmailAddress(solution.getLiteral("email").getString());
			user.setFirstName(ifLiteralPresent(solution, "firstName", ""));
			user.setLastName(ifLiteralPresent(solution, "lastName", ""));
			user.setMd5Password(ifLiteralPresent(solution, "pwd", ""));
			user.setPasswordLinkExpires(ifLongPresent(solution, "expire", 0L));
			user.setLoginCount(ifIntPresent(solution, "count", 0));
			user.setLastLoginTime(ifLongPresent(solution, "lastLogin", 0));
			user.setStatus(parseStatus(solution, "status", null));
			user.setRootUser(solution.contains("isRoot"));
			return user;
		}

		private Status parseStatus(QuerySolution solution, String variableName,
				Status defaultValue) {
			Literal literal = solution.getLiteral(variableName);
			if (literal == null) {
				return defaultValue;
			} else {
				String string = literal.getString();
				try {
					return Status.valueOf(string);
				} catch (Exception e) {
					String uri = getUriFromSolution(solution);
					log.warn("Failed to parse the status value for '" + uri
							+ "': '" + string + "'");
					return defaultValue;
				}
			}
		}

		private String getUriFromSolution(QuerySolution solution) {
			return solution.getResource("uri").getURI();
		}
	}

	private static class CountQueryParser extends QueryParser<Integer> {
		@Override
		protected Integer defaultValue() {
			return 0;
		}

		@Override
		protected Integer parseResults(String queryStr, ResultSet results) {
			int count = 0;

			if (!results.hasNext()) {
				log.warn("count query returned no results.");
			}
			try {
				QuerySolution solution = results.next();
				count = ifIntPresent(solution, ".1", 0);
			} catch (Exception e) {
				log.warn("Failed to parse the query result" + queryStr, e);
			}

			return count;
		}
	}

	private static class PermissionsQueryParser extends
			QueryParser<Set<String>> {
		@Override
		protected Set<String> defaultValue() {
			return Collections.emptySet();
		}

		@Override
		protected Set<String> parseResults(String queryStr, ResultSet results) {
			Set<String> permissions = new HashSet<String>();

			while (results.hasNext()) {
				try {
					QuerySolution solution = results.next();
					Resource r = solution.getResource("ps");
					if (r != null) {
						permissions.add(r.getURI());
					}
				} catch (Exception e) {
					log.warn("Failed to parse the query result: " + queryStr, e);
				}
			}

			return permissions;
		}
	}

}
