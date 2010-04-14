/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.skife.csv.CSVReader;
import org.skife.csv.SimpleReader;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Csv2Rdf {

	private String namespace;
	private String tboxNamespace;
	private String typeName;
	private String individualNameBase;
	private String propertyNameBase;
	private char separatorChar;
    private char[] quoteChars;
	
    public Csv2Rdf(char[] quoteChars, String namespace, String tboxNamespace, String typeName) {
    	this.separatorChar = ',';
		this.quoteChars = quoteChars;
		this.namespace = namespace;
		this.tboxNamespace = tboxNamespace;
		this.typeName = typeName;
		this.individualNameBase = typeName.toLowerCase();
		this.propertyNameBase = individualNameBase+"_";
	}
    
	public Csv2Rdf(char separatorChar, char[] quoteChars, String namespace, String tboxNamespace, String typeName) {
		this.separatorChar = separatorChar;
		this.quoteChars = quoteChars;
		this.namespace = namespace;
		this.tboxNamespace = tboxNamespace;
		this.typeName = typeName;
		this.individualNameBase = typeName.toLowerCase();
		this.propertyNameBase = individualNameBase+"_";
	}
	
	public Model[] convertToRdf(InputStream fis) throws IOException {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntModel tboxOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        ontModel.addSubModel(tboxOntModel);
        OntClass theClass = tboxOntModel.createClass(tboxNamespace+typeName);

		CSVReader cReader = new SimpleReader();
		cReader.setSeperator(separatorChar);
		cReader.setQuoteCharacters(quoteChars);	
		
		List<String[]> fileRows = cReader.parse(fis);
		
        String[] columnHeaders = fileRows.get(0);

        DatatypeProperty[] dpArray = new DatatypeProperty[columnHeaders.length];

        for (int i=0; i<columnHeaders.length; i++) {
            dpArray[i] = tboxOntModel.createDatatypeProperty(tboxNamespace+propertyNameBase+columnHeaders[i].replaceAll("\\W",""));
        }

        for (int row=1; row<fileRows.size(); row++) {
	        Individual ind = ontModel.createIndividual(namespace+individualNameBase+row,theClass);
	        String[] cols = fileRows.get(row);
	        for (int col=0; col<cols.length; col++) {
				String value = cols[col].trim();
	            if (value.length()>0) {
	                ind.addProperty(dpArray[col], value); // no longer using: , XSDDatatype.XSDstring);
	                // TODO: specification of datatypes for columns
	            }
	        }
        }
        
        ontModel.removeSubModel(tboxOntModel);
		
		Model[] resultModels = new Model[2];
		resultModels[0] = ontModel;
		resultModels[1] = tboxOntModel;
		return resultModels;
		
	}
	
}
