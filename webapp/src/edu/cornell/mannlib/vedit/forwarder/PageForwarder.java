/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.forwarder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.cornell.mannlib.vedit.beans.EditProcessObject;

public interface PageForwarder {

    public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo);
}
