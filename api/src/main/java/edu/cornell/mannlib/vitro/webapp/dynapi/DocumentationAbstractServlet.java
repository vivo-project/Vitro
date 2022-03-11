package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.DocsRequestPath;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

public abstract class DocumentationAbstractServlet extends VitroHttpServlet {

    private final String APPLICATION_JSON = "application/json";
    private final String APPLICATION_YAML = "application/yaml";

    private final DynamicAPIDocumentation dynamicAPIDocumentation;

    public DocumentationAbstractServlet() {
        this.dynamicAPIDocumentation = getInstance();
    }

    protected void process(HttpServletRequest request, HttpServletResponse response) {
        try {
            DocsRequestPath requestPath = DocsRequestPath.from(request);

            if (requestPath.isValid()) {
                OpenAPI openApi = dynamicAPIDocumentation.generate(requestPath);
                String mimeType = determineMimeType(request);
                String content = null;

                if (mimeType.equalsIgnoreCase(APPLICATION_JSON)) {
                    content = Json.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(openApi);
                } else {
                    JsonNode node = Yaml.mapper().convertValue(openApi, JsonNode.class);
                    content = Yaml.pretty(node);
                }

                response.setStatus(200);
                response.setContentType(mimeType);
                response.getWriter().print(content);
                response.getWriter().flush();
                // System.out.println("\n\n" + content + "\n\n");

            } else {
                response.setStatus(400);
            }
        } catch (ConfigurationBeanLoaderException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract protected DynamicAPIDocumentation getInstance();

    /**
     * Determine the mime-type from the ACCEPT HTTP header.
     * 
     * This needs to be improved to handle the priorities (ie: q=0.1).
     * This might also need to be improved to handle wildcards.
     *
     * @param request HTTP Servlet Request.
     */
    private String determineMimeType(HttpServletRequest request) {
        String accept = request.getHeader("Accept");

        if (accept != null) {
            String[] acceptParts = accept.split(";");

            for (String part : acceptParts) {
                if (part.equalsIgnoreCase(APPLICATION_JSON)) {
                    return APPLICATION_JSON;
                }
            }
        }

        return APPLICATION_YAML;
    }

}
