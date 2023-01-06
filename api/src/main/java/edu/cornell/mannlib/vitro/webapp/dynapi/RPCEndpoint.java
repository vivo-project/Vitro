package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;

import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath;

@WebServlet(name = "RPCEndpoint", urlPatterns = { RPC_SERVLET_PATH + "/*" })
public class RPCEndpoint extends VitroHttpServlet {

    private static final Log log = LogFactory.getLog(RPCEndpoint.class);

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
			UserAccount user = (UserAccount) request.getSession(false).getAttribute("user");
	        if (!action.hasPermissions(user)) {
	        	OperationResult.notAuthorized().prepareResponse(response);
                action.removeClient();
	        	return;
	        } 
            DataStore dataStore = new DataStore();
            if (requestPath.isResourceRequest()) {
                dataStore.setResourceID(requestPath.getResourceId());
            }
            try {
                Map<String, ProcedureDescriptor> dependencies = action.getDependencies();
                Action defaultInstance = actionPool.getDefault();
                for (String uri : dependencies.keySet()) {
                    Action dependency = actionPool.getByUri(uri);
                    if (defaultInstance.equals(dependency)) {
                        throw new InitializationException(
                                Action.class.getSimpleName() + " dependency with uri:'" + uri + "' not found in pool.");
                    }
                    dataStore.putDependency(uri, dependency);
                }
            } catch (InitializationException e) {
                log.error(e, e);
                dataStore.removeDependencies();
                action.removeClient();
                response.setStatus(500);
                return;
            }
            try {
            	Converter.convert(request, action, dataStore);
            } catch (Exception e) {
            	log.error(e,e);
                dataStore.removeDependencies();
                action.removeClient();
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
                dataStore.removeDependencies();
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
