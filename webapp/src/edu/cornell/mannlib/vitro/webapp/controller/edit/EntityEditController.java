/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoBackEndEditing;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;

public class EntityEditController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(EntityEditController.class.getName());

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new DoBackEndEditing()))) {
        	return;
        }

        String entURI = request.getParameter("uri");
        VitroRequest vreq = (new VitroRequest(request));
        ApplicationBean application = vreq.getAppBean();

        Individual ent = vreq.getAssertionsWebappDaoFactory().getIndividualDao().getIndividualByURI(entURI);
        if (ent == null) {
        	try {
        		RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        		request.setAttribute("bodyJsp","/jenaIngest/notfound.jsp");
        		request.setAttribute("title","Individual Not Found");
        		request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+application.getThemeDir()+"css/edit.css\"/>");
        		rd.forward(request, response);
            } catch (Exception e) {
                log.error("EntityEditController could not forward to view.");
                log.error(e.getMessage());
                log.error(e.getStackTrace());
            }
        }
        
        Individual inferredEnt = vreq.getFullWebappDaoFactory().getIndividualDao().getIndividualByURI(entURI);
        if (inferredEnt == null) {
        	inferredEnt = new IndividualImpl(entURI);
        }
        
        request.setAttribute("entity",ent);

        ArrayList<String> results = new ArrayList<String>();
        int colCount = 4;
        results.add("Name");
        results.add("class");
        results.add("display level");
        results.add("edit level");
        results.add("last updated");
        colCount++;
        results.add("URI");
        colCount++;
        
        String rName = null;
        if (ent.getName() != null && ent.getName().length() > 0) {
        	rName = ent.getName();
        } else if (ent.getLocalName() != null && ent.getLocalName().length() > 0) {
        	rName = ent.getLocalName();
        } else if (ent.isAnonymous()) {
        	rName = "[anonymous resource]";
        } else {
        	rName = "[resource]";
        }
        results.add(rName);
        
        String classStr = "";
        List<VClass> classList = inferredEnt.getVClasses(false);
        if (classList != null) {
	        for (Iterator<VClass> classIt = classList.iterator(); classIt.hasNext();) {
	        	VClass vc = classIt.next();
	        	String rClassName = "";
	            try {
	                rClassName = "<a href=\"vclassEdit?uri=" +
	                		URLEncoder.encode(vc.getURI(),"UTF-8")+"\">" + 
	                		vc.getLocalNameWithPrefix()+"</a>";
	            } catch (Exception e) {
	                rClassName = vc.getLocalNameWithPrefix();
	            }
	            classStr += rClassName;
	            if (classIt.hasNext()) {
	            	classStr += ", ";
	            }
	        }
        }
        results.add(classStr);
                
        results.add(ent.getHiddenFromDisplayBelowRoleLevel()  == null ? "unspecified" : ent.getHiddenFromDisplayBelowRoleLevel().getLabel());
        results.add(ent.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : ent.getProhibitedFromUpdateBelowRoleLevel().getLabel());

        String rModTime = (ent.getModTime()==null) ? "" : publicDateFormat.format(ent.getModTime());
        results.add(rModTime);
        results.add( (ent.getURI() == null) ? "[anonymous individual]" : ent.getURI() );
        request.setAttribute("results",results);
        request.setAttribute("columncount", colCount);
        request.setAttribute("suppressquery","true");
        
        EditProcessObject epo = super.createEpo(request,FORCE_NEW);
        request.setAttribute("epo", epo);

        FormObject foo = new FormObject();
        HashMap<String, List<Option>> OptionMap = new HashMap<String, List<Option>>();
        
        request.setAttribute("types",ent.getVClasses(false)); // we're displaying all assertions, including indirect types
        
        try {
            List<Option> externalIdOptionList = new LinkedList<Option>();
            if (ent.getExternalIds() != null) {
                Iterator<DataPropertyStatement> externalIdIt = ent.getExternalIds().iterator();
                while (externalIdIt.hasNext()) {
                    DataPropertyStatement eid = externalIdIt.next();
                    String multiplexedString = new String ("DatapropURI:" + new String(Base64.encodeBase64(eid.getDatapropURI().getBytes())) + ";" + "Data:" + new String(Base64.encodeBase64(eid.getData().getBytes())));
                    externalIdOptionList.add(new Option(multiplexedString, eid.getData()));
                }
            }
            OptionMap.put("externalIds", externalIdOptionList);
        } catch (Exception e) {
            log.error(e, e);
        }
                
        try{
            OptionMap.put("VClassURI", FormUtils.makeOptionListFromBeans(
                    vreq.getFullWebappDaoFactory().getVClassDao().getAllVclasses(),
                            "URI", "PickListName", ent.getVClassURI(), null, false));        
        } catch (Exception e) {
            log.error(e, e);
        }
        
        PropertyInstanceDao piDao = vreq.getFullWebappDaoFactory().getPropertyInstanceDao();
        // existing property statements
        try {
            List epiOptionList = new LinkedList();
            Collection<PropertyInstance> epiColl = piDao.getExistingProperties(ent.getURI(),null);
            Iterator<PropertyInstance> epiIt = epiColl.iterator();
            while (epiIt.hasNext()) {
                PropertyInstance pi = epiIt.next();
                String multiplexedString = new String ("PropertyURI:" + new String(Base64.encodeBase64(pi.getPropertyURI().getBytes())) + ";" + "ObjectEntURI:" + new String(Base64.encodeBase64(pi.getObjectEntURI().getBytes())));
                epiOptionList.add(new Option(multiplexedString, pi.getDomainPublic()+" "+pi.getObjectName()));
            }
            OptionMap.put("ExistingPropertyInstances", epiOptionList);
        } catch (Exception e) {
            log.error(e, e);
        }
        // possible property statements
        try {
            Collection piColl = piDao.getAllPossiblePropInstForIndividual(ent.getURI());
            List piList = new ArrayList();
            piList.addAll(piColl);
            OptionMap.put("PropertyURI", FormUtils.makeOptionListFromBeans(piList, "PropertyURI", "DomainPublic", (String)null, (String)null, false));
        } catch (Exception e) {
            log.error(e, e);
        }

        foo.setOptionLists(OptionMap);

        epo.setFormObject(foo);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("entityWebapp", ent);
        request.setAttribute("bodyJsp","/templates/edit/specific/ents_edit.jsp");
        request.setAttribute("title","Individual Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+application.getThemeDir()+"css/edit.css\"/>");
        request.setAttribute("scripts", "/templates/edit/specific/ents_edit_head.jsp");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("EntityEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doPost (HttpServletRequest request, HttpServletResponse response) {
    	log.trace("Please don't POST to the "+this.getClass().getName()+". Use GET instead as there should be no change of state.");
        doPost(request,response);
    }

}
