/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.template.velocity;

import org.apache.velocity.app.event.IncludeEventHandler;

public class VitroIncludeEventHandler implements IncludeEventHandler {

    @Override
    public String includeEvent(String includeResourcePath,
            String currentResourcePath, String directiveName) {
        // TODO Auto-generated method stub
        System.out.println("IN includeEvent: including template " + includeResourcePath);
        return null;
    }

}
