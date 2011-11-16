/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditSiteInformation;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;

public class ApplicationBeanRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(ApplicationBeanRetryController.class.getName());
	
    public void doPost (HttpServletRequest req, HttpServletResponse response) {

        if (!isAuthorizedToDisplayPage(req, response, new Actions(new EditSiteInformation()))) {
        	return;
        }
    	
    	VitroRequest request = new VitroRequest(req);

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error(e,e);
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        epo.setBeanClass(ApplicationBean.class);

        String action = "update";

        ApplicationDao aDao = request.getFullWebappDaoFactory().getApplicationDao();
        ApplicationBean applicationForEditing = aDao.getApplicationBean();
        epo.setDataAccessObject(aDao);
 
        if (!epo.getUseRecycledBean()){
            action = "update";
            epo.setOriginalBean(applicationForEditing);
        } else {
            applicationForEditing = (ApplicationBean) epo.getNewBean();
            action = "update";
            log.debug("using newBean");
        }

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            epo.setGetMethod(aDao.getClass().getDeclaredMethod("getApplicationBean"));
        } catch (NoSuchMethodException e) {
            log.error("could not find the getApplicationBean method in the facade");
        }

        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());

        HashMap optionMap = new HashMap();
        
        List<Option> themeOptions = getThemeOptions(applicationForEditing);
        optionMap.put("ThemeDir", themeOptions);
        
        foo.setOptionLists(optionMap);

        epo.setFormObject(foo);
        FormUtils.populateFormFromBean(applicationForEditing, epo.getAction(), foo);
   
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formContact.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/applicationBean_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Site Information");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","ApplicationBean");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(e, e);
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }
    
    /**
     * Returns a list of Option objects for valid themes in the application, based on names of subdirectories
     * of the "/themes" directory.
     * 
     * @return list of Options for valid themes
     */
    private final List<Option> getThemeOptions(ApplicationBean application) {
    	 
    	// Get the available themes
    	ServletContext sc = getServletContext();
    	boolean doSort = true;
    	List<String> themeNames = ApplicationBean.themeInfo.getThemeNames();

        // Create the list of theme Options
        String currentThemeDir = application.getThemeDir(); 
        Iterator<String> i = themeNames.iterator();
        List<Option> themeOptions = new ArrayList<Option>(themeNames.size());
        String themeName, themeDir;
        boolean selected;
        while (i.hasNext()) {
        	themeName = i.next();
        	themeDir = "themes/" + themeName + "/";
        	selected = themeDir.equals(currentThemeDir);
        	themeOptions.add(new Option(themeDir, themeName, selected));
        }
        
        return themeOptions;
    }
    
}

