package edu.cornell.mannlib.vitro.webapp.dynapi.components.types;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ObjectParameterType extends ParameterType {

    private Parameters internalElements = new Parameters();

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasInternalElement")
    public void addInternalElement(Parameter param) {
        internalElements.add(param);
    }

    public Parameters getInternalElements() {
        return internalElements;
    }
    @Override
    public String computePrefix(String fieldName) {
        String retVal = "";
        boolean exist = false;
        for (String internalFieldName : this.getInternalElements().getNames()) {
            Parameter internalParameter = this.getInternalElements().get(internalFieldName);
            String prefix = internalParameter.computePrefix(fieldName);
            if (prefix != null) {
                retVal += prefix;
                exist = true;
                break;
            }
        }
        if (!exist) {
            retVal = null;
        }

        return (retVal != null && retVal.length() > 0) ? retVal + "." : retVal;
    }

}
