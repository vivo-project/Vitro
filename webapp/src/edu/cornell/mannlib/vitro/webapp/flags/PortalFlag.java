package edu.cornell.mannlib.vitro.webapp.flags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;
import edu.cornell.mannlib.vitro.webapp.flags.RequestToPortalFlag;

/**
 * The intent of PortalFlag is that it will be the place for code that takes the
 * HttpServletRequest and the Context and gathers info about the state of which
 * portal the user is 'in' and what filtering is required by either the application
 * itself or user input via a form or a special request parameter in the URL.
 *
 * This behavior should be separated from the code that implements the filtering
 * on portal at the database level.
 *
 * @author jc55
 *
 */
public class PortalFlag   {
    /****************** properties ****************************/
    private int     flag1Numeric = 0;
    private int     flag2Numeric = 0;
    private int     flag3Numeric = 0;

    /* flagXExclusive == false means omit anything matching the flag numeric mask */
    private boolean flag1Exclusive = false;
    private boolean flag2Exclusive = false;
    private boolean flag3Exclusive = false;

    private int     flag1DisplayStatus = SHOW_CURRENT_PORTAL;

    /**
     * This indicates whether there should be filtering -- authorized users can override default portal-based filtering, which
     * is useful while editing to help avoid duplicate content.
     */
    private boolean filteringActive = true;

    /** Indicates if the application has flag 1 filtering active */
    public boolean flag1Active = false;
    /** Indicates if the application has flag 2 filtering active */
    public boolean flag2Active = false;
    /** Indicates if the application has flag 3 filtering active */
    public boolean flag3Active = false;

    /*************** getters and setters ************************/
    public boolean isFlag1Exclusive() { return flag1Exclusive;    }
    public void setFlag1Exclusive(boolean flag1Exclusive) {
        this.flag1Exclusive = flag1Exclusive;
    }

    public boolean isFlag2Exclusive() {return flag2Exclusive;}
    public void setFlag2Exclusive(boolean flag2Exclusive) {
        this.flag2Exclusive = flag2Exclusive;
    }

    public boolean isFlag3Exclusive() {return flag3Exclusive;}
    public void setFlag3Exclusive(boolean flag3Exclusive) {
        this.flag3Exclusive = flag3Exclusive;
    }

    public  int     getFlag1Numeric() { return flag1Numeric; }
    public void setFlag1Numeric(int flag1Numeric) { this.flag1Numeric = flag1Numeric; }

    public  int     getFlag2Numeric() { return flag2Numeric; }
    public void setFlag2Numeric(int i) {
        this.flag2Numeric = i;
    }

    public  int     getFlag3Numeric() { return flag3Numeric; }
    public void setFlag3Numeric(int i) {
        this.flag3Numeric = i;
    }

    /* flagXExclusive == false means omit anything matching the flag numeric mask */
    public boolean  getFlag1Exclusive() { return flag1Exclusive; }
    public boolean  getFlag2Exclusive() { return flag2Exclusive; }
    public boolean  getFlag3Exclusive() { return flag3Exclusive; }

    public  int     getFlag1DisplayStatus()      { return flag1DisplayStatus; }
    public  void    setFlag1DisplayStatus(int i) { flag1DisplayStatus=i;      }


    public boolean  isFilteringActive()           { return filteringActive; }
    public void     setFilteringActive(boolean b) { filteringActive=b;      }

    /***************** constructors ***********************/
    /** this is for testing only. */
    protected PortalFlag(){}

    /**
     * Constructs a PortalFlag that is current, public and
     * limited to the indicated portal.
     * 
     */
    /* 2008-05-27 BJL23 un-deprecated this method because it's still needed by BrowseController.  
     * Unless/until we do away with the PortalFlag class, it seems we'll need a way of constructing one independent of the current HTTP request.
     */ 
    public PortalFlag(int portalId){
        if( portalId != 60 ){
            this.flag1Exclusive = true;
            this.flag1Numeric = (int) FlagMathUtils.portalId2Numeric( portalId );
            this.flag1Active = true;
        }else{
            this.flag1Numeric = 60;
            this.flag1DisplayStatus = PortalFlag.SHOW_SHARED_PORTALS;
        }
    }

    /**
     * Use this to make a new PortalFlag from the http request and the ServletContext.
     *
     * @param req
     */
    public PortalFlag(HttpServletRequest req,ApplicationBean appBean, Portal portalBean)
        throws FlagException {
        RequestToPortalFlag.preparePortalStateForFiltering(this, req,appBean,portalBean);
    }

    /******************* methods *************************/
    /** for debugging  */
    public String toString(){
        return
        "flag1DisplayStatus: " + getFlag1DisplayStatus() + " \n"+
        "flag1Numeric: " + getFlag1Numeric() + " \n"+
        "flag2Numeric: " + getFlag2Numeric() + " \n"+
        "flag3Numeric: " + getFlag3Numeric() + " \n"+
        "flag1Exclusive: " + getFlag1Exclusive() + "\n"+
        "flag2Exclusive: " + getFlag2Exclusive() + "\n"+
        "flag3Exclusive: " + getFlag3Exclusive() + "\n"+
        /*"userSecurityLevel: " + getUserSecurityLevel() +*/ "\n";
    }

    /*********** static constants *******************/
    public static final int SHOW_ALL_PORTALS    = 3;
    public static final int SHOW_SHARED_PORTALS = 2;
    public static final int SHOW_CURRENT_PORTAL = 1;
    public static final int SHOW_NO_PORTALS     = 0;

    /**
     * The impact portal is special and should almost never get
     * stuff added to it.  Here is a mask you can AND with a int
     * to get a int flag1 portal value with impact never set.
     * To wack out impact portal do something like:
     * int myImpactlessFlag = orgFlag & MASKOUT_IMPACT_PORTAL_BIT;
     */
    public static final int MASKOUT_IMPACT_PORTAL_BIT = ~(1<<6);

    private static final Log log = LogFactory.getLog(PortalFlag.class.getName());

}
