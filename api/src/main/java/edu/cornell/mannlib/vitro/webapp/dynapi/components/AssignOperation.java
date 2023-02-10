package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class AssignOperation extends AbstractOperation {

    private static final Log log = LogFactory.getLog(AssignOperation.class);

    private Parameters inputParams = new Parameters();
    private Parameters outputParams = new Parameters();

    private Parameter targetParam;
    private Parameter assignableParam;
    private String key;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#assignableParameter", minOccurs = 1, maxOccurs = 1)
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
        assignableParam = param;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#targetParameter", minOccurs = 1, maxOccurs = 1)
    public void addOutputParameter(Parameter param) {
        outputParams.add(param);
        targetParam = param;
    }

    @Override
    public OperationResult run(DataStore dataStore) {
        if (!isValid(dataStore)) {
            return OperationResult.internalServerError();
        }
        Data targetData;
        if (dataStore.contains(targetParam.getName())) {
            targetData = dataStore.getData(targetParam.getName());    
        } else {
            targetData = new Data(targetParam);
        }
        Data assignableData = dataStore.getData(assignableParam.getName());
        targetData.copyObject(assignableData);
        if (!dataStore.contains(targetParam.getName())){
            dataStore.addData(targetParam.getName(), targetData);
        }
        return OperationResult.ok();
    }

    private String getAssignableParamName() {
        return assignableParam.getName();
    }

    @Override
    public void dereference() {
    }

    @Override
    public Parameters getInputParams() {
        return inputParams;
    }

    @Override
    public Parameters getOutputParams() {
        return outputParams;
    }

    public boolean isValid() {
        if (assignableParam == null) {
            log.error("assignable parameter is not set");
            return false;
        }
        if (targetParam == null) {
            log.error("target parameter  is not set");
            return false;
        }
        ParameterType assignableType = assignableParam.getType();
        ParameterType targetType = targetParam.getType();
        if (!assignableType.equals(targetType)) {
            log.error(String.format("assignable '%s' and target '%s' parameters are not compatible",
                    assignableParam.getName(), targetParam.getName()));
            return false;
        }

        return true;
    }

    public boolean isValid(DataStore dataStore) {
        if (!isValid()) {
            return false;
        }
        if (dataStore == null) {
            log.error("data store is null");
            return false;
        }
        

        Data assignableData = dataStore.getData(getAssignableParamName());
        if (assignableData == null) {
            log.error("assignable data is not provided in data store");
            return false;
        }
        if (assignableData.getParam() == null) {
            log.error("assignable data param is null");
            return false;
        }
        return true;
    }
}
