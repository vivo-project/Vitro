package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.Endpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;

public class ProcedureDescriptorCall {

    public static void execute(ProcedureDescriptor procedureDescriptor, DataStore dataStore) throws ConversionException,
            InitializationException {
        if (procedureDescriptor.hasUriParam()) {
            unsafeCall(procedureDescriptor, dataStore);
        } else {
            safeCall(procedureDescriptor, dataStore);
        }
    }

    private static void safeCall(ProcedureDescriptor procedureDescriptor, DataStore dataStore)
            throws ConversionException {
        Parameters inputParams = procedureDescriptor.getInputParams();
        String uri = procedureDescriptor.getUri();
        Procedure procedure = dataStore.getDependency(uri);
        execute(procedureDescriptor, dataStore, uri, procedure, inputParams);
    }

    private static void unsafeCall(ProcedureDescriptor procedureDescriptor, DataStore dataStore)
            throws ConversionException, InitializationException {
        Parameters inputParams = new Parameters();
        inputParams.addAll(procedureDescriptor.getInputParams());
        String uri = procedureDescriptor.getUri(dataStore);
        inputParams.remove(procedureDescriptor.getUriParam());
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        try (Procedure procedure = procedurePool.getByUri(uri);) {
            Endpoint.collectDependencies(procedure, dataStore, procedurePool);
            execute(procedureDescriptor, dataStore, uri, procedure, inputParams);
        } finally {
            dataStore.removeDependencies();
        }
    }

    private static void execute(ProcedureDescriptor procedureDescriptor, DataStore dataStore, String uri,
            Procedure procedure, Parameters inputParams) throws ConversionException {
        DataStore localStore = new DataStore();
        Parameters internalParams = procedure.getInternalParams();
        initilaizeLocalStore(dataStore, localStore, inputParams, internalParams);
        if (!procedure.hasPermissions(localStore.getUser())) {
            throw new RuntimeException(formatNotAuthorizedErrorMessage(procedureDescriptor, uri));
        }
        OperationResult result = procedure.run(localStore);
        if (result.hasError()) {
            throw new RuntimeException(formatErrorMessage(procedureDescriptor, uri));
        }
        copyData(localStore, dataStore, procedureDescriptor.getOutputParams());
    }

    protected static String formatNotAuthorizedErrorMessage(ProcedureDescriptor procedureDescriptor, String uri) {
        return String.format("User not authorized to access procedure %s, defined by descriptor %s", uri,
                procedureDescriptor.toString());
    }

    protected static String formatErrorMessage(ProcedureDescriptor procedureDescriptor, String uri) {
        return String.format("Procedure '%s' described by descriptor '%s' returned error", uri, procedureDescriptor
                .toString());
    }

    public static void initilaizeLocalStore(DataStore externalStore, DataStore localStore, Parameters paramsToCopy,
            Parameters localParams) throws ConversionException {
        copyData(externalStore, localStore, paramsToCopy);
        localStore.putDependencies(externalStore.getDependencies());
        localStore.setUser(externalStore.getUser());
        Converter.convertInternalParams(localParams, localStore);
    }

    public static void copyData(DataStore sourceStore, DataStore destinationStore, Parameters params) {
        for (String name : params.getNames()) {
            Data data = sourceStore.getData(name);
            if (data == null) {
                throw new RuntimeException(String.format("Data '%s' not provided", name));
            }
            destinationStore.addData(name, data);
        }
    }
}
