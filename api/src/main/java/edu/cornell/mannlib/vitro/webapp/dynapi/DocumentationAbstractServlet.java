package edu.cornell.mannlib.vitro.webapp.dynapi;

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

    private final DynamicAPIDocumentation dynamicAPIDocumentation;

    public DocumentationAbstractServlet() {
        this.dynamicAPIDocumentation = getInstance();
    }

    protected void process(HttpServletRequest request, HttpServletResponse response) {
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

    abstract protected DynamicAPIDocumentation getInstance();

}
