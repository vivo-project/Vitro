/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.util.JSONUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;

public class ListFauxPropertiesController extends FreemarkerHttpServlet {

	private static final Log log = LogFactory.getLog(ListFauxPropertiesController.class.getName());
	
    private static final String TEMPLATE_NAME = "siteAdmin-fauxPropertiesList.ftl";

    private ObjectPropertyDao opDao = null;
    private PropertyGroupDao pgDao = null;
    private FauxPropertyDao fpDao = null;
	private String notFoundMessage = "";
	
    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTION;
	}
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        try {

            String displayOption = "";
            
            if ( vreq.getParameter("displayOption") != null ) {
                displayOption = vreq.getParameter("displayOption");
            }
            else {
                displayOption = "listing";
            }
            body.put("displayOption", displayOption);
            
            if ( displayOption.equals("listing") ) {
                body.put("pageTitle", "Faux Property Listing");
            }
            else {
                body.put("pageTitle", "Faux Properties by Base Property");
            }
                        
            opDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getObjectPropertyDao();
			fpDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getFauxPropertyDao();
			pgDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getPropertyGroupDao();

			List<ObjectProperty> objectProps = null;
            objectProps = opDao.getRootObjectProperties();

			Map<String, Object> allFauxProps = new TreeMap<String, Object>();
			// get the faux depending on the display option 
			if ( displayOption.equals("listing") ) {
                allFauxProps = getFauxPropertyList(objectProps);
            }
			else {
				allFauxProps = getFauxByBaseList(objectProps);
			}
			
			log.debug(allFauxProps.toString());

			if ( notFoundMessage.length() == 0 ) {
				body.put("message", notFoundMessage);
			}
			else {
            	body.put("fauxProps", allFauxProps);
			}
                    
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new TemplateResponseValues(TEMPLATE_NAME, body);

    }

	private TreeMap<String, Object> getFauxPropertyList(List<ObjectProperty> objectProps) {
		List<FauxProperty> fauxProps = null;
		TreeMap<String, Object> theFauxProps = new TreeMap<String, Object>();
        if ( objectProps != null ) {	
            Iterator<ObjectProperty> opIt = objectProps.iterator();
            if ( !opIt.hasNext()) {
                notFoundMessage = "No object properties found."; 
            } 
			else {
                while (opIt.hasNext()) {
					
                    ObjectProperty op = opIt.next();
					String baseURI = op.getURI();
                    fauxProps = fpDao.getFauxPropertiesForBaseUri(baseURI);
					if ( fauxProps != null ) {
						Iterator<FauxProperty> fpIt = fauxProps.iterator();
						if ( !fpIt.hasNext()) {
							notFoundMessage = "No faux properties found."; 
						}
						else {
							while (fpIt.hasNext()) {
								// No point in getting these unless we have a faux property
								String baseLabel = getDisplayLabel(op) == null ? "(no name)" : getDisplayLabel(op);
								String baseLocalName = op.getLocalNameWithPrefix();
								baseLabel = baseLabel.substring(0,baseLabel.indexOf("("));
								baseLabel += "(" + baseLocalName + ")";
								// get the info we need from the faux property
								FauxProperty fp = fpIt.next();
								String fauxLabel = fp.getDisplayName();
								String rangeLabel = fp.getRangeLabel();
								String rangeURI = fp.getRangeURI();
								String domainLabel = fp.getDomainLabel();
								String domainURI = fp.getDomainURI();
								String groupURI = fp.getGroupURI();
								// FauxProperty only gets groupURI but we want the label
								PropertyGroup pGroup = pgDao.getGroupByURI(groupURI);
								String groupLabel = ( pGroup == null ) ? "unspecified" : pGroup.getName();
								// store all the strings in a hash with the faux property label as the key
								Map<String, Object> tmpHash = new HashMap<String, Object>();
								tmpHash.put("base", baseLabel);
								tmpHash.put("baseURI", baseURI);
								tmpHash.put("group", groupLabel);
								tmpHash.put("range", rangeLabel);
								tmpHash.put("rangeURI", rangeURI);
								tmpHash.put("domain", domainLabel);
								tmpHash.put("domainURI", domainURI);
								// add the faux and its details to the treemap	
								theFauxProps.put(fauxLabel + "@@" + domainLabel, tmpHash);
							} 
						} 
					}
            	}	
            }
        }
        return theFauxProps;
	}

	private TreeMap<String, Object> getFauxByBaseList(List<ObjectProperty> objectProps) {
		List<FauxProperty> fauxProps = null;
		TreeMap<String, Object> fauxByBaseProps = new TreeMap<String, Object>();
        if ( objectProps != null ) {	
            Iterator<ObjectProperty> opIt = objectProps.iterator();
            if ( !opIt.hasNext()) {
                notFoundMessage = "No object properties found."; 
            } 
			else {
                while (opIt.hasNext()) {
					TreeMap<String, Object> fauxForGivenBase = new TreeMap<String, Object>();
                    ObjectProperty op = opIt.next();
					String baseURI = op.getURI();
                    fauxProps = fpDao.getFauxPropertiesForBaseUri(baseURI);

					if ( fauxProps != null ) {
						Iterator<FauxProperty> fpIt = fauxProps.iterator();
						if ( !fpIt.hasNext()) {
							notFoundMessage = "No faux properties found."; 
						}
						else {
							String baseLabel = getDisplayLabel(op) == null ? "(no name)" : getDisplayLabel(op);
							String baseLocalName = op.getLocalNameWithPrefix();
							baseLabel = baseLabel.substring(0,baseLabel.indexOf("("));
							baseLabel += "(" + baseLocalName + ")" + "|" + baseURI;
							while (fpIt.hasNext()) {
								// get the info we need from the faux property
								FauxProperty fp = fpIt.next();
								String fauxLabel = fp.getDisplayName();
								String rangeLabel = fp.getRangeLabel();
								String rangeURI = fp.getRangeURI();
								String domainLabel = fp.getDomainLabel();
								String domainURI = fp.getDomainURI();
								String groupURI = fp.getGroupURI();
								// FauxProperty only gets groupURI but we want the label
								PropertyGroup pGroup = pgDao.getGroupByURI(groupURI);
								String groupLabel = ( pGroup == null ) ? "unspecified" : pGroup.getName();
								// store all the strings in a hash with the faux property label as the key
								Map<String, Object> tmpHash = new HashMap<String, Object>();
								tmpHash.put("baseURI", baseURI);
								tmpHash.put("group", groupLabel);
								tmpHash.put("range", rangeLabel);
								tmpHash.put("rangeURI", rangeURI);
								tmpHash.put("domain", domainLabel);
								tmpHash.put("domainURI", domainURI);
								// add the faux and its details to the treemap	
								fauxForGivenBase.put(fauxLabel + "@@" + domainLabel, tmpHash);
							}
							 fauxByBaseProps.put(baseLabel, fauxForGivenBase);
						} 
					}
            	}	
            }
        }
        return fauxByBaseProps;
	}

    /*
     * should never be null
     */
    public static String getDisplayLabel(ObjectProperty op) {
        String displayLabel = op.getPickListName();
    	displayLabel = (displayLabel != null && displayLabel.length() > 0)  
			? displayLabel 
			: op.getLocalName();
		return (displayLabel != null) ? displayLabel : "[object property]" ;
    }

}