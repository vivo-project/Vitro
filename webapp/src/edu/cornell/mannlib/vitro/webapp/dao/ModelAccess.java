/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.EnumMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
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

	public enum ModelID {
		APPLICATION_METADATA,

		USER_ACCOUNTS,

		DISPLAY, DISPLAY_DISPLAY, DISPLAY_TBOX,

		BASE_ABOX, BASE_TBOX, BASE_FULL,

		INFERRED_ABOX, INFERRED_TBOX, INFERRED_FULL,

		UNION_ABOX, UNION_TBOX, UNION_FULL
	}

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
	private final Map<ModelID, OntModel> modelMap = new EnumMap<>(ModelID.class);
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
		return getOntModel(ModelID.APPLICATION_METADATA);
	}

	public OntModel getUserAccountsModel() {
		return getOntModel(ModelID.USER_ACCOUNTS);
	}

	public OntModel getDisplayModel() {
		return getOntModel(ModelID.DISPLAY);
	}

	public OntModel getJenaOntModel() {
		return getOntModel(ModelID.UNION_FULL);
	}

	public OntModel getBaseOntModel() {
		return getOntModel(ModelID.BASE_FULL);
	}

	public OntModel getInferenceOntModel() {
		return getOntModel(ModelID.INFERRED_FULL);
	}

	public void setOntModel(ModelID id, OntModel ontModel) {
		if (ontModel == null) {
			modelMap.remove(id);
		} else {
			modelMap.put(id, ontModel);
		}
	}

	public OntModel getOntModel(ModelID id) {
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
		return new FacadeOntModelSelector(this, ModelID.BASE_ABOX,
				ModelID.BASE_TBOX, ModelID.BASE_FULL);
	}

	public OntModelSelector getInferenceOntModelSelector() {
		return new FacadeOntModelSelector(this, ModelID.INFERRED_ABOX,
				ModelID.INFERRED_TBOX, ModelID.INFERRED_FULL);
	}

	public OntModelSelector getUnionOntModelSelector() {
		return new FacadeOntModelSelector(this, ModelID.UNION_ABOX,
				ModelID.UNION_TBOX, ModelID.UNION_FULL);
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
			setOntModel(ModelID.USER_ACCOUNTS, modelMaker,
					ModelNames.USER_ACCOUNTS);
			setOntModel(ModelID.DISPLAY, modelMaker, ModelNames.DISPLAY);
			setOntModel(ModelID.DISPLAY_DISPLAY, modelMaker,
					ModelNames.DISPLAY_DISPLAY);
			setOntModel(ModelID.DISPLAY_TBOX, modelMaker,
					ModelNames.DISPLAY_TBOX);
		} else {
			setOntModel(ModelID.APPLICATION_METADATA, modelMaker,
					ModelNames.APPLICATION_METADATA);
			setOntModel(ModelID.BASE_TBOX, modelMaker,
					ModelNames.TBOX_ASSERTIONS);
			setOntModel(ModelID.INFERRED_TBOX, modelMaker,
					ModelNames.TBOX_INFERENCES);
			setOntModel(ModelID.UNION_TBOX, modelMaker, ModelNames.TBOX_UNION);
			setOntModel(ModelID.BASE_ABOX, modelMaker,
					ModelNames.ABOX_ASSERTIONS);
			setOntModel(ModelID.INFERRED_ABOX, modelMaker,
					ModelNames.ABOX_INFERENCES);
			setOntModel(ModelID.UNION_ABOX, modelMaker, ModelNames.ABOX_UNION);
			setOntModel(ModelID.BASE_FULL, modelMaker,
					ModelNames.FULL_ASSERTIONS);
			setOntModel(ModelID.INFERRED_FULL, modelMaker,
					ModelNames.FULL_INFERENCES);
			setOntModel(ModelID.UNION_FULL, modelMaker, ModelNames.FULL_UNION);
			
			/*
			 * KLUGE
			 * 
			 * For some reason, the union of two OntModels (like this) works
			 * fine as the UNION_TBOX, but an OntModel wrapped around the union
			 * of two Models (from ModelMakers) does not work.
			 * 
			 * See also the Kluge in RequestModelsPrep.
			 */
			setOntModel(ModelID.UNION_TBOX, VitroModelFactory.createUnion(
					getOntModel(ModelID.BASE_TBOX),
					getOntModel(ModelID.INFERRED_TBOX)));
		}
	}

	private void setOntModel(ModelID id, ModelMaker mm, String uri) {
		setOntModel(id, VitroModelFactory.createOntologyModel(mm.getModel(uri)));
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

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * This OntModelSelector doesn't actually hold any OntModels. Instead, it
	 * links back to the ModelAccess that it was created from. So, if you change
	 * a model on the ModelAccess, it will change on the OntModelSelector also.
	 * Even if the OntModelSelector was created first.
	 */
	private static class FacadeOntModelSelector implements OntModelSelector {
		private final ModelAccess parent;
		private final ModelID aboxID;
		private final ModelID tboxID;
		private final ModelID fullID;

		public FacadeOntModelSelector(ModelAccess parent, ModelID aboxID,
				ModelID tboxID, ModelID fullID) {
			this.parent = parent;
			this.aboxID = aboxID;
			this.tboxID = tboxID;
			this.fullID = fullID;
		}

		@Override
		public OntModel getABoxModel() {
			return parent.getOntModel(aboxID);
		}

		@Override
		public OntModel getTBoxModel() {
			return parent.getOntModel(tboxID);
		}

		@Override
		public OntModel getFullModel() {
			return parent.getOntModel(fullID);
		}

		@Override
		public OntModel getApplicationMetadataModel() {
			return parent.getOntModel(ModelID.APPLICATION_METADATA);
		}

		@Override
		public OntModel getUserAccountsModel() {
			return parent.getOntModel(ModelID.USER_ACCOUNTS);
		}

		@Override
		public OntModel getDisplayModel() {
			return parent.getOntModel(ModelID.DISPLAY);
		}
	}

}
