/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.steps;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ErrorStep implements Step {

    OperationResult result = null;

    @Override
    public OperationResult run(DataStore dataStore) {
        return result;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#errorCode", minOccurs = 1, maxOccurs = 1)
    public void setErrorCode(int input) throws InitializationException {
        if (!OperationResult.hasError(input)) {
            throw new InitializationException(String.format(
                    "Code '%s' is not an error. Error range is between 400 and 599", input));
        }
        result = new OperationResult(input);
    }

    @Override
    public void dereference() {
    }

    @Override
    public Set<StepInfo> getNextNodes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public Parameters getInputParams() {
        return new Parameters();
    }

    @Override
    public Parameters getOutputParams() {
        return new Parameters();
    }

    @Override
    public Map<String, ProcedureDescriptor> getDependencies() {
        return Collections.emptyMap();
    }

    public boolean isValid() {
        return result != null;
    }

}
