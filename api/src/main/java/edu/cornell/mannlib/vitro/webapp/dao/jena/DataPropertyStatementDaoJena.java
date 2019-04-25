/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;

public class DataPropertyStatementDaoJena extends JenaBaseDao implements DataPropertyStatementDao
{
    private static final Log log = LogFactory.getLog(DataPropertyStatementDaoJena.class);


    private DatasetWrapperFactory dwf;

    public DataPropertyStatementDaoJena(DatasetWrapperFactory dwf,
                                        WebappDaoFactoryJena wadf) {
        super(wadf);
        this.dwf = dwf;
    }


    public void deleteDataPropertyStatement( DataPropertyStatement dataPropertyStatement )
    {
        OntModel ontModel = getOntModelSelector().getABoxModel();
        try {
            ontModel.enterCriticalSection(Lock.WRITE);
            getOntModel().getBaseModel().notifyEvent(
                    new IndividualUpdateEvent(
                            getWebappDaoFactory().getUserURI(),
                            true,
                            dataPropertyStatement.getIndividualURI()));
            org.apache.jena.ontology.Individual ind = ontModel.getIndividual(
                        dataPropertyStatement.getIndividualURI());
            OntModel tboxModel = getOntModelSelector().getTBoxModel();
            tboxModel.enterCriticalSection(Lock.READ);
            try {
                Property prop = tboxModel.getProperty(
                        dataPropertyStatement.getDatapropURI());
                Literal l = jenaLiteralFromDataPropertyStatement(
                        dataPropertyStatement, ontModel);
                if (ind != null && prop != null && l != null) {
                    ontModel.getBaseModel().remove(ind, prop, l);
                }
            } finally {
                tboxModel.leaveCriticalSection();
            }
        } finally {
            getOntModel().getBaseModel().notifyEvent(
                    new IndividualUpdateEvent(
                            getWebappDaoFactory().getUserURI(),
                            false,
                            dataPropertyStatement.getIndividualURI()));
            ontModel.leaveCriticalSection();
        }
    }

    public Individual fillExistingDataPropertyStatementsForIndividual( Individual entity/*, boolean allowAnyNameSpace*/)
    {
        if( entity.getURI() == null )
        {
            return entity;
        }
        else
        {
        	OntModel ontModel = getOntModelSelector().getABoxModel();
            ontModel.enterCriticalSection(Lock.READ);
            try {
                Resource ind = ontModel.getResource(entity.getURI());
                List<DataPropertyStatement> edList = new ArrayList<DataPropertyStatement>();
                StmtIterator stmtIt = ind.listProperties();
                while( stmtIt.hasNext() )
                {
                    Statement st = stmtIt.next();
                    boolean addToList = /*allowAnyNameSpace ? st.getObject().canAs(Literal.class) :*/ st.getObject().isLiteral() &&
                          (
                              (RDF.value.equals(st.getPredicate()) || VitroVocabulary.value.equals(st.getPredicate().getURI()))
                              || !(NONUSER_NAMESPACES.contains(st.getPredicate().getNameSpace()))
                          );
                    if( addToList )
                    {   /* now want to expose Cornellemailnetid and potentially other properties so can at least control whether visible
                        boolean isExternalId = false;
                        ClosableIterator externalIdStmtIt = getOntModel().listStatements(st.getPredicate(), DATAPROPERTY_ISEXTERNALID, (Literal)null);
                        try {
                            if (externalIdStmtIt.hasNext()) {
                                isExternalId = true;
                            }
                        } finally {
                            externalIdStmtIt.close();
                        }
                        if (!isExternalId) { */
                        DataPropertyStatement ed = new DataPropertyStatementImpl();
                        Literal lit = (Literal)st.getObject();
                        fillDataPropertyStatementWithJenaLiteral(ed,lit);
                        ed.setDatapropURI(st.getPredicate().getURI());
                        ed.setIndividualURI(ind.getURI());
                        ed.setIndividual(entity);
                        edList.add(ed);
                     /* } */
                    }
                }
                entity.setDataPropertyStatements(edList);
                return entity;
            } finally {
                ontModel.leaveCriticalSection();
            }
        }
    }

    public void deleteDataPropertyStatementsForIndividualByDataProperty(String individualURI, String dataPropertyURI) {
        deleteDataPropertyStatementsForIndividualByDataProperty(individualURI, dataPropertyURI, getOntModelSelector().getABoxModel());
    }

    public void deleteDataPropertyStatementsForIndividualByDataProperty(
            String individualURI,
            String dataPropertyURI,
            OntModel ontModel) {

        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(
                getWebappDaoFactory().getUserURI(),
                true,
                individualURI));
        try {
            Resource indRes = ResourceFactory.createResource(individualURI);
            Property datatypeProperty = ResourceFactory.createProperty(
                    dataPropertyURI);
            ontModel.removeAll(indRes, datatypeProperty, (Literal)null);
        } catch(Exception ex) {
        	log.error("Error occurred in removal of data property " + dataPropertyURI + " for " + individualURI);
        }
        finally {

        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(
        	        getWebappDaoFactory().getUserURI(),
        	        false,
        	        individualURI));
            ontModel.leaveCriticalSection();
        }

    }

    public void deleteDataPropertyStatementsForIndividualByDataProperty(Individual individual, DataProperty dataProperty) {
    	this.deleteDataPropertyStatementsForIndividualByDataProperty(individual.getURI(), dataProperty.getURI());
    }

    public Collection<DataPropertyStatement> getDataPropertyStatementsForIndividualByDataPropertyURI(Individual entity,
            String datapropURI) {
    	Collection<DataPropertyStatement> edList = new ArrayList<DataPropertyStatement>();
    	if (entity.getURI() == null || datapropURI == null) {
			return edList;
		}
    	// do something nicer if we're not dealing with a blank node
    	Resource res = ResourceFactory.createResource(entity.getURI());
    	if (!VitroVocabulary.PSEUDO_BNODE_NS.equals(entity.getNamespace())) {
    		for (Literal lit : this.getDataPropertyValuesForIndividualByProperty(res.getURI(), datapropURI)) {
    		    log.debug("Literal lit = " + lit);
    			DataPropertyStatement ed = new DataPropertyStatementImpl();
    			fillDataPropertyStatementWithJenaLiteral(ed, lit);
                ed.setIndividualURI(entity.getURI());
                ed.setIndividual(entity);
                ed.setDatapropURI(datapropURI);
                edList.add(ed);
    		}
    		return edList;
    	}
        // do something annoying if we are dealing with a blank node
    	try {
	    	getOntModel().enterCriticalSection(Lock.READ);
	        OntResource ontRes = getOntModel().createResource(
	        		new AnonId(entity.getLocalName())).as(OntResource.class);
	        if (ontRes == null) {
	        	return edList;
	        }
	        ClosableIterator stmtIt;
	        stmtIt = (datapropURI != null) ? ontRes.listProperties(getOntModel().getProperty(datapropURI)) : ontRes.listProperties();
	        try {
	            while (stmtIt.hasNext()) {
	                Statement st = (Statement) stmtIt.next();
	                if (st.getObject().isLiteral()) {
	                    DataPropertyStatement ed = new DataPropertyStatementImpl();
	                    Literal lit = (Literal)st.getObject();
	                    fillDataPropertyStatementWithJenaLiteral(ed, lit);
	                    ed.setIndividualURI(entity.getURI());
	                    ed.setIndividual(entity);
	                    ed.setDatapropURI(st.getPredicate().getURI());
	                    edList.add(ed);
	                }
	            }
	        } finally {
	            stmtIt.close();
	        }
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
        return edList;
    }

    @Deprecated
    public List getExistingQualifiers(String datapropURI) {
        // TODO Auto-generated method stub
        return null;
    }

    private int NO_LIMIT = -1;

    public List<DataPropertyStatement> getDataPropertyStatements(DataProperty dp) {
    	return getDataPropertyStatements(dp, NO_LIMIT, NO_LIMIT);
    }

    public List<DataPropertyStatement> getDataPropertyStatements(DataProperty dp, int startIndex, int endIndex) {
    	getOntModel().enterCriticalSection(Lock.READ);
    	List<DataPropertyStatement> dpss = new ArrayList<DataPropertyStatement>();
    	try {
    		Property prop = ResourceFactory.createProperty(dp.getURI());
    		ClosableIterator dpsIt = getOntModel().listStatements(null,prop,(Literal)null);
    		try {
    			int count = 0;
    			while ( (dpsIt.hasNext()) && ((endIndex<0) || (count<endIndex)) ) {
    				++count;
    				Statement stmt = (Statement) dpsIt.next();
    				if (startIndex<0 || startIndex<=count) {
    					Literal lit = (Literal) stmt.getObject();
	    				DataPropertyStatement dps = new DataPropertyStatementImpl();
	    				dps.setDatapropURI(dp.getURI());
	    				dps.setIndividualURI(stmt.getSubject().getURI());
	    				fillDataPropertyStatementWithJenaLiteral(dps,lit);
	    				dpss.add(dps);
    				}
    			}
    		} finally {
    			dpsIt.close();
    		}
    	} finally {
    		getOntModel().leaveCriticalSection()
;    	}
    	return dpss;
    }

    public int insertNewDataPropertyStatement(DataPropertyStatement dataPropertyStmt) {
    	return insertNewDataPropertyStatement(dataPropertyStmt, getOntModelSelector().getABoxModel());
    }

    public int insertNewDataPropertyStatement(DataPropertyStatement dataPropertyStmt, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,dataPropertyStmt.getIndividualURI()));
		DataProperty dp = getWebappDaoFactory().getDataPropertyDao().getDataPropertyByURI(dataPropertyStmt.getDatapropURI());
		if ( (dataPropertyStmt.getDatatypeURI() == null) && (dp != null) && (dp.getRangeDatatypeURI() != null) ) {
			dataPropertyStmt.setDatatypeURI(dp.getRangeDatatypeURI());
		}
        Property prop = ontModel.getProperty(dataPropertyStmt.getDatapropURI());
        try {
            Resource res = ontModel.getResource(dataPropertyStmt.getIndividualURI());
            Literal literal = jenaLiteralFromDataPropertyStatement(dataPropertyStmt,ontModel);
            if (res != null && prop != null && literal != null && dataPropertyStmt.getData().length()>0) {
                res.addProperty(prop, literal);
            }
        } catch(Exception ex){
        	log.error("Error occurred in adding a data property for " + dataPropertyStmt.toString());
        }finally {
        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,dataPropertyStmt.getIndividualURI()));
            ontModel.leaveCriticalSection();
        }
        return 0;
    }

    protected DataPropertyStatement fillDataPropertyStatementWithJenaLiteral(DataPropertyStatement dataPropertyStatement, Literal l) {
    	dataPropertyStatement.setData(l.getLexicalForm());
        dataPropertyStatement.setDatatypeURI(l.getDatatypeURI());
        dataPropertyStatement.setLanguage(l.getLanguage());
        return dataPropertyStatement;
    }

    protected Literal jenaLiteralFromDataPropertyStatement(DataPropertyStatement dataPropertyStatement, OntModel ontModel) {
    	Literal l = null;
        if ((dataPropertyStatement.getLanguage()) != null && (dataPropertyStatement.getLanguage().length()>0)) {
        	l = ontModel.createLiteral(dataPropertyStatement.getData(),dataPropertyStatement.getLanguage());
        } else if ((dataPropertyStatement.getDatatypeURI() != null) && (dataPropertyStatement.getDatatypeURI().length()>0)) {
        	l = ontModel.createTypedLiteral(dataPropertyStatement.getData(),TypeMapper.getInstance().getSafeTypeByName(dataPropertyStatement.getDatatypeURI()));
        } else {
        	l = ontModel.createLiteral(dataPropertyStatement.getData());
        }
        return l;
    }

    /*
     * SPARQL-based methods for getting the individual's values for a single data property.
     */

    protected static final String DATA_PROPERTY_VALUE_QUERY_STRING =
        "SELECT ?value WHERE { \n" +
        "    ?subject ?property ?value . \n" +
        // ignore statements with uri values
        " FILTER ( isLiteral(?value) ) " +
        "} ORDER BY ?value";

    protected static Query dataPropertyValueQuery;
    static {
        try {
            dataPropertyValueQuery = QueryFactory.create(DATA_PROPERTY_VALUE_QUERY_STRING);
        } catch(Throwable th) {
            log.error("could not create SPARQL query for DATA_PROPERTY_VALUE_QUERY_STRING " + th.getMessage());
            log.error(DATA_PROPERTY_VALUE_QUERY_STRING);
        }
    }

    @Override
    public List<Literal> getDataPropertyValuesForIndividualByProperty(Individual subject, DataProperty property) {
        return getDataPropertyValuesForIndividualByProperty(subject.getURI(), property.getURI());
    }

    @Override
    public List<Literal> getDataPropertyValuesForIndividualByProperty(String subjectUri, String propertyUri) {
        log.debug("Data property value query string:\n" + DATA_PROPERTY_VALUE_QUERY_STRING);
        log.debug("Data property value:\n" + dataPropertyValueQuery);

        // Due to a Jena bug, prebinding on ?subject combined with the isLiteral()
        // filter causes the query to fail. Insert the subjectUri manually instead.
        // QuerySolutionMap initialBindings = new QuerySolutionMap();
        // initialBindings.add("subject", ResourceFactory.createResource(subjectUri));
        // initialBindings.add("property", ResourceFactory.createResource(propertyUri));
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("subject", subjectUri);
        bindings.put("property", propertyUri);
        String queryString = QueryUtils.subUrisForQueryVars(DATA_PROPERTY_VALUE_QUERY_STRING, bindings);

        // Run the SPARQL query to get the properties
        List<Literal> values = new ArrayList<Literal>();
        DatasetWrapper w = dwf.getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        QueryExecution qexec = null;
        try {
            qexec = QueryExecutionFactory.create(
                    queryString, dataset);
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution sol = results.next();
                Literal value = sol.getLiteral("value");
                values.add(value);
            }
            return values;

        } catch (Exception e) {
            log.error("Error getting data property values for individual " + subjectUri + " and property " + propertyUri);
            return Collections.emptyList();

        } finally {
            dataset.getLock().leaveCriticalSection();
            w.close();
            if (qexec != null) {
                qexec.close();
            }
        }

    }

    @Override
    public List<Literal> getDataPropertyValuesForIndividualByProperty(
            Individual subject,
            DataProperty property,
            String queryString, Set<String> constructQueryStrings ) {
        return getDataPropertyValuesForIndividualByProperty(subject.getURI(), property.getURI(), queryString, constructQueryStrings );
    }

    @Override
    public List<Literal> getDataPropertyValuesForIndividualByProperty(
            String subjectUri,
            String propertyUri,
            String queryString, Set<String> constructQueryStrings ) {

        Model constructedModel = constructModelForSelectQueries(
                subjectUri, propertyUri, constructQueryStrings);

        log.debug("Query string for data property " + propertyUri + ": " + queryString);

        Query query = null;
        try {
            query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        } catch(Throwable th){
            log.error("Could not create SPARQL query for query string. " + th.getMessage());
            log.error(queryString);
            return Collections.emptyList();
        }

        QuerySolutionMap initialBindings = new QuerySolutionMap();
        initialBindings.add("subject", ResourceFactory.createResource(subjectUri));
        initialBindings.add("property", ResourceFactory.createResource(propertyUri));

        // Run the SPARQL query to get the properties
        List<Literal> values = new ArrayList<Literal>();
        DatasetWrapper w = dwf.getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        QueryExecution qexec = null;
        try {

            qexec = (constructedModel == null)
                    ? QueryExecutionFactory.create(
                            query, dataset, initialBindings)
                    : QueryExecutionFactory.create(
                            query, constructedModel, initialBindings);

            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution sol = results.next();
                Literal value = sol.getLiteral("value");
                if(value != null) {
                    values.add(value);
                }
            }
            log.debug("values = " + values);
            return values;

        } catch (Exception e) {
            log.error("Error getting data property values for subject " + subjectUri + " and property " + propertyUri);
            return Collections.emptyList();
        } finally {
            dataset.getLock().leaveCriticalSection();
            w.close();
            if (qexec != null) {
                qexec.close();
            }
        }

    }
    private Model constructModelForSelectQueries(String subjectUri,
                                                 String propertyUri,
                                                 Set<String> constructQueries) {

        if (constructQueries == null || constructQueries.isEmpty() ) {
            return null;
        }

        Model constructedModel = ModelFactory.createDefaultModel();

        for (String queryString : constructQueries) {

            log.debug("CONSTRUCT query string for object property " +
                    propertyUri + ": " + queryString);

            Query query = null;
            try {
                query = QueryFactory.create(queryString, Syntax.syntaxARQ);
            } catch(Throwable th){
                log.error("Could not create CONSTRUCT SPARQL query for query " +
                          "string. " + th.getMessage());
                log.error(queryString);
                return constructedModel;
            }

            QuerySolutionMap initialBindings = new QuerySolutionMap();
            initialBindings.add(
                    "subject", ResourceFactory.createResource(subjectUri));
            initialBindings.add(
                    "property", ResourceFactory.createResource(propertyUri));

            DatasetWrapper w = dwf.getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            QueryExecution qe = null;
            try {
                qe = QueryExecutionFactory.create(
                        query, dataset, initialBindings);
                qe.execConstruct(constructedModel);
            } catch (Exception e) {
                log.error("Error getting constructed model for subject " + subjectUri + " and property " + propertyUri);
            } finally {
                if (qe != null) {
                    qe.close();
                }
                dataset.getLock().leaveCriticalSection();
                w.close();
            }
        }

        return constructedModel;

    }
}
