/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * The current user is blacklisted for this reason.
 */
public class IsBlacklisted extends AbstractCommonIdentifier implements
		Identifier {
	private static final Log log = LogFactory.getLog(IsBlacklisted.class);

	private final static String BLACKLIST_SPARQL_DIR = "/admin/selfEditBlacklist";
	private static final String NOT_BLACKLISTED = null;

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * If this individual is blacklisted, return an appropriate Identifier.
	 * Otherwise, return null.
	 */
	public static IsBlacklisted getInstance(Individual individual,
			ServletContext context) {
		if (individual == null) {
			throw new NullPointerException("individual may not be null.");
		}
		if (context == null) {
			throw new NullPointerException("context may not be null.");
		}

		String reasonForBlacklisting = checkForBlacklisted(individual, context);
		IsBlacklisted id = new IsBlacklisted(individual.getURI(),
				reasonForBlacklisting);
		if (id.isBlacklisted()) {
			return id;
		} else {
			return null;
		}
	}

	/**
	 * Runs through .sparql files in the BLACKLIST_SPARQL_DIR.
	 * 
	 * The first that returns one or more rows will be cause the user to be
	 * blacklisted.
	 * 
	 * The first variable from the first solution set will be returned.
	 */
	private static String checkForBlacklisted(Individual ind,
			ServletContext context) {
		String realPath = context.getRealPath(BLACKLIST_SPARQL_DIR);
		File blacklistDir = new File(realPath);
		if (!blacklistDir.isDirectory() || !blacklistDir.canRead()) {
			log.debug("cannot read blacklist directory " + realPath);
			return NOT_BLACKLISTED;
		}

		log.debug("checking directlry " + realPath
				+ " for blacklisting sparql query files");
		File[] files = blacklistDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".sparql");
			}
		});

		String reasonForBlacklist = NOT_BLACKLISTED;
		for (File file : files) {
			try {
				reasonForBlacklist = runSparqlFileForBlacklist(file, ind,
						context);
				if (reasonForBlacklist != NOT_BLACKLISTED)
					break;
			} catch (RuntimeException ex) {
				log.error(
						"Could not run blacklist check query for file "
								+ file.getAbsolutePath() + File.separatorChar
								+ file.getName(), ex);
			}
		}
		return reasonForBlacklist;
	}

	/**
	 * Runs the SPARQL query in the file with the uri of the individual
	 * substituted in.
	 * 
	 * The URI of ind will be substituted into the query where ever the token
	 * "?individualURI" is found.
	 * 
	 * If there are any solution sets, then the URI of the variable named
	 * "cause" will be returned. Make sure that it is a resource with a URI.
	 * Otherwise null will be returned.
	 */
	private static String runSparqlFileForBlacklist(File file, Individual ind,
			ServletContext context) {
		if (!file.canRead()) {
			log.debug("cannot read blacklisting SPARQL file " + file.getName());
			return NOT_BLACKLISTED;
		}

		String queryString = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte b[] = new byte[fis.available()];
			fis.read(b);
			queryString = new String(b);
		} catch (IOException ioe) {
			log.debug(ioe);
			return NOT_BLACKLISTED;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.warn("could not close file", e);
				}
			}
		}

		if (StringUtils.isEmpty(queryString)) {
			log.debug(file.getName() + " is empty");
			return NOT_BLACKLISTED;
		}

		Model model = (Model) context.getAttribute("jenaOntModel");

		queryString = queryString.replaceAll("\\?individualURI",
				"<" + ind.getURI() + ">");
		log.debug(queryString);

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution solution = results.nextSolution();
				if (solution.contains("cause")) {
					RDFNode node = solution.get("cause");
					if (node.isResource()) {
						return node.asResource().getURI();
					} else if (node.isLiteral()) {
						return node.asLiteral().getString();
					}
				} else {
					log.error("Query solution must contain a variable "
							+ "\"cause\" of type Resource or Literal.");
					return NOT_BLACKLISTED;
				}
			}
		} finally {
			qexec.close();
		}
		return NOT_BLACKLISTED;
	}

	public static Collection<IsBlacklisted> getIdentifiers(IdentifierBundle ids) {
		return getIdentifiersForClass(ids, IsBlacklisted.class);
	}

	public static Collection<String> getBlacklistReasons(IdentifierBundle ids) {
		Set<String> set = new HashSet<String>();
		for (IsBlacklisted id : getIdentifiers(ids)) {
			if (id.isBlacklisted()) {
				set.add(id.getReasonForBlacklisting());
			}
		}
		return set;
	}

	// ----------------------------------------------------------------------
	// the Identifier
	// ----------------------------------------------------------------------

	private final String associatedIndividualUri;
	private final String reasonForBlacklisting;

	public IsBlacklisted(String associatedIndividualUri,
			String reasonForBlacklisting) {
		this.associatedIndividualUri = associatedIndividualUri;
		this.reasonForBlacklisting = reasonForBlacklisting;
	}

	public String getAssociatedIndividualUri() {
		return associatedIndividualUri;
	}

	public boolean isBlacklisted() {
		return reasonForBlacklisting != NOT_BLACKLISTED;
	}

	public String getReasonForBlacklisting() {
		return reasonForBlacklisting;
	}

	@Override
	public String toString() {
		return "IsBlacklisted[" + associatedIndividualUri + ", "
				+ reasonForBlacklisting + "]";
	}

}
