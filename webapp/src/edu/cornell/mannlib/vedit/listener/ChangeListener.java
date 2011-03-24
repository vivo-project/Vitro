package edu.cornell.mannlib.vedit.listener;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vedit.beans.EditProcessObject;

public interface ChangeListener {

    public void doInserted(Object newObj, EditProcessObject epo);

    public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo);

    public void doDeleted(Object oldObj, EditProcessObject epo);

}
