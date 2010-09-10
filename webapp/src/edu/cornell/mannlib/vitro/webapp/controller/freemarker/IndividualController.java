/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
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
import freemarker.template.Configuration;

/**
 * Handles requests for entity information.
 * Calls EntityPropertyListController to draw property list.
 *
 * @author bdc34
 *
 */
public class IndividualController extends FreemarkerHttpServlet {
    private static final Log log = LogFactory.getLog(IndividualController.class);

    private String default_jsp      = Controllers.BASIC_JSP;
    private String default_body_jsp = Controllers.ENTITY_JSP;
    private ApplicationBean appBean;

    
    protected String getBody(VitroRequest vreq, Map<String, Object> body, Configuration config) {
    	try {

	        HttpSession session = vreq.getSession();
	        
    		cleanUpSession(session);
    		

    		
	        // get URL without hostname or servlet context
	        String url = vreq.getRequestURI().substring(vreq.getContextPath().length());   
	        
	        // Title = individual label
	
	        //Check to see if the request is for a non-information resource, redirect if it is.
	        String redirectURL = checkForRedirect ( url, vreq.getHeader("accept") );
	        if( redirectURL != null ){
	        	//doRedirect( vreq, res, redirectURL );
	        	return "";
	        }            	           
	
	        ContentType rdfFormat = checkForLinkedDataRequest(url,vreq.getHeader("accept"));
	        if( rdfFormat != null ){
	        	//doRdf( vreq, res, rdfFormat );
	        	return "";
	        }                                 
	
	        Individual individual = null;
	        try{
	            individual = getEntityFromRequest( vreq);
	        }catch(Throwable th){
	            //doHelp(res);
	            return "";
	        }
	        
	        if( individual == null || checkForHidden(vreq, individual) || checkForSunset(vreq, individual)){
	        	//doNotFound(vreq, res);
	        	return "";
	        }
	
	        // If this is an uploaded file, redirect to its "alias URL".
	        String aliasUrl = getAliasUrlForBytestreamIndividual(vreq, individual);
	        if (aliasUrl != null) {
	        	//res.sendRedirect(vreq.getContextPath() + aliasUrl);
	        	return "";
	        }


	        int securityLevel = getSecurityLevel(session);
	        UrlBuilder urlBuilder = new UrlBuilder(vreq.getPortal());
    		body.put("editStatus", getEditingData(vreq, securityLevel, individual, urlBuilder));
	        body.putAll(getIndividualData(vreq, individual));                    
	        body.put("title", individual.getName());
	        
	        String bodyTemplate = "individual.ftl";             
	        return mergeBodyToTemplate(bodyTemplate, body, config);
        
	    } catch (Throwable e) {
	        log.error(e);
	        //vreq.setAttribute("javax.servlet.jsp.jspException",e);
	        // RequestDispatcher rd = vreq.getRequestDispatcher("/error.jsp");
	        //rd.forward(vreq, res);
	        return "";
	    }
    }

    private void cleanUpSession(HttpSession session) {
		// Session cleanup: anytime we are at an entity page we shouldn't have an editing config or submission
	    session.removeAttribute("editjson");
	    EditConfiguration.clearAllConfigsInSession(session);
	    EditSubmission.clearAllEditSubmissionsInSession(session);
    }
    
    private int getSecurityLevel(HttpSession session) {
    	String loginStatus = null;
    	int securityLevel = LoginFormBean.ANYBODY;
    	LoginFormBean loginHandler = (LoginFormBean)session.getAttribute("loginHandler");
    	if (loginHandler != null) {
			loginStatus = loginHandler.getLoginStatus();
			if  ("authenticated".equals(loginStatus)) {
				securityLevel = Integer.parseInt(loginHandler.getLoginRole());  
			}
    	}
    	return securityLevel;
    	
    }

    private Map<String, Object> getEditingData(VitroRequest vreq, int securityLevel, Individual individual, UrlBuilder urlBuilder) {
		// Set values related to access privileges
    	Map<String, Object> editingData = new HashMap<String, Object>();

		editingData.put("showEditLinks", VitroRequestPrep.isSelfEditing(vreq) || securityLevel >= LoginFormBean.NON_EDITOR);	
		
		boolean showAdminPanel = securityLevel >= LoginFormBean.EDITOR;
		editingData.put("showAdminPanel", showAdminPanel);
		if (showAdminPanel) {
			
			editingData.put("editingUrl", urlBuilder.getPortalUrl("/entityEdit", "uri", individual.getURI()));
		}

		return editingData;
		
    }
    
	private Map<String, Object> getIndividualData(VitroRequest vreq, Individual individual) throws ServletException, IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		
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
        		Map<String, Object> relatedSubject = new HashMap<String, Object>();
        		relatedSubject.put("name", relatedSubjectInd.getName());
        		relatedSubject.put("url", (new IndividualTemplateModel(relatedSubjectInd)).getProfileUrl());
                String relatingPredicateUri = vreq.getParameter("relatingPredicateUri");
                if (relatingPredicateUri != null) {
                	ObjectProperty relatingPredicateProp = opDao.getObjectPropertyByURI(relatingPredicateUri);
                	if (relatingPredicateProp != null) {
                		relatedSubject.put("relatingPredicateDomainPublic", relatingPredicateProp.getDomainPublic());
                	}
                }
                map.put("relatedSubject", relatedSubject);
        	}
        }

        individual.setKeywords(iwDao.getKeywordsForIndividualByMode(individual.getURI(),"visible"));
        individual.sortForDisplay();

//        String vclassName = "unknown";
//        String customView = null;
//
//        if( indiv.getVClass() != null ){
//            vclassName = indiv.getVClass().getName();
//            List<VClass> clasList = indiv.getVClasses(true);
//            for (VClass clas : clasList) {
//                customView = clas.getCustomDisplayView();
//                if (customView != null) {
//                    if (customView.length()>0) {
//                        vclassName = clas.getName(); // reset entity vclassname to name of class where a custom view
//                        log.debug("Found direct class ["+clas.getName()+"] with custom view "+customView+"; resetting entity vclassName to this class");
//                        break;
//                    } else {
//                        customView = null;
//                    }
//                }
//            }
//            if (customView == null) { //still
//                clasList = indiv.getVClasses(false);
//                for (VClass clas : clasList) {
//                    customView = clas.getCustomDisplayView();
//                    if (customView != null) {
//                        if (customView.length()>0) {
//                            // note that NOT changing entity vclassName here yet
//                            log.debug("Found inferred class ["+clas.getName()+"] with custom view "+customView);
//                            break;
//                        } else {
//                            customView = null;
//                        }
//                    }
//                }
//            }
//        } else {
//            log.error("Entity " + indiv.getURI() + " with vclass URI " +
//                    indiv.getVClassURI() + ", no vclass with that URI exists");
//        }
//        if (customView!=null) {
//            // insert test for whether a css files of the same name exists, and populate the customCss string for use when construction the header
//        }
        //String netid = iwDao.getNetId(indiv.getURI());
        
        //data.put("netid", netid);
        //data.put("vclassName", vclassName);

    	// RY We should not have references to a specific ontology in the vitro code!
        // Figure out how to move this out of here.
        boolean isPerson = individual.isVClass("http://xmlns.com/foaf/0.1/Person");
        map.put("isPerson", isPerson);
        if (isPerson) {
        	map.put("visualizationUrl", UrlBuilder.getUrl("/visualization", 
        			                                      "uri", individual.getURI()));
        }
        
        // RY Would like to use IndividualTemplateModel object, but may just end up copying all methods. Since object is put in template
        // with a read-only wrapper, it should be restrictive enough.
        map.put("individual",individual); //data.put("individual", new IndividualTemplateModel(indiv)); 
        //Portal portal = vreq.getPortal();
        //data.put("portal",String.valueOf(portal));
//        String view= getViewFromRequest(vreq);
//        if( view == null){
//            if (customView == null) {
//                view = default_jsp;
//                data.put("bodyJsp","/"+Controllers.ENTITY_JSP);
//                log.debug("no custom view and no view parameter in request for rendering "+indiv.getName());
//            } else {
//                view = default_jsp;
//                log.debug("setting custom view templates/entity/"+ customView + " for rendering "+indiv.getName());
//                data.put("bodyJsp", "/templates/entity/"+customView);
//            }
//            data.put("entityPropsListJsp",Controllers.ENTITY_PROP_LIST_JSP);
//            data.put("entityDatapropsListJsp",Controllers.ENTITY_DATAPROP_LIST_JSP);
//            data.put("entityMergedPropsListJsp",Controllers.ENTITY_MERGED_PROP_LIST_GROUPED_JSP);
//            data.put("entityKeywordsListJsp",Controllers.ENTITY_KEYWORDS_LIST_JSP);
//        } else {
//            log.debug("Found view parameter "+view+" in request for rendering "+indiv.getName());
//        }

        //setup highlighter for search terms
        checkForSearch(vreq, individual);


        if(  individual.getURI().startsWith( vreq.getWebappDaoFactory().getDefaultNamespace() )){        	
        	map.put("entityLinkedDataURL", individual.getURI() + "/" + individual.getLocalName() + ".rdf");	
        }
        
        
		// generate link to RDF representation for semantic web clients like Piggy Bank
		// BJL 2008-07-16: I'm temporarily commenting this out because I forgot we need to make sure it filters out the hidden properties
        // generate url for this entity
        // String individualToRDF = "http://"+vreq.getServerName()+":"+vreq.getServerPort()+vreq.getContextPath()+"/entity?home=1&uri="+forURL(entity.getURI())+"&view=rdf.rdf"; 
        //css += "<link rel='alternate' type='application/rdf+xml' title='"+entity.getName()+"' href='"+individualToRDF+"' />";


        //RequestDispatcher rd = vreq.getRequestDispatcher( view );
        //rd.forward(vreq,res);	
        
        return map;
	}

	private void doRdf(VitroRequest vreq, HttpServletResponse res,
			ContentType rdfFormat) throws IOException, ServletException {    	

		Individual indiv = getEntityFromRequest(vreq);		
		if( indiv == null ){
			doNotFound(vreq, res);
			return;
		}
				
		OntModel ontModel = null;
		HttpSession session = vreq.getSession(false);
		if( session != null )
			ontModel =(OntModel)session.getAttribute("jenaOntModel");		
		if( ontModel == null)
			ontModel = (OntModel)getServletContext().getAttribute("jenaOntModel");
			
		Model newModel;
		newModel = getRDF(indiv, ontModel, ModelFactory.createDefaultModel(), 0);		
		
		res.setContentType(rdfFormat.getMediaType());
		String format = ""; 
		if ( RDFXML_MIMETYPE.equals(rdfFormat.getMediaType()))
			format = "RDF/XML";
		else if( N3_MIMETYPE.equals(rdfFormat.getMediaType()))
			format = "N3";
		else if ( TTL_MIMETYPE.equals(rdfFormat.getMediaType()))
			format ="TTL";
		
		newModel.write( res.getOutputStream(), format );		
	}

	private void doRedirect(HttpServletRequest req, HttpServletResponse res,
			String redirectURL) {	
		// It seems like there must be a better way to do this
		String hn = req.getHeader("Host");		
    	res.setHeader("Location", res.encodeURL( "http://" + hn + req.getContextPath() + redirectURL ));
    	res.setStatus(res.SC_SEE_OTHER);		
	}


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

	private boolean checkForSunset(VitroRequest vreq, Individual entity) {
        // TODO Auto-generated method stub
        return false;
    }

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
    
    private String forURL(String frag) {
    	String result = null;
        try {
        	result = URLEncoder.encode(frag, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
    }
    
    private class HelpException extends Throwable{}
    private class EntityNotFoundException extends Throwable{}
}
