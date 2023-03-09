/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import java.util.Collections;

/**
 * An extension of {@link VClassDaoJena} for databases, such as TDB.
 */
public class VClassDaoDB extends VClassDaoJena {

    private DatasetWrapper dw;
    private DatasetMode mode;

    /**
     * Initialize VClass DAO.
     *
     * @param dw The data wrapper.
     * @param mode The data set mode.
     * @param wadf The web application DAO factory.
     * @param isUnderlyingStoreReasoned True if is underlying store reasoned and false otherwise.
     */
    public VClassDaoDB(DatasetWrapper dw, DatasetMode mode, WebappDaoFactoryJena wadf,
            boolean isUnderlyingStoreReasoned) {

        super(wadf, isUnderlyingStoreReasoned);
        this.dw = dw;
        this.mode = mode;
    }

    /**
     * Get the data set wrapper.
     *
     * @return The data set wrapper.
     */
    protected DatasetWrapper getDatasetWrapper() {
        return dw;
    }

    /**
     * Add VClasses to the given group.
     *
     * @param group the VClass group.
     * @param includeUninstantiatedClasses True to include and false otherwise.
     * @param getIndividualCount True to get the count and false otherwise.
     *
     * @deprecated
     */
    @Override
    @Deprecated
    public void addVClassesToGroup(VClassGroup group, boolean includeUninstantiatedClasses, boolean getIndividualCount) {

        if (getIndividualCount) {
            group.setIndividualCount( getClassGroupInstanceCount(group));
        }

        getOntModel().enterCriticalSection(Lock.READ);
        try {
            if ((group != null) && (group.getURI() != null)) {
                Resource groupRes = ResourceFactory.createResource(group.getURI());
                Property inClassGroup = ResourceFactory.createProperty(VitroVocabulary.IN_CLASSGROUP);
                if (inClassGroup != null) {
                    StmtIterator annotIt = getOntModel().listStatements((Resource)null,inClassGroup, groupRes);
                    try {
                        while (annotIt.hasNext()) {
                            try {
                                Statement annot = (Statement) annotIt.next();
                                Resource cls = annot.getSubject();
                                VClass vcw = getVClassByURI(cls.getURI());
                                if (vcw != null) {
                                    boolean classIsInstantiated = false;
                                    if (getIndividualCount) {
                                        int count = 0;
                                        String[] graphVars = { "?g" };
                                        String countQueryStr = "SELECT COUNT(DISTINCT ?s) WHERE \n" +
                                                               "{ GRAPH ?g { ?s a <" + cls.getURI() + "> } \n" +
                                                               WebappDaoFactoryDB.getFilterBlock(graphVars, mode) +
                                                               "} \n";
                                        Query countQuery = QueryFactory.create(countQueryStr, Syntax.syntaxARQ);
                                        DatasetWrapper w = getDatasetWrapper();
                                        Dataset dataset = w.getDataset();
                                        dataset.getLock().enterCriticalSection(Lock.READ);
                                        try {
                                            QueryExecution qe = QueryExecutionFactory.create(countQuery, dataset);
                                            ResultSet rs = qe.execSelect();
                                            count = Integer.parseInt(((Literal) rs.nextSolution().get(".1")).getLexicalForm());
                                        } finally {
                                            dataset.getLock().leaveCriticalSection();
                                            w.close();
                                        }
                                        vcw.setEntityCount(count);
                                        classIsInstantiated = (count > 0);
                                    } else if (!includeUninstantiatedClasses) {
                                        // Note: to support SDB models, may want to do this with
                                        // SPARQL and LIMIT 1 if SDB can take advantage of it
                                        Model aboxModel = getOntModelSelector().getABoxModel();
                                        aboxModel.enterCriticalSection(Lock.READ);
                                        try {
                                            StmtIterator countIt = aboxModel.listStatements(null,RDF.type,cls);
                                            try {
                                                if (countIt.hasNext()) {
                                                    classIsInstantiated = true;
                                                }
                                            } finally {
                                                countIt.close();
                                            }
                                        } finally {
                                            aboxModel.leaveCriticalSection();
                                        }
                                    }

                                    if (includeUninstantiatedClasses || classIsInstantiated) {
                                        group.add(vcw);
                                    }
                                }
                            } catch (ClassCastException cce) {
                                LOG.error(cce, cce);
                            }
                        }
                    } finally {
                        annotIt.close();
                    }
                }
            }
            Collections.sort(group.getVitroClassList());
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    /**
     * Get the VClass group instance count
     *
     * @param vcg the VClass group.
     *
     * @return The total number of VClass group instances.
     */
    @Override
    int getClassGroupInstanceCount(VClassGroup vcg){
        int count = 0;

        try {
            String queryText =
                "SELECT COUNT( DISTINCT ?instance ) WHERE { \n" +
                "      ?class <" + VitroVocabulary.IN_CLASSGROUP + "> <" + vcg.getURI() + "> .\n" +
                "      ?instance a ?class .  \n" +
                "} \n" ;

            Query countQuery = QueryFactory.create(queryText, Syntax.syntaxARQ);
            DatasetWrapper dw = getDatasetWrapper();
            Dataset dataset = dw.getDataset();

            dataset.getLock().enterCriticalSection(Lock.READ);
            try {
                QueryExecution qe = QueryExecutionFactory.create(countQuery, dataset);
                ResultSet rs = qe.execSelect();
                count = Integer.parseInt(((Literal) rs.nextSolution().get(".1")).getLexicalForm());
            } finally {
                dataset.getLock().leaveCriticalSection();
                dw.close();
            }
        } catch (Exception e) {
            LOG.error("error in getClassGroupInstanceCount()", e);
        }

        return count;
    }

}