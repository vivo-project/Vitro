/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.org.apache.jena.rdf.model;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;

/**
 * Only implemented what I needed so far. The rest is left as an exercise for
 * the student.
 */
public class LiteralStub implements Literal {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	final String language;

	public LiteralStub(String language) {
		this.language = language;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public boolean isAnon() {
		return false;
	}

	@Override
	public boolean isResource() {
		return false;
	}

	@Override
	public boolean isURIResource() {
		return false;
	}

	@Override
	public Literal asLiteral() {
		return this;
	}

	@Override
	public Resource asResource() {
		throw new ClassCastException();
	}

	@Override
	public String getLanguage() {
		return language;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public <T extends RDFNode> T as(Class<T> view) {
		throw new RuntimeException("LiteralStub.as() not implemented.");
	}

	@Override
	public <T extends RDFNode> boolean canAs(Class<T> arg0) {
		throw new RuntimeException("LiteralStub.canAs() not implemented.");
	}

	@Override
	public Model getModel() {
		throw new RuntimeException("LiteralStub.getModel() not implemented.");
	}

	@Override
	public Object visitWith(RDFVisitor arg0) {
		throw new RuntimeException("LiteralStub.visitWith() not implemented.");
	}

	@Override
	public Node asNode() {
		throw new RuntimeException("LiteralStub.asNode() not implemented.");
	}

	@Override
	public boolean getBoolean() {
		throw new RuntimeException("LiteralStub.getBoolean() not implemented.");
	}

	@Override
	public byte getByte() {
		throw new RuntimeException("LiteralStub.getByte() not implemented.");
	}

	@Override
	public char getChar() {
		throw new RuntimeException("LiteralStub.getChar() not implemented.");
	}

	@Override
	public RDFDatatype getDatatype() {
		throw new RuntimeException("LiteralStub.getDatatype() not implemented.");
	}

	@Override
	public String getDatatypeURI() {
		throw new RuntimeException(
				"LiteralStub.getDatatypeURI() not implemented.");
	}

	@Override
	public double getDouble() {
		throw new RuntimeException("LiteralStub.getDouble() not implemented.");
	}

	@Override
	public float getFloat() {
		throw new RuntimeException("LiteralStub.getFloat() not implemented.");
	}

	@Override
	public int getInt() {
		throw new RuntimeException("LiteralStub.getInt() not implemented.");
	}

	@Override
	public String getLexicalForm() {
		throw new RuntimeException(
				"LiteralStub.getLexicalForm() not implemented.");
	}

	@Override
	public long getLong() {
		throw new RuntimeException("LiteralStub.getLong() not implemented.");
	}

	@Override
	public short getShort() {
		throw new RuntimeException("LiteralStub.getShort() not implemented.");
	}

	@Override
	public String getString() {
		throw new RuntimeException("LiteralStub.getString() not implemented.");
	}

	@Override
	public Object getValue() {
		throw new RuntimeException("LiteralStub.getValue() not implemented.");
	}

	@Override
	public Literal inModel(Model arg0) {
		throw new RuntimeException("LiteralStub.inModel() not implemented.");
	}

	@Override
	public boolean isWellFormedXML() {
		throw new RuntimeException(
				"LiteralStub.isWellFormedXML() not implemented.");
	}

	@Override
	public boolean sameValueAs(Literal arg0) {
		throw new RuntimeException("LiteralStub.sameValueAs() not implemented.");
	}

}
