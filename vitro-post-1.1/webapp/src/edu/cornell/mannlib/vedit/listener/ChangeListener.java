/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.listener;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;

public interface ChangeListener {

    public void doInserted(Object newObj, EditProcessObject epo);

    public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo);

    public void doDeleted(Object oldObj, EditProcessObject epo);

}
