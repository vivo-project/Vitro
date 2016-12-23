/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ontology.update.AtomicOntologyChange.AtomicChangeType;

/**
 * Performs parsing on Prompt output and provides change object list.
 * 
 * @author ass92
 *
 */

public class OntologyChangeParser {

    private final Log log = LogFactory.getLog(OntologyChangeParser.class);
    
	private ChangeLogger logger;
	
	public OntologyChangeParser(ChangeLogger logger) {
		this.logger = logger;
	}
		
	/**
	 * @param diffPath Diff path
	 */
	
	@SuppressWarnings({ "unchecked", "null", "static-access" })
	public ArrayList<AtomicOntologyChange> parseFile(String diffPath) throws IOException{
		
		AtomicOntologyChange changeObj;
		ArrayList<AtomicOntologyChange> changeObjects = new ArrayList<AtomicOntologyChange>();
		int countColumns = 0;
		String URI = null;
		String rename = null;
		String sourceURI = null;
		String destinationURI = null;
		StringTokenizer stArr = null; 
		FileInputStream in = new FileInputStream(new File(diffPath));

		CSVParser readFile = new CSVParser(new InputStreamReader(in),
				CSVFormat.DEFAULT.withRecordSeparator('\t'));

		int rowNum = 0;
		for (CSVRecord record : readFile) {
			rowNum++;
			if (record.size() != 5) {
				logger.logError("Invalid PromptDiff data at row " + (rowNum)
					   + ". Expected 5 columns; found " + record.size() );
			} else {
				String col = null;
				changeObj = new AtomicOntologyChange();

				col = record.get(0);
				if (col != null && col.length() > 0) {
					changeObj.setSourceURI(col);
				}

				col = record.get(1);
				if (col != null && col.length() > 0) {
					changeObj.setDestinationURI(col);
				}

				col = record.get(4);
				if (col != null && col.length() > 0) {
                  changeObj.setNotes(col);
                }

				if ("Yes".equals(record.get(2))) {
					changeObj.setAtomicChangeType(AtomicChangeType.RENAME);
				} else if ("Delete".equals(record.get(3))) {
					changeObj.setAtomicChangeType(AtomicChangeType.DELETE); 
				} else if ("Add".equals(record.get(3))) {
					changeObj.setAtomicChangeType(AtomicChangeType.ADD);
				} else {
					logger.logError("Invalid rename or change type data: '" +
							record.get(2) + " " + record.get(3) + "'");
				}

			    log.debug(changeObj);
				
				changeObjects.add(changeObj);
			}
		}

		readFile.close();
		
		if (changeObjects.size() == 0) {
			logger.log("No ABox updates are required.");
		}
		return changeObjects;
	}

}
