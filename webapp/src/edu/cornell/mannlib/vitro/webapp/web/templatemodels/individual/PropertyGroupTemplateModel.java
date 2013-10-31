/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class PropertyGroupTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyGroupTemplateModel.class); 
    
    private final String name;
    private final List<PropertyTemplateModel> properties;



    PropertyGroupTemplateModel(VitroRequest vreq, PropertyGroup group, 
            Individual subject, boolean editing, 
            List<DataProperty> populatedDataPropertyList, 
            List<ObjectProperty> populatedObjectPropertyList) {

        this.name = group.getName();
        
        List<Property> propertyList = group.getPropertyList();
        properties = new ArrayList<PropertyTemplateModel>(propertyList.size());
        for (Property p : propertyList)  {
            if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty) p;
                RequestedAction dop = new DisplayObjectProperty(op);
                if (!PolicyHelper.isAuthorizedForActions(vreq, dop)) {
                    continue;
                }
                ObjectPropertyTemplateModel tm = ObjectPropertyTemplateModel.getObjectPropertyTemplateModel(
                        op, subject, vreq, editing, populatedObjectPropertyList);
                if (!tm.isEmpty() || (editing && !tm.getAddUrl().isEmpty())) {
                    properties.add(tm);                    
                }

            } else {
                properties.add(new DataPropertyTemplateModel((DataProperty)p, subject, vreq, editing, populatedDataPropertyList));
            }
        }
    }

    protected boolean isEmpty() {
        return properties.isEmpty();
    }
    
    protected void remove(PropertyTemplateModel ptm) {
        properties.remove(ptm);
    }
    

    public String toString(){
        String ptmStr ="";
        for( int i=0; i < properties.size() ; i ++ ){
            PropertyTemplateModel ptm = properties.get(i);
            String spacer = "\n  ";
            if( ptm != null )
                ptmStr = ptmStr + spacer + ptm.toString();
        }
        return String.format("\nPropertyGroupTemplateModel %s[%s] ",name, ptmStr );
    }
    
    /* Accessor methods for templates */
    // Add this so it's included in dumps for debugging. The templates will want to display
    // name using getName(String)
    public String getName() {
        return name;
    }
    
    public String getName(String otherGroupName) {
        if (name == null || name.isEmpty()) {
            return otherGroupName;
        } else {
            return name;
        }
    }
    
    public List<PropertyTemplateModel> getProperties() {
        return properties;
    }    
}
