/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabEntityFactory;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory.TabEntityFactoryAutoJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory.TabEntityFactoryGalleryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory.TabEntityFactoryManualJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory.TabEntityFactoryMixedJena;

public class TabDaoJena extends JenaBaseDao implements TabDao {

    public TabDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
           try {
                   entityLinkMethods = new HashMap();
                   entityLinkMethods.put("auto",TAB_AUTOLINKABLETAB);
                   entityLinkMethods.put("manual",TAB_MANUALLYLINKABLETAB);
                   List mixedTypes = new LinkedList();
                   mixedTypes.add(TAB_MIXEDTAB);
                   mixedTypes.add(TAB_AUTOLINKABLETAB);
                   mixedTypes.add(TAB_MANUALLYLINKABLETAB);
                   entityLinkMethods.put("mixed", mixedTypes);
                   tabtypes = new HashMap();
                   tabtypes.put(18, TAB_SUBCOLLECTIONCATEGORY);
                   tabtypes.put(20, TAB_SUBCOLLECTION);
                   tabtypes.put(22, TAB_COLLECTION);
                   tabtypes.put(24, TAB_SECONDARYTAB);
                   tabtypes.put(26, TAB_PRIMARYTABCONTENT);
                   tabtypes.put(28, TAB_PRIMARYTAB);
               } catch (Exception e) {
                   log.error("error constructing convenience HashMaps in TabDaoJena() constructor "+e.getStackTrace());
               }
    }

    private static final Log log = LogFactory.getLog(TabDaoJena.class.getName());

    /* constant to use when calling recursiveAddChildTabs to get all children */
    final static int NO_DEPTH_LIMIT = -1;

    /*  gets portal id from portal's uri */
    Pattern portalIdFromUriPattern = Pattern.compile("^.*portal(.*)$");

    private HashMap entityLinkMethods = null;
    private HashMap tabtypes = null;

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getApplicationMetadataModel();
    }
    
    private class TabAlphabetizer implements java.util.Comparator {
        public int compare (Object o1, Object o2) {
            return (((Tab)o1).getTitle()).compareTo(((Tab)o2).getTitle());
        }
    }

    public void addParentTab(Tab tab, Tab parent) {
    	addParentTab(tab,parent,getOntModel());
    }

    public void addParentTab(Tab tab, Tab parent, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource childRes = ontModel.getResource(DEFAULT_NAMESPACE+"tab"+tab.getTabId());
            Resource parentRes = ontModel.getResource(DEFAULT_NAMESPACE+"tab"+parent.getTabId());
            if (childRes != null && parentRes != null && TAB_SUBTABOF != null) {
                ontModel.add(childRes, TAB_SUBTABOF, parentRes);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public void addParentTab(String tabURI, String parentURI) {

    }

    public void removeParentTab(Tab tab, Tab parent) {
    	removeParentTab(tab,parent,getOntModel());
    }

    public void removeParentTab(Tab tab, Tab parent, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource childRes = ontModel.getResource(DEFAULT_NAMESPACE+"tab"+tab.getTabId());
            Resource parentRes = ontModel.getResource(DEFAULT_NAMESPACE+"tab"+parent.getTabId());
            if (childRes != null && parentRes != null && TAB_SUBTABOF != null) {
                ontModel.remove(childRes, TAB_SUBTABOF, parentRes);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public void removeParentTab(String tabURI, String parentURI) {

    }

    public List<Tab> getParentTabs(Tab tab) {
        return getParentTabs(DEFAULT_NAMESPACE+"tab"+tab.getTabId());
    }

    public List<Tab> getParentTabs(String tabURI) {
        List<Tab> parentList = new ArrayList<Tab>();
        com.hp.hpl.jena.ontology.Individual tabInd = getOntModel().getIndividual(tabURI);
        ClosableIterator stmtIt = getOntModel().listStatements(tabInd, TAB_SUBTABOF, (Resource)null);
        try {
            while (stmtIt.hasNext()) {
                Statement stmt = (Statement) stmtIt.next();
                Resource parentRes = (Resource) stmt.getObject();
                if (parentRes != null) {
                    com.hp.hpl.jena.ontology.Individual parentInd = getOntModel().getIndividual(parentRes.getURI());
                    parentList.add(tabFromTabIndividual(parentInd));
                }
            }
        } finally {
            stmtIt.close();
        }
        return parentList;
    }

    public List<Tab> getChildTabs(Tab tab) {
        return getChildTabs(DEFAULT_NAMESPACE+"tab"+tab.getTabId());
    }

    public List<Tab> getChildTabs(String tabURI) {
        List<Tab> childList = new ArrayList<Tab>();
        com.hp.hpl.jena.ontology.Individual tabInd = getOntModel().getIndividual(tabURI);
        ClosableIterator stmtIt = getOntModel().listStatements(null, TAB_SUBTABOF, tabInd);
        try {
            while (stmtIt.hasNext()) {
                Statement stmt = (Statement) stmtIt.next();
                Resource childRes = stmt.getSubject();
                if (childRes != null) {
                    com.hp.hpl.jena.ontology.Individual childInd = getOntModel().getIndividual(childRes.getURI());
                    childList.add(tabFromTabIndividual(childInd));
                }
            }
        } finally {
            stmtIt.close();
        }
        return childList;
    }

    public int cloneTab(int tabId) throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }

    public void deleteTab(Tab tab) {
    	deleteTab(tab,getOntModel());
    }

    public void deleteTab(Tab tab, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Individual tabInd = ontModel.getIndividual(DEFAULT_NAMESPACE+"tab"+tab.getTabId());
            if (tabInd != null)
                tabInd.remove();
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public List<Tab> getAllAutolinkableTabs(int portalId) {
        List<Tab> tabs = new ArrayList<Tab>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator tabIt = getOntModel().listIndividuals(TAB_AUTOLINKABLETAB);
            try {
                while (tabIt.hasNext()) {
                        tabs.add(tabFromTabIndividual((Individual) tabIt.next()));
                }
            } finally {
                tabIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return tabs;
    }

    public List<Tab> getAllManuallyLinkableTabs(int portalId) {
        List<Tab> tabs = new LinkedList<Tab>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator tabIt = getOntModel().listIndividuals(TAB_MANUALLYLINKABLETAB);
            try {
                while (tabIt.hasNext()) {
                        tabs.add(tabFromTabIndividual((Individual) tabIt.next()));
                }
            } finally {
                tabIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return tabs;
    }

    /**
     *  returns a list of URI strings of VClasses auto-affiliated with tab (tabId)
     */
    public List<String> getTabAutoLinkedVClassURIs(int tabId) {
        List<String> typeURIs = new LinkedList<String>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Resource tab = getOntModel().getResource(DEFAULT_NAMESPACE+"tab"+tabId);
            if (tab != null && TAB_AUTOLINKEDTOTAB != null) {
                ClosableIterator typeIt = getOntModel().listStatements(null, TAB_AUTOLINKEDTOTAB, tab);
                try {
                    while (typeIt.hasNext()) {
                        Statement st = (Statement) typeIt.next();
                        Resource type = st.getSubject();
                        if (type != null) {
                            typeURIs.add(type.getURI());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    typeIt.close();
                }
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return typeURIs;
    }

    /**
     * returns a list of URI strings of Entities manually linked to tab (tabId)
     */
    public List<String> getTabManuallyLinkedEntityURIs(int tabId) {
        List<String> entityURIs = new LinkedList<String>();
//        getOntModel().enterCriticalSection(Lock.READ);
//        try {
//            Resource tab = getOntModel().getResource(DEFAULT_NAMESPACE+"tab"+tabId);
//            if (tab != null && TAB_LINKEDENTITY != null) {
//                ClosableIterator entityIt = getOntModel().listStatements(tab, TAB_LINKEDENTITY, (Resource)null);
//                try {
//                    while (entityIt.hasNext()) {
//                        Statement st = (Statement) entityIt.next();
//                        Resource entity = (Resource) st.getObject();
//                        if (entity != null) {
//                            entityURIs.add(entity.getURI());
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    entityIt.close();
//                }
//            }
//        } finally {
//            getOntModel().leaveCriticalSection();
//        }
        return entityURIs;
    }

    public String getNameForTabId(int tabId) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Individual tabInd = getOntModel().getIndividual(DEFAULT_NAMESPACE+"tab"+tabId);
            if (tabInd != null) {
                return getLabelOrId(tabInd);
            } else {
                return null;
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

     /**
     * Get an ordered list of primary tabs for a given portal.
     */
    public List<Tab> getPrimaryTabs(int portalId){
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            List<Tab> primaryTabs = new LinkedList<Tab>();
            ClosableIterator tabIt = getOntModel().listIndividuals(TAB_PRIMARYTAB);
            try {
                while (tabIt.hasNext()) {
                    Individual tabInd = (Individual) tabIt.next();
                    ObjectProperty inPortal = TAB_PORTAL;
                    if (inPortal == null)
                        return null;
                    Statement stmt = tabInd.getProperty(inPortal);
                    if (stmt != null) {
	                    Resource portal = (Resource) stmt.getObject();
	                    if (portal != null) {
		                    int tabPortalId = (Integer.decode(portal.getLocalName().substring(6))).intValue();
		                    if (portalId == tabPortalId) {
		                        primaryTabs.add(tabFromTabIndividual(tabInd));
		                    }
	                    }
                    }
                }
            } finally {
                tabIt.close();
            }
            Collections.sort(primaryTabs, new TabComparator());
            return primaryTabs;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public int getRootTabId(int portalId) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Individual portalInd = getOntModel().getIndividual(DEFAULT_NAMESPACE+"portal"+portalId);
            if (portalInd == null)
                return -1;
            Resource rootTabResource = (Resource) portalInd.getProperty(getOntModel().getObjectProperty(VitroVocabulary.PORTAL_ROOTTAB)).getObject();
            if (rootTabResource == null)
                return -1;
            String id = rootTabResource.getLocalName().substring(3);
            return Integer.decode(id);
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public List getSecondaryTabs(int primaryTabId) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Individual primaryTabInd = getOntModel().getIndividual(DEFAULT_NAMESPACE+"tab"+primaryTabId);
            if (primaryTabInd != null) {
                ObjectProperty subTabOf = getOntModel().getObjectProperty(VitroVocabulary.TAB_SUBTABOF);
                if (subTabOf != null) {
                    List secondaryTabs = new ArrayList();
                    Iterator stmtIt = getOntModel().listStatements(null, subTabOf, primaryTabInd);
                    while (stmtIt.hasNext()) {
                        // I think there's a better way of doing the next line
                        Individual secondaryTabInd = getOntModel().getIndividual(((Resource)((Statement)stmtIt.next()).getSubject()).getURI());
                        if (secondaryTabInd != null) {
                            Iterator typesIt = secondaryTabInd.listRDFTypes(false);
                            while (typesIt.hasNext()) {
                                Resource type = (Resource) typesIt.next();
                                if (type.getURI().equals(VitroVocabulary.TAB_SECONDARYTAB)) {
                                    secondaryTabs.add(tabFromTabIndividual(secondaryTabInd));
                                    break;
                                }
                            }
                        }
                    }
                    Collections.sort(secondaryTabs, new TabComparator());
                    return secondaryTabs;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    private class TabComparator implements  Comparator {
        public int compare (Object o1, Object o2) {
            return ((Tab)o1).getDisplayRank()-((Tab)o2).getDisplayRank();
        }
    }

    /*************************************************************
     * This makes a new tab, sets up the TabEntityFactory
     * and gets all children tabs (excluding cycles).
     *
     *
    @param tab_id - int: tab to retrieve from database
    @param auth_level - int: if <0, don't check entity statusId;
    otherwise filter to entities whose statusId<=auth_level
    @param app - ApplicationBean with info about flags and time filtering.
    @return a tab object;
    **/
    public Tab getTab(int tab_id, int auth_level, ApplicationBean app) {
        Tab tab = getTabWithEntityFact(tab_id, auth_level,app);
        recursiveAddChildTabs(tab,null,auth_level,app ,NO_DEPTH_LIMIT);
        return tab;
    }

    /*************************************************************
     * This makes a new tab, sets up the TabEntityFactory
     * and gets all children tabs (excluding cycles).
     *
     *
    @param tab_id - int: tab to retrieve from database
    @param auth_level - int: if <0, don't check entity statusId;
    otherwise filter to entities whose statusId<=auth_level
    @param app - ApplicationBean with info about flags and time filtering.
    @param depth - depth of child tabs to get. 0==none, 1==first generation,
    2==child and grandchild tabs, etc.  passing -1 here will get all children.
    @return a tab object;
    */
    public Tab getTab(int tab_id, int auth_level, ApplicationBean app, int depth) {
        Tab tab = getTabWithEntityFact(tab_id, auth_level,app);
        recursiveAddChildTabs(tab,null,auth_level,app ,depth );
        return tab;
    }

    public Tab getTab(int tab_id) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Individual tabInd = getOntModel().getIndividual(DEFAULT_NAMESPACE+"tab"+tab_id);
            if (tabInd != null)
                return tabFromTabIndividual(tabInd);
            else
                return null;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    //doesn't seem to be used anywhere
    public Tab getTabByName(String tabName) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the tab hierarchy above a tab as a list of tab Ids.
     * Boradest tab id is first, requested tabId is last in list.
     * If you want the portals root tab to be the first in the list
     * you can put the id for that tab in rootTab. Otherwise the list
     * returned only goes up the first tab with no broader tab.
     * @param tabId
     * @param rootTab if > 0 prepend as root
     * @return List of Integer objects
     */
    public List getTabHierarchy(int tabId, int rootTab){
        List hier = new LinkedList();
        hier.add(0,new Integer(tabId));
        int current = tabId;
        Integer broader = null;
        boolean keepChecking = true;
        while ( keepChecking ){
            broader = getBroaderTabId( current );
            if( broader != null ){
                keepChecking = true;
                current = broader.intValue();
                hier.add(0,broader);//stick on front of list
            }else{
                keepChecking = false;
            }
        }

        //append root tab if it is not redundent or negative
        if(rootTab > 0 && rootTab != tabId && rootTab != current)
            hier.add(0,new Integer(rootTab));
        return hier;
    }

    private Integer getBroaderTabId(int tabId){
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Integer i = null;
            Individual narrowerTab = getOntModel().getIndividual(DEFAULT_NAMESPACE+"tab"+tabId);
            if (narrowerTab != null) {
                ObjectProperty subTabOf = getOntModel().getObjectProperty(VitroVocabulary.TAB_SUBTABOF);
                if (subTabOf != null) {
                    Iterator stmtIt = narrowerTab.listProperties(subTabOf);
                    if (stmtIt.hasNext()) {
                        return Integer.decode(((Resource)((Statement)stmtIt.next()).getObject()).getLocalName().substring(3));
                    }
                }
            }
            return i;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public List getTabsForPortal(int portalId) {
        List tabsForPortal = new ArrayList();
    	getOntModel().enterCriticalSection(Lock.READ);
        try {
            Individual thePortal = getOntModel().getIndividual(DEFAULT_NAMESPACE+"portal"+portalId);
            if (thePortal != null){
                Iterator stmtIt = getOntModel().listStatements(null, TAB_PORTAL, thePortal);
                while (stmtIt.hasNext()) {
                    Individual tabInd = (Individual)((Resource)((Statement)stmtIt.next()).getSubject()).as(Individual.class);
                    tabsForPortal.add(tabFromTabIndividual(tabInd));
                }
                try {
                    Collections.sort(tabsForPortal, new TabAlphabetizer());
                } catch (Exception e) {
                    log.error(this.getClass().getName()+".getTabsForPortal(): error sorting tab list");
                }
            }
            return tabsForPortal;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public List getTabsForPortalByTabtypes(int portalId, boolean direction,
            int tabtypeId) {
        List<Tab> filtered = new LinkedList<Tab>();
        Iterator allIt = this.getTabsForPortal(portalId).iterator();
        while (allIt.hasNext()) {
            Tab t = (Tab) allIt.next();
            if (direction) {
                if (t.getTabtypeId() > tabtypeId) {
                    filtered.add(t);
                }
            } else {
                if (t.getTabtypeId() < tabtypeId) {
                    filtered.add(t);
                }
            }
        }
        return filtered;
    }

    public int insertTab(Tab tab) {
    	return insertTab(tab, getOntModel());
    }

    public int insertTab(Tab tab, OntModel ontModel) {
        String nameStr = tab.getTitle();
        Individual tabInd = null;
        if (tab.getTabId() < 0)
            tab.setTabId(Math.abs((nameStr+tab.getPortalId()).hashCode()));
        boolean inserted=false;
        while (!inserted) {
            ontModel.enterCriticalSection(Lock.READ);
            Individual dupTestInd = null;
            try {
                dupTestInd = ontModel.getIndividual(DEFAULT_NAMESPACE+tab.getTabId());
            } finally {
                ontModel.leaveCriticalSection();
            }
            if (dupTestInd != null) {
                nameStr+="a";
                tab.setTabId(Math.abs((nameStr+tab.getPortalId()).hashCode()));
            } else {
                ontModel.enterCriticalSection(Lock.WRITE);
                try {
	                tabInd = ontModel.createIndividual(DEFAULT_NAMESPACE+"tab"+tab.getTabId(), this.TAB);
	                inserted = true;
                } finally {
                    ontModel.leaveCriticalSection();
                }
            }
        }
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            try {
                tabInd.setLabel(tab.getTitle(), (String) getDefaultLanguage());
            } catch (Exception e) {log.error("error setting label for "+tabInd.getURI());}
            addPropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.DESCRIPTION), tab.getDescription(), ontModel);
            addPropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_STATUSID), tab.getStatusId(), ontModel);
            addPropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_BODY), tab.getBody(), ontModel);
            addPropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.DISPLAY_RANK), tab.getDisplayRank(), ontModel);
            addPropertyIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_DAYLIMIT), tab.getDayLimit(), ontModel);
            addPropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_GALLERYROWS), tab.getGalleryRows(), ontModel);
            addPropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_GALLERYCOLS), tab.getGalleryCols(), ontModel);
            addPropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_MORETAG), tab.getMoreTag(), ontModel);
            addPropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_IMAGEWIDTH), tab.getImageWidth(), ontModel);
            addPropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_ENTITYSORTFIELD), tab.getEntitySortField(), ontModel);
            addPropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_ENTITYSORTDIRECTION), tab.getEntitySortDirection(), ontModel);
            addPropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_FLAG2MODE), tab.getFlag2Mode(), ontModel);
            addPropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_FLAG2SET), tab.getFlag2Set(), ontModel);
            try {
                Object o = entityLinkMethods.get(tab.getEntityLinkMethod());
                if (o instanceof List) {
                    for (Object type : (List) o) {
                        tabInd.addRDFType((Resource) type);
                    }
                } else {
                    tabInd.addRDFType((Resource) o);
                }
            } catch (Exception e) {log.error("error setting entityLinkType for tab "+tabInd.getURI());}
            try {
            	Resource tabTypeRes = (Resource)tabtypes.get(tab.getTabtypeId());
            	if (tabTypeRes != null) {
            		tabInd.addRDFType(tabTypeRes);
            	}
            } catch (Exception e) {log.error("error setting tabtype for tab "+tabInd.getURI());}
            if (tab.getPortalId() > 0) {
                try {
                    tabInd.addProperty(TAB_PORTAL, ontModel.getResource(DEFAULT_NAMESPACE+"portal"+tab.getPortalId()));
                } catch (Exception e) {log.error("error setting portal for tab "+tabInd.getURI());}
            }
            return tab.getTabId();
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public void updateTab(Tab tab) {
    	updateTab(tab,getOntModel());
    }

    public void updateTab(Tab tab, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            if (tab.getTabId() > -1) {
                Individual tabInd = ontModel.getIndividual(DEFAULT_NAMESPACE+"tab"+tab.getTabId());
                if (tabInd != null) {
                    try {
                        tabInd.setLabel(tab.getTitle(), (String) getDefaultLanguage());
                    } catch (Exception e) {log.error("Error updating title for tab "+tab.getTabId());}
                    updatePropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.DESCRIPTION), tab.getDescription(), ontModel);
                    updatePropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_STATUSID), tab.getStatusId(), ontModel);
                    updatePropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_BODY), tab.getBody(), ontModel);
                    updatePropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.DISPLAY_RANK), tab.getDisplayRank(), ontModel);
                    updatePropertyIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_DAYLIMIT), tab.getDayLimit(), ontModel);
                    updatePropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_GALLERYROWS), tab.getGalleryRows(), ontModel);
                    updatePropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_GALLERYCOLS), tab.getGalleryCols(), ontModel);
                    updatePropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_MORETAG), tab.getMoreTag(), ontModel);
                    updatePropertyNonNegativeIntValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_IMAGEWIDTH), tab.getImageWidth(), ontModel);
                    updatePropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_ENTITYSORTFIELD), tab.getEntitySortField(), ontModel);
                    updatePropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_ENTITYSORTDIRECTION), tab.getEntitySortDirection(), ontModel);
                    updatePropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_FLAG2MODE), tab.getFlag2Mode(), ontModel);
                    updatePropertyStringValue(tabInd, ontModel.getDatatypeProperty(VitroVocabulary.TAB_FLAG2SET), tab.getFlag2Set(), ontModel);
                    Iterator types = tabInd.listRDFTypes(false);
                    List typesToRemove = new ArrayList();
                    while (types.hasNext()) {
                        typesToRemove.add((Resource)types.next());
                    }
                    Iterator typesToRemoveIt = typesToRemove.iterator();
                    while (typesToRemoveIt.hasNext()) {
                        tabInd.removeRDFType((Resource)typesToRemoveIt.next());
                    }
                    tabInd.addRDFType(TAB);
                    try {
                        Object o = entityLinkMethods.get(tab.getEntityLinkMethod());
                        if (o instanceof List) {
                            for (Object type : (List) o) {
                                tabInd.addRDFType((Resource) type);
                            }
                        } else {
                            tabInd.addRDFType((Resource) o);
                        }
                    } catch (Exception e) {log.error("error setting entityLinkType for tab "+tabInd.getURI());}
                    try {
                    	Resource tabTypeRes = (Resource)tabtypes.get(tab.getTabtypeId());
                    	if (tabTypeRes != null) {
                    		tabInd.addRDFType(tabTypeRes);
                    	}
                    } catch (Exception e) {log.error("error setting tabtype for tab "+tabInd.getURI());}
                    if (tab.getPortalId() > 0) {
                        try {
                            tabInd.addProperty(TAB_PORTAL, ontModel.getResource(DEFAULT_NAMESPACE+"portal"+tab.getPortalId()));
                        } catch (Exception e) {log.error("error setting portal for tab "+tabInd.getURI());}
                    }
                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    private Tab tabFromTabIndividual(Individual tabInd) {
        OntModel om = getOntModel();
        Tab tab = new Tab();
        tab.setTabId(Integer.decode(tabInd.getLocalName().substring(3)).intValue());
        try {
            if (tabInd.getLabel(null) != null)
                tab.setTitle(tabInd.getLabel(null));
            else
                tab.setTitle(tabInd.getLocalName());
        } catch (Exception e) {log.error("Error in TabDaoJena.tabFromTabIndividual - setTitle");}
        tab.setDescription(getPropertyStringValue(tabInd, DESCRIPTION));
        tab.setStatusId(getPropertyNonNegativeIntValue(tabInd, TAB_STATUSID));
        tab.setBody(getPropertyStringValue(tabInd, TAB_BODY));
        tab.setDisplayRank(getPropertyNonNegativeIntValue(tabInd, DISPLAY_RANK));
        tab.setDayLimit(getPropertyNonNegativeIntValue(tabInd, TAB_DAYLIMIT));
        tab.setGalleryRows(getPropertyNonNegativeIntValue(tabInd, TAB_GALLERYROWS));
        tab.setGalleryCols(getPropertyNonNegativeIntValue(tabInd, TAB_GALLERYCOLS));
        tab.setMoreTag(getPropertyStringValue(tabInd, TAB_MORETAG));
        tab.setImageWidth(getPropertyNonNegativeIntValue(tabInd, TAB_IMAGEWIDTH));
        tab.setEntitySortField(getPropertyStringValue(tabInd, TAB_ENTITYSORTFIELD));
        tab.setEntitySortDirection(getPropertyStringValue(tabInd, TAB_ENTITYSORTDIRECTION));
        tab.setFlag2Mode(getPropertyStringValue(tabInd, TAB_FLAG2MODE));
        tab.setFlag2Set(getPropertyStringValue(tabInd, TAB_FLAG2SET));
        tab.setPortalId( makePortalIdFromIndividual( tabInd ) );
        try {
            Iterator typesIt = tabInd.listRDFTypes(false);
            while (typesIt.hasNext()) {
                Resource type = (Resource) typesIt.next();
                if (type.getURI().equals(VitroVocabulary.TAB_PRIMARYTAB)) {
                    tab.setTabtypeId(Tab.PRIMARY_TAB);
                } else
                if (type.getURI().equals(VitroVocabulary.TAB_PRIMARYTABCONTENT)) {
                    tab.setTabtypeId(Tab.PRIMARY_TAB_CONTENT);
                } else
                if (type.getURI().equals(VitroVocabulary.TAB_SECONDARYTAB)) {
                    tab.setTabtypeId(Tab.SECONDARY_TAB);
                } else
                if (type.getURI().equals(VitroVocabulary.TAB_SUBCOLLECTION)) {
                    tab.setTabtypeId(Tab.SUBCOLLECTION);
                } else
                if (type.getURI().equals(VitroVocabulary.TAB_COLLECTION)) {
                    tab.setTabtypeId(Tab.COLLECTION);
                } else
                if (type.getURI().equals(VitroVocabulary.TAB_SUBCOLLECTIONCATEGORY)) {
                    tab.setTabtypeId(Tab.CATEGORY);
                }
                if (type.getURI().equals(VitroVocabulary.TAB_MIXEDTAB)) {
                    tab.setEntityLinkMethod("mixed");
                } else
                if (!tab.getEntityLinkMethod().equals("mixed") && type.getURI().equals(VitroVocabulary.TAB_AUTOLINKABLETAB)) {
                    tab.setEntityLinkMethod("auto");
                } else
                if (!tab.getEntityLinkMethod().equals("mixed") && type.getURI().equals(VitroVocabulary.TAB_MANUALLYLINKABLETAB)) {
                    tab.setEntityLinkMethod("manual");
                }
            }
        } catch (Exception e) {log.error("Error in TabDaoJena.tabFromIndividual - setTabtypeId"); }
        return tab;
    }


    private int makePortalIdFromIndividual(Individual tabInd){
        int portalId = -8723;
        OntModel model = getOntModel();
        model.enterCriticalSection(Lock.READ);
        try{
            ClosableIterator stmts = model.listStatements( tabInd, TAB_PORTAL, (RDFNode)null);
            try{
                boolean portalFound = false;
                /* NOTICE: this use the portaId from the first ObjectPropertyStatement encountered */
                /* this might not be the correct thing to do if there are multiple portals associated
                /* with a tab */
                while( stmts.hasNext()){
                    Statement stmt = (Statement) stmts.next();
                    String portalUri = stmt.getObject().asNode().getURI();
                    Matcher match = portalIdFromUriPattern.matcher( portalUri);
                    if( match.matches() ){
                        portalId = Integer.parseInt( match.group( 1 ) );
                        portalFound = true;
                        break;
                    }
                }
                if( portalFound == false && log.isErrorEnabled() )
                    log.error("tab " + tabInd.getURI() + " is not associated with any portals");
            }   finally {
                stmts.close();
            }
        } finally {
            model.leaveCriticalSection();
        }
        return portalId;
    }

    /**
     * Gets a tab with its entity factory set up correctly.
     * The returned tab will have no child tabs.
     *
     * @param tab_id - id of the tab
     * @param auth_level - authorization level
     * @param app - state of the application
     * @return tab with entity factory, but no children.
     */
    private Tab getTabWithEntityFact(int tab_id, int auth_level, ApplicationBean app) {
        Tab tab = getTab(tab_id);

        //this is where the tab gets an object for finding associated Entities.
        assignTabEntityFactory( tab,  app, auth_level);
        return tab;
    }

    /**
     * Adds an object to the Tab which defines the method to get entity object
     * that are associated with the Tab.
     */
    private void assignTabEntityFactory( Tab tab,
            ApplicationBean appBean,
            int auth_level) {
        if( tab == null ) return;

        TabEntityFactory factory = null;
        String linkMethod = tab.getEntityLinkMethod();

        if( "auto".equalsIgnoreCase( linkMethod ) ){
            factory = new TabEntityFactoryAutoJena(tab, auth_level, appBean, getWebappDaoFactory());
        } else if ( "manual".equalsIgnoreCase( linkMethod ) ){
            factory = new TabEntityFactoryManualJena(tab, auth_level, appBean, getWebappDaoFactory());
        } else if( "mixed".equalsIgnoreCase( linkMethod )){
            factory = new TabEntityFactoryMixedJena(tab, auth_level, appBean, getWebappDaoFactory());
        }else{
            log.debug("TabDao.assignTabEntityFactory(): tab "+tab.getTabId()+" " +
                    "is UNKNOWN, LinkMethod:" + linkMethod + " tabTypeId: " + tab.getTabtypeId());
        }

        if (tab.getGalleryRows()>0 && tab.getGalleryCols()>0) {
            factory = new TabEntityFactoryGalleryJena(factory, tab, auth_level, appBean, getWebappDaoFactory());
        }

        tab.placeEntityFactory(factory);
    }

    /**
     * recursively add all child tabs.
     * @param depth -1 == no depth limit, 0== no children, 1 == direct children,
     * 2 == 2 generations of children, etc.
     */
    @SuppressWarnings("unchecked")
    private void recursiveAddChildTabs(Tab tab, Set visitedIds, int auth_level,
            ApplicationBean appBean, int depth ){
        if(tab == null || depth == 0 ) return;

        if(visitedIds == null )
            visitedIds = new HashSet();

        if(visitedIds.contains(String.valueOf(tab.getTabId())))
            return;

        visitedIds.add( String.valueOf(tab.getTabId()) );

        getChildTabs(tab, auth_level, appBean);
        if( tab.getChildTabs() != null ){
            Iterator it = tab.getChildTabs().iterator();
            while(it.hasNext()){
                int newDepth = depth - 1;
                if( depth == NO_DEPTH_LIMIT )
                    newDepth = NO_DEPTH_LIMIT;

                //make a new HashSet each call so we only avoid cycles in paths,
                //not duplicates in the tree.
                recursiveAddChildTabs((Tab)it.next(), new HashSet(visitedIds),
                        auth_level, appBean, newDepth );
            }
        }
    }

    /**
     * Gets the direct children of the tab.
     * Depending on level:
     * 28 primary tab,
     * 26 primary tab content,
     * 24 secondary tab,
     * 22 collection,
     * 20 subcollection,
     * 18 subcollection category
     *
     * primary tabs can have either secondary tabs (which are displayed without entities)
     * or content tabs, whose entities are displayed
     * look first for heading tabs
     */
    private void getChildTabs(Tab tab, int auth_level, ApplicationBean appBean){
        ObjectProperty subTabOf = getOntModel().getObjectProperty(VitroVocabulary.TAB_SUBTABOF);
        if (subTabOf == null) {
            log.error("cannot find property "+ VitroVocabulary.TAB_SUBTABOF);
            return;
        }
        Individual parentTabInd = getOntModel().getIndividual(DEFAULT_NAMESPACE+"tab"+tab.getTabId());
        if (parentTabInd == null)
            return;
        Iterator stmtIt = getOntModel().listStatements(null, subTabOf, parentTabInd);
        List childIds = new ArrayList();
        while (stmtIt.hasNext()) {
            Statement stmt = (Statement) stmtIt.next();
            Resource child = (Resource) stmt.getSubject();
            childIds.add(Integer.decode(child.getLocalName().substring(3)));
        }
        Iterator childIdIt = childIds.iterator();
        Tab childTab = null;
        List childTabList = null;
        while (childIdIt.hasNext()) {
            if (childTabList==null)
                childTabList=new ArrayList();

            int childTabId=((Integer)childIdIt.next());
            // do not pass alpha_qualifier down to next generation
            // may want to switch so can return non-null childErrorMessage with
            // otherwise successful return
            childTab=getTabWithEntityFact(childTabId,auth_level,appBean);
            if (childTab != null)
                childTabList.add(childTab);
        }
        if (childTabList != null)
            Collections.sort(childTabList,new TabComparator());
        tab.setChildTabs(childTabList);
    }

}
