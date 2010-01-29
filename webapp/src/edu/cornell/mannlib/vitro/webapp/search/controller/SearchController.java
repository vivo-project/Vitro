package edu.cornell.mannlib.vitro.webapp.search.controller;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.util.ArrayList;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClassList;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.Searcher;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroHighlighter;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryWrapper;

/**
 * This controller will execute the search and process
 * the search resutls into entites.  Those entites will be stuffed
 * into the request scope and the controll will be passed to a
 * jsp for rendering html.
 *
 * -- What are the parameters that this servlet uses?
 * This servlet uses the PortalFlag to represent which portal and how
 * the portals will be accounted for in the search.  So you should look
 * at edu.cornell.mannlib.vitro.flags.PortalFlag to find out what http parameters
 * cause it to be set to what states.
 *
 * Here is a quick run down of the interesting ones:
 * filter - if set to 'false' no flag filtering will happen
 * flag1  - if set to 'nofiltering' no flag filtering will happen
 * querytext - query text to use for search.
 *
 * This controller will 1) use VitroServlet to process the httpRequest
 * for parameters 2) build a query and execute it on the
 * index 3) take the results of the query, which are just entity ids,
 * and turn them into short entities 4) highlight the entities.
 * 5) sort into ClassGroups 6) stick into the request scope.
 *
 *
 * This controller will do the searing for any kind of search
 * back end. There are two things that make this possible: 1
 * interfaces, 2 stashing a pointer to the instance of the search
 * that we intend to use.
 *
 * 1) an interface is a definition of method signatures with no
 * code. This is similar to a c header file. It allows us to define
 * a set of methods we can call without having to know what will
 * happen for us to get the outputs of those methods.
 *
 * 2) Once we have this we need a way to get an instantiated object
 * that implements the interface we are interested in using. There
 * are different ways of doing this. We will just get an object out
 * of the application scope. This object was set when the context
 * started up and can be set in the web.xml. See LuceneSetup.java
 * for an exmple.
 */
public class SearchController extends VitroHttpServlet{
    private static final Log log = LogFactory.getLog(SearchController.class.getName());
    String NORESULT_MSG = "The search returned no results.";

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     *
     *
     * @author bdc34
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            //this will set up the portal flag, which is where the search query
            //will get ALL of the information about portal filtering.
            super.doGet(request,response);
            VitroRequest vreq = new VitroRequest(request);
            Portal portal = vreq.getPortal();
            PortalFlag portalFlag = vreq.getPortalFlag();

            // ////////////////////////////////////////////////////////////////////
            // FIRST: we want to get the search object
            // All that we know about it is that it implements the Searcher
            // interface.
            ServletContext context = getServletContext();
            Searcher searcher = (Searcher) context.getAttribute(Searcher.class.getName());

            // ///////////////////////////////////////////////////////////////////
            // NEXT: we make a query. The searcher object has a method that
            // makes a
            // query for us. This allows the search object to implement specific
            // transformations from the request to the query.
            // Possible improvement: make some kind of standard queryRequest
            // object that all QueryFactory objs take.

            VitroQueryFactory qFactory = searcher.getQueryFactory();
            VitroQuery query = qFactory.getQuery(vreq, portalFlag);

            // qFactory.getQuery() will return null when there is not enough
            // information in the request to make a query.
            if (query == null ) {
                doNoQuery(request, response);
                return;
            }

            // ////////////////////////////////////////////////////////////////////
            // Now we want to do the search
            long start = System.currentTimeMillis();
            List hits = null;
            try{
                hits = searcher.search(query);
            }catch(Throwable t){
                log.error("in first pass at search: " + t);
                // this is a hack to deal with odd cases where search and index threads interact
                try{                    
                    Thread.currentThread().sleep(150);
                    hits = searcher.search(query);
                }catch (SearchException ex){
                    log.error(ex);
                    String msg = makeBadSearchMessage(query,ex.getMessage());
                    if(msg == null ) msg = "<p>The search request contained errors.</p>";
                    doFailedSearch(request, response, msg, query);
                    return;
                }
            }

            long end = System.currentTimeMillis();
            long time=end-start;

            if (hits == null || hits.size() < 1) {
                doFailedSearch(request, response, NORESULT_MSG, query);
                return;
            }
            log.debug("found "+hits+" hits");

            // now we have hits which are only entity ids
            // so convert the hits to usable entity beans
            List beans = makeUsableBeans(hits,vreq);
            log.debug("makeUsableBeans() succeeded");

            //sort by ClassGroup and the sort the ClassGroups
            Map collatedListOfLists = null;
            if (beans != null && beans.size() > 0) { // collate entities
                collatedListOfLists = collate(beans,vreq); // sort each classgroup
                // list by a default
                // setting if available
                // sort each class group
                collatedListOfLists = sortClassGroups(collatedListOfLists);
            }
            log.debug("sort by classgroups succeeded");

            VClassGroup.removeEmptyClassGroups(collatedListOfLists);
            log.debug("empty classgroups removed");
            collateClassGroupsByVClass(collatedListOfLists.values());
            log.debug("classgroups collated");

            //Now collatedLitOfLists has Map of VCLassGroups
            //and each VClassGroup has a list of VCLassList, each with a list of entities.

            //attempt to do highlighting
            try {
                beans = highlightBeans( beans , searcher.getHighlighter(query) );
            } catch (Exception e) {
                log.error("Error highlighting search result beans", e);
            }
            log.debug("beans highlighted");

            // ////////////////////////////////////////////////////////////////////
            // stick the data in the requestScope and sessionScope

            // stick the results in the requestScope:
            request.setAttribute("collatedResultsLists", collatedListOfLists);
            request.setAttribute("collatedGroupNames", collatedListOfLists
                    .keySet());

            String terms = query.getTerms();

            request.setAttribute("title", terms+" - "+portal.getAppName()+" Search Results" );
            request.setAttribute("bodyJsp", Controllers.SEARCH_BASIC_JSP);
            request.setAttribute("css", Controllers.TOGGLE_SCRIPT_ELEMENT);
            request.setAttribute("querytext", terms);

            VitroQueryWrapper queryWrapper =
                new VitroQueryWrapper(query,
                        (VitroHighlighter)searcher.getHighlighter(query),
                        2, time);
            request.getSession(true).setAttribute("LastQuery", queryWrapper);
                        
            log.debug("query wrapper created");

            // ////////////////////////////////////////////////////////////////////
            // FINALLY: send off to the BASIC_JSP to get turned into html

            RequestDispatcher rd = request
                    .getRequestDispatcher(Controllers.BASIC_JSP);
            rd.forward(request, response);
        } catch (Throwable e) {
            log.error("SearchController.doGet(): " + e);            
            doSearchError(request, response, e.getMessage(), null);
            return;
        }
    }

    private void doSearchError(HttpServletRequest request,
            HttpServletResponse response, String message, Object object)         
        throws ServletException, IOException {
            Portal portal = (new VitroRequest(request)).getPortal();            

            request.setAttribute("bodyJsp", Controllers.SEARCH_ERROR_JSP);
            RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
            rd.forward(request, response);
    }

    private List highlightBeans(List beans, VitroHighlighter highlighter) {
        Iterator it = beans.iterator();
        while(it.hasNext()){
            Object obj = it.next();
            if( obj instanceof Individual){
                Individual ent = (Individual)obj;
                highlighter.fragmentHighlight(ent);
            }
        }
        return beans;
    }

    private void doNoQuery(HttpServletRequest request,
                           HttpServletResponse response)
    throws ServletException, IOException {
            Portal portal = (new VitroRequest(request)).getPortal();
            request.setAttribute("title", "Search "+portal.getAppName());
            request.setAttribute("bodyJsp", Controllers.SEARCH_FORM_JSP);

            RequestDispatcher rd = request
                    .getRequestDispatcher(Controllers.BASIC_JSP);
            rd.forward(request, response);
    }

    private void doFailedSearch(HttpServletRequest request,
            HttpServletResponse response, String message, VitroQuery query)
        throws ServletException, IOException {
        Portal portal = (new VitroRequest(request)).getPortal();
        if( query != null ){
            String terms = query.getTerms();
            request.setAttribute("querytext", terms);
            request.setAttribute("title", terms+" - "+portal.getAppName()+" Search" );
        }else{
            request.setAttribute("title", portal.getAppName()+" Search" );
            request.setAttribute("querytext", "");
        }
        if( message != null && message.length() > 0)
            request.setAttribute("message", message);

        request.setAttribute("bodyJsp", Controllers.SEARCH_FAILED_JSP);
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        rd.forward(request, response);
    }


    /**
     * Makes a message to display to user for a bad search term.
     * @param query
     * @param exceptionMsg
     */
    private String makeBadSearchMessage(VitroQuery query, String exceptionMsg){
        String term = query.getTerms();
        String rv = "";
        try{
            //try to get the column in the search term that is causing the problems
            int coli = exceptionMsg.indexOf("column");
            if( coli == -1) return "";
            int numi = exceptionMsg.indexOf(".", coli+7);
            if( numi == -1 ) return "";
            String part = exceptionMsg.substring(coli+7,numi );
            int i = Integer.parseInt(part) - 1;

            // figure out where to cut preview and post-view
            int errorWindow = 5;
            int pre = i - errorWindow;
            if (pre < 0)
                pre = 0;
            int post = i + errorWindow;
            if (post > term.length())
                post = term.length();
            // log.warn("pre: " + pre + " post: " + post + " term len:
            // " + term.length());

            // get part of the search term before the error and after
            String before = term.substring(pre, i);
            String after = "";
            if (post > i)
                after = term.substring(i + 1, post);

            rv = "The search term had an error near <span class='searchQuote'>"
                    + before + "<span class='searchError'>" + term.charAt(i)
                    + "</span>" + after + "</span>";
        } catch (Throwable ex) {
            return "";
        }
        return rv;
    }

    /**
     * Upgrades beans using individual entity upgrade().
     *
     * @param hits
     * @return
     */
    @SuppressWarnings("unchecked")
    private List makeUsableBeans(List hits,VitroRequest vreq) {
        if (hits == null || hits.size() == 0) {
            return new ArrayList();
        }
        LinkedList beans = new LinkedList();
        Iterator it = hits.iterator();
        while (it.hasNext()) {
            try {
                Object obj = it.next();
                if (obj != null) {
                    obj = vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI(((Individual)obj).getURI());
                    //null would indicate that the entity that was found in
                    //the search index is no longer in the system.
                    if( obj != null ) {
                        beans.add(obj);
                    }
                }
            } catch (Exception e) {
                log.error(e.getStackTrace());
            }
        }
        return beans;
    }

    public static final String NO_GRP_FOUND = "others";

    public static final String TABS = "tabs";

    /**
     * Make a map of VClassGroup objs to Lists of entites. There will also be a
     * map from TABS -> a List of tabs and NO_CLASSGRP_FOUND -> a list of
     * Entities where the class or classgroup could not be found.
     *
     * @param beans
     * @return
     */
    private Map /* of List */collate(List beans, VitroRequest vreq) {
        if (beans == null || beans.size() == 0) {
            return new HashMap();
        }
        /* make a hash map of VClassGroup -> List[ ent, ent , ent ... ] */
        Map groups = vreq.getWebappDaoFactory().getVClassGroupDao().getClassGroupMap();
        Map groupsByURI = makeGroupURIBasedMap(groups);

        VClass vclass = null;
        VClassGroup tabs = null, other = null;
        Iterator it = beans.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof Individual){
                vclass = (VClass)((Individual) obj).getVClass();
                if( vclass != null && vclass.getGroupURI() != null ){                                   
                    putInGroupsList(groupsByURI, vclass.getGroupURI(), obj);                    
                } else { /* didn't find a group */
                    if( !groupsByURI.containsKey("vitro://NullClassGroup") ){
                        other = new VClassGroup("vitro://NullClassGroup",NO_GRP_FOUND);
                        groupsByURI.put("vitro://NullClassGroup", other);
                        groups.put(NO_GRP_FOUND,other);
                    }
                    putInGroupsList(groupsByURI, "vitro://NullClassGroup", obj);
                }
            } else if (obj instanceof Tab) {
                if( !groupsByURI.containsKey("vitro://NullTabGroup") ){
                    tabs = new VClassGroup("vitro://NullTabGroup",TABS);
                    groupsByURI.put(0, tabs );
                    groups.put(TABS,tabs);
                }
                putInGroupsList(groupsByURI, null, obj);
            } 
        }
        return groups;
    }

    private void putInGroupsList(Map groupsById, String key, Object obj) {
        List classList = (List) groupsById.get(key);
        if (classList == null) {/* make new list if this is a new grp */
            classList = new LinkedList();
            groupsById.put(key, classList);
        }
        classList.add(obj);
    }

    /**
     * Make a new map from the Integer() -> VClassGroup.
     * @param groups
     * @return
     */
    private Map makeGroupURIBasedMap( Map groups){
        LinkedHashMap <String,VClassGroup> map=
            new LinkedHashMap<String, VClassGroup>();
        for( Object obj: groups.values() ){
            if( !(obj instanceof VClassGroup) )
                throw new Error("VitroRequest.getWebappDaoFactory().getVClassGroupDao().getClassGroupMap() returned something other than a VClassGroup");
            VClassGroup grp = (VClassGroup)obj;
            map.put(grp.getURI() , grp );
        }
        return map;
    }



    /**
     * Collate a Collection of Entities into VClassLists.
     * Sort the vclasses.
     *
     * @param ents
     * @return
     */
    @SuppressWarnings({ "unused", "unchecked" })
    private LinkedList /*VClassList*/ collateVClasses(Collection ents){
        log.debug("in collateVClasses ");
        if( ents == null ) return null;
        Map <String, VClassList> vclasses = new HashMap<String, VClassList>( );
        Iterator it = ents.iterator();
        while( it.hasNext()){
            Individual ent = (Individual) it.next();
            if(vclasses.containsKey(ent.getVClassURI()+"")){
                vclasses.get(ent.getVClassURI()+"").getEntities().add(ent);
            }else{
                List <Individual> a = new LinkedList<Individual>();
                a.add(ent);
                VClass waVClass = (VClass)ent.getVClass();
                VClassList vcl = null;
                if (waVClass==null) {
                    VClass vcw = new VClass();
                    vcw.setURI("vitro://UnknownClass");
                    vcw.setName("Unknown Class");
                    vcl = new VClassList(vcw,a);
                } else {
                    vcl = new VClassList(waVClass,a);
                }

                vclasses.put(ent.getVClassURI()+"", vcl);
            }
        }
        LinkedList vclassList = new LinkedList(vclasses.values());

        if( log.isDebugEnabled() ){
            it = vclassList.iterator();
            while(it.hasNext()){
                log.debug(((VClass)it.next()).getName());
            }
        }

        //This is the object that will compare vclasses for
        //sort order in the search results

        Comparator vclassComparator = new Comparator<Object>(){
            public int compare(Object o1, Object o2) {
                if( o1 == null && o2 == null) return 0;
                if( o1 == null ) return 1;
                if( o2 == null ) return -1;

                if( o1 instanceof VClass && o2 instanceof VClass)
                    return compare((VClass) o1, (VClass) o2);
                //what to do when we don't know what we have:
                return o2.hashCode() - o1.hashCode();

            }
            private int compare(VClass v1, VClass v2){
                if( v1== null && v2 == null ) return 0;
                if( v1 == null ) return -1;
                if( v2 == null ) return 1;
                if( v1.getURI().equals(v2.getURI()) )return 0;

                //check if display ranks are different
                int v1rank=v1.getDisplayRank();
                int v2rank=v2.getDisplayRank();

                if( v1rank == v2rank )
                    //same rank, sort by vclass name
                    return v1.getName().compareToIgnoreCase( v2.getName());

                //bdc34: this is not working as I expect it to
                // This is what I think:
                // comparators should return 0 same; -1 2nd object is first;
                // +1 1st object should be first
                //
                // vclass rank should work such that display Rank 1 get to go first,
                // 99 gets to go last.  All vclasses with the same rank should get
                // alpha sorting.
                //
                // So I would guess that returning +1 when v1.disRank < v2.disRank
                // would make sense but that seems to be worng.
                //
                // I tried the other way and things seem to work correctly.
                // I might just be confused about the values that Comparator
                // object should return

                if( v1rank > v2rank )
                    return 1;
                else
                    return -1;
            }
        };
         Collections.sort(vclassList,vclassComparator);
         if( log.isDebugEnabled()){
             it = vclassList.iterator();
             while(it.hasNext()){
                 log.debug(((VClass)it.next()).getName());
             }
         }
         return vclassList;
    }

    private void collateClassGroupsByVClass( Collection cGrps ){
        if( cGrps == null ) return;
        Iterator it = cGrps.iterator();
        while(it.hasNext()){
            VClassGroup grp = (VClassGroup) it.next();
            List vclassLists = collateVClasses( grp );
            grp.clear();
            grp.addAll(vclassLists);
        }
    }

    /**
     * Try to sort the classGroup lists.  For each item in the map, check if it
     * is a collection, if yes, try to sort as if things in the list are Entity
     * objects.
     *
     * @return
     */
    private Map /* of List */sortClassGroups(Map classGroups) {
        if( classGroups == null || classGroups.values() == null )
            return classGroups;

        //first we sort the entities in each grouup.
        Iterator it = classGroups.values().iterator();

        //wow, I have to sit down and learn this generic stuff.
        Comparator entComparator = new Comparator<Object>(){
            public int compare(Object o1, Object o2) {
                if( o1 == null && o2 == null) return 0;
                if( o1 == null ) return 1;
                if( o2 == null ) return -1;

                if( o1 instanceof VClass && o2 instanceof VClass)
                    return compareClasses(((VClass) o1), ((VClass) o2));

                if( o1 instanceof Individual && o2 instanceof Individual)
                    return compareEnts((Individual)o1, (Individual)o2);
                if( o1 instanceof Individual && ! (o2 instanceof Individual))
                    return -1;
                if( !(o1 instanceof Individual) && o2 instanceof Individual)
                    return 1;
                //what to do when we don't know what we have:
                return o2.hashCode() - o1.hashCode();

            }
            //here we sort entities
            private int compareEnts(Individual o1, Individual o2){
                int vclassCompare = compareClasses((VClass) o1.getVClass(),
                                                   (VClass) o2.getVClass() );
                //sort by vclass first, then by entity name
                if( vclassCompare != 0 )
                    return vclassCompare;
                else
                    return ((o1.getName()==null)?"":o1.getName()).compareToIgnoreCase((o2.getName()==null)?"": o2.getName());
            }

            private int compareClasses(VClass v1, VClass v2){
                if( v1== null && v2 == null ) return 0;
                if( v1 == null ) return -1;
                if( v2 == null ) return 1;
                if( v1.getURI().equals(v2.getURI()) )return 0;

                //check if display ranks are different
                int v1DisplayRank=v1.getDisplayRank(),
                    v2DisplayRank=v2.getDisplayRank();
                int displayRankDiff = v2DisplayRank - v1DisplayRank;

                if( displayRankDiff != 0 ) return displayRankDiff;

                //same rank, sort by vclass name
                return v1.getName().compareToIgnoreCase( v2.getName());
            }
        };

        while(it.hasNext()){
            Object obj = it.next();
            if(!( obj instanceof Collection))
                continue; //skip this one if it is not a collection
            List <List<Object>>entList = (List<List<Object>>)obj;
            Collections.sort(entList, entComparator);
        }

//      //now we sort the classgroups
//      Comparator cgComparator = new Comparator<Object>(){
//          public int compare(Object o1, Object o2){
//              if( o1 == null && o2 == null) return 0;
//              if( o1 == null ) return 1;
//              if( o2 == null ) return -1;
//
//              if( o1 instanceof ClassGroup && o2 instanceof ClassGroup)
//                  return compare((ClassGroup)o1, (ClassGroup)o2);
//              if( o1 instanceof ClassGroup && ! (o2 instanceof ClassGroup))
//                  return -1;
//              if( !(o1 instanceof ClassGroup) && o2 instanceof ClassGroup)
//                  return 1;
//              //what to do when we don't know what we have:
//              return o2.hashCode() - o1.hashCode();
//          }
//          private int compare(ClassGroup c1, ClassGroup c2){
//              return 0;
//          }
//      };
        return classGroups;
    }

}
