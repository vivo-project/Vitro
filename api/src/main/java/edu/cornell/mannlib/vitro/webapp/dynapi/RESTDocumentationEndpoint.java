package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.DocsRequestPath.REST_DOCS_SERVLET_PATH;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.DocsRequestPath;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

// /docs/rest/{version}
// /docs/rest/{version}/{resource}
@WebServlet(name = "RESTDocumentationEndpoint", urlPatterns = { REST_DOCS_SERVLET_PATH + "/*" })
public class RESTDocumentationEndpoint extends VitroHttpServlet {

	private static final Log log = LogFactory.getLog(RESTDocumentationEndpoint.class);

	private final DynamicAPIDocumentation dynamicAPIDocumentation;

	public RESTDocumentationEndpoint() {
		this.dynamicAPIDocumentation = DynamicAPIDocumentation.getInstance();
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getMethod().equalsIgnoreCase("PATCH")) {
			doPatch(request, response);
		} else {
			super.service(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.methodNotAllowed().prepareResponse(response);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.methodNotAllowed().prepareResponse(response);
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.methodNotAllowed().prepareResponse(response);
	}

	public void doPatch(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.methodNotAllowed().prepareResponse(response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) {
		try {
			OpenAPI openApi = dynamicAPIDocumentation.generate(DocsRequestPath.from(request));

			try {
				String swaggerJson = Json.mapper().writeValueAsString(openApi);

				System.out.println("\n\n" + swaggerJson + "\n\n");

			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			JsonNode node = Yaml.mapper().convertValue(openApi, JsonNode.class);

			String serializedYaml = Yaml.pretty(node);

			System.out.println("\n\n" + serializedYaml + "\n\n");
		} catch (ConfigurationBeanLoaderException e) {
			e.printStackTrace();
		}
	}

}
