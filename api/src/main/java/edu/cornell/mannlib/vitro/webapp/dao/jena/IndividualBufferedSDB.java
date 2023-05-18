package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB.IndividualNotFoundException;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;


public class IndividualBufferedSDB extends IndividualSDB {
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
            + "DESCRIBE <__INDIVIDUAL_IRI__> ?i1 ?i2 \n" 
            + " where {\n" + "  <__INDIVIDUAL_IRI__> ?p1 ?i1 \n"
            + "  FILTER(regex(str(?i1), \"individual\" )) \n"
            + "  ?i1 ?p2 ?i2 .\n"
            + "  FILTER(regex(str(?i2), \"individual\" )) \n" 
            + "}";


//    private OntModel _buffOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    private static final Log log = LogFactory.getLog(IndividualBufferedSDB.class.getName());    

    public IndividualBufferedSDB(String individualURI, DatasetWrapperFactory datasetWrapperFactory,
            SDBDatasetMode datasetMode, WebappDaoFactorySDB wadf)
            throws edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB.IndividualNotFoundException {
        super(individualURI, datasetWrapperFactory, datasetMode, wadf, true);
    }
    private List<String> _prefLangs;


    private Resource _individualJenaResource;


    private IndividualDaoJena _individualDaoJena;


    private IndividualJena _individualJena;


    private WebappDaoFactoryJena _jenaDaoFact;
    public Model populateIndividualBufferModel(String individualUri) {
        if (!_buffOntModel.isEmpty())
            return _buffOntModel;
        try {
            log.info("Loading the buffer model of :" +individualUri);
            _prefLangs = webappDaoFactory.config.getPreferredLanguages();
            _buffOntModel.getLock().enterCriticalSection(Lock.READ);
            _individualJenaResource = ResourceFactory.createResource(individualURI);
            String _query = indvGraphSparqlQuery.replace("__INDIVIDUAL_IRI__", individualUri);
            log.debug(_query);
            webappDaoFactory.getRDFService().sparqlConstructQuery(
                    indvGraphSparqlQuery.replace("__INDIVIDUAL_IRI__", individualUri), _buffOntModel);
//            RDFDataMgr.write(System.out, _buffOntModel, Lang.TURTLE);
            
            _jenaDaoFact = (new WebappDaoFactoryJena(_buffOntModel));
            _individualJena = new IndividualJena(_buffOntModel.getOntResource(individualUri), _jenaDaoFact);
            log.debug(_individualDaoJena);
        } catch (RDFServiceException e) {
            e.printStackTrace();
        } finally {
            _buffOntModel.getLock().leaveCriticalSection();
        }
        return _buffOntModel;
    }
    private Model populateIndividualBufferModel() {
        return populateIndividualBufferModel(this.individualURI);
    }

    public List<String> getMostSpecificTypeURIs() {
        populateIndividualBufferModel();
        return _individualJena.getMostSpecificTypeURIs();
    }
}
