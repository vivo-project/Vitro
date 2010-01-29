package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/**
 * @version 2 2005-09-14
 * @author Brian Caruso, Jon Corson-Rikert
 *
 * UPDATES:
 * 2006-03-13   jcr   minor changes having to do with browse functionality; removing old commented out code and comments
 * 2005-10-19   jcr   added variables and methods to retrieve MAX PORTAL ID from database and use that and minSharedPortalId/maxSharedPortalId
 *                    to get rid of need for ALL CALS RESEARCH
 * 2005-09-14   bdc34 modified to initialize itself from database and store static instance in class
 * 2005-07-05   JCR   added onlyCurrent and onlyPublic to get rid of constants stuck here and there in the code
 * 2005-06-14   JCR   added boolean initialized value to help detect when settings come from current site database
 *
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * This object is intended to represent the single row of data in the table application.
 *
 * @author jc55
 *
 */
public class ApplicationBean {
    public final static int FILTER_SECURITY_LEVEL = 4;
    public final static int CALS_SEARCHBOX_SIZE         = 25;
    public final static int VIVO_SEARCHBOX_SIZE         = 20;

    private static ApplicationBean singleAppBean = null;

    private final static String  DEFAULT_APPLICATION_NAME     = "Vitro";
    private final static String  DEFAULT_FLAG1_NAME           = "portal";
    private final static int     DEFAULT_MAX_PORTAL_ID        = 15;
    private final static int     ALL_PORTAL_FOR_MAX_OF_15     = 65535;
    private final static int     DEFAULT_MIN_SHARED_PORTAL    = 2;
    private final static int     DEFAULT_MAX_SHARED_PORTAL    = 5;
    private final static String DEFAULT_ROOT_LOGOTYPE_IMAGE   = "";
    private final static int     DEFAULT_ROOT_LOGOTYPE_WIDTH  = 0;
    private final static int     DEFAULT_ROOT_LOGOTYPE_HEIGHT = 0;
    private final static String  DEFAULT_ROOT_LOGOTYPE_TITLE  = "";
    private final static String  DEFAULT_SEARCH_DOMAIN        = "";
    // private final static String  DEFAULT_ROOT_LOGOTYPE_IMAGE  = "CornellSeal.69x65.transparent.clipped.gif";
    // private final static int     DEFAULT_ROOT_LOGOTYPE_WIDTH  = 65;
    // private final static int     DEFAULT_ROOT_LOGOTYPE_HEIGHT = 65;
    //private final static int     DEFAULT_HEADER_IMAGE_WIDTH   = 69;
    //private final static int     DEFAULT_HEADER_IMAGE_HEIGHT  = 65;
    //private final static String  DEFAULT_ROOT_LOGOTYPE_TITLE  = "Cornell University";
    //private final static String  DEFAULT_SEARCH_DOMAIN        = "http://www.cornell.edu";
    private final static boolean DEFAULT_ONLY_CURRENT         = true;
    private final static boolean DEFAULT_ONLY_PUBLIC          = true;


    // Default initializations, which may be overwritten in the AppBeanMapper
    // but are otherwise not changed there
    private boolean   initialized             = false;
    private String    sessionIdStr            = null;
    private String    applicationName         = DEFAULT_APPLICATION_NAME;
    private String    flag1Name               = DEFAULT_FLAG1_NAME;
    private String    flag2Name               = null;
    private String    flag3Name               = null;
    private ArrayList flag1List               = null;
    private ArrayList flag2List               = null;
    private ArrayList flag3List               = null;
    private int       maxPortalId             = DEFAULT_MAX_PORTAL_ID;
    private int       allPortalFlagNumeric    = ALL_PORTAL_FOR_MAX_OF_15; // this is calculated dynamically and becomes the way to search the entire database (all portals)    
    private int       minSharedPortalId       = DEFAULT_MIN_SHARED_PORTAL;
    private int       maxSharedPortalId       = DEFAULT_MAX_SHARED_PORTAL;
    private int       sharedPortalFlagNumeric = 60; // was 0; this is calculated dynamically and becomes the id of the ALL CALS RESEARCH portal
    private String    keywordHeading          = null;
    private String    rootLogotypeImage       = DEFAULT_ROOT_LOGOTYPE_IMAGE;
    private int       rootLogotypeWidth       = DEFAULT_ROOT_LOGOTYPE_WIDTH;
    private int       rootLogotypeHeight      = DEFAULT_ROOT_LOGOTYPE_HEIGHT;
    private String    rootLogotypeTitle       = DEFAULT_ROOT_LOGOTYPE_TITLE;
    private boolean   onlyCurrent             = DEFAULT_ONLY_CURRENT;
    private boolean   onlyPublic              = DEFAULT_ONLY_PUBLIC;
    
    /** for internal use only */
    private int minUsedToCalcualteShared = -1;
    /** for internal use only */
    private int maxUsedToCalculateShared = -1;
    /** for internal use only */
    private int       maxPortalUsedToCalculateAllPortal = DEFAULT_MAX_PORTAL_ID;
    
    public static ApplicationBean getAppBean(ServletContext sc){
        if( sc != null ){
            Object obj = sc.getAttribute("applicationBean");
            if( obj != null )
                return (ApplicationBean)obj;
        }
        return new ApplicationBean();
    }
        
    public static int calculateAllPortalFlagNumeric(int max_portal_id) {
        int returnVal=0;
        if (max_portal_id>0) {
            for (int i=0; i<max_portal_id; i++) {
                returnVal+=(int)Math.pow(2.0,i);
            }
        }
        return returnVal;
    }

    public static int calculateSharedPortalFlagNumeric(int min_shared_portal_id,int max_shared_portal_id) {
        int returnVal=0;
        if (max_shared_portal_id>min_shared_portal_id) {
            for (int i=min_shared_portal_id; i<=max_shared_portal_id; i++) {
                returnVal+=(int)Math.pow(2.0,i);
            }
        } else if (min_shared_portal_id==max_shared_portal_id) {
            returnVal=(int)Math.pow(2.0,min_shared_portal_id);
        } else {
            returnVal=(int)Math.pow(2.0,(min_shared_portal_id+1)); //max in this case
            for (int i=min_shared_portal_id; i>=max_shared_portal_id; i--) {
                returnVal-=(int)Math.pow(2.0,i);
            }
        }
        return returnVal;
    }


    public String toString() {
        String output = "Application Bean Contents:\n";
        output += "  initialized from DB:    [" + initialized             + "]\n";
        output += "  session id:             [" + sessionIdStr            + "]\n";
        output += "  application name:       [" + applicationName         + "]\n";
        output += "  flag1 name:             [" + flag1Name               + "]\n";
        output += "  flag2 name:             [" + flag2Name               + "]\n";
        output += "  flag3 name:             [" + flag3Name               + "]\n";
        output += "  max portal id:          [" + maxPortalId             + "]\n";
        output += "  allPortalFlagNumeric    [" + allPortalFlagNumeric    + "]\n";
        output += "  min shared portal:      [" + minSharedPortalId       + "]\n";
        output += "  max shared portal:      [" + maxSharedPortalId       + "]\n";
        output += "  sharedPortalFlagNumeric [" + sharedPortalFlagNumeric + "]\n";
        output += "  keyword table:          [" + keywordHeading          + "]\n";
        output += "  root logotype image:    [" + rootLogotypeImage       + "]\n";
        output += "  root logotype width:    [" + rootLogotypeWidth       + "]\n";
        output += "  root logotype height:   [" + rootLogotypeHeight      + "]\n";
        output += "  root logotype title:    [" + rootLogotypeTitle       + "]\n";
        output += "  only current entities:  [" + onlyCurrent             + "]\n";
        output += "  only public entities:   [" + onlyPublic              + "]\n";
        return output;
    }

    /*************************** SET functions ****************************/

    public void setInitialized( boolean boolean_val) {
        initialized=boolean_val;
    }

    public void setSessionIdStr( String string_val ) {
        sessionIdStr = string_val;
    }

    public void setApplicationName( String string_val ) {
        applicationName = string_val;
    }

    public void setFlag1Name( String string_val ) {
        flag1Name = string_val;
    }

    public void setFlag2Name( String string_val ) {
        flag2Name = string_val;
    }

    public void setFlag3Name( String string_val ) {
        flag3Name = string_val;
    }

    public void setMaxPortalId( int int_val ) {
        maxPortalId = int_val;        
    }

//    public void setAllPortalFlagNumeric( int int_val ) {
//        allPortalFlagNumeric=int_val;
//    }

    public void setMinSharedPortalId( int int_val ) {
        minSharedPortalId = int_val;
    }

    public void setMaxSharedPortalId( int int_val ) {
        maxSharedPortalId = int_val;
    }

//    public void setSharedPortalFlagNumeric( int int_val ) {
//        sharedPortalFlagNumeric=int_val;
//    }

    public void setKeywordHeading(String string_val) {
        keywordHeading=string_val;
    }

    public void setRootLogotypeImage(String string_val) {
        rootLogotypeImage=string_val;
    }

    public void setRootLogotypeWidth( int int_val ) {
        rootLogotypeWidth = int_val;
    }

    public void setRootLogotypeHeight( int int_val ) {
        rootLogotypeHeight = int_val;
    }

    public void setRootLogotypeTitle(String string_val) {
        rootLogotypeTitle=string_val;
    }


    public void setOnlyCurrent(boolean boolean_val) {
        onlyCurrent=boolean_val;
    }

    public void setOnlyPublic(boolean boolean_val) {
        onlyPublic=boolean_val;
    }

    public void setFlag1List(ArrayList h) {
        flag1List=h;
    }

    public void setFlag2List(ArrayList h) {
        flag2List=h;
    }

    public void setFlag3List(ArrayList h) {
        flag3List=h;
    }



    /*************************** GET functions ****************************/

    public boolean isInitialized() {
        return initialized;
    }

    public String getSessionIdStr() {
        return sessionIdStr;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getFlag1Name() {
        return flag1Name;
    }

    public boolean isFlag1Active() {
        return (maxPortalId>1);
    }

    public String getFlag2Name() {
        return flag2Name;
    }

    public boolean isFlag2Active() {
        return (flag2Name!=null && !flag2Name.equals(""));
    }

    public String getFlag3Name() {
        return flag3Name;
    }

    public boolean isFlag3Active() {
        return (flag3Name!=null && !flag3Name.equals(""));
    }

    public int getMaxPortalId() {
        return maxPortalId;
    }

    public int getAllPortalFlagNumeric() {
        if( this.maxPortalId != this.maxPortalUsedToCalculateAllPortal ){
            this.allPortalFlagNumeric = calculateAllPortalFlagNumeric( this.maxPortalId );
            this.maxPortalUsedToCalculateAllPortal = this.maxPortalId;
        }
        return this.allPortalFlagNumeric;        
    }

    public int getMinSharedPortalId() {
        return minSharedPortalId;
    }

    public int getMaxSharedPortalId() {
        return maxSharedPortalId;
    }
    
    public int getSharedPortalFlagNumeric() {
        if( minSharedPortalId != minUsedToCalcualteShared ||
            maxSharedPortalId != maxUsedToCalculateShared ){
            this.sharedPortalFlagNumeric 
                = calculateSharedPortalFlagNumeric(minSharedPortalId, maxSharedPortalId);
            minUsedToCalcualteShared = minSharedPortalId;
            maxUsedToCalculateShared = maxSharedPortalId;
        }
        return sharedPortalFlagNumeric;
    }

    public String getKeywordHeading() {
        return keywordHeading;
    }

    public String getRootLogotypeImage() {
        return rootLogotypeImage;
    }

    public int getRootLogotypeWidth() {
        return rootLogotypeWidth;
    }

    public int getRootLogotypeHeight() {
        return rootLogotypeHeight;
    }

    public String getRootLogotypeTitle() {
        return rootLogotypeTitle;
    }



    public boolean isOnlyCurrent() {
        return onlyCurrent;
    }

    public boolean isOnlyPublic() {
        return onlyPublic;
    }

    public ArrayList getFlag1List() {
        return flag1List;
    }

/*  public int getFlag1NumericFromString(String flagSet) {
        return getFlagNumericValue(flag1List,flagSet);
    } */

    public ArrayList getFlag2List() {
        return flag2List;
    }

/*  public int getFlag2NumericFromString(String flagSet) {
        return getFlagNumericValue(flag2List,flagSet);
    } */

    public ArrayList getFlag3List() {
        return flag3List;
    }


    public int removeMarkedFlagListEntries(ArrayList flagList,String status) {
        int removeCount=0;
        if (flagList!=null && flagList.size()>0){
            Object[] removals = new Object[flagList.size()];
            for(int i=0; i<flagList.size(); i++) {
                Flagpole thisEntry = (Flagpole)flagList.get(i);
                if (thisEntry!=null) {
                    if (thisEntry.getStatus().equalsIgnoreCase(status)) {
                        removals[removeCount]=flagList.get(i);
                        ++removeCount;
                    }
                }
            }

            for(int j=0; j < /*removeCount*/ removals.length; j++) {
                flagList.remove(removals[j]);
            }
        }
        return removeCount;
    }

/*  public int getFlag3NumericFromString(String flagSet) {
        return getFlagNumericValue(flag3List,flagSet);
    } */

    /* not needed as long as get retrieve flags numerically via flagSet+0
       in the ents_edit.jsp and then specify just flagSet in ents_retry for creating or updating

    private int getFlagNumericValue(ArrayList flagList,String flagSet) {
        int numericValue=0;
        if (flagList!=null && flagList.size()>0) {
            if (flagSet!=null && !flagSet.equals("")) {
                StringTokenizer sTokens = new StringTokenizer(flagSet,",");
                int sCount = sTokens.countTokens();
                for (int s=0; s<sCount; s++ ) {
                    String tokenStr=sTokens.nextToken().trim();
                    if (tokenStr != null && !tokenStr.equals("")) {
                        Iterator iter = flagList.iterator();
                        while (iter.hasNext()) {
                            Flagpole flagpole=(Flagpole)iter.next();
                            if (tokenStr.equals(flagpole.getCheckboxLabel())) {
                                numericValue+=flagpole.getNumeric();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return numericValue;
    } */
}

