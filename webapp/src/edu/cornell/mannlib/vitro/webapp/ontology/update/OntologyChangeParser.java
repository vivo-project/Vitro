/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.skife.csv.CSVReader;
import org.skife.csv.SimpleReader;

import edu.cornell.mannlib.vitro.webapp.ontology.update.AtomicOntologyChange.AtomicChangeType;

/**
 * Performs parsing on Prompt output and provides change object list.
 * 
 * @author ass92
 *
 */

public class OntologyChangeParser {

	private ChangeLogger logger;
	
	public OntologyChangeParser(ChangeLogger logger) {
		this.logger = logger;
	}
		
	/**
	 * @param args
	 * @throws IOException 
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
		CSVReader readFile = new SimpleReader();
		readFile.setSeperator('\t');
		
		List<String[]> rows = readFile.parse(in);
		
		for(int rowNum = 0; rowNum < rows.size(); rowNum++){
			
			String[] cols = rows.get(rowNum);
			if (cols.length != 5) {
				logger.logError("Invalid PromptDiff data at row " + (rowNum + 1) 
					   + ". Expected 5 columns; found " + cols.length );
			} else {
		
				changeObj = new AtomicOntologyChange();
				
				if (cols[0] != null && cols[0].length() > 0) {
					changeObj.setSourceURI(cols[0]);
				}
				
				if (cols[1] != null && cols[1].length() > 0) {
					changeObj.setDestinationURI(cols[1]);
				}

				if (cols[4] != null && cols[4].length() > 0) {
                  changeObj.setNotes(cols[4]);
                }

				if ("Yes".equals(cols[2])) {
					changeObj.setAtomicChangeType(AtomicChangeType.RENAME);
				} else if ("Delete".equals(cols[3])) {
					changeObj.setAtomicChangeType(AtomicChangeType.DELETE); 
				} else if ("Add".equals(cols[3])) {
					changeObj.setAtomicChangeType(AtomicChangeType.ADD);
				} else {
					logger.logError("Invalid rename or change type data: '" +
							cols[2] + " " + cols[3] + "'");
				}
				
				
				changeObjects.add(changeObj);
					
			}
			
		}
		
		if (changeObjects.size() == 0) {
			logger.log("did not find any changes in PromptDiff output file.");
		}
		return changeObjects;
	}

}
