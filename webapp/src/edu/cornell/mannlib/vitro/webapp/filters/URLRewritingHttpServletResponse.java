/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperFactory;

public class URLRewritingHttpServletResponse implements HttpServletResponse {

	private final static Log log = LogFactory.getLog(URLRewritingHttpServletResponse.class);
	
	private HttpServletResponse _response;
	private HttpServletRequest _request;
	private ServletContext _context;
	private int contextPathDepth;
	private Pattern slashPattern = Pattern.compile("/");
	
	public URLRewritingHttpServletResponse(HttpServletResponse response, HttpServletRequest request, ServletContext context) {
		this._response = response;
		this._context = context;
		this.contextPathDepth = slashPattern.split(request.getContextPath()).length-1;
	}

	public void addCookie(Cookie arg0) {
		_response.addCookie(arg0);
	}

	public void addDateHeader(String arg0, long arg1) {
		_response.addDateHeader(arg0, arg1);
	}

	public void addHeader(String arg0, String arg1) {
		_response.addHeader(arg0, arg1);
	}

	public void addIntHeader(String arg0, int arg1) {
		_response.addIntHeader(arg0, arg1);
	}

	public boolean containsHeader(String arg0) {
		return _response.containsHeader(arg0);
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
		try {
			
			VitroURL url = new VitroURL(inUrl,this.getCharacterEncoding());
			if (url.host != null) {
				// if it's not an in-context URL, we don't want to mess with it
				// It looks like encodeURL isn't even called for external URLs
				return _response.encodeURL(inUrl);
			}
			
			// rewrite home parameters as portal prefixes for URLs not relative to the current location
			if (url.pathBeginsWithSlash && PortalPickerFilter.isPortalPickingActive) {
				PortalPickerFilter ppf = PortalPickerFilter.getPortalPickerFilter(this._context);
				if ( (ppf != null) && (url.queryParams != null) ) {
					Iterator<String[]> qpIt = url.queryParams.iterator();
					int qpIndex = -1;
					int indexToRemove = -1;
					while (qpIt.hasNext()) {
						String[] keyAndValue = qpIt.next();
						qpIndex++;			
						if ( ("home".equals(keyAndValue[0])) && (keyAndValue.length>1) && (keyAndValue[1] != null) ) {
							try {
								int portalId = Integer.decode(keyAndValue[1].trim());
								if ((Portal.DEFAULT_PORTAL_ID == portalId)) {
									indexToRemove = qpIndex;
								} else {
									String prefix = ppf.getPortalId2PrefixMap().get(portalId);
									if ( (prefix != null) && (!prefix.equals(url.pathParts.get(contextPathDepth))) ) {		
										url.pathParts.add(contextPathDepth,prefix);									
										url.pathBeginsWithSlash = true;
										indexToRemove = qpIndex;
									}
								}
							} catch (NumberFormatException nfe) {
								log.error("Invalid portal id string: "+keyAndValue[1], nfe);
							}
						}
					}
					if (indexToRemove > -1) {
						url.queryParams.remove(indexToRemove);
					}
		
				}
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
						NamespaceMapper nsMap = NamespaceMapperFactory.getNamespaceMapper(_context);
						try {
							URI uri = new URIImpl(keyAndValue[1]);
							if ( (uri.getNamespace() != null) && (uri.getLocalName() != null) ) { 
								String prefix = nsMap.getPrefixForNamespace(uri.getNamespace());
								String localName = uri.getLocalName();
								if (prefix != null) {
									// add the pretty path parts
									url.pathParts.add(prefix);
									url.pathParts.add(localName);
									// remove the ugly uri parameter
									indexToRemove = qpIndex;
								}
							}
						} catch (Exception e) {
							log.error("Invalid URI "+keyAndValue[1], e);
						}
					}
				}
				if (indexToRemove > -1) {
					url.queryParams.remove(indexToRemove);
				}
	
			}
			return _response.encodeURL(_response.encodeURL(url.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			return _response.encodeURL(inUrl);
		}
	}

	public void flushBuffer() throws IOException {
		_response.flushBuffer();
	}

	public int getBufferSize() {
		return _response.getBufferSize();
	}

	public String getCharacterEncoding() {
		return _response.getCharacterEncoding();
	}

	public String getContentType() {
		return _response.getContentType();
	}

	public Locale getLocale() {
		return _response.getLocale();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return _response.getOutputStream();
	}

	public PrintWriter getWriter() throws IOException {
		return _response.getWriter();
	}

	public boolean isCommitted() {
		return _response.isCommitted();
	}

	public void reset() {
		_response.reset();
	}

	public void resetBuffer() {
		_response.resetBuffer();
	}

	public void sendError(int arg0, String arg1) throws IOException {
		_response.sendError(arg0, arg1);
	}

	public void sendError(int arg0) throws IOException {
		_response.sendError(arg0);
	}

	public void sendRedirect(String arg0) throws IOException {
		_response.sendRedirect(arg0);
	}

	public void setBufferSize(int arg0) {
		_response.setBufferSize(arg0);
	}

	public void setCharacterEncoding(String arg0) {
		_response.setCharacterEncoding(arg0);
	}

	public void setContentLength(int arg0) {
		_response.setContentLength(arg0);
	}

	public void setContentType(String arg0) {
		_response.setContentType(arg0);
	}

	public void setDateHeader(String arg0, long arg1) {
		_response.setDateHeader(arg0, arg1);
	}

	public void setHeader(String arg0, String arg1) {
		_response.setHeader(arg0, arg1);
	}

	public void setIntHeader(String arg0, int arg1) {
		_response.setIntHeader(arg0, arg1);
	}

	public void setLocale(Locale arg0) {
		_response.setLocale(arg0);
	}

	/**
	 * @deprecated
	 */
	public void setStatus(int arg0, String arg1) {
		_response.setStatus(arg0, arg1);
	}

	public void setStatus(int arg0) {
		_response.setStatus(arg0);
	}
	
	private class VitroURL {
		// this is to get away from some of the 
		// annoyingness of java.net.URL
		// and to handle general weirdness
		
		private String characterEncoding;
		
		public String protocol;
		public String host;
		public String port;
		public List<String> pathParts;
		public List<String[]> queryParams;
		public String fragment;
		
		private Pattern commaPattern = Pattern.compile("/");
		private Pattern equalsSignPattern = Pattern.compile("=");
		private Pattern ampersandPattern = Pattern.compile("&");
		private Pattern questionMarkPattern = Pattern.compile("\\?");
		public  boolean pathBeginsWithSlash = false;
		public  boolean pathEndsInSlash = false;
		public  boolean wasXMLEscaped = false;
		
		public VitroURL(String urlStr, String characterEncoding) {
			this.characterEncoding = characterEncoding;
			if (urlStr.indexOf("&amp;")>-1) {
				wasXMLEscaped = true;
				urlStr = StringEscapeUtils.unescapeXml(urlStr);
			}
			try {
				URL url = new URL(urlStr);
				this.protocol = url.getProtocol();
				this.host = url.getHost();
				this.port = Integer.toString(url.getPort());
				this.pathParts = splitPath(url.getPath());
				this.pathBeginsWithSlash = beginsWithSlash(url.getPath());
				this.pathEndsInSlash = endsInSlash(url.getPath());
				this.queryParams = parseQueryParams(url.getQuery());
				this.fragment = url.getRef();
			} catch (Exception e) { 
				// Under normal circumstances, this is because the urlStr is relative
				// We'll assume that we just have a path and possibly a query string.
				// This is likely to be a bad assumption, but let's roll with it.
				String[] urlParts = questionMarkPattern.split(urlStr);
				try {
					this.pathParts = splitPath(URLDecoder.decode(urlParts[0],characterEncoding));
					this.pathBeginsWithSlash = beginsWithSlash(urlParts[0]);
					this.pathEndsInSlash = endsInSlash(urlParts[0]);
					if (urlParts.length>1) {
						this.queryParams = parseQueryParams(URLDecoder.decode(urlParts[1],characterEncoding));
					}
				} catch (UnsupportedEncodingException uee) {
					log.error("Unable to use character encoding "+characterEncoding, uee);
				}
			}
		}
		
		private List<String> splitPath(String pathStr) {
			String[] splitStr = commaPattern.split(pathStr);
			if (splitStr.length>0) {
				int len = splitStr.length;
				if (splitStr[0].equals("")) {
					len--;
				}
				if (splitStr[splitStr.length-1].equals("")) {
					len--;
				}
				if (len>0) {
					String[] temp = new String[len];
					int tempI = 0;
					for (int i=0; i<splitStr.length; i++) {
						if (!splitStr[i].equals("")) {
							temp[tempI] = splitStr[i];
							tempI++;
						}
					}
					splitStr = temp;
				}
			}
			// TODO: rewrite the chunk above with lists in mind. 
			List<String> strList = new ArrayList<String>();
			for (int i=0; i<splitStr.length; i++) {
				strList.add(splitStr[i]);
			}
			return strList;
		}	
		
		public boolean beginsWithSlash(String pathStr) {
			if (pathStr.length() == 0) {
				return false;
			}
			return (pathStr.charAt(0) == '/');
		}
		
		public boolean endsInSlash(String pathStr) {
			if (pathStr.length() == 0) {
				return false;
			}
			return (pathStr.charAt(pathStr.length()-1) == '/');
		}
		
		private List<String[]> parseQueryParams(String queryStr) {
			List<String[]> queryParamList = new ArrayList<String[]>();
			if (queryStr == null) {
				return queryParamList;
			}
			String[] keyValuePairs = ampersandPattern.split(queryStr);
			for (int i=0; i<keyValuePairs.length; i++) {
				String[] pairParts = equalsSignPattern.split(keyValuePairs[i]);
				queryParamList.add(pairParts);
			}
			return queryParamList;
		}
		
		public String toString() {
			StringBuffer out = new StringBuffer();
				try {
				if (this.protocol != null) {
					out.append(this.protocol);
				}
				if (this.host != null) {
					out.append(this.host);
				}
				if (this.port != null) {
					out.append(":").append(this.port);
				}
				if (this.pathParts != null) {
					if (this.pathBeginsWithSlash) {
						out.append("/");
					}
					Iterator<String> pathIt = pathParts.iterator();
					while(pathIt.hasNext()) {
						String part = pathIt.next();
						out.append(part);
						if (pathIt.hasNext()) {
							out.append("/");
						}
					}
					if (this.pathEndsInSlash) {
						out.append("/");
					}
				}
				if (this.queryParams != null) {
					Iterator<String[]> qpIt = queryParams.iterator();
					if (qpIt.hasNext()) {
						out.append("?");
					}
					while (qpIt.hasNext()) {
						String[] keyAndValue = qpIt.next();
						out.append(URLEncoder.encode(keyAndValue[0],characterEncoding)).append("=");
						if (keyAndValue.length>1) {
							out.append(URLEncoder.encode(keyAndValue[1],characterEncoding));
						}
						if (qpIt.hasNext()) { 
							out.append("&");
						}
					}
				}
			} catch (UnsupportedEncodingException uee) {
				log.error("Unable to use encoding "+characterEncoding, uee);
			}
			String str = out.toString();
			if (this.wasXMLEscaped) {
				str = StringEscapeUtils.escapeXml(str);
			}
			return str;
		}
		
	}
	
	
}
