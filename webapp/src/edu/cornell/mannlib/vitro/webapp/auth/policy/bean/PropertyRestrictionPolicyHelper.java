/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.ApplicationConfigurationOntologyUtils;

/**
 * Assists the role-based policies in determining whether a property or resource
 * may be displayed or modified.
 * 
 * There is a bean in the context that holds the current threshold role levels
 * for displaying and modifying restricted properties.
 * 
 * Create this bean after the Jena model is in place in the context.
 * 
 * Add PropertyRestrictionListener to your EditProcessObject if you are editing
 * a property, to ensure that the bean stays current.
 */
public class PropertyRestrictionPolicyHelper {
	private static final Log log = LogFactory
			.getLog(PropertyRestrictionPolicyHelper.class);

	private static final Collection<String> PROHIBITED_NAMESPACES = Arrays
			.asList(new String[] { VitroVocabulary.vitroURI, "" });

	private static final Collection<String> PERMITTED_EXCEPTIONS = Arrays
			.asList(new String[] {
					VitroVocabulary.MONIKER,
					VitroVocabulary.MODTIME,

					VitroVocabulary.IND_MAIN_IMAGE,

					VitroVocabulary.LINK,
					VitroVocabulary.PRIMARY_LINK,
					VitroVocabulary.ADDITIONAL_LINK,
					VitroVocabulary.LINK_ANCHOR,
					VitroVocabulary.LINK_URL });

	/**
	 * The bean is attached to the ServletContext using this attribute name.
	 */
	private static final String ATTRIBUTE_NAME = PropertyRestrictionPolicyHelper.class
			.getName();

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	public static PropertyRestrictionPolicyHelper getBean(ServletContext ctx) {
		Object attribute = ctx.getAttribute(ATTRIBUTE_NAME);
		if (!(attribute instanceof PropertyRestrictionPolicyHelper)) {
			throw new IllegalStateException(
					"PropertyRestrictionPolicyHelper has not been initialized.");
		}
		return (PropertyRestrictionPolicyHelper) attribute;
	}

	private static void removeBean(ServletContext ctx) {
		ctx.removeAttribute(ATTRIBUTE_NAME);
	}

	public static void setBean(ServletContext ctx,
			PropertyRestrictionPolicyHelper bean) {
		if (bean == null) {
			throw new NullPointerException("bean may not be null.");
		}
		ctx.setAttribute(ATTRIBUTE_NAME, bean);
	}

	/**
	 * Initialize the bean with the standard prohibitions and exceptions, and
	 * with the thresholds obtained from the model.
	 */
	public static PropertyRestrictionPolicyHelper createBean(OntModel model, 
	                                                         Model displayModel) {
	    
	    
		Map<Pair<String, Pair<String,String>>, RoleLevel> displayThresholdMap = 
		        new HashMap<Pair<String, Pair<String,String>>, RoleLevel>();
		Map<Pair<String, Pair<String,String>>, RoleLevel> modifyThresholdMap = 
		        new HashMap<Pair<String, Pair<String,String>>, RoleLevel>();
		
		OntModel union = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
		        ModelFactory.createUnion(displayModel, model));


		populateThresholdMap(union, displayThresholdMap,
				VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
		populateThresholdMap(
				union,
				modifyThresholdMap,
				VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);


		PropertyRestrictionPolicyHelper bean = new PropertyRestrictionPolicyHelper(
				PROHIBITED_NAMESPACES, PERMITTED_EXCEPTIONS,
				displayThresholdMap, modifyThresholdMap, displayModel);

		return bean;
	}

	private RoleLevel getModifyThreshold(Property property) {
	    return getThreshold(property, modifyThresholdMap);
	}
	
	private RoleLevel getThreshold(Property property, 
	                               Map<Pair<String, Pair<String,String>>, RoleLevel> 
	                                       thresholdMap) {
	    if (property.getURI() == null) {
	        return RoleLevel.NOBODY;
	    }
	    RoleLevel roleLevel = getRoleLevelFromMap(
	            property.getDomainVClassURI(), property.getURI(), 
	                    property.getRangeVClassURI(), thresholdMap);
	    if (roleLevel == null) {
	        roleLevel = getRoleLevelFromMap(
	                OWL.Thing.getURI(), property.getURI(), OWL.Thing.getURI(),
	                        thresholdMap);
	    }
	    return roleLevel;
	}
	
	private RoleLevel getRoleLevelFromMap(String domainURI, 
	                                      String predicateURI, 
	                                      String rangeURI,
	                                      Map<Pair<String, Pair<String,String>>, 
	                                              RoleLevel> map) {
	    return map.get(
                new Pair<String, Pair<String,String>>(
                        domainURI, new Pair<String,String>(
                                predicateURI, rangeURI)));
	}
	
	/**
	 * Find all the resources that possess this property, and map the resource
	 * URI to the required RoleLevel.
	 */
	private static void populateThresholdMap(OntModel model,
			Map<Pair<String,Pair<String,String>>, RoleLevel> map, String propertyUri) {
		model.enterCriticalSection(Lock.READ);
		try {
		    com.hp.hpl.jena.rdf.model.Property property = model.getProperty(propertyUri);
			StmtIterator stmts = model.listStatements((Resource) null,
					property, (Resource) null);
			try {
    			while (stmts.hasNext()) {
    				Statement stmt = stmts.next();
    				Resource subject = stmt.getSubject();
    				RDFNode objectNode = stmt.getObject();
    				if ((subject == null) || (!(objectNode instanceof Resource))) {
    					continue;
    				}
    				Resource object = (Resource) objectNode;
    				RoleLevel role = RoleLevel.getRoleByUri(object.getURI());
    				map.put(new Pair<String,Pair<String,String>>(
    				        OWL.Thing.getURI(), new Pair<String,String>(
    				                subject.getURI(), OWL.Thing.getURI())), role);
    			} 
			} finally {
	            stmts.close();			    
			}
            List<ObjectProperty> fauxOps = ApplicationConfigurationOntologyUtils
                    .getAdditionalFauxSubproperties(null, null, model, model);
            for (ObjectProperty faux : fauxOps) {
                RoleLevel role = null;
                if(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT
                        .equals(propertyUri)) {
                    role = faux.getProhibitedFromUpdateBelowRoleLevel();
                } else if (VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT
                        .equals(propertyUri)) {
                    role = faux.getHiddenFromDisplayBelowRoleLevel();
                }
                if (role != null) {
                    log.debug("Putting D:" + faux.getDomainVClassURI() + " P:" + faux.getURI() + " R:" + faux.getRangeVClassURI() + " ==> L:" + role);
                    map.put(new Pair<String,Pair<String,String>>(
                            faux.getDomainVClassURI(), new Pair<String,String>(
                                    faux.getURI(), faux.getRangeVClassURI())), role);
                }
            }

		} finally {
			model.leaveCriticalSection();
		}
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	/**
	 * URIs in these namespaces can't be modified, unless they are listed in the
	 * exceptions.
	 */
	private final Collection<String> modifyProhibitedNamespaces;

	/**
	 * These URIs can be modified, even if they are in the prohibited
	 * namespaces.
	 */
	private final Collection<String> modifyExceptionsAllowedUris;

	/**
	 * These URIs can be displayed only if the user's role is at least as high
	 * as the threshold role.
	 */
	private final Map<Pair<String, Pair<String,String>>, RoleLevel> displayThresholdMap;

	/**
	 * These URIs can be modified only if the user's role is at least as high as
	 * the threshold role.
	 */
	private final Map<Pair<String, Pair<String,String>>, RoleLevel> modifyThresholdMap;
	

	/**
	 * Store unmodifiable versions of the inputs.
	 * 
	 * Protected access: the bean should only be created by the static methods,
	 * or by unit tests.
	 */
	protected PropertyRestrictionPolicyHelper(
			Collection<String> modifyProhibitedNamespaces,
			Collection<String> modifyExceptionsAllowedUris,
			Map<Pair<String, Pair<String,String>>, RoleLevel> displayThresholdMap,
			Map<Pair<String, Pair<String,String>>, RoleLevel> modifyThresholdMap,
			Model displayModel) {
		this.modifyProhibitedNamespaces = unmodifiable(modifyProhibitedNamespaces);
		this.modifyExceptionsAllowedUris = unmodifiable(modifyExceptionsAllowedUris);
		this.displayThresholdMap = displayThresholdMap;
		this.modifyThresholdMap = modifyThresholdMap;
//	    this.displayThresholdMap = unmodifiable(displayThresholdMap);
//	    this.modifyThresholdMap = unmodifiable(modifyThresholdMap);

		if (log.isDebugEnabled()) {
			log.debug("prohibited: " + this.modifyProhibitedNamespaces);
			log.debug("exceptions: " + this.modifyExceptionsAllowedUris);
			log.debug("display thresholds: " + this.displayThresholdMap);
			log.debug("modify thresholds: " + this.modifyThresholdMap);
		}
	}

	private Collection<String> unmodifiable(Collection<String> raw) {
		if (raw == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableCollection(new HashSet<String>(raw));
		}
	}

	@SuppressWarnings("unused")
	private Map<String, RoleLevel> unmodifiable(Map<String, RoleLevel> raw) {
		if (raw == null) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(new HashMap<String, RoleLevel>(
					raw));
		}
	}

	/**
	 * Any resource can be displayed.
	 * 
	 * (Someday we may want to implement display restrictions based on VClass.)
	 */
	@SuppressWarnings("unused")
	public boolean canDisplayResource(String resourceUri, RoleLevel userRole) {
		if (resourceUri == null) {
			log.debug("can't display resource: resourceUri was null");
			return false;
		}

		log.debug("can display resource '" + resourceUri + "'");
		return true;
	}

	/**
	 * A resource cannot be modified if its namespace is in the prohibited list
	 * (but some exceptions are allowed).
	 * 
	 * (Someday we may want to implement modify restrictions based on VClass.)
	 */
	@SuppressWarnings("unused")
	public boolean canModifyResource(String resourceUri, RoleLevel userRole) {
		if (resourceUri == null) {
			log.debug("can't modify resource: resourceUri was null");
			return false;
		}

		if (modifyProhibitedNamespaces.contains(namespace(resourceUri))) {
			if (modifyExceptionsAllowedUris.contains(resourceUri)) {
				log.debug("'" + resourceUri + "' is a permitted exception");
			} else {
				log.debug("can't modify resource '" + resourceUri
						+ "': prohibited namespace: '" + namespace(resourceUri)
						+ "'");
				return false;
			}
		}

		log.debug("can modify resource '" + resourceUri + "'");
		return true;
	}

	/**
	 * If display of a predicate is restricted, the user's role must be at least
	 * as high as the restriction level.
	 */
	public boolean canDisplayPredicate(Property predicate, RoleLevel userRole) {
	    //TODO change
	    String predicateUri = predicate.getURI();
	    
		if (predicateUri == null) {
			log.debug("can't display predicate: predicateUri was null");
			return false;
		}
		
		RoleLevel displayThreshold = getThreshold(predicate, displayThresholdMap);		
		       	        
		if (isAuthorized(userRole, displayThreshold)) {
			log.debug("can display predicate: '" + predicateUri
					+ "', userRole=" + userRole + ", thresholdRole="
					+ displayThreshold);
			return true;
		}

		log.debug("can't display predicate: '" + predicateUri + "', userRole="
				+ userRole + ", thresholdRole=" + displayThreshold);
		return false;
	}
	
	/**
	 * A predicate cannot be modified if its namespace is in the prohibited list
	 * (some exceptions are allowed).
	 * 
	 * If modification of a predicate is restricted, the user's role must be at
	 * least as high as the restriction level.
	 */
	public boolean canModifyPredicate(Property predicate, RoleLevel userRole) {
		if (predicate == null || predicate.getURI() == null) {
			log.debug("can't modify predicate: predicate was null");
			return false;
		}

		if (modifyProhibitedNamespaces.contains(namespace(predicate.getURI()))) {
			if (modifyExceptionsAllowedUris.contains(predicate.getURI())) {
				log.debug("'" + predicate.getURI() + "' is a permitted exception");
			} else {
				log.debug("can't modify resource '" + predicate.getURI()
						+ "': prohibited namespace: '"
						+ namespace(predicate.getURI()) + "'");
				return false;
			}
		}

		RoleLevel modifyThreshold = getModifyThreshold(predicate);
		if (isAuthorized(userRole, modifyThreshold)) {
			log.debug("can modify predicate: '" + predicate.getURI() + "', domain=" 
		            + predicate.getDomainVClassURI() + ", range=" 
			        + predicate.getRangeVClassURI() + ", userRole="
					+ userRole + ", thresholdRole=" + modifyThreshold);
			return true;
		}

        log.debug("can't modify predicate: '" + predicate.getURI() + "', domain=" 
                + predicate.getDomainVClassURI() + ", range=" 
                + predicate.getRangeVClassURI() + ", userRole="
                + userRole + ", thresholdRole=" + modifyThreshold);
		return false;
	}

	private boolean isAuthorized(RoleLevel userRole, RoleLevel thresholdRole) {
		if (userRole == null) {
			return false;
		}
		if (thresholdRole == null) {
			return true;
		}
		return userRole.compareTo(thresholdRole) >= 0;
	}

	private String namespace(String uri) {
		return uri.substring(0, Util.splitNamespace(uri));
	}

	// ----------------------------------------------------------------------
	// Setup class
	// ----------------------------------------------------------------------

	/**
	 * Create the bean at startup and remove it at shutdown.
	 */
	public static class Setup implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);

			try {
				OntModel model = ModelAccess.on(ctx).getJenaOntModel();
				if (model == null) {
					throw new NullPointerException(
							"jenaOntModel has not been initialized.");
				}
                Model displayModel = ModelAccess.on(ctx).getDisplayModel();
                if (displayModel == null) {
                    throw new NullPointerException(
                            "display model has not been initialized.");
                }
                
				PropertyRestrictionPolicyHelper bean = PropertyRestrictionPolicyHelper
						.createBean(model, displayModel);
				PropertyRestrictionPolicyHelper.setBean(ctx, bean);
			} catch (Exception e) {
				ss.fatal(this, "could not set up PropertyRestrictionPolicyHelper", e);
			}
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			removeBean(sce.getServletContext());
		}
	}

}
