/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

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

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This object is intended to represent the single row of data in the table application.
 *
 * @author jc55
 *
 */
public class ApplicationBean {
    
	private static final Log log = LogFactory.getLog(ApplicationBean.class);
	
    public final static int      CALS_SEARCHBOX_SIZE         = 25;
    public final static int      VIVO_SEARCHBOX_SIZE         = 20;

    private final static String  DEFAULT_APPLICATION_NAME     = "Vitro";
    private final static String  DEFAULT_ROOT_LOGOTYPE_IMAGE  = "";
    private final static int     DEFAULT_ROOT_LOGOTYPE_WIDTH  = 0;
    private final static int     DEFAULT_ROOT_LOGOTYPE_HEIGHT = 0;
    private final static String  DEFAULT_ROOT_LOGOTYPE_TITLE  = "";
    
    // Value gets set in default theme setup context listener at application startup
    public static String DEFAULT_THEME_DIR_FROM_CONTEXT  = null;

    // Default initializations, which may be overwritten in the AppBeanMapper
    // but are otherwise not changed there
    private boolean   initialized             = false;
    private String    sessionIdStr            = null;
    private String    applicationName         = DEFAULT_APPLICATION_NAME;
 
    private String    rootLogotypeImage       = DEFAULT_ROOT_LOGOTYPE_IMAGE;
    private int       rootLogotypeWidth       = DEFAULT_ROOT_LOGOTYPE_WIDTH;
    private int       rootLogotypeHeight      = DEFAULT_ROOT_LOGOTYPE_HEIGHT;
    private String    rootLogotypeTitle       = DEFAULT_ROOT_LOGOTYPE_TITLE;
    
    private String    aboutText;
    private String    acknowledgeText;
    private String    contactMail;
    private String    correctionMail;
    private String    copyrightURL;
    private String    copyrightAnchor;
    private String    themeDir;
       
    public static ApplicationBean getAppBean(ServletContext sc){
        if( sc != null ){
            Object obj = sc.getAttribute("applicationBean");
            if( obj != null )
                return (ApplicationBean)obj;
        }
        return new ApplicationBean();
    }

    public String toString() {
        String output = "Application Bean Contents:\n";
        output += "  initialized from DB:    [" + initialized             + "]\n";
        output += "  session id:             [" + sessionIdStr            + "]\n";
        output += "  application name:       [" + applicationName         + "]\n";
        output += "  root logotype image:    [" + rootLogotypeImage       + "]\n";
        output += "  root logotype width:    [" + rootLogotypeWidth       + "]\n";
        output += "  root logotype height:   [" + rootLogotypeHeight      + "]\n";
        output += "  root logotype title:    [" + rootLogotypeTitle       + "]\n";
        return output;
    }

    /*************************** SET functions ****************************/

    public void setSessionIdStr( String string_val ) {
        sessionIdStr = string_val;
    }

    public void setApplicationName( String string_val ) {
        applicationName = string_val;
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
    
    public void setAboutText(String string_val) {
        aboutText = string_val;
    }

    public void setAcknowledgeText(String string_val) {
        acknowledgeText = string_val;
    }
    
    public void setContactMail(String string_val) {
        contactMail = string_val;
    }

    public void setCorrectionMail(String string_val) {
        correctionMail = string_val;
    }
    
    public void setCopyrightURL(String string_val) {
        copyrightURL = string_val;
    }

    public void setCopyrightAnchor(String string_val) {
        copyrightAnchor = string_val;
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


    /*************************** GET functions ****************************/

    public String getSessionIdStr() {
        return sessionIdStr;
    }

    public String getApplicationName() {
        return applicationName;
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
    
    public String getAboutText() {
        return aboutText;
    }

    public String getAcknowledgeText() {
        return acknowledgeText;
    }
    
    public String getContactMail() {
        return contactMail;
    }

    public String getCorrectionMail() {
        return correctionMail;
    }
    
    public String getCopyrightURL() {
        return copyrightURL;
    }

    public String getCopyrightAnchor() {
        return copyrightAnchor;
    }

    // TODO deprecate or remove the following three legacy methods?

    public String getRootBreadCrumbURL() {
    	return "";
    }
    
    public String getRootBreadCrumbAnchor() {
    	return "";
    }
    
    public String getShortHand() {
    	return "";
    }

    /**
     * Directory to find the images.  Subdirectories include css, jsp and site_icons.
     * Example: "themes/enhanced/"
     * @return
     */
    public String getThemeDir(){
        return (themeDir != null && themeDir.length()>0) 
        		? themeDir 
        	    : DEFAULT_THEME_DIR_FROM_CONTEXT;
    }

    /**********************************************************************/
    
    public  boolean themeDirExists(){
        String themeDir = this.getThemeDir();
        if( themeDir == null || themeDir.length() < 1 ){
            log.error("Application has no themeDir/stylesheet set in the db." );
            return false;
        }

        File dir = new File(themeDir);
        if( !dir.exists() ){
            log.error("Application: the themeDir/stylesheet "
                    + dir.getAbsolutePath()+ " does not exist.");
            return false;
        }
        if( !dir.isDirectory() ){
            log.error("Application: themeDir/stylesheet "
                    + dir.getAbsolutePath() + " is not a directory.");
            return false;
        }
        if( !dir.canRead() ){
            log.error("Application: themeDir/stylesheet "
                    + dir.getAbsolutePath() + " is not readable.");
            return false;
        }
        return true;
    }
    
}

