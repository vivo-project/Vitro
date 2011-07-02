/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class EditConfigurationTemplateModel extends BaseTemplateModel {
    EditConfigurationVTwo editConfig;
    VitroRequest vreq;
    
    public EditConfigurationTemplateModel( EditConfigurationVTwo editConfig, VitroRequest vreq){
        this.editConfig = editConfig;
        this.vreq = vreq;
    }
    
    public String getEditKey(){
        return editConfig.getEditKey();
    }
    
    public boolean isUpdate(){
        return editConfig.isUpdate();
    }
    
    public String getSubmitToUrl(){
        return  getUrl( editConfig.getSubmitToUrl() );
    }
}
