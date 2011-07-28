/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;

/**
 * Builds the N3 strings for the given EditConfiguration, model
 * and EditSubmission.   Main responsibility is the proper substitution
 * of URI and literal strings in to the template N3.
 *
 * 
 */
public class EditN3Generator {
    EditConfiguration editConfig;
    static final Log log = LogFactory.getLog( EditN3Generator.class );

    public EditN3Generator( EditConfiguration editConfig ){
        this.editConfig = editConfig;
    }

    public List<String> generateN3(EditSubmission editSub, Model model){
        return Collections.EMPTY_LIST;
    }

  
    
    public static List<String> subInUris(Map<String,String> varsToVals, List<String> targets){
        if( varsToVals == null || varsToVals.isEmpty() ) return targets;
        ArrayList<String> outv = new ArrayList<String>();
        for( String target : targets){
            String temp = target;
            for( String key : varsToVals.keySet()) {
                temp = subInUris( key, varsToVals.get(key), temp)  ;
            }
            outv.add(temp);
        }
        return outv;
    }



    public static String subInUris(String var, String value, String target){
        //empty URIs get skipped
        if( var == null || var.length() == 0 || value==null  )
            return target;
        /* var followed by dot some whitespace or var followed by whitespace*/
        String varRegex = "\\?" + var + "(?=\\.\\p{Space}|\\p{Space})";
        String out = null;
        if("".equals(value))
        	out = target.replaceAll(varRegex,">::" + var + " was BLANK::< ");
        else
        	out = target.replaceAll(varRegex,"<"+Matcher.quoteReplacement(value)+"> ");
        if( out != null && out.length() > 0 )
            return out;
        else
            return target;
    }

    public static List<String>subInUris(String var, String value, List<String> targets){
        ArrayList<String> outv =new ArrayList<String>();
        for( String target : targets){
            outv.add( subInUris( var,value, target) ) ;
        }
        return outv;
    }
    
    public  List<String> subInLiterals(Map<String, Literal> varsToVals, List<String> targets){
        if( varsToVals == null || varsToVals.isEmpty()) return targets;

        ArrayList<String> outv =new ArrayList<String>();
        for( String target : targets){
            String temp = target;
            for( String key : varsToVals.keySet()) {
                temp = subInLiterals( key, varsToVals.get(key), temp);
            }
            outv.add(temp);
        }
        return outv;
    }

//    public  List<String>subInLiterals(String var, String value, List<String> targets){
//        ArrayList<String> outv =new ArrayList<String>();
//        for( String target : targets){
//            outv.add( subInLiterals( var,value, target) ) ;
//        }
//        return outv;
//    }

    /**
     * When we sub in literals we have to take in to account the Lang or Datatype of
     * the literal.  N3 needs to have its literals escaped in Python style.  Java regex
     * Matcher is used to do the substitution and it need escaping to avoid group
     * references, Matcher.quoteReplacement() serves the purpose.
     *
     */
    public  String subInLiterals(String var, Literal literal, String target){
        String varRegex = "\\?" + var + "(?=\\.\\p{Space}|\\p{Space})";
        if (target==null ) {
            log.error("subInLiterals was passed a null target");
            return "blankBecauseTargetOrValueWasNull";
        }else if( var == null ){
            log.warn("subInLiterals was passed a null var name");
            return target;
        }else if( literal == null ){
            log.debug("subInLiterals was passed a null value for var '"+var+"'; returning target: '"+target+"'");
            return target;
        }
                
        try{
        if( literal.getValue() == null )
            log.debug("value of literal for " + var + " was null");        
        }catch(com.hp.hpl.jena.datatypes.DatatypeFormatException ex){        	
        	log.debug("value for " + var + " " + ex.getMessage());
        }
        
        //if( editConfig != null && editConfig.getFields() != null &&
        //    editConfig.getFields().get(var) != null ){
            //The var might not be in the editConfig.fields if an EditN3Generator
            //is being used to substitute in values that are not on the form, 
            //eg ?fileSize for file uploads
        
        String replacement = null;                       
        if ( literal.getLexicalForm().length()==0 ) {
            log.debug("empty string found on form for " + var + ".");
            replacement = ">::" + var + " was empty::<";
        }else{
            replacement = formatLiteral(literal);
        }
        
        String out = null;
        if( replacement != null )
            out = target.replaceAll(varRegex, Matcher.quoteReplacement( replacement ));
        else
            out = target;
        
        if( out != null && out.length() > 0 ) 
            return out;
        else{
            log.debug("After attempting to substitue in literals, the target N3 was empty" );
            return target;
        }
    }

     public Map<String,List<String>> substituteIntoValues
             (Map<String,String> varsToUris,
              Map<String,Literal> varsToLiterals,
              Map<String,List<String>> namesToN3 )
     {
        Map<String,List<String>> outHash = new HashMap<String,List<String>>();

        if (namesToN3==null) {
            return outHash;
        } else if (namesToN3.isEmpty()) {
            return outHash;
        } else {
            for(String fieldName : namesToN3.keySet()){
                List<String> n3strings = namesToN3.get(fieldName);
                List<String> newList  = new ArrayList<String>();
                if( varsToUris != null)
                    newList = subInUris(varsToUris, n3strings);
                if( varsToLiterals != null)
                    newList = subInLiterals(varsToLiterals, newList);
                outHash.put(fieldName, newList);
            }
        }
        return outHash;
    }

    protected String quoteForN3(String in){
        //TODO: THIS  NEEDS TO BE ESCAPED FOR N3 which is python string escaping
        return in;
    }
    
    
    /*
     * bdc34 2008-07-33
     *
     * The following methods are from 
     * HP's Jena project ver 2.5.5   
     * Found in file Jena-2.5.5/src/com/hp/hpl/jena/n3/N3JenaWriterCommon.java 
     *
     * The following copyright statement applies to these methods.
     */
    
    /*
     *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
     *  All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions
     * are met:
     * 1. Redistributions of source code must retain the above copyright
     *    notice, this list of conditions and the following disclaimer.
     * 2. Redistributions in binary form must reproduce the above copyright
     *    notice, this list of conditions and the following disclaimer in the
     *    documentation and/or other materials provided with the distribution.
     * 3. The name of the author may not be used to endorse or promote products
     *    derived from this software without specific prior written permission.
     *
     * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */
    
    protected String formatLiteral(Literal literal)
    {
        String datatype = literal.getDatatypeURI() ;
        String lang = literal.getLanguage() ;
        String s = literal.getLexicalForm() ;
    
        if ( datatype != null )
        {
            // Special form we know how to handle?
            // Assume valid text
            if ( datatype.equals(XSD.integer.getURI()) )
            {
                try {
                    new java.math.BigInteger(s) ;
                    return s ;
                } catch (NumberFormatException nfe) {}
                // No luck.  Continue.
                // Continuing is always safe.
            }
                
            if ( datatype.equals(XSD.decimal.getURI()) )
            {
                // Must have ., can't have e or E
                if ( s.indexOf('.') >= 0 &&
                     s.indexOf('e') == -1 && s.indexOf('E') == -1 )
                {
                    // See if parsable.
                    try {
                        BigDecimal d = new BigDecimal(s) ;
                        return s ;
                    } catch (NumberFormatException nfe) {}
                }
            }
            
            if ( datatype.equals(XSD.xdouble.getURI()) )
            {
                // Must have 'e' or 'E' (N3 and Turtle now read 2.3 as a decimal).
                if ( s.indexOf('e') >= 0 ||
                     s.indexOf('E') >= 0 )
                {
                    try {
                        // Validate it.
                        Double.parseDouble(s) ;
                        return s ;
                    } catch (NumberFormatException nfe) {}
                    // No luck.  Continue.
                }
            }
        }
        // Format the text - with escaping.
        StringBuffer sbuff = new StringBuffer() ;
        
        String quoteMarks = "\"" ;
        
        sbuff.append(quoteMarks);
        pyString(sbuff, s ) ;
        sbuff.append(quoteMarks);
    
        // Format the language tag 
        if ( lang != null && lang.length()>0)
        {
            sbuff.append("@") ;
            sbuff.append(lang) ;
        }
        
        // Format the datatype
        if ( datatype != null )
        {
            sbuff.append("^^") ;
            sbuff.append(formatURI(datatype)) ;
        }
        return sbuff.toString() ;
    }

    
    /*
     * 
     * see http://www.python.org/doc/2.5.2/ref/strings.html
     * or see jena's n3 grammar jena/src/com/hp/hpl/jena/n3/n3.g
     */ 
    protected static void pyString(StringBuffer sbuff, String s)
    {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // Escape escapes and quotes
            if (c == '\\' || c == '"' )
            {
                sbuff.append('\\') ;
                sbuff.append(c) ;
                continue ;
            }            

            // Whitespace                        
            if (c == '\n'){ sbuff.append("\\n");continue; }
            if (c == '\t'){ sbuff.append("\\t");continue; }
            if (c == '\r'){ sbuff.append("\\r");continue; }
            if (c == '\f'){ sbuff.append("\\f");continue; }                            
            if (c == '\b'){ sbuff.append("\\b");continue; }
            if( c == 7 )  { sbuff.append("\\a");continue; }
            
            // Output as is (subject to UTF-8 encoding on output that is)
            sbuff.append(c) ;
            
//            // Unicode escapes
//            // c < 32, c >= 127, not whitespace or other specials
//            String hexstr = Integer.toHexString(c).toUpperCase();
//            int pad = 4 - hexstr.length();
//            sbuff.append("\\u");
//            for (; pad > 0; pad--)
//                sbuff.append("0");
//            sbuff.append(hexstr);
        }
    }

    protected String formatURI(String uriStr)
    {
        // Not as a qname - write as a quoted URIref
        // Should we unicode escape here?
        // It should be right - the writer should be UTF-8 on output.
        return "<"+uriStr+">" ;
    }
    
    /*************************************************************************
     * End code taken from the Jena project and Hewlett-Packard 
     *************************************************************************/
}
