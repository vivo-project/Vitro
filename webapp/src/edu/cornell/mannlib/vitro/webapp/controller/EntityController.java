package edu.cornell.mannlib.vitro.webapp.controller;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryWrapper;
import edu.cornell.mannlib.vitro.webapp.web.EntityWebUtils;
import edu.cornell.mannlib.vitro.webapp.web.jsptags.StringProcessorTag;

/**
 * Handles requests for entity information.
 * Calls EntityPropertyListController to draw property list.
 *
 * @author bdc34
 *
 */
public class EntityController extends VitroHttpServlet {
    private static final Log log = LogFactory.getLog(EntityController.class.getName());

    private String default_jsp      = Controllers.BASIC_JSP;
    private String default_body_jsp = Controllers.ENTITY_JSP;
    private ApplicationBean appBean;
    
    /**
     *
     * @author bdc34
     */
    public void doGet( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        try {
            super.doGet(req, res);

            log.debug("In doGet");

            VitroRequest vreq = new VitroRequest(req);
            IndividualDao iwDao = vreq.getWebappDaoFactory().getIndividualDao();
            ObjectPropertyDao opDao = vreq.getWebappDaoFactory().getObjectPropertyDao();

            Individual entity = null;
            try{
                entity = EntityWebUtils.getEntityFromRequest(vreq);
            }catch(Throwable th){
                doHelp(res);
                return;
            }
            if( entity == null ){
                doNotFound(req, res );
                return;
            }
            
            if ( checkForHidden( vreq, entity ) ||
                 checkForSunset( vreq, entity ) ){         
                doNotFound( req, res);
                return;
            } 
            
            //Check if a "relatedSubjectUri" parameter has been supplied, and,
            //if so, retrieve the related individual.t
            //Some individuals make little sense standing alone and should
            //be displayed in the context of their relationship to another.
            String relatedSubjectUri = vreq.getParameter("relatedSubjectUri"); 
            if (relatedSubjectUri != null) {
            	Individual relatedSubjectInd = iwDao.getIndividualByURI(relatedSubjectUri);
            	if (relatedSubjectInd != null) {
            		vreq.setAttribute("relatedSubject", relatedSubjectInd);
            	}
            }
            String relatingPredicateUri = vreq.getParameter("relatingPredicateUri");
            if (relatingPredicateUri != null) {
            	ObjectProperty relatingPredicateProp = opDao.getObjectPropertyByURI(relatingPredicateUri);
            	if (relatingPredicateProp != null) {
            		vreq.setAttribute("relatingPredicate", relatingPredicateProp);
            	}
            }

            entity.setKeywords(iwDao.getKeywordsForIndividualByMode(entity.getURI(),"visible"));
            entity.sortForDisplay();

            String vclassName = "unknown";
            String customView = null;
            String customCss = null;
            if( entity.getVClass() != null ){
                vclassName = entity.getVClass().getName();
                List<VClass> clasList = entity.getVClasses(true);
                for (VClass clas : clasList) {
                    customView = clas.getCustomDisplayView();
                    if (customView != null) {
                        if (customView.length()>0) {
                            vclassName = clas.getName(); // reset entity vclassname to name of class where a custom view
                            log.debug("Found direct class ["+clas.getName()+"] with custom view "+customView+"; resetting entity vclassName to this class");
                            break;
                        } else {
                            customView = null;
                        }
                    }
                }
                if (customView == null) { //still
                    clasList = entity.getVClasses(false);
                    for (VClass clas : clasList) {
                        customView = clas.getCustomDisplayView();
                        if (customView != null) {
                            if (customView.length()>0) {
                                // note that NOT changing entity vclassName here yet
                                log.debug("Found inferred class ["+clas.getName()+"] with custom view "+customView);
                                break;
                            } else {
                                customView = null;
                            }
                        }
                    }
                }
            } else {
                log.error("Entity " + entity.getURI() + " with vclass URI " +
                        entity.getVClassURI() + ", no vclass with that URI exists");
            }
            if (customView!=null) {
                // insert test for whether a css files of the same name exists, and populate the customCss string for use when construction the header
            }
            String netid = iwDao.getNetId(entity.getURI());
            vreq.setAttribute("netid", netid);
            vreq.setAttribute("vclassName", vclassName);
            vreq.setAttribute("entity",entity);
            Portal portal = vreq.getPortal();
            vreq.setAttribute("portal",String.valueOf(portal));
            String view= getViewFromRequest(req);
            if( view == null){
                if (customView == null) {
                    view = default_jsp;
                    vreq.setAttribute("bodyJsp","/"+Controllers.ENTITY_JSP);
                    log.debug("no custom view and no view parameter in request for rendering "+entity.getName());
                } else {
                    view = default_jsp;
                    log.debug("setting custom view templates/entity/"+ customView + " for rendering "+entity.getName());
                    vreq.setAttribute("bodyJsp", "/templates/entity/"+customView);
                }
                vreq.setAttribute("entityPropsListJsp",Controllers.ENTITY_PROP_LIST_JSP);
                vreq.setAttribute("entityDatapropsListJsp",Controllers.ENTITY_DATAPROP_LIST_JSP);
                vreq.setAttribute("entityMergedPropsListJsp",Controllers.ENTITY_MERGED_PROP_LIST_GROUPED_JSP);
                vreq.setAttribute("entityKeywordsListJsp",Controllers.ENTITY_KEYWORDS_LIST_JSP);
            } else if (view.equals("rdf.rdf")) { 
            	writeRDF(entity, req, res);
				// BJL23 temporarily disabling this until we add filtering of hidden properties (get RDF through Vitro API with filtering DAOs)
            } else {
                log.debug("Found view parameter "+view+" in request for rendering "+entity.getName());
            }
            
            //set title before we do the highlighting so we don't get markup in it.
            vreq.setAttribute("title",entity.getName());
            //setup highlighter for search terms
            checkForSearch(req, entity);

			// set CSS and script elements
            String contextPath = "";
            if (req.getContextPath().length()>1) {
            	contextPath = req.getContextPath();
            }
            String css = "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
			+ contextPath + "/" + portal.getThemeDir() + "css/entity.css\"/>\n"
			+ "<script language='JavaScript' type='text/javascript' src='"+contextPath+"/js/toggle.js'></script> \n";
            if (customCss!=null) {
                css += customCss;
            }

			// generate link to RDF representation for semantic web clients like Piggy Bank
			// BJL 2008-07-16: I'm temporarily commenting this out because I forgot we need to make sure it filters out the hidden properties
            // generate url for this entity
            // String individualToRDF = "http://"+vreq.getServerName()+":"+vreq.getServerPort()+vreq.getContextPath()+"/entity?home=1&uri="+forURL(entity.getURI())+"&view=rdf.rdf"; 
            //css += "<link rel='alternate' type='application/rdf+xml' title='"+entity.getName()+"' href='"+individualToRDF+"' />";

            vreq.setAttribute("css",css);
            vreq.setAttribute("scripts", "/templates/entity/entity_inject_head.jsp");

            RequestDispatcher rd = vreq.getRequestDispatcher( view );
            rd.forward(req,res);
        } catch (Throwable e) {
            log.error(e);
            req.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
            rd.forward(req, res);
        }
    }
    
    private boolean checkForSunset(VitroRequest vreq, Individual entity) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean checkForHidden(VitroRequest vreq, Individual entity){ 
        // TODO Auto-generated method stub
        return false;
    }

    private void writeRDF(Individual entity, HttpServletRequest req, HttpServletResponse res) {
    	OntModel ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
    	
    	Resource i = ontModel.getResource(entity.getURI());
    	ModelMaker modelMaker = ontModel.getImportModelMaker();
    	Model newModel = modelMaker.createModel(entity.getURI(), false);
    	newModel = getRDF(entity, ontModel, newModel, true);
    	try {
    		ServletOutputStream outstream = res.getOutputStream();
    		/*
			newModel.remove(newModel.listStatements());
        	newModel.add(aboutEntity);
        	aboutEntity.close();
        	StmtIterator aboutEntity2 = ontModel.listStatements(i , null, (RDFNode)null);
        	while(aboutEntity2.hasNext()) {
        		Statement st = aboutEntity2.nextStatement();
        		Resource o = null;
        		Property p = st.getPredicate();
        		RDFNode obj = st.getObject();
        		if(!(obj instanceof Literal)) o = (Resource)obj;
        		if(!p.getURI().equals(RDF.type.getURI()) && o!=null) {
        			StmtIterator aboutObject = ontModel.listStatements(o, null, (RDFNode)null);
        			newModel.add(aboutObject);
        		}
        	}
        	*/
        	res.setContentType("application/rdf+xml");
        	newModel.write(outstream);
    	}
    	catch (Throwable e) {
    		log.error(e);
    		System.out.println(e);
    	}
    	
    }
    
    // Adds data from ontModel about entity to newModel. Go down 1 level if recurse is true
    private Model getRDF(Individual entity, Model ontModel, Model newModel, boolean recurse) {
    	Resource subj = ontModel.getResource(entity.getURI());
    	List<DataPropertyStatement> dstates = entity.getDataPropertyStatements();
    	//System.out.println("data: "+dstates.size());
    	for (DataPropertyStatement ds: dstates) {
    		Property dp = ontModel.getProperty(ds.getDatapropURI());
    		//if(!(dp instanceof DatatypeProperty)) System.out.println("not datatype prop "+dp.getURI());

    		Literal lit = newModel.createLiteral(ds.getData());
    		newModel.add(newModel.createStatement(subj, dp, lit));
    	}
    	if(recurse) {
	    	List<ObjectPropertyStatement> ostates = entity.getObjectPropertyStatements();
	    	//System.out.println("obj: "+ostates.size());
	    	for (ObjectPropertyStatement os: ostates) {
	    		Property op = ontModel.getProperty(os.getPropertyURI());
	    		Resource obj = ontModel.getResource(os.getObjectURI());
	    		newModel.add(newModel.createStatement(subj, op, obj));
	    		newModel.add(getRDF(os.getObject(), ontModel, newModel, false));
	    	}
    	}
    	return newModel;
    }

    private void checkForSearch(HttpServletRequest req, Individual ent) {
        
        
        if (req.getSession().getAttribute("LastQuery") != null) {
            VitroQueryWrapper qWrap = (VitroQueryWrapper) req.getSession()
                    .getAttribute("LastQuery");
            if (qWrap.getRequestCount() > 0 && qWrap.getQuery() != null) {
                VitroQuery query = qWrap.getQuery();

                //set query text so we can get it in JSP
                req.setAttribute("querytext", query.getTerms());

                //setup highlighting for output
                StringProcessorTag.putStringProcessorInRequest(req, qWrap.getHighlighter());                                
                        
                qWrap.setRequestCount(qWrap.getRequestCount() - 1);
            } else {
                req.getSession().removeAttribute("LastQuery");
            }
        }
    }

    private Pattern badrequest= Pattern.compile(".*([&\\?=]|\\.\\.).*");

    public String getViewFromRequest(HttpServletRequest request){
        String viewParam = request.getParameter("view");
        if( viewParam != null ){
            if( badrequest.matcher(viewParam).matches()  ){
                log.debug("request for a non-default view was bad: " + viewParam);
                return null;
            }else{
                log.debug("view request : " + viewParam);
                return viewParam;
            }
        }
        return null;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }

    private void doHelp(HttpServletResponse res)
    throws IOException, ServletException {
        ServletOutputStream out = res.getOutputStream();
        res.setContentType("text/html; charset=UTF-8");
        out.println("<html><body><h2>Quick Notes on using entity:</h2>");
        out.println("<p>id is the id of the entity to query for. netid also works.</p>");
        out.println("</body></html>");
    }

    private void doNotFound(HttpServletRequest req, HttpServletResponse res)
    throws IOException, ServletException {
        VitroRequest vreq = new VitroRequest(req);
        Portal portal = vreq.getPortal();
        ApplicationBean appBean = ApplicationBean.getAppBean(getServletContext());
        int allPortalId = appBean.getAllPortalFlagNumeric();
        
        //If an Individual is not found, there is possibility that it
        //was requested from a portal where it was not visible.
        //In this case redirect to the all portal.    
        try{      
            Portal allPortal = 
                vreq.getWebappDaoFactory().getPortalDao().getPortal(allPortalId);
            // there must be a portal defined with the ID of the all portal
            // for this to work
            if( portal.getPortalId() !=  allPortalId && allPortal != null ) {            
                                
                //bdc34: 
                // this is hard coded to get the all portal 
                // I didn't find a way to get the id of the all portal
                // it is likely that redirecting will not work in non VIVO clones
                String portalPrefix = null;
                String portalParam  = null;
                if( allPortal != null && allPortal.getUrlprefix() != null )              
                    portalPrefix = allPortal.getUrlprefix();
                else
                    portalParam = "home=" + allPortalId; 
                                        
                String queryStr = req.getQueryString();
                if( queryStr == null && portalParam != null && !"".equals(portalParam)){
                    queryStr = portalParam;
                } else {                
                    if( portalParam != null && !"".equals(portalParam))
                        queryStr = queryStr + "&" + portalParam;
                }   
                if( queryStr != null && !queryStr.startsWith("?") )
                    queryStr = "?" + queryStr;
                           
                StringBuilder url = new StringBuilder();
                url.append( req.getContextPath() );                                
                if( req.getContextPath() != null && !req.getContextPath().endsWith("/"))
                    url.append('/');
                
                if( portalPrefix != null && !"".equals(portalPrefix)) 
                    url.append( portalPrefix ).append('/');            
                    
                String servletPath = req.getServletPath();
                String spath = "";
                if( servletPath != null ){ 
                    if( servletPath.startsWith("/") )
                        spath = servletPath.substring(1);
                    else
                        spath = servletPath;
                }
                                
                if( spath != null && !"".equals(spath))
                    url.append( spath );
                
                if( req.getPathInfo() != null )
                    url.append( req.getPathInfo() );
                
                if( queryStr != null && !"".equals(queryStr ))
                    url.append( queryStr );
                
                res.sendRedirect(url.toString());
                return;
            }
        }catch(Throwable th){
            log.error("could not do a redirect", th);
        }

        //set title before we do the highlighting so we don't get markup in it.
        req.setAttribute("title","not found");
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);

        String css = "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
            + portal.getThemeDir() + "css/entity.css\"/>"
            + "<script language='JavaScript' type='text/javascript' src='js/toggle.js'></script>";
        req.setAttribute("css",css);

        req.setAttribute("bodyJsp","/"+Controllers.ENTITY_NOT_FOUND_JSP);

        RequestDispatcher rd = req.getRequestDispatcher(Controllers.BASIC_JSP);
        rd.forward(req,res);
    }
    
    private String forURL(String frag)
    {
            String result = null;
            try 
            {
                    result = URLEncoder.encode(frag, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
            return result;
    }
    
    private class HelpException extends Throwable{}
    private class EntityNotFoundException extends Throwable{}
}
