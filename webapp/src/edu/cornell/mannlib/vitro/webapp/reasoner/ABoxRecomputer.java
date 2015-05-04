/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class ABoxRecomputer {

    private static final Log log = LogFactory.getLog(ABoxRecomputer.class);

    private final SearchIndexer searchIndexer;

    private OntModel tboxModel;             // asserted and inferred TBox axioms
    private OntModel aboxModel;
    private Model inferenceModel;
    private RDFService rdfService;
    private SimpleReasoner simpleReasoner;
    private Object lock1 = new Object();
    private volatile boolean recomputing = false;
    private boolean stopRequested = false;

    private final int BATCH_SIZE = 100;
    private final int REPORTING_INTERVAL = 1000;

    /**
     * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
     * @param aboxModel - input.  This model contains asserted ABox statements
     * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
     */
    public ABoxRecomputer(OntModel tboxModel,
            OntModel aboxModel,
            RDFService rdfService,
            SimpleReasoner simpleReasoner,
            SearchIndexer searchIndexer) {
        this.tboxModel = tboxModel;
        this.aboxModel = aboxModel;
        this.rdfService = rdfService;
        this.inferenceModel = RDFServiceGraph.createRDFServiceModel(
                new RDFServiceGraph(rdfService, ModelNames.ABOX_INFERENCES));
        this.simpleReasoner = simpleReasoner;
        this.searchIndexer = searchIndexer;
        recomputing = false;
        stopRequested = false;		
    }

    /**
     * Returns true if the recomputer is in the process of recomputing
     * all inferences.
     */
    public boolean isRecomputing() {
        return recomputing;
    }

    /**
     * Recompute all inferences.
     */
    public void recompute() {	
        synchronized (lock1) {
            if (recomputing) {
                return;
            } else {
                recomputing = true;
            }
        }
        try {
            if  (searchIndexer != null) {
                searchIndexer.pauseInAnticipationOfRebuild();
            }
            recomputeABox();
        } finally {
            if  (searchIndexer != null) {
                searchIndexer.rebuildIndex();
                searchIndexer.unpause();
            }
            synchronized (lock1) {
                recomputing = false;	    		
            }
        }
    }

    /*
     * Recompute the entire ABox inference graph.
     */
    protected void recomputeABox() {
        log.info("Recomputing ABox inferences.");
        log.info("Finding individuals in ABox.");
        Collection<String> individuals = this.getAllIndividualURIs();
        log.info("Recomputing inferences for " + individuals.size() + " individuals");
        long start = System.currentTimeMillis();
        int numInds = 0;
        Model rebuildModel = ModelFactory.createDefaultModel();
        Model additionalInferences = ModelFactory.createDefaultModel();
        List<String> individualsInBatch = new ArrayList<String>();
        Iterator<String> individualIt = individuals.iterator();
        while (individualIt.hasNext()) {
            String individualURI = individualIt.next();
            try {
                additionalInferences.add(recomputeIndividual(
                        individualURI, rebuildModel));
                numInds++;
                individualsInBatch.add(individualURI);
                boolean batchFilled = (numInds % BATCH_SIZE) == 0;
                boolean reportingInterval = (numInds % REPORTING_INTERVAL) == 0;
                if (batchFilled || !individualIt.hasNext()) {
                    log.debug(rebuildModel.size() + " total inferences");
                    updateInferenceModel(rebuildModel, individualsInBatch);
                    rebuildModel.removeAll();
                    individualsInBatch.clear();
                }
                if (reportingInterval) {
                    log.info("Still recomputing inferences (" 
                            + numInds + "/" + individuals.size() + " individuals)");
                    log.info((System.currentTimeMillis() - start) / numInds + " ms per individual");
                }
                if (stopRequested) {
                    log.info("a stopRequested signal was received during recomputeABox. Halting Processing.");
                    return;
                }
            } catch (Exception e) {
                log.error("Error recomputing inferences for individual <" + individualURI + ">", e);
            }
        }
        if(additionalInferences.size() > 0) {
            log.info("Writing additional inferences generated by reasoner plugins.");
            ChangeSet change = rdfService.manufactureChangeSet();
            change.addAddition(makeN3InputStream(additionalInferences), 
                    RDFService.ModelSerializationFormat.N3, ModelNames.ABOX_INFERENCES);
            try {
                rdfService.changeSetUpdate(change);
            } catch (RDFServiceException e) {
                log.error("Unable to write additional inferences from reasoner plugins", e);
            }
        }
        log.info("Finished recomputing inferences");
    }

    private static final boolean RUN_PLUGINS = true;
    private static final boolean SKIP_PLUGINS = !RUN_PLUGINS;

    private Model recomputeIndividual(String individualURI, 
            Model rebuildModel) throws RDFServiceException {
        long start = System.currentTimeMillis();
        Model assertions = getAssertions(individualURI);
        log.trace((System.currentTimeMillis() - start) + " ms to get assertions.");
        Model additionalInferences = recomputeIndividual(
                individualURI, null, assertions, rebuildModel, RUN_PLUGINS);

        if (simpleReasoner.getSameAsEnabled()) {
            Set<String> sameAsInds = getSameAsIndividuals(individualURI);
            for (String sameAsInd : sameAsInds) {
                // sameAs for plugins is handled by the SimpleReasoner
                Model sameAsIndAssertions = getAssertions(sameAsInd);
                recomputeIndividual(
                        sameAsInd, individualURI, sameAsIndAssertions, rebuildModel, SKIP_PLUGINS);
                rebuildModel.add(
                        rewriteInferences(getAssertions(sameAsInd), individualURI));
                Resource indRes = ResourceFactory.createResource(individualURI);
                Resource sameAsIndRes = ResourceFactory.createResource(sameAsInd); 
                if(!assertions.contains(indRes, OWL.sameAs, sameAsIndRes)) {
                    rebuildModel.add(indRes, OWL.sameAs, sameAsIndRes);
                }
            }
        }
        return additionalInferences;
    }

    /**
     * Adds inferences to temporary rebuildmodel
     * @param individualURI
     * @param rebuildModel
     * @return any additional inferences produced by plugins that affect other 
     *         individuals
     */
    private Model recomputeIndividual(String individualURI, String aliasURI, 
            Model assertions, Model rebuildModel, boolean runPlugins) 
                    throws RDFServiceException {

        Model additionalInferences = ModelFactory.createDefaultModel();
        Resource individual = ResourceFactory.createResource(individualURI);

        long start = System.currentTimeMillis();
        Model types = ModelFactory.createDefaultModel();
        types.add(assertions.listStatements(null, RDF.type, (RDFNode) null));
        Model inferredTypes = rewriteInferences(getInferredTypes(individual, types), aliasURI);
        rebuildModel.add(inferredTypes);
        log.trace((System.currentTimeMillis() - start) + " to infer " + inferredTypes.size() + " types");

        start = System.currentTimeMillis();
        types.add(inferredTypes);
        Model mst = getMostSpecificTypes(individual, types);
        rebuildModel.add(rewriteInferences(mst, aliasURI));
        log.trace((System.currentTimeMillis() - start) + " to infer " + mst.size() + " mostSpecificTypes");

        start = System.currentTimeMillis();
        Model inferredInvs = getInferredInverseStatements(individualURI);
        inferredInvs.remove(assertions);
        rebuildModel.add(rewriteInferences(inferredInvs, aliasURI));
        log.trace((System.currentTimeMillis() - start) + " to infer " + inferredInvs.size() + " inverses");

        List<ReasonerPlugin> pluginList = simpleReasoner.getPluginList();
        if (runPlugins && pluginList.size() > 0) {
            Model tmpModel = ModelFactory.createDefaultModel();
            StmtIterator sit = assertions.listStatements();
            while (sit.hasNext()) {
                Statement s = sit.nextStatement();
                for (ReasonerPlugin plugin : pluginList) {
                    plugin.addedABoxStatement(s, aboxModel, tmpModel, tboxModel);
                }
            }
            StmtIterator tmpIt = tmpModel.listStatements();
            while(tmpIt.hasNext()) {
                Statement tmpStmt = tmpIt.nextStatement();
                if(individual.equals(tmpStmt.getSubject())) {
                    rebuildModel.add(tmpStmt);
                } else {
                    additionalInferences.add(tmpStmt);
                }
            }
        }
        return additionalInferences;
    }

    private Model getAssertions(String individualURI) throws RDFServiceException {
        String queryStr = "CONSTRUCT { \n" +
                "    <" + individualURI + "> ?p ?value \n" +
                "} WHERE { \n" +
                "    GRAPH ?g { \n" +
                "        <" + individualURI + "> ?p ?value \n" +
                "    } \n" +
                "    FILTER (?g != <" + ModelNames.ABOX_INFERENCES + ">)\n" +
                "} \n";
        return RDFServiceUtils.parseModel(
                rdfService.sparqlConstructQuery(
                        queryStr, RDFService.ModelSerializationFormat.N3)
                        , RDFService.ModelSerializationFormat.N3);
    }

    private Model getInferredTypes(Resource individual, Model assertedTypes) {
        String queryStr = "CONSTRUCT { \n" +
                "    <" + individual.getURI() + "> a ?type \n" +
                "} WHERE { \n" +
                "    <" + individual.getURI() + "> a ?assertedType .\n" +
                "    { ?assertedType <" + RDFS.subClassOf.getURI() + "> ?type } \n" +
                "     UNION \n" +
                "    { ?assertedType <" + OWL.equivalentClass.getURI() + "> ?type } \n" +
                "    FILTER (isURI(?type)) \n" +
                "    FILTER NOT EXISTS { \n" +
                "        <" + individual.getURI() + "> a ?type \n" +
                "    } \n" +
                "} \n";
        Model union = ModelFactory.createUnion(assertedTypes, tboxModel);
        tboxModel.enterCriticalSection(Lock.READ);
        try {
            Query q = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(q, union);
            return qe.execConstruct();
        } finally {
            tboxModel.leaveCriticalSection();
        }
    }

    private Model getMostSpecificTypes(Resource individual, Model assertedTypes) {
        String queryStr = "CONSTRUCT { \n" +
                "    <" + individual.getURI() + "> <" + VitroVocabulary.MOST_SPECIFIC_TYPE + "> ?type \n" + 
                "} WHERE { \n" +
                "    <" + individual.getURI() + "> a ?type .\n" +
                "    FILTER (isURI(?type)) \n" +
                "    FILTER NOT EXISTS { \n" +
                "        <" + individual.getURI() + "> a ?type2 . \n" +
                "        ?type2 <" + RDFS.subClassOf.getURI() + "> ?type. \n" +
                "    } \n" +
                "    FILTER NOT EXISTS { \n" +
                "        <" + individual.getURI() + "> <" + VitroVocabulary.MOST_SPECIFIC_TYPE + "> ?type \n" +
                "    } \n" +
                "} \n";
        Model union = ModelFactory.createUnion(assertedTypes, tboxModel);
        tboxModel.enterCriticalSection(Lock.READ);
        try {
            Query q = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(q, union);
            return qe.execConstruct();
        } finally {
            tboxModel.leaveCriticalSection();
        }
    }

    private Model getInferredInverseStatements(String individualURI) throws RDFServiceException {
        String queryStr = "CONSTRUCT { \n" +
                "    <" + individualURI + "> ?inv ?value \n" +
                "} WHERE { \n" +
                "    GRAPH ?gr { \n" +
                "        ?value ?prop <" + individualURI + "> \n" +
                "    } \n" +
                "   FILTER (isURI(?value)) \n" +
                "   FILTER (?gr != <" + ModelNames.ABOX_INFERENCES + ">) \n" +
                "    { ?prop <" + OWL.inverseOf.getURI() + "> ?inv } \n" +
                "     UNION \n" +
                "    { ?inv <" + OWL.inverseOf.getURI() + "> ?prop } \n" +
                "} \n";
        return RDFServiceUtils.parseModel(
                rdfService.sparqlConstructQuery(
                        queryStr, RDFService.ModelSerializationFormat.N3)
                        , RDFService.ModelSerializationFormat.N3);
    }

    private Model rewriteInferences(Model inferences, String aliasURI) {
        if (aliasURI == null) {
            return inferences;
        }
        Model rewrite = ModelFactory.createDefaultModel();
        Resource alias = ResourceFactory.createResource(aliasURI);
        StmtIterator sit = inferences.listStatements();
        while(sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            rewrite.add(alias, stmt.getPredicate(), stmt.getObject());
        }
        return rewrite;
    }

    /*
     * Get the URIs for all individuals in the system
     */
    protected Collection<String> getAllIndividualURIs() {
        HashSet<String> individualURIs = new HashSet<String>();
        List<String> classList = new ArrayList<String>();
        tboxModel.enterCriticalSection(Lock.READ);
        try {
            StmtIterator classIt = tboxModel.listStatements(
                    (Resource) null, RDF.type, OWL.Class);
            while(classIt.hasNext()) {
                Statement stmt = classIt.nextStatement();
                if(stmt.getSubject().isURIResource() 
                        && stmt.getSubject().getURI() != null 
                        && !stmt.getSubject().getURI().isEmpty()) {
                    classList.add(stmt.getSubject().getURI());
                }
            }
        } finally {
            tboxModel.leaveCriticalSection();
        }
        for (String classURI : classList) {
            String queryString = "SELECT ?s WHERE { ?s a <" + classURI + "> } ";
            getIndividualURIs(queryString, individualURIs);
        }
        return individualURIs;
    }

    protected void getIndividualURIs(String queryString, Set<String> individuals) {
        int batchSize = 50000;
        int offset = 0;
        boolean done = false;
        while (!done) {
            String queryStr = queryString + " LIMIT " + batchSize + " OFFSET " + offset;
            if(log.isDebugEnabled()) {
                log.debug(queryStr);
            }
            ResultSet results = null;
            try {
                InputStream in = rdfService.sparqlSelectQuery(queryStr, RDFService.ResultFormat.JSON);
                results = ResultSetFactory.fromJSON(in);
            } catch (RDFServiceException e) {
                throw new RuntimeException(e);
            }
            if (!results.hasNext()) {
                done = true;
            }
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                Resource resource = solution.getResource("s");

                if ((resource != null) && !resource.isAnon()) {
                    individuals.add(resource.getURI());
                }					
            }
            if(log.isDebugEnabled()) {
                log.debug(individuals.size() + " in set");
            }
            offset += batchSize;
        }

    }

    /*
     * reconcile a set of inferences into the application inference model
     */
    protected void updateInferenceModel(Model rebuildModel, 
            Collection<String> individuals) throws RDFServiceException {
        Model existing = ModelFactory.createDefaultModel();
        for (String individualURI : individuals) {
            Resource subjInd = ResourceFactory.createResource(individualURI); 
            existing.add(inferenceModel.listStatements(subjInd, null, (RDFNode) null));           
        }
        Model retractions = existing.difference(rebuildModel);
        Model additions = rebuildModel.difference(existing);
        long start = System.currentTimeMillis();
        ChangeSet change = rdfService.manufactureChangeSet();
        change.addRemoval(makeN3InputStream(retractions), 
                RDFService.ModelSerializationFormat.N3, ModelNames.ABOX_INFERENCES);
        change.addAddition(makeN3InputStream(additions), 
                RDFService.ModelSerializationFormat.N3, ModelNames.ABOX_INFERENCES);
        rdfService.changeSetUpdate(change);
        log.debug((System.currentTimeMillis() - start) + 
                " ms to retract " + retractions.size() + 
                " statements and add " + additions.size() + " statements");
    }

    private InputStream makeN3InputStream(Model m) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.write(out, "N3");
        return new ByteArrayInputStream(out.toByteArray());
    }

    private Set<String> getSameAsIndividuals(String individualURI) {
        HashSet<String> sameAsInds = new HashSet<String>();
        sameAsInds.add(individualURI);
        getSameAsIndividuals(individualURI, sameAsInds);
        sameAsInds.remove(individualURI);
        return sameAsInds;
    }

    private void getSameAsIndividuals(String individualURI, Set<String> sameAsInds) {
        Model m = RDFServiceGraph.createRDFServiceModel(new RDFServiceGraph(rdfService));
        Resource individual = ResourceFactory.createResource(individualURI);
        StmtIterator sit = m.listStatements(individual, OWL.sameAs, (RDFNode) null);
        while(sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            if (stmt.getObject().isURIResource()) {
                String sameAsURI = stmt.getObject().asResource().getURI();
                if (!sameAsInds.contains(sameAsURI)) {
                    sameAsInds.add(sameAsURI);
                    getSameAsIndividuals(sameAsURI, sameAsInds);
                }
            }
        }
        sit = m.listStatements(null, OWL.sameAs, individual);
        while(sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            if (stmt.getSubject().isURIResource()) {
                String sameAsURI = stmt.getSubject().asResource().getURI();
                if (!sameAsInds.contains(sameAsURI)) {
                    sameAsInds.add(sameAsURI);
                    getSameAsIndividuals(sameAsURI, sameAsInds);
                }
            }
        }        
    }

    /**
     * This is called when the application shuts down.
     */
    public void setStopRequested() {
        this.stopRequested = true;
    }
}
