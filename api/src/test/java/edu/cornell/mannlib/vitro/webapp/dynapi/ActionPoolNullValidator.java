package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.validator.ModelValidator;
import edu.cornell.mannlib.vitro.webapp.dynapi.validator.NullValidator;
import org.apache.jena.rdf.model.Model;

public class ActionPoolNullValidator extends ActionPool{

    private static ActionPoolNullValidator INSTANCE = new ActionPoolNullValidator();

    public static ActionPoolNullValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ModelValidator getValidator(Model data, Model scheme) {
        return NullValidator.getInstance();
    }
}
