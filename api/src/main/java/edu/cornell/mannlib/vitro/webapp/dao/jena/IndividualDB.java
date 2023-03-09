/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

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
import edu.cornell.mannlib.vitro.webapp.dao.jena.exception.IndividualNotFoundException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * An extension of {@link IndividualJena} for databases, such as TDB.
 */
public class IndividualDB extends IndividualJena {

    private static final Log LOG = LogFactory.getLog(IndividualJena.class.getName());
    private static final String VITRO = "http://vitro.mannlib.cornell.edu/ns/vitro/public";

    private String indURI;
    private DatasetWrapper dw;
    private DatasetMode mode;
    private Model model;
    private Boolean hasThumb;

    /**
     * Initialize the individual based on an initialization model.
     *
     * @param indURI The URI representing the individual.
     * @param dw The data set wrapper.
     * @param mode The database mode filter to utilize.
     * @param wadf The DAO factory.
     * @param initModel The model to use in order to initialize the individual.
     */
    public IndividualDB(String indURI, DatasetWrapper dw, DatasetMode mode,
            WebappDaoFactoryDB wadf, Model initModel) {

        super(null, wadf);

        this.indURI = indURI;
        this.dw = dw;
        this.mode = mode;
        this.model = null;
        this.hasThumb = null;

        try {
            initModel.getLock().enterCriticalSection(Lock.READ);

            String getStatements =
                "CONSTRUCT \n" +
                "{ <" + indURI + ">  <" + RDFS.label.getURI() + "> ?ooo. \n" +
                    "<" + indURI + ">  a ?type . \n" +
                "} \n" +
                "WHERE { \n" +
                    "{ <" + indURI + ">  <" + RDFS.label.getURI() + "> ?ooo }  \n" +
                    " UNION { <" + indURI + "> a ?type } \n" +
                "} ";

            model = QueryExecutionFactory.create(
                QueryFactory.create(getStatements), initModel).execConstruct();
        } finally {
            initModel.getLock().leaveCriticalSection();
        }

        setInd(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model)
            .createOntResource(indURI));

        setupURIParts(getInd());
    }

    /**
     * Initialize the individual.
     *
     * @param indURI The URI representing the individual.
     * @param dw The data set wrapper.
     * @param mode The database mode filter to utilize.
     * @param wadf The DAO factory.
     *
     * @throws IndividualNotFoundException
     */
    public IndividualDB(String indURI, DatasetWrapper dw, DatasetMode mode,
            WebappDaoFactoryDB wadf)
            throws IndividualNotFoundException {

        this(indURI, dw, mode, wadf, false);
    }

    /**
     * Initialize the individual, possibly skipping the model initialization.
     *
     * @param indURI The URI representing the individual.
     * @param dw The data set wrapper.
     * @param mode The database mode filter to utilize.
     * @param wadf The DAO factory.
     * @param skipInitialization True to skip model initialize, False to
     *        perform model initialization.
     *
     * @throws IndividualNotFoundException
     */
    public IndividualDB(String indURI, DatasetWrapper dw, DatasetMode mode,
            WebappDaoFactoryDB wadf, boolean skipInitialization)
            throws IndividualNotFoundException {

        super(null, wadf);

        this.indURI = indURI;
        this.dw = dw;
        this.mode = mode;
        this.model = null;
        this.hasThumb = null;

        validateIndividualURI(indURI);

        if (skipInitialization) {
            setInd(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)
                .createOntResource(indURI));
        } else {
            try {
                String getStatements =
                    "CONSTRUCT " +
                    "{ <" + indURI + ">  <" + RDFS.label.getURI() + "> ?ooo \n" +
                    "} WHERE {" +
                        "{ <" + indURI + ">  <" + RDFS.label.getURI() + "> ?ooo } \n" +
                    "}";

                model = ModelFactory.createDefaultModel();
                getWebappDaoFactory().getRDFService().sparqlConstructQuery(getStatements, model);
            } catch (RDFServiceException e) {
                LOG.debug(e);
            }

            setInd(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model)
                .createOntResource(indURI));

            if (model == null || model.isEmpty() && noTriplesForUri()) {
                throw new IndividualNotFoundException();
            }
        }

        setupURIParts(getInd());
    }

    /**
     * Get the most specific type URI, using the RDF service.
     *
     * @return The list of URIs.
     */
    public List<String> getMostSpecificTypeURIs() {
        final List<String> typeURIs = new ArrayList<String>();

        if (this.getURI() == null) {
            return typeURIs;
        }

        String queryStr = "SELECT ?type WHERE { <" + this.getURI() + "> <" +
            VitroVocabulary.MOST_SPECIFIC_TYPE + "> ?type }";

        try {
            getWebappDaoFactory().getRDFService().sparqlSelectQuery(queryStr,
                new ResultSetConsumer() {
                    protected void processQuerySolution(QuerySolution qs) {
                        RDFNode node = qs.get("type");
                        if (node.isURIResource()) {
                            typeURIs.add(node.asResource().getURI());
                        }
                    }
                }
            );

            return typeURIs;
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determine if individual has a thumbnail URI.
     *
     * @return True if has thumbnail, false otherwise.
     */
    public boolean hasThumb() {
        if (hasThumb != null) {
            return hasThumb;
        }

        String ask =
            "ASK { " +
            "    <" + indURI + "> <" + VITRO + "#mainImage> ?mainImage . \n" +
            "    ?mainImage <" + VITRO + "#thumbnailImage> ?thumbImage . }\n"  ;
        try {
            hasThumb = getWebappDaoFactory().getRDFService().sparqlAskQuery(ask);
        } catch (Exception e) {
            LOG.error(e, e);
            hasThumb = false;
        }

        return hasThumb;
    }

    /**
     * Get data property statements for the given property URI.
     *
     * @param propertyUri The URI.
     *
     * @return The found list of statements.
     */
    public List<DataPropertyStatement> getDataPropertyStatements(String propertyUri) {
        List<DataPropertyStatement> stmts = this.dataPropertyStatements;
        if (stmts == null) {
            return sparqlForDataPropertyStatements(propertyUri);
        }

        List<DataPropertyStatement> stmtsForProp = new ArrayList<>();
        for (DataPropertyStatement stmt : stmts) {
            if (stmt.getDatapropURI().equals(propertyUri)) {
                stmtsForProp.add(stmt);
            }
        }

        return stmtsForProp;
    }

    /**
     * Get the search boost.
     *
     * @return The search boost.
     */
    public Float getSearchBoost() {
        String getPropertyValue =
            "SELECT ?value \n" +
            "WHERE { \n" +
            "<" + indURI + "> <" + getWebappDaoFactory().getJenaBaseDao().SEARCH_BOOST_ANNOT +
            "> ?value \n" +
            "}";

        Dataset dataset = dw.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        QueryExecution qe = QueryExecutionFactory.create(
            QueryFactory.create(getPropertyValue), dataset);

        try {
            ResultSet rs = qe.execSelect();
            if (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                if (qs.get("value") != null) {
                    Literal value = qs.get("value").asLiteral();
                    searchBoost = Float.parseFloat(value.getLexicalForm());
                    return searchBoost;
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            qe.close();
            dataset.getLock().leaveCriticalSection();
        }

        return null;
    }

    /**
     * Get VClasses.
     *
     * @param direct Only get direct VClasses rather than all.
     *
     * @return List of VClasses.
     */
    public List<VClass> getVClasses(boolean direct) {
        if (direct) {
            if (directVClasses == null) {
                directVClasses = getMyVClasses(true);
            }

            return directVClasses;
        }

        if (allVClasses == null) {
            allVClasses = getMyVClasses(false);
        }

        return allVClasses;
    }

    /**
     * Get the data values for the property URI.
     *
     * This is overridden to use Sparql queries from the RDF service.
     *
     * @param propertyUri The URI.
     *
     * @return The found list of values.
     *         Null is returned if propertyUri or getURI() is null.
     */
    public List<String> getDataValues(String propertyUri) {
        if (propertyUri == null) {
            LOG.error("Cannot retrieve value for null property");
            return null;
        }

        if (this.getURI() == null) {
            LOG.error("Cannot retrieve value of property " + propertyUri
                + " for anonymous individual");
            return null;
        }

        List<String> values = new ArrayList<>();
        List<DataPropertyStatement> stmts = sparqlForDataPropertyStatements(propertyUri);

        if (stmts != null) {
            for (DataPropertyStatement stmt : stmts) {
                values.add(stmt.getData());
            }
        }

        return values;
    }

    /**
     * Check if the URI is a VClass.
     *
     * The base method in {@link IndividualImpl} is adequate if the reasoner is up to date.
     *
     * If the base method returns false, check directly to see if any of the super classes of the
     * direct classes will satisfy this request.
     *
     * @param uri The URI to check.
     *
     * @return True if URI is a VClass, false otherwise.
     */
    public boolean isVClass(String uri) {
        if (uri == null || this.getURI() == null) {
            return false;
        }

        String queryString = "ASK { <" + this.getURI() + "> a <" + uri + "> }";
        try {
            return getWebappDaoFactory().getRDFService().sparqlAskQuery(queryString);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get web application DAO factory for databases, such as TDB.
     *
     * @return The web application DAO factory.
     */
    protected WebappDaoFactoryDB getWebappDaoFactory() {
        return (WebappDaoFactoryDB) super.getWebappDaoFactory();
    }

    /**
     * Check that indURI is valid.
     *
     * This is used to help prevent SPARQL injection attacks.
     *
     * @param indURI
     * @throws IndividualNotFoundException
     *
     * @see "https://www.w3.org/TR/rdf-sparql-query/#rIRI_REF"
     */
    private void validateIndividualURI(String indURI) 
            throws IndividualNotFoundException {

        // Check that indURI is valid. (Prevent SPARQL injection attack.)
        // Valid syntax is defined here: https://www.w3.org/TR/rdf-sparql-query/#rIRI_REF
        if (!indURI.matches("[^<>\"{}|^`\\\\\u0000-\u0020]*")) {
            throw new IndividualNotFoundException();
        }
    }

    /**
     * Determine if there are any triples for the given URI.
     *
     * @return True if there are no triples, false otherwise.
     */
    private boolean noTriplesForUri() {
        try {
            return !getWebappDaoFactory().getRDFService()
                .sparqlAskQuery("ASK { <" + indURI + "> ?p ?o }");
        } catch (RDFServiceException e) {
            LOG.debug(e);
        }

        return true;
    }

    /**
     * Execute sparql to select statements for the given property URI.
     *
     * @param propertyUri The URI.
     *
     * @return The found list of statements.
     */
    private List<DataPropertyStatement> sparqlForDataPropertyStatements(final String propertyUri) {
        final List<DataPropertyStatement> stmts = new ArrayList<>();
        final IndividualDB individualDB = this;

        String queryStr = "SELECT (str(?value) as ?valueString) WHERE { <" + this.getURI()
            + "> <" + propertyUri + "> ?value }";

        try {
            getWebappDaoFactory().getRDFService().sparqlSelectQuery(
                    queryStr, new ResultSetConsumer() {

                protected void processQuerySolution(QuerySolution qs) {
                    RDFNode node = qs.get("valueString");
                    if (!node.isLiteral()) {
                        LOG.debug("Ignoring non-literal value for " + node + " for property "
                            + propertyUri);
                    } else {
                        Literal lit = node.asLiteral();
                        DataPropertyStatement stmt = new DataPropertyStatementImpl();

                        stmt.setData(lit.getLexicalForm());
                        stmt.setDatatypeURI(lit.getDatatypeURI());
                        stmt.setLanguage(lit.getLanguage());
                        stmt.setDatapropURI(propertyUri);
                        stmt.setIndividualURI(individualDB.getURI());
                        stmt.setIndividual(individualDB);
                        stmts.add(stmt);
                    }
                }
            });
        } catch (RDFServiceException e) {
            LOG.error(e,e);
            throw new RuntimeException(e);
        }

        return stmts;
    }

    /**
     * Get object property statements for some property URI.
     *
     * @param propertyUri The URI.
     *
     * @return list of statements.
     */
    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyUri) {
        if (propertyUri == null) {
            return null;
        }

        List<ObjectPropertyStatement> objectPropertyStatements = new ArrayList<>();
        Model tempModel = ModelFactory.createDefaultModel();
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Dataset dataset = dw.getDataset();
        QueryExecution qexec = null;

        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            String valuesOfProperty =
                "CONSTRUCT{ <" + this.indURI + "> <" + propertyUri + "> ?object }" +
                "WHERE{ <" + this.indURI + "> <" + propertyUri + "> ?object } \n";

            qexec = QueryExecutionFactory.create(QueryFactory.create(valuesOfProperty), dataset);
            tempModel = qexec.execConstruct();
            ontModel.add(tempModel.listStatements());

            Resource ontRes = ontModel.getResource(this.indURI);
            StmtIterator sit = ontRes.listProperties(ontRes.getModel().getProperty(propertyUri));

            while (sit.hasNext()) {
                Statement s = sit.nextStatement();
                if (!s.getSubject().canAs(OntResource.class) || !s.getObject().canAs(OntResource.class)) {
                    continue;
                }

                Individual subj = null;
                Individual obj = null;

                try {
                    subj = new IndividualDB(s.getSubject()
                        .as(OntResource.class).getURI(), dw, mode, getWebappDaoFactory());
                } catch (IndividualNotFoundException e) {
                    // leave null subject
                }

                try {
                    obj = new IndividualDB(s.getObject()
                        .as(OntResource.class).getURI(), dw, mode, getWebappDaoFactory());
                } catch (IndividualNotFoundException e) {
                    // leave null object
                }

                ObjectProperty op = getWebappDaoFactory().getObjectPropertyDao()
                    .getObjectPropertyByURI(s.getPredicate().getURI());
                // We don't want to filter out statements simply because we
                // can't find a type for the property, so we'll just make a
                // new ObjectProperty bean if we can't get one from the DAO.
                if (op == null) {
                    op = new ObjectProperty();
                    op.setURI(propertyUri);
                }

                if (subj != null && obj != null) {
                    ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
                    ops.setSubject(subj);
                    ops.setSubjectURI(subj.getURI());
                    ops.setObject(obj);
                    ops.setObjectURI(obj.getURI());
                    ops.setProperty(op);
                    ops.setPropertyURI(op.getURI());
                    objectPropertyStatements.add(ops);
                }
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }

            tempModel.close();
            ontModel.close();
            dataset.getLock().leaveCriticalSection();
            dw.close();
        }

        return objectPropertyStatements;
    }

    /**
     * Get related individuals for some property URI.
     *
     * @param propertyUri The URI.
     *
     * @return list of related individuals.
     */
    public List<Individual> getRelatedIndividuals(String propertyUri) {
        if (propertyUri == null) {
            return null;
        }

        List<Individual> relatedIndividuals = new ArrayList<>();
        Dataset dataset = dw.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            String valuesOfProperty =
                "SELECT ?object " +
                "WHERE{ <" + this.indURI + "> <" + propertyUri + "> ?object } \n";
            ResultSet values = QueryExecutionFactory.create(
                QueryFactory.create(valuesOfProperty), dataset).execSelect();
            QuerySolution result = null;

            while (values.hasNext()) {
                result = values.next();
                RDFNode value = result.get("object");

                try {
                    if (value.canAs(OntResource.class)) {
                        relatedIndividuals.add(new IndividualDB(value.as(OntResource.class)
                            .getURI(), dw, mode, getWebappDaoFactory()));
                    }
                } catch (IndividualNotFoundException e) {
                    // don't add to the list
                }
            }
        } finally {
            dataset.getLock().leaveCriticalSection();
            dw.close();
        }

        return relatedIndividuals;
    }

    /**
     * Get a single related individual for some property URI.
     *
     * @param propertyUri The URI.
     *
     * @return list of related individuals.
     */
    public Individual getRelatedIndividual(String propertyUri) {
        if (propertyUri == null) {
            return null;
        }

        Dataset dataset = dw.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            String valueOfProperty =
                "SELECT ?object " +
                "WHERE{ <" + this.indURI + "> <" + propertyUri + "> ?object } \n";
            QueryExecution qe = QueryExecutionFactory.create(
                QueryFactory.create(valueOfProperty), dataset);

            try {
                ResultSet results = qe.execSelect();
                if (results.hasNext()) {
                    QuerySolution result = results.next();
                    RDFNode value = result.get("object");

                    if (value != null && value.canAs(OntResource.class)) {
                        try {
                            return new IndividualDB(value.as(OntResource.class)
                                .getURI(), dw, mode, getWebappDaoFactory());
                        } catch (IndividualNotFoundException e) {
                        }
                    }
                }

                return null;
            } finally {
                qe.close();
            }
        } finally {
            dataset.getLock().leaveCriticalSection();
            dw.close();
        }
    }

    /**
     * Get VClass for the individual.
     *
     * @param assertedOnly Set to TRUE to only get asserted, FALSE to get all.
     *
     * @return The list of VClass.
     */
    private List<VClass> getMyVClasses(boolean assertedOnly) {
        List<VClass> vClassList = new ArrayList<>();
        Model tempModel = null;

        if (getInd().getModel().contains((Resource) null, RDF.type, (RDFNode) null)){
            tempModel = getInd().getModel();
        } else {
            tempModel = ModelFactory.createDefaultModel();
            String getTypesQuery = buildMyVClassesQuery(assertedOnly);

            RDFService service = getWebappDaoFactory().getRDFService();
            try {
                service.sparqlConstructQuery(getTypesQuery, tempModel);
            } catch (RDFServiceException e) {
                throw new RuntimeException(e);
            }
        }

        StmtIterator stmtItr = tempModel.listStatements((Resource) null, RDF.type, (RDFNode) null);
        LinkedList<String> list = new LinkedList<>();

        while (stmtItr.hasNext()) {
            Statement stmt = stmtItr.nextStatement();
            if (stmt.getObject().isResource() && !stmt.getObject().isAnon()) {
                list.add(((Resource) stmt.getObject()).getURI());
            }
        }

        if (assertedOnly) {
            Iterator<String> itr = null;
            VClassDao checkSubClass = getWebappDaoFactory().getVClassDao();
            boolean directTypes = false;
            String currentType = null;
            ArrayList<String> done = new ArrayList<>();

            while (!directTypes) {
                itr = list.listIterator();

                do {
                    if (itr.hasNext()) {
                        currentType = itr.next();
                    } else {
                        directTypes = true;
                        break;
                    }
                } while (done.contains(currentType));

                if (directTypes) {
                    break;
                } else {
                    itr = list.listIterator();
                }

                while (itr.hasNext()) {
                    String nextType = itr.next();
                    if (checkSubClass.isSubClassOf(currentType, nextType)
                            && !currentType.equalsIgnoreCase(nextType)) {
                        itr.remove();
                    }
                }

                done.add(currentType);
            }
        }

        for (Iterator<String> it = list.iterator(); it.hasNext();) {
            Resource type = ResourceFactory.createResource(it.next().toString());
            if (type.getNameSpace() == null ||
               (!getWebappDaoFactory().getNonuserNamespaces().contains(type.getNameSpace())) ) {

                VClass vc = getWebappDaoFactory().getVClassDao().getVClassByURI(type.getURI());
                if (vc != null) {
                    vClassList.add(vc);
                }
            }
        }

        try {
            Collections.sort(vClassList);
        } catch (Exception e) {
            LOG.error("Unable to sort VClass list", e);
        }

        return vClassList;
    }

    /**
     * Build the VClass Sparql query.
     *
     * If we are restricting to asserted types, either by request or by dataset
     * mode, then filter by graph and include a UNION clause to support
     * retrieving inferred types from the unnamed base graph, as in Sesame and
     * OWLIM.
     *
     * @param assertedOnly Set to TRUE to only get asserted, FALSE to get all.
     *
     * @return The query for getting all of the individuals VClass.
     */
    private String buildMyVClassesQuery(boolean assertedOnly) {
        DatasetMode queryMode = assertedOnly ? DatasetMode.ASSERTIONS_ONLY : mode;

        String filterBlock = WebappDaoFactoryDB.getFilterBlock(new String[] { "?g" }, queryMode);

        if (filterBlock.isEmpty()) {
            return
                "CONSTRUCT { <" + indURI + "> " + "<" + RDF.type + "> ?types }\n" +
                "WHERE { <" + indURI + "> <" + RDF.type + "> ?types } \n";
        }

        String unionBlock = queryMode.equals(DatasetMode.ASSERTIONS_ONLY)
            ? ""
            : "UNION { <" + indURI +"> <" +RDF.type+ "> ?types }";

        return
            "CONSTRUCT{ <" + indURI + "> " + "<" + RDF.type + "> ?types }\n" +
            "WHERE{ { GRAPH ?g"
            + " { <" + indURI +"> <" + RDF.type + "> ?types } \n"
            + filterBlock
            + "} \n"
            + unionBlock
            + "} \n";
    }

}
