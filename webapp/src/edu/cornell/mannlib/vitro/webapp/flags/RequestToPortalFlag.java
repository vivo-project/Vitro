package edu.cornell.mannlib.vitro.webapp.flags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.flags.FlagException;
import edu.cornell.mannlib.vitro.webapp.flags.AuthFlag;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 5, 2007
 * Time: 10:50:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class RequestToPortalFlag {
    /**
     * This method is required in order to pass the current browse filter settings one level deeper than
     * the Index command, so that if a user (most often an authorized user involved in editing) uses
     * filtering on the Index page, clicking on one of the vclass names listed under each classgroup
     * will keep the same filtering in effect when the individual entities of that vclass are displayed.
     *
     * @return a String value for appending to the link associated with each vclass name within each classgroup
     * @param portalFlag
     */
    public static String getPassThroughParameters(PortalFlag portalFlag) {
        String paramStr="";
        if (portalFlag.isFilteringActive()) { // default is on
            if (portalFlag.getFlag1Numeric() >0) {
                paramStr+="&amp;flag1n="+ portalFlag.getFlag1Numeric();
                if (portalFlag.getFlag1Exclusive()){
                    paramStr+="&amp;omit1=true";
                }
            }
            switch (portalFlag.getFlag1DisplayStatus()) {
            case PortalFlag.SHOW_NO_PORTALS: paramStr+="&amp;display1=none";   break;
            case PortalFlag.SHOW_CURRENT_PORTAL: break;
            case PortalFlag.SHOW_SHARED_PORTALS: paramStr+="&amp;display1=shared"; break;
            case PortalFlag.SHOW_ALL_PORTALS: paramStr+="&amp;display1=all";    break;
            }

            if (portalFlag.getFlag2Numeric() >0) {
                paramStr+="&amp;flag2n="+ portalFlag.getFlag2Numeric();
                if (portalFlag.getFlag2Exclusive()){
                    paramStr+="&amp;omit2=true";
                }
            }
            if (portalFlag.getFlag3Numeric() > 0) {
                paramStr+="&amp;flag3n="+ portalFlag.getFlag3Numeric();
                if (portalFlag.getFlag3Exclusive()){
                    paramStr+="&amp;omit3=true";
                }
            }
        } else {
            paramStr="&amp;filter=false";
        }
        return paramStr;
    }

    /**
     * Returns true if the request seems to be set for no filtering.
     * @param request
     * @return
     */
    public static boolean checkForFilteringParam(HttpServletRequest req){
    	
    	VitroRequest request = new VitroRequest(req);
    	
        String filterToggleStr=request.getParameter("filter");
        if (filterToggleStr!=null && filterToggleStr.equalsIgnoreCase("false")){
                return false;
        }

        filterToggleStr=request.getParameter("flag1");
        if (filterToggleStr!=null && filterToggleStr.equalsIgnoreCase("nofiltering")){
            return false;
        }
        filterToggleStr=request.getParameter("flag2");
        if (filterToggleStr!=null && filterToggleStr.equalsIgnoreCase("nofiltering")){
            return false;
        }
        filterToggleStr=request.getParameter("flag3");
        if (filterToggleStr!=null && filterToggleStr.equalsIgnoreCase("nofiltering")){
            return false;
        }
        return true;
    }

    /**
     * === Use this method to make a new PortalFlag Object in your Controller ===
     *
     * This must set up the portal flag state from the information in the request and
     * in the context.  The intent of this object is to extract from the HttpServletRequest
     * and the ServletContext the PortalFlag state.  A secondary function is to put data
     * in the user session indicating which portal the user is in.
     *
     * It may stash info in the session.  This would be the case if the request indicates
     * a transition from one portal to another.
     *
     * The PortalFilter will use the info in this PortalFlag object to build the
     * request clauses that are needed to do the actual filtering of the objects from
     * the data store.
     *
     *  TODO: document this method
     *  WHAT REQUEST PARAMETERS DOES IT USE?
     *  WHAT DOES IT DO FOR THOSE PARAMETERS?
     *  WHAT SHOULD THOSE PARAMETERS BE TAKEN TO MEAN BY OTHER CODE IN THE SYSTEM?
     *
     *  HTTP PARAMETERS DISCOVERED IN THIS METHOD:
     *  ** for searching, from head.jsp **
     *  "flag1"    -- a flag1 value in the form of a MySQL entity flag1Set member (portal ids from 1 to 15)
     *              - OR a value greater than ApplicationBean.maxPortalId, which is treated slightly differently
     *              - Active in the CALS portals or when a user is logged in for editing, in which case the default
     *              - becomes searching the entire database, signified by a portal value equal to the ApplicationBean's
     *              - allPortalFlagNumeric value, set in the calculateAllPortalFlagNumeric() method of the ApplicationBean
     *              - Also, if flag1 is nofiltering, then no flag1,flag2,or flag3 filtering will happen.
     *              -- if flag1 is 'nofiltering' then flag1 will be set nofiltering and no flag1,flag2 or flag3 filtering will happen.
     *  "filter"   -- whether filtering is active or not (boolean)
     *  ** for browsing, from portalFlagChoices.jsp when included in browseGroup.jsp **
     *  "flag1"    -- a flag1 value in the form of a MySQL entity flag1Set member (portal ids from 0 to 15)
     *              - OR a value greater than ApplicationBean.maxPortalId, which is treated slightly differently
     *  "flag1n"   -- the numeric (power of 2) equivalent of a flag1 value
     *  "omit1"    -- whether the flag1 parameter should effect entity inclusion (omit1=false) or exclusion (omit1=true)
     *  "display1" -- whether to display flag1 values
     *  "flag2"    -- a flag2 value in the form of a MySQL entity flag2Set member (A&S,CALS, etc. for VIVO)
     *  "flag2n"   -- the numeric (power of 2) equivalent of a flag2 value
     *  "omit2"    -- whether the flag2 parameter should effect entity inclusion (omit2=false) or exclusion (omit2=true)
     *  "flag3"    -- a flag3 value in the form of a MySQL entity flag3Set member (Geneva,Ithaca,NYC etc. for VIVO)
     *  "flag3n"   -- the numeric (power of 2) equivalent of a flag3 value
     *  "omit3"    -- whether the flag3 parameter should effect entity inclusion (omit3=false) or exclusion (omit3=true)
     *  "filter"   -- whether filtering is active or not (boolean)
     *  WARNING THERE MAY BE MORE.
     */

    public static void preparePortalStateForFiltering(PortalFlag portalFlag, HttpServletRequest req,
        ApplicationBean appBean, Portal portalBean) throws FlagException {
    	
    	VitroRequest request = new VitroRequest(req);
    	
        HttpSession currentSession = request.getSession();

        if( portalFlag == null ) return;
        if( portalBean == null ) portalBean = new Portal();
        if( appBean == null ) throw new FlagException("You must pass a appBean to preparePortalStateForFiltering");

        // NOTE: filtering by portal is by default ON, even if no parameter -- has to have 'filter' parameter value "false" present to be OFF
        portalFlag.setFilteringActive(checkForFilteringParam(request));

        portalFlag.flag1Active=appBean.isFlag1Active();
        portalFlag.flag2Active=appBean.isFlag2Active();
        portalFlag.flag3Active=appBean.isFlag3Active() ;

        if(  !portalBean.isFlag1Filtering() 
            || "nofiltering".equals(req.getParameter("flag1")) ) {
            portalFlag.flag1Active=false;
            portalFlag.flag2Active=false;
            portalFlag.flag3Active=false;
            portalFlag.setFilteringActive(false);
            return;
        }

        /* JCR 1/30/2007 pulled user auth level out here so always show full array 
         * of choices for filtering to editors when logged in */
        /* BDC 12/18/2008 editors no longer automatically get SHOW_ALL_PORTALS */
        int currentUserSecurityLevel=0;
        AuthFlag authFlag=(AuthFlag)request.getAttribute("authFlag");
        if (authFlag!=null) {
            currentUserSecurityLevel=authFlag.getUserSecurityLevel();
        } else {
            LoginFormBean f = (LoginFormBean) currentSession.getAttribute( "loginHandler" );
            if (f!=null) {
                if (f.getLoginStatus().equals("authenticated")) { // test if session is still valid
                    if (currentSession.getId().equals(f.getSessionId())) {
                        if (request.getRemoteAddr().equals(f.getLoginRemoteAddr())) {
                            currentUserSecurityLevel=Integer.parseInt(f.getLoginRole());
                        }
                    }
                }
            }
        }

        if (portalFlag.isFilteringActive() ){
            // flag1
            String flag1ModeStr=request.getParameter("omit1");
            if (flag1ModeStr!=null && flag1ModeStr.equalsIgnoreCase("true")) {
                portalFlag.setFlag1Exclusive(true);
            } else {
                portalFlag.setFlag1Exclusive(false);
            }
            portalFlag.setFlag1DisplayStatus( PortalFlag.SHOW_NO_PORTALS );
            if (appBean.getMaxPortalId()>1) {
                portalFlag.setFlag1DisplayStatus(PortalFlag.SHOW_CURRENT_PORTAL);
                String[] flag1ParameterValues=request.getParameterValues("flag1");
                if (flag1ParameterValues==null) {
                    String flag1NumericStr=request.getParameter("flag1n");
                    if (flag1NumericStr!=null && !flag1NumericStr.equals("")) {
                        try {
                            portalFlag.setFlag1Numeric (Integer.parseInt(flag1NumericStr));
                        } catch (NumberFormatException ex){
                            throw new FlagException("PortalFlag.java: flag1n parameter"+flag1NumericStr+" cannot be read as an integer value");
                        }
                    } else if (!portalFlag.getFlag1Exclusive()) {
                        if (portalBean.getPortalId()>appBean.getMaxPortalId()) {
                            portalFlag.setFlag1Numeric(portalBean.getPortalId());
                        } else {
                            portalFlag.setFlag1Numeric((int)Math.pow(2.0,portalBean.getPortalId()));
                        }
                    }
                } else {
                    if( flag1ParameterValues.length > 1){ //check for duplicates
                        List params = Arrays.asList( flag1ParameterValues);
                        Set paramSet = new HashSet( params );
                        flag1ParameterValues = (String[])paramSet.toArray((Object[])flag1ParameterValues);
                    }

                    for (int i=0; i<flag1ParameterValues.length;i++) {
                        if( flag1ParameterValues[i] == null || "".equals(flag1ParameterValues[i]) )
                            continue;
                        if( "nofiltering".equalsIgnoreCase(flag1ParameterValues[i] ))
                            if (currentUserSecurityLevel>=ApplicationBean.FILTER_SECURITY_LEVEL)
                                    portalFlag.setFlag1DisplayStatus(PortalFlag.SHOW_ALL_PORTALS);

                        int parameterPortalId = 0;
                        try{
                            parameterPortalId=Integer.parseInt(flag1ParameterValues[i]);
                        }catch( NumberFormatException nfe ){
                            continue;  //bdc34: Use to not catch this exception, but what should happen when flag1 can't be parsed?
                        }
                        if (parameterPortalId>appBean.getMaxPortalId()){
                            portalFlag.setFlag1Numeric(parameterPortalId);
                            break;
                        } else {
                            portalFlag.setFlag1Numeric(portalFlag.getFlag1Numeric()+(int) FlagMathUtils.portalId2Numeric(Long.parseLong(flag1ParameterValues[i])));
                        }
                    }
                }

                String flag1DisplayStr=request.getParameter("display1");
                    
                if (flag1DisplayStr!=null) {
                    if (flag1DisplayStr.equalsIgnoreCase("all")){
                        if (currentUserSecurityLevel>=ApplicationBean.FILTER_SECURITY_LEVEL) {
                            portalFlag.setFlag1DisplayStatus(PortalFlag.SHOW_ALL_PORTALS);
                        }
                    } else if (flag1DisplayStr.equalsIgnoreCase("shared")){
                        if ((portalBean.getPortalId()>=appBean.getMinSharedPortalId()&&portalBean.getPortalId()<=appBean.getMaxSharedPortalId()) || portalBean.getPortalId()>appBean.getMaxPortalId()){
                            portalFlag.setFlag1DisplayStatus(PortalFlag.SHOW_SHARED_PORTALS);
                        }
                    } else if (flag1DisplayStr.equalsIgnoreCase("none")){
                        portalFlag.setFlag1DisplayStatus(PortalFlag.SHOW_NO_PORTALS);
                /*  } else if (portalBean.getPortalId()>=appBean.getMinSharedPortalId()&&portalBean.getPortalId()<=appBean.getMaxSharedPortalId()){
                        this.flag1DisplayStatus=SHOW_SHARED_PORTALS; */
                    }
                } else if (portalBean.getPortalId()>=appBean.getMinSharedPortalId()&&portalBean.getPortalId()<=appBean.getMaxSharedPortalId()){
                    portalFlag.setFlag1DisplayStatus(PortalFlag.SHOW_SHARED_PORTALS);
                }
            }

            // flag2
            String flag2ModeStr=request.getParameter("omit2");
            if (flag2ModeStr!=null && flag2ModeStr.equalsIgnoreCase("true")) {
                portalFlag.setFlag2Exclusive(true);
            } else {
                portalFlag.setFlag2Exclusive(false);
            }
            if (appBean.getFlag2List()!=null && appBean.getFlag2List().size()>0){
                String[] flag2ParameterValues=request.getParameterValues("flag2");
                if (flag2ParameterValues==null) {
                    String flag2NumericStr=request.getParameter("flag2n");
                    if (flag2NumericStr!=null && !flag2NumericStr.equals("")) {
                        try {
                            portalFlag.setFlag2Numeric(Integer.parseInt(flag2NumericStr));
                        } catch (NumberFormatException ex){
                            throw new FlagException("PortalFlag.java: flag2n parameter"+flag2NumericStr+" cannot be read as an integer value");
                        }
                /*  } else if (!this.flag2Exclusive) {
                        flag2Numeric=portalBean.getFlag2Numeric(); */
                    }
                } else {
                    for (int i=0; i<flag2ParameterValues.length;i++) {
                        try {
                            int flag2Val=Integer.parseInt(flag2ParameterValues[i]);
                            portalFlag.setFlag2Numeric( portalFlag.getFlag2Numeric()+(int)Math.pow(2.0,flag2Val));
                        } catch (NumberFormatException ex) {
                            throw new FlagException("PortalFlag.java: flag2 parameter"+flag2ParameterValues[i]+" cannot be read as an integer value");
                        }
                    }
                }
            }

            // flag3
            String flag3ModeStr=request.getParameter("omit3");
            if (flag3ModeStr!=null && flag3ModeStr.equalsIgnoreCase("true")) {
                portalFlag.setFlag3Exclusive(true);
            } else {
                portalFlag.setFlag3Exclusive(false);
            }
            if (appBean.getFlag3List()!=null && appBean.getFlag3List().size()>0){
                String[] flag3ParameterValues=request.getParameterValues("flag3");
                if (flag3ParameterValues==null) {
                    String flag3NumericStr=request.getParameter("flag3n");
                    if (flag3NumericStr!=null && !flag3NumericStr.equals("")) {
                        try {
                            portalFlag.setFlag3Numeric(Integer.parseInt(flag3NumericStr));
                        } catch (NumberFormatException ex){
                            throw new FlagException("PortalFlag.java: flag3n parameter"+flag3NumericStr+" cannot be read as an integer value");
                        }
                /*  } else if (!this.flag3Exclusive){
                        flag3Numeric=portalBean.getFlag3Numeric(); */
                    }
                } else {
                    for (int i=0; i<flag3ParameterValues.length;i++) {
                        try {
                            int flag3Val=Integer.parseInt(flag3ParameterValues[i]);
                            portalFlag.setFlag3Numeric (portalFlag.getFlag3Numeric() +(int)Math.pow(2.0,flag3Val));
                        } catch (NumberFormatException ex) {
                            throw new FlagException("PortalFlag.java: flag3 parameter"+flag3ParameterValues[i]+" cannot be read as an integer value");
                        }
                    }
                }
            }
        }
    }
}
