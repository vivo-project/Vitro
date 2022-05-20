package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.HAS_DELETE_QUERY;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

@WebServlet(name = "DeleteIndividualController", urlPatterns = "/deleteIndividualController")
public class DeleteIndividualController extends FreemarkerHttpServlet {

	private static final String INDIVIDUAL_URI = "individualUri";
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(DeleteIndividualController.class);
	private static final boolean BEGIN = true;
	private static final boolean END = !BEGIN;

	private static String queryForDeleteQuery = ""
			+ "SELECT ?deleteQueryText WHERE { "
			+ "?associatedUri <" + HAS_DELETE_QUERY + "> ?deleteQueryText ."
			+ "}";

	private static final String DEFAULT_DELETE_QUERY_TEXT = ""
			+ "CONSTRUCT { ?individualUri ?p1 ?o1 . ?s2 ?p2 ?individualUri . } "
			+ "WHERE {"
			+ "  { ?individualUri ?p1 ?o1 . } UNION { ?s2 ?p2 ?individualUri. } "
			+ "}";

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.DO_FRONT_END_EDITING.ACTION;
	}

	protected ResponseValues processRequest(VitroRequest vreq) {
		String errorMessage = handleErrors(vreq);
		if (!errorMessage.isEmpty()) {
			return prepareErrorMessage(errorMessage);
		}
		String individualUri = vreq.getParameter(INDIVIDUAL_URI);
		List<String> types = getObjectMostSpecificTypes(individualUri, vreq);
		Model displayModel = vreq.getDisplayModel();

		String deleteQueryText = getDeleteQueryForTypes(types, displayModel);
		Model toRemove = getIndividualsToDelete(individualUri, deleteQueryText, vreq);
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
		String uri = vreq.getParameter(INDIVIDUAL_URI);
		if (uri == null) {
			return "Individual uri is null. No object to delete.";
		}
		if (uri.contains("<") || uri.contains(">")) {
			return "Individual IRI shouldn't contain '<' or '>";
		}
		return "";
	}

	private static String getDeleteQueryForTypes(List<String> types, Model displayModel) {
		String deleteQueryText = DEFAULT_DELETE_QUERY_TEXT;
		String foundType = "";
		for ( String type: types) {
			Query queryForTypeSpecificDeleteQuery = QueryFactory.create(queryForDeleteQuery);
			QuerySolutionMap initialBindings = new QuerySolutionMap();
			initialBindings.add("associatedURI", ResourceFactory.createResource(type));
			displayModel.enterCriticalSection(Lock.READ);
			try {
				QueryExecution qexec = QueryExecutionFactory.create(queryForTypeSpecificDeleteQuery, displayModel,
						initialBindings);
				try {
					ResultSet results = qexec.execSelect();
					if (results.hasNext()) {
						QuerySolution solution = results.nextSolution();
						deleteQueryText = solution.get("deleteQueryText").toString();
						foundType = type;
					}
				} finally {
					qexec.close();
				}
			} finally {
				displayModel.leaveCriticalSection();
			}
			if (!foundType.isEmpty()) {
				break;
			}
		}
		
		if (!foundType.isEmpty()) {
			log.debug("For " + foundType + " found delete query \n" + deleteQueryText);
			if (!deleteQueryText.contains(INDIVIDUAL_URI)){
				log.error("Safety check failed. Delete query text should contain " + INDIVIDUAL_URI + ", "
						+ "but it didn't. To prevent bad consequences query was rejected.");
				log.error("Delete query which caused the error: \n" + deleteQueryText);
				deleteQueryText = DEFAULT_DELETE_QUERY_TEXT;
			}
		} else {
			log.debug("For most specific types: " + types.stream().collect(Collectors.joining(",")) + " no delete query was found. Using default query \n" + deleteQueryText);
		}
		return deleteQueryText;
	}

	private List<String> getObjectMostSpecificTypes(String individualUri, VitroRequest vreq) {
		List<String> types = new LinkedList<String>();
			Individual individual = vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI(individualUri);
			if (individual != null) {
				types = individual.getMostSpecificTypeURIs();
			}
		if (types.isEmpty()) {
			log.error("Failed to get most specific type for individual Uri " + individualUri);
		}
		return types;
	}

	private Model getIndividualsToDelete(String targetIndividual, String deleteQuery, VitroRequest vreq) {
		try {
			Query queryForTypeSpecificDeleteQuery = QueryFactory.create(deleteQuery);
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(INDIVIDUAL_URI, ResourceFactory.createResource(targetIndividual));
			Model ontModel = ModelAccess.on(vreq).getOntModelSelector().getABoxModel();
			QueryExecution qexec = QueryExecutionFactory.create(queryForTypeSpecificDeleteQuery, ontModel, bindings);
			Model results = qexec.execConstruct();
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
			log.error(e,e);
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
