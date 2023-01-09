package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.Map;

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

public abstract class Endpoint extends VitroHttpServlet{
    
    private ActionPool actionPool = ActionPool.getInstance();
    public static final Log log = LogFactory.getLog(Endpoint.class);

    public void processActionRequest(HttpServletRequest request, HttpServletResponse response,
            ApiRequestPath requestPath, String actionName) {
        if (log.isDebugEnabled()) {
            actionPool.printKeys();
        }
        
        Action action = actionPool.get(actionName);
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
    }
}
