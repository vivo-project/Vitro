package edu.cornell.mannlib.vitro.webapp.template.velocity;

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
