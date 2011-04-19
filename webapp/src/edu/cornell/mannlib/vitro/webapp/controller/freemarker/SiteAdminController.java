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

import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseIndividualEditorPages;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseOntologyEditorPages;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseSiteAdminPage;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseSiteInfoEditingPage;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.listing.AllTabsForPortalListingController;
import edu.cornell.mannlib.vitro.webapp.controller.edit.listing.PortalsListingController;
import edu.cornell.mannlib.vitro.webapp.controller.edit.listing.UsersListingController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;

@RequiresAuthorizationFor(UseSiteAdminPage.class)
public class SiteAdminController extends FreemarkerHttpServlet {
	
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(SiteAdminController.class);
    private static final String TEMPLATE_DEFAULT = "siteAdmin-main.ftl";
    
    @Override
	public String getTitle(String siteName, VitroRequest vreq) {
        return siteName + " Site Administration";
	}

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();        

        UrlBuilder urlBuilder = new UrlBuilder(vreq.getPortal());
        
    	if (PolicyHelper.isAuthorizedForAction(vreq, UseIndividualEditorPages.class)) {
    		body.put("dataInput", getDataInputData(vreq));
    	}

        body.put("siteConfig", getSiteConfigurationData(vreq, urlBuilder));

        // rjy7 There is a risk that the login levels required to show the links will get out
        // of step with the levels required by the pages themselves. We should implement a 
        // mechanism similar to what's used on the front end to display links to Site Admin
        // and Revision Info iff the user has access to those pages.
        if (PolicyHelper.isAuthorizedForAction(vreq, UseOntologyEditorPages.class)) {
        	body.put("ontologyEditor", getOntologyEditorData(vreq, urlBuilder));
        }
		if (PolicyHelper.isAuthorizedForAction(vreq, UseAdvancedDataToolsPages.class)) {
            body.put("dataTools", getDataToolsData(vreq, urlBuilder));
            
            // Only for DataStar. Should handle without needing a DataStar-specific version of this controller.
            //body.put("customReports", getCustomReportsData(vreq));
        }
        
        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
 
    }
    
    private Map<String, Object> getDataInputData(VitroRequest vreq) {
    
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("formAction", UrlBuilder.getUrl("/edit/editRequestDispatch.jsp"));
        
        WebappDaoFactory wadf = vreq.getFullWebappDaoFactory();
        
        // Create map for data input entry form options list
        List classGroups = wadf.getVClassGroupDao().getPublicGroupsWithVClasses(true,true,false); // order by displayRank, include uninstantiated classes, don't get the counts of individuals
        
        //boolean classGroupDisplayAssumptionsMet = checkClassGroupDisplayAssumptions(classGroups);   
        
        Set<String> seenGroupNames = new HashSet<String>();
        
        Iterator classGroupIt = classGroups.iterator();
        LinkedHashMap<String, List> orderedClassGroups = new LinkedHashMap<String, List>(classGroups.size());
        while (classGroupIt.hasNext()) {
            VClassGroup group = (VClassGroup)classGroupIt.next();            
            List opts = FormUtils.makeOptionListFromBeans(group.getVitroClassList(),"URI","PickListName",null,null,false);
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
        return map;
    }
    
    private Map<String, Object> getSiteConfigurationData(VitroRequest vreq, UrlBuilder urlBuilder) {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, String> urls = new HashMap<String, String>();

		if (PolicyHelper.isAuthorizedForServlet(vreq, AllTabsForPortalListingController.class)) {
			urls.put("tabs", urlBuilder.getPortalUrl("/listTabs"));
		}
        
        if (PolicyHelper.isAuthorizedForServlet(vreq, UsersListingController.class)) {                
            urls.put("users", urlBuilder.getPortalUrl("/listUsers"));
        }

        if (PolicyHelper.isAuthorizedForServlet(vreq, PortalsListingController.class)) {
        	if ((!vreq.getFullWebappDaoFactory().getPortalDao().isSinglePortal())) {
				urls.put("portals", urlBuilder.getPortalUrl("/listPortals"));
			}
		}
 
		if (PolicyHelper.isAuthorizedForAction(vreq, UseSiteInfoEditingPage.class)) {
			urls.put("siteInfo", urlBuilder.getPortalUrl("/editForm", new ParamMap("controller", "Portal", "id", String.valueOf(urlBuilder.getPortalId()))));
		}

		if (PolicyHelper.isAuthorizedForServlet(vreq, MenuN3EditController.class)) {
            urls.put("menuN3Editor", urlBuilder.getPortalUrl("/menuN3Editor"));            
        }
        
        map.put("urls", urls);
        
        return map;
    }
    
    private Map<String, Object> getOntologyEditorData(VitroRequest vreq, UrlBuilder urlBuilder) {

        Map<String, Object> map = new HashMap<String, Object>();
 
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
        
        urls.put("ontologies", urlBuilder.getPortalUrl("/listOntologies"));
        urls.put("classHierarchy", urlBuilder.getPortalUrl("/showClassHierarchy"));
        urls.put("classGroups", urlBuilder.getPortalUrl("/listGroups"));
        urls.put("dataPropertyHierarchy", urlBuilder.getPortalUrl("/showDataPropertyHierarchy"));
        urls.put("propertyGroups", urlBuilder.getPortalUrl("/listPropertyGroups"));            
        urls.put("objectPropertyHierarchy", urlBuilder.getPortalUrl("/showObjectPropertyHierarchy", new ParamMap("iffRoot", "true")));
        map.put("urls", urls);
        
        return map;
    }

    private Map<String, Object> getDataToolsData(VitroRequest vreq, UrlBuilder urlBuilder) {

        Map<String, Object> map = new HashMap<String, Object>();
        
        Map<String, String> urls = new HashMap<String, String>();
        urls.put("ingest", UrlBuilder.getUrl("/ingest"));
        urls.put("rdfData", urlBuilder.getPortalUrl("/uploadRDFForm"));
        urls.put("rdfExport", urlBuilder.getPortalUrl("/export"));
        urls.put("sparqlQuery", UrlBuilder.getUrl("/admin/sparqlquery"));
        urls.put("sparqlQueryBuilder", UrlBuilder.getUrl("/admin/sparqlquerybuilder"));
        map.put("urls", urls);
        
        return map;
    }

    /*
     * There is a problem where labels are being used as keys for a map of 
     * classgroups that gets passed to the templates. This representation isn't an accurate 
     * reflection of the model since classgroups could have two labels, multiple classgroups 
     * could have the same label, and a classgroup could have no lables.
     * 
     *  Check the assumptions and use the URIs as the key if the assumptions are not
     *  meet. see issue NIHVIVO-1635.
     */
//    private boolean checkClassGroupDisplayAssumptions( List<VClassGroup> groups){
//        //Assumption A: all of the classgroups have a non-null rdfs:label
//        //Assumption B: none of the classgroups have the same rdfs:label
//        //the assumption that all classgroups have only one rdfs:label is not checked
//        boolean rvalue = true;
//        Set<String> seenPublicNames = new HashSet<String>();
//        
//        for( VClassGroup group :groups ){
//            //check Assumption A
//            if( group.getPublicName() == null){
//                rvalue = false;
//                break;
//            }
//                            
//            //check Assumption B
//            if( seenPublicNames.contains(group.getPublicName()) ){
//                rvalue = false;
//                break;
//            }
//            seenPublicNames.add(group.getPublicName());            
//        }
//        
//        
//        if( !rvalue )
//            log.error("The rdfs:labels on the classgroups in the system do " +
//                    "not meet the display assumptions.  Falling back to alternative.");
//        return rvalue;
//    }

}
