/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoBackEndEditing;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditSiteInformation;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageMenus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageProxies;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageUserAccounts;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.SeeSiteAdminPage;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.SeeStartupStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.search.controller.IndexController;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

public class BaseSiteAdminController extends FreemarkerHttpServlet {
	
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(BaseSiteAdminController.class);
    protected static final String TEMPLATE_DEFAULT = "siteAdmin-main.ftl";

    public static final Actions REQUIRED_ACTIONS = new Actions(new SeeSiteAdminPage());
    
    @Override
	protected Actions requiredActions(VitroRequest vreq) {
    	return REQUIRED_ACTIONS;
	}

	@Override
	public String getTitle(String siteName, VitroRequest vreq) {
        return siteName + " Site Administration";
	}

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        
        Map<String, Object> body = new HashMap<String, Object>();        

        body.put("dataInput", getDataInputData(vreq));
        body.put("siteConfig", getSiteConfigData(vreq));        
        body.put("indexCacheRebuild", getIndexCacheRebuildUrls(vreq));     
        body.put("ontologyEditor", getOntologyEditorData(vreq));
        body.put("dataTools", getDataToolsUrls(vreq));

        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
    }
    
    protected Map<String, String> getIndexCacheRebuildUrls(VitroRequest vreq) {
        
        Map<String, String> urls = new HashMap<String, String>();

        if (PolicyHelper.isAuthorizedForActions(vreq, new UseMiscellaneousAdminPages())) {

            urls.put("recomputeInferences", UrlBuilder.getUrl("/RecomputeInferences"));     
        
            urls.put("rebuildClassGroupCache", UrlBuilder.getUrl("/browse?clearcache=1"));
        }
        
		if (PolicyHelper.isAuthorizedForActions(vreq, IndexController.REQUIRED_ACTIONS)) {
			urls.put("rebuildSearchIndex", UrlBuilder.getUrl("/SearchIndex"));
		}
		
        return urls;
    }

    protected Map<String, Object> getDataInputData(VitroRequest vreq) {
    
        Map<String, Object> map = new HashMap<String, Object>();
        
        if (PolicyHelper.isAuthorizedForActions(vreq, new DoBackEndEditing())) {

            map.put("formAction", UrlBuilder.getUrl("/editRequestDispatch"));
            
            WebappDaoFactory wadf = vreq.getFullWebappDaoFactory();
            
            // Create map for data input entry form options list
            List<VClassGroup> classGroups = wadf.getVClassGroupDao().getPublicGroupsWithVClasses(true,true,false); // order by displayRank, include uninstantiated classes, don't get the counts of individuals
    
            Set<String> seenGroupNames = new HashSet<String>();
            
            Iterator<VClassGroup> classGroupIt = classGroups.iterator();
            LinkedHashMap<String, List<Option>> orderedClassGroups = new LinkedHashMap<String, List<Option>>(classGroups.size());
            while (classGroupIt.hasNext()) {
                VClassGroup group = classGroupIt.next();            
                List<Option> opts = FormUtils.makeOptionListFromBeans(group.getVitroClassList(),"URI","PickListName",null,null,false);
                if( seenGroupNames.contains(group.getPublicName() )){
                    //have a duplicate classgroup name, stick in the URI
                    orderedClassGroups.put(group.getPublicName() + " ("+group.getURI()+")", opts);
                }else if( group.getPublicName() == null ){
                    //have an unlabeled group, stick in the URI
                    orderedClassGroups.put("unnamed group ("+group.getURI()+")", opts);
                }else{
                    orderedClassGroups.put(group.getPublicName(),opts);
                    seenGroupNames.add(group.getPublicName());
                }             
            }
            
            map.put("groupedClassOptions", orderedClassGroups);
        }
        return map;
    }
    
    protected Map<String, Object> getSiteConfigData(VitroRequest vreq) {

        Map<String, Object> data = new HashMap<String, Object>();
        
        if (PolicyHelper.isAuthorizedForActions(vreq, new ManageUserAccounts())) {
        	data.put("userAccounts", UrlBuilder.getUrl("/accountsAdmin"));
        }
 
        if (PolicyHelper.isAuthorizedForActions(vreq, new ManageProxies())) {
        	data.put("manageProxies", UrlBuilder.getUrl("/manageProxies"));
        }
        
        if (PolicyHelper.isAuthorizedForActions(vreq, new EditSiteInformation())) {
            data.put("siteInfo", UrlBuilder.getUrl("/editForm", "controller", "ApplicationBean"));
        }
        
        if (PolicyHelper.isAuthorizedForActions(vreq, new ManageMenus())) {
            data.put("menuManagement", UrlBuilder.getUrl("/individual",
                    "uri", "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#DefaultMenu",
                    "switchToDisplayModel", "true"));
        }
        
        if (PolicyHelper.isAuthorizedForActions(vreq, new SeeStartupStatus())) {
        	data.put("startupStatus", UrlBuilder.getUrl("/startupStatus"));
        	data.put("startupStatusAlert", !StartupStatus.getBean(getServletContext()).allClear());
        }
        
        return data;
    }
    
    protected Map<String, Object> getOntologyEditorData(VitroRequest vreq) {

        Map<String, Object> map = new HashMap<String, Object>();
 
        if (PolicyHelper.isAuthorizedForActions(vreq, new EditOntology())) {
            
            String pelletError = null;
            String pelletExplanation = null;
            Object plObj = getServletContext().getAttribute("pelletListener");
            if ( (plObj != null) && (plObj instanceof PelletListener) ) {
                PelletListener pelletListener = (PelletListener) plObj;
                if (!pelletListener.isConsistent()) {
                    pelletError = "INCONSISTENT ONTOLOGY: reasoning halted.";
                    pelletExplanation = pelletListener.getExplanation();
                } else if ( pelletListener.isInErrorState() ) {
                    pelletError = "An error occurred during reasoning. Reasoning has been halted. See error log for details.";
                }
            }
    
            if (pelletError != null) {
                Map<String, String> pellet = new HashMap<String, String>();
                pellet.put("error", pelletError);
                if (pelletExplanation != null) {
                    pellet.put("explanation", pelletExplanation);
                }
                map.put("pellet", pellet);
            }
                    
            Map<String, String> urls = new HashMap<String, String>();
            
            urls.put("ontologies", UrlBuilder.getUrl("/listOntologies"));
            urls.put("classHierarchy", UrlBuilder.getUrl("/showClassHierarchy"));
            urls.put("classGroups", UrlBuilder.getUrl("/listGroups"));
            urls.put("dataPropertyHierarchy", UrlBuilder.getUrl("/showDataPropertyHierarchy"));
            urls.put("propertyGroups", UrlBuilder.getUrl("/listPropertyGroups"));            
            urls.put("objectPropertyHierarchy", UrlBuilder.getUrl("/showObjectPropertyHierarchy", new ParamMap("iffRoot", "true")));
            map.put("urls", urls);
        }
        
        return map;
    }

    protected Map<String, String> getDataToolsUrls(VitroRequest vreq) {

        Map<String, String> urls = new HashMap<String, String>();
        
        if (PolicyHelper.isAuthorizedForActions(vreq, new UseAdvancedDataToolsPages())) {            
            urls.put("ingest", UrlBuilder.getUrl("/ingest"));
            urls.put("rdfData", UrlBuilder.getUrl("/uploadRDFForm"));
            urls.put("rdfExport", UrlBuilder.getUrl("/export"));
            urls.put("sparqlQuery", UrlBuilder.getUrl("/admin/sparqlquery"));
            urls.put("sparqlQueryBuilder", UrlBuilder.getUrl("/admin/sparqlquerybuilder"));
        }
        
        return urls;
    }

}
