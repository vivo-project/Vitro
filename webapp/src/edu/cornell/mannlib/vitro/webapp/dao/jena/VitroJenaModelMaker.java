/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This is a bit of a nutty idea but we'll see if it works.  This can wrap an RDBModelMaker and return a memory model 
 * synced with the underlying RDB model.  Note, however, that a Jena RDBModelMaker won't auto-reconnect.  Maybe I can 
 * revisit the reconnecting IDBConnection issue or make a special RDBModelMaker that uses the reconnection system.
 *  
 * @author bjl23
 *
 */

public class VitroJenaModelMaker implements ModelMaker {
	
	private static final Log log = LogFactory.getLog(VitroJenaModelMaker.class);

	private ModelMaker innerModelMaker = null;
	private HashMap<String,Model> modelCache = null;
	private HttpServletRequest request = null;
	
	public VitroJenaModelMaker(ModelMaker mm) {
		this.innerModelMaker = mm;
		modelCache = new HashMap<String,Model>();
	}
	
	public VitroJenaModelMaker(ModelMaker mm,  HttpServletRequest request) {
		this.innerModelMaker = mm;
		if (mm instanceof VitroJenaModelMaker) { 
			log.debug("Using cache from inner model maker ");
			this.modelCache = ((VitroJenaModelMaker)mm).getCache();
		} else {
			log.debug("Creating new cache");
			this.modelCache = new HashMap<String,Model>();
		}
		this.request = request;
	}
	
	public ModelMaker getInnerModelMaker() {
		return this.innerModelMaker;
	}
	
	protected HashMap<String,Model> getCache() {
		return this.modelCache;
	}
	
	private Model copyModelIntoMem(Model underlyingModel) {
		Model memModel = ModelFactory.createDefaultModel();
		memModel.add(underlyingModel);
		memModel.register(new ModelSynchronizer(underlyingModel));
		return memModel;
	}
	
	public void close() {
		// TODO Auto-generated method stub
		// So, in theory, this should close database connections and drop references
		// to in-memory models and all that kind of stuff.
	}

	public Model createModel(String arg0) {
		Model specialModel = null;
		if ( (specialModel = getSpecialModel(arg0)) != null ) { return specialModel; }
		Model cachedModel = modelCache.get(arg0);
		if (cachedModel != null) {
			log.debug("Returning "+arg0+" ("+cachedModel.hashCode()+") from cache");
			return cachedModel;
		} else {
			 Model newModel = copyModelIntoMem(innerModelMaker.createModel(arg0));
			 modelCache.put(arg0,newModel);
			 log.debug("Returning "+arg0+" ("+newModel.hashCode()+") from cache");
			 return newModel;
		}
	}

	public Model createModel(String arg0, boolean arg1) {
		Model specialModel = null;
		if ( (specialModel = getSpecialModel(arg0)) != null ) { return specialModel; }
		Model cachedModel = modelCache.get(arg0);
		if (cachedModel != null) {
			return cachedModel;
		} else {
			 Model newModel = copyModelIntoMem(innerModelMaker.createModel(arg0,arg1));
			 modelCache.put(arg0,newModel);
			 return newModel;
		}
	}
	
	public GraphMaker getGraphMaker() {
		return innerModelMaker.getGraphMaker();
	}

	public boolean hasModel(String arg0) {
		return innerModelMaker.hasModel(arg0);
	}

	public ExtendedIterator listModels() {
		return innerModelMaker.listModels();
	}

	public Model openModel(String arg0, boolean arg1) {
		Model specialModel = null;
		if ( (specialModel = getSpecialModel(arg0)) != null ) { return specialModel; }
		Model cachedModel = modelCache.get(arg0);
		if (cachedModel != null) {
			return cachedModel;
		} else {
			 Model newModel = copyModelIntoMem(innerModelMaker.openModel(arg0,arg1));
			 modelCache.put(arg0,newModel);
			 return newModel;
		}
	}

	public void removeModel(String arg0) {
		Model m = modelCache.get(arg0);
		if (m != null) {
			m.close();
			modelCache.remove(arg0);
		}
		innerModelMaker.removeModel(arg0);
	}

	
	public Model addDescription(Model arg0, Resource arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Model createModelOver(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Model getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Model getDescription(Resource arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Model createDefaultModel() {
		return innerModelMaker.createDefaultModel();
	}

	
	public Model createFreshModel() {
		return innerModelMaker.createFreshModel();
	}

	@Deprecated
	public Model createModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public Model getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Model openModel(String arg0) {
		Model specialModel = null;
		if ( (specialModel = getSpecialModel(arg0)) != null ) { return specialModel; }
		Model cachedModel = modelCache.get(arg0);
		if (cachedModel != null) {
			return cachedModel;
		} else {
			 Model newModel = copyModelIntoMem(innerModelMaker.openModel(arg0));
			 modelCache.put(arg0,newModel);
			 return newModel;
		}
	}

	
	public Model openModelIfPresent(String arg0) {
		Model specialModel = null;
		if ( (specialModel = getSpecialModel(arg0)) != null ) { return specialModel; }
		Model cachedModel = modelCache.get(arg0);
		if (cachedModel != null) {
			return cachedModel;
		} else {
			 Model newModel = copyModelIntoMem(innerModelMaker.openModelIfPresent(arg0));
			 modelCache.put(arg0,newModel);
			 return newModel;
		}
	}

	
	public Model getModel(String arg0) {
		Model specialModel = null;
		if ( (specialModel = getSpecialModel(arg0)) != null ) { return specialModel; }
		Model cachedModel = modelCache.get(arg0);
		if (cachedModel != null) {
			return cachedModel;
		} else {
			 Model newModel = copyModelIntoMem(innerModelMaker.getModel(arg0));
			 modelCache.put(arg0,newModel);
			 return newModel;
		}
	}

	
	public Model getModel(String arg0, ModelReader arg1) {
		Model specialModel = null;
		if ( (specialModel = getSpecialModel(arg0)) != null ) { return specialModel; }
		Model cachedModel = modelCache.get(arg0);
		if (cachedModel != null) {
			return cachedModel;
		} else {
			 Model newModel = copyModelIntoMem(innerModelMaker.getModel(arg0));
			 modelCache.put(arg0,newModel);
			 return newModel;
		}
	}
	
	/**
	 * This will trap for strings like "vitro:jenaOntModel" and return the
	 * appropriate in-memory model used by the current webapp context.
	 * To use this functionality, the VitroJenaModelMaker must be constructed 
	 * with a VitroRequest parameter
	 */
	private Model getSpecialModel(String modelName) {
		if (request != null) {
			if ("vitro:jenaOntModel".equals(modelName)) {
				Object sessionOntModel = request.getSession().getAttribute("jenaOntModel");
				if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
					log.debug("Returning jenaOntModel from session");
					return (OntModel) sessionOntModel;
				} else {
					log.debug("Returning jenaOntModel from context");
					return (OntModel) request.getSession().getServletContext().getAttribute("jenaOntModel");
				}
			} else if ("vitro:baseOntModel".equals(modelName)) {
				Object sessionOntModel = request.getSession().getAttribute("baseOntModel");
				if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
					return (OntModel) sessionOntModel;
				} else {
					return (OntModel) request.getSession().getServletContext().getAttribute("baseOntModel");
				}
			} else if ("vitro:inferenceOntModel".equals(modelName)) {
				Object sessionOntModel = request.getSession().getAttribute("inferenceOntModel");
				if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
					return (OntModel) sessionOntModel;
				} else {
					return (OntModel) request.getSession().getServletContext().getAttribute("inferenceOntModel");
				}
			} else {
				return null;
			}
		}
		return null;
	}

}
