/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupsForRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;

/**
 *This class will get all the vclasses in the system.
 */
public class GetAllVClasses extends JsonObjectProducer {
	private static final Log log = LogFactory
			.getLog(GetAllVClasses.class);

	public GetAllVClasses(VitroRequest vreq) {
		super(vreq);
	}

	@Override
	protected JSONObject process() throws Exception {                
        JSONObject map = new JSONObject();           
        //Get all VClassGroups
        List<VClass> vclasses = new ArrayList<VClass>();     
        VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(vreq);
        List<VClassGroup> groups = vcgc.getGroups();
        for(VClassGroup vcg: groups) {
             for( VClass vc : vcg){
                 vclasses.add(vc);
             }
            
        }
       
        //Sort vclass by name
        Collections.sort(vclasses);
   	 	ArrayList<JSONObject> classes = new ArrayList<JSONObject>(vclasses.size());

        for(VClass vc: vclasses) {
        	JSONObject vcObj = new JSONObject();
        	vcObj.put("name", vc.getName());
        	vcObj.put("URI", vc.getURI());
        	classes.add(vcObj);            
        }
        map.put("classes", classes);                
       
        return map;
    }

}
