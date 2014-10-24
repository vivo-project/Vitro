/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.controller;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption.ASSERTIONS_ONLY;

import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class BaseEditController extends VitroHttpServlet {

	public static final boolean FORCE_NEW = true; // when you know you're starting a new edit process
    
    public static final String JSP_PREFIX = "/templates/edit/specific/";
	
    protected static DateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    protected static final int BASE_10 = 10;

    private static final Log log = LogFactory.getLog(BaseEditController.class.getName());
    private static final String DEFAULT_LANDING_PAGE = Controllers.SITE_ADMIN;
    protected static final String MULTIPLEXED_PARAMETER_NAME = "multiplexedParam";
    private final String EPO_HASH_ATTR = "epoHash";
    private final String EPO_KEYLIST_ATTR = "epoKeylist";
    private final int MAX_EPOS = 5;
    private final Calendar cal = Calendar.getInstance();

    /* EPO is reused if the controller is passed an epoKey, e.g.
      if a previous form submission failed validation, or the edit is a multistage process. */

    protected EditProcessObject createEpo(HttpServletRequest request) {
    	return createEpo(request, false);
    }
    
    protected EditProcessObject createEpo(HttpServletRequest request, boolean forceNew) {
        /* this is actually a bit of a misnomer, because we will reuse an epo
        if an epoKey parameter is passed */
        EditProcessObject epo = null;
        HashMap epoHash = getEpoHash(request);
        String existingEpoKey = request.getParameter("_epoKey");
        if (!forceNew && existingEpoKey != null && epoHash.get(existingEpoKey) != null)  {
            epo = (EditProcessObject) epoHash.get(existingEpoKey);
            epo.setKey(existingEpoKey);
            epo.setUseRecycledBean(true);
        } else {
            LinkedList epoKeylist = getEpoKeylist(request);
            if (epoHash.size() == MAX_EPOS) {
            	try {
            		epoHash.remove(epoKeylist.getFirst());
            		epoKeylist.removeFirst();
            	} catch (Exception e) {
            		// see JIRA issue VITRO-340, "Odd exception from backend editing"
            		// possible rare concurrency issue here
            		log.error("Error removing old EPO", e);
            	}
            }
            Random rand = new Random();
            String epoKey = createEpoKey();
            while (epoHash.get(epoKey) != null) {
                epoKey+=Integer.toHexString(rand.nextInt());
            }
            epo = new EditProcessObject();
            epoHash.put (epoKey,epo);
            epoKeylist.add(epoKey);
            epo.setKey(epoKey);
            epo.setReferer( (forceNew) ? request.getRequestURL().append('?').append(request.getQueryString()).toString() : request.getHeader("Referer") );
            epo.setSession(request.getSession());
        }
        return epo;
    }

    private LinkedList getEpoKeylist(HttpServletRequest request){
        return (LinkedList) request.getSession().getAttribute(EPO_KEYLIST_ATTR);
    }

    private HashMap getEpoHash(HttpServletRequest request){
        HashMap epoHash = (HashMap) request.getSession().getAttribute(EPO_HASH_ATTR);
        if (epoHash == null) {
            epoHash = new HashMap();
            request.getSession().setAttribute(EPO_HASH_ATTR,epoHash);
            //since we're making a new EPO hash, we should also make a new keylist.
            LinkedList epoKeylist = new LinkedList();
            request.getSession().setAttribute(EPO_KEYLIST_ATTR,epoKeylist);
        }
        return epoHash;
    }

    private String createEpoKey(){
        return Long.toHexString(cal.getTimeInMillis());
    }

    protected void setRequestAttributes(HttpServletRequest request, EditProcessObject epo){
    	VitroRequest vreq = new VitroRequest(request);
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("epo",epo);
        request.setAttribute("globalErrorMsg",epo.getAttribute("globalErrorMsg"));
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+vreq.getAppBean().getThemeDir()+"css/edit.css\"/>");
    }

    protected void populateBeanFromParams(Object bean, HttpServletRequest request) {
        Map params = request.getParameterMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()){
            String key = "";
            try {
                key = (String) paramNames.nextElement();
            } catch (ClassCastException cce) {
                log.error("populateBeanFromParams() could not cast parameter name to String");
            }
            String value = "";
            if (key.equals(MULTIPLEXED_PARAMETER_NAME)) {
                String multiplexedStr = request.getParameterValues(key)[0];
                Map paramMap = FormUtils.beanParamMapFromString(multiplexedStr);
                Iterator paramIt = paramMap.keySet().iterator();
                while (paramIt.hasNext()) {
                    String param = (String) paramIt.next();
                    String demultiplexedValue = (String) paramMap.get(param);
                    FormUtils.beanSet(bean, param, demultiplexedValue);
                }

            } else {
                try {
                    value = (String) request.getParameterValues(key)[0];
                } catch (ClassCastException cce) {
                    try {
                        value = ((Integer) params.get(key)).toString();
                    } catch (ClassCastException ccf) {
                        log.error("populateBeanFromParams() could not cast parameter name to String");
                    }
                }
                FormUtils.beanSet(bean, key, value);
            }
        }
    }
    
    public List<Option> getSortedList(HashMap<String,Option> hashMap, List<Option> optionList, VitroRequest vreq){
        
        class ListComparator implements Comparator<String>{
            
            Collator collator;
            
            public ListComparator(Collator collator) {
                this.collator = collator;
            }
            
            @Override
            public int compare(String str1, String str2) {
                return collator.compare(str1, str2);
            }
            
        }

       List<String> bodyVal = new ArrayList<String>();
       List<Option> options = new ArrayList<Option>();
       Iterator<Option> itr = optionList.iterator();
        while(itr.hasNext()){
            Option option = itr.next();
            hashMap.put(option.getBody(),option);
           bodyVal.add(option.getBody());
        }
        
                
       Collections.sort(bodyVal, new ListComparator(vreq.getCollator()));
       ListIterator<String> itrStr = bodyVal.listIterator();
       while(itrStr.hasNext()){
           options.add(hashMap.get(itrStr.next()));
       }
       return options;
   }
    
    protected WebappDaoFactory getWebappDaoFactory() {
    	return ModelAccess.on(getServletContext()).getWebappDaoFactory(ASSERTIONS_ONLY);
    }
    
    protected WebappDaoFactory getWebappDaoFactory(String userURI) {
        return getWebappDaoFactory().getUserAwareDaoFactory(userURI);
    }
    
    public String getDefaultLandingPage(HttpServletRequest request) {
    	return(request.getContextPath() + DEFAULT_LANDING_PAGE);
    }

}
