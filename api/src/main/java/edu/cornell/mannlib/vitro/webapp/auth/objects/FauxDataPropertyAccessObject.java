package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxDataPropertyWrapper;

public class FauxDataPropertyAccessObject extends AccessObject {

    public FauxDataPropertyAccessObject(DataProperty dataProperty) {
        setDataProperty(dataProperty);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.FAUX_DATA_PROPERTY;
    }

    public String getUri() {
        DataProperty dp = getDataProperty();
        if (dp instanceof FauxDataPropertyWrapper) {
            return ((FauxDataPropertyWrapper) dp).getConfigUri();
        }
        if (dp != null) {
            return dp.getURI();
        }
        return null;
    }

    @Override
    public String toString() {
        DataProperty dp = getDataProperty();
        if (dp instanceof FauxDataPropertyWrapper) {
            return getClass().getSimpleName() + ": " + ((FauxDataPropertyWrapper) dp).getConfigUri();
        }
        return getClass().getSimpleName() + ": " + (dp == null ? dp : dp.getURI());
    }
}
