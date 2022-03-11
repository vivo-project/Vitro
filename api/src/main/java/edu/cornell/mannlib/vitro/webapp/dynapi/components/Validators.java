package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.Validator;

public class Validators implements Removable {

    private static final Log log = LogFactory.getLog(Validators.class);

    List<Validator> validators = new LinkedList<Validator>();

    public void add(Validator validator) {
        validators.add(validator);
    }

    public boolean isAllValid(String name, String value) {
        for (Validator validator : validators) {
            if (!validator.isValid(name, value)) {
                log.error("Parameter " + name + " is invalid. Validator failed " + validator.getClass().getSimpleName());
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
