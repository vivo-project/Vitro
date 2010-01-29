package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

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
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.filters.PortalPickerFilter;
import edu.cornell.mannlib.vitro.webapp.utils.ThemeUtils;

public class PortalRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(PortalRetryController.class.getName());
	
    public void doPost (HttpServletRequest req, HttpServletResponse response) {

    	VitroRequest request = new VitroRequest(req);
        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("PortalRetryController encountered exception calling super.doGet()");
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        epo.setBeanClass(Portal.class);

        String action = "insert";

        PortalDao pDao = getWebappDaoFactory().getPortalDao();
        epo.setDataAccessObject(pDao);
        TabDao tDao = getWebappDaoFactory().getTabDao();

        boolean creatingNewPortal = false; 
        Portal portalForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("id") != null) {
                int id = Integer.parseInt(request.getParameter("id"));
                if (id > 0) {
                    try {
                        portalForEditing = (Portal)pDao.getPortal(id);
                        action = "update";
                    } catch (NullPointerException e) {
                        log.error("Need to implement 'record not found' error message.");
                    }
                }
            } else {
                portalForEditing = new Portal();
                creatingNewPortal = true;
            }
            epo.setOriginalBean(portalForEditing);
        } else {
            portalForEditing = (Portal) epo.getNewBean();
            action = "update";
            log.error("using newBean");
        }

             
        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="Id";
        simpleMaskPair[1]=Integer.valueOf(portalForEditing.getPortalId());
        epo.getSimpleMask().add(simpleMaskPair);


        //set any validators

        //set up any listeners
        List changeListenerList = new ArrayList();
        epo.setChangeListenerList(changeListenerList);
        changeListenerList.add(new PortalPrefixUpdater(getServletContext(),getWebappDaoFactory().getPortalDao()));
        
        epo.setPostDeletePageForwarder(new PortalDeletionPageForwarder(getServletContext(),pDao));

        //set portal flag to current portal
        Portal currPortal = (Portal) request.getAttribute("portalBean");
        int currPortalId = 1;
        if (currPortal != null) {
            currPortalId = currPortal.getPortalId();
        }

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = int.class;
            epo.setGetMethod(pDao.getClass().getDeclaredMethod("getPortal",args));
        } catch (NoSuchMethodException e) {
            log.error("PortalRetryController could not find the getPortalById method in the facade");
        }

        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());

        HashMap optionMap = new HashMap();
        
        List tabList = FormUtils.makeOptionListFromBeans(tDao.getPrimaryTabs(portalForEditing.getPortalId()),"TabId","Title",Integer.valueOf(portalForEditing.getRootTabId()).toString(),null,portalForEditing.getRootTabId()>0);
        tabList.add(0,new Option("0","No Tab Yet",false));
        optionMap.put("RootTabId", tabList);

        List flag1filteringList = new ArrayList(2);
        flag1filteringList.add( new Option("true", "true", currPortal.isFlag1Filtering()));
        flag1filteringList.add( new Option("false", "false", !currPortal.isFlag1Filtering()));
        optionMap.put("Flag1Filtering",flag1filteringList);
        
        List<Option> themeOptions = getThemeOptions(portalForEditing);
        optionMap.put("ThemeDir", themeOptions);
        
        foo.setOptionLists(optionMap);

        epo.setFormObject(foo);

        String html = FormUtils.htmlFormFromBean(portalForEditing,action,foo,epo.getBadValueMap());

        Boolean singlePortal = new Boolean(request.getWebappDaoFactory().getPortalDao().isSinglePortal());
        request.setAttribute("singlePortal", singlePortal);
        request.setAttribute("creatingNewPortal", new Boolean(creatingNewPortal));
            
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/portal_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        if(  singlePortal && ! creatingNewPortal )
            request.setAttribute("title","Site Information Editing Form");
        else
            request.setAttribute("title","Portal Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Portal");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("PortalRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    private class PortalPrefixUpdater implements ChangeListener {

    	private ServletContext context;
    	private PortalDao portalDao;
    	
    	public PortalPrefixUpdater(ServletContext servletContext, PortalDao portalDao) {
    		this.context = servletContext;
    		this.portalDao = portalDao;
    	}

    	// TODO: setupPortalMappings is not thread safe. 
    	// Should make it so if we expect to be calling this a lot
		public void doDeleted(Object oldObj, EditProcessObject epo) {
			PortalPickerFilter.getPortalPickerFilter(context).setupPortalMappings(portalDao.getAllPortals());
		}

		public void doInserted(Object newObj, EditProcessObject epo) {
			PortalPickerFilter.getPortalPickerFilter(context).setupPortalMappings(portalDao.getAllPortals());
		}

		public void doUpdated(Object oldObj, Object newObj,
				EditProcessObject epo) {
			if ( (oldObj instanceof Portal) && (newObj instanceof Portal) ) {
				Portal oldPortal = (Portal) oldObj;
				Portal newPortal = (Portal) newObj;
				boolean urlPrefixChanged = false;
				if ( (oldPortal.getUrlprefix() == null) && (newPortal.getUrlprefix() != null) ) { 
					urlPrefixChanged = true;
				} else if ( (oldPortal.getUrlprefix() != null) && (newPortal.getUrlprefix() == null) ) {
					urlPrefixChanged = true;
				} else if ( (oldPortal.getUrlprefix() != null) && (newPortal.getUrlprefix() == null) && (!(oldPortal.getUrlprefix().equals(newPortal.getUrlprefix()))) ) {
					urlPrefixChanged = true;
				}
				if (urlPrefixChanged) {
					PortalPickerFilter.getPortalPickerFilter(context).setupPortalMappings(portalDao.getAllPortals());
				}
			}
		}
		
    }
    
    private class PortalDeletionPageForwarder implements PageForwarder {

    	private static final String REDIRECT_PAGE = "/listPortals";
    	
    	private ServletContext context;
    	private PortalDao portalDao;
    	
    	public PortalDeletionPageForwarder(ServletContext servletContext, PortalDao portalDao) {
    		this.context = servletContext;
    		this.portalDao = portalDao;
    	}
    	
		public void doForward(HttpServletRequest request,
				HttpServletResponse response, EditProcessObject epo) {
			VitroRequest vreq = new VitroRequest(request);
			Portal redirectPortal = vreq.getPortal();
			if (epo.getOriginalBean() instanceof Portal) {
				Portal deletedPortal = (Portal) epo.getOriginalBean();
				if (vreq.getPortal().getPortalId() == deletedPortal.getPortalId()) {
					redirectPortal = portalDao.getPortal(Portal.DEFAULT_PORTAL_ID);
				}
			} 
			StringBuffer redirectPath = (new StringBuffer()).append(request.getContextPath());
			if (redirectPortal.getUrlprefix() != null) {
				redirectPath.append("/").append(redirectPortal.getUrlprefix()).append(REDIRECT_PAGE);
			} else {
				redirectPath.append(REDIRECT_PAGE).append("?home=").append(redirectPortal.getPortalId());
			}
			try {
				response.sendRedirect(redirectPath.toString());
			} catch (IOException ioe) {
				log.error("Unable to redirect to "+redirectPath);
			}
		}
    	
    }
    
    /**
     * Returns a list of Option objects for valid themes in the application, based on names of subdirectories
     * of the "/themes" directory.
     * 
     * @return list of Options for valid themes
     */
    private final List<Option> getThemeOptions(Portal portal) {
    	 
    	// Get the available themes
    	ServletContext sc = getServletContext();
    	boolean doSort = true;
    	ArrayList<String> themeNames = ThemeUtils.getThemes(sc, doSort);

        // Create the list of theme Options
        String currentThemeDir = portal.getThemeDir(); // the current value for the portal
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

