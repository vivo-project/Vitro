package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;

public class DummyPropertyGroupTemplateModel extends
        PropertyGroupTemplateModel {
    
    private String name;

    DummyPropertyGroupTemplateModel(String name) {
        super(null);  
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

}
