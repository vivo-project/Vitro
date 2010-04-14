/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class PortalDaoJena extends JenaBaseDao implements PortalDao {    
    
    public PortalDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @SuppressWarnings("unchecked")
    public boolean isSinglePortal(){
    	try {
    		getOntModel().enterCriticalSection(Lock.READ);
	        Iterator portalIt = getOntModel().listIndividuals(PORTAL);
	        int count=0;
	        while( portalIt.hasNext()){
	            if( portalIt.next() != null )
	                count++;                
	            if( count > 1 )        
	                break;      
	        } 
	        return  count <= 1;
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    }
    
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getApplicationMetadataModel();
    }
    
    public void deletePortal(Portal portal) {
        deletePortal(portal.getPortalId());
    }

    public void deletePortal(int portalId) {
    	try {
    		getOntModel().enterCriticalSection(Lock.WRITE);
    		Individual p = getOntModel().getIndividual(super.DEFAULT_NAMESPACE+"portal"+portalId);
    		if (p != null)
    			p.remove();
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    }

    public Collection<Portal> getAllPortals() {
        List<Portal> portals = new ArrayList<Portal>();
        try {
        	getOntModel().enterCriticalSection(Lock.READ);
	        Iterator portalIt = getOntModel().listIndividuals(PORTAL);
	        while (portalIt.hasNext()) {
	            portals.add(portalFromPortalIndividual((Individual)portalIt.next()));
	        }
        } finally {
        	getOntModel().leaveCriticalSection();
        }
        Collections.sort(portals);
        return portals;
    }

    public Portal getPortal(int id) {
    	try {
    		getOntModel().enterCriticalSection(Lock.READ);
    		Individual portalInd = getOntModel().getIndividual(super.DEFAULT_NAMESPACE+"portal"+id);
    		return (portalInd==null) ? null : portalFromPortalIndividual(portalInd);
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    }

    public Portal getPortalByURI(String uri){
        if( uri == null ) return null;
        try {
        	getOntModel().enterCriticalSection(Lock.READ);
	        Individual portalInd = getOntModel().getIndividual(uri);
	        return portalFromPortalIndividual(portalInd);
        } finally {
        	getOntModel().leaveCriticalSection();
        }
    }

    public int insertPortal(Portal portal) throws InsertException {
    	String portalURIStr = DEFAULT_NAMESPACE + "portal" + portal.getPortalId();
    	try {
    		getOntModel().enterCriticalSection(Lock.WRITE);
	    	Individual portalInd = getOntModel().getIndividual(portalURIStr);
	    	if (portalInd != null) {
	    		throw new InsertException ("Portal "+portal.getPortalId()+" already exists.");
	    	}
	    	portalInd = getOntModel().createIndividual(portalURIStr,PORTAL);
	    	updatePortalIndividualUsingPortalBean(portalInd,portal);
	    	return portal.getPortalId();
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    }
    
    public void updatePortal(Portal portal) {
    	updatePortal(portal,getOntModel());
    }

    private void updatePortalIndividualUsingPortalBean(Individual ind, Portal portal) {
    	OntModel ontModel = ind.getOntModel();
        if (ind != null) {
            ind.setLabel(portal.getAppName(), (String) getDefaultLanguage());
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_SHORTHAND), portal.getShortHand(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_CONTACTMAIL), portal.getContactMail(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_CORRECTIONMAIL), portal.getCorrectionMail(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_THEMEDIR), portal.getThemeDir(), ontModel);
            updatePropertyNonNegativeIntValue(ind, ontModel.getProperty(VitroVocabulary.DISPLAY_RANK), portal.getDisplayRank(), ontModel);
            updatePropertyNonNegativeIntValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_IMAGETHUMBWIDTH), portal.getImageThumbWidth(), ontModel);
            updatePropertyNonNegativeIntValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_BANNERWIDTH), portal.getBannerWidth(), ontModel);
            updatePropertyNonNegativeIntValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_BANNERHEIGHT), portal.getBannerHeight(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_BANNERIMAGE), portal.getBannerImage(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_LOGOTYPEIMAGE), portal.getLogotypeImage(), ontModel);
            updatePropertyNonNegativeIntValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_LOGOTYPEWIDTH), portal.getLogotypeWidth(), ontModel);
            updatePropertyNonNegativeIntValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_LOGOTYPEHEIGHT), portal.getLogotypeHeight(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_COPYRIGHTURL), portal.getCopyrightURL(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_COPYRIGHTANCHOR), portal.getCopyrightAnchor(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_ROOTBREADCRUMBURL), portal.getRootBreadCrumbURL(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_ROOTBREADCRUMBANCHOR), portal.getRootBreadCrumbAnchor(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_ABOUTTEXT), portal.getAboutText(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_ACKNOWLEGETEXT), portal.getAcknowledgeText(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_URLPREFIX), portal.getUrlprefix(), ontModel);
            updatePropertyStringValue(ind, ontModel.getProperty(VitroVocabulary.PORTAL_FLAG1FILTERING), portal.isFlag1Filtering()?"true":"false", ontModel);
            OntProperty rootTab = ontModel.getOntProperty(VitroVocabulary.PORTAL_ROOTTAB);
            Individual theRootTab = ontModel.getIndividual(DEFAULT_NAMESPACE+"tab"+portal.getRootTabId());
            if (rootTab != null && theRootTab != null) {
                ind.removeAll(rootTab);
                ind.addProperty(rootTab,theRootTab);
            }
        }
    }
    
    public void updatePortal(Portal portal, OntModel ontModel) {
    	try {
    		ontModel.enterCriticalSection(Lock.WRITE);
    		Individual ind = ontModel.getIndividual(DEFAULT_NAMESPACE+"portal"+portal.getPortalId());
    		updatePortalIndividualUsingPortalBean(ind, portal);
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    }

    private Portal portalFromPortalIndividual(Individual portalInd) {
        Portal portal = new Portal();
        if (portalInd == null) {
            portal.setPortalId(1);
            portal.setAppName("Vitro");
        }
        try {
            portal.setPortalId(Integer.decode(portalInd.getLocalName().substring(6)));
            portal.setAppName(portalInd.getLabel(null));
        } catch (Exception e) {}
        try {
            portal.setAboutText(((Literal)(portalInd.getProperty(PORTAL_ABOUTTEXT).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setAcknowledgeText(((Literal)(portalInd.getProperty(PORTAL_ACKNOWLEGETEXT).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setBannerHeight(Integer.decode(((Literal)(portalInd.getProperty(PORTAL_BANNERHEIGHT).getObject())).getString()).intValue());
        } catch (Exception e) {}
        try {
            portal.setBannerImage(((Literal)(portalInd.getProperty(PORTAL_BANNERIMAGE).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setBannerWidth(Integer.decode(((Literal)(portalInd.getProperty(PORTAL_BANNERWIDTH).getObject())).getString()).intValue());
        } catch (Exception e) {}
        try {
            portal.setContactMail(((Literal)(portalInd.getProperty(PORTAL_CONTACTMAIL).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setCorrectionMail(((Literal)(portalInd.getProperty(PORTAL_CORRECTIONMAIL).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setCopyrightAnchor(((Literal)(portalInd.getProperty(PORTAL_COPYRIGHTANCHOR).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setCopyrightURL(((Literal)(portalInd.getProperty(PORTAL_COPYRIGHTURL).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setDisplayRank(Integer.decode(((Literal)(portalInd.getProperty(DISPLAY_RANK).getObject())).getString()).intValue());
        } catch (Exception e) {}
        portal.setUrlprefix(getPropertyStringValue(portalInd,PORTAL_URLPREFIX));
        // TODO: flag1SearchFilters
        // TODO: flag2SearchFilters
        // TODO: flag3SearchFilters
        try {
            portal.setImageThumbWidth(Integer.decode(((Literal)(portalInd.getProperty(PORTAL_IMAGETHUMBWIDTH).getObject())).getString()).intValue());
        } catch (Exception e) {}
        try {
            portal.setLogotypeHeight(Integer.decode(((Literal)(portalInd.getProperty(PORTAL_LOGOTYPEHEIGHT).getObject())).getString()).intValue());
        } catch (Exception e) {}
        try {
            portal.setLogotypeWidth(Integer.decode(((Literal)(portalInd.getProperty(PORTAL_LOGOTYPEWIDTH).getObject())).getString()).intValue());
        } catch (Exception e) {}
        try {
            portal.setLogotypeImage(((Literal)(portalInd.getProperty(PORTAL_LOGOTYPEIMAGE).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setRootBreadCrumbAnchor(((Literal)(portalInd.getProperty(PORTAL_ROOTBREADCRUMBANCHOR).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setRootBreadCrumbURL(((Literal)(portalInd.getProperty(PORTAL_ROOTBREADCRUMBURL).getObject())).getString());
        } catch (Exception e) {}
        try {
            Resource rootTabRes = (Resource) portalInd.getProperty(PORTAL_ROOTTAB).getObject();
            portal.setRootTabId(Integer.decode(rootTabRes.getLocalName().substring(3)).intValue());
        } catch (Exception e) {}
        try {
            portal.setShortHand(((Literal)(portalInd.getProperty(PORTAL_SHORTHAND).getObject())).getString());
        } catch (Exception e) {}
        try {
            portal.setThemeDir(((Literal)(portalInd.getProperty(PORTAL_THEMEDIR).getObject())).getString());
        } catch (Exception e) {}
        try {
            String filtering = ((Literal)(portalInd.getProperty(PORTAL_FLAG1FILTERING).getObject())).getString();
            portal.setFlag1Filtering(filtering);
        } catch (Exception e) {}
        portal.setDisplayRank(portal.getPortalId()); // TODO: un-hack this
        return portal;
    }

}
