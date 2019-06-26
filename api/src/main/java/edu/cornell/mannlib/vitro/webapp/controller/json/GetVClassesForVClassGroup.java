/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

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
 *
 */
public class GetVClassesForVClassGroup extends JsonObjectProducer {
	private static final Log log = LogFactory
			.getLog(GetVClassesForVClassGroup.class);

	public GetVClassesForVClassGroup(VitroRequest vreq) {
		super(vreq);
	}

	@Override
	protected ObjectNode process() throws Exception {
        ObjectNode map = JsonNodeFactory.instance.objectNode();
        String vcgUri = vreq.getParameter("classgroupUri");
        if( vcgUri == null ){
            throw new Exception("no URI passed for classgroupUri");
        }

        VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(vreq);
        VClassGroup vcg = vcgc.getGroup(vcgUri);
        if( vcg == null ){
            throw new Exception("Could not find vclassgroup: " + vcgUri);
        }

        ArrayNode classes = JsonNodeFactory.instance.arrayNode();
        for( VClass vc : vcg){
            ObjectNode vcObj = JsonNodeFactory.instance.objectNode();
            vcObj.put("name", vc.getName());
            vcObj.put("URI", vc.getURI());
            vcObj.put("entityCount", vc.getEntityCount());
            classes.add(vcObj);
        }
        map.put("classes", classes);
        map.put("classGroupName", vcg.getPublicName());
        map.put("classGroupUri", vcg.getURI());

        return map;
    }

}
