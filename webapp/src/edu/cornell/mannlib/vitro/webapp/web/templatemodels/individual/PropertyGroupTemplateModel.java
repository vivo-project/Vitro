/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class PropertyGroupTemplateModel extends BaseTemplateModel {
    
    protected PropertyGroup group;
    protected List<PropertyTemplateModel> properties;
    
    PropertyGroupTemplateModel() { }
    
    PropertyGroupTemplateModel(PropertyGroup group) {
        this.group = group;
    }
    
    public String getName() {
        return group.getName();
    }
    
    public List<PropertyTemplateModel> getProperties() {
        return properties;
    }

}
