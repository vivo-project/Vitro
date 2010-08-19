/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

/**
 * Utility methods that get the appropriate model locks before manipluating
 * resources.
 */
public class ResourceWrapper {

	public static Statement getProperty(Resource resource, Property property) {
		resource.getModel().enterCriticalSection(Lock.READ);
		try {
			return resource.getProperty(property);
		} finally {
			resource.getModel().leaveCriticalSection();
		}
	}

	public static void addProperty(Resource resource, Property property,
			String value) {
		resource.getModel().enterCriticalSection(Lock.WRITE);
		try {
			resource.addProperty(property, value);
		} finally {
			resource.getModel().leaveCriticalSection();
		}
	}

	public static void removeAll(Resource resource, Property property) {
		resource.getModel().enterCriticalSection(Lock.WRITE);
		try {
			resource.removeAll(property);
		} finally {
			resource.getModel().leaveCriticalSection();
		}
	}

	public static Collection<Statement> listProperties(Resource resource,
			Property prop) {
		List<Statement> list = new ArrayList<Statement>();
		StmtIterator stmts = resource.listProperties(prop);
		try {
			while (stmts.hasNext()) {
				list.add(stmts.next());
			}
			return list;
		} finally {
			stmts.close();
		}
	}

}
