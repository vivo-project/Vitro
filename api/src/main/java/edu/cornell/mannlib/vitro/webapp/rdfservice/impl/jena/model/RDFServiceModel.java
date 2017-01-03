/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.RDFServiceJena;

public class RDFServiceModel extends RDFServiceJena implements RDFService {

    private final static Log log = LogFactory.getLog(RDFServiceModel.class);
    
    private Model model;
    private Dataset dataset;
    private String modelName;
    
    /**
     * Create an RDFService to access a single default graph
     * @param model Jena Model
     */
    public RDFServiceModel(Model model) {
        this.model = model;
    }
    
    /**
     * Create an RDFService to access a Jena Dataset
     * @param dataset Jena Dataset
     */
    public RDFServiceModel(Dataset dataset) {
        this.dataset = dataset;
    }
    
    @Override
    protected DatasetWrapper getDatasetWrapper() {
      Dataset d = null;
      if (dataset != null)  {
          d = dataset; 
      } else {
          d = DatasetFactory.createMem();
          if (modelName == null) {
              d.setDefaultModel(this.model);
          } else {
              d.addNamedModel(this.modelName, model);
          }
      }
      DatasetWrapper datasetWrapper = new DatasetWrapper(d);
      return datasetWrapper;
    }
    
    @Override
    public boolean changeSetUpdate(ChangeSet changeSet)
            throws RDFServiceException {
             
        if (changeSet.getPreconditionQuery() != null 
                && !isPreconditionSatisfied(
                        changeSet.getPreconditionQuery(), 
                                changeSet.getPreconditionQueryType())) {
            return false;
        }
            
        //Dataset dataset = getDatasetWrapper().getDataset();
        		        
        try {                   
            for (Object o : changeSet.getPreChangeEvents()) {
                this.notifyListenersOfEvent(o);
            }

            Iterator<ModelChange> csIt = changeSet.getModelChanges().iterator();
            while (csIt.hasNext()) {
                ModelChange modelChange = csIt.next();
                if (!modelChange.getSerializedModel().markSupported()) {
                    byte[] bytes = IOUtils.toByteArray(modelChange.getSerializedModel());
                    modelChange.setSerializedModel(new ByteArrayInputStream(bytes));
                }
                modelChange.getSerializedModel().mark(Integer.MAX_VALUE);
                Model m = this.model;
                if (m == null && dataset != null) {
                    String changeGraphURI = modelChange.getGraphURI();
                    if (changeGraphURI != null) {
                        m = dataset.getNamedModel(changeGraphURI);
                    } else {
                        m = dataset.getDefaultModel();
                    }
                }                
                operateOnModel(m, modelChange, null);
            }
                        
            // notify listeners of triple changes
            notifyListenersOfChanges(changeSet);
//            csIt = changeSet.getModelChanges().iterator();
//            while (csIt.hasNext()) {
//                ModelChange modelChange = csIt.next();
//                modelChange.getSerializedModel().reset();
//                Model model = ModelFactory.createModelForGraph(
//                        new ListeningGraph(modelChange.getGraphURI(), this));
//                operateOnModel(model, modelChange, null);
//            }
            
            for (Object o : changeSet.getPostChangeEvents()) {
                this.notifyListenersOfEvent(o);
            }
            
        } catch (Exception e) {
            log.error(e, e);
            throw new RDFServiceException(e);
        } 
        
        return true;
    }    
}
