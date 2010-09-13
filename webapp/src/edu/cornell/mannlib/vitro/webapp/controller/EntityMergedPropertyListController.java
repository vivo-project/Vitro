/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jga.fn.UnaryFunctor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.PropertyMaskingSetup;

public class EntityMergedPropertyListController extends VitroHttpServlet {

    /**
     * This gets the Entity object in the requestScope "entity" and
     * sets up a merged property list for it.  This merged list includes 
     * Object and Data properties.
     * 
     * After that a jsp is called to draw the data.
     *
     * Expected parameters:
     *
     * Expected Attributes:
     * entity - set to entity to display properties for.
     *
     * @author jc55 forked this file from EntityPropertyListController  
     */
    
    private static final Log log = LogFactory.getLog(EntityMergedPropertyListController.class.getName());
    private static final int MAX_GROUP_DISPLAY_RANK = 99;
    
    /** Don't include these properties in the list. */
	private static final Collection<String> SUPPRESSED_OBJECT_PROPERTIES = Collections
			.unmodifiableCollection(Arrays
					.asList(new String[] { VitroVocabulary.IND_MAIN_IMAGE }));

    public void doGet( HttpServletRequest request, HttpServletResponse res )
    throws IOException, ServletException {
    	
    	VitroRequest req = new VitroRequest(request);
    	
        try {
            super.doGet(req, res);
            Object obj = req.getAttribute("entity");
            if( obj == null || !(obj instanceof Individual))
                throw new HelpException("EntityMergedPropertyListController requires request.attribute 'entity' to be of"
                        +" type " + Individual.class.getName() );
            Individual subject =(Individual)obj;            
            subject = filterFromContext( subject );
            
            // determine whether are just displaying populated properties or also interleaving unpopulated ones
            boolean editMode = false;
            String modeStr = req.getParameter("mode");
            if (modeStr != null && modeStr.indexOf("edit")>=0) {
                editMode = true;
            }
            
            boolean groupedMode = false;
            String groupedStr = req.getParameter("grouped");
            if (groupedStr != null && groupedStr.equalsIgnoreCase("true")) {
                groupedMode = true;
            }
            
            String groupForUngroupedProperties = null;
            String unassignedStr = req.getParameter("unassignedPropsGroupName");
            if (unassignedStr != null && unassignedStr.length()>0) {
                groupForUngroupedProperties = unassignedStr;
                //pass this on to entityMergedPropsList.jsp
                req.setAttribute("unassignedPropsGroupName", unassignedStr);
                log.debug("found temp group parameter \""+unassignedStr+"\" for unassigned properties");
            }
            
            // set up a new list for the combined object and data properties
            VitroRequest vreq = new VitroRequest(req);
            WebappDaoFactory wdf = vreq.getWebappDaoFactory();
            PropertyGroupDao pgDao = null;
            List <PropertyGroup> groupsList = null;
            if (groupedMode) {
                pgDao = wdf.getPropertyGroupDao();
                groupsList = pgDao.getPublicGroups(false); // may be returned empty but not null
            }
            
            List<Property> mergedPropertyList = new ArrayList<Property>();
            // now first get the properties this entity actually has, presumably populated with statements 
            List<ObjectProperty> objectPropertyList = subject.getObjectPropertyList();                        
            
            for (ObjectProperty op : objectPropertyList) {
            	if (!SUPPRESSED_OBJECT_PROPERTIES.contains(op)) {
                    op.setEditLabel(op.getDomainPublic());
                    mergedPropertyList.add(op);
            	}else{
            		log.debug("suppressed " + op.getURI());
            	}
            }
            
            if (editMode) {
                // for the full list, in order to show empty properties, now need to merge in new ObjectProperty objects with null objectPropertyStatements
                PropertyInstanceDao piDao = wdf.getPropertyInstanceDao();
                ObjectPropertyDao opDao = wdf.getObjectPropertyDao();
                Collection<PropertyInstance> allPropInstColl = piDao.getAllPossiblePropInstForIndividual(subject.getURI());
                if (allPropInstColl != null) {
                    for (PropertyInstance pi : allPropInstColl) {
                        if (pi!=null) {
                            if (!alreadyOnObjectPropertyList(objectPropertyList,pi)) {
                                ObjectProperty op = opDao.getObjectPropertyByURI(pi.getPropertyURI());
                                if (op == null) {
                                    log.error("ObjectProperty op returned null from opDao.getObjectPropertyByURI()");
                                } else if (op.getURI() == null) {
                                    log.error("ObjectProperty op returned with null propertyURI from opDao.getObjectPropertyByURI()");
                                } else if (!alreadyOnPropertyList(mergedPropertyList,op)) {
                                    op.setEditLabel(op.getDomainPublic());
                                    mergedPropertyList.add(op);
                                }
                            }
                        } else {
                            log.error("a property instance in the Collection created by PropertyInstanceDao.getAllPossiblePropInstForIndividual() is unexpectedly null");
                        }
                    }
                } else {
                    log.error("a null Collection is returned from PropertyInstanceDao.getAllPossiblePropInstForIndividual()");
                }
            }
            
            // now do much the same with data properties: get the list of populated data properties, then add in placeholders for missing ones
            List<DataProperty> dataPropertyList = subject.getDataPropertyList();
            for (DataProperty dp : dataPropertyList) {
                dp.setEditLabel(dp.getPublicName());
                mergedPropertyList.add(dp);
            }

            if (editMode) {
                DataPropertyDao dpDao = wdf.getDataPropertyDao();
                Collection <DataProperty> allDatapropColl = dpDao.getAllPossibleDatapropsForIndividual(subject.getURI());
                if (allDatapropColl != null) {
                    for (DataProperty dp : allDatapropColl ) {
                        if (dp!=null) {
                            if (dp.getURI() == null) {
                                log.error("DataProperty dp returned with null propertyURI from dpDao.getAllPossibleDatapropsForIndividual()");
                            } else if (!alreadyOnPropertyList(mergedPropertyList,dp)) {
                                dp.setEditLabel(dp.getPublicName());
                                mergedPropertyList.add(dp);
                            }
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
                
                //deal with collateBySubclass annotations on object properties                
                mergedPropertyList = collateBySubclass( mergedPropertyList );
                
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
            }
            
            if (groupedMode) {
                req.setAttribute("groupsList",groupsList);
            } else {
            	UnaryFunctor<List<Property>,List<Property>> entityPropertyListFilter = PropertyMaskingSetup.getEntityPropertyListFilter(getServletContext());
            	if (entityPropertyListFilter != null) {
            		mergedPropertyList = entityPropertyListFilter.fn(mergedPropertyList);
            	}
            	            	
                req.setAttribute("mergedList",mergedPropertyList);
            }            
                                   
            req.setAttribute("entity",subject);

            RequestDispatcher rd = req.getRequestDispatcher(groupedMode ? Controllers.ENTITY_MERGED_PROP_LIST_GROUPED_JSP : Controllers.ENTITY_MERGED_PROP_LIST_UNGROUPED_JSP);
            rd.include(req,res);
        } catch (HelpException help){
            doHelp(res);
        } catch (Throwable e) {
            req.setAttribute("javax.servlet.jsp.jspException",e);
            log.error("exception thrown: "+e.getMessage());
            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
            // rd.forward(req, res);  response has already been committed
            rd.include(req, res);
        }
    }



	public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }
    
    private boolean alreadyOnObjectPropertyList(List<ObjectProperty> opList, PropertyInstance pi) {
        if (pi.getPropertyURI() == null) {
            return false;
        }
        for (ObjectProperty op : opList) {
            if (op.getURI() != null && op.getURI().equals(pi.getPropertyURI())) {
                return op.isSubjectSide() == pi.getSubjectSide();
            }
        }
        return false;
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
            tempGroup = pgDao.createTempPropertyGroup(unassignedGroupName,MAX_GROUP_DISPLAY_RANK);
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
                             log.debug("adding property "+p.getEditLabel()+" to members of temp group "+unassignedGroupName);
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
            try {
                if (p1.getGroupURI()!=null) {
                    PropertyGroup pg1 = pgDao.getGroupByURI(p1.getGroupURI());
                    if (pg1!=null) {
                        p1GroupRank=pg1.getDisplayRank();
                    }
                }
            } catch (Exception ex) {
                log.error("Cannot retrieve p1GroupRank for group "+p1.getEditLabel());
            }
            
            int p2GroupRank=MAX_GROUP_RANK;
            try {
                if (p2.getGroupURI()!=null) {
                    PropertyGroup pg2 = pgDao.getGroupByURI(p2.getGroupURI());
                    if (pg2!=null) {
                        p2GroupRank=pg2.getDisplayRank();
                    }
                }
            } catch (Exception ex) {
                log.error("Cannot retrieve p2GroupRank for group "+p2.getEditLabel());
            }
            
            // int diff = pgDao.getGroupByURI(p1.getGroupURI()).getDisplayRank() - pgDao.getGroupByURI(p2.getGroupURI()).getDisplayRank();
            int diff=p1GroupRank - p2GroupRank;
            if (diff==0) {
                diff = determineDisplayRank(p1) - determineDisplayRank(p2);
                if (diff==0) {
                    return p1.getEditLabel().compareTo(p2.getEditLabel());
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
                String tierStr = op.getDomainDisplayTier(); // no longer used: p.isSubjectSide() ? op.getDomainDisplayTier() : op.getRangeDisplayTier();
                try {
                    return Integer.parseInt(tierStr);
                } catch (NumberFormatException ex) {
                    log.error("Cannot decode object property display tier value "+tierStr+" as an integer");
                }
            } else {
                log.error("Property is of unknown class in PropertyRanker()");  
            }
            return 0;
        }
    }
    
    
    private List<Property> collateBySubclass(List<Property> mergedPropertyList) {
    	for( Property prop : mergedPropertyList){
    		if( prop instanceof ObjectProperty && ((ObjectProperty)prop).getCollateBySubclass() ){    			
    			collateBySubclass((ObjectProperty)prop);
    		}
    	}    	
    	return mergedPropertyList;
	}
    
    /**
     * Sort the object property statements for each property so that they 
     * are sorted first by subclass and then by rdfs:label.
     * 
     * This will be tricky since "subclass" is vaguely defined. Here directly 
     * asserted classes are used.
     */
	private void collateBySubclass(ObjectProperty prop) {
		List<ObjectPropertyStatement> orgStmtList = prop.getObjectPropertyStatements();
		if( orgStmtList == null )
			return;
		Map<String,VClass> directClasses = getDirectClasses( getObjectsFromStmts( orgStmtList ) );
		//don't do collateBySubclass if there is only one class
		if( directClasses.size() < 2 )
			prop.setCollateBySubclass(false); //this overrides the value from the model
		else{
			System.out.println("statements for object property: " + orgStmtList.size());
			//get list of direct classes and sort them 
			List<VClass> vclasses = new LinkedList<VClass>(directClasses.values());
			Collections.sort(
					vclasses, 
					new Comparator<VClass>(){
						public int compare(VClass o1, VClass o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
			
			//order object property statements by sorted direct class list
			List<ObjectPropertyStatement> sortedStmtList = new LinkedList<ObjectPropertyStatement>();
			for (VClass clazz : vclasses) {
				// get all obj prop stmts with objects of this class
				List<ObjectPropertyStatement> stmtsForClass = new ArrayList<ObjectPropertyStatement>();
				
				System.out.println("statements for object property: " + orgStmtList.size());
				Iterator<ObjectPropertyStatement> it = orgStmtList.iterator();
				while( it.hasNext()){
					ObjectPropertyStatement stmt = it.next();
					//if (stmt.getObject().getVClasses(true).contains(clazz)) {
					
					Individual obj = stmt.getObject();
					List<VClass> vclassesForObj = obj.getVClasses(true);					
					if (vclassesForObj != null && vclassesForObj.contains(clazz)) {
						System.out.println("adding " + stmt + " to class "
								+ clazz.getURI());
						System.out.println("subjectURI " + stmt.getSubjectURI()
								+ " objectURI" + stmt.getObject().getURI());
						System.out.println("stmtsForclass size: "
								+ stmtsForClass.size());						
						System.out.println("stmtsForclass size: "
								+ stmtsForClass.size());
						
						stmtsForClass.add(stmt);
					}
				}
				
				//bdc34: What do we do if a object individual is directly asserted to two different
				//types?  For now we just show them in whichever type shows up first. related to NIHVIVO-876
				orgStmtList.removeAll(stmtsForClass);				
				
				// rjy7 Fix for NIHVIVO-426 Sort people in organization listing by name, rather than by the position name.
				// This is an ugly hack and we should not refer to ontology properties here. Need a better fix in the long term.
				if (prop.getURI().equals("http://vivoweb.org/ontology/core#organizationForPosition")) {
					sortByRelatedIndividualNames(stmtsForClass, "http://vivoweb.org/ontology/core#positionForPerson");
				} else {
					Collections.sort(stmtsForClass,
							new Comparator<ObjectPropertyStatement>() {
								public int compare(ObjectPropertyStatement o1,
										ObjectPropertyStatement o2) {
									return o1.getObject().getName().compareTo(
											o2.getObject().getName());
								}
							});
				}
				
				System.out.println("stmtsForclass size after sort: "
						+ stmtsForClass.size());
				System.out.println("sortedStmtList size before add: "
						+ sortedStmtList.size());
				sortedStmtList.addAll(stmtsForClass);
				System.out.println("sortedStmtList size after add: "
						+ sortedStmtList.size());
			}
			prop.setObjectPropertyStatements(sortedStmtList);
		}
			
	}

	private void sortByRelatedIndividualNames(List<ObjectPropertyStatement> opStmts, String predicateUri) {
		
        final LinkedHashMap<ObjectPropertyStatement, String> stmtsToNames = new LinkedHashMap<ObjectPropertyStatement, String>(opStmts.size());
        for (ObjectPropertyStatement stmt : opStmts) {
            Individual relatedIndividual = stmt.getObject().getRelatedIndividual(predicateUri);
        	String relatedIndividualName = relatedIndividual != null ? relatedIndividual.getName() : "";
            stmtsToNames.put(stmt, relatedIndividualName);
        }
        // Sort the object property statements by the names
        Collections.sort(opStmts, new Comparator<ObjectPropertyStatement>() { 
            public int compare(ObjectPropertyStatement left, ObjectPropertyStatement right) { 
                return stmtsToNames.get(left).compareTo(stmtsToNames.get(right)); 
            } 
        });	
	}

	private List<Individual> getObjectsFromStmts(List<ObjectPropertyStatement> orgStmtList) {
		List<Individual> individuals = new LinkedList<Individual>();
		for( ObjectPropertyStatement stmt : orgStmtList ){
			individuals.add( stmt.getObject() );
		}			
		return individuals;
	}

	private Map<String,VClass> getDirectClasses(List<Individual> objectsFromStmts) {
		Map<String,VClass> directClasses = new HashMap<String,VClass>();

		for (Individual ind : objectsFromStmts) {
			for (VClass clazz : ind.getVClasses(true)) {
				directClasses.put(clazz.getURI(),clazz);
			}
		}
		return directClasses;
	}

	/**
	 * Look for filter in servlet context and filter properties with it if there is one.
	 * 
	 * This allows a vitro instance to have specialized filtering for display.  It was originally
	 * created to deal with problems caused by custom short views.
	 * * 
	 * @param objectPropertyList
	 * @param wdf
	 * @return
	 */
	private Individual filterFromContext(Individual ind ) {
		try{
			UnaryFunctor<Individual,Individual> filter = getMergedPropertyListFilter(getServletContext()); 
			if( filter == null )
				return ind;
			else
				return filter.fn(ind);			
		}catch(Throwable t){
			log.error(t,t);
		}
		return ind;
	}
	
	public static void setMergedPropertyListFilter( UnaryFunctor<Individual,Individual>fn, ServletContext sc){
		sc.setAttribute("EntityMergedPropertyListController.toFilteringIndividual", fn);	
	}
	
	public static UnaryFunctor<Individual,Individual> getMergedPropertyListFilter(  ServletContext sc){
		return(UnaryFunctor<Individual,Individual>)sc.getAttribute("EntityMergedPropertyListController.toFilteringIndividual");	
	}
}
