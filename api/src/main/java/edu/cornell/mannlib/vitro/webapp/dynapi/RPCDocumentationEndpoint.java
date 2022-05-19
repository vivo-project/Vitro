package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.DocsRequestPath.RPC_DOCS_SERVLET_PATH;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;

// /docs/rpc
// /docs/rpc/{action}
@WebServlet(name = "RPCDocumentationEndpoint", urlPatterns = { RPC_DOCS_SERVLET_PATH, RPC_DOCS_SERVLET_PATH + "/*" })
public class RPCDocumentationEndpoint extends DocumentationAbstractServlet {

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

    protected DynamicAPIDocumentation getInstance() {
        return DynamicAPIDocumentation.getInstance();
    }

}
