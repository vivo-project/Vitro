/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseBasicAjaxControllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;


public class PrimitiveDelete extends VitroAjaxController {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(PrimitiveDelete.class);  

    @Override
    protected Actions requiredActions(VitroRequest vreq) {
    	return new Actions(new UseBasicAjaxControllers());
    }
    
    @Override
    protected void doRequest(VitroRequest vreq, HttpServletResponse response) {
     
        String uriToDelete = vreq.getParameter("deletion");
        if (StringUtils.isEmpty(uriToDelete)) {
            doError(response, "No individual specified for deletion", 500);
            return;
        }
        
        // Check permissions
        // The permission-checking code should be inherited from superclass
        boolean hasPermission = true;
        
        if( !hasPermission ){
            //if not okay, send error message
            doError(response,"Insufficent permissions.", HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        WebappDaoFactory wdf = vreq.getFullWebappDaoFactory();
        IndividualDao idao = wdf.getIndividualDao();
        int result = idao.deleteIndividual(uriToDelete);
        if (result == 1) {
            doError(response, "Error deleting individual", 500);
        }
    }

}
