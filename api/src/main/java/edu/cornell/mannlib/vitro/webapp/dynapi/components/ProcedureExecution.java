package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;

public class ProcedureExecution {


    public static void execute(ProcedureDescriptor procedureDescriptor, DataStore dataStore) throws ConversionException {
        if (procedureDescriptor.hasUriParam()) {
            unsafeExecution(procedureDescriptor, dataStore);
        } else {
            safeExecution(procedureDescriptor, dataStore);
        }
    }

    private static void safeExecution(ProcedureDescriptor procedureDescriptor, DataStore dataStore)
            throws ConversionException {
        Parameters inputParams = procedureDescriptor.getInputParams();
        String uri = procedureDescriptor.getUri();
        Procedure procedure = dataStore.getDependency(uri);
        execute(procedureDescriptor, dataStore, uri, procedure, inputParams);
    }

    private static void unsafeExecution(ProcedureDescriptor procedureDescriptor, DataStore dataStore)
            throws ConversionException {
        Parameters inputParams = new Parameters();
        inputParams.addAll(procedureDescriptor.getInputParams());
        String uri = procedureDescriptor.getUri(dataStore);
        inputParams.remove(procedureDescriptor.getUriParam());
        Procedure procedure = ProcedurePool.getInstance().getByUri(uri);
        execute(procedureDescriptor, dataStore, uri, procedure, inputParams);
    }

    private static void execute(ProcedureDescriptor procedureDescriptor, DataStore dataStore, String uri,
            Procedure procedure, Parameters inputParams) throws ConversionException {
        DataStore localStore = new DataStore();
        Parameters internalParams = procedure.getInternalParams();
        initilaizeLocalStore(dataStore, localStore, inputParams, internalParams);
        OperationResult result = procedure.run(localStore);
        if (result.hasError()) {
            throw new RuntimeException(formatErrorMessage(procedureDescriptor, uri));
        }
        copyDataToStore(localStore, dataStore, procedureDescriptor.getOutputParams());
    }

    protected static String formatErrorMessage(ProcedureDescriptor procedureDescriptor, String uri) {
        return String.format("Procedure '%s' described by descriptor '%s' returned error",
                uri, procedureDescriptor.toString());
    }

    public static void initilaizeLocalStore(DataStore externalStore, DataStore localStore, Parameters paramsToCopy,
            Parameters localParams) throws ConversionException {
        copyDataToStore(externalStore, localStore, paramsToCopy);
        localStore.putDependencies(externalStore.getDependencies());
        Converter.convertInternalParams(localParams, localStore);
    }

    protected static void copyDataToStore(DataStore fromStore, DataStore toStore, Parameters params) {
        for (String name : params.getNames()) {
            Data data = fromStore.getData(name);
            if (data == null) {
                throw new RuntimeException(String.format("Data '%s' not provided", name));
            }
            toStore.addData(name, data);
        }
    }
}
