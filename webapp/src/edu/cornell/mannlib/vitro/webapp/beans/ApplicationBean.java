/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This object is intended to represent the single row of data in the table application.
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
    public static ThemeInfo themeInfo  = new ThemeInfo(null, "no_default_theme", new ArrayList<String>());

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
    	if (themeInfo.isValidThemeDir(themeDir)) {
    		return themeDir;
    	} else {
    		return themeInfo.getDefaultThemeDir();
    	}
    }

    /**********************************************************************/

	/**
	 * Hold the names of the available themes, the name of the default theme,
	 * and the base directory that contains the theme directories.
	 * 
	 * The theme names are stored as simple strings, like "wilma".
	 * 
	 * To be backwards compatible, we need to be able to test a string like
	 * "themes/wilma/ to see whether it is available, or to return the default
	 * directory in that form.
	 */
	public static class ThemeInfo {
		private final File themesBaseDir;
		private final String defaultThemeName;
		private final List<String> themeNames;

		public ThemeInfo(File themesBaseDir, String defaultThemeName,
				List<String> themeNames) {
			this.themesBaseDir = themesBaseDir;
			this.defaultThemeName = defaultThemeName;
			this.themeNames = Collections
					.unmodifiableList(new ArrayList<String>(themeNames));
		}

		public static String themeNameFromDir(String themeDir) {
			if (themeDir == null) {
				return themeDir;
			}
			if (!themeDir.startsWith("themes/") || !themeDir.endsWith("/")) {
				return themeDir;
			}
			return themeDir.substring(7, themeDir.length() - 1);
		}

		public boolean isValidThemeDir(String themeDir) {
			if (themeDir == null) {
				return false;
			}
			return themeNames.contains(themeNameFromDir(themeDir));
		}

		public String getDefaultThemeDir() {
			return "themes/" + defaultThemeName + "/";
		}

		public File getThemesBaseDir() {
			return themesBaseDir;
		}

		public String getDefaultThemeName() {
			return defaultThemeName;
		}

		public List<String> getThemeNames() {
			return themeNames;
		}

	}
	
}

