/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryWrapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperFactory;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;
import edu.cornell.mannlib.vitro.webapp.web.jsptags.StringProcessorTag;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.IndividualTemplateModel;

/**
 * Handles requests for entity information.
 * Calls EntityPropertyListController to draw property list.
 *
 * @author bdc34
 *
 */
public class IndividualController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(IndividualController.class);
    
    private static final String TEMPLATE_INDIVIDUAL_DEFAULT = "individual.ftl";
    private static final String TEMPLATE_HELP = "individual-help.ftl";
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
    	try {
    		cleanUpSession(vreq);

	        // get URL without hostname or servlet context
	        String url = vreq.getRequestURI().substring(vreq.getContextPath().length()); 
	
	        // Check to see if the request is for a non-information resource, redirect if it is.
	        String redirectURL = checkForRedirect ( url, vreq.getHeader("accept") );
	        if( redirectURL != null ){
	            return new RedirectResponseValues(redirectURL);
	        }            	                                         
	
	        Individual individual = null;
	        try {
	            individual = getEntityFromRequest(vreq);
	        } catch (Throwable th) {
	            return doHelp();
	        }
	        
	        if( individual == null || checkForHidden(vreq, individual) || checkForSunset(vreq, individual)){
	        	return doNotFound(vreq);
	        }

            ContentType rdfFormat = checkForLinkedDataRequest(url,vreq.getHeader("accept"));
            if( rdfFormat != null ){
                return doRdf(vreq, individual, rdfFormat);
            }   
	            
	        // If this is an uploaded file, redirect to its "alias URL".
	        String aliasUrl = getAliasUrlForBytestreamIndividual(vreq, individual);
	        if (aliasUrl != null) {
	        	return new RedirectResponseValues(UrlBuilder.getUrl(vreq.getContextPath() + aliasUrl));
	        }

	        Map<String, Object> body = new HashMap<String, Object>();
	        
    		body.put("editStatus", getEditingData(vreq));

            body.put("title", individual.getName());
            
    		body.put("relatedSubject", getRelatedSubject(vreq));
    		
    		IndividualTemplateModel ind = getIndividualTemplateModel(vreq, individual); 
	        body.put("individual", ind); 
	        
	        String template = getIndividualTemplate(individual);
	                
	        return new TemplateResponseValues(template, body);
        
	    } catch (Throwable e) {
	        log.error(e);
	        return new ExceptionResponseValues(e);
	    }
    }

    private void cleanUpSession(VitroRequest vreq) {
		// Session cleanup: any time we are at an entity page we shouldn't have an editing config or submission
        HttpSession session = vreq.getSession();
	    session.removeAttribute("editjson");
	    EditConfiguration.clearAllConfigsInSession(session);
	    EditSubmission.clearAllEditSubmissionsInSession(session);
    }
    
    // Set template values related to access privileges
    // RY We may want to define an EditingIndividualTemplateModel class, with methods like getAdminPanel() and
    // getEditLinks(property). The constructor would take an individual and a loginFormBean object, both of which
    // are needed to generate property edit links. Another idea is to subclass IndividualTemplateModel with 
    // EditableIndividualTemplateModel, and define editing-related methods there. However, that means in the 
    // template we will have expressions like individual.adminPanel or individual.editingLinks(property), 
    // which might seem opaque to template authors.
    private Map<String, Object> getEditingData(VitroRequest vreq) {
  
        LoginStatusBean loginBean = LoginStatusBean.getBean(vreq);
        
    	Map<String, Object> editingData = new HashMap<String, Object>();

		editingData.put("showEditLinks", VitroRequestPrep.isSelfEditing(vreq) || loginBean.isLoggedInAtLeast(LoginStatusBean.NON_EDITOR));	
		
		boolean showAdminPanel = loginBean.isLoggedInAtLeast(LoginStatusBean.EDITOR);
		editingData.put("showAdminPanel", showAdminPanel);

		return editingData;		
    }
    
    private Map<String, Object> getRelatedSubject(VitroRequest vreq) {
        Map<String, Object> map = null;
        
        IndividualDao iwDao = vreq.getWebappDaoFactory().getIndividualDao();
        ObjectPropertyDao opDao = vreq.getWebappDaoFactory().getObjectPropertyDao();
        
        // Check if a "relatedSubjectUri" parameter has been supplied, and,
        // if so, retrieve the related individual.
        // Some individuals make little sense standing alone and should
        // be displayed in the context of their relationship to another.
        String relatedSubjectUri = vreq.getParameter("relatedSubjectUri"); 
        if (relatedSubjectUri != null) {
            Individual relatedSubjectInd = iwDao.getIndividualByURI(relatedSubjectUri);
            if (relatedSubjectInd != null) {
                map = new HashMap<String, Object>();
                map.put("name", relatedSubjectInd.getName());
                map.put("url", (new IndividualTemplateModel(relatedSubjectInd, vreq)).getProfileUrl());
                String relatingPredicateUri = vreq.getParameter("relatingPredicateUri");
                if (relatingPredicateUri != null) {
                    ObjectProperty relatingPredicateProp = opDao.getObjectPropertyByURI(relatingPredicateUri);
                    if (relatingPredicateProp != null) {
                        map.put("relatingPredicateDomainPublic", relatingPredicateProp.getDomainPublic());
                    }
                }
            }
        }
        return map;
    }
    
	private IndividualTemplateModel getIndividualTemplateModel(VitroRequest vreq, Individual individual) 
	    throws ServletException, IOException {
		
    	IndividualDao iwDao = vreq.getWebappDaoFactory().getIndividualDao();
        
        individual.setKeywords(iwDao.getKeywordsForIndividualByMode(individual.getURI(),"visible"));
        individual.sortForDisplay();

        //setup highlighter for search terms
        //checkForSearch(vreq, individual);
        
        return new IndividualTemplateModel(individual, vreq);
	}
	
	// Determine whether the individual has a custom display template based on its class membership.
	// If not, return the default individual template.
	private String getIndividualTemplate(Individual individual) {
	    
        String vclassName = "unknown";
        String customTemplate = null;

        if( individual.getVClass() != null ){
            vclassName = individual.getVClass().getName();
            List<VClass> clasList = individual.getVClasses(true);
            for (VClass clas : clasList) {
                customTemplate = clas.getCustomDisplayView();
                if (customTemplate != null) {
                    if (customTemplate.length()>0) {
                        vclassName = clas.getName(); // reset entity vclassname to name of class where a custom view
                        log.debug("Found direct class ["+clas.getName()+"] with custom view "+customTemplate+"; resetting entity vclassName to this class");
                        break;
                    } else {
                        customTemplate = null;
                    }
                }
            }
            if (customTemplate == null) { //still
                clasList = individual.getVClasses(false);
                for (VClass clas : clasList) {
                    customTemplate = clas.getCustomDisplayView();
                    if (customTemplate != null) {
                        if (customTemplate.length()>0) {
                            // note that NOT changing entity vclassName here yet
                            log.debug("Found inferred class ["+clas.getName()+"] with custom view "+customTemplate);
                            break;
                        } else {
                            customTemplate = null;
                        }
                    }
                }
            }
        } else if (individual.getVClassURI() != null) {
            log.debug("Individual " + individual.getURI() + " with class URI " +
                    individual.getVClassURI() + ": no class found with that URI");
        }
        
        return customTemplate != null ? customTemplate : TEMPLATE_INDIVIDUAL_DEFAULT;
        
	}

	private ResponseValues doRdf(VitroRequest vreq, Individual individual,
			ContentType rdfFormat) throws IOException, ServletException {    	
				
		OntModel ontModel = null;
		HttpSession session = vreq.getSession(false);
		if( session != null )
			ontModel = (OntModel)session.getAttribute("jenaOntModel");		
		if( ontModel == null)
			ontModel = (OntModel)getServletContext().getAttribute("jenaOntModel");
			
		Model newModel = getRDF(individual, ontModel, ModelFactory.createDefaultModel(), 0);		
		
		return new RdfResponseValues(rdfFormat, newModel);
	}

	// RY **** Remove "fm" from the patterns when switching web.xml to this controller.
	private static Pattern LINKED_DATA_URL = Pattern.compile("^/individualfm/([^/]*)$");		
	private static Pattern NS_PREFIX_URL = Pattern.compile("^/individualfm/([^/]*)/([^/]*)$");
	
    /**
        Gets the entity id from the request.
        Works for the following styles of URLs:        
        
        /individual?id=individualLocalName
        /individual?entityId=individualLocalName
        /individual?uri=urlencodedURI
        /individual?nedit=bdc34
        /individual?nedIt=bdc34
        /individual/nsprefix/localname
        /individual/localname         
        /individual/localname/localname.rdf
        /individual/localname/localname.n3
        /individual/localname/localname.ttl
        /individual/localname/localname.html
          
        @return null on failure.
    */
    public static Individual getEntityFromRequest(VitroRequest vreq) {
        String netIdStr = null;
        Individual entity = null;
        IndividualDao iwDao = vreq.getWebappDaoFactory().getIndividualDao();

        String entityIdStr = vreq.getParameter("id");
        if (entityIdStr == null || entityIdStr.equals(""))
            entityIdStr = vreq.getParameter("entityId");

        if( entityIdStr != null){
            try {
                String entityURI = vreq.getWebappDaoFactory().getDefaultNamespace()+"individual"+entityIdStr;
                entity = iwDao.getIndividualByURI(entityURI);
            } catch ( Exception e ) {
                log.warn("Could not parse entity id: " + entityIdStr);
                return null; 
            }
            return entity;
        }

        String entityURIStr = vreq.getParameter("uri");
        if (entityURIStr != null) {
            try {
                entity = iwDao.getIndividualByURI(entityURIStr);
            } catch (Exception e) {             
                log.warn("Could not retrieve entity "+entityURIStr);
                return null;
            }
            return entity;
        }
        
        //get URL without hostname or servlet context
        String url = vreq.getRequestURI().substring(vreq.getContextPath().length());
        
		/* check for parts of URL that indicate request for RDF
		   http://vivo.cornell.edu/individual/n23/n23.rdf
		   http://vivo.cornell.edu/individual/n23/n23.n3
		   http://vivo.cornell.edu/individual/n23/n23.ttl */					
		String uri = null;
		Matcher m = RDF_REQUEST.matcher(url);
		if( m.matches() && m.groupCount() == 1)
			uri = m.group(1);
		m = N3_REQUEST.matcher(url);
		if( m.matches() && m.groupCount() == 1)
			uri = m.group(1);
		m = TTL_REQUEST.matcher(url);
		if( m.matches() && m.groupCount() == 1)
			uri= m.group(1);
		m = HTML_REQUEST.matcher(url);
		if( m.matches() && m.groupCount() == 1)
			uri= m.group(1);
		if( uri != null )
			return iwDao.getIndividualByURI(vreq.getWebappDaoFactory().getDefaultNamespace() + uri);
		
        // see if we can get the URI from a name space prefix and a local name
        Matcher prefix_match = NS_PREFIX_URL.matcher(url);
		if( prefix_match.matches() && prefix_match.groupCount() == 2){		
			String prefix = prefix_match.group(1);
			String localName = prefix_match.group(2);
			
			//String[] requestParts = requestURI.split("/individual/");
			//String[] URIParts = requestParts[1].split("/");
			//String localName = URIParts[1];
			
			String namespace = "";
			NamespaceMapper namespaceMapper = NamespaceMapperFactory.getNamespaceMapper(vreq.getSession().getServletContext());
			String t;
			namespace = ( (t = namespaceMapper.getNamespaceForPrefix(prefix)) != null) ? t : "";
						
			return iwDao.getIndividualByURI(namespace+localName);
		}

        // see if we can get a local name
		Matcher linkedDataMatch = LINKED_DATA_URL.matcher(url);
		if( linkedDataMatch.matches() && linkedDataMatch.groupCount() == 1){
			String localName = linkedDataMatch.group(1);
			String ns = vreq.getWebappDaoFactory().getDefaultNamespace();
			return iwDao.getIndividualByURI( ns + localName );
		}
		
		//so we try to get the netid
        netIdStr = vreq.getParameter("netId");      
        if (netIdStr==null || netIdStr.equals(""))
            netIdStr = vreq.getParameter("netid");
        if ( netIdStr != null ){
            uri = iwDao.getIndividualURIFromNetId(netIdStr);
            return iwDao.getIndividualByURI(uri);
        }

		return null;		
    }
 
	
	private static Pattern URI_PATTERN = Pattern.compile("^/individualfm/([^/]*)$");
    //Redirect if the request is for http://hostname/individual/localname
    // if accept is nothing or text/html redirect to ???
    // if accept is some RDF thing redirect to the URL for RDF
	private String checkForRedirect(String url, String acceptHeader) {
		Matcher m = URI_PATTERN.matcher(url);
		if( m.matches() && m.groupCount() == 1 ){			
			ContentType c = checkForLinkedDataRequest(url, acceptHeader);			
			if( c != null ){
				String redirectUrl = "/individual/" + m.group(1) + "/" + m.group(1) ; 
				if( RDFXML_MIMETYPE.equals( c.getMediaType())  ){
					return redirectUrl + ".rdf";
				}else if( N3_MIMETYPE.equals( c.getMediaType() )){
					return redirectUrl + ".n3";
				}else if( TTL_MIMETYPE.equals( c.getMediaType() )){
					return redirectUrl + ".ttl";
				}//else send them to html													
			}
			//else redirect to HTML representation
			return "/display/" + m.group(1) ;
		}else{			
			return null;
		}
	}

	private static Pattern RDF_REQUEST = Pattern.compile("^/individualfm/([^/]*)/\\1.rdf$");
    private static Pattern N3_REQUEST = Pattern.compile("^/individualfm/([^/]*)/\\1.n3$");
    private static Pattern TTL_REQUEST = Pattern.compile("^/individualfm/([^/]*)/\\1.ttl$");
    private static Pattern HTML_REQUEST = Pattern.compile("^/displayfm/([^/]*)$");
    
    /**  
     * @return null if this is not a linked data request, returns content type if it is a 
     * linked data request.
     */
	protected ContentType checkForLinkedDataRequest(String url, String acceptHeader) {		
		try {
			//check the accept header			
			if (acceptHeader != null) {
				List<ContentType> actualContentTypes = new ArrayList<ContentType>();				
				actualContentTypes.add(new ContentType( XHTML_MIMETYPE ));
				actualContentTypes.add(new ContentType( HTML_MIMETYPE ));				
				
				actualContentTypes.add(new ContentType( RDFXML_MIMETYPE ));
				actualContentTypes.add(new ContentType( N3_MIMETYPE ));
				actualContentTypes.add(new ContentType( TTL_MIMETYPE ));
				
								
				ContentType best = ContentType.getBestContentType(acceptHeader,actualContentTypes);
				if (best!=null && (
						RDFXML_MIMETYPE.equals(best.getMediaType()) || 
						N3_MIMETYPE.equals(best.getMediaType()) ||
						TTL_MIMETYPE.equals(best.getMediaType()) ))
					return best;				
			}
			
			/*
			 * check for parts of URL that indicate request for RDF
			   http://vivo.cornell.edu/individual/n23/n23.rdf
			   http://vivo.cornell.edu/individual/n23/n23.n3
			   http://vivo.cornell.edu/individual/n23/n23.ttl
			 */
						
			Matcher m = RDF_REQUEST.matcher(url);
			if( m.matches() )
				return new ContentType(RDFXML_MIMETYPE);
			m = N3_REQUEST.matcher(url);
			if( m.matches() )
				return new ContentType(N3_MIMETYPE);
			m = TTL_REQUEST.matcher(url);
			if( m.matches() )
				return new ContentType(TTL_MIMETYPE);
			
		} catch (Throwable th) {
			log.error("problem while checking accept header " , th);
		}
		return null;
	}  

	@SuppressWarnings("unused")
	private boolean checkForSunset(VitroRequest vreq, Individual entity) {
        // TODO Auto-generated method stub
        return false;
    }

    @SuppressWarnings("unused")
	private boolean checkForHidden(VitroRequest vreq, Individual entity){ 
        // TODO Auto-generated method stub
        return false;
    }
    
	/**
	 * If this entity represents a File Bytestream, get its alias URL so we can
	 * properly serve the file contents.
	 */
	private String getAliasUrlForBytestreamIndividual(VitroRequest vreq, Individual entity)
			throws IOException {
		FileInfo fileInfo = FileInfo.instanceFromBytestreamUri(vreq.getWebappDaoFactory(), entity.getURI());
		if (fileInfo == null) {
			log.trace("Entity '" + entity.getURI() + "' is not a bytestream.");
			return null;
		}

		String url = fileInfo.getBytestreamAliasUrl();
		log.debug("Alias URL for '" + entity.getURI() + "' is '" + url + "'");
		return url;
	}
 
    private Model getRDF(Individual entity, OntModel contextModel, Model newModel, int recurseDepth ) {
    	Resource subj = newModel.getResource(entity.getURI());
    	
    	List<DataPropertyStatement> dstates = entity.getDataPropertyStatements();
    	//System.out.println("data: "+dstates.size());
    	TypeMapper typeMapper = TypeMapper.getInstance();
    	for (DataPropertyStatement ds: dstates) {
    		Property dp = newModel.getProperty(ds.getDatapropURI());
	    	Literal lit = null;
	        if ((ds.getLanguage()) != null && (ds.getLanguage().length()>0)) {
	        	lit = newModel.createLiteral(ds.getData(),ds.getLanguage());
	        } else if ((ds.getDatatypeURI() != null) && (ds.getDatatypeURI().length()>0)) {
	        	lit = newModel.createTypedLiteral(ds.getData(),typeMapper.getSafeTypeByName(ds.getDatatypeURI()));
	        } else {
	        	lit = newModel.createLiteral(ds.getData());
	        } 
    		newModel.add(newModel.createStatement(subj, dp, lit));
    	}
    	
    	if( recurseDepth < 5 ){
	    	List<ObjectPropertyStatement> ostates = entity.getObjectPropertyStatements();
	    	for (ObjectPropertyStatement os: ostates) {
	    		ObjectProperty objProp = os.getProperty();
	    		Property op = newModel.getProperty(os.getPropertyURI());
	    		Resource obj = newModel.getResource(os.getObjectURI());
	    		newModel.add(newModel.createStatement(subj, op, obj));
	    		if( objProp.getStubObjectRelation() )
	    			newModel.add(getRDF(os.getObject(), contextModel, newModel, recurseDepth + 1));
	    	}
    	}
    	
    	newModel = getLabelAndTypes(entity, contextModel, newModel );
    	return newModel;
    }

    /* Get the properties that are difficult to get via a filtered WebappDaoFactory. */
    private Model getLabelAndTypes(Individual entity, Model ontModel, Model newModel){
    	for( VClass vclass : entity.getVClasses()){
    		newModel.add(newModel.getResource(entity.getURI()), RDF.type, newModel.getResource(vclass.getURI()));
    	}
    	
    	ontModel.enterCriticalSection(Lock.READ);
		try {
			newModel.add(ontModel.listStatements(ontModel.getResource(entity.getURI()), RDFS.label, (RDFNode)null));
		} finally {
			ontModel.leaveCriticalSection();
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

    private ResponseValues doHelp() throws IOException, ServletException {
        return new TemplateResponseValues(TEMPLATE_HELP);
    }
    
    private ResponseValues doNotFound(VitroRequest vreq) throws IOException, ServletException {
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
                                        
                String queryStr = vreq.getQueryString();
                if( queryStr == null && portalParam != null && !"".equals(portalParam)){
                    queryStr = portalParam;
                } else {                
                    if( portalParam != null && !"".equals(portalParam))
                        queryStr = queryStr + "&" + portalParam;
                }   
                if( queryStr != null && !queryStr.startsWith("?") )
                    queryStr = "?" + queryStr;
                           
                StringBuilder url = new StringBuilder();
                url.append( vreq.getContextPath() );                                
                if( vreq.getContextPath() != null && !vreq.getContextPath().endsWith("/"))
                    url.append('/');
                
                if( portalPrefix != null && !"".equals(portalPrefix)) 
                    url.append( portalPrefix ).append('/');            
                    
                String servletPath = vreq.getServletPath();
                String spath = "";
                if( servletPath != null ){ 
                    if( servletPath.startsWith("/") )
                        spath = servletPath.substring(1);
                    else
                        spath = servletPath;
                }
                                
                if( spath != null && !"".equals(spath))
                    url.append( spath );
                
                if( vreq.getPathInfo() != null )
                    url.append( vreq.getPathInfo() );
                
                if( queryStr != null && !"".equals(queryStr ))
                    url.append( queryStr );
                
                return new RedirectResponseValues(url.toString());
            }
        }catch(Throwable th){
            log.error("could not do a redirect", th);
        }

        //set title before we do the highlighting so we don't get markup in it.
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title","Individual Not Found");
        body.put("errorMessage", "The individual was not found in the system.");
        
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_NOT_FOUND);
    }

}
