package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.util.HashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

@WebServlet(name = "DeleteIndividualController", urlPatterns = "/deleteIndividualController")
public class DeleteIndividualController extends FreemarkerHttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(DeleteIndividualController.class);
	private static final boolean BEGIN = true;
	private static final boolean END = !BEGIN;

	private static String TYPE_QUERY = "" + "PREFIX vitro:    <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>"
			+ "SELECT ?type " + "WHERE" + "{ ?individualURI vitro:mostSpecificType ?type ." + "}";
	private static String queryForDeleteQuery = "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS + "> \n"
			+ "SELECT ?deleteQueryText WHERE { ?associatedURI display:hasDeleteQuery ?deleteQueryText }";

	private static final String DEFAULT_DELETE_QUERY_TEXT = "DESCRIBE ?individualURI";

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.DO_FRONT_END_EDITING.ACTION;
	}

	protected ResponseValues processRequest(VitroRequest vreq) {
		String errorMessage = handleErrors(vreq);
		if (!errorMessage.isEmpty()) {
			return prepareErrorMessage(errorMessage);
		}
		String individualUri = vreq.getParameter("individualUri");
		String type = getObjectMostSpecificType(individualUri, vreq);
		Model displayModel = vreq.getDisplayModel();

		String delteQueryText = getDeleteQueryForType(type, displayModel);
		Model toRemove = getIndividualsToDelete(individualUri, delteQueryText, vreq);
		if (toRemove.size() > 0) {
			deleteIndividuals(toRemove, vreq);
		}
		String redirectUrl = getRedirectUrl(vreq);

		return new RedirectResponseValues(redirectUrl, HttpServletResponse.SC_SEE_OTHER);
	}

	private String getRedirectUrl(VitroRequest vreq) {
		String redirectUrl = vreq.getParameter("redirectUrl");
		if (redirectUrl != null) {
			return redirectUrl;
		}
		return "/";
	}

	private TemplateResponseValues prepareErrorMessage(String errorMessage) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("errorMessage", errorMessage);
		return new TemplateResponseValues("error-message.ftl", map);
	}

	private String handleErrors(VitroRequest vreq) {
		String uri = vreq.getParameter("individualUri");
		if (uri == null) {
			return "Individual uri is null. No object to delete.";
		}
		if (uri.contains(">")) {
			return "Individual uri shouldn't contain >";
		}
		return "";
	}

	private static String getDeleteQueryForType(String typeURI, Model displayModel) {
		String deleteQueryText = DEFAULT_DELETE_QUERY_TEXT;
		Query queryForTypeSpecificDeleteQuery = QueryFactory.create(queryForDeleteQuery);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("associatedURI", ResourceFactory.createResource(typeURI));
		displayModel.enterCriticalSection(Lock.READ);
		try {
			QueryExecution qexec = QueryExecutionFactory.create(queryForTypeSpecificDeleteQuery, displayModel,
					initialBindings);
			try {
				ResultSet results = qexec.execSelect();
				if (results.hasNext()) {
					QuerySolution solution = results.nextSolution();
					deleteQueryText = solution.get("deleteQueryText").toString();
				}
			} finally {
				qexec.close();
			}
		} finally {
			displayModel.leaveCriticalSection();
		}

		if (!deleteQueryText.equals(DEFAULT_DELETE_QUERY_TEXT)) {
			log.debug("For " + typeURI + " found delete query \n" + deleteQueryText);
		} else {
			log.debug("For " + typeURI + " delete query not found. Using defalut query \n" + deleteQueryText);
		}
		return deleteQueryText;
	}

	private String getObjectMostSpecificType(String individualURI, VitroRequest vreq) {
		String type = "";
		try {
			Query typeQuery = QueryFactory.create(TYPE_QUERY);
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add("individualURI", ResourceFactory.createResource(individualURI));
			Model ontModel = vreq.getJenaOntModel();
			QueryExecution qexec = QueryExecutionFactory.create(typeQuery, ontModel, bindings);
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution solution = results.nextSolution();
				type = solution.get("type").toString();
				log.debug(type);
			}
		} catch (Exception e) {
			log.error("Failed to get type for individual URI " + individualURI);
			log.error(e, e);
		}
		return type;
	}

	private Model getIndividualsToDelete(String targetIndividual, String deleteQuery, VitroRequest vreq) {
		try {
			Query queryForTypeSpecificDeleteQuery = QueryFactory.create(deleteQuery);
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add("individualURI", ResourceFactory.createResource(targetIndividual));
			Model ontModel = vreq.getJenaOntModel();
			QueryExecution qexec = QueryExecutionFactory.create(queryForTypeSpecificDeleteQuery, ontModel, bindings);
			Model results = qexec.execDescribe();
			return results;

		} catch (Exception e) {
			log.error("Query raised an error \n" + deleteQuery);
			log.error(e, e);
		}
		return ModelFactory.createDefaultModel();
	}

	private void deleteIndividuals(Model model, VitroRequest vreq) {
		RDFService rdfService = vreq.getRDFService();
		ChangeSet cs = makeChangeSet(rdfService);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			model.write(out, "N3");
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			cs.addRemoval(in, RDFServiceUtils.getSerializationFormatFromJenaString("N3"), ModelNames.ABOX_ASSERTIONS);
			rdfService.changeSetUpdate(cs);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			model.write(sw, "N3");
			log.error("Got " + e.getClass().getSimpleName() + " while removing\n" + sw.toString());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private ChangeSet makeChangeSet(RDFService rdfService) {
		ChangeSet cs = rdfService.manufactureChangeSet();
		cs.addPreChangeEvent(new BulkUpdateEvent(null, BEGIN));
		cs.addPostChangeEvent(new BulkUpdateEvent(null, END));
		return cs;
	}

}
