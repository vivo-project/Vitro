package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath;

public abstract class Endpoint extends VitroHttpServlet {

    private ProcedurePool procedurePool = ProcedurePool.getInstance();
    public static final Log log = LogFactory.getLog(Endpoint.class);

    public void processRequest(HttpServletRequest request, HttpServletResponse response,
            ApiRequestPath requestPath, String procedureUri) {
        if (log.isDebugEnabled()) {
            procedurePool.printKeys();
        }
        Procedure procedure = procedurePool.get(procedureUri);
        UserAccount user = getUser(request);
        if (!procedure.hasPermissions(user)) {
            procedure.removeClient();
            OperationResult.notAuthorized().prepareResponse(response);
            return;
        }
        DataStore dataStore = new DataStore();
        dataStore.setUser(user);
        if (requestPath.isResourceRequest()) {
            dataStore.setResourceID(requestPath.getResourceId());
        }
        try {
            collectDependencies(procedure, dataStore, procedurePool);
            Converter.convertFromRequest(request, procedure, dataStore);
        } catch (Exception e) {
            log.error(e, e);
            dataStore.removeDependencies();
            procedure.removeClient();
            OperationResult.internalServerError().prepareResponse(response);
            return;
        }
        OperationResult result = procedure.run(dataStore);
        if (!result.hasSuccess()) {
            dataStore.removeDependencies();
            procedure.removeClient();
            result.prepareResponse(response);
            return;
        }
        try {
            Converter.convertToResponse(response, procedure, dataStore);
        } catch (ConversionException e) {
            log.error(e, e);
            OperationResult.internalServerError().prepareResponse(response);
            return;
        } finally {
            dataStore.removeDependencies();
            procedure.removeClient();
        }
        OperationResult.ok().prepareResponse(response);
        return;
    }

    private UserAccount getUser(HttpServletRequest request) {
        UserAccount user = (UserAccount) request.getSession(false).getAttribute("user");
        if (user != null) {
            return user;
        }
        user = LoginStatusBean.getCurrentUser(request);
        return user;
    }

    public static void collectDependencies(Procedure procedure, DataStore dataStore, ProcedurePool procedurePool) throws InitializationException {
        Map<String, ProcedureDescriptor> dependencies = procedure.getDependencies();
        for (String uri : dependencies.keySet()) {
            if (dataStore.containsDependency(uri)) {
                continue;
            }
            ProcedureDescriptor descriptor = dependencies.get(uri);
            if (descriptor.getUriParam() != null) {
                continue;
            }
            Procedure dependency = procedurePool.getByUri(uri);
            if (NullProcedure.getInstance().equals(dependency)) {
                throw new InitializationException(String.format("%s dependency with uri:'%s' not found in pool.",
                        Procedure.class.getSimpleName(), uri));
            }
            validateDependency(descriptor, dependency);
            dataStore.putDependency(uri, dependency);
            collectDependencies(dependency, dataStore, procedurePool);
        }
    }

    private static void validateDependency(ProcedureDescriptor descriptor, Procedure procedure) throws InitializationException {
        validateInputParameters(descriptor, procedure);
        validateOutputParameters(descriptor, procedure);
    }

    private static void validateInputParameters(ProcedureDescriptor descriptor, Procedure procedure)
            throws InitializationException {
        Parameters providedInput = descriptor.getInputParams();
        Parameters requiredInput = procedure.getInputParams();
        String errorMessage = "Input parameter with name %s required by procedure with uri:'" + procedure.getUri() + "' is not provided by descriptor.";

        validateParameters(providedInput, requiredInput, errorMessage);
    }

    private static void validateOutputParameters(ProcedureDescriptor descriptor, Procedure procedure)
            throws InitializationException {
        Parameters providedOutput = procedure.getOutputParams();
        Parameters requiredOutput = descriptor.getOutputParams();
        String errorMessage = "Output parameter with name '%s' required by descriptor is not provided by procedure with uri:'" + procedure.getUri() + "'.";

        validateParameters(providedOutput, requiredOutput, errorMessage);
    }

    private static void validateParameters(Parameters provided, Parameters required, String errorMessage)
            throws InitializationException {
        for (String paramName : required.getNames()) {
            Parameter param = required.get(paramName);
            if (!provided.contains(param)) {
                throw new InitializationException(String.format(errorMessage, paramName));
            }
        }
    }
    
}
