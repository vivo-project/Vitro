package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullAction;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath;

public abstract class Endpoint extends VitroHttpServlet {

    private ActionPool actionPool = ActionPool.getInstance();
    public static final Log log = LogFactory.getLog(Endpoint.class);

    public void processActionRequest(HttpServletRequest request, HttpServletResponse response,
            ApiRequestPath requestPath, String actionKey) {
        if (log.isDebugEnabled()) {
            actionPool.printKeys();
        }
        Action action = actionPool.get(actionKey);
        UserAccount user = (UserAccount) request.getSession(false).getAttribute("user");
        if (!action.hasPermissions(user)) {
            action.removeClient();
            OperationResult.notAuthorized().prepareResponse(response);
            return;
        }
        DataStore dataStore = new DataStore();
        try {
            getDependencies(action, dataStore, actionPool);
            Converter.convert(request, action, dataStore);
        } catch (Exception e) {
            log.error(e, e);
            dataStore.removeDependencies();
            action.removeClient();
            OperationResult.internalServerError().prepareResponse(response);
            return;
        }
        if (requestPath.isResourceRequest()) {
            dataStore.setResourceID(requestPath.getResourceId());
        }
        OperationResult result = action.run(dataStore);
        if (!result.hasSuccess()) {
            dataStore.removeDependencies();
            action.removeClient();
            result.prepareResponse(response);
            return;
        }
        try {
            Converter.convert(response, action, dataStore);
        } catch (ConversionException e) {
            log.error(e, e);
            OperationResult.internalServerError().prepareResponse(response);
            return;
        } finally {
            dataStore.removeDependencies();
            action.removeClient();
        }
        OperationResult.ok().prepareResponse(response);
        return;
    }

    public static void getDependencies(Action action, DataStore dataStore, ActionPool actionPool) throws InitializationException {
        Map<String, ProcedureDescriptor> dependencies = action.getDependencies();
        for (String uri : dependencies.keySet()) {
            if (dataStore.containsDependency(uri)) {
                continue;
            }
            ProcedureDescriptor descriptor = dependencies.get(uri);
            Action dependency = actionPool.getByUri(uri);
            if (NullAction.getInstance().equals(dependency)) {
                throw new InitializationException(String.format("%s dependency with uri:'%s' not found in pool.",
                        Action.class.getSimpleName(), uri));
            }
            validateDependency(descriptor, dependency);
            dataStore.putDependency(uri, dependency);
            getDependencies(dependency, dataStore, actionPool);
        }
    }

    private static void validateDependency(ProcedureDescriptor descriptor, Action action) throws InitializationException {
        checkInputParameters(descriptor, action);
        checkOutputParameters(descriptor, action);
    }

    private static void checkInputParameters(ProcedureDescriptor descriptor, Action action)
            throws InitializationException {
        Parameters providedInput = descriptor.getInputParams();
        Parameters requiredInput = action.getInputParams();
        for (String paramName : requiredInput.getNames()) {
            Parameter param = requiredInput.get(paramName);
            if (!providedInput.contains(param)) {
                throw new InitializationException(String.format(
                        "Input parameter with name %s required by procedure with uri:'%s' is not provided by descriptor.",
                        paramName, action.getUri()));
            }
            
        }
    }

    private static void checkOutputParameters(ProcedureDescriptor descriptor, Action action)
            throws InitializationException {
        Parameters providedOutput = action.getOutputParams();
        Parameters requiredOutput = descriptor.getOutputParams();
        for (String paramName : requiredOutput.getNames()) {
            Parameter param = requiredOutput.get(paramName);
            //TODO:contains parameter with the same name, same parameter type, rdf type, impl type, serial/deserial types.
            if (!providedOutput.contains(param)) {
                throw new InitializationException(String.format(
                        "Output parameter with name '%s' required by descriptor is not provided by procedure with uri:'%s'.",
                        paramName, action.getUri()));
            }
            
        }
    }
}
