/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewServiceSetup;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.TemplateAndSupplementalData;
import freemarker.core.Environment;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Find the short-view template for the specified Individual in the specified
 * context. Get any required data, and render the template to HTML.
 */
public class IndividualShortViewDirective extends BaseTemplateDirectiveModel {
	private static final Log log = LogFactory
			.getLog(IndividualShortViewDirective.class);

	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		// Get the Individual URI and check it.
		String individualUri = getRequiredSimpleScalarParameter(params, "uri");
		Individual individual = getIndividual(individualUri);
		if (individual == null) {
			throw new TemplateModelException(
					"Can't find individual for URI: \"" + individualUri + "\"");
		}

		// Get the view context and check it.
		String vcString = getRequiredSimpleScalarParameter(params,
				"viewContext");
		ShortViewContext viewContext = ShortViewContext.fromString(vcString);
		if (viewContext == null) {
			throw new TemplateModelException(
					"viewContext must be one of these: "
							+ ShortViewContext.valueList());
		}

		// Find the details of the short view and include it in the output.
		renderShortView(individual, viewContext);
	}

	private Individual getIndividual(String individualUri) {
		Environment env = Environment.getCurrentEnvironment();
		HttpServletRequest request = (HttpServletRequest) env
				.getCustomAttribute("request");
		VitroRequest vreq = new VitroRequest(request);
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
		IndividualDao iDao = wdf.getIndividualDao();
		return iDao.getIndividualByURI(individualUri);
	}

	private void renderShortView(Individual individual,
			ShortViewContext svContext) {
		Environment env = Environment.getCurrentEnvironment();
		HttpServletRequest request = (HttpServletRequest) env
				.getCustomAttribute("request");
        ServletContext ctx = request.getSession().getServletContext();

		ShortViewService svs = ShortViewServiceSetup.getService(ctx);
		if (svs == null) {
			log.warn("ShortViewService was not initialized properly.");
			return;
		}
		
		TemplateAndSupplementalData svInfo = svs.getShortViewInfo(individual,
				svContext, new VitroRequest(request));

		ObjectWrapper objectWrapper = env.getConfiguration().getObjectWrapper();

		for (String name : svInfo.getSupplementalData().keySet()) {
			Object value = svInfo.getSupplementalData().get(name);
			try {
				env.setVariable(name, objectWrapper.wrap(value));
			} catch (TemplateModelException e) {
				log.error("Failed to wrap supplemental data '" + name + "' = '"
						+ value + "'", e);
			}
		}

		try {
			Template template = env.getTemplateForInclusion(
					svInfo.getTemplateName(), null, true);
			env.include(template);
		} catch (IOException e) {
			log.error("Could not load template '" + svInfo.getTemplateName()
					+ "': " + e);
			renderErrorMessage(individual);
		} catch (TemplateException e) {
			log.error("Could not process template '" + svInfo.getTemplateName()
					+ "'", e);
			renderErrorMessage(individual);
		}
	}

	/** If there is a problem rendering the custom view, do this instead. */
	private void renderErrorMessage(Individual individual) {
		Environment env = Environment.getCurrentEnvironment();
		try {
			env.getOut().append(
					"<span>Can't process the custom short view for "
							+ individual.getName() + "</span>");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public Map<String, Object> help(String name) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		map.put("effect", "Find the short view that applies "
				+ "to this individual in this context -- a template and "
				+ "optional DataGetters. "
				+ "Execute the DataGetters and render the template.");

		map.put("comments",
				"The path should be an absolute path, starting with \"/\".");

		Map<String, String> params = new HashMap<String, String>();
		params.put("uri", "The URI of the individual being displayed.");
		params.put("viewContext",
				"One of these: " + ShortViewContext.valueList());
		map.put("parameters", params);

		List<String> examples = new ArrayList<String>();
		examples.add("&lt;img src=\"<@shortView uri=individual.uri viewContext=\"SEARCH\" />\" /&gt;");
		map.put("examples", examples);

		return map;
	}

}
