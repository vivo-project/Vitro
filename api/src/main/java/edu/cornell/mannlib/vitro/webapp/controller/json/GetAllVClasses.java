/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	protected ObjectNode process() throws Exception {
        ObjectNode map = JsonNodeFactory.instance.objectNode();
        //Get all VClassGroups
        List<VClass> vclasses = new ArrayList<VClass>();
        VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(vreq);
        List<VClassGroup> groups = vcgc.getGroups();
        for(VClassGroup vcg: groups) {
            vclasses.addAll(vcg);

        }

        //Sort vclass by name
        Collections.sort(vclasses);
        ArrayNode classes = JsonNodeFactory.instance.arrayNode();

        for(VClass vc: vclasses) {
        	ObjectNode vcObj = JsonNodeFactory.instance.objectNode();
        	vcObj.put("name", vc.getName());
        	vcObj.put("URI", vc.getURI());
        	classes.add(vcObj);
        }
        map.put("classes", classes);

        return map;
    }

}
