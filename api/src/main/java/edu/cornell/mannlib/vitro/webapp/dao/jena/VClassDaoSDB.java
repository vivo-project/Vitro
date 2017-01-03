/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;


import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntClass;
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
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;

public class VClassDaoSDB extends VClassDaoJena {

	private DatasetWrapperFactory dwf;
	private SDBDatasetMode datasetMode;
	
    public VClassDaoSDB(DatasetWrapperFactory datasetWrapperFactory, 
                        SDBDatasetMode datasetMode,
                        WebappDaoFactoryJena wadf, boolean isUnderlyingStoreReasoned) {
        super(wadf, isUnderlyingStoreReasoned);
        this.dwf = datasetWrapperFactory;
        this.datasetMode = datasetMode;
    }
    
    protected DatasetWrapper getDatasetWrapper() {
    	return dwf.getDatasetWrapper();
    }
    
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
                                		                       WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +
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
                                    } else if (includeUninstantiatedClasses == false) {
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
                                log.error(cce, cce);
                            }
                        }
                    } finally {
                        annotIt.close();
                    }
                }
            }
            java.util.Collections.sort(group.getVitroClassList());
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }
        
//    protected void addIndividualCountToGroups( List<VClassGroup> cgList ){
//        for( VClassGroup cg : cgList){           
//            cg.setIndividualCount(getClassGroupInstanceCount(cg));
//        }        
//    }
    
    @Override
    int getClassGroupInstanceCount(VClassGroup vcg){
        int count = 0;               
        try {
            String queryText =              
                "SELECT COUNT( DISTINCT ?instance ) WHERE { \n" +
                "      ?class <"+VitroVocabulary.IN_CLASSGROUP+"> <"+vcg.getURI() +"> .\n" +                
                "      ?instance a ?class .  \n" +
                "} \n" ;
            
            Query countQuery = QueryFactory.create(queryText, Syntax.syntaxARQ);
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
        }catch(Exception ex){
            log.error("error in getClassGroupInstanceCount()", ex);
        }    
        
        return count;
    }

    
}
