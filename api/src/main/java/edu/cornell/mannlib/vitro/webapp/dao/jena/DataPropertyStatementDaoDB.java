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

/**
 * An extension of {@link DataPropertyStatementDaoJena} for databases, such as TDB.
 */
public class DataPropertyStatementDaoDB extends DataPropertyStatementDaoJena implements DataPropertyStatementDao {

    /**
     * Initialize the data property statement DAO.
     *
     * @param dw The data wrapper.
     * @param wadf The web application DAO factory.
     */
    public DataPropertyStatementDaoDB(DatasetWrapper dw, WebappDaoFactoryDB wadf) {
        super (dw, wadf);
    }

    /**
     * Fill existing data property statements for an individual.
     *
     * @param entity The individual.
     *
     * @return A filled out individual.
     */
    public Individual fillExistingDataPropertyStatementsForIndividual(Individual entity) {
        if (entity.getURI() == null) {
            return entity;
        }

        String query =
            "CONSTRUCT { \n" +
            "   <" + entity.getURI() + "> ?p ?o . \n" +
            "} WHERE { \n" +
            "   <" + entity.getURI() + "> ?p ?o . \n" +
            "   FILTER(isLiteral(?o)) \n" +
            "}" ;
        Model results = null;
        Dataset dataset = getDataWrapper().getDataset();
        QueryExecution qexec = null;

        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            qexec = QueryExecutionFactory.create(QueryFactory.create(query), dataset);
            results = qexec.execConstruct();
        } finally {
            if (qexec != null) qexec.close();
            dataset.getLock().leaveCriticalSection();
            getDataWrapper().close();
        }

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, results);
        ontModel.enterCriticalSection(Lock.READ);
        try {
            Resource ind = ontModel.getResource(entity.getURI());
            List<DataPropertyStatement> edList = new ArrayList<>();
            StmtIterator stmtIt = ind.listProperties();

            while (stmtIt.hasNext()) {
                Statement st = stmtIt.next();
                boolean addToList = st.getObject().isLiteral() &&
                      (
                          (
                              RDF.value.equals(st.getPredicate())
                              || VitroVocabulary.value.equals(st.getPredicate().getURI()))
                              || MONIKER.equals(st.getPredicate())
                              || !(NONUSER_NAMESPACES.contains(st.getPredicate().getNameSpace())
                          )
                      );

                if (addToList) {
                    DataPropertyStatement ed = new DataPropertyStatementImpl();
                    Literal lit = (Literal)st.getObject();
                    fillDataPropertyStatementWithJenaLiteral(ed,lit);
                    ed.setDatapropURI(st.getPredicate().getURI());
                    ed.setIndividualURI(ind.getURI());
                    ed.setIndividual(entity);
                    edList.add(ed);
                }
            }

            entity.setDataPropertyStatements(edList);
            return entity;
        } finally {
            ontModel.leaveCriticalSection();
        }
    }
}
