/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Assists the role-based policies in determining whether a property or resource
 * may be displayed, modified, or published in linked open data.
 * 
 * There is a singleton bean that holds the current threshold role levels for
 * displaying, modifying, or publishing restricted properties.
 * 
 * Create this bean after the context models are in place.
 * 
 * Add PropertyRestrictionListener to your EditProcessObject if you are editing
 * a property, to ensure that the bean stays current.
 */
public abstract class PropertyRestrictionBean {
	private static final Log log = LogFactory
			.getLog(PropertyRestrictionBean.class);

	private static final PropertyRestrictionBean NULL = new PropertyRestrictionBeanNull();

	protected static volatile PropertyRestrictionBean instance = NULL;

	protected static final Collection<String> PROHIBITED_NAMESPACES = Arrays
			.asList(new String[] { VitroVocabulary.vitroURI, "" });

	protected static final Collection<String> PERMITTED_EXCEPTIONS = Arrays
			.asList(new String[] { VitroVocabulary.MONIKER,
					VitroVocabulary.MODTIME, VitroVocabulary.IND_MAIN_IMAGE,
					VitroVocabulary.LINK, VitroVocabulary.PRIMARY_LINK,
					VitroVocabulary.ADDITIONAL_LINK,
					VitroVocabulary.LINK_ANCHOR, VitroVocabulary.LINK_URL });

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	public static PropertyRestrictionBean getBean() {
		return instance;
	}

	// ----------------------------------------------------------------------
	// instance methods
	// ----------------------------------------------------------------------

	/**
	 * Any resource can be displayed.
	 * 
	 * (Someday we may want to implement display restrictions based on VClass.)
	 */
	public abstract boolean canDisplayResource(String resourceUri,
			RoleLevel userRole);

	/**
	 * A resource cannot be modified if its namespace is in the prohibited list
	 * (but some exceptions are allowed).
	 * 
	 * (Someday we may want to implement modify restrictions based on VClass.)
	 */
	public abstract boolean canModifyResource(String resourceUri,
			RoleLevel userRole);

	/**
	 * Any resource can be published.
	 * 
	 * (Someday we may want to implement publish restrictions based on VClass.)
	 */
	public abstract boolean canPublishResource(String resourceUri,
			RoleLevel userRole);

	/**
	 * If display of a predicate is restricted, the user's role must be at least
	 * as high as the restriction level.
	 */
	public abstract boolean canDisplayPredicate(Property predicate,
			RoleLevel userRole);

	/**
	 * A predicate cannot be modified if its namespace is in the prohibited list
	 * (some exceptions are allowed).
	 * 
	 * If modification of a predicate is restricted, the user's role must be at
	 * least as high as the restriction level.
	 */
	public abstract boolean canModifyPredicate(Property predicate,
			RoleLevel userRole);

	/**
	 * If publishing of a predicate is restricted, the user's role must be at
	 * least as high as the restriction level.
	 */
	public abstract boolean canPublishPredicate(Property predicate,
			RoleLevel userRole);

	/**
	 * The threshold values for this property may have changed.
	 */
	public abstract void updateProperty(PropertyRestrictionLevels levels);

	// ----------------------------------------------------------------------
	// The null implementation
	// ----------------------------------------------------------------------

	/**
	 * A placeholder for when the bean instance is not set.
	 */
	private static class PropertyRestrictionBeanNull extends
			PropertyRestrictionBean {
		@Override
		public boolean canDisplayResource(String resourceUri, RoleLevel userRole) {
			warn();
			return false;
		}

		@Override
		public boolean canModifyResource(String resourceUri, RoleLevel userRole) {
			warn();
			return false;
		}

		@Override
		public boolean canPublishResource(String resourceUri, RoleLevel userRole) {
			warn();
			return false;
		}

		@Override
		public boolean canDisplayPredicate(Property predicate,
				RoleLevel userRole) {
			warn();
			return false;
		}

		@Override
		public boolean canModifyPredicate(Property predicate, RoleLevel userRole) {
			warn();
			return false;
		}

		@Override
		public boolean canPublishPredicate(Property predicate,
				RoleLevel userRole) {
			warn();
			return false;
		}

		@Override
		public void updateProperty(PropertyRestrictionLevels levels) {
			warn();
		}

		private void warn() {
			try {
				throw new IllegalStateException();
			} catch (IllegalStateException e) {
				log.warn("No PropertyRestrictionBean in place.", e);
			}
		}
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
				instance = new PropertyRestrictionBeanImpl(
						PROHIBITED_NAMESPACES, PERMITTED_EXCEPTIONS,
						ModelAccess.on(ctx));
			} catch (Exception e) {
				ss.fatal(this,
						"could not set up PropertyRestrictionBean", e);
			}
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			instance = NULL;
		}
	}

}
