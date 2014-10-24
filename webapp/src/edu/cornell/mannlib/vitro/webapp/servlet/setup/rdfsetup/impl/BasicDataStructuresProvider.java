/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;

import java.util.EnumMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.JoinedOntModelCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.ModelMakerOntModelCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.UnionModelsOntModelsCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.UnionModelsOntModelsCache.UnionSpec;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.DataStructuresProvider;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.ShortTermDataStructuresProvider;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * A simple implementation of DataStructuresProvider.
 * 
 * Note that we don't bother to cache the RDFServices, RDFServiceFactories, and
 * ModelMakers, since the providers can be expected to cache them.
 * 
 * We must cache the OntModelCache because it was created here. If we were to
 * recreate it, we would lose any sub-models that had been attached in the
 * meantime.
 */
public class BasicDataStructuresProvider implements DataStructuresProvider {
	/**
	 * Create union models for ABox and TBox, and full models for assertions and
	 * inferences. No need to create FULL_UNION, since it's the default model.
	 */
	public static final UnionSpec[] CONTENT_UNIONS = new UnionSpec[] {
			UnionSpec.base(ABOX_ASSERTIONS).plus(ABOX_INFERENCES)
					.yields(ABOX_UNION),
			UnionSpec.base(TBOX_ASSERTIONS).plus(TBOX_INFERENCES)
					.yields(TBOX_UNION),
			UnionSpec.base(ABOX_ASSERTIONS).plus(TBOX_ASSERTIONS)
					.yields(FULL_ASSERTIONS),
			UnionSpec.base(ABOX_INFERENCES).plus(TBOX_INFERENCES)
					.yields(FULL_INFERENCES) };

	private final Map<WhichService, SingleSourceDataStructuresProvider> providers;
	private final Map<WhichService, OntModelCache> ontModels;
	private final OntModelCache ontModelCache;

	public BasicDataStructuresProvider(
			SingleSourceDataStructuresProvider contentProvider,
			SingleSourceDataStructuresProvider configurationProvider) {
		providers = new EnumMap<>(WhichService.class);
		providers.put(CONTENT, contentProvider);
		providers.put(CONFIGURATION, configurationProvider);

		ontModels = new EnumMap<>(WhichService.class);
		ontModels.put(CONTENT, new ModelMakerOntModelCache(
				getModelMaker(CONTENT)));
		ontModels.put(CONFIGURATION, new ModelMakerOntModelCache(
				getModelMaker(CONFIGURATION)));

		ontModelCache = new UnionModelsOntModelsCache(new JoinedOntModelCache(
				ontModels.get(CONTENT), ontModels.get(CONFIGURATION)),
				CONTENT_UNIONS);
	}

	protected OntModelCache getOntModels(WhichService whichService) {
		return ontModels.get(whichService);
	}

	protected RDFServiceFactory getRDFServiceFactory(WhichService whichService) {
		return providers.get(whichService).getRDFServiceFactory();
	}

	@Override
	public RDFService getRDFService(WhichService whichService) {
		return providers.get(whichService).getRDFService();
	}

	@Override
	public Dataset getDataset(WhichService whichService) {
		return providers.get(whichService).getDataset();
	}

	@Override
	public ModelMaker getModelMaker(WhichService whichService) {
		return providers.get(whichService).getModelMaker();
	}

	@Override
	public OntModelCache getOntModelCache() {
		return ontModelCache;
	}

	@Override
	public ShortTermDataStructuresProvider getShortTermDataStructuresProvider(
			HttpServletRequest req) {
		return new BasicShortTermDataStructuresProvider(req, this, providers);
	}

	@Override
	public String toString() {
		return "BasicDataStructuresProvider[" + ToString.hashHex(this)
				+ ", providers=" + providers + ", ontModels=" + ontModelCache
				+ "]";
	}

}
