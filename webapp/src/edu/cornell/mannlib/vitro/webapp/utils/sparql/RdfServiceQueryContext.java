/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.SelectQueryRunner.ExecutingSelectQueryContext;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.SelectQueryRunner.SelectQueryContext;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.SelectQueryRunner.StringResultsMapping;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.SelectQueryRunner.StringResultsMappingImpl;

/**
 * An implementation of QueryContext based on an RDFService.
 * 
 * Package access. Instances should be created only by SelectQueryRunner, or by
 * a method on this class.
 */
class RdfServiceQueryContext implements SelectQueryContext {
	private static final Log log = LogFactory
			.getLog(RdfServiceQueryContext.class);

	private final RDFService rdfService;
	private final SelectQueryHolder query;

	RdfServiceQueryContext(RDFService rdfService, SelectQueryHolder query) {
		this.rdfService = rdfService;
		this.query = query;
	}

	@Override
	public RdfServiceQueryContext bindVariableToUri(String name, String uri) {
		return new RdfServiceQueryContext(rdfService,
				query.bindToUri(name, uri));
	}

	@Override
	public ExecutingSelectQueryContext execute() {
		return new RdfServiceExecutingQueryContext(rdfService, query);
	}

	private static class RdfServiceExecutingQueryContext implements
			ExecutingSelectQueryContext {
		private final RDFService rdfService;
		private final SelectQueryHolder query;

		public RdfServiceExecutingQueryContext(RDFService rdfService,
				SelectQueryHolder query) {
			this.rdfService = rdfService;
			this.query = query;
		}

		@Override
		public StringResultsMapping getStringFields(String... names) {
			Set<String> fieldNames = new HashSet<>(Arrays.asList(names));
			StringResultsMappingImpl mapping = new StringResultsMappingImpl();
			try {
				ResultSet results = RDFServiceUtils.sparqlSelectQuery(
						query.getQueryString(), rdfService);
				return mapResultsForQuery(results, fieldNames);
			} catch (Exception e) {
				log.error(
						"problem while running query '"
								+ query.getQueryString() + "'", e);
			}
			return mapping;
		}

		private StringResultsMapping mapResultsForQuery(ResultSet results,
				Set<String> fieldNames) {
			StringResultsMappingImpl mapping = new StringResultsMappingImpl();
			while (results.hasNext()) {
				Map<String, String> rowMapping = mapResultsForRow(
						results.nextSolution(), fieldNames);
				if (!rowMapping.isEmpty()) {
					mapping.add(rowMapping);
				}
			}
			return mapping;
		}

		private Map<String, String> mapResultsForRow(QuerySolution row,
				Set<String> fieldNames) {
			Map<String, String> map = new HashMap<>();
			for (Iterator<String> names = row.varNames(); names.hasNext();) {
				String name = names.next();
				RDFNode node = row.get(name);
				String text = getTextForNode(node);
				if (StringUtils.isNotBlank(text)) {
					map.put(name, text);
				}
			}
			if (!fieldNames.isEmpty()) {
				map.keySet().retainAll(fieldNames);
			}
			return map;
		}

		private String getTextForNode(RDFNode node) {
			if (node == null) {
				return "";
			} else if (node.isLiteral()) {
				return node.asLiteral().getString().trim();
			} else {
				return node.toString().trim();
			}
		}

	}

}
