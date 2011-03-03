/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class BaseIndividualTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(BaseIndividualTemplateModel.class);
    
    protected Individual individual;
    protected VitroRequest vreq;
    protected UrlBuilder urlBuilder;
    protected GroupedPropertyList propertyList = null;
    protected LoginStatusBean loginStatusBean = null;
    private EditingPolicyHelper policyHelper = null;

    public BaseIndividualTemplateModel(Individual individual, VitroRequest vreq) {
        this.individual = individual;
        this.vreq = vreq;
        this.loginStatusBean = LoginStatusBean.getBean(vreq);
        // Needed for getting portal-sensitive urls. Remove if multi-portal support is removed.
        this.urlBuilder = new UrlBuilder(vreq.getPortal());
        
        // If editing, create a helper object to check requested actions against policies
        if (isEditable()) {
            policyHelper = new EditingPolicyHelper(vreq, getServletContext());
        } 
    }
    
    protected boolean isVClass(String vClassUri) {
        boolean isVClass = individual.isVClass(vClassUri);  
        // If reasoning is asynchronous (under RDB), this inference may not have been made yet. 
        // Check the superclasses of the individual's vclass.
        if (!isVClass && SimpleReasoner.isABoxReasoningAsynchronous(getServletContext())) { 
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
    
    /* These methods perform some manipulation of the data returned by the Individual methods */
    
    public String getProfileUrl() {
        return UrlBuilder.getIndividualProfileUrl(individual, vreq.getWebappDaoFactory());
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
        
        URI uri = new URIImpl(individualUri);
        String namespace = uri.getNamespace();
        
        // Individuals in the default namespace
        // e.g., http://vivo.cornell.edu/individual/n2345/n2345.rdf
        // where default namespace = http://vivo.cornell.edu/individual/ 
        // Other individuals: http://some.other.namespace/n2345?format=rdfxml
        String defaultNamespace = vreq.getWebappDaoFactory().getDefaultNamespace();
        return (defaultNamespace.equals(namespace)) ? profileUrl + "/" + getLocalName() + ".rdf" 
                                                    : UrlBuilder.addParams(profileUrl, "format", "rdfxml");

    }
    
    public String getEditUrl() {
        return urlBuilder.getPortalUrl(Route.INDIVIDUAL_EDIT, "uri", getUri());
    }

    public GroupedPropertyList getPropertyList() {
        if (propertyList == null) {
            propertyList = new GroupedPropertyList(individual, vreq, policyHelper);
        }
        return propertyList;
    }
    
    public boolean isEditable() {
        // RY This will be improved later. What is important is not whether the user is a self-editor,
        // but whether he has editing privileges on this profile. This is just a crude way of determining
        // whether to even bother looking at the editing policies.
        return VitroRequestPrep.isSelfEditing(vreq) || loginStatusBean.isLoggedIn();            
    }
    
    public boolean getShowAdminPanel() {
        return loginStatusBean.isLoggedInAtLeast(LoginStatusBean.EDITOR);
    }
 
    /* rdfs:label needs special treatment, because it is not possible to construct a 
     * DataProperty from it. It cannot be handled the way the vitro links and vitro public image
     * are handled like ordinary ObjectProperty instances.
     */
    public DataPropertyStatementTemplateModel getNameStatement() {
        String propertyUri = VitroVocabulary.LABEL; // rdfs:label
        DataPropertyStatementTemplateModel dpstm = new DataPropertyStatementTemplateModel(getUri(), propertyUri, vreq, policyHelper);
        
        // If the individual has no rdfs:label, return the local name. It will not be editable (this replicates previous behavior;
        // perhaps we would want to allow a label to be added. But such individuals do not usually have their profiles viewed or
        // edited directly.
        if (dpstm.getValue() == null) {
            dpstm.setValue(getLocalName());
        }
        
        return dpstm;
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

    public String getMoniker() {
        return individual.getMoniker();
    }

    public String getUri() {
        return individual.getURI();
    }
    
    public String getLocalName() {
        return individual.getLocalName();
    }   
    
}
