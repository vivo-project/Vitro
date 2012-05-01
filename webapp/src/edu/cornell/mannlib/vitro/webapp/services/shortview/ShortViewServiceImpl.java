/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.shortview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.services.freemarker.FreemarkerProcessingService;
import edu.cornell.mannlib.vitro.webapp.services.freemarker.FreemarkerProcessingService.TemplateParsingException;
import edu.cornell.mannlib.vitro.webapp.services.freemarker.FreemarkerProcessingServiceSetup;
import edu.cornell.mannlib.vitro.webapp.services.shortview.FakeApplicationOntologyService.TemplateAndDataGetters;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;

/**
 * The basic implementation of ShortViewService
 */
public class ShortViewServiceImpl implements ShortViewService {
	private static final Log log = LogFactory
			.getLog(ShortViewServiceImpl.class);

	private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

	private final FakeApplicationOntologyService faker;

	public ShortViewServiceImpl(FakeApplicationOntologyService faker) {
		this.faker = faker;
	}

	@Override
	public String renderShortView(Individual individual,
			ShortViewContext context, Map<String, Object> modelMap,
			VitroRequest vreq) {

		TemplateAndSupplementalData tsd = getShortViewInfo(individual, context,
				vreq);
		String templateName = tsd.getTemplateName();
		Map<String, Object> supplementalData = tsd.getSupplementalData();

		try {
			Map<String, Object> fullModelMap = new HashMap<String, Object>(
					modelMap);
			fullModelMap.putAll(supplementalData);

			FreemarkerProcessingService fps = FreemarkerProcessingServiceSetup
					.getService(vreq.getSession().getServletContext());

			if (!fps.isTemplateAvailable(templateName, vreq)) {
				return "<p>Can't locate short view template '" + templateName
						+ "' for " + individual.getName() + "</p>";
			}

			return fps.renderTemplate(templateName, fullModelMap, vreq);
		} catch (TemplateParsingException e) {
			return "<p>Can't parse the short view template '" + templateName
					+ "' for " + individual.getName() + "</p>";
		} catch (Exception e) {
			log.error(e, e);
			return "<p>Failed to render the short view for "
					+ individual.getName() + "</p>";
		}
	}

	@Override
	public TemplateAndSupplementalData getShortViewInfo(Individual individual,
			ShortViewContext svContext, VitroRequest vreq) {
		TemplateAndDataGetters tdg = fetchTemplateAndDataGetters(individual,
				svContext, vreq);
		Map<String, Object> gotData = runDataGetters(tdg.getDataGetters(), vreq);
		return new TemplateAndSupplementalDataImpl(tdg.getTemplateName(),
				gotData);
	}

	/** Get most specific classes from Individual, sorted by alpha. */
	private SortedSet<String> figureMostSpecificClassUris(Individual individual) {
		SortedSet<String> classUris = new TreeSet<String>();
		List<ObjectPropertyStatement> stmts = individual
				.getObjectPropertyStatements(VitroVocabulary.MOST_SPECIFIC_TYPE);
		for (ObjectPropertyStatement stmt : stmts) {
			classUris.add(stmt.getObjectURI());
		}
		return classUris;
	}

	/** Find the template and data getters for this individual in this context. */
	private TemplateAndDataGetters fetchTemplateAndDataGetters(
			Individual individual, ShortViewContext svContext, VitroRequest vreq) {
		List<String> classUris = new ArrayList<String>();
		classUris.addAll(figureMostSpecificClassUris(individual));

		for (String classUri : classUris) {
			TemplateAndDataGetters tdg = faker.getShortViewProperties(
					vreq.getWebappDaoFactory(), individual, classUri,
					svContext.name());
			if (tdg != null) {
				return tdg;
			}
		}

		// Didn't find one? Use the default values.
		return new TemplateAndDataGetters(svContext.getDefaultTemplateName());
	}

	/** Build a data map from the combined results of all data getters. */
	private Map<String, Object> runDataGetters(Set<DataGetter> dataGetters,
			VitroRequest vreq) {
		ServletContext ctx = vreq.getSession().getServletContext();

		Map<String, Object> gotData = new HashMap<String, Object>();
		for (DataGetter dg : dataGetters) {
			gotData.putAll(dg.getData(ctx, vreq, EMPTY_MAP));
		}
		return gotData;
	}

	private static class TemplateAndSupplementalDataImpl implements
			TemplateAndSupplementalData {
		private final String templateName;
		private final Map<String, Object> customData;

		public TemplateAndSupplementalDataImpl(String templateName,
				Map<String, Object> customData) {
			this.templateName = templateName;
			this.customData = customData;
		}

		@Override
		public String getTemplateName() {
			return templateName;
		}

		@Override
		public Map<String, Object> getSupplementalData() {
			return customData;
		}

	}
}
