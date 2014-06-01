/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.BASE_FULL;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.BASE_TBOX;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.DISPLAY_DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.DISPLAY_TBOX;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.INFERRED_FULL;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.INFERRED_TBOX;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.UNION_FULL;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID.USER_ACCOUNTS;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelMakerID.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelMakerID.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_APPLICATION_METADATA_MODEL;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_DISPLAY_DISPLAY_MODEL;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_DISPLAY_METADATA_MODEL;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_DISPLAY_TBOX_MODEL;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_TBOX_INF_MODEL;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_USER_ACCOUNTS_MODEL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroInterceptingModelMaker;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Sets up the content models, OntModelSelectors and webapp DAO factories.
 */
public class ModelMakerSetup extends JenaDataSourceSetupBase implements
		javax.servlet.ServletContextListener {
	private static final Log log = LogFactory.getLog(ModelMakerSetup.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		createConfigurationModelMaker(ctx);
		createContentModelMaker(ctx);

		ss.info(this, "Created model makers.");
	}

	private void createConfigurationModelMaker(ServletContext ctx) {
		RDFServiceFactory rdfServiceFactory = RDFServiceUtils
				.getRDFServiceFactory(ctx, WhichService.CONFIGURATION);
		RDFServiceModelMaker configMM = new RDFServiceModelMaker(
				rdfServiceFactory);
		Map<String, Model> specials = populateConfigurationSpecialMap(ctx);
		VitroInterceptingModelMaker viMM = new VitroInterceptingModelMaker(
				configMM, specials);
		ModelAccess.on(ctx).setModelMaker(CONFIGURATION, viMM);
	}

	private void createContentModelMaker(ServletContext ctx) {
		RDFServiceFactory rdfServiceFactory = RDFServiceUtils
				.getRDFServiceFactory(ctx);
		RDFServiceModelMaker contentMM = new RDFServiceModelMaker(
				rdfServiceFactory);
		Map<String, Model> specials = populateContentSpecialMap(ctx);
		VitroInterceptingModelMaker viMM = new VitroInterceptingModelMaker(
				contentMM, specials);
		ModelAccess.on(ctx).setModelMaker(CONTENT, viMM);
	}

	private Map<String, Model> populateConfigurationSpecialMap(
			ServletContext ctx) {
		Map<String, Model> map = new HashMap<>();
		map.put(JENA_DISPLAY_METADATA_MODEL,
				ModelAccess.on(ctx).getOntModel(DISPLAY));
		map.put(JENA_DISPLAY_TBOX_MODEL,
				ModelAccess.on(ctx).getOntModel(DISPLAY_TBOX));
		map.put(JENA_DISPLAY_DISPLAY_MODEL,
				ModelAccess.on(ctx).getOntModel(DISPLAY_DISPLAY));
		map.put(JENA_USER_ACCOUNTS_MODEL,
				ModelAccess.on(ctx).getOntModel(USER_ACCOUNTS));
		return map;
	}

	private Map<String, Model> populateContentSpecialMap(ServletContext ctx) {
		Map<String, Model> map = new HashMap<>();

		map.put("vitro:jenaOntModel",
				ModelAccess.on(ctx).getOntModel(UNION_FULL));
		map.put("vitro:baseOntModel", ModelAccess.on(ctx)
				.getOntModel(BASE_FULL));
		map.put("vitro:inferenceOntModel",
				ModelAccess.on(ctx).getOntModel(INFERRED_FULL));
		map.put(JENA_TBOX_ASSERTIONS_MODEL,
				ModelAccess.on(ctx).getOntModel(BASE_TBOX));
		map.put(JENA_TBOX_INF_MODEL,
				ModelAccess.on(ctx).getOntModel(INFERRED_TBOX));
		map.put(JENA_APPLICATION_METADATA_MODEL, ModelAccess.on(ctx)
				.getOntModel(APPLICATION_METADATA));

		return map;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do.
	}

}
