/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

/**
 * Utility methods that operate against the Model with proper locks.
 */
public class ModelWrapper {

	public static Collection<Resource> listResourcesWithProperty(Model model,
			Property property) {
		List<Resource> list = new ArrayList<Resource>();
		ResIterator iterator = model.listResourcesWithProperty(property);
		try {
			while (iterator.hasNext()) {
				Resource resource = iterator.next();
				list.add(resource);
			}
		} finally {
			iterator.close();
		}
		return list;
	}

	public static void removeStatement(Model model, Statement stmt) {
		model.enterCriticalSection(Lock.WRITE);
		try {
			model.remove(stmt);
		} finally {
			model.leaveCriticalSection();
		}
	}

	public static void add(Model model, Resource subject, Property predicate,
			String value) {
		model.enterCriticalSection(Lock.WRITE);
		try {
			model.add(subject, predicate, value);
		} finally {
			model.leaveCriticalSection();
		}
	}
}
