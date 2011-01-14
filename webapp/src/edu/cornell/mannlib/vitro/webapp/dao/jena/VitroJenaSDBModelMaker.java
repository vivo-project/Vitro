/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.ibm.icu.text.Collator;

public class VitroJenaSDBModelMaker implements ModelMaker {

	// TODO: need to rethink the inheritance/interfaces here
	
	private Store store = null;
	
	public static final String METADATA_MODEL_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/metadata";
	public static final String HAS_NAMED_MODEL_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/hasNamedModel";
	
	private Model metadataModel;
	private Resource sdbResource; // a resource representing the SDB database 
	
	public VitroJenaSDBModelMaker(Store store) {
		this.store = store;
		try {
			Model meta = getModel(METADATA_MODEL_URI);
			// Test query to see if the database has been initialized
			meta.listStatements(null, RDF.type, OWL.Nothing); 
		} catch (Exception e) {
			// initialize the store
			store.getTableFormatter().create();
        	store.getTableFormatter().truncate();
		}
		
		this.metadataModel = getModel(METADATA_MODEL_URI);
		
		if (metadataModel.size()==0) {
			// set up the model name metadata to avoid expensive calls to listNames()
			Resource sdbRes = metadataModel.createResource(); 
			this.sdbResource = sdbRes;
			Iterator nameIt = SDBFactory.connectDataset(store).listNames();
			while (nameIt.hasNext()) {
				String name = (String) nameIt.next();
				metadataModel.add(sdbResource,metadataModel.getProperty(HAS_NAMED_MODEL_URI),name);
			}
		} else {
			StmtIterator stmtIt = metadataModel.listStatements((Resource)null, metadataModel.getProperty(HAS_NAMED_MODEL_URI),(RDFNode)null);
			if (stmtIt.hasNext()) {
				Statement stmt = stmtIt.nextStatement();
				sdbResource = stmt.getSubject();
			}
			stmtIt.close();
		}
	}
	
	public void close() {
		store.close();
	}

	public Model createModel(String arg0) {
		Model model = SDBFactory.connectNamedModel(store, arg0);
		metadataModel.add(sdbResource,metadataModel.getProperty(HAS_NAMED_MODEL_URI),arg0);
		return model;
	}

	public Model createModel(String arg0, boolean arg1) {
		// TODO Figure out if we can offer a "create if not found" option using SDB
		return createModel(arg0);
	}

	public GraphMaker getGraphMaker() {
		throw new UnsupportedOperationException("GraphMaker not supported by "+this.getClass().getName());
	}

	public boolean hasModel(String arg0) {
		StmtIterator stmtIt = metadataModel.listStatements(sdbResource,metadataModel.getProperty(HAS_NAMED_MODEL_URI),arg0);
		try {
			return stmtIt.hasNext();
		} finally {
			if (stmtIt != null) {
				stmtIt.close();
			}
		}
	}

	public ExtendedIterator listModels() {
		ArrayList<String> metaNameList = new ArrayList<String>();
		ArrayList<String> storeNameList = new ArrayList<String>();
		ArrayList<String> unionNameList = new ArrayList<String>();
		Iterator<RDFNode> metadataNameIt = metadataModel.listObjectsOfProperty(metadataModel.getProperty(HAS_NAMED_MODEL_URI));
		Iterator<Node> storeNameIt = StoreUtils.storeGraphNames(store);
		Node node = null;
		RDFNode rdfNode = null;
		
		// implement comparator to sort the lists
		
		class sortList implements Comparator<String>{
			Collator collator = Collator.getInstance();
			int compareResult;
			public int compare(String str1, String str2){
				compareResult = collator.compare(str1, str2);
				if(compareResult > 0)
					return 1;
				else if(compareResult < 0)
					return -1;
				else 
					return 0;
			}
		}
		
		// put the names into the lists.
		
		while (metadataNameIt.hasNext()) {
			rdfNode = metadataNameIt.next();
			if (rdfNode.isLiteral()) {
				metaNameList.add( ((Literal)rdfNode).getLexicalForm());
			}
		}
		
		
	
		while (storeNameIt.hasNext()){
			 node = storeNameIt.next();
			 storeNameList.add(node.getURI());
		}
		
		
		// sort the lists
		if(metaNameList.size()!=0)
			Collections.sort(metaNameList, new sortList());
		if(storeNameList.size()!=0)
			Collections.sort(storeNameList, new sortList());
		
		
		// code to merge the lists.
				
		Collator collator = Collator.getInstance();
		int check = 0;
		
		Iterator<String> metaItr = metaNameList.iterator();
	    Iterator<String> storeItr = storeNameList.iterator();
	    String metaString = null;
	    String storeString = null;
	    
	    do{
	    	
	    	if(metaString != null && storeString !=null){
	    		check = collator.compare(metaString, storeString);
	    	}
	    	else if(metaString!=null && storeString == null){
	    		unionNameList.add(metaString);
	    		if(metaItr.hasNext())
	    			metaString = metaItr.next();
	    		else
	    			metaString = null;
	    		continue;
	    	}
	    	else if(metaString==null && storeString!=null){
	    		unionNameList.add(storeString);
	    		if(storeItr.hasNext())
	    			storeString = storeItr.next();
	    		else
	    			storeString = null;
	    		continue;
	    	}
	    	else{
	    	    if(metaItr.hasNext()){
	    	    	metaString = metaItr.next();
	    	    }
	    	    if(storeItr.hasNext()){
	    	    	storeString = storeItr.next();
	    	    }
	    	    if(metaString!=null && storeString !=null)
	    	    	check = collator.compare(metaString, storeString);
	    	    else
	    	    	continue;
	    	}
	    	
	    	if(check > 0){
	    		unionNameList.add(storeString);
	    		if(storeItr.hasNext())
	    			storeString = storeItr.next();
	    		else
	    			storeString = null;
	    	}
	    	else if(check < 0){
	    		unionNameList.add(metaString);
	    		if(metaItr.hasNext())
	    			metaString = metaItr.next();
	    		else
	    			metaString = null;
	    	}
	    	else{
	    		unionNameList.add(metaString);
	    		if(metaItr.hasNext())
	    			metaString = metaItr.next();
	    		else 
	    			metaString = null;
	    		
	    		if(storeItr.hasNext())
	    			storeString = storeItr.next();
	    		else
	    			storeString = null;
	    	}
	    	
	    		
	    }while(metaString!=null || storeString!=null);
		
		return WrappedIterator.create(unionNameList.iterator());
	}

	public Model openModel(String arg0, boolean arg1) {
		return SDBFactory.connectNamedModel(store,arg0);
	}

	public void removeModel(String arg0) {
		Model m = getModel(arg0);
		m.removeAll(null,null,null);
		metadataModel.remove(sdbResource,metadataModel.getProperty(HAS_NAMED_MODEL_URI),metadataModel.createLiteral(arg0));
	}

	public Model addDescription(Model arg0, Resource arg1) {
		throw new UnsupportedOperationException("addDescription not supported by "+this.getClass().getName());
	}

	public Model createModelOver(String arg0) {
		throw new UnsupportedOperationException("createModelOver not supported by "+this.getClass().getName());
	}

	public Model getDescription() {
		throw new UnsupportedOperationException("createModelOver not supported by "+this.getClass().getName());
	}

	public Model getDescription(Resource arg0) {
		throw new UnsupportedOperationException("getDescription not supported by "+this.getClass().getName());
	}

	public Model openModel() {
		return SDBFactory.connectDefaultModel(store);
	}

	public Model createDefaultModel() {
		return SDBFactory.connectDefaultModel(store);
	}

	public Model createFreshModel() {
		throw new UnsupportedOperationException("createFreshModel not supported by "+this.getClass().getName());
	}

	/**
	 * @deprecated
	 */
	public Model createModel() {
		return SDBFactory.connectDefaultModel(store);
	}

	/**
	 * @deprecated
	 */
	public Model getModel() {
		return SDBFactory.connectDefaultModel(store);
	}

	public Model openModel(String arg0) {
		return SDBFactory.connectDefaultModel(store);
	}

	public Model openModelIfPresent(String arg0) {
		return (this.hasModel(arg0)) ? SDBFactory.connectNamedModel(store,arg0) : null;
	}

	public Model getModel(String arg0) {
		return SDBFactory.connectNamedModel(store, arg0);
	}

	public Model getModel(String arg0, ModelReader arg1) {
		throw new UnsupportedOperationException("getModel(String,ModelReader) not supported by "+this.getClass().getName());
	}

}
