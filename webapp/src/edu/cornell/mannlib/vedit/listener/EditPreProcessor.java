package edu.cornell.mannlib.vedit.listener;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vedit.beans.EditProcessObject;

public interface EditPreProcessor {

    public void process(Object o, EditProcessObject epo);

}
