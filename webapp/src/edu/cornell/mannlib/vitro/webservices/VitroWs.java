package edu.cornell.mannlib.vitro.webservices;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

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
    //private WebappDaoFactory webDaoFactory;
    //private ApplicationBean appBean;
    private DateTime previousCall;
    
    private ServletContext ctx;
    
    Log log = LogFactory.getLog(VitroWs.class);
    
    public VitroWs() {
        previousCall = (new DateTime()).minusDays(200);
        
        //wow, this is how to get a servlet context in axis.
        HttpServlet srv =
            (HttpServlet)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLET);
        this.ctx = srv.getServletContext(); 
    }

    private WebappDaoFactory getWdf(){
      return    (WebappDaoFactory)ctx.getAttribute("webappDaoFactory");
    }
    
    private ApplicationBean getAppBean(){
        return new ApplicationBean(); //is this the correct way to get one of these?
    }
    
    
    /**
       Gets a tab with related entities.  The returned Tab does not have
       child tab list filled.
       
       withEntities parameter is ignored since it can create huge result sets.
       The tab specified by tabid has entities, any children tabs should not.
       
       depth parameter is only valid for 0 and 1.  All other values will be treated as 1.       
    */
    public Tab getFullTab(int tabid, int portalid, int depth, boolean withEntities){
        if( log.isDebugEnabled() )
            log.debug("calling getFullTab( tabid=" + tabid + ", portalid=" 
                    + portalid + ", depth=" + depth + ", withEntitys="
                    + withEntities + ")");
            
        long wait = checkCongestion();
        if( wait > 0 )
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        Tab tab = null;        
        try{
            VitroFilters dateFilter = VitroFilterUtils.getSunsetWindowFilter(new Date());
                
            WebappDaoFactory wdf = new WebappDaoFactoryFiltering(getWdf(), dateFilter);
            TabDao tabdao= wdf.getTabDao();
            tab = tabdao.getTab(tabid, PUBLIC_AUTH_LEVEL,getAppBean(), depth);
            
            if( tab == null ) return null;
            
            if( withEntities )        
                tab.setRelatedEntityList(tab.getRelatedEntityList(null));//null indicates no alpha filtering
            else
                tab.setRelatedEntityList(Collections.EMPTY_LIST);
                   
            strip( tab );
            tab.placeEntityFactory(null);
        }catch(RuntimeException re){
            log.error("Exception in getFullTab",re);
        }
        return tab;
    }

   

    /**       Gets Entity with properties filled out.    */
    public Individual getFullEntityById(String entityUri, int portalid){
        if( log.isDebugEnabled() )
            log.debug("calling getFullEntityById( entityUri=" + entityUri + 
                    ", " + portalid + ")");
                    
        long wait = checkCongestion();
        if( wait > 0 )
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        
        try {
            if (entityUri==null) {
                return null;
            }
            Individual entity = getWdf().getIndividualDao().getIndividualByURI( entityUri );
            if (entity==null) {
                return null;
            }
            setUpEntity(entity);
            setUpDescription(entity);
            return entity;
        } catch (RuntimeException e) {
            log.error("error in getFullEntityById()" , e);
            return null;
        }
    }

    public List getTabsForPortal( int portalid ){
        if( log.isDebugEnabled() )
            log.debug("calling getTabsForPortal( portalid=" + portalid + ")");
        
        long wait = checkCongestion();
        if( wait > 0 )
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        
        try{
            return getWdf().getTabDao().getTabsForPortal(portalid);
        }catch(RuntimeException e){
            log.error("error in getTabsForPortal()" ,e );
            return Collections.EMPTY_LIST;
        }
    }                                                                               

    private void setUpEntity(Individual entity){
        if (entity != null && entity.getURI()!=null) {
            WebappDaoFactory webDaoFactory = getWdf();
            webDaoFactory.getObjectPropertyStatementDao().fillExistingObjectPropertyStatements(entity);
            webDaoFactory.getIndividualDao().fillVClassForIndividual( entity );
            webDaoFactory.getLinksDao().addLinksToIndividual( entity );
            entity.setKeywords(webDaoFactory.getIndividualDao().getKeywordsForIndividual(entity.getURI()));
            
            // jc55 apparently causing problems for Entrepreneurship web services
            entity.setHiddenFromDisplayBelowRoleLevel(null);
            entity.setProhibitedFromUpdateBelowRoleLevel(null);
        }
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
                    /* If there is no description the overviewStatement or researchFocus get
                     * stuck into the description field.  */
                    DataPropertyStatement data = (DataPropertyStatement) it.next();
                    if( OVERVIEW_STATEMENT_DATAPROP_URI.equals( data.getDatapropURI() )
                        && data.getData() != null && data.getData().length() > 0){
                        //see jira issue VITRO-415
                        //http://issues.library.cornell.edu/browse/VITRO-415
                        entity.setDescription(data.getData());
                        break;
                    }else if( RESEARCH_FOCUS_DATAPROP_URI.equals( data.getDatapropURI() )
                              && data.getData() != null && data.getData().length() > 0 ){
                        entity.setDescription(data.getData());
                        break;
                    }
                }
            }
        }             
    }


    private void strip(Tab tab ){
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
        getWdf().getObjectPropertyDao().fillObjectPropertiesForIndividual(ind);
        
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
        ind.setHiddenFromDisplayBelowRoleLevel(null);
        ind.setProhibitedFromUpdateBelowRoleLevel(null);
        ind.setDatatypePropertyList(Collections.EMPTY_LIST);
        ind.setDataPropertyStatements(Collections.EMPTY_LIST);
        ind.setKeywords(Collections.EMPTY_LIST);
    }

   
    private synchronized int checkCongestion() {
        DateTime now = new DateTime();
        Interval sincePrevious = new Interval(previousCall, now );
        this.previousCall = now;        
        long since = sincePrevious.toDurationMillis();
        
        if( since < 660 )
            return 2000;                
        else if ( since < 1200 )
            return 600;
        else
            return 0;        
    }

    private static final int PUBLIC_AUTH_LEVEL = 0;
    private static final String RESEARCH_FOCUS_DATAPROP_URI = "http://vivo.library.cornell.edu/ns/0.1#researchFocus";
    private static final String OVERVIEW_STATEMENT_DATAPROP_URI = "http://vivo.library.cornell.edu/ns/0.1#overviewStatement";
    
    private static final String HAS_TEACHER = "http://vivo.library.cornell.edu/ns/0.1#SemesterCourseHasTeacherPerson";
    private static final String IN_SEMESTER = "http://vivo.library.cornell.edu/ns/0.1#SemesterCourseOccursInSemester";
}
