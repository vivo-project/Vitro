/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;

/**
 * Hierarchical storage for models. TODO
 * 
 * Could this be extended? Could it be used to replace or implement these
 * methods?
 * 
 * <pre>
 * VitroRequest.getAssertionsWebappDaoFactory()
 * VitroRequest.getFullWebappDaoFactory()
 * VitroRequest.getRDFService()
 * VitroRequest.getUnfilteredRDFService()
 * VitroRequest.getWebappDaoFactory()
 * VitroRequest.getWriteModel()
 * 
 * OntModelSelector.getABoxModel
 * OntModelSelector.getFullModel()
 * OntModelSelector.getTBoxModel()
 * VitroModelSource.getModel(URL)
 * VitroModelSource.getModel(URL, loadIfAbsent)
 * VitroModelSource.openModel(name)
 * VitroModelSource.openModelIfPresent(string)
 * ServletContext.getAttribute("pelletOntModel")
 * JenaDataSourceSetupBase.getStartupDataset()
 * HttpSession.getAttribute("jenaAuditModel")
 * </pre>
 */

public class ModelAccess {
	private static final Log log = LogFactory.getLog(ModelAccess.class);

	/** These attributes should only be accessed through this class. */
	private static final String ATTRIBUTE_NAME = ModelAccess.class.getName();

	public enum FactoryID {
		BASE, UNION, UNFILTERED_BASE, UNFILTERED_UNION
	}

	public enum ModelMakerID {
		CONTENT, CONFIGURATION
	}

	private enum Scope {
		CONTEXT, SESSION, REQUEST
	}

	// ----------------------------------------------------------------------
	// Factory methods
	// ----------------------------------------------------------------------

	public static ModelAccess on(HttpServletRequest req) {
		Object o = req.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof ModelAccess) {
			return (ModelAccess) o;
		} else {
			ModelAccess parent = on(req.getSession());
			ModelAccess ma = new ModelAccess(Scope.REQUEST, parent);
			req.setAttribute(ATTRIBUTE_NAME, ma);
			return ma;
		}
	}

	public static ModelAccess on(HttpSession session) {
		Object o = session.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof ModelAccess) {
			return (ModelAccess) o;
		} else {
			ModelAccess parent = on(session.getServletContext());
			ModelAccess ma = new ModelAccess(Scope.SESSION, parent);
			session.setAttribute(ATTRIBUTE_NAME, ma);
			return ma;
		}
	}

	public static ModelAccess on(ServletContext ctx) {
		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof ModelAccess) {
			return (ModelAccess) o;
		} else {
			ModelAccess ma = new ModelAccess(Scope.CONTEXT, null);
			ctx.setAttribute(ATTRIBUTE_NAME, ma);
			return ma;
		}
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final Scope scope;
	private final ModelAccess parent;
	private final Map<String, OntModel> modelMap = new HashMap<>();
	private final Map<FactoryID, WebappDaoFactory> factoryMap = new EnumMap<>(
			FactoryID.class);
	private final Map<ModelMakerID, ModelMaker> modelMakerMap = new EnumMap<>(
			ModelMakerID.class);

	public ModelAccess(Scope scope, ModelAccess parent) {
		this.scope = scope;
		this.parent = parent;
	}

	// ----------------------------------------------------------------------
	// Accessing the models
	// ----------------------------------------------------------------------

	public OntModel getApplicationMetadataModel() {
		return getOntModel(ModelNames.APPLICATION_METADATA);
	}

	public OntModel getUserAccountsModel() {
		return getOntModel(ModelNames.USER_ACCOUNTS);
	}

	public OntModel getDisplayModel() {
		return getOntModel(ModelNames.DISPLAY);
	}

	public OntModel getJenaOntModel() {
		return getOntModel(ModelNames.FULL_UNION);
	}

	public OntModel getBaseOntModel() {
		return getOntModel(ModelNames.FULL_ASSERTIONS);
	}

	public OntModel getInferenceOntModel() {
		return getOntModel(ModelNames.FULL_INFERENCES);
	}

	public void setOntModel(String id, OntModel ontModel) {
		if (ontModel == null) {
			modelMap.remove(id);
		} else {
			modelMap.put(id, ontModel);
		}
	}

	public OntModel getOntModel(String id) {
		if (modelMap.containsKey(id)) {
			log.debug("Using " + id + " model from " + scope);
			return modelMap.get(id);
		} else if (parent != null) {
			return parent.getOntModel(id);
		} else {
			log.warn("No model found for " + id);
			return null;
		}
	}

	// ----------------------------------------------------------------------
	// Accessing the Webapp DAO factories.
	// ----------------------------------------------------------------------

	public void setWebappDaoFactory(WebappDaoFactory wadf) {
		setWebappDaoFactory(FactoryID.UNION, wadf);
	}

	public WebappDaoFactory getWebappDaoFactory() {
		return getWebappDaoFactory(FactoryID.UNION);
	}

	public void setBaseWebappDaoFactory(WebappDaoFactory wadf) {
		setWebappDaoFactory(FactoryID.BASE, wadf);
	}

	public WebappDaoFactory getBaseWebappDaoFactory() {
		return getWebappDaoFactory(FactoryID.BASE);
	}

	public void setWebappDaoFactory(FactoryID id, WebappDaoFactory wadf) {
		if (wadf == null) {
			factoryMap.remove(id);
		} else {
			factoryMap.put(id, wadf);
		}
	}

	public void removeWebappDaoFactory(FactoryID id) {
		setWebappDaoFactory(id, null);
	}

	public WebappDaoFactory getWebappDaoFactory(FactoryID id) {
		if (factoryMap.containsKey(id)) {
			log.debug("Using " + id + " DAO factory from " + scope);
			return factoryMap.get(id);
		} else if (parent != null) {
			return parent.getWebappDaoFactory(id);
		} else {
			log.warn("No DAO factory found for " + id);
			return null;
		}
	}

	// ----------------------------------------------------------------------
	// Accessing the OntModelSelectors
	// ----------------------------------------------------------------------

	public OntModelSelector getOntModelSelector() {
		return getUnionOntModelSelector();
	}

	public OntModelSelector getBaseOntModelSelector() {
		return createOntModelSelector(ModelNames.ABOX_ASSERTIONS,
				ModelNames.TBOX_ASSERTIONS, ModelNames.FULL_ASSERTIONS);
	}

	public OntModelSelector getInferenceOntModelSelector() {
		return createOntModelSelector(ModelNames.ABOX_INFERENCES,
				ModelNames.TBOX_INFERENCES, ModelNames.FULL_INFERENCES);
	}

	public OntModelSelector getUnionOntModelSelector() {
		return createOntModelSelector(ModelNames.ABOX_UNION,
				ModelNames.TBOX_UNION, ModelNames.FULL_UNION);
	}

	// ----------------------------------------------------------------------
	// Accessing the ModelMakers
	// ----------------------------------------------------------------------

	public ModelMaker getModelMaker(ModelMakerID id) {
		if (modelMakerMap.containsKey(id)) {
			log.debug("Using " + id + " modelMaker from " + scope);
			return modelMakerMap.get(id);
		} else if (parent != null) {
			return parent.getModelMaker(id);
		} else {
			log.warn("No modelMaker found for " + id);
			return null;
		}
	}

	public void setModelMaker(ModelMakerID id, ModelMaker modelMaker) {
		modelMakerMap.put(id, modelMaker);
		if (id == ModelMakerID.CONFIGURATION) {
			setOntModel(modelMaker, ModelNames.USER_ACCOUNTS);
			setOntModel(modelMaker, ModelNames.DISPLAY);
			setOntModel(modelMaker, ModelNames.DISPLAY_DISPLAY);
			setOntModel(modelMaker, ModelNames.DISPLAY_TBOX);
		} else {
			setOntModel(modelMaker, ModelNames.APPLICATION_METADATA);
			setOntModel(modelMaker, ModelNames.TBOX_ASSERTIONS);
			setOntModel(modelMaker, ModelNames.TBOX_INFERENCES);
			setOntModel(modelMaker, ModelNames.TBOX_UNION);
			setOntModel(modelMaker, ModelNames.ABOX_ASSERTIONS);
			setOntModel(modelMaker, ModelNames.ABOX_INFERENCES);
			setOntModel(modelMaker, ModelNames.ABOX_UNION);
			setOntModel(modelMaker, ModelNames.FULL_ASSERTIONS);
			setOntModel(modelMaker, ModelNames.FULL_INFERENCES);
			setOntModel(modelMaker, ModelNames.FULL_UNION);

			/*
			 * KLUGE
			 * 
			 * For some reason, the union of two OntModels (like this) works
			 * fine as the TBOX_UNION, but an OntModel wrapped around the union
			 * of two Models (from ModelMakers) does not work.
			 * 
			 * See also the Kluge in RequestModelsPrep.
			 */
			setOntModel(ModelNames.TBOX_UNION, VitroModelFactory.createUnion(
					getOntModel(ModelNames.TBOX_ASSERTIONS),
					getOntModel(ModelNames.TBOX_INFERENCES)));
		}
	}

	private void setOntModel(ModelMaker mm, String name) {
		setOntModel(name,
				VitroModelFactory.createOntologyModel(mm.getModel(name)));
	}

	private OntModelSelector createOntModelSelector(String aboxName,
			String tboxName, String fullName) {
		OntModelSelectorImpl oms = new OntModelSelectorImpl();

		oms.setApplicationMetadataModel(getOntModel(ModelNames.APPLICATION_METADATA));
		oms.setDisplayModel(getOntModel(ModelNames.DISPLAY));
		oms.setUserAccountsModel(getOntModel(ModelNames.USER_ACCOUNTS));

		oms.setABoxModel(getOntModel(aboxName));
		oms.setTBoxModel(getOntModel(tboxName));
		oms.setFullModel(getOntModel(fullName));

		return oms;
	}

	// ----------------------------------------------------------------------
	// Close all locally stored models, WADFs, etc.
	// ----------------------------------------------------------------------

	public void close() {
		if (this.scope == Scope.REQUEST) {
			for (WebappDaoFactory wadf : factoryMap.values()) {
				wadf.close();
			}
		}
	}

}
