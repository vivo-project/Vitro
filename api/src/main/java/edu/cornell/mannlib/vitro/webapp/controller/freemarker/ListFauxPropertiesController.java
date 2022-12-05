/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "ListFauxPropertiesController", urlPatterns = {"/listFauxProperties"} )
public class ListFauxPropertiesController extends FreemarkerHttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(ListFauxPropertiesController.class.getName());

    private static final String TEMPLATE_NAME = "siteAdmin-fauxPropertiesList.ftl";

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
			List<ObjectProperty> objectProps = getOPDao(vreq).getRootObjectProperties();
            List<DataProperty> dataProps = getDPDao(vreq).getRootDataProperties();
            Map<String, Object> allFauxProps = new TreeMap<String, Object>();

			// get the faux depending on the display option
			if ( displayOption.equals("listing") ) {
                allFauxProps.putAll(getFauxPropertyList(objectProps, vreq));
                allFauxProps.putAll(getFauxDataPropertyList(dataProps, vreq));
            }
			else {
				allFauxProps.putAll(getFauxByBaseList(objectProps, vreq));
				allFauxProps.putAll(getFauxDataPropsByBaseList(dataProps, vreq));
			}

			log.debug(allFauxProps.toString());

			if ( notFoundMessage.length() == 0 ) {
				body.put("message", notFoundMessage);
			}
			else {
            	body.put("fauxProps", allFauxProps);
			}

        } catch (Throwable t) {
            log.error(t, t);
        }
        return new TemplateResponseValues(TEMPLATE_NAME, body);

    }

	private PropertyGroupDao getPGDao(VitroRequest vreq) {
		return vreq.getUnfilteredAssertionsWebappDaoFactory().getPropertyGroupDao();
	}

	private FauxPropertyDao getFPDao(VitroRequest vreq) {
		return vreq.getUnfilteredAssertionsWebappDaoFactory().getFauxPropertyDao();
	}

	private DataPropertyDao getDPDao(VitroRequest vreq) {
		return vreq.getUnfilteredAssertionsWebappDaoFactory().getDataPropertyDao();
	}

	private ObjectPropertyDao getOPDao(VitroRequest vreq) {
		return vreq.getUnfilteredAssertionsWebappDaoFactory().getObjectPropertyDao();
	}

	private TreeMap<String, Object> getFauxPropertyList(List<ObjectProperty> objectProps, VitroRequest vreq) {
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
                    fauxProps = getFPDao(vreq).getFauxPropertiesForBaseUri(baseURI);
					if ( fauxProps != null ) {
						Iterator<FauxProperty> fpIt = fauxProps.iterator();
						if ( !fpIt.hasNext()) {
							notFoundMessage = "No faux properties found.";
						}
						else {
							while (fpIt.hasNext()) {
								// No point in getting these unless we have a faux property
								String baseLabel = getBaseLabel(op, true);
								// get the info we need from the faux property
								FauxProperty fp = fpIt.next();
								String fauxLabel = fp.getDisplayName();
								String rangeLabel = fp.getRangeLabel();
								String rangeURI = fp.getRangeURI();
								String domainLabel = fp.getDomainLabel();
								String domainURI = fp.getDomainURI();
								String groupURI = fp.getGroupURI();
								// FauxProperty only gets groupURI but we want the label
								PropertyGroup pGroup = getPGDao(vreq).getGroupByURI(groupURI);
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
								tmpHash.put("editUrl", "propertyEdit");

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

	private TreeMap<String, Object> getFauxByBaseList(List<ObjectProperty> objectProps, VitroRequest vreq) {
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
                    fauxProps = getFPDao(vreq).getFauxPropertiesForBaseUri(baseURI);

					if ( fauxProps != null ) {
						Iterator<FauxProperty> fpIt = fauxProps.iterator();
						if ( !fpIt.hasNext()) {
							notFoundMessage = "No faux properties found.";
						}
						else {
							String baseLabel = getBaseLabel(op, true);
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
								PropertyGroup pGroup = getPGDao(vreq).getGroupByURI(groupURI);
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
								fauxForGivenBase.put("editUrl", "propertyEdit");
							}
							 fauxByBaseProps.put(baseLabel, fauxForGivenBase);
						}
					}
            	}
            }
        }
        return fauxByBaseProps;
	}
	
	private TreeMap<String, Object> getFauxDataPropertyList(List<DataProperty> objectProps, VitroRequest vreq) {
		List<FauxProperty> fauxProps = null;
		TreeMap<String, Object> theFauxProps = new TreeMap<String, Object>();
        if ( objectProps != null ) {
            Iterator<DataProperty> opIt = objectProps.iterator();
            if ( !opIt.hasNext()) {
                notFoundMessage = "No object properties found.";
            }
			else {
                while (opIt.hasNext()) {

                	DataProperty dp = opIt.next();
					String baseURI = dp.getURI();
                    fauxProps = getFPDao(vreq).getFauxPropertiesForBaseUri(baseURI);
					if ( fauxProps != null ) {
						Iterator<FauxProperty> fpIt = fauxProps.iterator();
						if ( !fpIt.hasNext()) {
							notFoundMessage = "No faux properties found.";
						}
						else {
							while (fpIt.hasNext()) {
								// No point in getting these unless we have a faux property
								String baseLabel = getBaseLabel(dp,false);
								// get the info we need from the faux property
								FauxProperty fp = fpIt.next();
								String fauxLabel = fp.getDisplayName();
								String rangeLabel = fp.getRangeLabel();
								String rangeURI = fp.getRangeURI();
								String domainLabel = fp.getDomainLabel();
								String domainURI = fp.getDomainURI();
								String groupURI = fp.getGroupURI();
								// FauxProperty only gets groupURI but we want the label
								PropertyGroup pGroup = getPGDao(vreq).getGroupByURI(groupURI);
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
								tmpHash.put("editUrl", "datapropEdit");
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

	private TreeMap<String, Object> getFauxDataPropsByBaseList(List<DataProperty> objectProps, VitroRequest vreq) {
		List<FauxProperty> fauxProps = null;
		TreeMap<String, Object> fauxByBaseProps = new TreeMap<String, Object>();
        if ( objectProps != null ) {
            Iterator<DataProperty> opIt = objectProps.iterator();
            if ( !opIt.hasNext()) {
                notFoundMessage = "No object properties found.";
            }
			else {
                while (opIt.hasNext()) {
					TreeMap<String, Object> fauxForGivenBase = new TreeMap<String, Object>();
					DataProperty dp = opIt.next();
					String baseURI = dp.getURI();
                    fauxProps = getFPDao(vreq).getFauxPropertiesForBaseUri(baseURI);

					if ( fauxProps != null ) {
						Iterator<FauxProperty> fpIt = fauxProps.iterator();
						if ( !fpIt.hasNext()) {
							notFoundMessage = "No faux properties found.";
						}
						else {
							String baseLabel = getBaseLabel(dp, true);
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
								PropertyGroup pGroup = getPGDao(vreq).getGroupByURI(groupURI);
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
								fauxForGivenBase.put("editUrl", "datapropEdit");
							}
							 fauxByBaseProps.put(baseLabel, fauxForGivenBase);
						}
					}
            	}
            }
        }
        return fauxByBaseProps;
	}

	private String getBaseLabel(Property property, boolean addUri) {
		String baseLabel = property.getPickListName();
		if (StringUtils.isEmpty(baseLabel)) {
			baseLabel = property.getLocalName();
		}
		if (StringUtils.isEmpty(baseLabel)) {
			baseLabel = "[property]";
		}
		String baseLocalName = property.getLocalNameWithPrefix();
		int indexOf = baseLabel.indexOf("(");
		if (indexOf > 0) {
			baseLabel = baseLabel.substring(0,indexOf);	
		}
		baseLabel += "(" + baseLocalName + ")";
		if (addUri) {
			baseLabel += "|" + property.getURI();
		}
		return baseLabel;
	}

}
