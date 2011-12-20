/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Wraps a model maker and returns Models from the servlet context when 
 * certain model URIs are requested
 * @author bjl23
 *
 */
public class VitroJenaSpecialModelMaker implements ModelMaker {

	private static final Log log = LogFactory.getLog(VitroJenaSpecialModelMaker.class.getName());
	
	private ModelMaker innerModelMaker;
	private HttpServletRequest request;
	
	public VitroJenaSpecialModelMaker(ModelMaker mm,  HttpServletRequest request) {
		this.innerModelMaker = mm;
		this.request = request;
	}
	
	public ModelMaker getInnerModelMaker() {
		return this.innerModelMaker;
	}
	
	public void close() {
		innerModelMaker.close();
	}

	public Model createModel(String arg0) {
		return innerModelMaker.createModel(arg0);
	}

	public Model createModel(String arg0, boolean arg1) {
		Model specialModel = getSpecialModel(arg0);
		return (specialModel != null) ? specialModel : innerModelMaker.createModel(arg0,arg1);
	}

	public GraphMaker getGraphMaker() {
		return innerModelMaker.getGraphMaker();
	}

	public boolean hasModel(String arg0) {
		return ( (getSpecialModel(arg0) != null) || innerModelMaker.hasModel(arg0) );
	}

	/**
	 * Won't list the special models
	 */
	public ExtendedIterator listModels() {
		return innerModelMaker.listModels(); 
	}

	public Model openModel(String arg0, boolean arg1) {
		Model specialModel = getSpecialModel(arg0);
		return (specialModel != null) ? specialModel : innerModelMaker.openModel(arg0,arg1);
	}

	public void removeModel(String arg0) {
		innerModelMaker.removeModel(arg0);
	}

	public Model createModelOver(String arg0) {
		Model specialModel = getSpecialModel(arg0);
		return (specialModel != null) ? specialModel : createModelOver(arg0);
	}

	public Model createDefaultModel() {
		return innerModelMaker.createDefaultModel();
	}

	public Model createFreshModel() {
		return innerModelMaker.createFreshModel();
	}

	public Model openModel(String arg0) {
		Model specialModel = getSpecialModel(arg0);
		return (specialModel != null) ? specialModel : innerModelMaker.openModel(arg0);
	}

	public Model openModelIfPresent(String arg0) {
		Model specialModel = getSpecialModel(arg0);
		return (specialModel != null) ? specialModel : innerModelMaker.openModelIfPresent(arg0);
	}

	public Model getModel(String arg0) {
		Model specialModel = getSpecialModel(arg0);
		return (specialModel != null) ? specialModel : innerModelMaker.getModel(arg0);
	}

	public Model getModel(String arg0, ModelReader arg1) {
		Model specialModel = getSpecialModel(arg0);
		return (specialModel != null) ? specialModel : innerModelMaker.getModel(arg0,arg1);
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
