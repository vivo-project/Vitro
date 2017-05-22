/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vedit.listener;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;

public interface EditPreProcessor {

    public void process(Object o, EditProcessObject epo);

}
