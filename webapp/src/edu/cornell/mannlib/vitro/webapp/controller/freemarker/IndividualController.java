/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.SeeVerbosePropertyInformation;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RdfResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperFactory;
import edu.cornell.mannlib.vitro.webapp.utils.jena.ExtendedLinkedDataUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaOutputUtils;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;
import edu.cornell.mannlib.vitro.webapp.web.beanswrappers.ReadOnlyBeansWrapper;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.ListedIndividual;

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
    private static final String RICH_EXPORT_ROOT = "/WEB-INF/rich-export/";
    private static final String PERSON_CLASS_URI = "http://xmlns.com/foaf/0.1/Person";
    private static final String INCLUDE_ALL = "all";
    
    @SuppressWarnings("serial")
    private static final Map<String, String> namespaces = new HashMap<String, String>() {{
        put("display", VitroVocabulary.DISPLAY);
        put("vitro", VitroVocabulary.vitroURI);
        put("vitroPublic", VitroVocabulary.VITRO_PUBLIC);
    }};
        
	private static final Property extendedLinkedDataProperty = ResourceFactory.createProperty(namespaces.get("vitro") + "extendedLinkedData");
	private static final Literal xsdTrue = ResourceFactory.createTypedLiteral("true", XSDDatatype.XSDboolean);
	
    private static final String TEMPLATE_INDIVIDUAL_DEFAULT = "individual.ftl";
    private static final String TEMPLATE_HELP = "individual-help.ftl";
    private static Map<String,Float>qsMap;
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
    	try {
    		cleanUpSession(vreq);

	        // get URL without hostname or servlet context
	        String url = vreq.getRequestURI().substring(vreq.getContextPath().length()); 
	
	        // Check to see if the request is for a non-information resource, redirect if it is.
	        String redirectURL = checkForRedirect ( url, vreq );
	        if( redirectURL != null ){
	            return new RedirectResponseValues(redirectURL, HttpServletResponse.SC_SEE_OTHER);
	        }            	                                         
	
	        Individual individual = null;
	        try {
	            individual = getIndividualFromRequest(vreq);
	        } catch (Throwable th) {
	            return doHelp();
	        }
	        
	        if( individual == null ){
	        	return doNotFound(vreq);
	        }

            ContentType rdfFormat = checkUrlForLinkedDataRequest(url, vreq);
            if( rdfFormat != null ){
                return doRdf(vreq, individual, rdfFormat);
            }   
	            
	        // If this is an uploaded file, redirect to its "alias URL".
	        String aliasUrl = getAliasUrlForBytestreamIndividual(vreq, individual);
	        if (aliasUrl != null) {
	            return new RedirectResponseValues(aliasUrl, HttpServletResponse.SC_SEE_OTHER);	            
	        }

	        Map<String, Object> body = new HashMap<String, Object>();

            body.put("title", individual.getName());            
    		body.put("relatedSubject", getRelatedSubject(vreq));
    		body.put("namespaces", namespaces);
    		body.put("temporalVisualizationEnabled", getTemporalVisualizationFlag());
    		body.put("verbosePropertySwitch", getVerbosePropertyValues(vreq));
    		
    		IndividualTemplateModel itm = getIndividualTemplateModel(individual, vreq);
    		/* We need to expose non-getters in displaying the individual's property list, 
    		 * since it requires calls to methods with parameters.
    		 * This is still safe, because we are only putting BaseTemplateModel objects
    		 * into the data model: no real data can be modified. 
    		 */
	        // body.put("individual", wrap(itm, BeansWrapper.EXPOSE_SAFE));
    		body.put("individual", wrap(itm, new ReadOnlyBeansWrapper()));
    		
	        body.put("headContent", getRdfLinkTag(itm));	       
	        
	        //If special values required for individuals like menu, include values in template values
	        body.putAll(getSpecialEditingValues(vreq));
	        
	        String template = getIndividualTemplate(individual, vreq);
	                
	        return new TemplateResponseValues(template, body);
        
	    } catch (Throwable e) {
	        log.error(e, e);
	        return new ExceptionResponseValues(e);
	    }
    }

    private void cleanUpSession(VitroRequest vreq) {
		// We should not remove edit configurations from the session because the user
        // may resubmit the forms via the back button and the system is setup to handle this.         
    }
    
    private Map<String, Object> getVerbosePropertyValues(VitroRequest vreq) {
        
        Map<String, Object> map = null;
        
        if (PolicyHelper.isAuthorizedForActions(vreq, new SeeVerbosePropertyInformation())) {
            // Get current verbose property display value
            String verbose = vreq.getParameter("verbose");
            Boolean verboseValue;
            // If the form was submitted, get that value
            if (verbose != null) {
                verboseValue = "true".equals(verbose);
            // If form not submitted, get the session value
            } else {
                Boolean verbosePropertyDisplayValueInSession = (Boolean) vreq.getSession().getAttribute("verbosePropertyDisplay"); 
                // True if session value is true, otherwise (session value is false or null) false
                verboseValue = Boolean.TRUE.equals(verbosePropertyDisplayValueInSession);           
            }
            vreq.getSession().setAttribute("verbosePropertyDisplay", verboseValue);
            
            map = new HashMap<String, Object>();
            map.put("currentValue", verboseValue);

            /* Factors contributing to switching from a form to an anchor element:
               - Can't use GET with a query string on the action unless there is no form data, since
                 the form data is appended to the action with a "?", so there can't already be a query string
                 on it.
               - The browser (at least Firefox) does not submit a form that has no form data.
               - Some browsers might strip the query string off the form action of a POST - though 
                 probably they shouldn't, because the HTML spec allows a full URI as a form action.
               - Given these three, the only reliable solution is to dynamically create hidden inputs
                 for the query parameters. 
               - Much simpler is to just create an anchor element. This has the added advantage that the
                 browser doesn't ask to resend the form data when reloading the page.
             */
            String url = vreq.getRequestURI() + "?verbose=" + !verboseValue;
            // Append request query string, except for current verbose value, to url
            String queryString = vreq.getQueryString();
            if (queryString != null) {
                String[] params = queryString.split("&");
                for (String param : params) {
                    if (! param.startsWith("verbose=")) {
                        url += "&" + param;
                    }
                }
            }
            map.put("url", url);            
        } else {
            vreq.getSession().setAttribute("verbosePropertyDisplay", false);
        }
        
        return map;
    }
    
    //Get special values for cases such as Menu Management editing
    private Map<String, Object> getSpecialEditingValues(VitroRequest vreq) {
        
        Map<String, Object> map = new HashMap<String, Object>();
        
    	if(vreq.getAttribute(VitroRequest.SPECIAL_WRITE_MODEL) != null) {
    		map.put("reorderUrl", UrlBuilder.getUrl(DisplayVocabulary.REORDER_MENU_URL));
    	}
    	
    	return map;
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
                map.put("url", UrlBuilder.getIndividualProfileUrl(relatedSubjectInd, vreq));
                map.put("url", (new ListedIndividual(relatedSubjectInd, vreq)).getProfileUrl());
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
        String linkedDataUrl = itm.getRdfUrl();
        if (linkedDataUrl != null) {
            linkTag = "<link rel=\"alternate\" type=\"application/rdf+xml\" href=\"" +
                          linkedDataUrl + "\" /> ";
        }
        return linkTag;
    }
    
	private IndividualTemplateModel getIndividualTemplateModel(Individual individual, VitroRequest vreq) 
	    throws ServletException, IOException {
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
            SimpleReasoner simpleReasoner = (SimpleReasoner) getServletContext().getAttribute(SimpleReasoner.class.getName());
            if (customTemplate == null && simpleReasoner != null && simpleReasoner.isABoxReasoningAsynchronous()) { 
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
				
		OntModel ontModel = vreq.getJenaOntModel();		
                
        String[] includes = vreq.getParameterValues("include");
		Model newModel = getRDF(individual,ontModel,ModelFactory.createDefaultModel(),0,includes);		
		JenaOutputUtils.setNameSpacePrefixes(newModel, vreq.getWebappDaoFactory());
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
    		List<Individual> assocInds = sec.getAssociatedIndividuals(iwDao, netIdStr);
    		if (!assocInds.isEmpty()) {
    			return assocInds.get(0);
    		}
        }

		return null;		
    }
	
	/* 
	 * Following recipe 3 from "Best Practice Recipes for Publishing RDF Vocabularies."
	 * See http://www.w3.org/TR/swbp-vocab-pub/#recipe3.
	 * The basic idea is that a URI like http://vivo.cornell.edu/individual/n1234
	 * identifies a real world individual. HTTP cannot send that as the response
	 * to a GET request because it can only send bytes and not things. The server 
	 * sends a 303, to mean "you asked for something I cannot send you, but I can 
	 * send you this other stream of bytes about that thing." 
	 * In the case of a request like http://vivo.cornell.edu/individual/n1234/n1234.rdf
	 * or http://vivo.cornell.edu/individual/n1234?format=rdfxml,
	 * the request is for a set of bytes rather than an individual, so no 303 is needed.
     */
    private static Pattern URI_PATTERN = Pattern.compile("^/individual/([^/]*)$");
	private String checkForRedirect(String url, VitroRequest vreq) {
	   
	    String formatParam = (String) vreq.getParameter("format");
	    if ( formatParam == null ) {
	        Matcher m = URI_PATTERN.matcher(url);
    		if ( m.matches() && m.groupCount() == 1 ) {	
    			ContentType c = checkAcceptHeaderForLinkedDataRequest(url, vreq);			
    			if ( c != null ) {
    				String redirectUrl = "/individual/" + m.group(1) + "/" + m.group(1) ; 
    				if ( RDFXML_MIMETYPE.equals( c.getMediaType() ) ) {
    					return redirectUrl + ".rdf";
    				} else if ( N3_MIMETYPE.equals( c.getMediaType() ) ) {
    					return redirectUrl + ".n3";
    				} else if ( TTL_MIMETYPE.equals( c.getMediaType() ) ) {
    					return redirectUrl + ".ttl";
    				}//else send them to html													
    			}
    			//else redirect to HTML representation
    			return "display/" + m.group(1);
    		} 
	    }
	    return null;
	}

    protected ContentType checkAcceptHeaderForLinkedDataRequest(String url, VitroRequest vreq) {
        try {
            /*
             * Check the accept header. This request will trigger a 
             * redirect with a 303 ("see also"), because the request is for 
             * an individual but the server can only provide a set of bytes.
             */
            String acceptHeader = vreq.getHeader("accept");
            if (acceptHeader != null) {             
                String ctStr = ContentType.getBestContentType(
                        ContentType.getTypesAndQ(acceptHeader), 
                        getAcceptedContentTypes());
                                
                if (ctStr!=null && (
                        RDFXML_MIMETYPE.equals(ctStr) || 
                        N3_MIMETYPE.equals(ctStr) ||
                        TTL_MIMETYPE.equals(ctStr) ))
                    return new ContentType(ctStr);              
            }            
        } catch (Throwable th) {
            log.error("Problem while checking accept header " , th);
        }
        return null;
    }

    public static final Pattern RDFXML_FORMAT = Pattern.compile("rdfxml");
    public static final Pattern N3_FORMAT = Pattern.compile("n3");
    public static final Pattern TTL_FORMAT = Pattern.compile("ttl");
    
    private static Pattern RDF_REQUEST = Pattern.compile("^/individual/([^/]*)/\\1.rdf$");
    private static Pattern N3_REQUEST = Pattern.compile("^/individual/([^/]*)/\\1.n3$");
    private static Pattern TTL_REQUEST = Pattern.compile("^/individual/([^/]*)/\\1.ttl$");
    private static Pattern HTML_REQUEST = Pattern.compile("^/display/([^/]*)$");
    
    /**  
     * @return null if this is not a linked data request, returns content type if it is a 
     * linked data request. 
     * These are Vitro-specific ways of requesting rdf, unrelated to semantic web standards.
     * They do not trigger a redirect with a 303, because the request is for a set of bytes
     * rather than an individual.
     */
	protected ContentType checkUrlForLinkedDataRequest(String url, VitroRequest vreq ) {		

	    Matcher m;
	    
	    /*
	     * Check for url param specifying format.
	     * Example: http://vivo.cornell.edu/individual/n23?format=rdfxml
	     */
	    String formatParam = (String) vreq.getParameter("format");
	    if (formatParam != null) {
	        m = RDFXML_FORMAT.matcher(formatParam);
	        if ( m.matches() ) {
	            return  ContentType.RDFXML;
	        }
            m = N3_FORMAT.matcher(formatParam);
            if( m.matches() ) {
                return  ContentType.N3;
            }
            m = TTL_FORMAT.matcher(formatParam);
            if( m.matches() ) {
                return  ContentType.TURTLE;
            } 		        
	    }

		/*
		 * Check for parts of URL that indicate request for RDF. Examples:
		 * http://vivo.cornell.edu/individual/n23/n23.rdf
		 * http://vivo.cornell.edu/individual/n23/n23.n3
		 * http://vivo.cornell.edu/individual/n23/n23.ttl
		 */
        m = RDF_REQUEST.matcher(url);
        if( m.matches() ) {
            return ContentType.RDFXML;
        }
        m = N3_REQUEST.matcher(url);
        if( m.matches() ) {
            return ContentType.N3;
        }
        m = TTL_REQUEST.matcher(url);
        if( m.matches() ) {
            return ContentType.TURTLE;
        }    
						
		return null;
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
		String property = ConfigurationProperties.getBean(getServletContext())
				.getProperty("visualization.temporal");
		return "enabled".equals(property);
	}

    private Model getRDF(Individual entity, OntModel contextModel, Model newModel, int recurseDepth, String[] includes) {
    	
    	Resource subj = newModel.getResource(entity.getURI());
    	
    	List<DataPropertyStatement> dstates = entity.getDataPropertyStatements();
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
    	
    	if (recurseDepth < 5) {
	    	List<ObjectPropertyStatement> ostates = entity.getObjectPropertyStatements();
	    	
	    	for (ObjectPropertyStatement os: ostates) {
	    		Property prop = newModel.getProperty(os.getPropertyURI());
	    		Resource obj = newModel.getResource(os.getObjectURI());
	    		newModel.add(newModel.createStatement(subj, prop, obj));
	    		if ( includeInLinkedData(obj, contextModel)) {
	    			newModel.add(getRDF(os.getObject(), contextModel, newModel, recurseDepth + 1, includes));
	    	    } else {
	    	    	contextModel.enterCriticalSection(Lock.READ);
	    			try {
	    				newModel.add(contextModel.listStatements(obj, RDFS.label, (RDFNode)null));
	    			} finally {
	    				contextModel.leaveCriticalSection();
	    			} 
	    	    }
	    	}
    	}
    	
    	newModel = getLabelAndTypes(entity, contextModel, newModel );
    		
    	// get all the statements not covered by the object property / datatype property code above
    	// note implication that extendedLinkedData individuals will only be evaluated for the
    	// recognized object properties.
    	contextModel.enterCriticalSection(Lock.READ);
		try {
			StmtIterator iter = contextModel.listStatements(subj, (Property) null, (RDFNode) null);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				if (!newModel.contains(stmt)) {
				   newModel.add(stmt);
				}
			}  
		} finally {
			contextModel.leaveCriticalSection();
		} 
			
		if (recurseDepth == 0 && includes != null && entity.isVClass(PERSON_CLASS_URI)) {
			
	        for (String include : includes) {
	       
	        	String rootDir = null;
	        	if (INCLUDE_ALL.equals(include)) {
	        		rootDir = RICH_EXPORT_ROOT;
	        	} else {
	        		rootDir = RICH_EXPORT_ROOT +  include + "/";
	        	}
	        	
	        	long start = System.currentTimeMillis();
				Model extendedModel = ExtendedLinkedDataUtils.createModelFromQueries(getServletContext(), rootDir, contextModel, entity.getURI());
	        	long elapsedTimeMillis = System.currentTimeMillis()-start;
	        	log.info("Time to create rich export model: msecs = " + elapsedTimeMillis);
	        	
				newModel.add(extendedModel);
	        }
		}
		
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

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }

    private ResponseValues doHelp() throws IOException, ServletException {
        return new TemplateResponseValues(TEMPLATE_HELP);
    }
    
    private ResponseValues doNotFound(VitroRequest vreq) throws IOException, ServletException {

        //set title before we do the highlighting so we don't get markup in it.
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title","Individual Not Found");
        body.put("errorMessage", "The individual was not found in the system.");
        
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_NOT_FOUND);
    }

    public static Map<String, Float> getAcceptedContentTypes() {
        if( qsMap == null ){
            HashMap<String,Float> map = new HashMap<String,Float>();
            map.put(HTML_MIMETYPE , 0.5f);
            map.put(XHTML_MIMETYPE, 0.5f);
            map.put("application/xml", 0.5f);
            map.put(RDFXML_MIMETYPE, 1.0f);
            map.put(N3_MIMETYPE, 1.0f);
            map.put(TTL_MIMETYPE, 1.0f);
            qsMap = map;
        }
        return qsMap;
    }
    
    public static boolean includeInLinkedData(Resource object, Model contextModel) {
 
       	boolean retval = false;
       	
       	contextModel.enterCriticalSection(Lock.READ);
       	
       	try {
	    	StmtIterator iter = contextModel.listStatements(object, RDF.type, (RDFNode)null);
	    	    	
	    	while (iter.hasNext()) {
	    		Statement stmt = iter.next();
	    		
	    		if (stmt.getObject().isResource() && contextModel.contains(stmt.getObject().asResource(), extendedLinkedDataProperty, xsdTrue)) {
	    			retval = true;
	    		    break;
	    		}	
	    	}
       	} finally {
       		contextModel.leaveCriticalSection();
       	}
    	   	
    	return retval;
    }    
}
