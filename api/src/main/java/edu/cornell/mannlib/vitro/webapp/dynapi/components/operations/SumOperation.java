package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.BigIntegerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.IntegerView;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SumOperation extends AbstractOperation {
    private static final Log log = LogFactory.getLog(SumOperation.class);
    private Parameter outputParam;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter", minOccurs = 1, maxOccurs = 1)
    public void addOutputParameter(Parameter param){
        outputParams.add(param);
        outputParam = param;
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter", minOccurs = 2)
    public void addInputParameter(Parameter param){
        inputParams.add(param);
    }
    
    @Override
    public OperationResult runOperation(DataStore dataStore) {
        BigDecimal result = compute(dataStore);
        createOutput(result,dataStore);
        return OperationResult.ok();
    }

    private void createOutput(BigDecimal result, DataStore dataStore) {
        Data newData = null;
        if (IntegerView.isInteger(outputParam)) {
            newData = IntegerView.createInteger(outputParam, result.intValue());
        } else if (BigIntegerView.isBigInteger(outputParam)) {
            newData = BigIntegerView.createBigInteger(outputParam, result.toBigInteger());
        }
        if (newData == null) {
            throw new RuntimeException("Output parameter type is not supported");
        }
        dataStore.addData(outputParam.getName(), newData);
    }

    private BigDecimal compute(DataStore dataStore) {
        BigDecimal result = new BigDecimal(0);
        for (String name : inputParams.getNames()) {
            Parameter param = inputParams.get(name);
            Data data = dataStore.getData(name);
            if (IntegerView.isInteger(param)) {
                int value = IntegerView.getInteger(data);
                result = result.add(new BigDecimal(value));
            }
            if (BigIntegerView.isBigInteger(param)) {
                BigInteger value = BigIntegerView.getBigInteger(data);
                result = result.add(new BigDecimal(value));
            }
            dataStore.getData(name);
        }
        return result;
    }

    public boolean isValid() {
        if (outputParam == null) {
            log.error("output parameter is not set");
            return false;
        }
        if (inputParams.size() < 2) {
            log.error("Not enough input params defined");
            return false;
        }
        for (String name : inputParams.getNames()) {
            Parameter inputParam = inputParams.get(name);
            if (!IntegerView.isInteger(inputParam) &&
                !BigIntegerView.isBigInteger(inputParam)) {
                log.error(String.format("Input '%s' parameter type is not supported", name));
                return false;
            }
        }
        return true;
    }
}
