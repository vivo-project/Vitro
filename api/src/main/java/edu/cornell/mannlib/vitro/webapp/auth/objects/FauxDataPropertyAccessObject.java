/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import java.util.Optional;

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

    @Override
    public Optional<String> getUri() {
        Optional<DataProperty> dp = getDataProperty();
        if (!dp.isPresent()) {
            return Optional.empty();
        }
        DataProperty property = dp.get();
        if (property instanceof FauxDataPropertyWrapper) {
            String uri = ((FauxDataPropertyWrapper) property).getConfigUri();
            if (uri == null) {
                return Optional.empty();
            }
            return Optional.of(uri);
        }
        String uri = property.getURI();
        if (uri == null) {
            return Optional.empty();
        }
        return Optional.of(uri);
    }

    @Override
    public String toString() {
        Optional<DataProperty> dp = getDataProperty();
        DataProperty property = dp.get();
        if (property instanceof FauxDataPropertyWrapper) {
            return getClass().getSimpleName() + ": " + ((FauxDataPropertyWrapper) property).getConfigUri();
        }
        return getClass().getSimpleName() + ": " + (!dp.isPresent() ? "not present" : dp.get().getURI());
    }
}
