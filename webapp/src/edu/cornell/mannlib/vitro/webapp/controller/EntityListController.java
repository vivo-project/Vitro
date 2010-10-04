/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.TabEntitiesController.PageRecord;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.flags.FlagException;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;

public class EntityListController extends VitroHttpServlet {

    public static final int ENTITY_LIST_CONTROLLER_MAX_RESULTS = 30000;
    public static final int INDIVIDUALS_PER_PAGE = 30;
    
    long startTime = -1;
    
    private static final Log log = LogFactory.getLog(EntityListController.class.getName());

    /**
     * This generates a list of entities and then sends that
     * off to a jsp to be displayed.
     *
     * Expected parameters:
     *
     * Expected Attributes:
     * entity - set to entity to display properties for.
     *
     * @author bdc34
     */
    public void doGet( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        startTime = System.currentTimeMillis(); // TODO: remove
        try {
            super.doGet(req, res);
            VitroRequest vreq = new VitroRequest(req);
            Object obj = req.getAttribute("vclass");
            VClass vclass=null;
            if( obj == null ) { // look for vitroclass id parameter
                String vitroClassIdStr=req.getParameter("vclassId");
                if (vitroClassIdStr!=null && !vitroClassIdStr.equals("")) {
                    try {
                        vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vitroClassIdStr);
                        if (vclass == null) {
                            log.error("Couldn't retrieve vclass "+vitroClassIdStr);
                            res.sendRedirect(Controllers.BROWSE_CONTROLLER+"?"+req.getQueryString());
                        }
                    } catch (Exception ex) {
                        throw new HelpException("EntityListController: request parameter 'vclassId' must be a URI string");
                    }
                }
            } else if (obj instanceof VClass) {
                vclass = (VClass)obj;
            } else {
                throw new HelpException("EntityListController: attribute 'vclass' must be of type "
                        + VClass.class.getName() );
            }
            
            if (vclass!=null)
                doVClass(vreq, res, vclass);
            else
                log.debug("no vclass found for " + obj);
            
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

    /**
     * Perform the actions needed for the entity list jsp.
     * @param request
     * @param res
     * @param vclass
     * @throws ServletException
     * @throws IOException
     */
    private void doVClass(VitroRequest request, HttpServletResponse res, VClass vclass)
    throws ServletException, IOException, FlagException {
        IndexSearcher index = LuceneIndexFactory.getIndexSearcher(getServletContext());
        boolean isSinglePortal = request.getWebappDaoFactory().getPortalDao().isSinglePortal();
        
        Portal portal = request.getPortal();
        int portalId = 1;
        if( portal != null )
            portalId = portal.getPortalId();
        
                 
        String alpha = request.getParameter("alpha");
        int page = getPage(request);
        IndividualDao indDao = request.getWebappDaoFactory().getIndividualDao();                        
        
        //make lucene query for this rdf:type
        Query query = getQuery(vclass.getURI(),alpha, isSinglePortal, portalId);        
        
        //execute lucene query for individuals of the specified type
        TopDocs docs = index.search(query, null, 
                ENTITY_LIST_CONTROLLER_MAX_RESULTS, 
                new Sort(Entity2LuceneDoc.term.NAMEUNANALYZED));    
        
        if( docs == null ){
            log.error("Search of lucene index returned null");
            throw new ServletException("Search of lucene index returned null");
        }
        
        //get list of individuals for the search results
        int size = docs.totalHits;
        // don't get all the results, only get results for the requestedSize
        List<Individual> individuals = new ArrayList<Individual>(INDIVIDUALS_PER_PAGE);
        int individualsAdded = 0;
        int ii = (page-1)*INDIVIDUALS_PER_PAGE;               
        while( individualsAdded < INDIVIDUALS_PER_PAGE && ii < size ){
            ScoreDoc hit = docs.scoreDocs[ii];
            if (hit != null) {
                Document doc = index.doc(hit.doc);
                if (doc != null) {                                                                                        
                    String uri = doc.getField(Entity2LuceneDoc.term.URI).stringValue();
                    Individual ind = indDao.getIndividualByURI( uri );                                
                    individuals.add( ind );                         
                    individualsAdded++;                    
                } else {
                    log.warn("no document found for lucene doc id " + hit.doc);
                }
            } else {
                log.debug("hit was null");
            }                         
            ii++;            
        }   
        
        
        request.setAttribute("servlet",Controllers.ENTITY_LIST);
        request.setAttribute("vclassId", vclass.getURI());
        request.setAttribute("controllerParam","vclassId=" + URLEncoder.encode(vclass.getURI(),"UTF-8"));
        request.setAttribute("count", size);
        
        if( size > INDIVIDUALS_PER_PAGE ){
            request.setAttribute("showPages", Boolean.TRUE);
            List<PageRecord> pageRecords = TabEntitiesController.makePagesList(size, INDIVIDUALS_PER_PAGE, page);
            request.setAttribute("pages", pageRecords);                    
        }
        request.setAttribute("showAlpha","1");
        request.setAttribute("letters",Controllers.getLetters());
        request.setAttribute("alpha",alpha);
        
        request.setAttribute("totalCount", size);
        request.setAttribute("entities",individuals);
        if (individuals == null) 
            log.debug("entities list is null for vclass " + vclass.getURI());
        

        VClassGroup classGroup=vclass.getGroup();
        if (classGroup==null) {
            request.setAttribute("title",vclass.getName()/* + "&nbsp;("+vclass.getEntityCount()+")"*/);
        } else {
            request.setAttribute("title",classGroup.getPublicName());
            request.setAttribute("subTitle",vclass.getName()/* + "&nbsp;("+vclass.getEntityCount()+")"*/);
        }
        
        //FINALLY: send off to the BASIC_JSP to get turned into html
        request.setAttribute("bodyJsp",Controllers.ENTITY_LIST_JSP);
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);

        // use this for more direct debugging: RequestDispatcher rd = request.getRequestDispatcher(Controllers.ENTITY_LIST_JSP);
        res.setContentType("text/html; charset=UTF-8");
        request.setAttribute("pageTime", System.currentTimeMillis()-startTime);
        rd.include(request,res);
    }

    private int getPage(VitroRequest request) {
        String pageStr = request.getParameter("page");
        if( pageStr != null ){
            try{
                return Integer.parseInt(pageStr);                
            }catch(NumberFormatException nfe){
                log.debug("could not parse page parameter");
                return 1;
            }                
        }else{                   
            return 1;
        }
    }

    private BooleanQuery getQuery(String vclassUri,  String alpha , boolean isSinglePortal, int portalId){
        BooleanQuery query = new BooleanQuery();
        try{      
           //query term for rdf:type
           query.add(
                   new TermQuery( new Term(Entity2LuceneDoc.term.RDFTYPE, vclassUri)),
                   BooleanClause.Occur.MUST );                          
                                         
           //check for portal filtering 
           if( ! isSinglePortal ){               
               if( portalId < 16 ){ //could be a normal portal
               query.add(
                       new TermQuery( new Term(Entity2LuceneDoc.term.PORTAL, Integer.toString(1 << portalId ))),
                       BooleanClause.Occur.MUST);
           }else{ //could be a combined portal
                   BooleanQuery tabQueries = new BooleanQuery();
                   Long[] ids= FlagMathUtils.numeric2numerics(portalId);
                   for( Long id : ids){                       
                       tabQueries.add(
                               new TermQuery( new Term(Entity2LuceneDoc.term.PORTAL,id.toString()) ),
                               BooleanClause.Occur.SHOULD);
                   }
                   query.add(tabQueries,BooleanClause.Occur.MUST);
               }
           }
                                      
           //Add alpha filter if it is needed
           Query alphaQuery = null;
           if( alpha != null && !"".equals(alpha) && alpha.length() == 1){      
               alphaQuery =    
                   new PrefixQuery(new Term(Entity2LuceneDoc.term.NAMEUNANALYZED, alpha.toLowerCase()));
               query.add(alphaQuery,BooleanClause.Occur.MUST);
           }                      
                           
           log.debug("Query: " + query);
           return query;
       }catch (Exception ex){
           log.error(ex,ex);
           return new BooleanQuery();        
       }        
    }    
    
    
    private void doHelp(HttpServletResponse res)
    throws IOException, ServletException {
        ServletOutputStream out = res.getOutputStream();
        res.setContentType("text/html; charset=UTF-8");
        out.println("<html><body><h2>Quick Notes on using EntityList:</h2>");
        out.println("<p>request.attributes 'entities' must be set by servlet before calling."
                +" It must be a List of Entity objects </p>");
        out.println("</body></html>");
    }

    private class HelpException extends Throwable{
        public HelpException(String string) {
            super(string);
        }
    }       
}
