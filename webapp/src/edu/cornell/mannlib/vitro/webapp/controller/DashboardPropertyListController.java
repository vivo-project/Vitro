/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.web.jsptags.InputElementFormattingTag;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles requests for entity information.
 * The methods for sorting Property/ObjectPropertyStatement Lists are here also.
 * @author bdc34
 *
 */
public class DashboardPropertyListController extends VitroHttpServlet {

    /**
     * This gets the Entity object in the requestScope "entity" and
     * sets up a merged property list for it, including Object and Data properties
     * to be sortable and displayable along with other properties.
     * After that a jsp is called to draw the data.
     *
     * Expected parameters:
     *
     * Expected Attributes:
     * entity - set to entity to display properties for.
     *
     * @author bdc34, then jc55
     */
    
    private static final Log log = LogFactory.getLog(DashboardPropertyListController.class.getName());
    private static final int MAX_GROUP_DISPLAY_RANK = 99;
    
    public void doGet( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        try {
            super.doGet(req, res);
            Object obj = req.getAttribute("entity");
            if( obj == null || !(obj instanceof Individual))
                throw new HelpException("EntityMergedPropertyListController requires request.attribute 'entity' to be of"
                        +" type " + Individual.class.getName() );
            Individual subject =(Individual)obj;
            
            String groupForUngroupedProperties = null;
            String unassignedStr = req.getParameter("unassignedPropsGroupName");
            if (unassignedStr != null && unassignedStr.length()>0) {
                groupForUngroupedProperties = unassignedStr;
                // pass this on to dashboardPropsList.jsp
                req.setAttribute("unassignedPropsGroupName", unassignedStr);
                log.debug("found temp group parameter \""+unassignedStr+"\" for unassigned properties");
            }
            
            boolean groupedMode = false;
            String groupedStr = req.getParameter("grouped");
            if (groupedStr != null && groupedStr.equalsIgnoreCase("true")) {
                groupedMode = true;
            }
            
            boolean onlyPopulatedProps = true;
            String allPossiblePropsStr = req.getParameter("allProps");
            if (allPossiblePropsStr != null && allPossiblePropsStr.length()>0) {
                log.debug("found props inclusion parameter \""+allPossiblePropsStr+"\"");
                if (allPossiblePropsStr.equalsIgnoreCase("true")) {
                    onlyPopulatedProps = false;
                }
            }

            VitroRequest vreq = new VitroRequest(req);
            WebappDaoFactory wdf = vreq.getWebappDaoFactory();
            
            PropertyGroupDao pgDao = null;
            List <PropertyGroup> groupsList = null;
            if (groupedMode) {
                pgDao = wdf.getPropertyGroupDao();
                groupsList = pgDao.getPublicGroups(false); // may be returned empty but not null
            }
           
            PropertyInstanceDao piDao = wdf.getPropertyInstanceDao();
            ObjectPropertyDao opDao = wdf.getObjectPropertyDao();
            
            // set up a new list for the combined object and data properties
            List<Property> mergedPropertyList = new ArrayList<Property>();
            
            if (onlyPopulatedProps) {
                // now first get the properties this entity actually has, presumably populated with statements 
                List<ObjectProperty> objectPropertyList = subject.getObjectPropertyList();
                for (ObjectProperty op : objectPropertyList) {
                    op.setLabel(op.getDomainPublic());
                    mergedPropertyList.add(op);
                }
            } else {
                log.debug("getting all possible object property choices");
                Collection<PropertyInstance> allPropInstColl = piDao.getAllPossiblePropInstForIndividual(subject.getURI());
                if (allPropInstColl != null) {
                    for (PropertyInstance pi : allPropInstColl) {
                        if (pi!=null) {
                            ObjectProperty op = opDao.getObjectPropertyByURI(pi.getPropertyURI());
                            op.setLabel(op.getDomainPublic()); // no longer relevant: pi.getSubjectSide() ? op.getDomainPublic() : op.getRangePublic());
                            mergedPropertyList.add(op);
                        } else {
                            log.error("a property instance in the Collection created by PropertyInstanceDao.getAllPossiblePropInstForIndividual() is unexpectedly null");
                        }
                    }
                } else {
                    log.error("a null Collection is returned from PropertyInstanceDao.getAllPossiblePropInstForIndividual()");
                }
            }
            
            DataPropertyDao dpDao = wdf.getDataPropertyDao();
            if (onlyPopulatedProps) {
                // now do much the same with data properties: get the list of populated data properties, then add in placeholders for missing ones
                List<DataProperty> dataPropertyList = subject.getDataPropertyList();
                for (DataProperty dp : dataPropertyList) {
                    dp.setLabel(dp.getPublicName());
                    mergedPropertyList.add(dp);
                }                
            } else {
                log.debug("getting all possible data property choices");
                Collection <DataProperty> allDatapropColl = dpDao.getAllPossibleDatapropsForIndividual(subject.getURI());
                if (allDatapropColl != null) {
                    for (DataProperty dp : allDatapropColl ) {
                        if (dp!=null) {
                            dp.setLabel(dp.getPublicName());
                            mergedPropertyList.add(dp);
                        } else {
                            log.error("a data property in the Collection created in DataPropertyDao.getAllPossibleDatapropsForIndividual() is unexpectedly null)");
                        }
                    }
                } else {
                    log.error("a null Collection is returned from DataPropertyDao.getAllPossibleDatapropsForIndividual())");
                }
            }
            
            if (mergedPropertyList!=null) {
                try {
                    Collections.sort(mergedPropertyList,new PropertyRanker(vreq));
                } catch (Exception ex) {
                    log.error("Exception sorting merged property list: " + ex.getMessage());
                }
                if (groupedMode) {
                    int groupsCount=0;
                    try {
                        groupsCount = populateGroupsListWithProperties(pgDao,groupsList,mergedPropertyList,groupForUngroupedProperties);
                    } catch (Exception ex) {
                        log.error("Exception on trying to populate groups list with properties: "+ex.getMessage());
                        ex.printStackTrace();
                    }
                    try {
                        int removedCount = pgDao.removeUnpopulatedGroups(groupsList);
                        if (removedCount == 0) {
                            log.warn("Of "+groupsCount+" groups, none removed by removeUnpopulatedGroups");
                    /*  } else {
                            log.warn("Of "+groupsCount+" groups, "+removedCount+" removed by removeUnpopulatedGroups"); */
                        }
                        groupsCount -= removedCount;
                        req.setAttribute("groupsCount", new Integer(groupsCount));
                        if (groupsCount > 0) { //still
                            for (PropertyGroup g : groupsList) {
                                int statementCount=0;
                                if (g.getPropertyList()!=null && g.getPropertyList().size()>0) {
                                    for (Property p : g.getPropertyList()) {
                                        if (p instanceof ObjectProperty) {
                                            ObjectProperty op = (ObjectProperty)p;
                                            if (op.getObjectPropertyStatements()!=null && op.getObjectPropertyStatements().size()>0) {
                                                statementCount += op.getObjectPropertyStatements().size();
                                            }
                                        }
                                    }
                                }
                                g.setStatementCount(statementCount);
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Exception on trying to prune groups list with properties: "+ex.getMessage());
                    }
                    mergedPropertyList.clear();
                }
                if (groupedMode) {
                    req.setAttribute("groupsList",groupsList);
                } else {
                    req.setAttribute("dashboardPropertyList",mergedPropertyList);
                }
            }
            req.setAttribute("entity",subject);

            RequestDispatcher rd = req.getRequestDispatcher(Controllers.DASHBOARD_PROP_LIST_JSP);
            rd.include(req,res);
        } catch (HelpException help){
            doHelp(res);
        } catch (Throwable e) {
            req.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
            rd.forward(req, res);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }
    
    private boolean alreadyOnPropertyList(List<Property> propsList, Property p) {
        if (p.getURI() == null) {
            log.error("Property p has no propertyURI in alreadyOnPropertyList()");
            return true; // don't add to list
        }
        for (Property ptest : propsList) {
            if (ptest.getURI() != null && ptest.getURI().equals(p.getURI())) {
                return true;
            }
        }
        return false;
    }

    private int populateGroupsListWithProperties(PropertyGroupDao pgDao, List<PropertyGroup> groupsList, List<Property> mergedPropertyList, String unassignedGroupName) {
        int count = groupsList.size();
        PropertyGroup tempGroup = null;
        if (unassignedGroupName!=null) {
            tempGroup = pgDao.createDummyPropertyGroup(unassignedGroupName,MAX_GROUP_DISPLAY_RANK);
            log.debug("creating temp property group "+unassignedGroupName+" for any unassigned properties");
        }
        switch (count) {
        case 0: log.warn("groupsList has no groups on entering populateGroupsListWithProperties(); will create a new group \"other\"");
                break;
        case 1: break;
        default: try {
                     Collections.sort(groupsList);
                 } catch (Exception ex) {
                     log.error("Exception on sorting groupsList in populateGroupsListWithProperties()");
                 }
        }
        if (count==0 && unassignedGroupName!=null) {
            groupsList.add(tempGroup);
        }
        for (PropertyGroup pg : groupsList) {
             if (pg.getPropertyList().size()>0) {
                 pg.getPropertyList().clear();
             }
             for (Property p : mergedPropertyList) {
                 if (p.getURI() == null) {
                     log.error("Property p has null URI in populateGroupsListWithProperties()");
                 } else if (p.getGroupURI()==null) {
                     if (tempGroup!=null) { // not assigned any group yet and are creating a group for unassigned properties
                         if (!alreadyOnPropertyList(tempGroup.getPropertyList(),p)) {
                             tempGroup.getPropertyList().add(p);
                             log.debug("adding property "+p.getLabel()+" to members of temp group "+unassignedGroupName);
                         }
                     } // otherwise don't put that property on the list
                 } else if (p.getGroupURI().equals(pg.getURI())) {
                     if (!alreadyOnPropertyList(pg.getPropertyList(),p)) {
                         pg.getPropertyList().add(p);
                     }
                 }
             }
             if (pg.getPropertyList().size()>1) {
                 try {
                     Collections.sort(pg.getPropertyList(),new Property.DisplayComparatorIgnoringPropertyGroup());
                 } catch (Exception ex) {
                     log.error("Exception sorting property group "+pg.getName()+" property list: "+ex.getMessage());
                 }
             }
        }
        if (count>0 && tempGroup!=null && tempGroup.getPropertyList().size()>0) {
            groupsList.add(tempGroup);
        }
        count = groupsList.size();
        return count;
    }

    private void doHelp(HttpServletResponse res)
    throws IOException, ServletException {
        ServletOutputStream out = res.getOutputStream();
        res.setContentType("text/html; charset=UTF-8");
        out.println("<html><body><h2>Quick Notes on using EntityMergedPropList:</h2>");
        out.println("<p>request.attributes 'entity' must be set by Entity servlet before calling."
                +" It should already be 'filled out.' </p>");
        out.println("</body></html>");
    }

    private class HelpException extends Throwable{

        public HelpException(String string) {
            super(string);
        }
    }
    
    private class PropertyRanker implements Comparator {
        VitroRequest vreq;
        WebappDaoFactory wdf;
        PropertyGroupDao pgDao;

        private PropertyRanker(VitroRequest vreq) {
            this.vreq = vreq;
            this.wdf = vreq.getWebappDaoFactory();
            this.pgDao = wdf.getPropertyGroupDao();
        }
        
        public int compare (Object o1, Object o2) {
            Property p1 = (Property) o1;
            Property p2 = (Property) o2;
            
            // sort first by property group rank; if the same, then sort by property rank
            final int MAX_GROUP_RANK=99;
            
            int p1GroupRank=MAX_GROUP_RANK;
            if (p1.getGroupURI()!=null) {
                PropertyGroup pg1 = pgDao.getGroupByURI(p1.getGroupURI());
                if (pg1!=null) {
                    p1GroupRank=pg1.getDisplayRank();
                }
            }
            
            int p2GroupRank=MAX_GROUP_RANK;
            if (p2.getGroupURI()!=null) {
                PropertyGroup pg2 = pgDao.getGroupByURI(p2.getGroupURI());
                if (pg2!=null) {
                    p2GroupRank=pg2.getDisplayRank();
                }
            }
            
            // int diff = pgDao.getGroupByURI(p1.getGroupURI()).getDisplayRank() - pgDao.getGroupByURI(p2.getGroupURI()).getDisplayRank();
            int diff=p1GroupRank - p2GroupRank;
            if (diff==0) {
                diff = determineDisplayRank(p1) - determineDisplayRank(p2);
                if (diff==0) {
                    return p1.getLabel().compareTo(p2.getLabel());
                } else {
                    return diff;
                }
            }
            return diff;
        }
        
        private int determineDisplayRank(Property p) {
            if (p instanceof DataProperty) {
                DataProperty dp = (DataProperty)p;
                return dp.getDisplayTier();
            } else if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty)p;
                return op.getDomainDisplayTier();
            } else {
                log.error("Property is of unknown class in PropertyRanker()");  
            }
            return 0;
        }
    }
}
