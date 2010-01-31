/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.pellet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.ObjectPropertyStatementPattern;

public class ObjectPropertyStatementPatternFactory {

	//private static Set<ObjectPropertyStatementPattern> patternSet = new HashSet<ObjectPropertyStatementPattern>();
	
	public static ObjectPropertyStatementPattern getPattern(Resource subject, Property predicate, Resource object) {
		//for (Iterator<ObjectPropertyStatementPattern> i = patternSet.iterator(); i.hasNext(); ) {
		//	ObjectPropertyStatementPattern pat = i.next();
		//	if ( ( (pat.getSubject()==null && subject==null) || (pat.getSubject().equals(subject)) )
		//	     && ( (pat.getPredicate()==null && predicate==null) || (pat.getPredicate().equals(predicate)) ) 
		//	     && ( (pat.getObject()==null && object==null) || (pat.getObject().equals(object)) ) ) {
		//		return pat;
		//	} 		
		//}
		ObjectPropertyStatementPattern newPat = new ObjectPropertyStatementPattern(subject,predicate,object);
		//patternSet.add(newPat);
		return newPat;
	}
	
}
