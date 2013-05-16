/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Hierarchical storage for models. TODO
 */
public class ModelAccess {
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

	// ----------------------------------------------------------------------
	// Factory methods
	// ----------------------------------------------------------------------

	public static ModelAccess on(HttpServletRequest req) {
		Object o = req.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof ModelAccess) {
			return (ModelAccess) o;
		} else {
			ModelAccess parent = on(req.getSession());
			ModelAccess ma = new ModelAccess(parent);
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
			ModelAccess ma = new ModelAccess(parent);
			session.setAttribute(ATTRIBUTE_NAME, ma);
			return ma;
		}
	}

	public static ModelAccess on(ServletContext ctx) {
		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof ModelAccess) {
			return (ModelAccess) o;
		} else {
			ModelAccess ma = new ModelAccess(null);
			ctx.setAttribute(ATTRIBUTE_NAME, ma);
			return ma;
		}
	}

	// ----------------------------------------------------------------------
	// Instance methods
	// ----------------------------------------------------------------------

	private final ModelAccess parent;
	private final Map<String, OntModel> modelMap = new HashMap<>();

	public ModelAccess(ModelAccess parent) {
		this.parent = parent;
	}

	public void setDisplayModel(OntModel m) {
		setOntModel(ModelID.DISPLAY, m);
	}

	public OntModel getDisplayModel() {
		return getOntModel(ModelID.DISPLAY);
	}

	// /** Is this the same as Assertions model? */
	// // public OntModel getBaseOntModel() {
	// // throw new RuntimeException(
	// // "ModelAccess.getBaseOntModel not implemented.");
	// // }
	//
	// public OntModel getAssertionsOntModel() {
	// throw new RuntimeException(
	// "ModelAccess.getAssertionsOntModel not implemented.");
	// }
	//
	// public void setUserAccountsModel(OntModel m) {
	// setOntModel(ModelID.USER_ACCOUNTS, m);
	// }
	//
	// public OntModel getUserAccountsModel() {
	// return getOntModel(ModelID.USER_ACCOUNTS);
	// }
	//
	// public OntModel getDisplayTboxOntModel() {
	// throw new RuntimeException(
	// "ModelAccess.getDisplayTboxOntModel not implemented.");
	// }
	//
	// public OntModel getDisplayModelDisplayOntModel() {
	// throw new RuntimeException(
	// "ModelAccess.getDisplayModelDisplayOntModel not implemented.");
	// }
	//
	// public OntModelSelector getOntModelSelector() {
	// return getUnionOntModelSelector();
	// }
	//
	// public OntModelSelector getBaseOntModelSelector() {
	// return new FacadeOntModelSelector(this, ModelID.BASE_ABOX,
	// ModelID.BASE_TBOX, ModelID.BASE_FULL);
	// }
	//
	// public OntModelSelector getInferenceOntModelSelector() {
	// return new FacadeOntModelSelector(this, ModelID.INFERRED_ABOX,
	// ModelID.INFERRED_TBOX, ModelID.INFERRED_FULL);
	// }
	//
	// public OntModelSelector getUnionOntModelSelector() {
	// return new FacadeOntModelSelector(this, ModelID.UNION_ABOX,
	// ModelID.UNION_TBOX, ModelID.UNION_FULL);
	// }
	//
	// private static class FacadeOntModelSelector implements OntModelSelector {
	// private final ModelAccess parent;
	// private final ModelID aboxID;
	// private final ModelID tboxID;
	// private final ModelID fullID;
	//
	// public FacadeOntModelSelector(ModelAccess parent, ModelID aboxID,
	// ModelID tboxID, ModelID fullID) {
	// this.parent = parent;
	// this.aboxID = aboxID;
	// this.tboxID = tboxID;
	// this.fullID = fullID;
	// }
	//
	// @Override
	// public OntModel getABoxModel() {
	// return parent.getOntModel(aboxID);
	// }
	//
	// @Override
	// public OntModel getTBoxModel() {
	// return parent.getOntModel(tboxID);
	// }
	//
	// @Override
	// public OntModel getTBoxModel(String ontologyURI) {
	// return parent.getOntModel(tboxID);
	// }
	//
	// @Override
	// public OntModel getFullModel() {
	// return parent.getOntModel(fullID);
	// }
	//
	// @Override
	// public OntModel getApplicationMetadataModel() {
	// return parent.getOntModel(ModelID.APPLICATION_METADATA);
	// }
	//
	// @Override
	// public OntModel getUserAccountsModel() {
	// return parent.getOntModel(ModelID.USER_ACCOUNTS);
	// }
	//
	// @Override
	// public OntModel getDisplayModel() {
	// return parent.getOntModel(ModelID.DISPLAY);
	// }
	// }

	/**
	 * <pre>
	 * From ModelContext
	 * 
	 * 	public static OntModelSelector getOntModelSelector(ServletContext ctx) {
	 * 		return (OntModelSelector) ctx.getAttribute(ONT_MODEL_SELECTOR);
	 * 	}
	 * 	
	 * 	public static void setOntModelSelector(OntModelSelector oms, ServletContext ctx) {
	 * 		ctx.setAttribute(ONT_MODEL_SELECTOR, oms); 
	 * 	}
	 * 	
	 * 	public static OntModelSelector getUnionOntModelSelector(ServletContext ctx) {
	 * 		return (OntModelSelector) ctx.getAttribute(UNION_ONT_MODEL_SELECTOR);
	 * 	}
	 * 	
	 * 	public static void setUnionOntModelSelector(OntModelSelector oms, ServletContext ctx) {
	 * 		ctx.setAttribute(UNION_ONT_MODEL_SELECTOR, oms); 
	 * 	}
	 *  	
	 * 	public static OntModelSelector getBaseOntModelSelector(ServletContext ctx) {
	 * 		return (OntModelSelector) ctx.getAttribute(BASE_ONT_MODEL_SELECTOR);
	 * 	}
	 * 	
	 * 	public static void setBaseOntModelSelector(OntModelSelector oms, ServletContext ctx) {
	 * 		ctx.setAttribute(BASE_ONT_MODEL_SELECTOR, oms); 
	 * 	}
	 * 	
	 * 	public static OntModelSelector getInferenceOntModelSelector(ServletContext ctx) {
	 * 		return (OntModelSelector) ctx.getAttribute(INFERENCE_ONT_MODEL_SELECTOR);
	 * 	}
	 * 	
	 * 	public static void setInferenceOntModelSelector(OntModelSelector oms, ServletContext ctx) {
	 * 		ctx.setAttribute(INFERENCE_ONT_MODEL_SELECTOR, oms); 
	 * 	}
	 * 	
	 * 	public static OntModel getJenaOntModel(ServletContext ctx) {
	 * 		return (OntModel) ctx.getAttribute(JENA_ONT_MODEL);
	 * 	}
	 * 	
	 * 	public static void setJenaOntModel(OntModel ontModel, ServletContext ctx) {
	 * 		ctx.setAttribute(JENA_ONT_MODEL, ontModel);
	 * 	}
	 * 	
	 * 	public static OntModel getBaseOntModel(ServletContext ctx) {
	 * 		return (OntModel) ctx.getAttribute(BASE_ONT_MODEL);
	 * 	}
	 * 	
	 * 	public static void setBaseOntModel(OntModel ontModel, ServletContext ctx) {
	 * 		ctx.setAttribute(BASE_ONT_MODEL, ontModel);
	 * 	}
	 * 	
	 * 	public static OntModel getInferenceOntModel(ServletContext ctx) {
	 * 		return (OntModel) ctx.getAttribute(INFERENCE_ONT_MODEL);
	 * 	}
	 * 	
	 * 	public static void setInferenceOntModel(OntModel ontModel, ServletContext ctx) {
	 * 		ctx.setAttribute(INFERENCE_ONT_MODEL, ontModel);
	 * 	}
	 * 
	 * </pre>
	 */
	/**
	 * <pre>
	 * VitroRequest.getAssertionsWebappDaoFactory()
	 * VitroRequest.getDeductionsWebappDaoFactory()
	 * VitroRequest.getFullWebappDaoFactory()
	 * VitroRequest.getJenaOntModel()
	 * VitroRequest.getRDFService()
	 * VitroRequest.getUnfilteredRDFService()
	 * VitroRequest.getWebappDaoFactory()
	 * VitroRequest.getWriteModel()
	 * ModelContext.getBaseOntModelSelector()
	 * ModelContext.getInferenceOntModel()
	 * ModelContext.getInferenceOntModelSelector()
	 * ModelContext.getJenaOntModel()
	 * ModelContext.getOntModelSelector()
	 * ModelContext.getUnionOntModelSelector()
	 * OntModelSelector.getAboxModel
	 * OntModelSelector.getApplicationMetadataModel()
	 * OntModelSelector.getFullModel()
	 * OntModelSelector.getTBoxModel()
	 * OntModelSelector.getTBoxModel(ontologyURI)
	 * OntModelSelector.getUserAccountsModel()
	 * VitroModelSource.getModel(URL)
	 * VitroModelSource.getModel(URL, loadIfAbsent)
	 * VitroModelSource.openModel(name)
	 * VitroModelSource.openModelIfPresent(string)
	 * ServletContext.getAttribute("assertionsWebappDaoFactory")
	 * ServletContext.getAttribute("baseOntModelSelector")
	 * ServletContext.getAttribute("jenaOntModel")
	 * ServletContext.getAttribute("jenaPersistentOntModel")
	 * ServletContext.getAttribute("pelletOntModel")
	 * ServletContext.getAttribute("webappDaoFactory")
	 * VitroJenaModelMaker
	 * VitroJenaSpecialModelMaker
	 * JenaDataSourceSetupBase.getApplicationDataSource(ctx)
	 * JenaDataSourceSetupBase.getStartupDataset()
	 * HttpSession.getAttribute("jenaAuditModel")
	 * HttpSession.getAttribute("jenaOntModel")
	 * ServletRequest.getAttribute("jenaOntModel")
	 * </pre>
	 */

	public void setOntModel(ModelID id, OntModel ontModel) {
		String key = id.toString();
		if (ontModel == null) {
			modelMap.remove(key);
		} else {
			modelMap.put(key, ontModel);
		}
	}

	public OntModel getOntModel(ModelID id) {
		String key = id.toString();
		if (modelMap.containsKey(key)) {
			return modelMap.get(key);
		} else if (parent != null) {
			return parent.getOntModel(id);
		} else {
			return null;
		}
	}

}
