/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.jga.fn.UnaryFunctor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * Interface for classes that highlight search results.
 * All method must gracefully deal with null inputs.
 *
 * @author bdc34
 *
 * @ deprecated there is no replacement 
 */
public abstract class VitroHighlighter extends UnaryFunctor<String,String> {
	
	private static final Log log = LogFactory.getLog(VitroHighlighter.class.getName());
	
    public static String preTag = "<span class='SearchTerm'>";
    public static String postTag = "</span>";

    public abstract String highlight(String in  );
    public abstract String getHighlightFragments(String in );

    public String fn(String in){
        if( in == null || "".equals(in))
            return "";
        try{ 
            return highlight( in );
        }catch(Throwable th){
            return in;
        }       
    }
    
    public void highlight(Individual ent){
        if( ent == null)
            return;

        ent.setBlurb( this.highlight(ent.getBlurb()));
        ent.setName( this.highlight(ent.getName()));
        ent.setMoniker( this.highlight(ent.getMoniker()));
        ent.setDescription( this.highlight(ent.getDescription()));
        Iterator edIt = ent.getDataPropertyStatements().iterator();
        while (edIt.hasNext()) {
            DataPropertyStatement dataPropertyStmt = (DataPropertyStatement)edIt.next();
            dataPropertyStmt.setData(this.highlight(dataPropertyStmt.getData()));
        }
        edIt = ent.getObjectPropertyStatements().iterator();
        /* works only if JSP doesn't escape the text
        while (edIt.hasNext()) {
            ObjectPropertyStatement objectPropertyStmt = (ObjectPropertyStatement)edIt.next();
            Individual object = objectPropertyStmt.getObject();
            object.setName(this.highlight(object.getName()));
        }
        */
        highlightKeywords(ent);
    }

    /**
     * Highlights the name and then replaces the description with
     * highlighted fragments.
     * @param ent
     */
    public void fragmentHighlight(Individual ent){
        if( ent == null )
            return;

        //highlight the name, anchor and moniker and place back in entity object
        if( ent.getName() != null )
            ent.setName(  highlight( stripHtml(ent.getName() )));
        if( ent.getAnchor() != null )
            ent.setAnchor(  highlight( stripHtml(ent.getAnchor() )));
        if( ent.getMoniker() != null )
            ent.setMoniker( highlight( stripHtml(ent.getMoniker() )));

        //make a buffer of text to use the fragmenting hightlighter on
        StringBuffer sb = new StringBuffer("");
        if(ent.getBlurb() != null){
            sb.append(ent.getBlurb());
            sb.append(' ');
        }
        if(ent.getDescription() != null ){
            sb.append(ent.getDescription());
            sb.append(' ');
        }
        if(ent.getDataPropertyStatements() != null) {
            Iterator edIt = ent.getDataPropertyStatements().iterator();
            while (edIt.hasNext()) {
                sb.append(((DataPropertyStatement)edIt.next()).getData());
                sb.append(' ');
            }
        }
        if(ent.getObjectPropertyStatements() != null) {
            Iterator edIt = ent.getObjectPropertyStatements().iterator();
            while (edIt.hasNext()) {
            	String t;
            	try {
	            	ObjectPropertyStatement stmt = (ObjectPropertyStatement) edIt.next();
	                sb.append( ( (t = stmt.getProperty().getDomainPublic()) != null) ? t : "" );
	                sb.append(' ');
	                sb.append( ( (t = stmt.getObject().getName()) != null) ? t : "" );
	                sb.append(' ');
            	} catch (Exception e) {
            		log.info("Error highlighting object property statement for individual "+ent.getURI());
            	}
            }
        }
        String keywords = ent.getKeywordString();
        if( keywords != null )
            sb.append(keywords);

        ent.setDescription(getHighlightFragments(  stripHtml( sb.toString() )));
    }

    private void highlightKeywords(Individual ent){
        List<String> terms = ent.getKeywords();
        if( terms == null || terms.size() == 0) return;

        List<String> replacement = new ArrayList<String>(terms.size());
        for(String term : ent.getKeywords()){
            replacement.add( highlight(term ) );
        }
        ent.setKeywords(replacement);
    }

    private final String stripHtml(String in){
        /* make a string with html stripped out */
        // ryounes 5/16/2011 Broken with upgrade to Solr 3.1: HTMLStripReader has been removed.
        // According to change list, should use HTMLStripCharFilter, but it's not immediately clear how
        // to migrate this code. Will enter Jira issue.
//        Reader stripIn = new HTMLStripReader( new StringReader( in ) );
//        StringWriter stripOut = new StringWriter(in.length());
//
//        char bytes[] = new char[5000];
//        int bytesRead = 0;
//        try {
//            //this is a mess, there must be a better way to do this.
//            while ( true  ){
//                bytesRead = stripIn.read( bytes );
//                if( bytesRead == -1 ) break;
//                stripOut.write(bytes, 0, bytesRead  );
//            }
//        } catch (IOException e1) {
//            log.error("LuceneHighlighter.getHighlightFragments()" +
//                 " - unable to strip html" + e1);
//        }
//        return stripOut.toString();
        return in;
    }
}

