/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class ApplicationDaoJena extends JenaBaseDao implements ApplicationDao {

    private static final Property LINKED_NAMESPACE_PROP =
            ResourceFactory.createProperty(
                    VitroVocabulary.DISPLAY + "linkedNamespace");

	Integer portalCount = null;
	List<String> externallyLinkedNamespaces = null;

    public ApplicationDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    private String getApplicationResourceURI() {
    	// TODO migrate to "application" in the resource URI
    	return super.DEFAULT_NAMESPACE + "portal" + 1;
    }

    public ApplicationBean getApplicationBean() {
    	ApplicationBean application = new ApplicationBean();
    	OntModel ontModel = getOntModelSelector().getApplicationMetadataModel();
    	Individual appInd = ontModel.getIndividual(
    			getApplicationResourceURI());
    	if (appInd == null) {
    		return application;
    	}
    	ontModel.enterCriticalSection(Lock.READ);
    	try {
    		String appName = appInd.getLabel(null);
    		if (appName != null) {
    			application.setApplicationName(appName);
    		} // else leave as default
	        application.setAboutText(getPropertyStringValue(
	        		appInd, APPLICATION_ABOUTTEXT));
	        application.setAcknowledgeText(getPropertyStringValue(
	        		appInd, APPLICATION_ACKNOWLEGETEXT));
	        application.setContactMail(getPropertyStringValue(
	        		appInd, APPLICATION_CONTACTMAIL));
	        application.setCorrectionMail(getPropertyStringValue(
	        		appInd, APPLICATION_CORRECTIONMAIL));
	        application.setCopyrightAnchor(getPropertyStringValue(
	        		appInd, APPLICATION_COPYRIGHTANCHOR));
            application.setCopyrightURL(getPropertyStringValue(
            		appInd, APPLICATION_COPYRIGHTURL));
            application.setThemeDir(getPropertyStringValue(
            		appInd, APPLICATION_THEMEDIR));
        } catch (Exception e) {
    		log.error(e, e);
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
        return application;
    }

    public void updateApplicationBean(ApplicationBean application) {
    	// TODO migrate to "application" in the resource URI
    	OntModel ontModel = getOntModelSelector().getApplicationMetadataModel();
    	Individual appInd = ontModel.getIndividual(
    			getApplicationResourceURI());
    	if (appInd == null) {
    		appInd = ontModel.createIndividual(
    				getApplicationResourceURI(), PORTAL);
    	}
    	ontModel.enterCriticalSection(Lock.WRITE);
    	try {
    		appInd.setLabel(application.getApplicationName(), null);
	        updatePropertyStringValue(
	        		appInd, APPLICATION_ABOUTTEXT, application.getAboutText(),
	        		    ontModel);
	        updatePropertyStringValue(
	        		appInd, APPLICATION_ACKNOWLEGETEXT,
	        		    application.getAcknowledgeText(), ontModel);
	        updatePropertyStringValue(
	        		appInd, APPLICATION_CONTACTMAIL,
	        		    application.getContactMail(), ontModel);
	        updatePropertyStringValue(
	        		appInd, APPLICATION_CORRECTIONMAIL,
	        		    application.getCorrectionMail(), ontModel);
	        updatePropertyStringValue(
	        		appInd, APPLICATION_COPYRIGHTANCHOR,
	        		    application.getCopyrightAnchor(), ontModel);
            updatePropertyStringValue(
            		appInd, APPLICATION_COPYRIGHTURL,
            		    application.getCopyrightURL(), ontModel);
            updatePropertyStringValue(
            		appInd, APPLICATION_THEMEDIR,
            		    application.getThemeDir(), ontModel);
        } catch (Exception e) {
    		log.error(e, e);
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    }

    public void close() {
            // nothing to do right now
    }

	private static final boolean CLEAR_CACHE = true;

	@Override
	public synchronized List<String> getExternallyLinkedNamespaces() {
	    return getExternallyLinkedNamespaces(!CLEAR_CACHE);
	}

    private synchronized List<String> getExternallyLinkedNamespaces(boolean clearCache) {
        if (clearCache || externallyLinkedNamespaces == null) {
            externallyLinkedNamespaces = new ArrayList<String>();
            OntModel ontModel = getOntModelSelector().getDisplayModel();
            NodeIterator nodes = ontModel.listObjectsOfProperty(LINKED_NAMESPACE_PROP);
            while (nodes.hasNext()) {
                RDFNode node = nodes.next();
                if (node.isLiteral()) {
                    String namespace = ((Literal)node).getLexicalForm();
                    // org.openrdf.model.impl.URIImpl.URIImpl.getNamespace() returns a
                    // namespace with a final slash, so this makes matching easier.
                    // It also accords with the way the default namespace is defined.
                    if (!namespace.endsWith("/")) {
                        namespace += "/";
                    }
                    externallyLinkedNamespaces.add(namespace);
                }
            }
        }
        return externallyLinkedNamespaces;
    }

    public boolean isExternallyLinkedNamespace(String namespace) {
        List<String> namespaces = getExternallyLinkedNamespaces();
        return namespaces.contains(namespace);
    }

}
