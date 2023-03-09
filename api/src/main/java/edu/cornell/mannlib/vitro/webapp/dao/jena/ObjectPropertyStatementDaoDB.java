/* $This file is distributed under the terms of the license in LICENSE$ */

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
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

/**
 * An extension of {@link ObjectPropertyStatementDaoJena} for databases, such as TDB.
 */
public class ObjectPropertyStatementDaoDB extends ObjectPropertyStatementDaoJena implements ObjectPropertyStatementDao {
    private static final Log LOG = LogFactory.getLog(ObjectPropertyStatementDaoDB.class);

    // Get the types of the base entity.
    private static final String SUBJECT_TYPE_QUERY =
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
        "CONSTRUCT { \n" +
        "   ?uri rdf:type ?type . \n" +
        "} WHERE { \n" +
        "   ?uri rdf:type ?type . \n" +
        "} \n";

    // Get the types of all objects of properties.
    private static final String OBJECT_TYPE_QUERY =
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
        "CONSTRUCT { \n" +
        "   ?uri ?p ?o . \n" +
        "   ?o rdf:type ?type . \n" +
        "} WHERE { \n" +
        "   ?uri ?p ?o . \n" +
        "   ?o rdf:type ?type . \n" +
        "} \n";

    // Get the labels of all objects of properties.
    private static final String OBJECT_LABEL_QUERY =
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
        "CONSTRUCT { \n" +
        "   ?uri ?p ?o . \n" +
        "   ?o rdfs:label ?label . \n" +
        "} WHERE { \n" +
        "   ?uri ?p ?o . \n" +
        "   ?o rdfs:label ?label . \n" +
        "} \n";

    private final WebappDaoFactoryDB wadf;
    private final DatasetMode mode;

    /**
     * Initialize the object property statement DAO.
     *
     * @param rdfService The rdf service.
     * @param dwf The data set wrapper.
     * @param mode The data set mode.
     * @param wadf The web application DAO factory.
     */
    public ObjectPropertyStatementDaoDB(RDFService rdfService, DatasetWrapper dw, DatasetMode mode,
            WebappDaoFactoryDB wadf) {

        super(rdfService, dw, wadf);
        this.wadf = wadf;
        this.mode = mode;
    }

    /**
     * Fill the existing object property statement for the given individual.
     *
     * @param entity The individual.
     *
     * @return The filled individual or null.
     */
    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        if (entity == null || entity.getURI() == null) {
            return entity;
        }

        List<ObjectPropertyStatement> objectPropertyStatements = new ArrayList<>();
        String subjectUri = entity.getURI();

        Model m = getInfoForObjectsOfThisEntity(subjectUri);

        for (ObjectPropertyPair pair : getRawObjectPropertyPairs(m,
                subjectUri)) {
            String predicateUri = pair.getPredicateUri();
            String objectUri = pair.getObjectUri();

            ObjectProperty prop = findRawProperty(predicateUri);
            if (prop == null) {
                continue;
            }

            Individual object = new IndividualDB(objectUri, dw, mode, wadf, m);
            objectPropertyStatements.add(createStatement(entity, prop, object));
        }
        entity.setObjectPropertyStatements(objectPropertyStatements);
        return entity;
    }

    /**
     * Get the information for objects of the given entity.
     *
     * Get the types of this entity. Get the related object and the predicates
     * by which they are related. Get the types and labels of those related
     * objects.
     *
     * @param subjectUri The URL representing the entity.
     *
     * @return The model representing the information for objects.
     */
    private Model getInfoForObjectsOfThisEntity(String subjectUri) {
        Model m = ModelFactory.createDefaultModel();

        try {
            m.add(RDFServiceUtils.parseModel(rdfService.sparqlConstructQuery(
                substituteUri(subjectUri, SUBJECT_TYPE_QUERY), N3), N3));
            m.add(RDFServiceUtils.parseModel(rdfService.sparqlConstructQuery(
                substituteUri(subjectUri, OBJECT_TYPE_QUERY), N3), N3));
            m.add(RDFServiceUtils.parseModel(rdfService.sparqlConstructQuery(
                substituteUri(subjectUri, OBJECT_LABEL_QUERY), N3), N3));
        } catch (RDFServiceException e) {
            LOG.warn("Failed to fill object property statements for '" + subjectUri + "'", e);
        }

        return m;
    }

    /**
     * Substitute the "?uri" text with a given URI string in the query.
     *
     * @param uri The URI to replace with.
     * @param query The query to be updated.
     *
     * @return The altered query.
     */
    private String substituteUri(String uri, String query) {
        return query.replace("?uri", "<" + uri + "> ");
    }

    /**
     * Get types for some model and URI.
     *
     * @param m The model.
     * @param uri The URI.
     *
     * @return A set of types.
     */
    private Set<String> getTypes(Model m, String uri) {
        Set<String> typeUris = new HashSet<>();

        NodeIterator iter = m.listObjectsOfProperty(m.createResource(uri), RDF.type);
        for (RDFNode typeNode : iter.toSet()) {
            if (typeNode.isURIResource()) {
                typeUris.add(typeNode.asResource().getURI());
            }
        }

        return typeUris;
    }

    /**
     * Get raw object property pairs.
     *
     * @param m The model.
     * @param subjectUri The subject URI.
     *
     * @return An array of object property pairs.
     */
    private List<ObjectPropertyPair> getRawObjectPropertyPairs(Model m, String subjectUri) {
        List<ObjectPropertyPair> list = new ArrayList<>();

        StmtIterator iter = m.listStatements(m.createResource(subjectUri), null, (RDFNode) null);
        for (Statement stmt : iter.toList()) {
            if (wadf.getNonuserNamespaces().contains(stmt.getPredicate().getNameSpace())) {
                continue;
            }

            if (!stmt.getObject().isURIResource()) {
                continue;
            }

            list.add(new ObjectPropertyPair(stmt.getPredicate().getURI(),
                stmt.getObject().asResource().getURI()));
        }

        return list;
    }

    /**
     * Find the raw property for some predicate URI.
     *
     * @param predicateUri The predicate URI.
     *
     * @return The raw object property.
     */
    private ObjectProperty findRawProperty(String predicateURI) {
        return wadf.getObjectPropertyDao().getObjectPropertyByURI(predicateURI);
    }

    /**
     * Create a statement from the given triple.
     *
     * @param entity The individual subject.
     * @param prop The individual property.
     * @param object The individual object (value).
     *
     * @return The object property statement.
     */
    private ObjectPropertyStatement createStatement(Individual entity, ObjectProperty prop,
            Individual object) {

        ObjectPropertyStatementImpl ops = new ObjectPropertyStatementImpl();
        ops.setSubject(entity);
        ops.setProperty(prop);
        ops.setObject(object);
        return ops;
    }

    /**
     * Helper class for a predicate URI and an object pair URI.
     */
    private static class ObjectPropertyPair {
        private final String predicateURI;
        private final String objectURI;

        public ObjectPropertyPair(String predicateUri, String objectUri) {
            this.predicateURI = predicateUri;
            this.objectURI = objectUri;
        }

        public String getPredicateUri() {
            return predicateURI;
        }

        public String getObjectUri() {
            return objectURI;
        }
    }

}
