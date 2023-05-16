/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode.ASSERTIONS_ONLY;

import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.joda.time.DateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

public class IndividualSDBBuffered extends IndividualSDB {

    private String indvGraphSparqlQuery = "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n"
            + "PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n"
            + "PREFIX swrl:     <http://www.w3.org/2003/11/swrl#>\n"
            + "PREFIX swrlb:    <http://www.w3.org/2003/11/swrlb#>\n"
            + "PREFIX vitro:    <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n"
            + "PREFIX c4o:      <http://purl.org/spar/c4o/>\n" + "PREFIX cito:     <http://purl.org/spar/cito/>\n"
            + "PREFIX event:    <http://purl.org/NET/c4dm/event.owl#>\n"
            + "PREFIX fabio:    <http://purl.org/spar/fabio/>\n" + "PREFIX foaf:     <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX dcterms:  <http://purl.org/dc/terms/>\n" + "PREFIX vann:     <http://purl.org/vocab/vann/>\n"
            + "PREFIX swo:      <http://www.ebi.ac.uk/efo/swo/>\n"
            + "PREFIX obo:      <http://purl.obolibrary.org/obo/>\n"
            + "PREFIX bibo:     <http://purl.org/ontology/bibo/>\n"
            + "PREFIX geo:      <http://aims.fao.org/aos/geopolitical.owl#>\n"
            + "PREFIX ocresd:   <http://purl.org/net/OCRe/study_design.owl#>\n"
            + "PREFIX ocrer:    <http://purl.org/net/OCRe/research.owl#>\n"
            + "PREFIX ro:       <http://purl.obolibrary.org/obo/ro.owl#>\n"
            + "PREFIX skos:     <http://www.w3.org/2004/02/skos/core#>\n"
            + "PREFIX ocresst:  <http://purl.org/net/OCRe/statistics.owl#>\n"
            + "PREFIX ocresp:   <http://purl.org/net/OCRe/study_protocol.owl#>\n"
            + "PREFIX vcard:    <http://www.w3.org/2006/vcard/ns#>\n"
            + "PREFIX p1:       <http://vivoweb.org/ontology/vitroAnnotfr_CA#>\n"
            + "PREFIX vitro-public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>\n"
            + "PREFIX vivo:     <http://vivoweb.org/ontology/core#>\n"
            + "PREFIX scires:   <http://vivoweb.org/ontology/scientific-research#>\n"
            + "DESCRIBE <__INDIVIDUAL_IRI__> ?i1 \n" + " where {\n" + "  <__INDIVIDUAL_IRI__> ?p1 ?i1 \n"
            + "  FILTER(regex(str(?i1), \"individual\" )) \n" + "}";

//    + "  ?i1 ?p2 ?i2 .\n"
//    + "  FILTER(regex(str(?i2), \"individual\" )) \n" 

    private OntModel _buffOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    private static final Log log = LogFactory.getLog(IndividualSDBBuffered.class.getName());
    private List<String> _prefLangs;

    private Resource _individualJenaResource;

    private Property toPredicate(String IRI) {
        return ResourceFactory.createProperty(IRI);
    }

    private Property MOST_SPECIFIC_TYPE = toPredicate(VitroVocabulary.MOST_SPECIFIC_TYPE);

    public IndividualSDBBuffered(String individualURI, DatasetWrapperFactory datasetWrapperFactory,
            SDBDatasetMode datasetMode, WebappDaoFactorySDB wadf, Model initModel) {
        super(individualURI, datasetWrapperFactory, datasetMode, wadf, initModel);
    }

    public IndividualSDBBuffered(String individualURI, DatasetWrapperFactory datasetWrapperFactory,
            SDBDatasetMode datasetMode, WebappDaoFactorySDB wadf, boolean skipInitialization)
            throws edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB.IndividualNotFoundException {
        super(individualURI, datasetWrapperFactory, datasetMode, wadf, skipInitialization);
    }

    public IndividualSDBBuffered(String individualURI, DatasetWrapperFactory datasetWrapperFactory,
            SDBDatasetMode datasetMode, WebappDaoFactorySDB wadf)
            throws edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB.IndividualNotFoundException {
        super(individualURI, datasetWrapperFactory, datasetMode, wadf);
    }

    public Model populateIndividualBufferModel(String individualUri) throws RDFServiceException {
        if (!_buffOntModel.isEmpty())
            return _buffOntModel;
        try {
            _prefLangs = webappDaoFactory.config.getPreferredLanguages();
            _buffOntModel.getLock().enterCriticalSection(Lock.READ);
            _individualJenaResource = ResourceFactory.createResource(individualURI);
            String _query = indvGraphSparqlQuery.replace("__INDIVIDUAL_IRI__", individualUri);
            log.debug(_query);
            webappDaoFactory.getRDFService().sparqlConstructQuery(
                    indvGraphSparqlQuery.replace("__INDIVIDUAL_IRI__", individualUri), _buffOntModel);
            // RDFDataMgr.write(System.out, _buffOntModel, Lang.TURTLE);
        } finally {
            _buffOntModel.getLock().leaveCriticalSection();
        }
        return _buffOntModel;
    }

    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyURI) {
        try {
            populateIndividualBufferModel(this.individualURI);
        } catch (RDFServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<ObjectPropertyStatement> objectPropertyStatements = new ArrayList<ObjectPropertyStatement>();
        Resource subj = ResourceFactory.createResource(individualURI);
        Property pred = ResourceFactory.createProperty(propertyURI);

        List<Statement> stmts = _buffOntModel.listStatements(subj, pred, (Resource) null).toList();
        for (Iterator iterator = stmts.iterator(); iterator.hasNext();) {
            Statement stmt = (Statement) iterator.next();
            ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
            Individual _subj = new IndividualImpl(stmt.getSubject().getURI());
            Individual _obj = new IndividualImpl(stmt.getObject().asResource().getURI());
            ObjectProperty _pred = new ObjectProperty();
            _pred.setURI(stmt.getPredicate().getURI());
            ops.setSubject(_subj);
            ops.setSubjectURI(_subj.getURI());
            ops.setObject(_obj);
            ops.setObjectURI(_obj.getURI());
            ops.setProperty(_pred);
            ops.setPropertyURI(_pred.getURI());
            objectPropertyStatements.add(ops);
        }
        return objectPropertyStatements;

    }

    public List<String> getMostSpecificTypeURIs() {
        try {
            populateIndividualBufferModel(this.individualURI);
        } catch (RDFServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final List<String> typeURIs = new ArrayList<String>();
        if (this.getURI() == null) {
            return typeURIs;
        } else {
            /*
             * Equivalent to String queryStr = "SELECT ?type WHERE { <" + this.getURI() +
             * "> <" + VitroVocabulary.MOST_SPECIFIC_TYPE + "> ?type }";
             */
            List<Statement> stmts = _buffOntModel.listStatements(_individualJenaResource, MOST_SPECIFIC_TYPE, (Resource) null).toList();
            for (Iterator<Statement> iterator = stmts.iterator(); iterator.hasNext();) {
                RDFNode node = (RDFNode) iterator.next().getObject();
                if (node.isURIResource()) {
                    typeURIs.add(node.asResource().getURI());
                }
            }
        }
        return typeURIs;
    }

    public String getMainImageUri() {
        if (this.mainImageUri != NOT_INITIALIZED) {
            return mainImageUri;
        } else {
            List<ObjectPropertyStatement> mainImgStmts = getObjectPropertyStatements(VitroVocabulary.IND_MAIN_IMAGE);
            if (mainImgStmts != null && mainImgStmts.size() > 0) {
                // arbitrarily return the first value in the list
                mainImageUri = mainImgStmts.get(0).getObjectURI();
                return mainImageUri;
            }
            return null;
        }
    }

    public String toString() {
        return "IndividualSDB [\n" + (individualURI != null ? "individualURI=" + individualURI + ", \n" : "")
                + (name != null ? "name=" + name + ", \n" : "")
                + (rdfsLabel != null ? "rdfsLabel=" + rdfsLabel + ", \n" : "")
                + (vClassURI != null ? "vClassURI=" + vClassURI + ", \n" : "")
                + (vClass != null ? "vClass=" + vClass + ", \n" : "")
                + (directVClasses != null ? "directVClasses=" + directVClasses + ", \n" : "")
                + (allVClasses != null ? "allVClasses=" + allVClasses + ", \n" : "")
                + (modTime != null ? "modTime=" + modTime + ", \n" : "")
                + (propertyList != null ? "propertyList=" + propertyList + ", \n" : "")
                + (populatedObjectPropertyList != null
                        ? "populatedObjectPropertyList=" + populatedObjectPropertyList + ", \n"
                        : "")
                + (objectPropertyMap != null ? "objectPropertyMap=" + objectPropertyMap + ", \n" : "")
                + (datatypePropertyList != null ? "datatypePropertyList=" + datatypePropertyList + ", \n" : "")
                + (populatedDataPropertyList != null ? "populatedDataPropertyList=" + populatedDataPropertyList + ", \n"
                        : "")
                + (dataPropertyMap != null ? "dataPropertyMap=" + dataPropertyMap + ", \n" : "")
                + (dataPropertyStatements != null ? "dataPropertyStatements=" + dataPropertyStatements + ", \n" : "")
                + (objectPropertyStatements != null ? "objectPropertyStatements=" + objectPropertyStatements + ", \n"
                        : "")
                + (rangeEnts2Ents != null ? "rangeEnts2Ents=" + rangeEnts2Ents + ", \n" : "")
                + (externalIds != null ? "externalIds=" + externalIds + ", \n" : "")
                + (mainImageUri != null ? "mainImageUri=" + mainImageUri + ", \n" : "")
                + (imageInfo != null ? "imageInfo=" + imageInfo + ", \n" : "")
                + (namespace != null ? "namespace=" + namespace + ", \n" : "")
                + (localName != null ? "localName=" + localName + ", \n" : "")
                + (localNameWithPrefix != null ? "localNameWithPrefix=" + localNameWithPrefix + ", \n" : "")
                + (pickListName != null ? "pickListName=" + pickListName : "\n") + "]";
    }

}
