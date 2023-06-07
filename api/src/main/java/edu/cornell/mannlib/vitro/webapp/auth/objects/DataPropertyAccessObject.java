package edu.cornell.mannlib.vitro.webapp.auth.objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxDataPropertyWrapper;

public class DataPropertyAccessObject extends AccessObject {

    private static final Log log = LogFactory.getLog(DataPropertyAccessObject.class);

    public DataPropertyAccessObject(DataProperty dataProperty) {
        setDataProperty(dataProperty);
        debug(dataProperty);
    }
    
    @Override
    public String getUri() {
        DataProperty dp = getDataProperty();
        if (dp != null) {
            return dp.getURI();
        }
        return null;
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.DATA_PROPERTY;
    }
    
    @Override
    public String toString() {
        ObjectProperty op = getObjectProperty();
        return getClass().getSimpleName() + ": " + (op == null ? op : op.getURI());
    }
    
    private void debug(DataProperty dataProperty) {
        if (true) {
            if (dataProperty instanceof FauxDataPropertyWrapper) {
                Throwable t = new Throwable();
                log.error("FauxDataPropertyWrapper provided in DataPropertyAccessObject constructor");
                log.error(t, t);
            }
        }
    }
}