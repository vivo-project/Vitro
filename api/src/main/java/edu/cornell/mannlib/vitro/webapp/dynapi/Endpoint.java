package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;
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

    private ProcedurePool procedurePool = ProcedurePool.getInstance();
    public static final Log log = LogFactory.getLog(Endpoint.class);

    public void processRequest(HttpServletRequest request, HttpServletResponse response,
            ApiRequestPath requestPath, String procedureUri) {
        if (log.isDebugEnabled()) {
            procedurePool.printKeys();
        }
        Procedure procedure = procedurePool.get(procedureUri);
        UserAccount user = (UserAccount) request.getSession(false).getAttribute("user");
        if (!procedure.hasPermissions(user)) {
            procedure.removeClient();
            OperationResult.notAuthorized().prepareResponse(response);
            return;
        }
        DataStore dataStore = new DataStore();
        try {
            getDependencies(procedure, dataStore, procedurePool);
            Converter.convert(request, procedure, dataStore);
        } catch (Exception e) {
            log.error(e, e);
            dataStore.removeDependencies();
            procedure.removeClient();
            OperationResult.internalServerError().prepareResponse(response);
            return;
        }
        if (requestPath.isResourceRequest()) {
            dataStore.setResourceID(requestPath.getResourceId());
        }
        OperationResult result = procedure.run(dataStore);
        if (!result.hasSuccess()) {
            dataStore.removeDependencies();
            procedure.removeClient();
            result.prepareResponse(response);
            return;
        }
        try {
            Converter.convert(response, procedure, dataStore);
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

    public static void getDependencies(Procedure procedure, DataStore dataStore, ProcedurePool procedurePool) throws InitializationException {
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
            getDependencies(dependency, dataStore, procedurePool);
        }
    }

    private static void validateDependency(ProcedureDescriptor descriptor, Procedure procedure) throws InitializationException {
        checkInputParameters(descriptor, procedure);
        checkOutputParameters(descriptor, procedure);
    }

    private static void checkInputParameters(ProcedureDescriptor descriptor, Procedure procedure)
            throws InitializationException {
        Parameters providedInput = descriptor.getInputParams();
        Parameters requiredInput = procedure.getInputParams();
        for (String paramName : requiredInput.getNames()) {
            Parameter param = requiredInput.get(paramName);
            if (!providedInput.contains(param)) {
                throw new InitializationException(String.format(
                        "Input parameter with name %s required by procedure with uri:'%s' is not provided by descriptor.",
                        paramName, procedure.getUri()));
            }
            
        }
    }

    private static void checkOutputParameters(ProcedureDescriptor descriptor, Procedure procedure)
            throws InitializationException {
        Parameters providedOutput = procedure.getOutputParams();
        Parameters requiredOutput = descriptor.getOutputParams();
        for (String paramName : requiredOutput.getNames()) {
            Parameter param = requiredOutput.get(paramName);
            //TODO:contains parameter with the same name, same parameter type, rdf type, impl type, serial/deserial types.
            if (!providedOutput.contains(param)) {
                throw new InitializationException(String.format(
                        "Output parameter with name '%s' required by descriptor is not provided by procedure with uri:'%s'.",
                        paramName, procedure.getUri()));
            }
            
        }
    }
}
