package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.Validator;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;

public class Validators implements Removable {

    private static final Log log = LogFactory.getLog(Validators.class);

    List<Validator> validators = new LinkedList<Validator>();

    public void add(Validator validator) {
        validators.add(validator);
    }

    public boolean isAllValid(String name, Data data) {
        for (Validator validator : validators) {
            if (!validator.isValid(name, data)) {
                log.error("Parameter " + name + " is invalid. Validator " + validator.getClass().getSimpleName());
                return false;
            }
        }

        return true;
    }

    @Override
    public void dereference() {
        for (Validator validator : validators) {
            validator.dereference();
        }
        validators = null;
    }
}
