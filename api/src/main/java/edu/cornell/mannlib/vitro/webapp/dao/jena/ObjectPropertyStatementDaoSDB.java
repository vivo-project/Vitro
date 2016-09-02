/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat.N3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class ObjectPropertyStatementDaoSDB extends
		ObjectPropertyStatementDaoJena implements ObjectPropertyStatementDao {
	private static final Log log = LogFactory
			.getLog(ObjectPropertyStatementDaoSDB.class);

	// Get the types of the base entity.
	private static final String SUBJECT_TYPE_QUERY = ""
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "CONSTRUCT { \n" //
			+ "   ?uri rdf:type ?type . \n" //
			+ "} WHERE { \n" //
			+ "   ?uri rdf:type ?type . \n" //
			+ "} \n";

	// Get the types of all objects of properties.
	private static final String OBJECT_TYPE_QUERY = ""
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "CONSTRUCT { \n" //
			+ "   ?uri ?p ?o . \n" //
			+ "   ?o rdf:type ?type . \n" //
			+ "} WHERE { \n" //
			+ "   ?uri ?p ?o . \n" //
			+ "   ?o rdf:type ?type . \n" //
			+ "} \n";

	// Get the labels of all objects of properties.
	private static final String OBJECT_LABEL_QUERY = ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
			+ "CONSTRUCT { \n" //
			+ "   ?uri ?p ?o . \n" //
			+ "   ?o rdfs:label ?label . \n" //
			+ "} WHERE { \n" //
			+ "   ?uri ?p ?o . \n" //
			+ "   ?o rdfs:label ?label . \n" //
			+ "} \n";

	private final WebappDaoFactorySDB wadf;
	private final SDBDatasetMode datasetMode;

	public ObjectPropertyStatementDaoSDB(RDFService rdfService,
			DatasetWrapperFactory dwf, SDBDatasetMode datasetMode,
			WebappDaoFactorySDB wadf) {
		super(rdfService, dwf, wadf);
		this.wadf = wadf;
		this.datasetMode = datasetMode;
	}

	@Override
	public Individual fillExistingObjectPropertyStatements(Individual entity) {
		if (entity == null || entity.getURI() == null)
			return entity;
		else {
			List<ObjectPropertyStatement> objectPropertyStatements = new ArrayList<>();
			String subjectUri = entity.getURI();

			Model m = getInfoForObjectsOfThisEntity(subjectUri);

			Set<String> subjectTypes = getTypes(m, subjectUri);
			for (ObjectPropertyPair pair : getRawObjectPropertyPairs(m,
					subjectUri)) {
				String predicateUri = pair.getPredicateUri();
				String objectUri = pair.getObjectUri();
				Set<String> objectTypes = getTypes(m, objectUri);

				ObjectProperty prop = findRawProperty(predicateUri);
				if (prop == null) {
					continue;
				}

				Individual object = new IndividualSDB(objectUri, dwf,
						datasetMode, wadf, m);
				objectPropertyStatements.add(createStatement(entity, prop,
						object));
			}
			entity.setObjectPropertyStatements(objectPropertyStatements);
			return entity;
		}
	}

	/**
	 * Get the types of this entity. Get the related object and the predicates
	 * by which they are related. Get the types and labels of those related
	 * objects.
	 */
	private Model getInfoForObjectsOfThisEntity(String subjectUri) {
		Model m = ModelFactory.createDefaultModel();
		try {
			m.add(RDFServiceUtils.parseModel(
					rdfService.sparqlConstructQuery(
							substituteUri(subjectUri, SUBJECT_TYPE_QUERY), N3),
					N3));
			m.add(RDFServiceUtils.parseModel(
					rdfService.sparqlConstructQuery(
							substituteUri(subjectUri, OBJECT_TYPE_QUERY), N3),
					N3));
			m.add(RDFServiceUtils.parseModel(
					rdfService.sparqlConstructQuery(
							substituteUri(subjectUri, OBJECT_LABEL_QUERY), N3),
					N3));
		} catch (RDFServiceException e) {
			log.warn("Failed to fill object property statements for '"
					+ subjectUri + "'", e);
		}
		return m;
	}

	private String substituteUri(String uri, String query) {
		return query.replace("?uri", "<" + uri + "> ");
	}

	private Set<String> getTypes(Model m, String uri) {
		Set<String> typeUris = new HashSet<>();
		for (RDFNode typeNode : m.listObjectsOfProperty(m.createResource(uri),
				RDF.type).toSet()) {
			if (typeNode.isURIResource()) {
				typeUris.add(typeNode.asResource().getURI());
			}
		}
		return typeUris;
	}

	private List<ObjectPropertyPair> getRawObjectPropertyPairs(Model m,
			String subjectUri) {
		List<ObjectPropertyPair> list = new ArrayList<>();
		for (Statement stmt : m.listStatements(m.createResource(subjectUri),
				null, (RDFNode) null).toList()) {
			if (wadf.getNonuserNamespaces().contains(
					stmt.getPredicate().getNameSpace())) {
				continue;
			}
			if (!stmt.getObject().isURIResource()) {
				continue;
			}
			list.add(new ObjectPropertyPair(stmt.getPredicate().getURI(), stmt
					.getObject().asResource().getURI()));
		}
		return list;
	}

	private ObjectProperty findRawProperty(String predicateUri) {
		return wadf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
	}

	private ObjectPropertyStatement createStatement(Individual entity,
			ObjectProperty prop, Individual object) {
		ObjectPropertyStatementImpl ops = new ObjectPropertyStatementImpl();
		ops.setSubject(entity);
		ops.setProperty(prop);
		ops.setObject(object);
		return ops;
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class ObjectPropertyPair {
		private final String predicateUri;
		private final String objectUri;

		public ObjectPropertyPair(String predicateUri, String objectUri) {
			this.predicateUri = predicateUri;
			this.objectUri = objectUri;
		}

		public String getPredicateUri() {
			return predicateUri;
		}

		public String getObjectUri() {
			return objectUri;
		}

	}

}
