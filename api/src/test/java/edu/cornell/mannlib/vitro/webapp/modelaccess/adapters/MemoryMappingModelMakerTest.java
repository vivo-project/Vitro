/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import stubs.org.apache.jena.rdf.model.ModelMaker.ModelMakerStub;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Checks the function of a MemoryMappingModelMaker, testing the difference in
 * behavior between an mapped model and an unmapped model.
 */
public class MemoryMappingModelMakerTest extends AbstractTestClass {
	private static final String URI_MAPPED = "http://memory.mapped.model";
	private static final String URI_UNMAPPED = "http://unmapped.model";
	private static final String MODEL_CONTENTS = "@prefix : <http://z#> . \n"
			+ ":a :b :c .";

	private GraphModelStructure unmapped;
	private GraphModelStructure mapped;
	private ModelMakerStub innerModelMaker;
	private MemoryMappingModelMaker mmmm;

	@Before
	public void setup() {
		unmapped = new GraphModelStructure(URI_UNMAPPED, MODEL_CONTENTS);
		mapped = new GraphModelStructure(URI_MAPPED, MODEL_CONTENTS);

		innerModelMaker = ModelMakerStub.rigorous(createModel(), createModel());
		innerModelMaker.put(mapped.uri, mapped.model);
		innerModelMaker.put(unmapped.uri, unmapped.model);

		mmmm = new MemoryMappingModelMaker(innerModelMaker, mapped.uri);

		unmapped.methodCalls.clear();
		mapped.methodCalls.clear();
	}

	// ----------------------------------------------------------------------
	// tests
	// ----------------------------------------------------------------------

	@Test
	public void unmappedRead() {
		assertModelContents(unmapped, "[http://z#a, http://z#b, http://z#c]");
		assertMethodCalls(unmapped, "find");
	}

	@Test
	public void mappedRead() {
		assertModelContents(mapped, "[http://z#a, http://z#b, http://z#c]");
		assertMethodCalls(mapped);
	}

	@Test
	public void unmappedWrite() {
		mmmm.openModel(URI_UNMAPPED).add(newStatement());
		assertModelContents(unmapped, "[http://z#a, http://z#b, http://z#c]",
				"[http://z#new, http://z#to, http://z#you]");
		assertMethodCalls(unmapped, "add", "find");
	}

	@Test
	public void mappedWrite() {
		mmmm.openModel(URI_MAPPED).add(newStatement());
		assertModelContents(mapped, "[http://z#a, http://z#b, http://z#c]",
				"[http://z#new, http://z#to, http://z#you]");
		assertMethodCalls(mapped, "add");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private static Model createModel() {
		return ModelFactory.createDefaultModel();
	}

	private void assertModelContents(GraphModelStructure gms,
			String... expected) {
		Set<Statement> stmts = mmmm.openModel(gms.uri).listStatements().toSet();
		assertStatements(stmts, expected);
	}

	private void assertStatements(Set<Statement> stmts, String... expected) {
		Set<String> actual = new HashSet<>();
		for (Statement stmt : stmts) {
			actual.add(stmt.toString());
		}
		assertEquals(new HashSet<>(Arrays.asList(expected)), actual);
	}

	private void assertMethodCalls(GraphModelStructure gms, String... expected) {
		assertEquals(Arrays.asList(expected), gms.methodCalls);
	}

	public Statement newStatement() {
		Resource s = ResourceFactory.createResource("http://z#new");
		Property p = ResourceFactory.createProperty("http://z#to");
		Resource o = ResourceFactory.createResource("http://z#you");
		return ResourceFactory.createStatement(s, p, o);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class GraphModelStructure {
		final String uri;
		final Graph graph;
		final List<String> methodCalls;
		final RecordingInvocationHandler handler;
		final Graph proxy;
		final Model model;

		public GraphModelStructure(String uri, String contents) {
			this.uri = uri;
			graph = new CollectionGraph();
			methodCalls = new ArrayList<>();
			handler = new RecordingInvocationHandler(graph, methodCalls);
			proxy = wrapGraph();
			model = ModelFactory.createModelForGraph(proxy);
			model.read(new StringReader(contents), null, "TURTLE");
		}

		private Graph wrapGraph() {
			ClassLoader classLoader = Model.class.getClassLoader();
			Class<?>[] interfaces = new Class<?>[] { Graph.class };
			return (Graph) Proxy.newProxyInstance(classLoader, interfaces,
					handler);
		}
	}

	private static class RecordingInvocationHandler implements
			InvocationHandler {
		private final Object inner;
		private final List<String> methodCalls;

		public RecordingInvocationHandler(Object inner, List<String> methodCalls) {
			this.inner = inner;
			this.methodCalls = methodCalls;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			methodCalls.add(method.getName());
			return method.invoke(inner, args);
		}
	}
}
