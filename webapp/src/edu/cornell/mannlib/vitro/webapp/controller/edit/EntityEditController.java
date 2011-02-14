/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.Checkbox;
import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.KeywordIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordDao;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class EntityEditController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(EntityEditController.class.getName());

    private final static int MIN_SHARED_PORTAL_ID = 16; // will this be available from the AppBean?

    public void doGet (HttpServletRequest request, HttpServletResponse response) {

        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("EntityEditController caught exception calling doGet()");
        }

        String entURI = request.getParameter("uri");
        VitroRequest vreq = (new VitroRequest(request));
        Portal portal = vreq.getPortal();
        ApplicationBean application = vreq.getAppBean();

        Individual ent = vreq.getAssertionsWebappDaoFactory().getIndividualDao().getIndividualByURI(entURI);
        if (ent == null) {
        	try {
        		RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        		request.setAttribute("bodyJsp","/jenaIngest/notfound.jsp");
        		request.setAttribute("portalBean",portal);
        		request.setAttribute("title","Individual Not Found");
        		request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");
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

        ArrayList results = new ArrayList();
        int colCount = 6;
        results.add("Name");
        results.add("moniker");
        results.add("class");
        results.add("blurb");
        results.add("display level");
        results.add("edit level");
        if (application.isOnlyCurrent()) {
        	results.add("sunrise");
        	results.add("timekey");
            results.add("sunset");
            colCount = colCount + 3;
        }
        if (vreq.getFullWebappDaoFactory().getApplicationDao().isFlag2Active()) {
        	results.add("Flag 2 values");
        	colCount++;
        }
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
        String rMoniker = (ent.getMoniker()==null) ? "unspecified" : ent.getMoniker();
        results.add(rMoniker);
        
        String classStr = "";
        List<VClass> classList = inferredEnt.getVClasses(false);
        if (classList != null) {
	        for (Iterator<VClass> classIt = classList.iterator(); classIt.hasNext();) {
	        	VClass vc = classIt.next();
	        	String rClassName = "";
	            try {
	                rClassName = "<a href=\"vclassEdit?home="+portal.getPortalId()+"&amp;uri="+URLEncoder.encode(vc.getURI(),"UTF-8")+"\">"+vc.getLocalNameWithPrefix()+"</a>";
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
        
        String rBlurb = (ent.getBlurb()==null) ? "" : ent.getBlurb();
        results.add(rBlurb);
        
        results.add(ent.getHiddenFromDisplayBelowRoleLevel()  == null ? "unspecified" : ent.getHiddenFromDisplayBelowRoleLevel().getLabel());
        results.add(ent.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : ent.getProhibitedFromUpdateBelowRoleLevel().getLabel());

        if (application.isOnlyCurrent()) {
	        String rSunrise = (ent.getSunrise()==null) ? "" : publicDateFormat.format(ent.getSunrise());
	        results.add(rSunrise);
	        String rTimekey = (ent.getTimekey()==null) ? "" : publicDateFormat.format(ent.getTimekey());
	        results.add(rTimekey);
	        String rSunset = (ent.getSunset()==null) ? "" : publicDateFormat.format(ent.getSunset());
	        results.add(rSunset);
        }
        if (vreq.getFullWebappDaoFactory().getApplicationDao().isFlag2Active()) {
	        String rFlag2Set = (ent.getFlag2Set()==null) ? "" : ent.getFlag2Set();
	        results.add(rFlag2Set);
        }
        String rModTime = (ent.getModTime()==null) ? "" : publicDateFormat.format(ent.getModTime());
        results.add(rModTime);
        results.add( (ent.getURI() == null) ? "[anonymous individual]" : ent.getURI() );
        request.setAttribute("results",results);
        request.setAttribute("columncount", colCount);
        request.setAttribute("suppressquery","true");
        
        EditProcessObject epo = super.createEpo(request,FORCE_NEW);
        request.setAttribute("epo", epo);

        FormObject foo = new FormObject();
        HashMap OptionMap = new HashMap();
        
        Collection<DataPropertyStatement> curationNotes = vreq.getFullWebappDaoFactory().getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(ent, VitroVocabulary.CURATOR_NOTE);
        List curationNoteStrs = new LinkedList();
        Iterator<DataPropertyStatement> cnIt = curationNotes.iterator();
        while (cnIt.hasNext()) {
            curationNoteStrs.add(cnIt.next().getData());
        }
        request.setAttribute("curationNotes",curationNotes);
        
        request.setAttribute("types",ent.getVClasses(false)); // we're displaying all assertions, including indirect types
        
        try {
            List externalIdOptionList = new LinkedList();
            if (ent.getExternalIds() != null) {
                Iterator<DataPropertyStatement> externalIdIt = ent.getExternalIds().iterator();
                while (externalIdIt.hasNext()) {
                    DataPropertyStatement eid = externalIdIt.next();
                    String multiplexedString = new String ("DatapropURI:" + new String(Base64.encodeBase64(eid.getDatapropURI().getBytes())) + ";" + "Data:" + new String(Base64.encodeBase64(eid.getData().getBytes())));
                    externalIdOptionList.add(new Option(multiplexedString, eid.getData()));
                }
            }
            OptionMap.put("externalIds", externalIdOptionList);
        } catch (Exception e) {e.printStackTrace();}
        
        try {
            OptionMap.put("ExtraURL", FormUtils.makeOptionListFromBeans(ent.getLinksList(), "URI", "Anchor", null, null, false));
        } catch (Exception e) {e.printStackTrace();}
        
        List classGroups = vreq.getFullWebappDaoFactory().getVClassGroupDao().getPublicGroupsWithVClasses(true,true,false); // order by displayRank, include uninstantiated classes, don't count the individuals
        Iterator classGroupIt = classGroups.iterator();
        ListOrderedMap optGroupMap = new ListOrderedMap();
        while (classGroupIt.hasNext()) {
            VClassGroup group = (VClassGroup)classGroupIt.next();
            List classes = group.getVitroClassList();
            optGroupMap.put(group.getPublicName(),FormUtils.makeOptionListFromBeans(classes,"URI","PickListName",ent.getVClassURI(),null,false));
            //mixes group names with classes:optGroupMap.put(group.getPublicName(),FormUtils.makeVClassOptionList(getFullWebappDaoFactory(),ent.getVClassURI()));
        }
        try {
            OptionMap.put("VClassURI", optGroupMap);
        } catch (Exception e) {e.printStackTrace();}       
        
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
            e.printStackTrace();
        }
        // possible property statements
        try {
            Collection piColl = piDao.getAllPossiblePropInstForIndividual(ent.getURI());
            List piList = new ArrayList();
            piList.addAll(piColl);
            OptionMap.put("PropertyURI", FormUtils.makeOptionListFromBeans(piList, "PropertyURI", "DomainPublic", (String)null, (String)null, false));
        } catch (Exception e) {
            e.printStackTrace();
        }

        foo.setOptionLists(OptionMap);

        // make the flag checkbox lists
        Boolean singlePortal = new Boolean(vreq.getFullWebappDaoFactory().getPortalDao().isSinglePortal());
        request.setAttribute("singlePortal", singlePortal);

        EditProcessObject flagEpo = super.createEpo(request);
        flagEpo.setOriginalBean(ent);
        flagEpo.setDataAccessObject(vreq.getFullWebappDaoFactory().getIndividualDao());
        request.setAttribute("_flagEpoKey",flagEpo.getKey());
        
        if (vreq.getFullWebappDaoFactory().getApplicationDao().isFlag1Active()) {
        	request.setAttribute("isFlag1Active",true);
	        PortalDao pDao = vreq.getFullWebappDaoFactory().getPortalDao();
	        HashSet indPortalSet = new HashSet();
	        if (ent.getFlag1Set() != null) {
	            String[] indPortal = ent.getFlag1Set().split(",");
	            for (int i=0; i<indPortal.length; i++) {
	                try {
	                    int portalId = Integer.decode(indPortal[i]);
	                    indPortalSet.add(portalId);
	                } catch (NumberFormatException nfe) {}
	            }
	        }
	        List<Checkbox> portalCheckboxList = new ArrayList<Checkbox>();
	        Collection<Portal> allPortals = pDao.getAllPortals();
	        if (allPortals != null) {
	            Iterator<Portal> portalIt = allPortals.iterator();
	            while (portalIt.hasNext()) {
	                Portal p = portalIt.next();
	                if (p.getPortalId() < MIN_SHARED_PORTAL_ID) {
	                    Checkbox checkbox = new Checkbox();
	                    checkbox.setValue(Integer.toString(p.getPortalId()));
	                    checkbox.setBody(p.getAppName());
	                    checkbox.setChecked( (indPortalSet.contains(p.getPortalId())) ? true : false );
	                    portalCheckboxList.add(checkbox);
	                }
	            }
	        }
	        foo.getCheckboxLists().put("portalFlag", portalCheckboxList);
       	} else {
       		request.setAttribute("isFlag1Active",false);
       	}

        if (vreq.getFullWebappDaoFactory().getApplicationDao().isFlag2Active()) {
        	try {
	        	request.setAttribute("isFlag2Active",true);
		        List<Checkbox> flag2CheckboxList = new ArrayList<Checkbox>();
		        Set<String> flag2ValueSet = new HashSet<String>();
		        String[] flag2Values = ent.getFlag2Set().split(",");
		        for (int ii = 0; ii<flag2Values.length; ii++) {
		            flag2ValueSet.add(flag2Values[ii]);
		        }
		        List<String> keyList = new ArrayList<String>();
		        keyList.addAll(((WebappDaoFactoryJena) vreq.getFullWebappDaoFactory()).getFlag2ValueMap().keySet());
		        Collections.sort(keyList);
		        for (Iterator<String> i = keyList.iterator(); i.hasNext(); ) {
		            String value = i.next();
		            Checkbox cb = new Checkbox();
		            cb.setValue(value);
		            cb.setBody(value);
		            if (flag2ValueSet.contains(value)) {
		                cb.setChecked(true);
		            }
		            flag2CheckboxList.add(cb);
		        }
		        foo.getCheckboxLists().put("flag2", flag2CheckboxList);
        	} catch (Exception e) {
        		log.error("Unable to set up flag2 checkboxes");
        	}
        } else {
        	request.setAttribute("isFlag2Active", false);
        }
        
        List<Option> existingKeywordRelations = new LinkedList();
        KeywordIndividualRelationDao kirDao = vreq.getFullWebappDaoFactory().getKeys2EntsDao();
        KeywordDao kDao = vreq.getFullWebappDaoFactory().getKeywordDao();
        List kirs = kirDao.getKeywordIndividualRelationsByIndividualURI(ent.getURI());
        if (kirs != null) {
            Iterator kirIt = kirs.iterator();
            while (kirIt.hasNext()) {
                KeywordIndividualRelation kir = (KeywordIndividualRelation) kirIt.next();
                if (kir.getKeyId() > 0) {
                    Keyword k = kDao.getKeywordById(kir.getKeyId());
                    if (k != null) {
                        Option kOpt = new Option();
                        kOpt.setValue(kir.getURI());
                        kOpt.setBody(k.getTerm()+" ("+kir.getMode()+")");
                        existingKeywordRelations.add(kOpt);
                    }
                }

            }
        }
        foo.getOptionLists().put("existingKeywordRelations",existingKeywordRelations);

        epo.setFormObject(foo);

        request.setAttribute("curatorNoteURI",VitroVocabulary.CURATOR_NOTE);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("entityWebapp", ent);
        request.setAttribute("bodyJsp","/templates/edit/specific/ents_edit.jsp");
        request.setAttribute("portalBean",portal);
        request.setAttribute("title","Individual Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");
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
