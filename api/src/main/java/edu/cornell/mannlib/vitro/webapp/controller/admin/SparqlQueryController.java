/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.InvalidQueryTypeException;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.SparqlQueryApiExecutor;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.SparqlQueryUtils;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.RdfResultMediaType;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.ResultSetMediaType;

/**
 * Present the SPARQL Query form, and execute the queries.
 */
@WebServlet(name = "SparqlQuery", urlPatterns = {"/admin/sparqlquery"})
public class SparqlQueryController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(SparqlQueryController.class);

	private static final String TEMPLATE_NAME = "admin-sparqlQueryForm.ftl";

	/**
	 * Always show these prefixes, even though they don't appear in the list of
	 * ontologies.
	 */
	private static final List<Prefix> DEFAULT_PREFIXES = buildDefaults();

	private static List<Prefix> buildDefaults() {
		Prefix[] array = new Prefix[] {
				new Prefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
				new Prefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
				new Prefix("xsd", "http://www.w3.org/2001/XMLSchema#"),
				new Prefix("owl", "http://www.w3.org/2002/07/owl#"),
				new Prefix("swrl", "http://www.w3.org/2003/11/swrl#"),
				new Prefix("swrlb", "http://www.w3.org/2003/11/swrlb#"),
				new Prefix("vitro",
						"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#") };
		return Collections.unmodifiableList(Arrays.asList(array));
	}

	private static final String[] SAMPLE_QUERY = { //
			"", //
			"#", //
			"i18n:sparql_query_description_0", //
			"i18n:sparql_query_description_1", //
			"#", //
			"SELECT ?geoLocation ?label", //
			"WHERE", //
			"{", //
			"      ?geoLocation rdf:type vivo:GeographicLocation",
			"      OPTIONAL { ?geoLocation rdfs:label ?label } ", //
			"}", //
			"LIMIT 20" //
	};

	/**
	 * If a query has been provided, we answer it directly, bypassing the
	 * Freemarker mechanisms.
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		if (!isAuthorizedToDisplayPage(req, resp,
				SimplePermission.USE_SPARQL_QUERY_PAGE.ACTION)) {
			return;
		}
		resp.addHeader("X-XSS-Protection", "0");
		if (req.getParameterMap().containsKey("query")) {
			respondToQuery(req, resp);
		} else {
			super.doGet(req, resp);
		}
	}

	private void respondToQuery(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		RDFService rdfService = ModelAccess.on(getServletContext())
				.getRDFService();

		String queryString = req.getParameter("query");
		boolean download = Boolean.parseBoolean(req.getParameter("download"));
		Query query = SparqlQueryUtils.create(queryString);
		try {
			String format = interpretRequestedFormats(req, query);
			SparqlQueryApiExecutor core = SparqlQueryApiExecutor.instance(
					rdfService, queryString, format);
			resp.setContentType(core.getMediaType());

			if (download) {	
				String extension = getFilenameExtension(req, query, format);
				resp.setHeader("Content-Transfer-Encoding", "binary");
				resp.setHeader("Content-disposition", "attachment; filename=query-results." + extension);
			}

			core.executeAndFormat(resp.getOutputStream());
		} catch (InvalidQueryTypeException e) {
			do400BadRequest("Query type is not SELECT, ASK, CONSTRUCT, "
					+ "or DESCRIBE: '" + queryString + "'", resp);
		} catch (QueryParseException e) {
			do400BadRequest("Failed to parse query: '" + queryString + "''", e,
					resp);
		} catch (NotAcceptableException | AcceptHeaderParsingException e) {
			do500InternalServerError("Problem with the page fields: the "
					+ "selected fields do not include an "
					+ "acceptable content type.", e, resp);
		} catch (RDFServiceException e) {
			do500InternalServerError("Problem executing the query.", e, resp);
		}
	}

	private String interpretRequestedFormats(HttpServletRequest req,
			Query query) throws NotAcceptableException {
		String parameterName = (query.isSelectType() || query.isAskType()) ? "resultFormat"
				: "rdfResultFormat";
		String parameterValue = req.getParameter(parameterName);
		if (StringUtils.isBlank(parameterValue)) {
			throw new NotAcceptableException("Parameter '" + parameterName
					+ "' was '" + parameterValue + "'.");
		} else {
			return parameterValue;
		}
	}

	private String getFilenameExtension(HttpServletRequest req, 
			Query query, String format) {
		String extension;
		if (query.isSelectType() || query.isAskType()) {
			ResultSetMediaType mediaType = ResultSetMediaType.fromContentType(format);
			extension = mediaType.getExtension();
		}
		else {
			RdfResultMediaType mediaType = RdfResultMediaType.fromContentType(format);
			extension = mediaType.getExtension();
		}
		return extension;
	}

	private void do400BadRequest(String message, HttpServletResponse resp)
			throws IOException {
		resp.setStatus(400);
		resp.getWriter().println(message);
	}

	private void do400BadRequest(String message, Exception e,
			HttpServletResponse resp) throws IOException {
		resp.setStatus(400);
		PrintWriter w = resp.getWriter();
		w.println(message);
		e.printStackTrace(w);
	}

	private void do500InternalServerError(String message, Exception e,
			HttpServletResponse resp) throws IOException {
		resp.setStatus(500);
		PrintWriter w = resp.getWriter();
		w.println(message);
		e.printStackTrace(w);
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) throws Exception {
		I18nBundle i18n = I18n.bundle(vreq);

		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put("sampleQuery", buildSampleQuery(i18n, buildPrefixList(vreq)));
		bodyMap.put("title", i18n.text("sparql_query_title"));
		bodyMap.put("submitUrl", UrlBuilder.getUrl("admin/sparqlquery"));
		return new TemplateResponseValues(TEMPLATE_NAME, bodyMap);
	}

	private List<Prefix> buildPrefixList(VitroRequest vreq) {
		List<Prefix> prefixList = new ArrayList<>(DEFAULT_PREFIXES);

		OntologyDao dao = vreq.getUnfilteredWebappDaoFactory().getOntologyDao();
		List<Ontology> ontologies = dao.getAllOntologies();
		if (ontologies == null) {
			ontologies = Collections.emptyList();
		}

		int unnamedOntologyIndex = 1;

		for (Ontology ont : ontologies) {
			String prefix = ont.getPrefix();
			if (prefix == null) {
				prefix = "p" + unnamedOntologyIndex++;
			}
			prefixList.add(new Prefix(prefix, ont.getURI()));
		}

		return prefixList;
	}

	private String buildSampleQuery(I18nBundle i18n, List<Prefix> prefixList) {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);

		for (Prefix p : prefixList) {
			writer.println(p);
		}
		for (String line : SAMPLE_QUERY) {
			if (line.startsWith("i18n:")) {
			    // Get i18n translation
				line =  i18n.text(line.substring("i18n:".length()));
			}
			writer.println(line);
		}

		return sw.toString();
	}

	public static class Prefix {
		private final String prefix;
		private final String uri;

		public Prefix(String prefix, String uri) {
			this.prefix = prefix;
			this.uri = uri;
		}

		@Override
		public String toString() {
			return String.format("PREFIX %-9s <%s>", prefix + ":", uri);
		}
	}

}
