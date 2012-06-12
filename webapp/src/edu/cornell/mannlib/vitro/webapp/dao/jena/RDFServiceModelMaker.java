/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

public class RDFServiceModelMaker implements ModelMaker {

    private final static Log log = LogFactory.getLog(VitroJenaSDBModelMaker.class);
    
    private RDFServiceFactory rdfServiceFactory;
        
    public static final String METADATA_MODEL_URI = 
            "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/metadata";
    
    public static final String HAS_NAMED_MODEL_URI = 
            "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/hasNamedModel";
    
    private Resource sdbResource; // a resource representing the SDB database 
    
    public RDFServiceModelMaker(RDFServiceFactory rdfServiceFactory) {
        this.rdfServiceFactory = rdfServiceFactory;
    }  
    
    protected RDFService getRDFService() {
        return rdfServiceFactory.getRDFService();
    }
    
    Model getMetadataModel() {
        return getModel(METADATA_MODEL_URI);
    }
        
    public void close() {
        // n.a.
    }

    public Model createModel(String modelName) {
        Model model = getModel(modelName);
        Model metadataModel = getMetadataModel();
        try {
            metadataModel.add(
                    sdbResource,metadataModel.getProperty(
                            HAS_NAMED_MODEL_URI), modelName);
        } finally {
            metadataModel.close();
        }
        return model;
    }

    public Model createModel(String arg0, boolean arg1) {
        // TODO Figure out if we can offer a "create if not found" option using SDB
        return createModel(arg0);
    }

    public GraphMaker getGraphMaker() {
        throw new UnsupportedOperationException(
                "GraphMaker not supported by " + this.getClass().getName());
    }

    public boolean hasModel(String arg0) {
        Model metadataModel = getMetadataModel();
        try {
            StmtIterator stmtIt = metadataModel.listStatements(
                    sdbResource, metadataModel.getProperty(
                            HAS_NAMED_MODEL_URI), arg0);
            try {
                return stmtIt.hasNext();
            } finally {
                if (stmtIt != null) {
                    stmtIt.close();
                }
            }
        } finally {
            metadataModel.close();
        }
    }

    public ExtendedIterator<String> listModels() {
        Model metadataModel = getMetadataModel();
        try {
            return listModelNames(metadataModel);
        } finally {    
            metadataModel.close();
        }
    }
    
    private ExtendedIterator<String> listModelNames(Model metadataModel) {
        
        Set<String> modelNameSet = new HashSet<String>();
        
        Iterator<RDFNode> metadataNameIt = metadataModel.listObjectsOfProperty(
                metadataModel.getProperty(HAS_NAMED_MODEL_URI));    
        while (metadataNameIt.hasNext()) {
            RDFNode rdfNode = metadataNameIt.next();
            if (rdfNode.isLiteral()) {
                modelNameSet.add(((Literal) rdfNode).getLexicalForm());
            }
        }
        
        RDFService service = getRDFService();
        try {
            modelNameSet.addAll(service.getGraphURIs());
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);    
        } finally {
            service.close();
        }
        
        List<String> modelNameList = new ArrayList<String>();
        modelNameList.addAll(modelNameSet);
        Collections.sort(modelNameList, Collator.getInstance());
                
        return WrappedIterator.create(modelNameList.iterator());
    }

    public Model openModel(String arg0, boolean arg1) {
        RDFService service = getRDFService();
        try {
            Dataset dataset = new RDFServiceDataset(service);
            return dataset.getNamedModel(arg0);
        } finally {
            service.close();
        }
    }

    public void removeModel(String arg0) {
        Model m = getModel(arg0);
        m.removeAll(null,null,null);
        Model metadataModel = getMetadataModel();
        try {
            metadataModel.remove(sdbResource, metadataModel.getProperty(
                    HAS_NAMED_MODEL_URI),metadataModel.createLiteral(arg0));
        } finally {
            metadataModel.close();
        }
    }

    public Model addDescription(Model arg0, Resource arg1) {
        throw new UnsupportedOperationException(
                "addDescription not supported by " + this.getClass().getName());
    }

    public Model createModelOver(String arg0) {
        throw new UnsupportedOperationException(
                "createModelOver not supported by " + this.getClass().getName());
    }

    public Model getDescription() {
        throw new UnsupportedOperationException(
                "createModelOver not supported by " + this.getClass().getName());
    }

    public Model getDescription(Resource arg0) {
        throw new UnsupportedOperationException(
                "getDescription not supported by "+this.getClass().getName());
    }

    public Model openModel() {
        RDFService service = getRDFService();
        try {
            Dataset dataset = new RDFServiceDataset(service);
            return dataset.getDefaultModel();
        } finally {
            service.close();
        }
    }

    public Model createDefaultModel() {
        return openModel();
    }

    public Model createFreshModel() {
        throw new UnsupportedOperationException(
                "createFreshModel not supported by " + this.getClass().getName());
    }

    /**
     * @deprecated
     */
    public Model createModel() {
        return openModel();
    }

    /**
     * @deprecated
     */
    public Model getModel() {
        return openModel();
    }

    public Model openModel(String arg0) {
        return openModel();
    }

    public Model openModelIfPresent(String arg0) {
        return (this.hasModel(arg0)) 
                ? openModel(arg0, false) 
                : null;
    }

    public Model getModel(String modelName) {
    	return openModel(modelName, true);
    }

    public Model getModel(String arg0, ModelReader arg1) {
        throw new UnsupportedOperationException(
                "getModel(String, ModelReader) not supported by " + 
                        this.getClass().getName());
    }

}
