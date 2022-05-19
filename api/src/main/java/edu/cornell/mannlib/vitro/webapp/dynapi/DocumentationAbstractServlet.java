package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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

                // keep to easily generate expected test api
                // String type = requestPath.getType() == RequestType.REST ? "/rest" : "/rpc";
                // new File("src/test/resources/dynapi/mock/docs/response/rest/1").mkdirs();
                // new File("src/test/resources/dynapi/mock/docs/response/rest/2").mkdirs();
                // new File("src/test/resources/dynapi/mock/docs/response/rest/4").mkdirs();
                // String next = requestPath.getPathInfo();
                // if (requestPath.getPathInfo().equals("")) {
                //     next = type + "/all";
                // } else if (requestPath.getPathInfo().equals("/1")) {
                //     next = type + "/1/all";
                // } else if (requestPath.getPathInfo().equals("/2")) {
                //     next = type + "/2/all";
                // } else if (requestPath.getPathInfo().equals("/4")) {
                //     next = type + "/4/all";
                // }
                // FileWriter fileWriter = new FileWriter("src/test/resources/dynapi/mock/docs/response" + next + ".json");
                // PrintWriter printWriter = new PrintWriter(fileWriter);
                // printWriter.print(content + "\n");
                // printWriter.close();

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

    /**
     * Write OpenAPI specification to a file.
     *
     * @param path location to write file
     * @param content file content
     * @throws IOException
     */
    private void writeApiFile(String path, String content) throws IOException {
        new File(path).getParentFile().mkdirs();
        FileWriter fileWriter = new FileWriter(path);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(content);
        printWriter.close();
    }

}
