/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

/**
 * @version 1 2005-08-27
 * @authors Jon Corson-Rikert, Brian Caruso
 *
 * A tab is a browsing facet within the application. 
 * 
 * UPDATES
 * BDC 2005-12-02 : updated to work with refactored db
 * 2005-10-19 jc55   changed test on date of entity sunrise field to <= from < when looking at sunrise (a negative value in tab dayLimit)
 * 2005-09-13 jc55   Added ability for tabs to have a mix of autolinked and manual entries - search for "mixed"
 * 2005-09-01 jc55   Made entity.name the default sort for related entities (had been none)
 * 2005-08-31 jc55   First version posted 8/31/05
 */

import edu.cornell.mannlib.vitro.webapp.dao.TabEntityFactory;

import java.text.Collator;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
   Data Structure for a vitro display tab.
   Tab is based on jcr's bean.TabBean.
*/


public class Tab implements java.io.Serializable, Comparable<Tab> {  // class to hold a single tab's values

    private static final long serialVersionUID = 7042411727511586634L;
    // this next is a klugey way to deal with tab flags while we work out how to get the same functionality via relationships
    public  final static int TAB_FILTER_FLAG2    =2;
    public  final static int TAB_FILTER_FLAG3    =3;
    public  final static int TAB_FILTER_FLAG_BOTH=5;

    /* tab types, hardcoded from tabtypes table */
    public final static int UNSPECIFIED = 0;
    public final static int CATEGORY = 18;
    public final static int SUBCOLLECTION = 20;
    public final static int COLLECTION = 22;
    public final static int SECONDARY_TAB = 24;
    public final static int PRIMARY_TAB_CONTENT = 26;
    public final static int PRIMARY_TAB = 28;

    public Tab(int id){
        tabId=id;
    }

    public String getTabDepthName(){
        String name = "UNSPECIFIED";
        switch (getTabtypeId()){
        case PRIMARY_TAB: name = "primary"; break;

        case SECONDARY_TAB:
        case PRIMARY_TAB_CONTENT: name = "secondary"; break;

        case CATEGORY: name = "category"; break;
        case SUBCOLLECTION: name = "subcollection"; break;
        case COLLECTION: name = "collection"; break;

        default: name = "unknownTabType";
        }
        return name;
    }
/*  // system variables
    private boolean initialized;
    public boolean  isInitialized()             {return initialized;}
    public void     setInitialized(boolean val) {initialized=val;}

    private String  sessionIdStr;
    public void     getSessionIdStr()           {return sessionIdStr;}
    public void     setSessionIdStr(String val) {sessionIdStr=val;}
    */
    // identifiers
    private int     tabId=0;         // primary key
    public void     setTabId(int val) {tabId=val;}
    public int      getTabId()        {return tabId;}

    private int     tabtypeId=0;
//  0 unspecified, 28 primary tab,26 primary tab content,24 secondary tab,22 collection,20 subcollection,18 subcollection category
    public void     setTabtypeId(int val) {tabtypeId=val;}
    public int      getTabtypeId()        {return tabtypeId;}

    private int     userId=0;
    public void     setUserId(int val) {userId=val;}
    public int      getUserId()        {return userId;}

    private int     statusId=0;
    //  0=visible to all,1=anonymous guests,2=registered user,3=individual user,4=system,5=webmaster
    public void     setStatusId(int val) {statusId=val;}
    public int      getStatusId()        {return statusId;}

    // display controls
    private int     dayLimit = 0; // positive for days in the future, negative for days in the past
    public void     setDayLimit(int val) {dayLimit=val;}
    public int      getDayLimit()        {return dayLimit;}

    private int     galleryRows=0; // triggers display as a gallery; controls # of rows of images
    public void     setGalleryRows(int val) {galleryRows=val;}
    public int      getGalleryRows()        {return galleryRows;}

    private int     galleryCols=0; // controls number of columns of images
    public void     setGalleryCols(int val) {galleryCols=val;}
    public int      getGalleryCols()        {return galleryCols;}

    private int     imageWidth=150; // width of image display in pixels
    public void     setImageWidth(int val) {imageWidth=val;}
    public int      getImageWidth()        {return imageWidth;}

    private String  entitySortField=null; // which entity field to sort by; null assumes sort by entity name; timekey another frequent option
    public void     setEntitySortField(String val) {entitySortField=val;}
    public String   getEntitySortField()           {return entitySortField;}

    private String  entitySortDirection=null; // null for normal; "desc" for descending, as with timekey
    public void     setEntitySortDirection(String val) {entitySortDirection=val;}
    public String   getEntitySortDirection()           {return entitySortDirection;}

    // filter controls
    private String  flag2Set=null;
    public void     setFlag2Set(String val)  {flag2Set=val;}
    public String   getFlag2Set()            {return flag2Set;}

    private int     flag2Numeric=0; // calculate from flag2Set; not a database field
    public void     setFlag2Numeric(int val) {flag2Numeric=val;}
    public int      getFlag2Numeric()        {return flag2Numeric;}

    private String  flag2Mode="include"; // enum('include','omit')
    public void     setFlag2Mode(String val) {flag2Mode=val;}
    public String   getFlag2Mode()           {return flag2Mode;}

    private String  flag3Set=null;
    public void     setFlag3Set(String val)  {flag3Set=val;}
    public String   getFlag3Set()            {return flag3Set;}

    private int     flag3Numeric=0; // calculate from flag3Set; not a database field
    public void     setFlag3Numeric(int val) {flag3Numeric=val;}
    public int      getFlag3Numeric()        {return flag3Numeric;}

    private String  flag3Mode="include"; // enum('include','omit')
    public void     setFlag3Mode(String val) {flag3Mode=val;}
    public String   getFlag3Mode()           {return flag3Mode;}

    // content
    private String  title=null;
    public void     setTitle(String val)   {title=val;}
    public String   getTitle()             {return title;}

    private String  moreTag=null; // shorter or more informal version of title for use in "More ___________"
    public void     setMoreTag(String val) {moreTag=val;}
    public String   getMoreTag()           {return moreTag;}

    private String  description=null;
    public void     setDescription(String val) {description=val;}
    public String   getDescription()           {return description;}

    private String  body=null;
    public void     setBody(String val)    {body=val;}
    public String   getBody()              {return body;}

    private String  rssURL=null;
    public void     setRssURL(String val) {rssURL=val;}
    public String   getRssURL()           {return rssURL;}

    private int     displayRank = 0;
    public void     setDisplayRank(int dr)  {displayRank=dr;}
    public int      getDisplayRank()        {return displayRank;}

    /**
     * entityLinkMethod indicates how entities should be associated with this tab.
     * valid values are 'manual' 'auto' and 'mixed'
     */
    private String  entityLinkMethod="manual"; // enum('manual','auto','mixed')
    public void     setEntityLinkMethod(String val) {entityLinkMethod=val;}
    public String   getEntityLinkMethod()           {return entityLinkMethod;}

    private int     autolinkTypeCount=0; // a dynamic count of how many etypes are set to be linked to tab
    public void     setAutolinkTypeCount(int val)    {autolinkTypeCount=val;}
    public int      getAutolinkTypeCount()           {return autolinkTypeCount;}

    private int     alphaFilteredEntityCount=0; // a count of how many entities are linked to tab, using alpha filters
    public void     setAlphaFilteredEntityCount(int val)    {alphaFilteredEntityCount=val;}
    public int      getAlphaFilteredEntityCount()           {return alphaFilteredEntityCount;}

    private int     alphaUnfilteredEntityCount=0; // a count of how many entities are linked to tab, regardless of filters
    public void     setAlphaUnfilteredEntityCount(int val)    {alphaUnfilteredEntityCount=val;}
    public int      getAlphaUnfilteredEntityCount()           {return alphaUnfilteredEntityCount;}

    private Collection relatedEntityList=null; // an ArrayList of EntityBeans
    
    public void        setRelatedEntityList(Collection val) {relatedEntityList=val;}
    
    public Collection <Individual> getRelatedEntityList(String alpha){
    	if (grabEntityFactory()==null && relatedEntityList == null) 
    		return new LinkedList<Individual>(); // BJL23 added to avoid ugly NPEs when creating new portals    	
    	else if( alpha != null && grabEntityFactory() != null)
            return grabEntityFactory().getRelatedEntites(alpha);
        else if(relatedEntityList != null )
            return relatedEntityList;
        else
            return grabEntityFactory().getRelatedEntites(null);
    }
    
    public Collection <Individual>getRelatedEntities(){ return getRelatedEntityList(null); }

    private Collection childTabList=null;
    public void        setChildTabs(Collection <Tab> val) {childTabList=val;}
    public Collection  <Tab>getChildTabs()               {return childTabList;}

    private Collection<Tab> getChildTabList(int tabType){
        LinkedList rv = new LinkedList();
        if( getChildTabs() != null ){
            Iterator it = getChildTabs().iterator();
            while(it.hasNext()){
                Tab sub = (Tab)it.next();
                if( sub.getTabtypeId() == tabType){
                    rv.add(sub);
                }
            }
        }
        return rv;
    }

    private TabEntityFactory entityFactory = null;
    public TabEntityFactory grabEntityFactory() {
        return entityFactory;
    }
    public void placeEntityFactory(TabEntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    public String getEntityFactoryDesc(){
        if( grabEntityFactory() == null )
            return "entity factory for this tab is null";
        else
            return grabEntityFactory().toString();
    }

    public boolean isGallery() {
        if ( getGalleryCols() > 0 && getGalleryRows() > 0)
            return true;
        else
            return false;
    }

    /** Gets only children tabs of type SECONDARY_TAB. */
    public Collection<Tab> filterChildrenForSubtabs(){
        Collection rv = new LinkedList();
        rv.addAll(getChildTabList(SECONDARY_TAB));
        rv.addAll(getChildTabList(SUBCOLLECTION));
        rv.addAll(getChildTabList(COLLECTION));
        rv.addAll(getChildTabList(CATEGORY));
        return rv;
    }

    /** gets only children tabs of type PRIMARY_TAB_CONTENT. */
    public Collection <Tab>filterChildrenForContentTabs(){
        return getChildTabList(PRIMARY_TAB_CONTENT);
    }


    // required parameter-less constructor
    public Tab() {
        // identifiers
        tabId        = -1; // primary key of tabs table is auto_increment so this could be 0;
        tabtypeId    = 0;
        userId       = 0;
        statusId     = 0;

        // display controls
        dayLimit=0;
        galleryRows=0;
        galleryCols=0;
        imageWidth=150;
        entitySortField="name";
        entitySortDirection=null;

        // filter controls
        flag2Set=null;
        flag2Numeric=0;
        flag2Mode="include";
        flag3Set=null;
        flag3Numeric=0;
        flag3Mode="include";

        // content
        title="untitled";
        moreTag=null;
        description=null;
        body=null;
        rssURL=null;

        entityLinkMethod="auto";
        autolinkTypeCount=0;
        alphaFilteredEntityCount=0;
        alphaUnfilteredEntityCount=0;
        relatedEntityList=null;
        childTabList=null;
    }

    public String toString(){
        return "Tab id " + getTabId() + " children: " +
        (getChildTabs()!=null? getChildTabs().size(): "null");
    }

    public String toHTML() {
        String output = "<p>Tab:<ul>";
        output += "<li>tab id:               [" + tabId               + "]</li>";
        output += "<li>tabtype id:           [" + tabtypeId           + "]</li>";
        output += "<li>user id:              [" + userId              + "]</li>";
        output += "<li>day limit:            [" + dayLimit            + "]</li>";
        output += "<li>gallery rows:         [" + galleryRows         + "]</li>";
        output += "<li>gallery columns:      [" + galleryCols         + "]</li>";
        output += "<li>image width           [" + imageWidth          + "]</li>";
        output += "<li>entity sort field:    [" + entitySortField     + "]</li>";
        output += "<li>entity sort direction [" + entitySortDirection + "]</li>";
        output += "<li>flag2Set:             [" + flag2Set            + "]</li>";
        output += "<li>flag2Numeric:         [" + flag2Numeric        + "]</li>";
        output += "<li>flag2Mode:            [" + flag2Mode           + "]</li>";
        output += "<li>flag3Set:             [" + flag3Set            + "]</li>";
        output += "<li>flag3Numeric:         [" + flag3Numeric        + "]</li>";
        output += "<li>flag3Mode:            [" + flag3Mode           + "]</li>";
        output += "<li>title:                [" + title               + "]</li>";
        output += "<li>moreTag:              [" + moreTag             + "]</li>";
        output += "<li>description:          [" + description         + "]</li>";
        output += "<li>body:                 [" + body                + "]</li>";
        output += "<li>rssURL:               [" + rssURL              + "]</li>";
        output += "<li>entity link method:   [" + entityLinkMethod    + "]</li>";
        output += "<li>autolink type count:  [" + autolinkTypeCount   + "]</li>";
        output += "<li>alpha filtered entity count  [" + alphaFilteredEntityCount  + "]</li>";
        output += "<li>alpha UNfiltered entity count  [" + alphaUnfilteredEntityCount  + "]</li>";
        if (relatedEntityList!=null && relatedEntityList.size()>0) {
            output += "<li>related entities      [" + relatedEntityList.toString()    + "]</li>";
        }
        if (relatedEntityList!=null && relatedEntityList.size()>0) {
            output += "<li>related entities      [" + relatedEntityList.toString()    + "]</li>";
        }
        output += "</ul></p>";
        return output;
    }
    public boolean isManualLinked() {
        return "manual".equalsIgnoreCase(entityLinkMethod);
    }
    public boolean isAutoLinked(){
        return "auto".equalsIgnoreCase(entityLinkMethod);
    }
    public boolean isMixedLinked(){
        return "mixed".equalsIgnoreCase(entityLinkMethod);
    }
    
    // sort by title
    public int compareTo(Tab t2) {
        Collator collator = Collator.getInstance();
        if (t2 == null) {
        	   return 1;
        } else {
        	   return collator.compare(this.getTitle(),t2.getTitle());
        }
    }

}
