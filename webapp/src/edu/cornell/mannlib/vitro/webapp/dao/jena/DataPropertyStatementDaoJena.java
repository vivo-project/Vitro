/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.edit.ReorderController;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
            com.hp.hpl.jena.ontology.Individual ind = ontModel.getIndividual(
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
                    Statement st = (Statement)stmtIt.next();
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
	        OntResource ontRes = (OntResource) getOntModel().createResource(
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
}
