/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * 
 */
public class GetSearchIndividualsByVClass extends JsonObjectProducer {
	private static final Log log = LogFactory
			.getLog(GetSearchIndividualsByVClass.class);
	
	protected GetSearchIndividualsByVClass(VitroRequest vreq) {
		super(vreq);
	}

	@Override
	protected JSONObject process() throws Exception {
        VClass vclass=null;
        
        String queryType = (String) vreq.getAttribute("queryType");
        String vitroClassIdStr = vreq.getParameter("vclassId");            
        if ( vitroClassIdStr != null && !vitroClassIdStr.isEmpty()){                             
            vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vitroClassIdStr);
            if (vclass == null) {
                log.debug("Couldn't retrieve vclass ");   
                throw new Exception ("Class " + vitroClassIdStr + " not found");
            }                           
        }else{
            log.debug("parameter vclassId URI parameter expected ");
            throw new Exception("parameter vclassId URI parameter expected ");
        }
        
        vreq.setAttribute("displayType", vitroClassIdStr);
        if ( queryType != null && queryType.equals("random")){
            return JsonServlet.getRandomSearchIndividualsByVClass(vclass.getURI(), vreq);             
        } else {
            return JsonServlet.getSearchIndividualsByVClass(vclass.getURI(), vreq);
        }
    }

}
