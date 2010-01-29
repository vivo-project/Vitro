package edu.cornell.mannlib.vitro.webapp.flags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.*;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * These methods generate flag choice html input elements.
 */
public class PortalFlagChoices {
	
	private static final Log log = LogFactory.getLog(PortalFlagChoices.class.getName());
	
    public static String getFlag1Choices(PortalFlag filterState, ApplicationBean appBean, int portalId ) {
        if (appBean == null || appBean.getFlag1List()==null || appBean.getFlag1List().size()==0){
            return null;
        }
        String returnStr="";
        int flag1CheckboxCount=0;

        // NOTE: NEVER show portal 0, which is reserved for possible future use as an "everything" portal
        int startPortalId = 1;
        int endPortalId   = 1;
        switch (filterState.getFlag1DisplayStatus()) {
            case PortalFlag.SHOW_CURRENT_PORTAL:
            case PortalFlag.SHOW_SHARED_PORTALS:
                if (((portalId >= appBean.getMinSharedPortalId()) &&
                        (portalId <= appBean.getMaxSharedPortalId()))
                        || (portalId > appBean.getMaxPortalId())) {
                    startPortalId = appBean.getMinSharedPortalId();
                    endPortalId   = appBean.getMaxSharedPortalId();
                } else {
                    startPortalId = portalId;
                    endPortalId   = startPortalId;
                }
                break;
            case PortalFlag.SHOW_ALL_PORTALS:
                startPortalId = 1;
                endPortalId   = appBean.getMaxPortalId();
                break;
        }
        Iterator iter=appBean.getFlag1List().iterator();
        while (iter.hasNext()){
            Object obj=iter.next();
            if (obj!=null && obj instanceof Flagpole) {
                Flagpole flag1Object=(Flagpole)obj;
                if ((flag1Object.getIndex())>=startPortalId && (flag1Object.getIndex())<=endPortalId) {
                    if (!flag1Object.getStatus().equalsIgnoreCase("missing")) {
                        ++flag1CheckboxCount;
                        if ((filterState.getFlag1Numeric() > 0) &&
                                ((filterState.getFlag1Numeric() & flag1Object.getNumeric()) > 0)){
                            returnStr+="<span class='nowrap'><input type='checkbox' name='flag1' value='"
                                    +flag1Object.getIndex()+"' checked='checked'/> "
                                    +flag1Object.getCheckboxLabel()+" </span>";
                        } else {
                            returnStr+="<span class='nowrap'><input type='checkbox' name='flag1' value='"
                                    +flag1Object.getIndex()+"'/> "+flag1Object.getCheckboxLabel()+" </span>";
                        }
                    }
                }
            }
        }
        // now load or reload the flag1 checkbox label for this portal in the single global appBean
        int removeCount=appBean.removeMarkedFlagListEntries(appBean.getFlag1List(),"missing");
        if (removeCount>0) {
            log.debug("Removed "+removeCount+" portal bean list entries from appBean");
        }
        if (flag1CheckboxCount>1){
            returnStr+=" : <a href='javascript:checkAllFlag1(document.filterForm);'> check all</a> | <a href='javascript:unCheckAllFlag1(document.filterForm)'>uncheck all</a>";
        }
        return returnStr;
    }

    public static String getFlag2Choices(PortalFlag filterState,ApplicationBean appBean) {
        if (appBean.getFlag2List()==null || appBean.getFlag2List().size()==0){
            return null;
        }
        String returnStr="";
        int flag2CheckboxCount=0;
        Iterator iter=appBean.getFlag2List().iterator();
        while (iter.hasNext()){
            Object obj=iter.next();
            if (obj!=null && obj instanceof Flagpole) {
                ++flag2CheckboxCount;
                Flagpole flag2Obj=(Flagpole)obj;
                if ( filterState.getFlag2Numeric()>0 && (filterState.getFlag2Numeric()&flag2Obj.getNumeric())>0 ){
                    returnStr+="<span class='nowrap'><input type='checkbox' name='flag2' value='"+flag2Obj.getIndex()+"' checked='checked'/> "+flag2Obj.getCheckboxLabel()+" </span>";
                } else {
                    returnStr+="<span class='nowrap'><input type='checkbox' name='flag2' value='"+flag2Obj.getIndex()+"'/> "+flag2Obj.getCheckboxLabel()+" </span>";
                }
            }
        }
        if (flag2CheckboxCount>1) {
            returnStr+=" : <a href='javascript:checkAllFlag2(document.filterForm);'> check all</a> | <a href='javascript:unCheckAllFlag2(document.filterForm)'>uncheck all</a>";
        }
        return returnStr;
    }

    public static String getFlag3Choices(PortalFlag filterState, ApplicationBean appBean, Portal portalBean) {
        if (appBean.getFlag3List()==null || appBean.getFlag3List().size()==0){
            return null;
        }
        String returnStr="";
        int flag3CheckboxCount=0;
        Iterator iter=appBean.getFlag3List().iterator();
        while (iter.hasNext()){
            Object obj=iter.next();
            if (obj!=null && obj instanceof Flagpole) {
                ++flag3CheckboxCount;
                Flagpole flag3Obj=(Flagpole)obj;
                if ( filterState.getFlag3Numeric()>0 && (filterState.getFlag3Numeric()&flag3Obj.getNumeric())>0 ){
                    returnStr+="<span class='nowrap'><input type='checkbox' name='flag3' value='"+flag3Obj.getIndex()+"' checked='checked'/> "+flag3Obj.getCheckboxLabel()+" </span>";
                } else {
                    returnStr+="<span class='nowrap'><input type='checkbox' name='flag3' value='"+flag3Obj.getIndex()+"'/> "+flag3Obj.getCheckboxLabel()+" </span>";
                }
            }
        }
        if (flag3CheckboxCount>1) {
            returnStr+=" : <a href='javascript:checkAllFlag3(document.filterForm);'> check all</a> | <a href='javascript:unCheckAllFlag3(document.filterForm)'>uncheck all</a>";
        }
        return returnStr;
    }


    public static String showFlagValues(ApplicationBean appBean, Individual entity, PortalDao portalDao)
        throws SQLException {
        if (appBean == null) {
            return "appBean is null in PortalFlag.showFlagValues()";
        }
        if (entity == null) {
            return "incoming Entity object is null in PortalFlag.showFlagValues()";
        }
        String returnStr="";
        if (appBean.isFlag1Active()){
            /* replacing the following numeric portal ids with portal appNames at Gail's request:
            *  returnStr+="| "+appBean.getFlag1Name()+": "+(entity.getFlag1Set()==null ? "null" : (entity.getFlag1Set().equals("") ? "blank" : entity.getFlag1Set()));
            */
            String entityFlag1SetValues=entity.getFlag1Set();
            if (entityFlag1SetValues==null) {
                returnStr+="| "+appBean.getFlag1Name()+": "+"null";
            } else if (entityFlag1SetValues.equals("")) {
                returnStr+="| "+appBean.getFlag1Name()+": "+"blank";
            } else {
                returnStr+="| "+appBean.getFlag1Name()+": ";
                StringTokenizer flag1Tokens=new StringTokenizer(entityFlag1SetValues,",");
                int tokenCount = flag1Tokens.countTokens();
                for ( int f=0; f<tokenCount; f++ ) {
                    String theToken=flag1Tokens.nextToken().trim();
                    try {
                        int portalId=Integer.parseInt(theToken);
                        if (portalId<=appBean.getMaxPortalId()){
                            Portal portal = portalDao.getPortal(portalId);
                            if (portal!=null) {
                                returnStr += (f==0 ? portal.getAppName() : "," + portal.getAppName());
                            }
                        } else {
                            returnStr += (f==0 ? "out of range value "+theToken : ",out of range value" + theToken);
                        }
                    } catch (NumberFormatException ex) {
                        returnStr += (f==0 ? "unreadable flag1Set value "+theToken : ",unreadable flag1Set value" + theToken);
                    }
                }
            }
        }
        if (appBean.isFlag2Active()){
            returnStr+= (returnStr==null?"":" | ") + appBean.getFlag2Name()+": "+(entity.getFlag2Set()==null ? "null" : (entity.getFlag2Set().equals("") ? "blank" : entity.getFlag2Set()));
        }
        if (appBean.isFlag3Active()){
            returnStr+= (returnStr==null?"":" | ") + appBean.getFlag3Name()+": "+(entity.getFlag3Set()==null ? "null" : (entity.getFlag3Set().equals("") ? "blank" : entity.getFlag3Set()));
        }
        return returnStr;
    }
}
