/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.SeeIndividualEditingPanel;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class BaseIndividualTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(BaseIndividualTemplateModel.class);
    
    protected final Individual individual;
    protected final LoginStatusBean loginStatusBean;
    protected final VitroRequest vreq;
    
    protected GroupedPropertyList propertyList;
    
    private EditingPolicyHelper policyHelper;

    public BaseIndividualTemplateModel(Individual individual, VitroRequest vreq) {
        this.vreq = vreq;
        this.individual = individual;
        this.loginStatusBean = LoginStatusBean.getBean(vreq);
        // Needed for getting portal-sensitive urls. Remove if multi-portal support is removed.
        
        // If editing, create a helper object to check requested actions against policies
        if (isEditable()) {
            policyHelper = new EditingPolicyHelper(vreq);
        } 
    }
    
    protected boolean isVClass(String vClassUri) {
        boolean isVClass = individual.isVClass(vClassUri);  
        // If reasoning is asynchronous (under RDB), this inference may not have been made yet. 
        // Check the superclasses of the individual's vclass.
        SimpleReasoner simpleReasoner = (SimpleReasoner)getServletContext().getAttribute(SimpleReasoner.class.getName());
        if (!isVClass && simpleReasoner != null && simpleReasoner.isABoxReasoningAsynchronous()) { 
            log.debug("Checking superclasses to see if individual is a " + vClassUri + " because reasoning is asynchronous");
            List<VClass> directVClasses = individual.getVClasses(true);
            for (VClass directVClass : directVClasses) {
                VClassDao vcDao = vreq.getWebappDaoFactory().getVClassDao();
                List<String> superClassUris = vcDao.getAllSuperClassURIs(directVClass.getURI());
                if (superClassUris.contains(vClassUri)) {
                    isVClass = true;
                    break;
                }
            }
        }
        return isVClass;
    }
    
    /* Template properties */
    
    public String getProfileUrl() {
        return UrlBuilder.getIndividualProfileUrl(individual, vreq);        
    }

    // For image, we use the default list view and Individual methods to reconstruct the image
    // url from various triples. A custom list view would require that logic to be duplicated here.
    public String getImageUrl() {
        String imageUrl = individual.getImageUrl();
        return imageUrl == null ? null : getUrl(imageUrl);
    }

    // For image, we use the default list view and Individual methods to reconstruct the image
    // url from various triples. A custom list view would require that logic to be duplicated here.
    public String getThumbUrl() {
        String thumbUrl = individual.getThumbUrl();
        return thumbUrl == null ? null : getUrl(thumbUrl);
    } 

    // Used to create a link to generate the individual's rdf.
    public String getRdfUrl() {
        
        String individualUri = getUri();
        String profileUrl = getProfileUrl();
        boolean isUriInDefaultNamespace = UrlBuilder.isUriInDefaultNamespace(individualUri, vreq);
        return isUriInDefaultNamespace ? profileUrl + "/" + getLocalName() + ".rdf" 
                                       : UrlBuilder.addParams(profileUrl, "format", "rdfxml");
    }

    public GroupedPropertyList getPropertyList() {
        if (propertyList == null) {
            propertyList = new GroupedPropertyList(individual, vreq, policyHelper);
        }
        return propertyList;
    }
    
	/**
	 * This page is editable if the user is authorized to add a data property or
	 * an object property to the Individual being shown.
	 */
    public boolean isEditable() {
		AddDataPropStmt adps = new AddDataPropStmt(individual.getURI(),
				RequestActionConstants.SOME_URI,
				RequestActionConstants.SOME_LITERAL, null, null);
		AddObjectPropStmt aops = new AddObjectPropStmt(individual.getURI(),
				RequestActionConstants.SOME_URI,
				RequestActionConstants.SOME_URI);
    	return PolicyHelper.isAuthorizedForActions(vreq, new Actions(adps).or(aops));
    }
    
    public boolean getShowAdminPanel() {
    	return PolicyHelper.isAuthorizedForActions(vreq, new SeeIndividualEditingPanel());
    }
 
    /* rdfs:label needs special treatment, because it is not possible to construct a 
     * DataProperty from it. It cannot be handled the way the vitro links and vitro public image
     * are handled like ordinary ObjectProperty instances.
     */
    public NameStatementTemplateModel getNameStatement() {
        return new NameStatementTemplateModel(getUri(), vreq, policyHelper);
    }
    
    /* These methods simply forward to the methods of the wrapped individual. It would be desirable to 
     * implement a scheme for proxying or delegation so that the methods don't need to be simply listed here. 
     * A Ruby-style method missing method would be ideal. 
     * Update: DynamicProxy doesn't work because the proxied object is of type Individual, so we cannot
     * declare new methods here that are not declared in the Individual interface. 
     */
    
    public String getName() {           
        return individual.getName();
    }

    public Collection<String> getMostSpecificTypes() {
        ObjectPropertyStatementDao opsDao = vreq.getWebappDaoFactory().getObjectPropertyStatementDao();
        Map<String, String> types = opsDao.getMostSpecificTypesInClassgroupsForIndividual(getUri()); 
        return types.values();
    }

    public String getUri() {
        return individual.getURI();
    }
    
    public String getLocalName() {
        return individual.getLocalName();
    }   
   
// For testing dump
//    // Properties
//    public String getANullString() {
//        return null;
//    }    
//    public String getAnEmptyString() {
//        return "";
//    }    
//    public String[] getANullArray() {
//        return null;
//    }    
//    public String[] getAnEmptyArray() {
//        String[] a = {};
//        return a;
//    }    
//    public List<String> getANullList() {
//        return null;
//    }    
//    public List<String> getAnEmptyList() {
//        return Collections.emptyList();
//    }    
//    public Map<String, String> getANullMap() {
//        return null;
//    }    
//    public Map<String, String> getAnEmptyMap() {
//        return Collections.emptyMap();
//    }    
//    
//    // Methods
//    public String nullString() {
//        return null;
//    }    
//    public String emptyString() {
//        return "";
//    }    
//    public String[] nullArray() {
//        return null;
//    }    
//    public String[] emptyArray() {
//        String[] a = {};
//        return a;
//    }    
//    public List<String> nullList() {
//        return null;
//    }    
//    public List<String> emptyList() {
//        return Collections.emptyList();
//    }   
//    public Map<String, String> nullMap() {
//        return null;
//    }    
//    public Map<String, String> emptyMap() {
//        return Collections.emptyMap();
//    }
    
    
    /* Template methods */
 
    public String selfEditingId() {
        String id = null;
        String idMatchingProperty = 
            ConfigurationProperties.getBean(getServletContext()).getProperty("selfEditing.idMatchingProperty");
        if (! StringUtils.isBlank(idMatchingProperty)) {
            WebappDaoFactory wdf = vreq.getUnfilteredWebappDaoFactory();
            Collection<DataPropertyStatement> ids = 
                wdf.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(individual, idMatchingProperty);
            if (ids.size() > 0) {
                id = ids.iterator().next().getData();
            }
        }
        return id;
    }
    
    public String controlPanelUrl() {
        return UrlBuilder.getUrl(Route.INDIVIDUAL_EDIT, "uri", getUri());
    }
    
}
