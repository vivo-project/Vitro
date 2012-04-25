/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.shortview;

import static edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext.SEARCH;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;

/**
 * TODO
 * 
 * Get rid of this when the Application Ontology is implemented.
 */
public class FakeApplicationOntologyService {
	private static final String FACULTY_MEMBER_CLASS_URI = "http://vivoweb.org/ontology/core#FacultyMember";

	/**
	 * Return the template name and DataGetter instances associated with this
	 * class and this short view context. If none, return null.
	 */
	public TemplateAndDataGetters getShortViewProperties(String classUri,
			String contextName) {
//		if ((SEARCH.name().equals(contextName))
//				&& (classUri.equals(FACULTY_MEMBER_CLASS_URI))) {
//			return new TemplateAndDataGetters("view-search-faculty.ftl", new FakeFacultyDataGetter());
//		} else {
			return null;
//		}
	}

	/** The info associated with a short view. */
	public static class TemplateAndDataGetters {
		private final String templateName;
		private final Set<DataGetter> dataGetters;

		public TemplateAndDataGetters(String templateName,
				DataGetter... dataGetters) {
			this.templateName = templateName;
			this.dataGetters = new HashSet<DataGetter>(
					Arrays.asList(dataGetters));
		}

		public String getTemplateName() {
			return templateName;
		}

		public Set<DataGetter> getDataGetters() {
			return dataGetters;
		}

	}

	private static class FakeFacultyDataGetter implements DataGetter {
		@Override
		public Map<String, Object> getData(ServletContext context,
				VitroRequest vreq, Map<String, Object> valueMap) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> extras = new HashMap<String, Object>();
			extras.put("departmentName", "Department of Redundancy Department");
			map.put("extra", extras);
			return map;
		}
		
	}
}
