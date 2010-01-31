/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.tidy.Node;
import org.w3c.tidy.Tidy;

public class MakeTidy {
    private static final Log log = LogFactory.getLog(MakeTidy.class.getName());
    private static PrintWriter outFile = null;
    
    public MakeTidy() {
        try {
            outFile = new PrintWriter( new FileWriter( "tidyErrorOutput.log"));
            System.out.println("logging errors to tidy.error.output in Tomcat logs directory\n");
        } catch (IOException ex) {
            log.error("cannot open Tidy error output file");
        }
    }

	public String process (String value) {
	    Tidy tidy = new Tidy(); // obtain a new Tidy instance
	    
	    // set desired config options using tidy setters: see http://jtidy.sourceforge.net/apidocs/index.html
	    tidy.setAsciiChars(true);                // convert quotes and dashes to nearest ASCII character
	    tidy.setDropEmptyParas(true);            // discard empty p elements
        tidy.setDropFontTags(true);              // discard presentation tags
	    tidy.setDropProprietaryAttributes(true); // discard proprietary attributes
        tidy.setForceOutput(true);               // output document even if errors were found
	    tidy.setLogicalEmphasis(true);           // replace i by em and b by strong
	    tidy.setMakeBare(true);                  // remove Microsoft cruft
	    tidy.setMakeClean(true);                 // remove presentational clutter
        tidy.setPrintBodyOnly(true);             // output BODY content only
        tidy.setShowWarnings(true);              // show warnings
        tidy.setTidyMark(true);                  // add meta element indicating tidied doc
        tidy.setTrimEmptyElements(true);         // trim empty elements
	    tidy.setWord2000(true);                  // draconian cleaning for Word 2000
        tidy.setXHTML(true);                     // output extensible HTML
	    
        if (outFile != null /* && (log.isDebugEnabled() */) {
            tidy.setErrout(outFile);
            tidy.setShowErrors(Integer.MAX_VALUE);
            outFile.println("\nInput:\n"+value+"\n");
        }
        
	    StringWriter sw = new StringWriter();
      /*Node rootNode = */tidy. parse(new StringReader(value),sw);
        String outputStr = sw.toString();
        if (outFile != null /*&& log.isDebugEnabled()*/) {
            outFile.println("\nTidied Output:\n"+outputStr+"\n");
        }
        outFile.flush();
        return outputStr;        
	}
}
