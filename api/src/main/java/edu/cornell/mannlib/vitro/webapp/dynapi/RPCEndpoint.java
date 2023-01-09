package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath;

@WebServlet(name = "RPCEndpoint", urlPatterns = { RPC_SERVLET_PATH + "/*" })
public class RPCEndpoint extends Endpoint {

    private static final Log log = LogFactory.getLog(RPCEndpoint.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        OperationResult.methodNotAllowed().prepareResponse(response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        ApiRequestPath requestPath = ApiRequestPath.from(request);
        if (!requestPath.isValid()) {
            OperationResult.notFound().prepareResponse(response);
            return;
        }
        String actionName = requestPath.getActionName();
        processActionRequest(request, response, requestPath, actionName);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        OperationResult.methodNotAllowed().prepareResponse(response);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        OperationResult.methodNotAllowed().prepareResponse(response);
    }

}
