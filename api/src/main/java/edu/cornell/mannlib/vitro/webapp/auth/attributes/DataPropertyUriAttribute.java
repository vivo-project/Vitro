package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;

public class DataPropertyUriAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(DataPropertyUriAttribute.class);

    public DataPropertyUriAttribute(String uri, String objectUri) {
        super(uri, objectUri);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        DataProperty dataProperty = ao.getDataProperty();
        String inputValue = dataProperty.getURI();
        if (dataProperty != null) {
            inputValue = dataProperty.getURI();
        }
        if (AttributeValueTester.test(this, ar, inputValue)) {
            log.debug("Attribute value(s) match requested statement data property uri '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute value(s) don't match requested statement data property uri '" + inputValue + "'");
        return false;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.OBJECT_PROPERTY_URI;
    }

}
