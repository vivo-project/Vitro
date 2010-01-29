package edu.cornell.mannlib.vitro.webservices;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.utils.VitroFilterFactory;
import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.util.ListIterator;

/**

 * This is exposed by apache axis to provide a web service where folks can
 * get tabs and simple entities.  Jim doesn't seem to be using the properties so    
 * they are commented out.
 *
 *  
 * Created: Wed Aug 16 11:36:15 2006
 * @version 1.0
  */
public class VitroWs {
    //private VitroFacade vf;
    private WebappDaoFactory webDaoFactory;
    private ApplicationBean appBean;
    
    public VitroWs() {
        //System.out.println("is this even getting deployed?");
        //wow, this is how to get a servlet context in axis.
        HttpServlet srv =
            (HttpServlet)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLET);
        ServletContext context = srv.getServletContext(); 
        //vf = VitroDataSourceSetup.getFacade(context);
        webDaoFactory = (WebappDaoFactory)context.getAttribute("webappDaoFactory");

        appBean = new ApplicationBean(); //is this the correct way to get one of these?
    }

    /**
       Gets a tab with related entities.  The returned Tab does not have
       child tab list filled.
    */
    public Tab getFullTab(int tabid, int portalid, int depth, boolean withEntities){
        Tab tab = null;
        //System.out.println("is this even getting deployed?");
        VitroFilters dateFilter = VitroFilterFactory.getSunsetWindowFilter(new Date());

        WebappDaoFactory wdf = new WebappDaoFactoryFiltering(webDaoFactory, dateFilter);
        TabDao tabdao= wdf.getTabDao();
        tab = tabdao.getTab(tabid, PUBLIC_AUTH_LEVEL,appBean, depth);
        
        if( withEntities )        
            tab.setRelatedEntityList(tab.getRelatedEntityList(null));//null indicates no alpha filtering
        else
            tab.setRelatedEntityList(Collections.EMPTY_LIST);
               
        strip(tab);
        tab.placeEntityFactory(null);
        return tab;
    }

    /**       Gets Entity with properties filled out.    */
    public Individual getFullEntityById(String entityUri, int portalid){
        //System.out.println("is this even getting deployed?");
        //IndividualWebapp entity = vf.entityById(entityId);
        Individual entity = webDaoFactory.getIndividualDao().getIndividualByURI( entityUri );
        setUpEntity(entity);
        setUpDescription(entity);
        return entity;
    }

    public List getTabsForPortal( int portalid ){
        //System.out.println("is this even getting deployed?");
        //return vf.getTabsForPortal(portalid);
        return webDaoFactory.getTabDao().getTabsForPortal(portalid);
    }                                                                               

    private void setUpEntity(Individual entity){
//        //webDaoFactory.getCoreDaoFactory().getEnts2EntsDao().fillExistingEnts2Ents( entity ); //this may filter entitys in the ents2ents list
//        //webDaoFactory.getPropertyWebappDao().fillEntityProperties( entity );

        //webDaoFactory.getDataPropertyStatementDao().fillExistingDataPropertyStatementsForIndividual( entity );

        webDaoFactory.getObjectPropertyStatementDao().fillExistingObjectPropertyStatements(entity);
        webDaoFactory.getIndividualDao().fillVClassForIndividual( entity );
        webDaoFactory.getLinksDao().addLinksToIndividual( entity );
        entity.setKeywords(webDaoFactory.getIndividualDao().getKeywordsForIndividual(entity.getURI()));
        
        // jc55 causes problems with web services if populated
        entity.setHiddenFromDisplayBelowRoleLevel(null);
        entity.setProhibitedFromUpdateBelowRoleLevel(null);
//        //entity.sortForDisplay();
    }
    
    /**
     * Set entity up with a description from data props if there is none.
     *
     * @param entity
     */
    private void setUpDescription(Individual entity) {
        if( entity != null && 
                (entity.getDescription() == null || entity.getDescription().length() == 0)){
            //if there is nothing in the desc then try to get a dataprop and stick that in the desc
            List e2d = entity.getDataPropertyStatements();
            if( e2d != null && e2d.size() > 0 ){    
                Iterator it = e2d.iterator();   
                while(it.hasNext()){    
                    DataPropertyStatement data = (DataPropertyStatement) it.next(); 
                    if( RESEARCH_FOCUS_DATAPROP_URI.equals( data.getDatapropURI() )
                            && data.getData() != null && data.getData().length() > 0 ){
                        entity.setDescription(data.getData());
                        break;
                    }
                }
            }
        }             
    }


    private void strip(Tab tab){
        if( tab == null ) return;
        if( tab.getChildTabs() != null ){
            for( Tab childTab : tab.getChildTabs()){
                childTab.setChildTabs(null);
                childTab.setRelatedEntityList(Collections.EMPTY_LIST);
            }
        }
        if( tab.getRelatedEntities() != null ){
            for( Individual ind : tab.getRelatedEntities()){
                strip(ind);
            }
        }
    }

    private void strip(Individual ind){                          
        webDaoFactory.getObjectPropertyDao().fillObjectPropertiesForIndividual(ind);
        
        List<ObjectProperty> props = ind.getObjectPropertyList();
                                
        if( props != null && props.size() > 0 ){           
            ListIterator<ObjectProperty> iterator = props.listIterator();
            while(iterator.hasNext()){
                ObjectProperty prop= iterator.next();
                
                // jc55 unlikely to be the source of the problem, but try anyway
                prop.setProhibitedFromUpdateBelowRoleLevel(null);
                prop.setHiddenFromDisplayBelowRoleLevel(null);
                
                if(! (    HAS_TEACHER.equals( prop.getURI() ) 
                       || IN_SEMESTER.equals( prop.getURI()) )){
                    iterator.remove();
                }
            }   
        }

        ind.setDatatypePropertyList(Collections.EMPTY_LIST);
        ind.setDataPropertyStatements(Collections.EMPTY_LIST);
        ind.setKeywords(Collections.EMPTY_LIST);
    }

     
    final int PUBLIC_AUTH_LEVEL = 0;
    final String RESEARCH_FOCUS_DATAPROP_URI = "http://vivo.library.cornell.edu/ns/0.1#researchFocus";
    
    final String HAS_TEACHER = "http://vivo.library.cornell.edu/ns/0.1#SemesterCourseHasTeacherPerson";
    final String IN_SEMESTER = "http://vivo.library.cornell.edu/ns/0.1#SemesterCourseOccursInSemester";
}
