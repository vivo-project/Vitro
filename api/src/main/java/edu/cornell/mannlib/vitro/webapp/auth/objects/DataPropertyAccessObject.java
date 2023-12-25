/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import java.util.Optional;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxDataPropertyWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataPropertyAccessObject extends AccessObject {

    private static final Log log = LogFactory.getLog(DataPropertyAccessObject.class);

    public DataPropertyAccessObject(DataProperty dataProperty) {
        setDataProperty(dataProperty);
        debug(dataProperty);
    }

    @Override
    public Optional<String> getUri() {
        Optional<DataProperty> dp = getDataProperty();
        if (dp.isPresent()) {
            String uri = dp.get().getURI();
            if (uri == null) {
                return Optional.empty();
            }
            return Optional.of(uri);
        }
        return Optional.empty();
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.DATA_PROPERTY;
    }

    @Override
    public String toString() {
        Optional<DataProperty> dp = getDataProperty();
        return getClass().getSimpleName() + ": " + (!dp.isPresent() ? "not present" : dp.get().getURI());
    }

    private void debug(DataProperty dataProperty) {
        if (dataProperty instanceof FauxDataPropertyWrapper) {
            Throwable t = new Throwable();
            log.error("FauxDataPropertyWrapper provided in DataPropertyAccessObject constructor");
            log.error(t, t);
        }
        if (dataProperty == null) {
            Throwable t = new Throwable();
            log.error("null provided in DataPropertyAccessObject constructor");
            log.error(t, t);
        }
    }
}
