/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.PageDataGetterUtils;

/**
 * Gets data based on data getter for page uri and returns in the form of Json objects
 */
public class GetDataForPage extends JsonObjectProducer {
	private static final Log log = LogFactory.getLog(GetDataForPage.class);

	protected GetDataForPage(VitroRequest vreq) {
		super(vreq);
	}

	@Override
	protected JSONObject process() throws Exception {
       JSONObject rObj = null;
	   String pageUri = vreq.getParameter("pageUri");
	   if(pageUri != null && !pageUri.isEmpty()) {
		   Map<String,Object> data = PageDataGetterUtils.getDataForPage(pageUri, vreq, ctx);
		   //Convert to json version based on type of page
		   if(data != null) {
			 //Convert to json version based on type of page
			   rObj = PageDataGetterUtils.covertDataToJSONForPage(pageUri, data, vreq, ctx);
	   		}
	   }
	   return rObj;
    }

}
