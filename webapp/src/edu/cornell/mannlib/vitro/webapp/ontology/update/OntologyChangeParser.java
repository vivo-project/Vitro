/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import edu.cornell.mannlib.vitro.webapp.ontology.update.AtomicOntologyChange;
import edu.cornell.mannlib.vitro.webapp.ontology.update.AtomicOntologyChange.AtomicChangeType;

import org.skife.csv.CSVReader;
import org.skife.csv.SimpleReader;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * Performs parsing on Prompt output and provides change object list.
 * 
 * @author ass92
 *
 */

public class OntologyChangeParser {

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	@SuppressWarnings({ "unchecked", "null", "static-access" })
	public static ArrayList<AtomicOntologyChange> parseFile(String diffPath) throws IOException{
		
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
		
		List<String[]> rows = readFile.parse(in);
		
		for(int rowNum = 0; rowNum < rows.size(); rowNum++){
			
			String[] cols = rows.get(rowNum);
			
			for(int col =0; col < cols.length; col++){
				String column = cols[col].trim();
				stArr = new StringTokenizer(column,"	");
				countColumns = stArr.countTokens();
				changeObj = new AtomicOntologyChange();
				
					if(countColumns == 4){
						
						URI = stArr.nextToken();
						rename = stArr.nextToken();
						String check = stArr.nextToken();
						
						if(check.equalsIgnoreCase("Add")){
							
							AtomicChangeType atomicChangeType = changeObj.getAtomicChangeType();
							changeObj.setAtomicChangeType(atomicChangeType.ADD);
							changeObj.setDestinationURI(URI);
							changeObjects.add(changeObj);
							
						}
						else{
							AtomicChangeType atomicChangeType = changeObj.getAtomicChangeType();
							changeObj.setAtomicChangeType(atomicChangeType.DELETE);
							changeObj.setSourceURI(URI);
							changeObjects.add(changeObj);
						}
					}
					else{
						
						sourceURI = stArr.nextToken();
						destinationURI = stArr.nextToken();
						AtomicChangeType atomicChangeType = changeObj.getAtomicChangeType();
						changeObj.setAtomicChangeType(atomicChangeType.RENAME);
						changeObj.setSourceURI(sourceURI);
						changeObj.setDestinationURI(destinationURI);
						changeObjects.add(changeObj);
					     
					}
					
			}
			
		}
		
		return changeObjects;
	}

}
