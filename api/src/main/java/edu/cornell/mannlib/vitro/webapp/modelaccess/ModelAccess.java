/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.ContextModelAccessImpl;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.RequestModelAccessImpl;
import edu.cornell.mannlib.vitro.webapp.triplesource.CombinedTripleSource;

/**
 * The root access point for the RDF data structures: RDFServices, Datasets,
 * ModelMakers, OntModels, OntModelSelectors and WebappDaoFactories.
 * 
 * Get a long-term data structure by accessing from the context. Get a
 * short-term data structure by accessing from the request.
 * 
 * <pre>
 * ModelAccess.on(ctx).getRDFService(CONFIGURATION);
 * ModelAccess.on(req).getOntModel(ModelNames.DISPLAY);
 * </pre>
 * 
 * ------------------------------------
 * 
 * The elaborate structure of options enums allows us to specify method
 * signatures like this on RequestModelAccess:
 * 
 * <pre>
 * getOntModelSelector(OntModelSelectorOption... options);
 * </pre>
 * 
 * Which can be invoked in any of these ways:
 * 
 * <pre>
 * ModelAccess.on(req).getOntModelSelector();
 * ModelAccess.on(req).getOntModelSelector(LANGUAGE_NEUTRAL);
 * ModelAccess.on(req).getOntModelSelector(INFERENCES_ONLY);
 * ModelAccess.on(req).getOntModelSelector(ASSERTIONS_ONLY, LANGUAGE_NEUTRAL);
 * </pre>
 * 
 * The compiler insures that only appropriate options are specified. However, if
 * conflicting options are supplied, it will only be caught at runtime.
 */
public class ModelAccess {
	private static final Log log = LogFactory.getLog(ModelAccess.class);

	// ----------------------------------------------------------------------
	// The options enums.
	// ----------------------------------------------------------------------

	/*
	 * It may seem complicated, but it allows us to verify options at compile
	 * time, and to provide or omit them in any order in a method call.
	 */

	public interface ModelAccessOption {
		boolean isDefault();
	}

	public interface RdfServiceOption extends ModelAccessOption {
		// Just a marker interface
	}

	public interface DatasetOption extends ModelAccessOption {
		// Just a marker interface
	}

	public interface OntModelSelectorOption extends ModelAccessOption {
		// Just a marker interface
	}

	public interface WebappDaoFactoryOption extends ModelAccessOption {
		// Just a marker interface
	}

	@SuppressWarnings("hiding")
	public enum LanguageOption implements RdfServiceOption, DatasetOption,
			OntModelSelectorOption, WebappDaoFactoryOption {
		LANGUAGE_NEUTRAL, LANGUAGE_AWARE;
		@Override
		public boolean isDefault() {
			return this == LANGUAGE_AWARE;
		}
	}

	@SuppressWarnings("hiding")
	public enum ReasoningOption implements OntModelSelectorOption,
			WebappDaoFactoryOption {
		ASSERTIONS_ONLY, INFERENCES_ONLY, ASSERTIONS_AND_INFERENCES;
		@Override
		public boolean isDefault() {
			return this == ASSERTIONS_AND_INFERENCES;
		}
	}

	@SuppressWarnings("hiding")
	public enum PolicyOption implements WebappDaoFactoryOption {
		POLICY_NEUTRAL, POLICY_AWARE;
		@Override
		public boolean isDefault() {
			return this == POLICY_AWARE;
		}

	}

	@SuppressWarnings("hiding")
	public enum WhichService implements RdfServiceOption, DatasetOption {
		CONTENT, CONFIGURATION;
		@Override
		public boolean isDefault() {
			return this == CONTENT;
		}

	}

	/*
	 * This is the easiest way to specify an option:
	 * ModelAccess.LANGUAGE_NEUTRAL, instead of
	 * ModelAccess.LanguageOption.LANGUAGE_NEUTRAL
	 */
	public static final LanguageOption LANGUAGE_NEUTRAL = LanguageOption.LANGUAGE_NEUTRAL;
	public static final LanguageOption LANGUAGE_AWARE = LanguageOption.LANGUAGE_AWARE;
	public static final ReasoningOption ASSERTIONS_ONLY = ReasoningOption.ASSERTIONS_ONLY;
	public static final ReasoningOption INFERENCES_ONLY = ReasoningOption.INFERENCES_ONLY;
	public static final ReasoningOption ASSERTIONS_AND_INFERENCES = ReasoningOption.ASSERTIONS_AND_INFERENCES;
	public static final PolicyOption POLICY_NEUTRAL = PolicyOption.POLICY_NEUTRAL;
	public static final PolicyOption POLICY_AWARE = PolicyOption.POLICY_AWARE;
	public static final WhichService CONTENT = WhichService.CONTENT;
	public static final WhichService CONFIGURATION = WhichService.CONFIGURATION;

	// ----------------------------------------------------------------------
	// The factory
	// ----------------------------------------------------------------------

	private static volatile CombinedTripleSource combinedTripleSource;
	private static volatile ModelAccessFactory factory = new ModelAccessFactory();

	/** These attributes should only be accessed through this class. */
	private static final String ATTRIBUTE_NAME = ModelAccess.class.getName();

	public static void setCombinedTripleSource(CombinedTripleSource source) {
		if (combinedTripleSource != null) {
			log.warn("Assigning CombinedTripleSource " + source
					+ ", but was already set to " + combinedTripleSource);
		}
		combinedTripleSource = source;
	}

	public static RequestModelAccess on(HttpServletRequest req) {
		Object o = req.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof RequestModelAccess) {
			return (RequestModelAccess) o;
		} else {
			RequestModelAccess access = factory.buildRequestModelAccess(req);
			req.setAttribute(ATTRIBUTE_NAME, access);
			return access;
		}
	}

	public static boolean isPresent(HttpServletRequest req) {
		return (req.getAttribute(ATTRIBUTE_NAME) instanceof RequestModelAccess);
	}

	public static ContextModelAccess on(ServletContext ctx) {
		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof ContextModelAccess) {
			return (ContextModelAccess) o;
		} else {
			ContextModelAccess access = factory.buildContextModelAccess(ctx);
			ctx.setAttribute(ATTRIBUTE_NAME, access);
			return access;
		}
	}

	// ----------------------------------------------------------------------
	// A factory to create the instances, so we can override in unit tests.
	// ----------------------------------------------------------------------

	public static class ModelAccessFactory {
		public ContextModelAccess buildContextModelAccess(ServletContext ctx) {
			return new ContextModelAccessImpl(ctx, combinedTripleSource);
		}

		/**
		 * Note that the RequestModelAccess must be closed when the request
		 * closes.
		 */
		public RequestModelAccess buildRequestModelAccess(HttpServletRequest req) {
			return new RequestModelAccessImpl(req,
					combinedTripleSource.getShortTermCombinedTripleSource(req));
		}
	}
}
