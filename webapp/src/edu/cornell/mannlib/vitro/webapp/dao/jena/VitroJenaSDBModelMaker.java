/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;

import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
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
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.ibm.icu.text.Collator;

public class VitroJenaSDBModelMaker implements ModelMaker {

	// TODO: need to rethink the inheritance/interfaces here
	
	private StoreDesc storeDesc = null;
	private BasicDataSource bds = null;
	private SDBConnection conn = null;
	
	public static final String METADATA_MODEL_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/metadata";
	public static final String HAS_NAMED_MODEL_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/hasNamedModel";
	
	private Resource sdbResource; // a resource representing the SDB database 
	
	public VitroJenaSDBModelMaker(StoreDesc storeDesc, BasicDataSource bds) throws SQLException {
		
	    this.storeDesc = storeDesc;
	    this.bds = bds;
	    Store store = getStore();
    	try {
		
    		Model metadataModel = getMetadataModel();
    		
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
	    } finally {
	        store.close();
	    }
	}
	
	private static final int MAX_TRIES = 10;
	
	private Store getStore() {
	    Store store = null;
	    boolean goodStore = false;
	    int tries = 0;
	    while (!goodStore && tries < MAX_TRIES) {
	        tries++;
	        if (conn == null) {
	            try {
                    conn = new SDBConnection(bds.getConnection());
                } catch (SQLException sqle) {
                    throw new RuntimeException(
                            "Unable to get SQL connection", sqle);
                }
	        }
	        store = SDBFactory.connectStore(conn, storeDesc);
	        try {
    	        if (!StoreUtils.isFormatted(store)) {
                    // initialize the store
                    store.getTableFormatter().create();
                    store.getTableFormatter().truncate();
                }
	        } catch (SQLException sqle) {
                conn.close();
                conn = null;
	            throw new RuntimeException(
	                    "Unable to set up SDB store for model maker", sqle);
	        }
	        if (!isWorking(store)) {
	            if (conn != null) {
	                conn.close();
                    conn = null;
	            }
	            
	        } else {
	            goodStore = true;
	        }
	    }
	    if (store == null) {
	        throw new RuntimeException(
	                "Unable to connect to SDB store after " + 
	                MAX_TRIES + " attempts");
	    }
	    return store;
	}
	
	Model getMetadataModel() {
	    return getModel(METADATA_MODEL_URI);
	}
	
	private boolean isWorking(Store store) {
	    Dataset d = SDBFactory.connectDataset(store);
	    try {
	        String validationQuery = "ASK { <" + RDFS.Resource.getURI() + "> " +
	                                 "   <" + RDFS.isDefinedBy.getURI() + "> " +
	                                 "   <" + RDFS.Resource.getURI() + "> }";
	        Query q = QueryFactory.create(validationQuery);
	        QueryExecution qe = QueryExecutionFactory.create(q, d);
	        try {
	            qe.execAsk();
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        } finally {
	            qe.close();
	        }
	    } finally {
	        d.close();
	    }
	}
	
	public void close() {
		getStore().close();
	}

	public Model createModel(String arg0) {
		Model model = SDBFactory.connectNamedModel(getStore(), arg0);
		Model metadataModel = getMetadataModel();
		try {
		    metadataModel.add(sdbResource,metadataModel.getProperty(HAS_NAMED_MODEL_URI),arg0);
		} finally {
		    metadataModel.close();
		}
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
	    Model metadataModel = getMetadataModel();
	    try {
    		StmtIterator stmtIt = metadataModel.listStatements(sdbResource,metadataModel.getProperty(HAS_NAMED_MODEL_URI),arg0);
    		try {
    			return stmtIt.hasNext();
    		} finally {
    			if (stmtIt != null) {
    				stmtIt.close();
    			}
    		}
	    } finally {
	        metadataModel.close();
	    }
	}

	public ExtendedIterator listModels() {
		ArrayList<String> metaNameList = new ArrayList<String>();
		ArrayList<String> storeNameList = new ArrayList<String>();
		ArrayList<String> unionNameList = new ArrayList<String>();
		
		Model metadataModel = getMetadataModel();
		
		Iterator<RDFNode> metadataNameIt = metadataModel.listObjectsOfProperty(metadataModel.getProperty(HAS_NAMED_MODEL_URI));
		Iterator<Node> storeNameIt = StoreUtils.storeGraphNames(getStore());
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
		
	    if (metadataModel != null) {
	        metadataModel.close();
	    }
	    
		return WrappedIterator.create(unionNameList.iterator());
	}

	public Model openModel(String arg0, boolean arg1) {
		return SDBFactory.connectNamedModel(getStore(),arg0);
	}

	public void removeModel(String arg0) {
		Model m = getModel(arg0);
		m.removeAll(null,null,null);
		Model metadataModel = getMetadataModel();
		try {
		    metadataModel.remove(sdbResource,metadataModel.getProperty(HAS_NAMED_MODEL_URI),metadataModel.createLiteral(arg0));
		} finally {
		    metadataModel.close();
		}
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
		return SDBFactory.connectDefaultModel(getStore());
	}

	public Model createDefaultModel() {
		return SDBFactory.connectDefaultModel(getStore());
	}

	public Model createFreshModel() {
		throw new UnsupportedOperationException("createFreshModel not supported by "+this.getClass().getName());
	}

	/**
	 * @deprecated
	 */
	public Model createModel() {
		return SDBFactory.connectDefaultModel(getStore());
	}

	/**
	 * @deprecated
	 */
	public Model getModel() {
		return SDBFactory.connectDefaultModel(getStore());
	}

	public Model openModel(String arg0) {
		return SDBFactory.connectDefaultModel(getStore());
	}

	public Model openModelIfPresent(String arg0) {
		return (this.hasModel(arg0)) ? SDBFactory.connectNamedModel(getStore(),arg0) : null;
	}

	public Model getModel(String arg0) {
		return SDBFactory.connectNamedModel(getStore(), arg0);
	}

	public Model getModel(String arg0, ModelReader arg1) {
		throw new UnsupportedOperationException("getModel(String,ModelReader) not supported by "+this.getClass().getName());
	}

}
