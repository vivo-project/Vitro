package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;

public class DataPropertyAccessObject extends AccessObject {

    public DataPropertyAccessObject(DataProperty dataProperty) {
        setDataProperty(dataProperty);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.DATA_PROPERTY;
    }
}