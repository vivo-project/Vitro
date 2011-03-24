package edu.cornell.mannlib.vitro.webapp.dao.filtering;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.FiltersForTabs;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.tabFactory.TabEntityFactoryFiltering;

public class TabDaoFiltering extends BaseFiltering implements TabDao{

    final TabDao innerDao;
    final VitroFilters filters;
    private PortalDao innerPortalDao;    

    public TabDaoFiltering(TabDao tabDao, PortalDao portalDao, VitroFilters filters) {
        this.innerPortalDao = portalDao;
        this.innerDao = tabDao;        
        this.filters = filters;
    }

    public void addParentTab(Tab tab, Tab parent) {
        innerDao.addParentTab(tab, parent);
    }

    public void addParentTab(String tabURI, String parentURI) {
        innerDao.addParentTab(tabURI, parentURI);
    }

    public void removeParentTab(Tab tab, Tab parent) {
        innerDao.removeParentTab(tab, parent);
    }

    public void removeParentTab(String tabURI, String parentURI) {
        innerDao.removeParentTab(tabURI, parentURI);
    }

    public int insertTab(Tab tab) {
        return innerDao.insertTab(tab);
    }

    public void updateTab(Tab tab) {
        innerDao.updateTab(tab);
    }

    public int cloneTab(int tabId) throws Exception {
        return innerDao.cloneTab(tabId);
    }

    public void deleteTab(Tab tab) {
        innerDao.deleteTab(tab);
    }


    public List<Tab> getParentTabs(Tab tab) {
        return setupFilteringTabs(innerDao.getParentTabs(tab));
    }

    public List<Tab> getParentTabs(String tabURI) {
        return setupFilteringTabs(innerDao.getParentTabs(tabURI));
    }

    public List<Tab> getChildTabs(Tab tab) {
        return setupFilteringTabs(innerDao.getChildTabs(tab));
    }

    public List<Tab> getChildTabs(String tabURI) {
        return setupFilteringTabs(innerDao.getChildTabs(tabURI));
    }

    public List<Tab> getAllAutolinkableTabs(int portalId) {
        return setupFilteringTabs(filter(innerDao.getAllAutolinkableTabs(portalId),filters.getTabFilter()));
    }

    public List<Tab> getAllManuallyLinkableTabs(int portalId) {
        return setupFilteringTabs(filter(innerDao.getAllManuallyLinkableTabs(portalId),filters.getTabFilter()));
    }

    public String getNameForTabId(int tabId) {
        return innerDao.getNameForTabId(tabId);
    }

    public List getPrimaryTabs(int portalId) {
        return setupFilteringTabs(filter(innerDao.getPrimaryTabs(portalId),filters.getTabFilter()));
    }

    public int getRootTabId(int portalId) {
        return innerDao.getRootTabId(portalId);
    }

    public List getSecondaryTabs(int primaryTabId) {
        return filter(innerDao.getSecondaryTabs(primaryTabId),filters.getTabFilter());
    }

    public Tab getTab(int tab_id, int auth_level, ApplicationBean app) {
        int NO_DEPTH_LIMIT = -1;
        return this.getTab(tab_id, auth_level, app, NO_DEPTH_LIMIT);
    }

    /** note that sub tabs are not filtered */
    public Tab getTab(int tab_id, int auth_level, ApplicationBean app, int depth) {
        Tab t = innerDao.getTab(tab_id, auth_level, app, depth);
        if( t != null && filters.getTabFilter().fn(t))
            return setupFilteringTab(t);
        else
            return null;
    }

    public Tab getTab(int tab_id) {
        Tab t = innerDao.getTab(tab_id);
        if( t != null && filters.getTabFilter().fn(t))
            return setupFilteringTab(t);
        else
            return null;
    }

   public Tab getTabByName(String tabName) {
        Tab t = innerDao.getTabByName(tabName);
        if( t != null && filters.getTabFilter().fn(t))
            return setupFilteringTab(t);
        else
            return null;
    }

    /** note not currently filtered */
    public List getTabAutoLinkedVClassURIs(int tab_id) {
        return innerDao.getTabAutoLinkedVClassURIs(tab_id);
    }


    /** note not currently filtered */
    public List getTabHierarcy(int tabId, int rootTab) {
        return innerDao.getTabHierarcy(tabId, rootTab);
    }

    /** note not currently filtered */
    public List getTabManuallyLinkedEntityURIs(int tab_id) {
        return innerDao.getTabManuallyLinkedEntityURIs(tab_id);
    }

    public List getTabsForPortal(int portalId) {
        return setupFilteringTabs(filter(innerDao.getTabsForPortal(portalId),filters.getTabFilter()));
    }

    public List getTabsForPortalByTabtypes(int portalId, boolean direction,
            int tabtypeId) {
        return setupFilteringTabs(
                filter(innerDao.getTabsForPortalByTabtypes(portalId, direction, tabtypeId),
                        filters.getTabFilter()) );
   }

    /**
     * Setup the filtering of the TabEntityFactory for the given Tab.
     */
    private Tab setupFilteringTab(Tab in){
        return setupFilteringTabCarefully(in, new HashSet<Integer>());
    }

    private List<Tab> setupFilteringTabs( Collection<Tab> tabs ){
        if( tabs == null ) return null;
        LinkedList<Tab> tabsOut = new LinkedList<Tab>();
        Set<Integer> visitedTabIds = new HashSet<Integer>();
        for( Tab tab : tabs){
            tabsOut.add( setupFilteringTabCarefully( tab, visitedTabIds) );
        }
        return tabsOut;
    }

    private List<Tab> setupFilteringTabs(Collection<Tab> tabs, Set<Integer> visitedTabIds){
        if( tabs == null ) return null;
        LinkedList<Tab> tabsOut = new LinkedList<Tab>();
        for( Tab tab : tabs){
            tabsOut.add( setupFilteringTabCarefully( tab, visitedTabIds) );
        }
        return tabsOut;
    }

    /** Sets up filtering on tab and sub-tabs and keeps track of what tabs have
     * already been visited*/
    private Tab setupFilteringTabCarefully( Tab in, Set<Integer> visitedTabIds){
        if( visitedTabIds.contains( in.getTabId() )) {
            return in;
        } else {
            //set up TabEntityFactory for in tab
            if( in.grabEntityFactory() == null )
                return in;
            else{
                /* NOTICE: this does not use the individualFilter that was passed in the constructor
                   it uses one based on the parameters of the tab. */
                boolean ascendingSort = !"desc".equalsIgnoreCase(in.getEntitySortDirection());
                TabEntityFactoryFiltering filteringFact =
                        new TabEntityFactoryFiltering(
                                in.grabEntityFactory(),
                                 FiltersForTabs.getFilterForTab( in, innerPortalDao.getPortal(in.getPortalId())),
                                new VitroFilterUtils.EntitySortTransform( in.getEntitySortField(),ascendingSort));
                in.placeEntityFactory(filteringFact);
            }
        }
        //deal with children
        List<Tab> children = setupFilteringTabs( in.getChildTabs(), visitedTabIds);
        in.setChildTabs( children );

        return in;
    }


}