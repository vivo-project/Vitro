/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * Brian Caruso with changes contributed by David Cliff, 2010-11-03
 * Before 2010-11 this was a private class of URLRewritingHttpServletResponse.java.
 * 
 * Useful reference:
 * http://labs.apache.org/webarch/uri/rfc/rfc3986.html
 */
class VitroURL {
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
    
    /**
     * Pattern to get the path and query of a relative URL
     * ex.
     *   /entity -> /entity 
     *   /entity?query=abc -> /entity query=abc
     */
    private Pattern pathPattern = Pattern.compile("([^\\?]*)\\??(.*)");
    
    private Pattern commaPattern = Pattern.compile("/");
    private Pattern equalsSignPattern = Pattern.compile("=");
    private Pattern ampersandPattern = Pattern.compile("&");          
    public  boolean pathBeginsWithSlash = false;
    public  boolean pathEndsInSlash = false;
    public  boolean wasXMLEscaped = false;
    
    private final static Log log = LogFactory.getLog(VitroURL.class);
    
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
            Matcher m = pathPattern.matcher(urlStr);
            String[] urlParts = new String[2];
            if( m.matches() ){
                urlParts[0]= m.group(1);
                if( m.groupCount() == 2 )
                    urlParts[1] = m.group(2);
            }else{
                //???
            }
                         
            try {
                this.pathParts = splitPath(URLDecoder.decode(getPath(urlStr),characterEncoding));
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
    
    private String getPath(String urlStr){
        Matcher m = pathPattern.matcher(urlStr);
        if( m.matches() )
            return m.group(1);
        else
            return "";
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
    
  
    /**
     * This is attempting to parse query parameters that might not be URLEncoded.
     * This seems like a huge problem.  We will only correctly handle odd things 
     * as a query parameter 'uri' in the last position.
     *  
     * @param queryStr
     * @return
     */    
    protected List<String[]> parseQueryParams(String queryStr) {
        List<String[]> queryParamList = new ArrayList<String[]>();
        if (queryStr == null) {
            return queryParamList;
        }
      
        while ( queryStr.length() > 0 ){
            //remove leading & if there was one
            if( queryStr.startsWith("&"))
                queryStr = queryStr.substring(1);
            
            String[] simplepair = getSimpleQueryMatch(queryStr);
            if( simplepair != null ){
                if( simplepair[1].contains("?") ){
                    //must be odd final pair
                    String[] finalPair = getFinalPairQueryMatch(queryStr);
                    if( finalPair != null){
                        queryParamList.add(finalPair);
                        queryStr="";
                    }else{
                        throw new Error("Cannot parse query string for URL " +
                        		"queryParams: \"" + queryStr + "\" this only accepts " +
                        	    "complex parameters in the final position with the key 'uri'."); 
                    }
                }else{
                    queryParamList.add(simplepair);
                    // remove found simple key vaule pair from query str
                    queryStr = queryStr.substring(
                        simplepair[0].length()+simplepair[1].length()+1);
                }
            }else{
                //maybe there is an odd final pair
                String[] finalPair = getFinalPairQueryMatch(queryStr);
                if( finalPair != null){
                    queryParamList.add(finalPair);
                    queryStr="";
                }
            }
        }
        return queryParamList;
    }
    
    /** Query for simple query param at start of string. */
    private Pattern simpleQueryParamPattern = Pattern.compile("^([^\\=]*)=([^\\&]*)");

    /**
     * Check for a simple match in a queryParam.
     * May return null.     
     */
    protected String[] getSimpleQueryMatch(String querystr){
        Matcher simpleMatch = simpleQueryParamPattern.matcher(querystr);
        if( simpleMatch.find() ){
            String[] rv = new String[2];
            rv[0]=simpleMatch.group(1);
            rv[1]=simpleMatch.group(2);
            return rv;
        }else{
            return null;
        }
    }
    
    private Pattern finalQueryParamPattern = Pattern.compile("^(uri)=(.*)");
    /**
     * Checks only for uri=.* as the last match of the queryParams.
     * May return null.
     */
    protected String[] getFinalPairQueryMatch(String querystr){
        Matcher finalMatch = finalQueryParamPattern.matcher(querystr);
        if( finalMatch.find() ){
            String[] rv = new String[2];
            rv[0]=finalMatch.group(1);
            rv[1]=finalMatch.group(2);
            return rv;
        }else{
            return null;
        }
        
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
