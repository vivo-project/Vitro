/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.template.velocity;

import org.apache.velocity.app.event.NullSetEventHandler;

public class VitroNullSetEventHandler implements NullSetEventHandler{

    @Override
    public boolean shouldLogOnNullSet(String lhs, String rhs) {
        // TODO Auto-generated method stub
        System.out.println("IN shouldLogOnNullSet");
        return true;
    }

}
