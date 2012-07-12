/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;

/**
 * A minimal implementation of the NamespaceMapper.
 * 
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class NamespaceMapperStub implements NamespaceMapper {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------
	
	private final Map<String, String> prefixMap = new HashMap<String, String>();
	
	public void setPrefixForNamespace(String prefix, String namespace) {
		prefixMap.put(prefix, namespace);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public String getNamespaceForPrefix(String prefix) {
		return prefixMap.get(prefix);
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void addedStatement(Statement arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.addedStatement() not implemented.");
	}

	@Override
	public void addedStatements(Statement[] arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.addedStatements() not implemented.");
	}

	@Override
	public void addedStatements(List<Statement> arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.addedStatements() not implemented.");
	}

	@Override
	public void addedStatements(StmtIterator arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.addedStatements() not implemented.");
	}

	@Override
	public void addedStatements(Model arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.addedStatements() not implemented.");
	}

	@Override
	public void notifyEvent(Model arg0, Object arg1) {
		throw new RuntimeException(
				"NamespaceMapperStub.notifyEvent() not implemented.");
	}

	@Override
	public void removedStatement(Statement arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.removedStatement() not implemented.");
	}

	@Override
	public void removedStatements(Statement[] arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.removedStatements() not implemented.");
	}

	@Override
	public void removedStatements(List<Statement> arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.removedStatements() not implemented.");
	}

	@Override
	public void removedStatements(StmtIterator arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.removedStatements() not implemented.");
	}

	@Override
	public void removedStatements(Model arg0) {
		throw new RuntimeException(
				"NamespaceMapperStub.removedStatements() not implemented.");
	}

	@Override
	public String getPrefixForNamespace(String namespace) {
		throw new RuntimeException(
				"NamespaceMapperStub.getPrefixForNamespace() not implemented.");
	}

	@Override
	public List<String> getPrefixesForNamespace(String namespace) {
		throw new RuntimeException(
				"NamespaceMapperStub.getPrefixesForNamespace() not implemented.");
	}

}
