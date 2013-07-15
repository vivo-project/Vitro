/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

/**
 * Class that performs the update of the uris in the search index
 * for the SearchService.
 */

public class UpdateUrisInIndex {
    private static final Log log = LogFactory.getLog(UpdateUrisInIndex.class);
    
    /**
     * Web service for update in search index of a list of URIs. 
     * @throws IOException 
     */
    protected void doUpdateUris(HttpServletRequest req, IndexBuilder builder)
    throws ServletException, IOException{
        

        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if( ! isMultipart ) 
            throw new ServletException("Expected Multipart Content");
        
 
        String charEncoding = getEncoding(req);
        try{
            int count = 0;
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iter = upload.getItemIterator(req);
            while( iter.hasNext()){
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                try{
                    count = count + addToSearchQueue(builder, new InputStreamReader(stream, charEncoding));
                }finally{
                    stream.close();
                    builder.doUpdateIndex();
                }
            }
        }catch (FileUploadException fex){
            throw new ServletException("Could not upload file to SearchServiceController", fex);
        }
    }

    private String getEncoding(HttpServletRequest req){
        String enc = req.getCharacterEncoding();
        if( enc == null || enc.isEmpty() ){
            log.debug("No encoding on POST request, That is acceptable.");
            enc = "UTF-8";
        }else if( enc.length() > 30){
            log.debug("Ignoring odd encoding of '" + enc + "'");
            enc = "UTF-8";
        }else{
            log.debug("Encoding set on POST request: " + enc);
        }
        log.debug("Reading POSTed URIs with encoding " + enc);
        return enc;
    }

    private int addToSearchQueue( IndexBuilder builder, Reader in  )
        throws IOException{
        int addedUriCount = 0;

        Iterator<String> uris = new UrisFromInputIterator( in );
        while(uris.hasNext()){
            String uri = uris.next();
            log.debug("Request to index uri '" + uri + "'");
            builder.addToChanged( uri );
            addedUriCount++;
        }

        return addedUriCount;
    }


    public static class UrisFromInputIterator implements Iterator<String> {
        BufferedReader reader;
        Iterator<String> uris;

        public UrisFromInputIterator(Reader in ){
            this.reader = new BufferedReader(in);
        }

        public void remove(){ throw new UnsupportedOperationException() ; }
        
        public boolean hasNext(){
            if( uris != null && uris.hasNext() ){
                return true;
            }else{
                try {
                    return getFromBuffer();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return false;
                }
            }
        }        
        
        public String next(){ 
            return uris.next(); 
        }
        
        /** Returns true if there are uris to get. 
         * @throws IOException */
        private boolean getFromBuffer() throws IOException{         
            uris = null;
            
            while( uris == null || !uris.hasNext() ){
                String chunk = reader.readLine();
                if( chunk == null ){ //at end of input
                    break;
                } else if( chunk.trim().isEmpty() ){
                    continue;
                }else{
                    uris = lineToUris(chunk).iterator();
                    if( uris.hasNext() ){
                        return true;
                    }
                }
            }
            return false;            
        }
    }
    
  
    
    private static List<String> removeNullAndEmpty(List<String> in ){
        ArrayList<String> out = new ArrayList<String>();
        for( String s : in ){
            if( s != null && !s.trim().isEmpty() ){
                out.add(s);
            }
        }
        return out;        
    }

    protected static List<String> lineToUris(String line){
        List<String> parts = removeNullAndEmpty( Arrays.asList(commaAndWhitespace.split( line ) ));
        return parts;
    }
    
    //split uris on whitespace and commas
    private static final Pattern commaAndWhitespace = Pattern.compile("[,\\s]");

}
