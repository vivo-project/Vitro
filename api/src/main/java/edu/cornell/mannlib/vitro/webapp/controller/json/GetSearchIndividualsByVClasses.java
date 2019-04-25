/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Accepts multiple vclasses and returns individuals which correspond to the
 * intersection of those classes (i.e. have all those types)
 */
public class GetSearchIndividualsByVClasses extends JsonObjectProducer {
	private static final Log log = LogFactory
		.getLog(GetSearchIndividualsByVClasses.class);

	public GetSearchIndividualsByVClasses(VitroRequest vreq) {
		super(vreq);
	}

	@Override
	protected ObjectNode process() throws Exception {
    log.debug("Executing retrieval of individuals by vclasses");
        VClass vclass=null;
        log.debug("Retrieving search individuals by vclasses");
        // Could have multiple vclass ids sent in
        String[] vitroClassIdStr = vreq.getParameterValues("vclassId");
        if ( vitroClassIdStr != null && vitroClassIdStr.length > 0){
        	for(String vclassId: vitroClassIdStr) {
        		log.debug("Iterating throug vclasses, using VClass " + vclassId);
                vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vclassId);
                if (vclass == null) {
                    log.error("Couldn't retrieve vclass ");
                    throw new Exception ("Class " + vclassId + " not found");
                }
        	}
        }else{
            log.error("parameter vclassId URI parameter expected but not found");
            throw new Exception("parameter vclassId URI parameter expected ");
        }
        List<String> vclassIds = Arrays.asList(vitroClassIdStr);
        return JsonServlet.getSearchIndividualsByVClasses(vclassIds, vreq);
    }

}
