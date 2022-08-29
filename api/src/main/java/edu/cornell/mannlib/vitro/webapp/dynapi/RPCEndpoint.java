package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath;

@WebServlet(name = "RPCEndpoint", urlPatterns = { RPC_SERVLET_PATH + "/*" })
public class RPCEndpoint extends VitroHttpServlet {

    private static final Log log = LogFactory.getLog(RESTEndpoint.class);

    private ActionPool actionPool = ActionPool.getInstance();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        OperationResult.methodNotAllowed().prepareResponse(response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        ApiRequestPath requestPath = ApiRequestPath.from(request);
        if (requestPath.isValid()) {
            if (log.isDebugEnabled()) {
                actionPool.printKeys();
            }
            Action action = actionPool.get(requestPath.getActionName());
            DataStore dataStore = new DataStore();
            if (requestPath.isResourceRequest()) {
                dataStore.setResourceID(requestPath.getResourceId());
            }
            try {
            	Converter.convert(request, action, dataStore);
            } catch (Exception e) {
            	log.error(e,e);
            	response.setStatus(500);
            	return;
            }

            try {
                OperationResult result = action.run(dataStore);
                Converter.convert(response, action, result, dataStore);
            } catch (ConversionException e) {
            	log.error(e,e);
            	response.setStatus(500);
            	return;
            } finally {
                action.removeClient();
            }
        } else {
            OperationResult.notFound().prepareResponse(response);
        }
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
