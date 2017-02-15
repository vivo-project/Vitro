/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.shortview;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Define a service that will produce HTML snippets for short views on
 * Individuals.
 */
public interface ShortViewService {

	/**
	 * Render the short view template that applies to this individual in this
	 * context. The data in the modelMap can be used to populate the template,
	 * along with any additional data returned by custom data getters.
	 * 
	 * If there are any problems, return a dummy piece of text that includes the
	 * label of the individual. Never return null or empty string.
	 * 
	 * This method should not be called from within an ongoing Freemarker
	 * process. In that case, use getShortViewInfo() instead.
	 */
	String renderShortView(Individual individual, ShortViewContext context,
			Map<String, Object> modelMap, VitroRequest vreq);

	/**
	 * What template should be used to render the short view of this individual
	 * in this context? What data is available from custom data getters?
	 * 
	 * Ask the Application Ontology for short view specifications on each of the
	 * most specific classes for this individual. If more than one such class
	 * has an applicable short view, the class with with the first URI
	 * (alphabetically) will be used.
	 */
	TemplateAndSupplementalData getShortViewInfo(Individual individual,
			ShortViewContext svContext, VitroRequest vreq);

	/**
	 * The information associated with a particular short view.
	 */
	public interface TemplateAndSupplementalData {
		/**
		 * The name of the template to be used in the short view.
		 * 
		 * Either the custom view assigned to the individual and context, or the
		 * default view. Never empty or null, but it might refer to a template
		 * that can't be located.
		 */
		String getTemplateName();

		/**
		 * The results of any custom data getters were associated with this
		 * individual in this short view context.
		 * 
		 * May be empty, but never null.
		 */
		Map<String, Object> getSupplementalData();
	}

	/**
	 * The available contexts for short views.
	 */
	public enum ShortViewContext {
		SEARCH("view-search-default.ftl"), INDEX("view-index-default.ftl"), BROWSE(
				"view-browse-default.ftl");

		private final String defaultTemplateName;

		ShortViewContext(String defaultTemplateName) {
			this.defaultTemplateName = defaultTemplateName;
		}

		public String getDefaultTemplateName() {
			return defaultTemplateName;
		}

		public static ShortViewContext fromString(String string) {
			for (ShortViewContext c : ShortViewContext.values()) {
				if (c.name().equalsIgnoreCase(string)) {
					return c;
				}
			}
			return null;
		}

		public static String valueList() {
			return StringUtils.join(ShortViewContext.values(), ", ");
		}
	}

}
