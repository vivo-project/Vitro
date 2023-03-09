/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.exception.IndividualNotFoundException;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDFS;
import org.joda.time.DateTime;

/**
 * An extension of {@link IndividualDaoJena} for databases, such as TDB.
 */
public class IndividualDaoDB extends IndividualDaoJena {

    private static final Log LOG = LogFactory.getLog(IndividualDaoDB.class.getName());

    private DatasetWrapper dw;
    private DatasetMode mode;

    /**
     * Initialize the individual DAO.
     *
     * @param dw The dataset wrapper.
     * @param mode The dataset mode.
     * @param wadf The web application DAO factory.
     */
    public IndividualDaoDB(DatasetWrapper dw, DatasetMode mode, WebappDaoFactoryDB wadf) {
        super(wadf);
        this.dw = dw;
        this.mode = mode;
    }

    /**
     * Get an individual by the URI, creating one if necessary.
     * 
     * @param entityURI The URI of the entity representing an individual.
     *
     * @return The individual.
     */
    public Individual getIndividualByURI(String entityURI) {
        if (entityURI == null || entityURI.length() == 0) {
            return null;
        }

        return makeIndividual(entityURI);
    }

    /**
     * Get all individuals based on VClass URI.
     *
     *@param vclassURI The VClass URI.
     *@param offset An offset to start from.
     *@param quantity A total amount of individuals to get.
     *
     * @return A list of individuals.
     */
    public List<Individual> getIndividualsByVClassURI(String vclassURI, int offset, int quantity) {
        if (vclassURI == null) {
            return null;
        }

        List<Individual> ents = new ArrayList<>();

        Resource theClass = (vclassURI.indexOf(PSEUDO_BNODE_NS) == 0)
            ? getOntModel().createResource(new AnonId(vclassURI.split("#")[1]))
            : ResourceFactory.createResource(vclassURI);

        if (theClass.isAnon() && theClass.canAs(UnionClass.class)) {
            UnionClass u = theClass.as(UnionClass.class);
            for (OntClass operand : u.listOperands().toList()) {
                VClass vc = new VClassJena(operand, getWebappDaoFactory());
                ents.addAll(getIndividualsByVClass(vc));
            }
        } else {
            // Check if there is a graph filter.
            // If so, we will use it in a slightly strange way.  Unfortunately,
            // performance is quite bad if we add several graph variables in
            // order to account for the fact that an individual's type
            // declaration may be in a different graph from its label.
            // Thus, we will run two queries: one with a single
            // graph variable to get the list of URIs, and a second against
            // the union graph to get individuals with their labels.
            // We will then toss out any individual in the second
            // list that is not also in the first list.
            // Annoying, yes, but better than the alternative.
            // Note that both queries need to sort identically or
            // the results may be very strange.
            String[] graphVars = {"?g"};
            String filterStr = WebappDaoFactoryDB.getFilterBlock(graphVars, mode);

            if (!StringUtils.isEmpty(filterStr)) {
                List<Individual> graphFilteredIndividualList =
                        getGraphFilteredIndividualList(theClass, filterStr);
                List<Individual> unfilteredIndividualList = getIndividualList(theClass);
                Iterator<Individual> unfilteredIt  = unfilteredIndividualList.iterator();

                for (Individual filt : graphFilteredIndividualList) {
                    Individual unfilt = unfilteredIt.next();
                    while (!unfilt.getURI().equals(filt.getURI())) {
                        unfilt = unfilteredIt.next();
                    }

                    ents.add(unfilt);
                }
            } else {
                ents = getIndividualList(theClass);
            }
        }

        Collections.sort(ents);

        if (quantity > 0 && offset > 0) {
            List<Individual> sublist = new ArrayList<>();
            for (int i = offset - 1; i < ((offset - 1) + quantity); i++) {
                sublist.add(ents.get(i));
            }

            return sublist;
        }

        return ents;
    }

    /**
     * Get individuals by data property using the full model.
     *
     * In Jena it can be difficult to get an object with a given dataproperty if
     * you do not care about the datatype or lang of the literal.  Use this
     * method if you would like to ignore the lang and datatype.
     *
     * Note: this method doesn't require that a property be declared in the
     * ontology as a data property -- only that it behaves as one.
     * 
     * @param dataPropertyUri The data property URI.
     * @param value The data property value.
     * 
     * @return The list of matching individuals.
     */
    @Override
    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value) {
        OntModel fullModel = getOntModelSelector().getFullModel();

        Property prop = null;
        if (RDFS.label.getURI().equals(dataPropertyUri)) {
            prop = RDFS.label;
        } else {
            prop = fullModel.getProperty(dataPropertyUri);
        }

        if (prop == null) {
            LOG.debug("Could not getIndividualsByDataProperty() because " + dataPropertyUri
                + "was not found in model.");
            return Collections.emptyList();
        }

        if( value == null ){
            LOG.debug("Could not getIndividualsByDataProperty() because value was null");
            return Collections.emptyList();
        }

        Literal litv1 = fullModel.createLiteral(value);
        Literal litv2 = fullModel.createTypedLiteral(value);

        //warning: this assumes that any language tags will be EN
        Literal litv3 = fullModel.createLiteral(value,"EN");

        HashMap<String, Individual> individualsMap = new HashMap<>();

        fullModel.enterCriticalSection(Lock.READ);
        try {
            StmtIterator stmts = fullModel.listStatements((Resource) null, prop, litv1);
            while (stmts.hasNext()) {
                Statement stmt = stmts.nextStatement();

                RDFNode sub = stmt.getSubject();
                if (sub == null || sub.isAnon() || sub.isLiteral()) {
                    continue;
                }

                RDFNode obj = stmt.getObject();
                if (obj == null || !obj.isLiteral()) {
                    continue;
                }

                Literal literal = (Literal) obj;
                Object v = literal.getValue();
                if (v == null) {
                    continue;
                }

                String subUri = ((Resource) sub).getURI();
                if (!individualsMap.containsKey(subUri)) {
                    individualsMap.put(subUri,makeIndividual(subUri));
                }
            }

            stmts = fullModel.listStatements((Resource) null, prop, litv2);
            while (stmts.hasNext()) {
                Statement stmt = stmts.nextStatement();

                RDFNode sub = stmt.getSubject();
                if (sub == null || sub.isAnon() || sub.isLiteral()) {
                    continue;
                }

                RDFNode obj = stmt.getObject();
                if (obj == null || !obj.isLiteral()) {
                    continue;
                }

                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if (v == null) {
                    continue;
                }

                String subUri = ((Resource)sub).getURI();
                if (!individualsMap.containsKey(subUri)) {
                    individualsMap.put(subUri, makeIndividual(subUri));
                }
            }

            stmts = fullModel.listStatements((Resource) null, prop, litv3);
            while (stmts.hasNext()) {
                Statement stmt = stmts.nextStatement();

                RDFNode sub = stmt.getSubject();
                if (sub == null || sub.isAnon() || sub.isLiteral()) {
                    continue;
                }

                RDFNode obj = stmt.getObject();
                if (obj == null || !obj.isLiteral()) {
                    continue;
                }

                Literal literal = (Literal) obj;
                Object v = literal.getValue();
                if (v == null) {
                    continue;
                }

                String subUri = ((Resource)sub).getURI();
                if (!individualsMap.containsKey(subUri)) {
                    individualsMap.put(subUri, makeIndividual(subUri));
                }
            }
        } finally {
            fullModel.leaveCriticalSection();
        }

        List<Individual> rv = new ArrayList<>(individualsMap.size());
        rv.addAll(individualsMap.values());
        return rv;
    }

    /**
     * Get each URI for all individuals.
     *
     * @return The list of URIs.
     */
    public Collection<String> getAllIndividualUris() {
        final List<String> list = new LinkedList<>();

        // get all labeled resources from any non-tbox and non-metadata graphs,
        // as well as the unnamed graph (first pattern below)
        String query = "SELECT DISTINCT ?ind WHERE { \n" +
                       " { ?ind <" + RDFS.label.getURI() + "> ?label } " +
                       " UNION { " +
                       "  GRAPH ?g { ?ind <" + RDFS.label.getURI() + "> ?label } \n" +
                       "  FILTER (?g != <" + ModelNames.APPLICATION_METADATA + "> " +
                       "          && !regex(str(?g),\"tbox\")) \n " +
                       " } " +
                       "}";

        Query q = QueryFactory.create(query);
        Dataset dataset = dw.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        QueryExecution qe = QueryExecutionFactory.create(q, dataset);
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                Resource res = rs.next().getResource("ind");
                if (!res.isAnon()) {
                    list.add(res.getURI());
                }
            }
        } finally {
            qe.close();
            dataset.getLock().leaveCriticalSection();
            dw.close();
        }

        return list;
    }

    /**
     * Get individual URIs updated since a given unit of time.
     *
     * @param updatedSince The instant in time, in milliseconds.
     *
     * @return A list of all individual URIs. 
     */
    public Iterator<String> getUpdatedSinceIterator(long updatedSince) {
        List<String> individualURIs = new ArrayList<String>();
        Date since = new DateTime(updatedSince).toDate();
        String sinceStr = xsdDateTimeFormat.format(since);

        getOntModel().enterCriticalSection(Lock.READ);
        try {
            String queryStr = "PREFIX vitro: <"+ VitroVocabulary.vitroURI+"> " +
                              "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
                              "SELECT ?ent " +
                              "WHERE { " +
                              "     ?ent vitro:modTime ?modTime ." +
                              "     FILTER (xsd:dateTime(?modTime) >= \""
                                    + sinceStr + "\"^^xsd:dateTime) " +
                              "}";
            Query query = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(query,getOntModel());
            try {
                ResultSet results = qe.execSelect();
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Resource res = (Resource) qs.get("?ent");
                    if (res.getURI() != null) {
                        individualURIs.add(res.getURI());
                    }
                }
            } finally {
                qe.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }

        return individualURIs.iterator();
    }

    /**
     * Get the web application DAO factory for databases such as TDB.
     *
     * @return The web application DAO factory.
     */
    protected WebappDaoFactoryDB getWebappDaoFactory() {
        return (WebappDaoFactoryDB) super.getWebappDaoFactory();
    }

    /**
     * Get the ontology model.
     *
     * @return the Ontology model.
     */
    protected OntModel getOntModel() {
        return getOntModelSelector().getABoxModel();
    }

    /**
     * Make an individual.
     *
     * @param indURI The URI of the individual.
     *
     * @return The made individual.
     */
    protected Individual makeIndividual(String indURI) {
        try {
            return new IndividualDB(indURI, dw, mode, getWebappDaoFactory());
        } catch (IndividualNotFoundException e) {
            LOG.debug("The individual represented by " + indURI + " does not exist");
        } catch (Exception ex) {
            LOG.error("An error occurred trying to make an individual ", ex);

            if (StringUtils.isNotEmpty(indURI)) {
                LOG.error("IndividualURI equals " + indURI);
            } else {
                LOG.error("IndividualURI is null or empty");
            }
        }

        return null;
    }

    /**
     * Get a list of individuals associated with the class resource.
     *
     * @param theClass The class resource.
     *
     * @return The list of individuals.
     */
    private List<Individual> getIndividualList(Resource theClass) {
        final List<Individual> ents = new ArrayList<>();
        Dataset dataset = dw.getDataset();

        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            String query =
                "SELECT DISTINCT ?ind ?label " +
                "WHERE " +
                 "{ \n" +
                    "{   ?ind a <" + theClass.getURI() + "> } \n" +
                    "UNION { \n" +
                    "    ?ind a <" + theClass.getURI() + "> . \n" +
                    "    ?ind  <" + RDFS.label.getURI() + "> ?label \n" +
                    "} \n" +
                 "} ORDER BY ?ind ?label";

            RDFService rdfService = getWebappDaoFactory().getRDFService();
            try {
                rdfService.sparqlSelectQuery(query, new ResultSetConsumer() {
                    String uri = null;
                    String label = null;

                    protected void processQuerySolution(QuerySolution qs) {
                        Resource currRes = qs.getResource("ind");
                        if (currRes.isAnon()) {
                            return;
                        }

                        if (uri != null && !uri.equals(currRes.getURI())) {
                            try {
                                ents.add(makeIndividual(uri, label));
                            } catch (IndividualNotFoundException e) {
                                // don't add
                            }

                            uri = currRes.getURI();
                            label = null;
                        } else if (uri == null) {
                            uri = currRes.getURI();
                        }

                        Literal labelLit = qs.getLiteral("label");
                        if (labelLit != null) {
                            label = labelLit.getLexicalForm();
                        }
                    }

                    @Override
                    protected void endProcessing() {
                        if (uri != null) {
                            try {
                                ents.add(makeIndividual(uri, label));
                            } catch (IndividualNotFoundException e) {
                                // don't add
                            }
                        }
                    }
                });
            } catch (RDFServiceException e) {
                LOG.debug(e);
                throw new RuntimeException(e);
            }
        } finally {
            dataset.getLock().leaveCriticalSection();
            dw.close();
        }

        return ents;
    }

    /**
     * Get a filtered list of individuals associated with the class resource.
     *
     * @param theClass The class resource.
     * @param filter The filter.
     *
     * @return The list of individuals.
     */
    private List<Individual> getGraphFilteredIndividualList(Resource theClass, String filter) {
        final List<Individual> filteredIndividualList = new ArrayList<>();
        Dataset dataset = dw.getDataset();

        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            String query =
                "SELECT DISTINCT ?ind " +
                "WHERE " +
                "{ GRAPH ?g { \n" +
                    "{   ?ind a <" + theClass.getURI() + "> } \n" +
                "  } \n" + filter +
                "} ORDER BY ?ind";
            RDFService rdfService = getWebappDaoFactory().getRDFService();
            try {
                rdfService.sparqlSelectQuery(query, new ResultSetConsumer() {
                    protected void processQuerySolution(QuerySolution qs) {
                        Resource currRes = qs.getResource("ind");
                        if (!currRes.isAnon()) {
                            try {
                                filteredIndividualList.add(makeIndividual(currRes.getURI(), null));
                            } catch (IndividualNotFoundException e) {
                                // don't add
                            }
                        }
                    }
                });
            } catch (RDFServiceException e) {
                LOG.debug(e);
                throw new RuntimeException(e);
            }
        } finally {
            dataset.getLock().leaveCriticalSection();
            //w.close();
        }

        return filteredIndividualList;
    }

    /**
     * Make an individual.
     *
     * @param indURI The URI of the individual.
     * @param label The label of the individual.
     *
     * @return The made individual.
     */
    private Individual makeIndividual(String indURI, String label)
            throws IndividualNotFoundException {

        Individual ent = new IndividualDB(
            indURI, dw, mode, (WebappDaoFactoryDB) getWebappDaoFactory(), true);

        ent.setName(label);
        ent.setRdfsLabel(label);
        return ent;
    }
}
