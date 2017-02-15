/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.AllDifferent;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.CardinalityQRestriction;
import org.apache.jena.ontology.CardinalityRestriction;
import org.apache.jena.ontology.ComplementClass;
import org.apache.jena.ontology.DataRange;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.EnumeratedClass;
import org.apache.jena.ontology.FunctionalProperty;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.IntersectionClass;
import org.apache.jena.ontology.InverseFunctionalProperty;
import org.apache.jena.ontology.MaxCardinalityQRestriction;
import org.apache.jena.ontology.MaxCardinalityRestriction;
import org.apache.jena.ontology.MinCardinalityQRestriction;
import org.apache.jena.ontology.MinCardinalityRestriction;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.ontology.Profile;
import org.apache.jena.ontology.QualifiedRestriction;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.ontology.SymmetricProperty;
import org.apache.jena.ontology.TransitiveProperty;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.Alt;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.NsIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.RSIterator;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.shared.Command;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;

import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * The base class for a delegating ontology model decorator.
 * 
 * As implemented, all methods simply delegate to the inner model. Subclasses
 * should override selected methods to provide functionality.
 */
public abstract class AbstractOntModelDecorator implements OntModel {
	private final OntModel inner;

	protected AbstractOntModelDecorator(OntModel m) {
		if (m == null) {
			throw new NullPointerException("m may not be null.");
		}
		this.inner = m;
	}
	
	@Override
	public String toString() {
		return ToString.simpleName(this) + "[" + ToString.hashHex(this) + ", "
				+ ToString.ontModelToString(inner) + "]";
	}

	@Override
	@Deprecated
	public Resource getResource(String uri, org.apache.jena.rdf.model.ResourceF f) {
		return inner.getResource(uri, f);
	}

	@Override
	public Property getProperty(String uri) {
		return inner.getProperty(uri);
	}

	@Override
	public Bag getBag(String uri) {
		return inner.getBag(uri);
	}

	@Override
	public Bag getBag(Resource r) {
		return inner.getBag(r);
	}

	@Override
	public Alt getAlt(String uri) {
		return inner.getAlt(uri);
	}

	@Override
	public Alt getAlt(Resource r) {
		return inner.getAlt(r);
	}

	@Override
	public Seq getSeq(String uri) {
		return inner.getSeq(uri);
	}

	@Override
	public Seq getSeq(Resource r) {
		return inner.getSeq(r);
	}

	@Override
	public Resource createResource(Resource type) {
		return inner.createResource(type);
	}

	@Override
	public RDFNode getRDFNode(Node n) {
		return inner.getRDFNode(n);
	}

	@Override
	public Resource createResource(String uri, Resource type) {
		return inner.createResource(uri, type);
	}

	@Override
	@Deprecated
	public Resource createResource(org.apache.jena.rdf.model.ResourceF f) {
		return inner.createResource(f);
	}

	@Override
	@Deprecated
	public Resource createResource(String uri, org.apache.jena.rdf.model.ResourceF f) {
		return inner.createResource(uri, f);
	}

	@Override
	public Property createProperty(String uri) {
		return inner.createProperty(uri);
	}

	@Override
	public Literal createLiteral(String v) {
		return inner.createLiteral(v);
	}

	@Override
	public Literal createTypedLiteral(boolean v) {
		return inner.createTypedLiteral(v);
	}

	@Override
	public Literal createTypedLiteral(int v) {
		return inner.createTypedLiteral(v);
	}

	@Override
	public Literal createTypedLiteral(long v) {
		return inner.createTypedLiteral(v);
	}

	@Override
	public Literal createTypedLiteral(Calendar d) {
		return inner.createTypedLiteral(d);
	}

	@Override
	public Literal createTypedLiteral(char v) {
		return inner.createTypedLiteral(v);
	}

	@Override
	public Literal createTypedLiteral(float v) {
		return inner.createTypedLiteral(v);
	}

	@Override
	public Literal createTypedLiteral(double v) {
		return inner.createTypedLiteral(v);
	}

	@Override
	public Literal createTypedLiteral(String v) {
		return inner.createTypedLiteral(v);
	}

	@Override
	public Literal createTypedLiteral(String lex, String typeURI) {
		return inner.createTypedLiteral(lex, typeURI);
	}

	@Override
	public Literal createTypedLiteral(Object value, String typeURI) {
		return inner.createTypedLiteral(value, typeURI);
	}

	@Override
	public Statement createLiteralStatement(Resource s, Property p, boolean o) {
		return inner.createLiteralStatement(s, p, o);
	}

	@Override
	public Statement createLiteralStatement(Resource s, Property p, float o) {
		return inner.createLiteralStatement(s, p, o);
	}

	@Override
	public Statement createLiteralStatement(Resource s, Property p, double o) {
		return inner.createLiteralStatement(s, p, o);
	}

	@Override
	public Statement createLiteralStatement(Resource s, Property p, long o) {
		return inner.createLiteralStatement(s, p, o);
	}

	@Override
	public Statement createLiteralStatement(Resource s, Property p, int o) {
		return inner.createLiteralStatement(s, p, o);
	}

	@Override
	public Statement createLiteralStatement(Resource s, Property p, char o) {
		return inner.createLiteralStatement(s, p, o);
	}

	@Override
	public Statement createLiteralStatement(Resource s, Property p, Object o) {
		return inner.createLiteralStatement(s, p, o);
	}

	@Override
	public Statement createStatement(Resource s, Property p, String o) {
		return inner.createStatement(s, p, o);
	}

	@Override
	public Statement createStatement(Resource s, Property p, String o, String l) {
		return inner.createStatement(s, p, o, l);
	}

	@Override
	public Statement createStatement(Resource s, Property p, String o,
			boolean wellFormed) {
		return inner.createStatement(s, p, o, wellFormed);
	}

	@Override
	public Statement createStatement(Resource s, Property p, String o,
			String l, boolean wellFormed) {
		return inner.createStatement(s, p, o, l, wellFormed);
	}

	@Override
	public Bag createBag() {
		return inner.createBag();
	}

	@Override
	public Bag createBag(String uri) {
		return inner.createBag(uri);
	}

	@Override
	public Alt createAlt() {
		return inner.createAlt();
	}

	@Override
	public PrefixMapping setNsPrefix(String prefix, String uri) {
		return inner.setNsPrefix(prefix, uri);
	}

	@Override
	public PrefixMapping removeNsPrefix(String prefix) {
		return inner.removeNsPrefix(prefix);
	}

	@Override
	public PrefixMapping setNsPrefixes(PrefixMapping other) {
		return inner.setNsPrefixes(other);
	}

	@Override
	public PrefixMapping setNsPrefixes(Map<String, String> map) {
		return inner.setNsPrefixes(map);
	}

	@Override
	public PrefixMapping withDefaultMappings(PrefixMapping map) {
		return inner.withDefaultMappings(map);
	}

	@Override
	public String getNsPrefixURI(String prefix) {
		return inner.getNsPrefixURI(prefix);
	}

	@Override
	public String getNsURIPrefix(String uri) {
		return inner.getNsURIPrefix(uri);
	}

	@Override
	public Map<String, String> getNsPrefixMap() {
		return inner.getNsPrefixMap();
	}

	@Override
	public String expandPrefix(String prefixed) {
		return inner.expandPrefix(prefixed);
	}

	@Override
	public String shortForm(String uri) {
		return inner.shortForm(uri);
	}

	@Override
	public String qnameFor(String uri) {
		return inner.qnameFor(uri);
	}

	@Override
	public PrefixMapping lock() {
		return inner.lock();
	}

	@Override
	public boolean samePrefixMappingAs(PrefixMapping other) {
		return inner.samePrefixMappingAs(other);
	}

	@Override
	public Statement asStatement(Triple t) {
		return inner.asStatement(t);
	}

	@Override
	public Graph getGraph() {
		return inner.getGraph();
	}

	@Override
	public RDFNode asRDFNode(Node n) {
		return inner.asRDFNode(n);
	}

	@Override
	public Resource wrapAsResource(Node n) {
		return inner.wrapAsResource(n);
	}

	@Override
	public RDFReader getReader() {
		return inner.getReader();
	}

	@Override
	public RDFReader getReader(String lang) {
		return inner.getReader(lang);
	}

	@Override
	public String setReaderClassName(String lang, String className) {
		return inner.setReaderClassName(lang, className);
	}

	@Override
	public void resetRDFReaderF() {
		inner.resetRDFReaderF();
	}

	@Override
	public String removeReader(String s) throws IllegalArgumentException {
		return inner.removeReader(s);
	}

	@Override
	public RDFWriter getWriter() {
		return inner.getWriter();
	}

	@Override
	public RDFWriter getWriter(String lang) {
		return inner.getWriter(lang);
	}

	@Override
	public String setWriterClassName(String lang, String className) {
		return inner.setWriterClassName(lang, className);
	}

	@Override
	public void resetRDFWriterF() {
		inner.resetRDFWriterF();
	}

	@Override
	public String removeWriter(String s) throws IllegalArgumentException {
		return inner.removeWriter(s);
	}

	@Override
	public Alt createAlt(String uri) {
		return inner.createAlt(uri);
	}

	@Override
	public Seq createSeq() {
		return inner.createSeq();
	}

	@Override
	public Seq createSeq(String uri) {
		return inner.createSeq(uri);
	}

	@Override
	public Model add(Resource s, Property p, RDFNode o) {
		return inner.add(s, p, o);
	}

	@Override
	public Model addLiteral(Resource s, Property p, boolean o) {
		return inner.addLiteral(s, p, o);
	}

	@Override
	public Model addLiteral(Resource s, Property p, long o) {
		return inner.addLiteral(s, p, o);
	}

	@Override
	public Model addLiteral(Resource s, Property p, int o) {
		return inner.addLiteral(s, p, o);
	}

	@Override
	public Model addLiteral(Resource s, Property p, char o) {
		return inner.addLiteral(s, p, o);
	}

	@Override
	public Model addLiteral(Resource s, Property p, float o) {
		return inner.addLiteral(s, p, o);
	}

	@Override
	public Model addLiteral(Resource s, Property p, double o) {
		return inner.addLiteral(s, p, o);
	}

	@Override
	@Deprecated
	public Model addLiteral(Resource s, Property p, Object o) {
		return inner.addLiteral(s, p, o);
	}

	@Override
	public Model addLiteral(Resource s, Property p, Literal o) {
		return inner.addLiteral(s, p, o);
	}

	@Override
	public Model add(Resource s, Property p, String o) {
		return inner.add(s, p, o);
	}

	@Override
	public Model add(Resource s, Property p, String lex, RDFDatatype datatype) {
		return inner.add(s, p, lex, datatype);
	}

	@Override
	public Model add(Resource s, Property p, String o, boolean wellFormed) {
		return inner.add(s, p, o, wellFormed);
	}

	@Override
	public Model add(Resource s, Property p, String o, String l) {
		return inner.add(s, p, o, l);
	}

	@Override
	public Model remove(Resource s, Property p, RDFNode o) {
		return inner.remove(s, p, o);
	}

	@Override
	public Model remove(StmtIterator iter) {
		return inner.remove(iter);
	}

	@Override
	public Model remove(Model m) {
		return inner.remove(m);
	}

	@Override
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, boolean object) {
		return inner.listLiteralStatements(subject, predicate, object);
	}

	@Override
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, char object) {
		return inner.listLiteralStatements(subject, predicate, object);
	}

	@Override
	public StmtIterator listLiteralStatements(Resource resource, Property property, int object) {
		return inner.listLiteralStatements(resource, property, object);
	}


	@Override
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, long object) {
		return inner.listLiteralStatements(subject, predicate, object);
	}

	@Override
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, float object) {
		return inner.listLiteralStatements(subject, predicate, object);
	}

	@Override
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, double object) {
		return inner.listLiteralStatements(subject, predicate, object);
	}

	@Override
	public StmtIterator listStatements(Resource subject, Property predicate,
			String object) {
		return inner.listStatements(subject, predicate, object);
	}

	@Override
	public StmtIterator listStatements(Resource subject, Property predicate,
			String object, String lang) {
		return inner.listStatements(subject, predicate, object, lang);
	}

	@Override
	public ResIterator listResourcesWithProperty(Property p, boolean o) {
		return inner.listResourcesWithProperty(p, o);
	}

	@Override
	public ResIterator listResourcesWithProperty(Property p, long o) {
		return inner.listResourcesWithProperty(p, o);
	}

	@Override
	public ResIterator listResourcesWithProperty(Property p, char o) {
		return inner.listResourcesWithProperty(p, o);
	}

	@Override
	public ResIterator listResourcesWithProperty(Property p, float o) {
		return inner.listResourcesWithProperty(p, o);
	}

	@Override
	public ResIterator listResourcesWithProperty(Property p, double o) {
		return inner.listResourcesWithProperty(p, o);
	}

	@Override
	public ResIterator listResourcesWithProperty(Property p, Object o) {
		return inner.listResourcesWithProperty(p, o);
	}

	@Override
	public ResIterator listSubjectsWithProperty(Property p, String o) {
		return inner.listSubjectsWithProperty(p, o);
	}

	@Override
	public ResIterator listSubjectsWithProperty(Property p, String o, String l) {
		return inner.listSubjectsWithProperty(p, o, l);
	}

	@Override
	public boolean containsLiteral(Resource s, Property p, boolean o) {
		return inner.containsLiteral(s, p, o);
	}

	@Override
	public boolean containsLiteral(Resource s, Property p, long o) {
		return inner.containsLiteral(s, p, o);
	}

	@Override
	public boolean containsLiteral(Resource s, Property p, int o) {
		return inner.containsLiteral(s, p, o);
	}

	@Override
	public boolean containsLiteral(Resource s, Property p, char o) {
		return inner.containsLiteral(s, p, o);
	}

	@Override
	public boolean containsLiteral(Resource s, Property p, float o) {
		return inner.containsLiteral(s, p, o);
	}

	@Override
	public boolean containsLiteral(Resource s, Property p, double o) {
		return inner.containsLiteral(s, p, o);
	}

	@Override
	public boolean containsLiteral(Resource s, Property p, Object o) {
		return inner.containsLiteral(s, p, o);
	}

	@Override
	public boolean contains(Resource s, Property p, String o) {
		return inner.contains(s, p, o);
	}

	@Override
	public boolean contains(Resource s, Property p, String o, String l) {
		return inner.contains(s, p, o, l);
	}

	@Override
	public void enterCriticalSection(boolean readLockRequested) {
		inner.enterCriticalSection(readLockRequested);
	}

	@Override
	public void leaveCriticalSection() {
		inner.leaveCriticalSection();
	}

	@Override
	public long size() {
		return inner.size();
	}

	@Override
	public boolean isEmpty() {
		return inner.isEmpty();
	}

	@Override
	public ResIterator listSubjects() {
		return inner.listSubjects();
	}

	@Override
	public NsIterator listNameSpaces() {
		return inner.listNameSpaces();
	}

	@Override
	public Resource getResource(String uri) {
		return inner.getResource(uri);
	}

	@Override
	public Property getProperty(String nameSpace, String localName) {
		return inner.getProperty(nameSpace, localName);
	}

	@Override
	public Resource createResource() {
		return inner.createResource();
	}

	@Override
	public Resource createResource(AnonId id) {
		return inner.createResource(id);
	}

	@Override
	public Resource createResource(String uri) {
		return inner.createResource(uri);
	}

	@Override
	public Property createProperty(String nameSpace, String localName) {
		return inner.createProperty(nameSpace, localName);
	}

	@Override
	public Literal createLiteral(String v, String language) {
		return inner.createLiteral(v, language);
	}

	@Override
	public Literal createLiteral(String v, boolean wellFormed) {
		return inner.createLiteral(v, wellFormed);
	}

	@Override
	public Literal createTypedLiteral(String lex, RDFDatatype dtype) {
		return inner.createTypedLiteral(lex, dtype);
	}

	@Override
	public Literal createTypedLiteral(Object value, RDFDatatype dtype) {
		return inner.createTypedLiteral(value, dtype);
	}

	@Override
	public Literal createTypedLiteral(Object value) {
		return inner.createTypedLiteral(value);
	}

	@Override
	public Statement createStatement(Resource s, Property p, RDFNode o) {
		return inner.createStatement(s, p, o);
	}

	@Override
	public RDFList createList() {
		return inner.createList();
	}

	@Override
	public RDFList createList(Iterator<? extends RDFNode> members) {
		return inner.createList(members);
	}

	@Override
	public RDFList createList(RDFNode[] members) {
		return inner.createList(members);
	}

	@Override
	public Model add(Statement s) {
		return inner.add(s);
	}

	@Override
	public Model add(Statement[] statements) {
		return inner.add(statements);
	}

	@Override
	public Model remove(Statement[] statements) {
		return inner.remove(statements);
	}

	@Override
	public Model add(List<Statement> statements) {
		return inner.add(statements);
	}

	@Override
	public Model remove(List<Statement> statements) {
		return inner.remove(statements);
	}

	@Override
	public Model add(StmtIterator iter) {
		return inner.add(iter);
	}

	@Override
	public Model add(Model m) {
		return inner.add(m);
	}

	@Override
	public Model read(String url) {
		return inner.read(url);
	}

	@Override
	public Model read(InputStream in, String base) {
		return inner.read(in, base);
	}

	@Override
	public Model read(InputStream in, String base, String lang) {
		return inner.read(in, base, lang);
	}

	@Override
	public Model read(Reader reader, String base) {
		return inner.read(reader, base);
	}

	@Override
	public Model read(String url, String lang) {
		return inner.read(url, lang);
	}

	@Override
	public Model read(Reader reader, String base, String lang) {
		return inner.read(reader, base, lang);
	}

	@Override
	public Model read(String url, String base, String lang) {
		return inner.read(url, base, lang);
	}

	@Override
	public Model write(Writer writer) {
		return inner.write(writer);
	}

	@Override
	public Model write(Writer writer, String lang) {
		return inner.write(writer, lang);
	}

	@Override
	public Model write(Writer writer, String lang, String base) {
		return inner.write(writer, lang, base);
	}

	@Override
	public Model write(OutputStream out) {
		return inner.write(out);
	}

	@Override
	public Model write(OutputStream out, String lang) {
		return inner.write(out, lang);
	}

	@Override
	public Model write(OutputStream out, String lang, String base) {
		return inner.write(out, lang, base);
	}

	@Override
	public Model remove(Statement s) {
		return inner.remove(s);
	}

	@Override
	public Statement getRequiredProperty(Resource s, Property p) {
		return inner.getRequiredProperty(s, p);
	}

	@Override
	public Statement getProperty(Resource s, Property p) {
		return inner.getProperty(s, p);
	}

	@Override
	public ResIterator listSubjectsWithProperty(Property p) {
		return inner.listSubjectsWithProperty(p);
	}

	@Override
	public ResIterator listResourcesWithProperty(Property p) {
		return inner.listResourcesWithProperty(p);
	}

	@Override
	public ResIterator listSubjectsWithProperty(Property p, RDFNode o) {
		return inner.listSubjectsWithProperty(p, o);
	}

	@Override
	public ResIterator listResourcesWithProperty(Property p, RDFNode o) {
		return inner.listResourcesWithProperty(p, o);
	}

	@Override
	public NodeIterator listObjects() {
		return inner.listObjects();
	}

	@Override
	public NodeIterator listObjectsOfProperty(Property p) {
		return inner.listObjectsOfProperty(p);
	}

	@Override
	public NodeIterator listObjectsOfProperty(Resource s, Property p) {
		return inner.listObjectsOfProperty(s, p);
	}

	@Override
	public boolean contains(Resource s, Property p) {
		return inner.contains(s, p);
	}

	@Override
	public boolean containsResource(RDFNode r) {
		return inner.containsResource(r);
	}

	@Override
	public boolean contains(Resource s, Property p, RDFNode o) {
		return inner.contains(s, p, o);
	}

	@Override
	public boolean contains(Statement s) {
		return inner.contains(s);
	}

	@Override
	public boolean containsAny(StmtIterator iter) {
		return inner.containsAny(iter);
	}

	@Override
	public boolean containsAll(StmtIterator iter) {
		return inner.containsAll(iter);
	}

	@Override
	public boolean containsAny(Model model) {
		return inner.containsAny(model);
	}

	@Override
	public boolean containsAll(Model model) {
		return inner.containsAll(model);
	}

	@Override
	public boolean isReified(Statement s) {
		return inner.isReified(s);
	}

	@Override
	public Resource getAnyReifiedStatement(Statement s) {
		return inner.getAnyReifiedStatement(s);
	}

	@Override
	public void removeAllReifications(Statement s) {
		inner.removeAllReifications(s);
	}

	@Override
	public void removeReification(ReifiedStatement rs) {
		inner.removeReification(rs);
	}

	@Override
	public StmtIterator listStatements() {
		return inner.listStatements();
	}

	@Override
	public StmtIterator listStatements(Selector s) {
		return inner.listStatements(s);
	}

	@Override
	public StmtIterator listStatements(Resource s, Property p, RDFNode o) {
		return inner.listStatements(s, p, o);
	}

	@Override
	public ReifiedStatement createReifiedStatement(Statement s) {
		return inner.createReifiedStatement(s);
	}

	@Override
	public ReifiedStatement createReifiedStatement(String uri, Statement s) {
		return inner.createReifiedStatement(uri, s);
	}

	@Override
	public RSIterator listReifiedStatements() {
		return inner.listReifiedStatements();
	}

	@Override
	public RSIterator listReifiedStatements(Statement st) {
		return inner.listReifiedStatements(st);
	}

	@Override
	public Model query(Selector s) {
		return inner.query(s);
	}

	@Override
	public Model union(Model model) {
		return inner.union(model);
	}

	@Override
	public Model intersection(Model model) {
		return inner.intersection(model);
	}

	@Override
	public Model difference(Model model) {
		return inner.difference(model);
	}

	@Override
	public Model begin() {
		return inner.begin();
	}

	@Override
	public Model abort() {
		return inner.abort();
	}

	@Override
	public Model commit() {
		return inner.commit();
	}

	@Override
	public Object executeInTransaction(Command cmd) {
		return inner.executeInTransaction(cmd);
	}

	@Override
	public boolean independent() {
		return inner.independent();
	}

	@Override
	public boolean supportsTransactions() {
		return inner.supportsTransactions();
	}

	@Override
	public boolean supportsSetOperations() {
		return inner.supportsSetOperations();
	}

	@Override
	public boolean isIsomorphicWith(Model g) {
		return inner.isIsomorphicWith(g);
	}

	@Override
	public void close() {
		inner.close();
	}

	@Override
	public Lock getLock() {
		return inner.getLock();
	}

	@Override
	public Model register(ModelChangedListener listener) {
		return inner.register(listener);
	}

	@Override
	public Model unregister(ModelChangedListener listener) {
		return inner.unregister(listener);
	}

	@Override
	public Model notifyEvent(Object e) {
		return inner.notifyEvent(e);
	}

	@Override
	public Model removeAll() {
		return inner.removeAll();
	}

	@Override
	public Model removeAll(Resource s, Property p, RDFNode r) {
		return inner.removeAll(s, p, r);
	}

	@Override
	public boolean isClosed() {
		return inner.isClosed();
	}

	@Override
	public Model getRawModel() {
		return inner.getRawModel();
	}

	@Override
	public Reasoner getReasoner() {
		return inner.getReasoner();
	}

	@Override
	public void rebind() {
		inner.rebind();
	}

	@Override
	public void prepare() {
		inner.prepare();
	}

	@Override
	public void reset() {
		inner.reset();
	}

	@Override
	public ValidityReport validate() {
		return inner.validate();
	}

	@Override
	public StmtIterator listStatements(Resource subject, Property predicate,
			RDFNode object, Model posit) {
		return inner.listStatements(subject, predicate, object, posit);
	}

	@Override
	public void setDerivationLogging(boolean logOn) {
		inner.setDerivationLogging(logOn);
	}

	@Override
	public Iterator<Derivation> getDerivation(Statement statement) {
		return inner.getDerivation(statement);
	}

	@Override
	public Model getDeductionsModel() {
		return inner.getDeductionsModel();
	}

	@Override
	public ExtendedIterator<Ontology> listOntologies() {
		return inner.listOntologies();
	}

	@Override
	public ExtendedIterator<OntProperty> listOntProperties() {
		return inner.listOntProperties();
	}

	@Override
	public ExtendedIterator<OntProperty> listAllOntProperties() {
		return inner.listAllOntProperties();
	}

	@Override
	public ExtendedIterator<ObjectProperty> listObjectProperties() {
		return inner.listObjectProperties();
	}

	@Override
	public ExtendedIterator<DatatypeProperty> listDatatypeProperties() {
		return inner.listDatatypeProperties();
	}

	@Override
	public ExtendedIterator<FunctionalProperty> listFunctionalProperties() {
		return inner.listFunctionalProperties();
	}

	@Override
	public ExtendedIterator<TransitiveProperty> listTransitiveProperties() {
		return inner.listTransitiveProperties();
	}

	@Override
	public ExtendedIterator<SymmetricProperty> listSymmetricProperties() {
		return inner.listSymmetricProperties();
	}

	@Override
	public ExtendedIterator<InverseFunctionalProperty> listInverseFunctionalProperties() {
		return inner.listInverseFunctionalProperties();
	}

	@Override
	public ExtendedIterator<Individual> listIndividuals() {
		return inner.listIndividuals();
	}

	@Override
	public ExtendedIterator<Individual> listIndividuals(Resource cls) {
		return inner.listIndividuals(cls);
	}

	@Override
	public ExtendedIterator<OntClass> listClasses() {
		return inner.listClasses();
	}

	@Override
	public ExtendedIterator<OntClass> listHierarchyRootClasses() {
		return inner.listHierarchyRootClasses();
	}

	@Override
	public ExtendedIterator<EnumeratedClass> listEnumeratedClasses() {
		return inner.listEnumeratedClasses();
	}

	@Override
	public ExtendedIterator<UnionClass> listUnionClasses() {
		return inner.listUnionClasses();
	}

	@Override
	public ExtendedIterator<ComplementClass> listComplementClasses() {
		return inner.listComplementClasses();
	}

	@Override
	public ExtendedIterator<IntersectionClass> listIntersectionClasses() {
		return inner.listIntersectionClasses();
	}

	@Override
	public ExtendedIterator<OntClass> listNamedClasses() {
		return inner.listNamedClasses();
	}

	@Override
	public ExtendedIterator<Restriction> listRestrictions() {
		return inner.listRestrictions();
	}

	@Override
	public ExtendedIterator<AnnotationProperty> listAnnotationProperties() {
		return inner.listAnnotationProperties();
	}

	@Override
	public ExtendedIterator<AllDifferent> listAllDifferent() {
		return inner.listAllDifferent();
	}

	@Override
	public ExtendedIterator<DataRange> listDataRanges() {
		return inner.listDataRanges();
	}

	@Override
	public Ontology getOntology(String uri) {
		return inner.getOntology(uri);
	}

	@Override
	public Individual getIndividual(String uri) {
		return inner.getIndividual(uri);
	}

	@Override
	public OntProperty getOntProperty(String uri) {
		return inner.getOntProperty(uri);
	}

	@Override
	public ObjectProperty getObjectProperty(String uri) {
		return inner.getObjectProperty(uri);
	}

	@Override
	public TransitiveProperty getTransitiveProperty(String uri) {
		return inner.getTransitiveProperty(uri);
	}

	@Override
	public SymmetricProperty getSymmetricProperty(String uri) {
		return inner.getSymmetricProperty(uri);
	}

	@Override
	public InverseFunctionalProperty getInverseFunctionalProperty(String uri) {
		return inner.getInverseFunctionalProperty(uri);
	}

	@Override
	public DatatypeProperty getDatatypeProperty(String uri) {
		return inner.getDatatypeProperty(uri);
	}

	@Override
	public AnnotationProperty getAnnotationProperty(String uri) {
		return inner.getAnnotationProperty(uri);
	}

	@Override
	public OntResource getOntResource(String uri) {
		return inner.getOntResource(uri);
	}

	@Override
	public OntResource getOntResource(Resource res) {
		return inner.getOntResource(res);
	}

	@Override
	public OntClass getOntClass(String uri) {
		return inner.getOntClass(uri);
	}

	@Override
	public ComplementClass getComplementClass(String uri) {
		return inner.getComplementClass(uri);
	}

	@Override
	public EnumeratedClass getEnumeratedClass(String uri) {
		return inner.getEnumeratedClass(uri);
	}

	@Override
	public UnionClass getUnionClass(String uri) {
		return inner.getUnionClass(uri);
	}

	@Override
	public IntersectionClass getIntersectionClass(String uri) {
		return inner.getIntersectionClass(uri);
	}

	@Override
	public Restriction getRestriction(String uri) {
		return inner.getRestriction(uri);
	}

	@Override
	public HasValueRestriction getHasValueRestriction(String uri) {
		return inner.getHasValueRestriction(uri);
	}

	@Override
	public SomeValuesFromRestriction getSomeValuesFromRestriction(String uri) {
		return inner.getSomeValuesFromRestriction(uri);
	}

	@Override
	public AllValuesFromRestriction getAllValuesFromRestriction(String uri) {
		return inner.getAllValuesFromRestriction(uri);
	}

	@Override
	public CardinalityRestriction getCardinalityRestriction(String uri) {
		return inner.getCardinalityRestriction(uri);
	}

	@Override
	public MinCardinalityRestriction getMinCardinalityRestriction(String uri) {
		return inner.getMinCardinalityRestriction(uri);
	}

	@Override
	public MaxCardinalityRestriction getMaxCardinalityRestriction(String uri) {
		return inner.getMaxCardinalityRestriction(uri);
	}

	@Override
	public QualifiedRestriction getQualifiedRestriction(String uri) {
		return inner.getQualifiedRestriction(uri);
	}

	@Override
	public CardinalityQRestriction getCardinalityQRestriction(String uri) {
		return inner.getCardinalityQRestriction(uri);
	}

	@Override
	public MinCardinalityQRestriction getMinCardinalityQRestriction(String uri) {
		return inner.getMinCardinalityQRestriction(uri);
	}

	@Override
	public MaxCardinalityQRestriction getMaxCardinalityQRestriction(String uri) {
		return inner.getMaxCardinalityQRestriction(uri);
	}

	@Override
	public Ontology createOntology(String uri) {
		return inner.createOntology(uri);
	}

	@Override
	public Individual createIndividual(Resource cls) {
		return inner.createIndividual(cls);
	}

	@Override
	public Individual createIndividual(String uri, Resource cls) {
		return inner.createIndividual(uri, cls);
	}

	@Override
	public OntProperty createOntProperty(String uri) {
		return inner.createOntProperty(uri);
	}

	@Override
	public ObjectProperty createObjectProperty(String uri) {
		return inner.createObjectProperty(uri);
	}

	@Override
	public ObjectProperty createObjectProperty(String uri, boolean functional) {
		return inner.createObjectProperty(uri, functional);
	}

	@Override
	public TransitiveProperty createTransitiveProperty(String uri) {
		return inner.createTransitiveProperty(uri);
	}

	@Override
	public TransitiveProperty createTransitiveProperty(String uri,
			boolean functional) {
		return inner.createTransitiveProperty(uri, functional);
	}

	@Override
	public SymmetricProperty createSymmetricProperty(String uri) {
		return inner.createSymmetricProperty(uri);
	}

	@Override
	public SymmetricProperty createSymmetricProperty(String uri,
			boolean functional) {
		return inner.createSymmetricProperty(uri, functional);
	}

	@Override
	public InverseFunctionalProperty createInverseFunctionalProperty(String uri) {
		return inner.createInverseFunctionalProperty(uri);
	}

	@Override
	public InverseFunctionalProperty createInverseFunctionalProperty(
			String uri, boolean functional) {
		return inner.createInverseFunctionalProperty(uri, functional);
	}

	@Override
	public DatatypeProperty createDatatypeProperty(String uri) {
		return inner.createDatatypeProperty(uri);
	}

	@Override
	public DatatypeProperty createDatatypeProperty(String uri,
			boolean functional) {
		return inner.createDatatypeProperty(uri, functional);
	}

	@Override
	public AnnotationProperty createAnnotationProperty(String uri) {
		return inner.createAnnotationProperty(uri);
	}

	@Override
	public OntClass createClass() {
		return inner.createClass();
	}

	@Override
	public OntClass createClass(String uri) {
		return inner.createClass(uri);
	}

	@Override
	public ComplementClass createComplementClass(String uri, Resource cls) {
		return inner.createComplementClass(uri, cls);
	}

	@Override
	public EnumeratedClass createEnumeratedClass(String uri, RDFList members) {
		return inner.createEnumeratedClass(uri, members);
	}

	@Override
	public UnionClass createUnionClass(String uri, RDFList members) {
		return inner.createUnionClass(uri, members);
	}

	@Override
	public IntersectionClass createIntersectionClass(String uri, RDFList members) {
		return inner.createIntersectionClass(uri, members);
	}

	@Override
	public Restriction createRestriction(Property p) {
		return inner.createRestriction(p);
	}

	@Override
	public Restriction createRestriction(String uri, Property p) {
		return inner.createRestriction(uri, p);
	}

	@Override
	public HasValueRestriction createHasValueRestriction(String uri,
			Property prop, RDFNode value) {
		return inner.createHasValueRestriction(uri, prop, value);
	}

	@Override
	public SomeValuesFromRestriction createSomeValuesFromRestriction(
			String uri, Property prop, Resource cls) {
		return inner.createSomeValuesFromRestriction(uri, prop, cls);
	}

	@Override
	public AllValuesFromRestriction createAllValuesFromRestriction(String uri,
			Property prop, Resource cls) {
		return inner.createAllValuesFromRestriction(uri, prop, cls);
	}

	@Override
	public CardinalityRestriction createCardinalityRestriction(String uri,
			Property prop, int cardinality) {
		return inner.createCardinalityRestriction(uri, prop, cardinality);
	}

	@Override
	public MinCardinalityRestriction createMinCardinalityRestriction(
			String uri, Property prop, int cardinality) {
		return inner.createMinCardinalityRestriction(uri, prop, cardinality);
	}

	@Override
	public MaxCardinalityRestriction createMaxCardinalityRestriction(
			String uri, Property prop, int cardinality) {
		return inner.createMaxCardinalityRestriction(uri, prop, cardinality);
	}

	@Override
	public MaxCardinalityQRestriction createMaxCardinalityQRestriction(
			String uri, Property prop, int cardinality, OntClass cls) {
		return inner.createMaxCardinalityQRestriction(uri, prop, cardinality,
				cls);
	}

	@Override
	public MinCardinalityQRestriction createMinCardinalityQRestriction(
			String uri, Property prop, int cardinality, OntClass cls) {
		return inner.createMinCardinalityQRestriction(uri, prop, cardinality,
				cls);
	}

	@Override
	public CardinalityQRestriction createCardinalityQRestriction(String uri,
			Property prop, int cardinality, OntClass cls) {
		return inner.createCardinalityQRestriction(uri, prop, cardinality, cls);
	}

	@Override
	public DataRange createDataRange(RDFList literals) {
		return inner.createDataRange(literals);
	}

	@Override
	public AllDifferent createAllDifferent() {
		return inner.createAllDifferent();
	}

	@Override
	public AllDifferent createAllDifferent(RDFList differentMembers) {
		return inner.createAllDifferent(differentMembers);
	}

	@Override
	public <T extends OntResource> T createOntResource(Class<T> javaClass,
			Resource rdfType, String uri) {
		return inner.createOntResource(javaClass, rdfType, uri);
	}

	@Override
	public OntResource createOntResource(String uri) {
		return inner.createOntResource(uri);
	}

	@Override
	public void loadImports() {
		inner.loadImports();
	}

	@Override
	public Set<String> listImportedOntologyURIs() {
		return inner.listImportedOntologyURIs();
	}

	@Override
	public Set<String> listImportedOntologyURIs(boolean closure) {
		return inner.listImportedOntologyURIs(closure);
	}

	@Override
	public boolean hasLoadedImport(String uri) {
		return inner.hasLoadedImport(uri);
	}

	@Override
	public void addLoadedImport(String uri) {
		inner.addLoadedImport(uri);
	}

	@Override
	public void removeLoadedImport(String uri) {
		inner.removeLoadedImport(uri);
	}

	@Override
	public Profile getProfile() {
		return inner.getProfile();
	}

	@Override
	@Deprecated
	public ModelMaker getModelMaker() {
		return inner.getModelMaker();
	}

	@Override
	public ModelMaker getImportModelMaker() {
		return inner.getImportModelMaker();
	}

	@Override
	public List<Graph> getSubGraphs() {
		return inner.getSubGraphs();
	}

	@Override
	@Deprecated
	public ExtendedIterator<OntModel> listImportedModels() {
		return inner.listImportedModels();
	}

	@Override
	public ExtendedIterator<OntModel> listSubModels(boolean withImports) {
		return inner.listSubModels(withImports);
	}

	@Override
	public ExtendedIterator<OntModel> listSubModels() {
		return inner.listSubModels();
	}

	@Override
	public int countSubModels() {
		return inner.countSubModels();
	}

	@Override
	public OntModel getImportedModel(String uri) {
		return inner.getImportedModel(uri);
	}

	@Override
	public Model getBaseModel() {
		return inner.getBaseModel();
	}

	@Override
	public void addSubModel(Model model) {
		inner.addSubModel(model);
	}

	@Override
	public void addSubModel(Model model, boolean rebind) {
		inner.addSubModel(model, rebind);
	}

	@Override
	public void removeSubModel(Model model) {
		inner.removeSubModel(model);
	}

	@Override
	public void removeSubModel(Model model, boolean rebind) {
		inner.removeSubModel(model, rebind);
	}

	@Override
	public boolean isInBaseModel(RDFNode node) {
		return inner.isInBaseModel(node);
	}

	@Override
	public boolean isInBaseModel(Statement stmt) {
		return inner.isInBaseModel(stmt);
	}

	@Override
	public boolean strictMode() {
		return inner.strictMode();
	}

	@Override
	public void setStrictMode(boolean strict) {
		inner.setStrictMode(strict);
	}

	@Override
	public void setDynamicImports(boolean dynamic) {
		inner.setDynamicImports(dynamic);
	}

	@Override
	public boolean getDynamicImports() {
		return inner.getDynamicImports();
	}

	@Override
	public OntDocumentManager getDocumentManager() {
		return inner.getDocumentManager();
	}

	@Override
	public OntModelSpec getSpecification() {
		return inner.getSpecification();
	}

	@Override
	public Model writeAll(Writer writer, String s) {
		return inner.writeAll(writer, s);
	}

	@Override
	public Model writeAll(OutputStream outputStream, String s) {
		return inner.writeAll(outputStream, s);
	}

	@Override
	public Model writeAll(Writer writer, String lang, String base) {
		return inner.writeAll(writer, lang, base);
	}

	@Override
	public Model writeAll(OutputStream out, String lang, String base) {
		return inner.writeAll(out, lang, base);
	}

	@Override
	public Statement getRequiredProperty(Resource resource, Property property, String s) {
		return inner.getRequiredProperty(resource, property, s);
	}

	@Override
	public Statement getProperty(Resource resource, Property property, String s) {
		return inner.getProperty(resource, property, s);
	}

	@Override
	public void executeInTxn(Runnable runnable) {
		inner.executeInTxn(runnable);
	}

	@Override
	public <T> T calculateInTxn(Supplier<T> supplier) {
		return inner.calculateInTxn(supplier);
	}

	@Override
	public PrefixMapping clearNsPrefixMap() {
		return inner.clearNsPrefixMap();
	}

	@Override
	public int numPrefixes() {
		return inner.numPrefixes();
	}
}
