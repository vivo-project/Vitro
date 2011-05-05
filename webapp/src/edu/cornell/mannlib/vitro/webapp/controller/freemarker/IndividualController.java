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

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RdfResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperFactory;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;
import edu.cornell.mannlib.vitro.webapp.web.functions.IndividualLocalNameMethod;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ListedIndividualTemplateModel;
import freemarker.ext.beans.BeansWrapper;

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
    
    private static final Map<String, String> namespaces = new HashMap<String, String>() {{
        put("rdfs", VitroVocabulary.RDFS);
        put("vitro", VitroVocabulary.vitroURI);
        put("vitroPublic", VitroVocabulary.VITRO_PUBLIC);
    }};
    
    private static final String TEMPLATE_INDIVIDUAL_DEFAULT = "individual.ftl";
    private static final String TEMPLATE_HELP = "individual-help.ftl";
    
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
    	try {
    		cleanUpSession(vreq);

	        // get URL without hostname or servlet context
	        String url = vreq.getRequestURI().substring(vreq.getContextPath().length()); 
	
	        // Check to see if the request is for a non-information resource, redirect if it is.
	        String redirectURL = checkForRedirect ( url, vreq );
	        if( redirectURL != null ){
	            return new RedirectResponseValues(redirectURL);
	        }            	                                         
	
	        Individual individual = null;
	        try {
	            individual = getIndividualFromRequest(vreq);
	        } catch (Throwable th) {
	            return doHelp();
	        }
	        
	        if( individual == null || checkForHidden(vreq, individual) || checkForSunset(vreq, individual)){
	        	return doNotFound(vreq);
	        }

            ContentType rdfFormat = checkForLinkedDataRequest(url, vreq);
            if( rdfFormat != null ){
                return doRdf(vreq, individual, rdfFormat);
            }   
	            
	        // If this is an uploaded file, redirect to its "alias URL".
	        String aliasUrl = getAliasUrlForBytestreamIndividual(vreq, individual);
	        if (aliasUrl != null) {
	        	return new RedirectResponseValues(UrlBuilder.getUrl(vreq.getContextPath() + aliasUrl));
	        }

	        Map<String, Object> body = new HashMap<String, Object>();

            body.put("title", individual.getName());            
    		body.put("relatedSubject", getRelatedSubject(vreq));
    		body.put("namespaces", namespaces);
    		body.put("temporalVisualizationEnabled", getTemporalVisualizationFlag());
    		
    		IndividualTemplateModel itm = getIndividualTemplateModel(vreq, individual);
    		/* We need to expose non-getters in displaying the individual's property list, 
    		 * since it requires calls to methods with parameters.
    		 * This is still safe, because we are only putting BaseTemplateModel objects
    		 * into the data model: no real data can be modified. 
    		 */
	        body.put("individual", getNonDefaultBeansWrapper(BeansWrapper.EXPOSE_SAFE).wrap(itm));
	        body.put("headContent", getRdfLinkTag(itm));	       
	        
	        String template = getIndividualTemplate(individual, vreq);
	                
	        return new TemplateResponseValues(template, body);
        
	    } catch (Throwable e) {
	        log.error(e, e);
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
                map.put("url", (new ListedIndividualTemplateModel(relatedSubjectInd, vreq)).getProfileUrl());
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
    
    private String getRdfLinkTag(IndividualTemplateModel itm) {
        String linkTag = null;
        String linkedDataUrl = itm.getRdfUrl(false);
        if (linkedDataUrl != null) {
            linkTag = "<link rel=\"alternate\" type=\"application/rdf+xml\" href=\"" +
                          linkedDataUrl + "\" /> ";
        }
        return linkTag;
    }
    
	private IndividualTemplateModel getIndividualTemplateModel(VitroRequest vreq, Individual individual) 
	    throws ServletException, IOException {
		
    	IndividualDao iwDao = vreq.getWebappDaoFactory().getIndividualDao();
        
        individual.setKeywords(iwDao.getKeywordsForIndividualByMode(individual.getURI(),"visible"));
        individual.sortForDisplay();

        return new IndividualTemplateModel(individual, vreq);
	}
	
	// Determine whether the individual has a custom display template based on its class membership.
	// If not, return the default individual template.
	private String getIndividualTemplate(Individual individual, VitroRequest vreq) {
	    
        @SuppressWarnings("unused")
        String vclassName = "unknown"; 
        String customTemplate = null;

        // First check vclass
        if( individual.getVClass() != null ){ 
            vclassName = individual.getVClass().getName();
            List<VClass> directClasses = individual.getVClasses(true);
            for (VClass vclass : directClasses) {
                customTemplate = vclass.getCustomDisplayView();
                if (customTemplate != null) {
                    if (customTemplate.length()>0) {
                        vclassName = vclass.getName(); // reset entity vclassname to name of class where a custom view; this call has side-effects
                        log.debug("Found direct class [" + vclass.getName() + "] with custom view " + customTemplate + "; resetting entity vclassName to this class");
                        break;
                    } else {
                        customTemplate = null;
                    }
                }
            }
            // If no custom template defined, check other vclasses
            if (customTemplate == null) {
                List<VClass> inferredClasses = individual.getVClasses(false);
                for (VClass vclass : inferredClasses) {
                    customTemplate = vclass.getCustomDisplayView();
                    if (customTemplate != null) {
                        if (customTemplate.length()>0) {
                            // note that NOT changing entity vclassName here yet
                            log.debug("Found inferred class [" + vclass.getName() + "] with custom view " + customTemplate);
                            break;
                        } else {
                            customTemplate = null;
                        }
                    }
                }
            }
            // If still no custom template defined, and inferencing is asynchronous (under RDB), check
            // the superclasses of the vclass for a custom template specification. 
            if (customTemplate == null && SimpleReasoner.isABoxReasoningAsynchronous(getServletContext())) { 
                log.debug("Checking superclasses for custom template specification because ABox reasoning is asynchronous");
                for (VClass directVClass : directClasses) {
                    VClassDao vcDao = vreq.getWebappDaoFactory().getVClassDao();
                    List<String> superClassUris = vcDao.getAllSuperClassURIs(directVClass.getURI());
                    for (String uri : superClassUris) {
                        VClass vclass = vcDao.getVClassByURI(uri);
                        customTemplate = vclass.getCustomDisplayView();
                        if (customTemplate != null) {
                            if (customTemplate.length()>0) {
                                // note that NOT changing entity vclassName here
                                log.debug("Found superclass [" + vclass.getName() + "] with custom view " + customTemplate);
                                break;
                            } else {
                                customTemplate = null;
                            }                            
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

	private static Pattern LINKED_DATA_URL = Pattern.compile("^/individual/([^/]*)$");		
	private static Pattern NS_PREFIX_URL = Pattern.compile("^/individual/([^/]*)/([^/]*)$");
	
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
    public static Individual getIndividualFromRequest(VitroRequest vreq) {
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
    		SelfEditingConfiguration sec = SelfEditingConfiguration.getBean(vreq);
    		uri = sec.getIndividualUriFromUsername(iwDao, netIdStr);
            return iwDao.getIndividualByURI(uri);
        }

		return null;		
    }
 
	
	private static Pattern URI_PATTERN = Pattern.compile("^/individual/([^/]*)$");
    //Redirect if the request is for http://hostname/individual/localname
    // if accept is nothing or text/html redirect to ???
    // if accept is some RDF thing redirect to the URL for RDF
	private String checkForRedirect(String url, VitroRequest vreq) {
		Matcher m = URI_PATTERN.matcher(url);
		if( m.matches() && m.groupCount() == 1 ){			
			ContentType c = checkForLinkedDataRequest(url, vreq);			
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
			return UrlBuilder.getUrl("display/" + m.group(1));
		}else{			
			return null;
		}
	}

	private static Pattern RDF_REQUEST = Pattern.compile("^/individual/([^/]*)/\\1.rdf$");
    private static Pattern N3_REQUEST = Pattern.compile("^/individual/([^/]*)/\\1.n3$");
    private static Pattern TTL_REQUEST = Pattern.compile("^/individual/([^/]*)/\\1.ttl$");
    private static Pattern HTML_REQUEST = Pattern.compile("^/display/([^/]*)$");
    
    public static final Pattern RDFXML_FORMAT = Pattern.compile("rdfxml");
    public static final Pattern N3_FORMAT = Pattern.compile("n3");
    public static final Pattern TTL_FORMAT = Pattern.compile("ttl");
    
    /**  
     * @return null if this is not a linked data request, returns content type if it is a 
     * linked data request.
     */
	protected ContentType checkForLinkedDataRequest(String url, VitroRequest vreq ) {		
		try {
		    ContentType contentType = null;
		    Matcher m;
		    // Check for url param specifying format
		    String formatParam = (String) vreq.getParameter("format");
		    if (formatParam != null) {
		        m = RDFXML_FORMAT.matcher(formatParam);
		        if ( m.matches() ) {
		            return new ContentType(RDFXML_MIMETYPE);
		        }
	            m = N3_FORMAT.matcher(formatParam);
	            if( m.matches() ) {
	                return new ContentType(N3_MIMETYPE);
	            }
	            m = TTL_FORMAT.matcher(formatParam);
	            if( m.matches() ) {
	                return new ContentType(TTL_MIMETYPE);
	            } 		        
		    }
		    
			//check the accept header
		    String acceptHeader = vreq.getHeader("accept");
			if (acceptHeader != null) {
				List<ContentType> actualContentTypes = new ArrayList<ContentType>();				
				actualContentTypes.add(new ContentType( XHTML_MIMETYPE ));
				actualContentTypes.add(new ContentType( HTML_MIMETYPE ));				
				
				actualContentTypes.add(new ContentType( RDFXML_MIMETYPE ));
				actualContentTypes.add(new ContentType( N3_MIMETYPE ));
				actualContentTypes.add(new ContentType( TTL_MIMETYPE ));
			
				contentType = ContentType.getBestContentType(acceptHeader,actualContentTypes);
				if (contentType!=null && (
						RDFXML_MIMETYPE.equals(contentType.getMediaType()) || 
						N3_MIMETYPE.equals(contentType.getMediaType()) ||
						TTL_MIMETYPE.equals(contentType.getMediaType()) ))
					return contentType;				
			}
			
			/*
			 * check for parts of URL that indicate request for RDF
			   http://vivo.cornell.edu/individual/n23/n23.rdf
			   http://vivo.cornell.edu/individual/n23/n23.n3
			   http://vivo.cornell.edu/individual/n23/n23.ttl
			 */
	        m = RDF_REQUEST.matcher(url);
	        if( m.matches() ) {
	            return new ContentType(RDFXML_MIMETYPE);
	        }
	        m = N3_REQUEST.matcher(url);
	        if( m.matches() ) {
	            return new ContentType(N3_MIMETYPE);
	        }
	        m = TTL_REQUEST.matcher(url);
	        if( m.matches() ) {
	            return new ContentType(TTL_MIMETYPE);
	        }    
			
			
		} catch (Throwable th) {
			log.error("problem while checking accept header " , th);
		}
		return null;
	}  
	
	private ContentType getContentTypeFromString(String string) {

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
		
		if (entity.getURI().equals(url)) {
			// Avoid a tight loop; if the alias URL is equal to the URI, then
			// don't recognize it as a File Bytestream.
			return null;
		} else {
			return url;
		}
	}
 
	private boolean getTemporalVisualizationFlag() {
		String property = ConfigurationProperties.getProperty("visualization.temporal");
		return "enabled".equals(property);
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
        
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body);
    }

}
