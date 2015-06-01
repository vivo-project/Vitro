/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.ConstructQueryRunner.ConstructQueryContext;

/**
 * An implementation of ConstructQueryContext based on an RDFService.
 * 
 * Package access. Instances should be created only by SelectQueryRunner, or by
 * a method on this class.
 */
class RdfServiceConstructQueryContext implements ConstructQueryContext {
	private static final Log log = LogFactory
			.getLog(RdfServiceConstructQueryContext.class);

	private final RDFService rdfService;
	private final ConstructQueryHolder query;

	RdfServiceConstructQueryContext(RDFService rdfService,
			ConstructQueryHolder query) {
		this.rdfService = rdfService;
		this.query = query;
	}

	@Override
	public RdfServiceConstructQueryContext bindVariableToUri(String name,
			String uri) {
		return new RdfServiceConstructQueryContext(rdfService, query.bindToUri(
				name, uri));
	}

	@Override
	public Model execute() {
		try {
		return RDFServiceUtils.parseModel(rdfService.sparqlConstructQuery(
				query.getQueryString(), NTRIPLE), NTRIPLE);
		} catch (Exception e) {
			log.error(
					"problem while running query '"
							+ query.getQueryString() + "'", e);
			return ModelFactory.createDefaultModel();
		}
	}
}
