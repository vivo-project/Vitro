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
    
    private Pattern commaPattern = Pattern.compile("/");
    private Pattern equalsSignPattern = Pattern.compile("=");
    private Pattern ampersandPattern = Pattern.compile("&");
    private Pattern questionMarkPattern = Pattern.compile("\\?");
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
