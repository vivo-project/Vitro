/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

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
	
	public Model[] convertToRdf(InputStream fis,VitroRequest vreq, Model destination) throws IOException {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntModel tboxOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        ontModel.addSubModel(tboxOntModel);
        OntClass theClass = tboxOntModel.createClass(tboxNamespace+typeName);

		CSVReader cReader = new SimpleReader();
		cReader.setSeperator(separatorChar);
		cReader.setQuoteCharacters(quoteChars);	
		WebappDaoFactory wdf = vreq.getFullWebappDaoFactory();
		Random random = new Random();
		boolean uriIsGood = false;
		boolean inDestination = false;
        int attempts = 0;
        String uri = null;
		String errMsg = null;
		List<String[]> fileRows = cReader.parse(fis);
		
        String[] columnHeaders = fileRows.get(0);

        DatatypeProperty[] dpArray = new DatatypeProperty[columnHeaders.length];

        for (int i=0; i<columnHeaders.length; i++) {
            dpArray[i] = tboxOntModel.createDatatypeProperty(tboxNamespace+propertyNameBase+columnHeaders[i].replaceAll("\\W",""));
        }

        for (int row=1; row<fileRows.size(); row++) {
        	while( uriIsGood == false && attempts < 30 ){	
        		uri = namespace+individualNameBase+random.nextInt( Math.min(Integer.MAX_VALUE,(int)Math.pow(2,attempts + 13)) );
	        errMsg = wdf.checkURI(uri);
	        Resource res = ResourceFactory.createResource(uri);
	        inDestination = destination.contains(res, null);
			if(  errMsg != null && !inDestination)
				uri = null;
			else
				uriIsGood = true;				
			attempts++;
		}
        	uriIsGood = false;
        	attempts =0;
        	inDestination = false;
        	Individual ind = ontModel.createIndividual(uri,theClass);
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
