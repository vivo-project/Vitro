/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans.display;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

/*
 * A VClassGroupDisplay object is associated with a VClassGroup for display.
 * It is an object that contains a linked list, rather than a type of linked list,
 * so that JSP EL can access properties such as publicName.
 * 
 * RY We may want an abstract display class as a superclass.
 * RY We may want an interface that the superclass would implement.
 * RY We may want to nest this class in the VClassGroup class.
 */
public class VClassGroupDisplay {

	private static final Log log = LogFactory.getLog(VClassGroupDisplay.class.getName());
	
    private VClassGroup vClassGroup;
    
    // RY Probably don't want the default constructor.
    public VClassGroupDisplay() {
    	vClassGroup = new VClassGroup();
    }
    
    public VClassGroupDisplay(VClassGroup vClassGroup) {
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
    
    public List<VClass> getVitroClassList() {
    	return vClassGroup.getVitroClassList();
    }
}
