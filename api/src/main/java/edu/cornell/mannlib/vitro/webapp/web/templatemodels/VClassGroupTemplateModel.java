/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;

public class VClassGroupTemplateModel extends BaseTemplateModel {

	private static final Log log = LogFactory.getLog(VClassGroupTemplateModel.class.getName());
	
    private final VClassGroup vClassGroup;
    private List<VClassTemplateModel> classes;
    
    public VClassGroupTemplateModel(VClassGroup vClassGroup) {
        this.vClassGroup = vClassGroup;
    }

    public int getDisplayRank() {
        return vClassGroup.getDisplayRank();
    }
    
    public String getUri() {
    	return vClassGroup.getURI();
    }
    
    public String getNamespace() {
    	return vClassGroup.getNamespace();
    }
    
    public String getLocalName() {
    	return vClassGroup.getLocalName();
    }

    public String getPublicName() {
    	return vClassGroup.getPublicName();
    }
    
    // Protect the template against a group without a name.
    public String getDisplayName() {
        String displayName = getPublicName();
        if (StringUtils.isBlank(displayName)) {
            displayName = getLocalName().replaceFirst("vitroClassGroup", "");
        }
        return displayName;
    }
    
    public List<VClassTemplateModel> getClasses() {
        if (classes == null) {
            List<VClass> classList = vClassGroup.getVitroClassList();
            classes = new ArrayList<VClassTemplateModel>();
            for (VClass vc : classList) {
                classes.add(new VClassTemplateModel(vc));
            }
        }
        
        return classes;
    }
    
    public int getIndividualCount(){
        if( vClassGroup.isIndividualCountSet() )
            return vClassGroup.getIndividualCount();
        else
            return 0;
    }
    
    public boolean isIndividualCountSet(){
        return vClassGroup.isIndividualCountSet();
    }    
    
}
