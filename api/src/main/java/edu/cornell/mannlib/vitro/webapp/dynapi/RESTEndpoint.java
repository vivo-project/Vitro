package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.REST_SERVLET_PATH;
import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath;

@WebServlet(name = "RESTEndpoint", urlPatterns = { REST_SERVLET_PATH + "/*" })
public class RESTEndpoint extends Endpoint {

    private static final Log log = LogFactory.getLog(RESTEndpoint.class);

    private ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();

    public final static String RESOURCE_ID = "resource_id";

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
        process(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        process(request, response);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        process(request, response);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        process(request, response);
    }

    public void doPatch(HttpServletRequest request, HttpServletResponse response) {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) {
        String method = request.getMethod();
        ApiRequestPath requestPath = ApiRequestPath.from(request);

        if (!requestPath.isValid()) {
            log.error(format("Request path %s is not found", request.getPathInfo()));
            OperationResult.notFound().prepareResponse(response);
            return;
        }

        if (!requestPath.isMethodAllowed(method)) {
            log.error(format("Method %s not allowed at path %s", method, request.getPathInfo()));
            OperationResult.methodNotAllowed().prepareResponse(response);
            return;
        }

        ResourceAPIKey resourceAPIKey = ResourceAPIKey.of(requestPath.getResourceName(),
                requestPath.getResourceVersion());

        if (log.isDebugEnabled()) {
            resourceAPIPool.printKeys();
        }
        ResourceAPI resourceAPI = resourceAPIPool.get(resourceAPIKey);

        if (NullResourceAPI.getInstance().equals(resourceAPI)) {
            log.error(format("ResourceAPI %s not found", resourceAPIKey));
            OperationResult.notFound().prepareResponse(response);
            return;
        }

        ResourceAPIKey key = resourceAPI.getKey();

        String procedureUri = null;

        if (requestPath.isCustomRestAction()) {
            String actionName = requestPath.getCustomRestActionName();
            try {
                procedureUri = resourceAPI.getProcedureUriByActionName(method, actionName);
            } catch (UnsupportedOperationException e) {
                log.error(format("Custom REST action %s not implemented for resource %s and method %s", actionName, key, method), e);
                OperationResult.methodNotAllowed().prepareResponse(response);
                return;
            } finally {
                resourceAPI.removeClient();
            }
        } else {
            try {
                procedureUri = resourceAPI.getProcedureUri(method, requestPath.isResourceRequest());
            } catch (UnsupportedOperationException e) {
                log.error(format("Method %s not implemented for resource %s", method, key), e);
                OperationResult.methodNotAllowed().prepareResponse(response);
                return;
            } finally {
                resourceAPI.removeClient();
            }
        }
        processRequest(request, response, requestPath, procedureUri);
    }

}
