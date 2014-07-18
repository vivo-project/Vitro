/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess;

import static edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService.CONTENT;

import javax.servlet.ServletContext;

import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService;

/**
 * Convenience methods for obtaining the ModelMakerFactories.
 */
public class ModelMakerUtils {
	public static final String ATTRIBUTE_BASE = ModelMakerUtils.class.getName();

	public static void setContentModelMakerFactory(ServletContext ctx,
			ModelMakerFactory mmFactory) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}
		if (mmFactory == null) {
			throw new NullPointerException("mmFactory may not be null.");
		}
		if (mmFactory.whichModelMaker() != CONTENT) {
			throw new IllegalArgumentException(
					"mmFactory must be a CONTENT ModelMakerFactory");
		}
		ctx.setAttribute(attributeName(CONTENT), mmFactory);
	}

	public static void setConfigurationModelMakerFactory(ServletContext ctx,
			ModelMakerFactory mmFactory) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}
		if (mmFactory == null) {
			throw new NullPointerException("mmFactory may not be null.");
		}
		if (mmFactory.whichModelMaker() != CONFIGURATION) {
			throw new IllegalArgumentException(
					"mmFactory must be a CONFIGURATION ModelMakerFactory");
		}
		ctx.setAttribute(attributeName(CONFIGURATION), mmFactory);
	}

	public static ModelMaker getModelMaker(ServletContext ctx,
			WhichService which) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}
		if (which == null) {
			throw new NullPointerException("which may not be null.");
		}
		return getFactory(ctx, which).getModelMaker(
				RDFServiceUtils.getRDFServiceFactory(ctx)
						.getShortTermRDFService());
	}

	public static ModelMaker getShortTermModelMaker(ServletContext ctx,
			RDFService shortTermRdfService, WhichService which) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}
		if (shortTermRdfService == null) {
			throw new NullPointerException(
					"shortTermRdfService may not be null.");
		}
		if (which == null) {
			throw new NullPointerException("which may not be null.");
		}

		return getFactory(ctx, which).getShortTermModelMaker(
				shortTermRdfService);
	}

	private static ModelMakerFactory getFactory(ServletContext ctx,
			WhichService which) {
		Object attribute = ctx.getAttribute(attributeName(which));
		if (attribute instanceof ModelMakerFactory) {
			return (ModelMakerFactory) attribute;
		} else {
			throw new IllegalStateException("Expected a ModelMakerFactory at '"
					+ attributeName(which) + "', but found " + attribute);
		}
	}

	private static String attributeName(WhichService which) {
		return ATTRIBUTE_BASE + "-" + which;
	}

	/**
	 * No need for an instance - all methods are static.
	 */
	private ModelMakerUtils() {
		// Nothing to instantiate.
	}

}
