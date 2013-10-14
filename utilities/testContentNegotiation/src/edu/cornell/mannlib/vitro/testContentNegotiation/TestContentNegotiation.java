/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.testContentNegotiation;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




import org.apache.http.NameValuePair;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 * Utility to test content negotiation of some features
 * of a running vivo instance.
 * 
 * usage:
 * java -cp $YOUR_CP edu.cornell.mannlib.vitro.testContentNegotiation VIVOURL email password
 *
 * This will return 0 and print "testContentNegotiation: Content negotiation tests passed.\n"
 * on success.
 * It will return 1 and print errors to STDOUT on failure.
 */
public class TestContentNegotiation{
    
    static CNTest[] tests = {
        //URL, QUERY, Accept header, pattern for response Content-Type, pattern for body, description
      new CNTest(constants.servlet, constants.query, "none", ".*", ".*","Test to see if a basic request works")       
    };    


    public static void main(String[] argv){
         
        if( argv.length != 3 ){
            doHelp();
            System.exit(2); 
        }

        String baseUrl = argv[0];
        String email = argv[1];
        String pw = argv[2];
        
        List<String> errors = new ArrayList<String>();
        
        for( CNTest test : tests ){
            String msg = test.doTest( baseUrl, email, pw);
            if( msg != null )
                errors.add( msg );
        }

        if( errors.isEmpty() ){
            System.out.println("testContentNegotiation: Content negotiation tests passed.\n");
            System.exit(0);
        }else{
            doErrors( errors );
            System.exit(1);
        }        
    }       
    
    private static void doErrors( List<String> errors){
        System.out.println("ERROR testContentNegotiation: There were " + errors.size() + " errors.");
        for( String err : errors){
            System.out.println( err );
        }
    }

    private static void doHelp(){
        System.out.println(
          "Utility to test content negotiation of some features \n" +
          "of a running vivo instance. \n" +
          "usage: \n" +
          "java -cp $YOUR_CP edu.cornell.mannlib.vitro.testContentNegotiation VIVOURL email password\n");
    }
    
    protected static class CNTest{
        String request;
        String acceptHeader;
        String query;
        Pattern expectedResponseContentType;
        Pattern expectedBody;
        String description;

        public CNTest(String request, String query, String acceptHeader, 
                String expectedResponseContentType, String expectedBody,
                String description){
            this.request = request;
            this.acceptHeader = acceptHeader;
            this.query = query;
            this.expectedResponseContentType = Pattern.compile( expectedResponseContentType );
            this.expectedBody = Pattern.compile( expectedBody );
            this.description = description;
        }

        /**
         * Returns a non-null string on failure.
         * Returns null on success.
         */
        String doTest(String baseUrl, String email, String pw) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost( baseUrl +  request );
            httpPost.addHeader("Accept", acceptHeader);
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();            
            nvps.add(new BasicNameValuePair("email", "vip"));
            nvps.add(new BasicNameValuePair("password", "secret"));
            nvps.add(new BasicNameValuePair("query", query));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            HttpResponse resp;
            try {
                resp = httpClient.execute(httpPost);
            } catch (Exception e){
                return "Failed, exception, " + e.getMessage() ;
            }

            //Check response content type
            try{
                Header[] contentType = resp.getHeaders("Content-Type");
                if( contentType == null || contentType.length == 0 ){
                    return "Failed, Content-Type was empty be expected " + expectedResponseContentType;
                }
                
                boolean foundMatch = false;
                for( Header header : contentType){
                    Matcher m = expectedResponseContentType.matcher( header.getValue() );
                    if( m.matches() ){
                        foundMatch = true;
                        break;
                    }
                }
                
                if( !foundMatch ){
                    return "Failed to match expected Content-Type " + 
                            expectedResponseContentType.toString() + 
                            " to value " + resp.getHeaders("Content-Type") ;
                }
            }finally{
                httpPost.releaseConnection();
            }
            
            //TODO: Check body 
            return null;
        }
    }    
    
    
    protected static class constants{
        
        static String servlet = "/admin/sparqlquery";

        static String query = 
                "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX owl:   <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX swrl:  <http://www.w3.org/2003/11/swrl#>\n" +
                "PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>\n" +
                "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n" +
                "PREFIX p.1: <http://purl.obolibrary.org/obo/arg/agent.owl#>\n" +
                "PREFIX p.2: <http://purl.obolibrary.org/obo/arg/bfo-bridge.owl#>\n" +
                "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
                "PREFIX c4o: <http://purl.org/spar/c4o/>\n" +
                "PREFIX p.3: <http://purl.obolibrary.org/obo/arg/clinical.owl#>\n" +
                "PREFIX p.4: <http://purl.obolibrary.org/obo/arg/contact.owl#>\n" +
                "PREFIX p.5: <http://purl.obolibrary.org/obo/arg/contact-vcard.owl#>\n" +
                "PREFIX p.6: <http://purl.obolibrary.org/obo/arg/data-properties.owl#>\n" +
                "PREFIX p.7: <http://purl.obolibrary.org/obo/arg/date-time.owl#>\n" +
                "PREFIX p.8: <http://vivoweb.org/ontology/core/dateTimeValuePrecision.owl#>\n" +
                "PREFIX p.9: <http://vivoweb.org/ontology/core/documentStatus.owl#>\n" +
                "PREFIX dcelem: <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "PREFIX p.10: <http://purl.obolibrary.org/obo/arg/education.owl#>\n" +
                "PREFIX p.11: <http://purl.obolibrary.org/obo/arg/event.owl#>\n" +
                "PREFIX event: <http://purl.org/NET/c4dm/event.owl#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX fabio: <http://purl.org/spar/fabio/>\n" +
                "PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#>\n" +
                "PREFIX p.12: <http://purl.obolibrary.org/obo/arg/geo-political.owl#>\n" +
                "PREFIX p.13: <http://purl.obolibrary.org/obo/arg/grant.owl#>\n" +
                "PREFIX p.14: <http://purl.obolibrary.org/obo/arg/location.owl#>\n" +
                "PREFIX p.15: <http://purl.obolibrary.org/obo/arg/object-properties.owl#>\n" +
                "PREFIX p.16: <http://purl.obolibrary.org/obo/arg/other.owl#>\n" +
                "PREFIX p.17: <http://purl.obolibrary.org/obo/arg/outreach.owl#>\n" +
                "PREFIX p.18: <http://purl.obolibrary.org/obo/arg/process.owl#>\n" +
                "PREFIX pvs: <http://vivoweb.org/ontology/provenance-support#>\n" +
                "PREFIX p.19: <http://purl.obolibrary.org/obo/arg/publication.owl#>\n" +
                "PREFIX p.20: <http://purl.obolibrary.org/obo/arg/relationship.owl#>\n" +
                "PREFIX p.21: <http://purl.obolibrary.org/obo/arg/research.owl#>\n" +
                "PREFIX p.22: <http://purl.obolibrary.org/obo/arg/research-resource.owl#>\n" +
                "PREFIX p.23: <http://purl.obolibrary.org/obo/arg/research-resource-iao.owl#>\n" +
                "PREFIX ero: <http://purl.obolibrary.org/obo/>\n" +
                "PREFIX p.24: <http://purl.obolibrary.org/obo/arg/role.owl#>\n" +
                "PREFIX scires: <http://vivoweb.org/ontology/scientific-research#>\n" +
                "PREFIX p.25: <http://purl.obolibrary.org/obo/arg/service.owl#>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX p.26: <http://purl.obolibrary.org/obo/arg/skos-vivo.owl#>\n" +
                "PREFIX p.27: <http://www.w3.org/2008/05/skos-xl#>\n" +
                "PREFIX p.28: <http://purl.obolibrary.org/obo/arg/teaching.owl#>\n" +
                "PREFIX vitro-public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>\n" +
                "PREFIX p.29: <http://purl.obolibrary.org/obo/arg/app-views/vivo/vivo-app.owl#>\n" +
                "PREFIX vivo: <http://vivoweb.org/ontology/core#>\n" +
                "\n" +
                "#\n" +
                "# This example query gets 20 geographic locations\n" +
                "# and (if available) their labels\n" +
                "#\n" +
                "SELECT ?geoLocation ?label\n" +
                "WHERE\n" +
                "{\n" +
                "      ?geoLocation rdf:type vivo:GeographicLocation\n" +
                "      OPTIONAL { ?geoLocation rdfs:label ?label } \n" +
                "}\n" +
                "LIMIT 20";
        

    }
}
