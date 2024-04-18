/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AssignOperation extends AbstractOperation {
    private static final Log log = LogFactory.getLog(AssignOperation.class);
    private Parameter targetParam;
    private Parameter assignableParam;

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
    public OperationResult runOperation(DataStore dataStore) {
        Data targetData;
        if (dataStore.contains(targetParam.getName())) {
            targetData = dataStore.getData(targetParam.getName());
        } else {
            targetData = new Data(targetParam);
        }
        Data assignableData = dataStore.getData(assignableParam.getName());
        targetData.copyObject(assignableData);
        if (!dataStore.contains(targetParam.getName())) {
            dataStore.addData(targetParam.getName(), targetData);
        }
        return OperationResult.ok();
    }

    private String getAssignableParamName() {
        return assignableParam.getName();
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
        Set<Class<?>> assignableInterfaces = assignableParam.getType().getInterfaces();
        Set<Class<?>> targetInterfaces = targetParam.getType().getInterfaces();
        if (!targetInterfaces.containsAll(assignableInterfaces)) {
            log.error(String.format("assignable '%s' and target '%s' parameters are not compatible", assignableParam
                    .getName(), targetParam.getName()));
            return false;
        }
        return true;
    }

    public boolean isInputValid(DataStore dataStore) {
        if (!super.isInputValid(dataStore)) {
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
