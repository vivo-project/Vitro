/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 11:18:28 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TabDao {

    void addParentTab(Tab tab, Tab parent);

    void addParentTab(String tabURI, String parentURI);

    void removeParentTab(Tab tab, Tab parent);

    void removeParentTab(String tabURI, String parentURI);

    List<Tab> getParentTabs(Tab tab);

    List<Tab> getParentTabs(String tabURI);

    List<Tab> getChildTabs(Tab tab);

    List<Tab> getChildTabs(String tabURI);

//  void addAutolinkedVClass(Tab tab, VClass vclass);
//
//  void addAutolinkedVClass(String tabURI, String vclassURI);
//
//  void removeAutolinkedVClass(Tab tab, VClass vclass);
//
//  void removeAutolinkedVClass(String tabURI, String vclassURI);
//
//  List /* of Tab */ getAutolinkedVClasses(Tab tab);
//
//  List /* of Tab */ getAutolinkedVClasses(String tabURI);



    Tab getTab(int tab_id, int auth_level, ApplicationBean app);

    Tab getTab(int tab_id, int auth_level, ApplicationBean app, int depth );

    int insertTab(Tab tab);

    void updateTab(Tab tab);

    void deleteTab(Tab tab);

    Tab getTab(int tab_id);

    List <String> getTabAutoLinkedVClassURIs(int tab_id);

    List <String> getTabManuallyLinkedEntityURIs(int tab_id);

    Tab getTabByName(String tabName);

    List getPrimaryTabs(int portalId );

    List getSecondaryTabs(int primaryTabId);

    List getTabsForPortal(int portalId);

    List getTabsForPortalByTabtypes(int portalId, boolean direction, int tabtypeId);

    int cloneTab(int tabId) throws Exception;

    String getNameForTabId(int tabId);

    List getTabHierarchy(int tabId, int rootTab);

    int getRootTabId(int portalId);

    List<Tab> getAllAutolinkableTabs(int portalId);

    List<Tab> getAllManuallyLinkableTabs(int portalId);


}
