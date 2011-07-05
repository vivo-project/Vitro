/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;

public class DataPropertyStatementDaoSDB extends DataPropertyStatementDaoJena
							implements DataPropertyStatementDao {

	private DatasetWrapperFactory dwf;
	private SDBDatasetMode datasetMode;
	
	public DataPropertyStatementDaoSDB(
	        DatasetWrapperFactory datasetWrapperFactory, 
	        SDBDatasetMode datasetMode, 
	        WebappDaoFactoryJena wadf) {
		super (datasetWrapperFactory, wadf);
		this.dwf = datasetWrapperFactory;
		this.datasetMode = datasetMode;
	}
	
	@Override
	public Individual fillExistingDataPropertyStatementsForIndividual( Individual entity/*, boolean allowAnyNameSpace*/)
    {
        if( entity.getURI() == null )
        {
            return entity;
        }
        else
        {
        	String query = 
	        	"CONSTRUCT { \n" +
			       "   <" + entity.getURI() + "> ?p ?o . \n" +
			       "} WHERE { \n" +
			       "   <" + entity.getURI() + "> ?p ?o . \n" +
			       "   FILTER(isLiteral(?o)) \n" +
	            "}" ;
        	Model results = null;
        	DatasetWrapper w = dwf.getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            QueryExecution qexec = null;
            try { 
                qexec = QueryExecutionFactory.create(QueryFactory.create(query), dataset);                
        	    results = qexec.execConstruct();
            } finally {
                if(qexec!=null) qexec.close();
                dataset.getLock().leaveCriticalSection();
                w.close();
            }
        	OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, results); 
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
                              || this.MONIKER.equals(st.getPredicate())
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
}
