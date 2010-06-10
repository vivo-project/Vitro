/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;

public class VClassGroupView extends ViewObject {

	private static final Log log = LogFactory.getLog(VClassGroupView.class.getName());
	
    private VClassGroup vClassGroup = null;
    private List<VClassView> classes = null;
    
    public VClassGroupView(VClassGroup vClassGroup) {
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
    
    public List<VClassView> getClasses() {
        if (classes == null) {
            List<VClass> classList = vClassGroup.getVitroClassList();
            classes = new ArrayList<VClassView>();
            Iterator<VClass> i = classList.iterator();
            while (i.hasNext()) {
                classes.add(new VClassView((VClass) i.next()));
            }
        }
        
        return classes;
    }
}
