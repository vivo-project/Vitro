/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperFactory;

public class URLRewritingHttpServletResponse extends HttpServletResponseWrapper/*implements HttpServletResponse */{

	private final static Log log = LogFactory.getLog(URLRewritingHttpServletResponse.class);
	
	private HttpServletResponse _response;
	private ServletContext _context;
	private WebappDaoFactory wadf;
	private int contextPathDepth;
	private Pattern slashPattern = Pattern.compile("/");
	
	public URLRewritingHttpServletResponse(HttpServletResponse response, HttpServletRequest request, ServletContext context) {
	    super(response);
		this._response = response;
		this._context = context;
		this.wadf = (WebappDaoFactory) context.getAttribute("webappDaoFactory");
		this.contextPathDepth = slashPattern.split(request.getContextPath()).length-1;
	}
		
	/** for testing. */
	protected URLRewritingHttpServletResponse(HttpServletResponse res){
	    super(res);
	}
	
	/**
	 * @deprecated
	 */
	public String encodeRedirectUrl(String arg0) {
		return _response.encodeRedirectUrl(arg0);
	}

	public String encodeRedirectURL(String arg0) {
		return _response.encodeRedirectURL(arg0);
	}
	
	/**
	 * @deprecated
	 */
	public String encodeUrl(String arg0) {
		return _response.encodeUrl(arg0);
	}

	public String encodeURL(String inUrl) {
	    List<String> externallyLinkedNamespaces = wadf.getApplicationDao().getExternallyLinkedNamespaces();
        NamespaceMapper nsMap = NamespaceMapperFactory.getNamespaceMapper(_context);

        if( log.isDebugEnabled() ){
            log.debug("START");
            log.debug("charEncoding: "  + this.getCharacterEncoding() );
            log.debug("contextPathDepth," + contextPathDepth);
            log.debug("nsMap," + nsMap);
            log.debug("wadf.getDefaultNamespace(), " + wadf.getDefaultNamespace());
            log.debug("externallyLinkedNamespaces " + externallyLinkedNamespaces);
            log.debug( inUrl );
        }
        
	    String encodedUrl = encodeForVitro(
	            inUrl,
	            this.getCharacterEncoding(),
	            /*wadf.getPortalDao().isSinglePortal()*/ true,
	            contextPathDepth,
	            nsMap,
	            wadf.getDefaultNamespace(),
	            externallyLinkedNamespaces
	            );
	    
	    log.debug(encodedUrl);
	    log.debug("END");
	    return encodedUrl;
	}
	
	/**
	 * bdc34: Isolating this method for unit 
	 * testing purposes.  This method should not use 
	 * any object properties, only objects passed into method. 
	 */
	protected String encodeForVitro(
	        String inUrl, 
	        String characterEncoding, 
	        Boolean isSInglePortal,
	        int contextPathDepth,
	        NamespaceMapper nsMap,
	        String defaultNamespace,
	        List<String> externalNamespaces) {
		try {
			if( log.isDebugEnabled() ){
			    log.debug("Incoming URL '" + inUrl + "'");
			}
			VitroURL url = new VitroURL(inUrl,characterEncoding);
			if (url.host != null) {
				// if it's not an in-context URL, we don't want to mess with it
				// It looks like encodeURL isn't even called for external URLs
			    //String rv = _response.encodeURL(inUrl); 
	            String rv = inUrl;
			    if( log.isDebugEnabled()){
			        log.debug("Encoded as  '"+rv+"'");
			    }
				return rv;
			}
			
			// rewrite "entity" as "individual"
			if ("entity".equals(url.pathParts.get(url.pathParts.size()-1))) {
				url.pathParts.set(url.pathParts.size()-1, "individual");
			}
			
			// rewrite individual URI parameters as pretty URLs if possible
			if ("individual".equals(url.pathParts.get(url.pathParts.size()-1))) {
				Iterator<String[]> qpIt = url.queryParams.iterator();
				int qpIndex = -1;
				int indexToRemove = -1;
				while (qpIt.hasNext()) {
					String[] keyAndValue = qpIt.next();
					qpIndex++;
					if ( ("uri".equals(keyAndValue[0])) && (keyAndValue.length>1) && (keyAndValue[1] != null) ) {
						try {
							URI uri = new URIImpl(keyAndValue[1]);
							String namespace = uri.getNamespace();
							String localName = uri.getLocalName();
							if ( (namespace != null) && (localName != null) ) { 
								String prefix = nsMap.getPrefixForNamespace(namespace);
								if (defaultNamespace.equals(namespace) && prefix == null) {
									// make a URI that matches the URI
									// of the resource to support
									// linked data request
									url.pathParts.add(localName);
									// remove the ugly uri parameter
									indexToRemove = qpIndex;
							    // namespace returned from URIImpl.getNamespace() ends in a slash, so will 
							    // match externally linked namespaces, which also end in a slash
								} else if (isExternallyLinkedNamespace(namespace,externalNamespaces)) {
								    log.debug("Found externally linked namespace " + namespace);
								    // Use the externally linked namespace in the url
								    url.pathParts = new ArrayList<String>();
								    // toString() will join pathParts with a slash, so remove this one.
								    url.pathParts.add(namespace.replaceAll("/$", ""));
								    url.pathParts.add(localName);
								    // remove the ugly uri parameter
								    indexToRemove = qpIndex;
								    // remove protocol, host, and port, since the external namespace
								    // includes these elements
								    url.protocol = null;
								    url.host = null;
								    url.port = null;
								    url.pathBeginsWithSlash = false;
								} else if (prefix != null) {
									// add the pretty path parts
									url.pathParts.add(prefix);
									url.pathParts.add(localName);
									// remove the ugly uri parameter
									indexToRemove = qpIndex;
								}
							}
						} catch (Exception e) {
						    if( keyAndValue.length > 0 )
						        log.debug("Invalid URI: '"+keyAndValue[1] + "'");
						    else
						        log.debug("empty URI");
						}
					}
				}
				if (indexToRemove > -1) {
					url.queryParams.remove(indexToRemove);
				}
	
			}
			//String rv = _response.encodeURL(_response.encodeURL(url.toString()));
	         String rv = url.toString();
			if( log.isDebugEnabled()){
			    log.debug("Encoded as  '" + rv + "'");
			}
			return rv;
		} catch (Exception e) {			
			log.error(e,e);			
            //String rv =  _response.encodeURL(inUrl);
			String rv =  inUrl;
            log.error("Encoded as  '"+rv+"'");
			return rv;
		}
	}	
	
	private boolean isExternallyLinkedNamespace(String namespace,List<String> externallyLinkedNamespaces) {	    
	    return externallyLinkedNamespaces.contains(namespace);
	}
	
}
