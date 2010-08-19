/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.listener;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;

public interface EditPreProcessor {

    public void process(Object o, EditProcessObject epo);

}
