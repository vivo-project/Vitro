/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelectionBuilder.ItemInfo;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelectionBuilder.Relationship;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ProxyRelationshipSelectionCriteria.ProxyRelationshipView;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.QueryParser;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryUtils;

/**
 * A class which will accept a ProxyRelationshipSelectionCriteria and produce a
 * ProxyRelationshipSelection.
 */
public class ProxyRelationshipSelector {
	private static final Log log = LogFactory
			.getLog(ProxyRelationshipSelector.class);

	/**
	 * Convenience method.
	 */
	public static ProxyRelationshipSelection select(Context context,
			ProxyRelationshipSelectionCriteria criteria) {
		return new ProxyRelationshipSelector(context, criteria).select();
	}

	private static final String PREFIX_LINES = ""
			+ "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> \n"
			+ "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n"
			+ "PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n"
			+ "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
			+ "PREFIX vpublic: <http://vitro.mannlib.cornell.edu/ns/vitro/public#> \n";

	private final Context context;
	private final ProxyRelationshipSelectionCriteria criteria;
	private final ProxyRelationshipSelectionBuilder builder;

	public ProxyRelationshipSelector(Context context,
			ProxyRelationshipSelectionCriteria criteria) {
		if (context == null) {
			throw new NullPointerException("context may not be null.");
		}
		this.context = context;

		if (criteria == null) {
			throw new NullPointerException("criteria may not be null.");
		}
		this.criteria = criteria;

		this.builder = new ProxyRelationshipSelectionBuilder(criteria);
	}

	public ProxyRelationshipSelection select() {
		if (criteria.getViewBy() == ProxyRelationshipView.BY_PROXY) {
			figureTotalResultCount();
			getProxyBasics();
			expandProxies();
			getRelationships();
			expandProfiles();
		} else {
			// This implementation is brain-dead if you ask for BY_PROFILE.
			// Maybe someday soon.
			log.error("Trying to select ProxyRelationships by profile!");
		}
		return builder.build();
	}

	private static final String COUNT_QUERY_TEMPLATE = "" //
			+ "%prefixes% \n" //
			+ "SELECT count(DISTINCT ?uri) \n" //
			+ "WHERE {\n" //
			+ "    ?uri a auth:UserAccount ; \n" //
			+ "            auth:firstName ?firstName ; \n" //
			+ "            auth:lastName ?lastName ; \n" //
			+ "            auth:proxyEditorFor ?profile . \n" //
			+ "    LET ( ?label := fn:concat(?lastName, ', ', ?firstName) )" //
			+ "    %filterClause% \n" //
			+ "} \n"; //

	private void figureTotalResultCount() {
		String qString = COUNT_QUERY_TEMPLATE.replace("%prefixes%",
				PREFIX_LINES);
		qString = replaceFilterClauses(qString);

		int count = new SparqlQueryRunner<Integer>(context.userAccountsModel,
				new CountQueryParser()).executeQuery(qString);

		log.debug("result count: " + count);
		builder.count = count;
	}

	private String replaceFilterClauses(String q) {
		String searchTerm = criteria.getSearchTerm();
		if (searchTerm.isEmpty()) {
			return q.replace("%filterClause%", "");
		} else {
			String clean = SparqlQueryUtils.escapeForRegex(searchTerm);
			return q.replace("%filterClause%",
					"FILTER (REGEX(str(?label), '^" + clean + "', 'i'))");
		}
	}

	private static final String QUERY_PROXY_BASICS = "" //
			+ "%prefixes% \n" //
			+ "SELECT DISTINCT ?uri ?label ?externalAuthId \n" //
			+ "WHERE { \n" //
			+ "    ?uri a auth:UserAccount ; \n" //
			+ "            auth:firstName ?firstName ; \n" //
			+ "            auth:lastName ?lastName ; \n" //
			+ "            auth:proxyEditorFor ?profile . \n" //
			+ "    LET ( ?label := fn:concat(?lastName, ', ', ?firstName) )" //
			+ "    OPTIONAL { ?uri auth:externalAuthId ?externalAuthId } \n" //
			+ "    %filterClause% \n" //
			+ "} \n" //
			+ "ORDER BY ASC(?lastName) ASC(?firstName) \n" //
			+ "LIMIT %perPage% \n" //
			+ "OFFSET %offset%\n";

	private void getProxyBasics() {
		String qString = QUERY_PROXY_BASICS
				.replace("%prefixes%", PREFIX_LINES)
				.replace("%perPage%",
						String.valueOf(criteria.getRelationshipsPerPage()))
				.replace("%offset%", offset());
		qString = replaceFilterClauses(qString);

		List<Relationship> relationships = new SparqlQueryRunner<List<Relationship>>(
				context.userAccountsModel, new ProxyBasicsParser())
				.executeQuery(qString);
		log.debug("getProxyBasics returns: " + relationships);
		builder.relationships.addAll(relationships);
	}

	private static final String QUERY_EXPAND_PROXY = "" //
			+ "%prefixes% \n" //
			+ "SELECT ?classLabel ?imageUrl \n" //
			+ "WHERE { \n" //
			+ "    ?uri <%matchingProperty%> '%externalAuthId%'. \n" //
			+ "    OPTIONAL { \n"
			+ "       ?uri vitro:mostSpecificType ?type. \n" //
			+ "       ?type rdfs:label ?classLabel  \n" //
			+ "       }  \n" //
			+ "   OPTIONAL { \n" //
			+ "       ?uri vpublic:mainImage ?imageUri. \n" //
			+ "       ?imageUri vpublic:thumbnailImage ?thumbUri. \n" //
			+ "       ?thumbUri vpublic:downloadLocation ?thumbstreamUri. \n" //
			+ "       ?thumbstreamUri vpublic:directDownloadUrl ?imageUrl. \n" //
			+ "       }  \n" //
			+ "} \n" //
			+ "LIMIT 1 \n"; //

	private void expandProxies() {
		if (context.matchingProperty.isEmpty()) {
			return;
		}

		for (Relationship r : builder.relationships) {
			for (ItemInfo proxy : r.proxyInfos) {
				if (proxy.externalAuthId.isEmpty()) {
					continue;
				}

				String qString = QUERY_EXPAND_PROXY
						.replace("%prefixes%", PREFIX_LINES)
						.replace("%matchingProperty%", context.matchingProperty)
						.replace("%externalAuthId%", proxy.externalAuthId);

				ItemInfo expansion = new SparqlQueryRunner<ItemInfo>(
						context.unionModel, new ExpandProxyParser())
						.executeQuery(qString);
				proxy.classLabel = expansion.classLabel;
				proxy.imageUrl = expansion.imageUrl;
			}
		}
	}

	private static final String QUERY_RELATIONSHIPS = "" //
			+ "%prefixes% \n" //
			+ "SELECT DISTINCT ?profileUri \n" //
			+ "WHERE { \n" //
			+ "    <%proxyUri%> a auth:UserAccount ; \n" //
			+ "            auth:proxyEditorFor ?profileUri . \n" //
			+ "} \n"; //

	private void getRelationships() {
		for (Relationship r : builder.relationships) {
			for (ItemInfo proxy : r.proxyInfos) {
				String qString = QUERY_RELATIONSHIPS.replace("%prefixes%",
						PREFIX_LINES).replace("%proxyUri%", proxy.uri);

				List<String> profileUris = new SparqlQueryRunner<List<String>>(
						context.userAccountsModel, new RelationshipsParser())
						.executeQuery(qString);

				for (String profileUri : profileUris) {
					r.profileInfos
							.add(new ItemInfo(profileUri, "", "", "", ""));
				}
			}
		}
	}

	private static final String QUERY_EXPAND_PROFILE = "" //
			+ "%prefixes% \n" //
			+ "SELECT ?label ?classLabel ?imageUrl \n" //
			+ "WHERE { \n" //
			+ "    <%profileUri%> rdfs:label ?label . \n" //
			+ "    OPTIONAL { \n"
			+ "       <%profileUri%> vitro:mostSpecificType ?type. \n" //
			+ "       ?type rdfs:label ?classLabel  \n" //
			+ "       }  \n" //
			+ "   OPTIONAL { \n" //
			+ "       <%profileUri%> vpublic:mainImage ?imageUri. \n" //
			+ "       ?imageUri vpublic:thumbnailImage ?thumbUri. \n" //
			+ "       ?thumbUri vpublic:downloadLocation ?thumbstreamUri. \n" //
			+ "       ?thumbstreamUri vpublic:directDownloadUrl ?imageUrl. \n" //
			+ "       }  \n" //
			+ "} \n" //
			+ "LIMIT 1 \n"; //

	private void expandProfiles() {
		for (Relationship r : builder.relationships) {
			for (ItemInfo profile : r.profileInfos) {
				String qString = QUERY_EXPAND_PROFILE.replace("%prefixes%",
						PREFIX_LINES).replace("%profileUri%", profile.uri);

				ItemInfo expansion = new SparqlQueryRunner<ItemInfo>(
						context.unionModel, new ExpandProfileParser())
						.executeQuery(qString);
				profile.label = expansion.label;
				profile.classLabel = expansion.classLabel;
				profile.imageUrl = expansion.imageUrl;
			}
		}
	}

	private String offset() {
		int offset = criteria.getRelationshipsPerPage()
				* (criteria.getPageIndex() - 1);
		return String.valueOf(offset);
	}

	/**
	 * What do we need to make this work? 2 models and an optional property.
	 */
	public static class Context {
		private final OntModel userAccountsModel;
		private final OntModel unionModel;
		private final String matchingProperty;

		public Context(OntModel userAccountsModel, OntModel unionModel,
				String matchingProperty) {
			if (userAccountsModel == null) {
				throw new NullPointerException(
						"userAccountsModel may not be null.");
			}
			this.userAccountsModel = userAccountsModel;

			if (unionModel == null) {
				throw new NullPointerException("unionModel may not be null.");
			}
			this.unionModel = unionModel;

			if (matchingProperty == null) {
				this.matchingProperty = "";
			} else {
				this.matchingProperty = matchingProperty;
			}
		}

	}

	// ----------------------------------------------------------------------
	// Parser classes
	// ----------------------------------------------------------------------

	private static class ProxyBasicsParser extends
			QueryParser<List<Relationship>> {
		@Override
		protected List<Relationship> defaultValue() {
			return Collections.emptyList();
		}

		@Override
		protected List<Relationship> parseResults(String queryStr,
				ResultSet results) {
			List<Relationship> relationships = new ArrayList<Relationship>();
			while (results.hasNext()) {
				try {
					relationships.add(parseSolution(results.next()));
				} catch (Exception e) {
					log.warn("Failed to parse the query result: " + queryStr, e);
				}
			}
			return relationships;
		}

		private Relationship parseSolution(QuerySolution solution) {
			ItemInfo info = new ItemInfo();
			info.uri = solution.getResource("uri").getURI();
			info.label = solution.getLiteral("label").getString();
			info.externalAuthId = ifLiteralPresent(solution, "externalAuthId",
					"");

			Relationship r = new Relationship();
			r.proxyInfos.add(info);
			return r;
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

	private static class ExpandProxyParser extends QueryParser<ItemInfo> {
		@Override
		protected ItemInfo defaultValue() {
			return new ItemInfo();
		}

		@Override
		protected ItemInfo parseResults(String queryStr, ResultSet results) {
			ItemInfo item = new ItemInfo();

			if (results.hasNext()) {
				try {
					QuerySolution solution = results.next();
					item.classLabel = ifLiteralPresent(solution, "classLabel",
							"");
					item.imageUrl = ifLiteralPresent(solution, "imageUrl", "");
				} catch (Exception e) {
					log.warn("Failed to parse the query result" + queryStr, e);
				}
			}

			return item;
		}
	}

	private static class RelationshipsParser extends QueryParser<List<String>> {
		@Override
		protected List<String> defaultValue() {
			return Collections.emptyList();
		}

		@Override
		protected List<String> parseResults(String queryStr, ResultSet results) {
			List<String> proxyUris = new ArrayList<String>();
			while (results.hasNext()) {
				try {
					QuerySolution solution = results.next();
					proxyUris.add(solution.getResource("profileUri").getURI());
				} catch (Exception e) {
					log.warn("Failed to parse the query result: " + queryStr, e);
				}
			}
			return proxyUris;
		}
	}

	private static class ExpandProfileParser extends QueryParser<ItemInfo> {
		@Override
		protected ItemInfo defaultValue() {
			return new ItemInfo();
		}

		@Override
		protected ItemInfo parseResults(String queryStr, ResultSet results) {
			ItemInfo item = new ItemInfo();

			if (results.hasNext()) {
				try {
					QuerySolution solution = results.next();
					item.label = ifLiteralPresent(solution, "label", "");
					item.classLabel = ifLiteralPresent(solution, "classLabel",
							"");
					item.imageUrl = ifLiteralPresent(solution, "imageUrl", "");
				} catch (Exception e) {
					log.warn("Failed to parse the query result" + queryStr, e);
				}
			}

			return item;
		}
	}

}
