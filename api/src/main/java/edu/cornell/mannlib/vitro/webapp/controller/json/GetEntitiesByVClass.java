/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import static edu.cornell.mannlib.vitro.webapp.controller.json.JsonServlet.REPLY_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 *  Gets a list of entities that are members of the indicated vClass.
 *
 * If the list is large then we will pass some token indicating that there is more
 * to come.  The results are sent back in 250 entity blocks.  To get all of the
 * entities for a VClass just keep requesting lists until there are not more
 * continue tokens.
 *
 * If there are more entities the last item on the returned array will be an object
 * with no id property. It will look like this:
 *
 * {@code
 * {"resultGroup":0,
 *  "resultKey":"2WEK2306",
 *  "nextUrl":"http://caruso.mannlib.cornell.edu:8080/vitro/dataservice?getEntitiesByVClass=1&resultKey=2WEK2306&resultGroup=1&vclassId=null",
 *  "entsInVClass":1752,
 *  "nextResultGroup":1,
 *  "standardReplySize":256}
 * }
 */
public class GetEntitiesByVClass extends JsonArrayProducer {
	private static final Log log = LogFactory.getLog(GetEntitiesByVClass.class);

	protected GetEntitiesByVClass(VitroRequest vreq) {
		super(vreq);
	}

	@Override
	protected JSONArray process() throws ServletException {
        log.debug("in getEntitiesByVClass()");
        String vclassURI = vreq.getParameter("vclassURI");
        WebappDaoFactory daos = vreq.getUnfilteredWebappDaoFactory();
        
        if( vclassURI == null ){
            throw new ServletException("getEntitiesByVClass(): no value for 'vclassURI' found in the HTTP request");
        }

        VClass vclass = daos.getVClassDao().getVClassByURI( vclassURI );                       
        if( vclass == null ){
            throw new ServletException("getEntitiesByVClass(): could not find vclass for uri '"+  vclassURI + "'");
        }

        List<Individual> entsInVClass = daos.getIndividualDao().getIndividualsByVClass( vclass );
        if( entsInVClass == null ){
            throw new ServletException("getEntitiesByVClass(): null List<Individual> retruned by getIndividualsByVClass() for "+vclassURI);
        }
        int numberOfEntsInVClass = entsInVClass.size();

        List<Individual> entsToReturn = new ArrayList<Individual>( REPLY_SIZE );
        String requestHash = null;
        int count = 0;
        boolean more = false;
        /* we have a large number of items to send back so we need to stash the list in the session scope */
        if( entsInVClass.size() > REPLY_SIZE){
            more = true;
            HttpSession session = vreq.getSession(true);
            requestHash = Integer.toString((vclassURI + System.currentTimeMillis()).hashCode());
            session.setAttribute(requestHash, entsInVClass );

            ListIterator<Individual> entsFromVclass = entsInVClass.listIterator();
            while ( entsFromVclass.hasNext() && count < REPLY_SIZE ){
                entsToReturn.add( entsFromVclass.next());
                entsFromVclass.remove();
                count++;
            }
            if( log.isDebugEnabled() ){ log.debug("getEntitiesByVClass(): Creating reply with continue token, found " + numberOfEntsInVClass + " Individuals"); } 
        }else{
            if( log.isDebugEnabled() ) log.debug("getEntitiesByVClass(): sending " + numberOfEntsInVClass +" Individuals without continue token");
            entsToReturn = entsInVClass;
            count = entsToReturn.size();
        }

        
        //put all the entities on the JSON array
        JSONArray ja =  individualsToJson( entsToReturn );
        
        //put the responseGroup number on the end of the JSON array
        if( more ){
            try{
                JSONObject obj = new JSONObject();
                obj.put("resultGroup", "true");
                obj.put("size", count);
                obj.put("total", numberOfEntsInVClass);

                StringBuffer nextUrlStr = vreq.getRequestURL();
                nextUrlStr.append("?")
                        .append("getEntitiesByVClass").append( "=1&" )
                        .append("resultKey=").append( requestHash );
                obj.put("nextUrl", nextUrlStr.toString());

                ja.put(obj);
            }catch(JSONException je ){
                throw new ServletException("unable to create continuation as JSON: " + je.getMessage());
            }
        }
        
        log.debug("done with getEntitiesByVClass()");
        return ja;
    }
    

}
