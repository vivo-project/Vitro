package edu.cornell.mannlib.vitro.webapp.template.velocity;

import org.apache.velocity.app.event.implement.ReportInvalidReferences;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;

public class VitroInvalidReferenceEventHandler extends ReportInvalidReferences {

    @Override
    public Object invalidGetMethod(Context context, String reference,
            Object object, String property, Info info) {
        // TODO Auto-generated method stub
        super.invalidGetMethod(context, reference, object, property, info);
        System.out.println("IN invalidGetMethod");
        return "";
    }

    @Override
    public Object invalidMethod(Context context, String reference,
            Object object, String method, Info info) {
        // TODO Auto-generated method stub
        System.out.println("IN invalidMethod");
        return null;
    }

    @Override
    public boolean invalidSetMethod(Context context, String leftreference,
            String rightreference, Info info) {
        // TODO Auto-generated method stub
        System.out.println("IN invalidSetMethod");
        return false;
    }

}
