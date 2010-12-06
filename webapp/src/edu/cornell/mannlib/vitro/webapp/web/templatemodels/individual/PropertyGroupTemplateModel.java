/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class PropertyGroupTemplateModel extends BaseTemplateModel {
    
    private String name;
    private List<PropertyTemplateModel> properties;
      
    PropertyGroupTemplateModel(WebappDaoFactory wdf, PropertyGroup group, Individual subject) {
        this.name = group.getName();
        
        List<Property> propertyList = group.getPropertyList();
        properties = new ArrayList<PropertyTemplateModel>(propertyList.size());
        for (Property p : propertyList)  {
            if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty)p;
                if (op.getCollateBySubclass()) {
                    properties.add(new CollatedObjectProperty(op));
                }  else {
                    properties.add(new UncollatedObjectProperty(op));
                }
            } else {
                properties.add(new DataPropertyTemplateModel((DataProperty)p, subject, wdf));
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public List<PropertyTemplateModel> getProperties() {
        return properties;
    }

}
