/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * This class is intended to store the information from one row off the portals table.
 * It is not intended to store any sort of user or request state.
 *
 * @author bdc34
 *
 */
public class Portal implements Comparable {
	//   defaults for empty constructor or if database values are missing in a newly created clone or new portal, so something appears on web pages
    public static int     		DEFAULT_PORTAL_ID              = 1;
    	// DEFAULT_PORTAL_ID is not final (at the moment) because we don't yet have an RDF configuration for this,
    	// and certain clones might use a ServletContextListener to change the default at context startup.
    final public static int     DEFAULT_ROOT_TAB_ID            = 23;
    final public static String  DEFAULT_APPNAME                = "VITRO"; // signals bean is not being initialized correctly

    //final public static String  DEFAULT_STYLESHEET             = "portal"; // has .css.jsp added to it when file is retrieved

    final public static String  DEFAULT_COPYRIGHT_URL          = "http://www.library.cornell.edu";
    final public static String  DEFAULT_COPYRIGHT_ANCHOR       = "Cornell University Library";

    final public static String  DEFAULT_ROOT_BREADCRUMB_URL    = "http://www.library.cornell.edu"; // may want to be null except for CALS portals, but used to provide URL behind logo
    final public static String  DEFAULT_ROOT_BREADCRUMB_ANCHOR = "Cornell University Library"; // may want to be null except for CALS portals, but used to provide URL behind logo

    final public static String  DEFAULT_CONTACT_MAIL           = "jc55@cornell.edu";

    final public static int     DEFAULT_IMAGE_THUMB_WIDTH      = 150;

    final public static String  DEFAULT_BANNER_IMAGE           = null; // "pamlogo.328x76.transparent.gif";
    final public static int     DEFAULT_BANNER_WIDTH           = 328;
    final public static int     DEFAULT_BANNER_HEIGHT          = 76;

    final public static String  DEFAULT_LOGOTYPE_IMAGE         = "libraryOnly_2color.transparent.181x76.gif";
    final public static int     DEFAULT_LOGOTYPE_WIDTH         = 181;
    final public static int     DEFAULT_LOGOTYPE_HEIGHT        = 65;

    // Value gets set in default theme setup context listener at application startup
    public static String DEFAULT_THEME_DIR_FROM_CONTEXT  = null;
    
    final static boolean NOISY = false;
    private final static boolean DEFAULT_FLAG1_FILTERING   = true;

    // NOT STATIC, applying to each instance of the class that is created
//This is the start of fields that are from actual columns on the database
    private int     portalId;
    private int     rootTabId;
    private String  appName;
    //stylesheet
    private String  bannerImage;
    //oldname
//  private String  flag1Values;
//  private String  flag2Values;
    private String  contactMail;
    private String  correctionMail;
    private String  shortHand;
    private String  aboutText;
    private String  acknowledgeText;
    private int     bannerWidth;
    private int     bannerHeight;
//  private String  flag3Values;
    private int     flag2Numeric;
    private int     flag3Numeric;
    private String  copyrightURL;
    private String  copyrightAnchor;
    private String  rootBreadCrumbURL;
    private String  rootBreadCrumbAnchor;
    private String  logotypeImage;
    private int     logotypeWidth;
    private int     logotypeHeight;
    private int     imageThumbWidth;
    private int     displayRank;
    private boolean flag1SearchFiltering;
    private boolean flag2SearchFiltering;
    private boolean flag3SearchFiltering;
    private String  urlprefix;

    /** If true, then this portal will be filtered by flag1 so that only individuals in the
     * owl class Flag1Value${portalId}Thing will be displayed */
    private boolean   flag1Filtering          = DEFAULT_FLAG1_FILTERING;

//END of fields that are from columns on database


    private boolean initialized;
    private String  searchOptions;
    private String  navigationChoices;
    private String themeDir;
    private WebappDaoFactory webappDaoFactory;

    private static final Log log = LogFactory.getLog(Portal.class.getName());

    /************************** Default Constructor ********************************/

    /*
    public Portal() {
        initialized          = false;
        portalId             = DEFAULT_PORTAL_ID;
        displayRank          = portalId;
        rootTabId            = DEFAULT_ROOT_TAB_ID;
        appName              = DEFAULT_APPNAME;
        shortHand            = DEFAULT_APPNAME;
        imageThumbWidth      = DEFAULT_IMAGE_THUMB_WIDTH;
        bannerImage          = DEFAULT_BANNER_IMAGE;
        bannerWidth          = DEFAULT_BANNER_WIDTH;
        bannerHeight         = DEFAULT_BANNER_HEIGHT;
        logotypeImage        = DEFAULT_LOGOTYPE_IMAGE;
        logotypeWidth        = DEFAULT_LOGOTYPE_WIDTH;
        logotypeHeight       = DEFAULT_LOGOTYPE_HEIGHT;
        flag2Numeric         = 0;
        flag3Numeric         = 0;
        flag1SearchFiltering = true;
        flag2SearchFiltering = true;
        flag3SearchFiltering = true;
        aboutText            = null;
        acknowledgeText      = null;
        contactMail          = DEFAULT_CONTACT_MAIL;
        copyrightURL         = DEFAULT_COPYRIGHT_URL;
        copyrightAnchor      = DEFAULT_COPYRIGHT_ANCHOR;
        rootBreadCrumbURL    = null; // don't always want to display a root breadcrumb URL from outside the site
        rootBreadCrumbAnchor = null;
        searchOptions        = null;
        navigationChoices    = null;
        themeDir             = DEFAULT_THEME_DIR;
        urlprefix            = null;
    }
	*/


    /*
    public PortalBean(String session_id_str,int portal_id,int root_tab_id,String app_name,String short_hand,String style_sheet,
                      int image_thumb_width,String banner_image,int banner_width,int banner_height,
                      String logotype_image,int logotype_width,int logotype_height,String contact_mail,
                      int flag2_numeric,int flag3_numeric,String copyright_url,String copyright_anchor,String root_breadcrumb_url,String root_breadcrumb_anchor) {
        initialized          = true;
        sessionIdStr         = session_id_str;
        portalId             = portal_id;
        rootTabId            = root_tab_id;
        appName              = app_name;
        shortHand            = short_hand;
        stylesheet           = style_sheet;
        imageThumbWidth      = image_thumb_width;
        bannerImage          = banner_image;
        bannerWidth          = banner_width;
        bannerHeight         = banner_height;
        logotypeImage        = logotype_image;
        logotypeWidth        = logotype_width;
        logotypeHeight       = logotype_height;
        flag2Numeric         = flag2_numeric;
        flag3Numeric         = flag3_numeric;
        contactMail          = contact_mail;
        copyrightURL         = copyright_url;
        copyrightAnchor      = copyright_anchor;
        rootBreadCrumbURL    = root_breadcrumb_url;
        rootBreadCrumbAnchor = root_breadcrumb_anchor;
      //displayRank          = display_rank; // not set from JSP
    } */





    public String toHTML() {
        String output = "<p>Portal Info:<ul>";
        output += "<li>portal id:            [" + portalId             + "]</li>";
        output += "<li>root tab id:          [" + rootTabId            + "]</li>";
        output += "<li>portal app name:      [" + appName              + "]</li>";
        output += "<li>portal shortHand:     [" + shortHand            + "]</li>";
        output += "<li>contact email:        [" + contactMail          + "]</li>";
        output += "<li>correction email:     [" + contactMail          + "]</li>";
        output += "<li>portal theme directory: [" + themeDir           + "]</li>";
        output += "<li>image thumb width     [" + imageThumbWidth      + "]</li>";
        output += "<li>banner image:         [" + bannerImage          + "]</li>";
        output += "<li>banner image width:   [" + bannerWidth          + "]</li>";
        output += "<li>banner image height:  [" + bannerHeight         + "]</li>";
        output += "<li>logotype image:       [" + logotypeImage        + "]</li>";
        output += "<li>logotype image width: [" + logotypeWidth        + "]</li>";
        output += "<li>logotype image height:[" + logotypeHeight       + "]</li>";
//      output += "<li>flag 1 values:        [" + flag1Values          + "]</li>";
//      output += "<li>flag 2 values:        [" + flag2Values          + "]</li>";
        output += "<li>flag 2 numeric        [" + flag2Numeric         + "]</li>";
//      output += "<li>flag 3 values:        [" + flag3Values          + "]</li>";
        output += "<li>flag 3 numeric        [" + flag3Numeric         + "]</li>";
        output += "<li>flag1 search filters  [" + flag1SearchFiltering + "]</li>";
        output += "<li>flag2 search filters  [" + flag2SearchFiltering + "]</li>";
        output += "<li>flag3 search filters  [" + flag3SearchFiltering + "]</li>";
        output += "<li>About text            [" + aboutText            + "]</li>";
        output += "<li>Acknowledge text      [" + acknowledgeText      + "]</li>";
        output += "<li>copyright URL:        [" + copyrightURL         + "]</li>";
        output += "<li>copyright anchor:     [" + copyrightAnchor      + "]</li>";
        output += "<li>breadcrumb URL:       [" + rootBreadCrumbURL    + "]</li>";
        output += "<li>breadcrumb anchor:    [" + rootBreadCrumbAnchor + "]</li>";
        output += "<li>display rank:         [" + displayRank          + "]</li>";
        output += "<li>search options        [" + searchOptions        + "]</li>";
        output += "<li>navigation choices    [" + navigationChoices    + "]</li>";
        output += "<li>urlprefix             [" + urlprefix            + "]</li>";
        output += "</ul></p>";
        return output;
    }

    /*************************** SET functions ****************************/

    public void setInitialized( boolean boolean_val ) {
        initialized=boolean_val;
    }

    public void setPortalId(int int_val) {
        portalId=int_val;
    }

    public void setRootTabId(int int_val) {
        rootTabId=int_val;
    }

    public void setAppName(String string_val) {
        appName = string_val;
    }

    public void setShortHand(String string_val) {
        shortHand = string_val;
    }

    public void setContactMail(String string_val) {
        contactMail = string_val;
    }

    public void setCorrectionMail(String string_val) {
        correctionMail = string_val;
    }
    
    public void setImageThumbWidth(int int_val) {
        imageThumbWidth = int_val;
    }

    public void setBannerImage(String string_val) {
        bannerImage = string_val;
    }

    public void setBannerWidth(int int_val) {
        bannerWidth = int_val;
    }

    public void setBannerHeight(int int_val) {
        bannerHeight = int_val;
    }

    public void setLogotypeImage(String string_val) {
        logotypeImage = string_val;
    }

    public void setLogotypeWidth(int int_val) {
        logotypeWidth = int_val;
    }

    public void setLogotypeHeight(int int_val) {
        logotypeHeight = int_val;
    }

/*  public void setFlag1Values(String string_val) {
        flag1Values = string_val;
    }

    public void setFlag2Values(String string_val) {
        flag2Values = string_val;
    } */

    public void setFlag2Numeric(int int_val) {
        flag2Numeric = int_val;
    }

/*  public void setFlag3Values(String string_val) {
        flag3Values = string_val;
    } */

    public void setFlag3Numeric(int int_val) {
        flag3Numeric = int_val;
    }

    public void setFlag1SearchFilters(boolean b_val) {
        flag1SearchFiltering=b_val;
    }

    public void setFlag2SearchFilters(boolean b_val) {
        flag2SearchFiltering=b_val;
    }

    public void setFlag3SearchFilters(boolean b_val) {
        flag3SearchFiltering=b_val;
    }

    public void setAboutText(String string_val) {
        aboutText = string_val;
    }

    public void setAcknowledgeText(String string_val) {
        acknowledgeText = string_val;
    }

    public void setCopyrightURL(String string_val) {
        copyrightURL = string_val;
    }

    public void setCopyrightAnchor(String string_val) {
        copyrightAnchor = string_val;
    }

    public void setRootBreadCrumbURL(String string_val) {
        rootBreadCrumbURL = string_val;
    }

    public void setRootBreadCrumbAnchor(String string_val) {
        rootBreadCrumbAnchor = string_val;
    }

    public void setDisplayRank(int int_val) {
        displayRank=int_val;
    }

    public void setSearchOptions(String string_val) {
        searchOptions=string_val;
    }

    public void setNavigationChoices(String string_val) {
        navigationChoices=string_val;
    }

    public void setUrlprefix(String in){
        this.urlprefix = in;
    }
     public void setFlag1Filtering(String b){
        flag1Filtering = "true".equalsIgnoreCase(b);
    }
     
    public void setWebappDaoFactory (WebappDaoFactory wdf) {
    	this.webappDaoFactory = wdf;
    }
    
    /*************************** GET functions ****************************/

    public boolean isInitialized() {
        return initialized;
    }

    public int getPortalId() {
        return portalId;
    }

    public int getRootTabId() {
        return rootTabId;
    }

    public String getAppName() {
        return appName;
    }

    public String getShortHand() {
        return shortHand;
    }

    public String getContactMail() {
        return contactMail;
    }

    public String getCorrectionMail() {
        return correctionMail;
    }

    public int getImageThumbWidth() {
        return imageThumbWidth;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public int getBannerWidth() {
        return bannerWidth;
    }

    public int getBannerHeight() {
        return bannerHeight;
    }

    public String getLogotypeImage() {
        return logotypeImage;
    }

    public int getLogotypeWidth() {
        return logotypeWidth;
    }

    public int getLogotypeHeight() {
        return logotypeHeight;
    }

/*  public String getFlag1Values() {
        return flag1Values;
    }

    public String getFlag2Values() {
        return flag2Values;
    } */

    public int getFlag2Numeric() {
        return flag2Numeric;
    }

/*  public String getFlag3Values() {
        return flag3Values;
    } */

    public int getFlag3Numeric() {
        return flag3Numeric;
    }

    public boolean isFlag1SearchFiltering() {
        return flag1SearchFiltering;
    }

    public boolean isFlag2SearchFiltering() {
        return flag2SearchFiltering;
    }

    public boolean isFlag3SearchFiltering() {
        return flag3SearchFiltering;
    }

    public String getAboutText() {
        return aboutText;
    }

    public String getAcknowledgeText() {
        return acknowledgeText;
    }

    public String getCopyrightURL() {
        return copyrightURL;
    }

    public String getCopyrightAnchor() {
        return copyrightAnchor;

    }
    public String getRootBreadCrumbURL() {
        return rootBreadCrumbURL;
    }

    public String getRootBreadCrumbAnchor() {
        return rootBreadCrumbAnchor;
    }

    public int getDisplayRank() {
        return displayRank;
    }

    public String getSearchOptions() {
        return searchOptions;
    }

    public String getNavigationChoices() {
        return navigationChoices;
    }

    public String getUrlprefix(){
        return this.urlprefix;
    }

    public boolean isFlag1Filtering(){
        return flag1Filtering;
    }

    /**
     * Directory to find the images.  Subdirectories include css, jsp and site_icons.
     * Example: "themes/enhanced/"
     * @return
     */
    public String getThemeDir(){
        return (themeDir != null && themeDir.length()>0) ? themeDir : DEFAULT_THEME_DIR_FROM_CONTEXT;
    }
    public void setThemeDir(String string_val) {
        if( string_val == null || string_val.length() == 0
            || "default".equalsIgnoreCase(string_val)
            || "portal".equalsIgnoreCase(string_val)
            || "null".equalsIgnoreCase(string_val)
            || "&nbsp;".equalsIgnoreCase(string_val) )
            themeDir = DEFAULT_THEME_DIR_FROM_CONTEXT;
        else
            themeDir = string_val;
    }

    public  boolean themeDirExists(){
        String themeDir = this.getThemeDir();
        if( themeDir == null || themeDir.length() < 1 ){
            log.error("Portal id: " + this.getPortalId() + " has no themeDir/stylesheet set in the db." );
            return false;
        }

        File dir = new File(themeDir);
        if( !dir.exists() ){
            log.error("Portal id: " + getPortalId() + ", the themeDir/stylesheet "
                    + dir.getAbsolutePath()+ " does not exist.");
            return false;
        }
        if( !dir.isDirectory() ){
            log.error("Portal id: " + getPortalId() + ", themeDir/stylesheet "
                    + dir.getAbsolutePath() + " is not a directory.");
            return false;
        }
        if( !dir.canRead() ){
            log.error("Portal id: " + getPortalId() + ", themeDir/stylesheet "
                    + dir.getAbsolutePath() + " is not readable.");
            return false;
        }
        return true;
    }

    public int compareTo(Object o1) {
        return this.portalId - ((Portal)o1).getPortalId();
    }

    // nac26: 20080618 - no longer used as we are not switching themes as part of self editing
    //public String getDefaultEditThemeDir() {
    //    return "themes/editdefault/";
    //}
    
    public WebappDaoFactory getWebappDaoFactory() {
    	return this.webappDaoFactory;
    }
    
    public String getTypeUri(){
        return VitroVocabulary.vitroURI + "Flag1Value" 
                + getPortalId() + "Thing";
    }        
}
