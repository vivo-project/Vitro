/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDF;

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
                    Statement st = stmtIt.next();
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
