/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.jga.fn.UnaryFunctor;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.EntityPropertyListFilter;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

public class PropertyMaskingSetup implements ServletContextListener {

	private final static String ENTITY_PROPERTY_LIST_FILTER_ATTR_NAME = "entityPropertyListFilter";

	public void contextInitialized(ServletContextEvent sce) {
		OntModel jenaOntModel = ModelAccess.on(sce.getServletContext()).getOntModel();
        sce.getServletContext().setAttribute(ENTITY_PROPERTY_LIST_FILTER_ATTR_NAME, new EntityPropertyListFilter(jenaOntModel));
	}

	public static UnaryFunctor<List<Property>,List<Property>> getEntityPropertyListFilter(ServletContext ctx) {
		return (UnaryFunctor<List<Property>,List<Property>>) ctx.getAttribute(ENTITY_PROPERTY_LIST_FILTER_ATTR_NAME);
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to worry about
	}

}
