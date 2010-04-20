package edu.cornell.mannlib.vitro.webapp.template.velocity;

import org.apache.velocity.app.event.NullSetEventHandler;

public class VitroNullSetEventHandler implements NullSetEventHandler{

    @Override
    public boolean shouldLogOnNullSet(String lhs, String rhs) {
        // TODO Auto-generated method stub
        System.out.println("IN shouldLogOnNullSet");
        return true;
    }

}
