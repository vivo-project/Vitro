/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import static org.apache.jena.ontology.OntModelSpec.OWL_MEM;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.apache.jena.graph.impl.GraphWithPerform;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.testing.RecordingProxy;
import edu.cornell.mannlib.vitro.testing.RecordingProxy.MethodCall;
import edu.cornell.mannlib.vitro.testing.RecordingProxy.MethodCallRecorder;

/**
 * Test that the VitroModelFactory is doing what we want, with regard to bulk
 * updates.
 * 
 * With the switch to Jena 2.10, bulk update operations are deprecated, but
 * still supported, to a large extent. A Graph still has a bulk updater which
 * can be called for bulk operations (like adding multiple statements). However,
 * the default Model won't call the bulk updater of its Graph, and neither will
 * the default OntModel.
 * 
 * VitroModelFactory creates Models and OntModels that do call the bulk updaters
 * of their respective graphs.
 * 
 * ---------------
 * 
 * These tests show which methods are called on which objects (graph, model,
 * listener) for both simple operations (add a statement) and bulk operations
 * (add multiple statements).
 * 
 * The tests of the default ModelFactory aren't necessary. They do add
 * confidence to the testing mechanism, and provide a contrast with the
 * VitroModelFactory.
 * 
 * The tests of simple operations may or may not add value. Probably good to
 * keep them.
 * 
 * ----------------
 * 
 * Who knows how we will deal with this in the next Jena upgrade, when
 * presumably the bulk updaters will be removed completely.
 */
public class VitroModelFactoryTest extends AbstractTestClass {
	private static final Statement SINGLE_STATEMENT = stmt(
			resource("http://subject"), property("http://add"),
			literal("object"));
	private static final Statement[] MULTIPLE_STATEMENTS = {
			stmt(resource("http://subject"), property("http://add"),
					literal("first")),
			stmt(resource("http://subject"), property("http://add"),
					literal("second")) };

	private static final String[] BORING_METHOD_NAMES = { "getPrefixMapping",
			"getEventManager", "getBulkUpdateHandler", "find", "getGraph" };

	// ----------------------------------------------------------------------
	// createModelForGraph()
	// ----------------------------------------------------------------------

	/**
	 * A ModelGroup has a talkative graph, with a talkative bulkUpdater, wrapped
	 * by a model that has a talkative listener attached.
	 * 
	 * But what kind of model?
	 */
	private static abstract class ModelGroup extends TestObjectGrouping {
		final GraphWithPerform g;
		final ModelChangedListener l;
		final Model m;

		protected ModelGroup() {
			GraphMem rawGraph = new GraphMem();
			this.g = wrapGraph(rawGraph);
			this.l = makeListener();

			this.m = wrapModel(makeModel(this.g));
			this.m.register(this.l);

			reset(g);
			reset(l);
			reset(m);
		}

		protected abstract Model makeModel(GraphWithPerform g);
	}

	/** A ModelGroup with a default-style model. */
	private static class DefaultModelGroup extends ModelGroup {
		@Override
		protected Model makeModel(GraphWithPerform g) {
			return ModelFactory.createModelForGraph(g);
		}
	}

	/** A ModelGroup with a Vitro-style model. */
	private static class VitroModelGroup extends ModelGroup {
		@Override
		protected Model makeModel(GraphWithPerform g) {
			return VitroModelFactory.createModelForGraph(g);
		}
	}

	private ModelGroup mg;

	@Test
	public void addOneToModel() {
		mg = new DefaultModelGroup();
		mg.m.add(SINGLE_STATEMENT);
		new MethodCalls().add(mg.g, "add").add(mg.l, "addedStatement").test();
	}

	@Test
	public void addOneToVitroModel() {
		mg = new VitroModelGroup();
		mg.m.add(SINGLE_STATEMENT);
		new MethodCalls().add(mg.g, "add").add(mg.l, "addedStatement").test();
	}

	@Test
	public void addMultipleToModel() {
		mg = new DefaultModelGroup();
		mg.m.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(mg.g, "performAdd", "performAdd").add(mg.l, "addedStatements").test();
	}

	@Test
	public void addMultipleToVitroModel() {
		mg = new VitroModelGroup();
		mg.m.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(mg.g, "performAdd", "performAdd")
				.add(mg.l, "addedStatements").test();
	}

	// ----------------------------------------------------------------------
	// createOntologyModel()
	// ----------------------------------------------------------------------

	private OntModelGroup omg;

	/**
	 * An OntModelGroup is like a ModelGroup, but the model is wrapped in an
	 * OntModel that has its own talkative listener.
	 * 
	 * But what kind of Model, and what kind of OntModel?
	 */
	private static abstract class OntModelGroup extends ModelGroup {
		final ModelChangedListener ol;
		final OntModel om;

		protected OntModelGroup() {
			this.ol = makeListener();
			this.om = wrapOntModel(makeOntModel(this.m));
			this.om.register(this.ol);
		}

		protected abstract OntModel makeOntModel(Model m);

	}

	/**
	 * An OntModelGroup with a default-style OntModel and a default-style Model.
	 */
	private static class DefaultOntModelGroup extends OntModelGroup {
		@Override
		protected OntModel makeOntModel(Model m) {
			return ModelFactory.createOntologyModel(OWL_MEM, m);

		}

		@Override
		protected Model makeModel(GraphWithPerform g) {
			return ModelFactory.createModelForGraph(g);
		}
	}

	/**
	 * An OntModelGroup with a Vitro-style OntModel and a Vitro-style Model.
	 */
	private static class VitroOntModelGroup extends OntModelGroup {
		@Override
		protected OntModel makeOntModel(Model m) {
			return VitroModelFactory.createOntologyModel(m);

		}

		@Override
		protected Model makeModel(GraphWithPerform g) {
			return VitroModelFactory.createModelForGraph(g);
		}
	}

	@Test
	public void addOneToOntModel() {
		omg = new DefaultOntModelGroup();
		omg.om.add(SINGLE_STATEMENT);
		new MethodCalls().add(omg.g, "add")
				.add(omg.l, "addedStatement").add(omg.ol, "addedStatement")
				.test();
	}

	@Test
	public void addOneToVitroOntModel() {
		omg = new VitroOntModelGroup();
		omg.om.add(SINGLE_STATEMENT);
		new MethodCalls().add(omg.g, "add")
				.add(omg.l, "addedStatement").add(omg.ol, "addedStatement")
				.test();
	}

	@Test
	public void addMultipleToOntModel() {
		omg = new DefaultOntModelGroup();
		omg.om.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(omg.g, "add", "add")
				.add(omg.l, "addedStatement", "addedStatement")
				.add(omg.ol, "addedStatements").test();
	}

	@Test
	public void addMultipleToVitroOntModel() {
		omg = new VitroOntModelGroup();
		omg.om.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(omg.g, "add", "add")
				.add(omg.l, "addedStatement", "addedStatement")
				.add(omg.ol, "addedStatements").test();
	}

	// ----------------------------------------------------------------------
	// createUnion(Model, Model)
	// ----------------------------------------------------------------------

	/**
	 * A UnionModelGroup is two ModelGroups, joined into a union that has its
	 * own talkative listener.
	 * 
	 * But what kind of ModelGroup, and what kind of union?
	 */
	private abstract static class UnionModelGroup extends TestObjectGrouping {
		final ModelGroup base;
		final ModelGroup plus;
		final Model m;
		final ModelChangedListener l;

		protected UnionModelGroup() {
			this.base = makeModelGroup();
			this.plus = makeModelGroup();
			this.m = wrapModel(makeUnion(this.base.m, this.plus.m));

			this.l = makeListener();
			this.m.register(this.l);

		}

		protected abstract ModelGroup makeModelGroup();

		protected abstract Model makeUnion(Model baseModel, Model plusModel);
	}

	/**
	 * A UnionModelGroup with default-style Models and a default-style union.
	 */
	private static class DefaultUnionModelGroup extends UnionModelGroup {
		@Override
		protected ModelGroup makeModelGroup() {
			return new DefaultModelGroup();
		}

		@Override
		protected Model makeUnion(Model baseModel, Model plusModel) {
			return ModelFactory.createUnion(baseModel, plusModel);
		}
	}

	/**
	 * A UnionModelGroup with Vitro-style Models and a Vitro-style union.
	 */
	private static class VitroUnionModelGroup extends UnionModelGroup {
		@Override
		protected ModelGroup makeModelGroup() {
			return new VitroModelGroup();
		}

		@Override
		protected Model makeUnion(Model baseModel, Model plusModel) {
			return VitroModelFactory.createUnion(baseModel, plusModel);
		}
	}

	private UnionModelGroup umg;

	@Test
	public void addOneToUnion() {
		umg = new DefaultUnionModelGroup();
		umg.m.add(SINGLE_STATEMENT);
		new MethodCalls().add(umg.base.g, "add")
				.add(umg.base.l, "addedStatement").add(umg.plus.g)
				.add(umg.plus.l).add(umg.l, "addedStatement")
				.test();
	}

	@Test
	public void addOneToVitroUnion() {
		umg = new VitroUnionModelGroup();
		umg.m.add(SINGLE_STATEMENT);
		new MethodCalls().add(umg.base.g, "add")
				.add(umg.base.l, "addedStatement").add(umg.plus.g)
				.add(umg.plus.l).add(umg.l, "addedStatement")
				.test();
	}

	@Test
	public void addMultipleToUnion() {
		umg = new DefaultUnionModelGroup();
		umg.m.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(umg.base.g, "add", "add")
				.add(umg.base.l, "addedStatement", "addedStatement")
				.add(umg.plus.g).add(umg.plus.l)
				.add(umg.l, "addedStatements").test();
	}

	@Test
	public void addMultipleToVitroUnion() {
		umg = new VitroUnionModelGroup();
		umg.m.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(umg.base.g, "add", "add")
				.add(umg.base.l, "addedStatement", "addedStatement")
				.add(umg.plus.g).add(umg.plus.l)
				.add(umg.l, "addedStatements").test();
	}

	// ----------------------------------------------------------------------
	// createUnion(OntModel, OntModel)
	// ----------------------------------------------------------------------

	/**
	 * A UnionOntModelGroup is two OntModelGroups, joined into a union that has
	 * its own talkative listener.
	 * 
	 * But what kind of OntModelGroup, and what kind of union?
	 */
	private abstract static class UnionOntModelGroup extends TestObjectGrouping {
		final OntModelGroup base;
		final OntModelGroup plus;
		final OntModel om;
		final ModelChangedListener l;

		protected UnionOntModelGroup() {
			this.base = makeOntModelGroup();
			this.plus = makeOntModelGroup();
			this.om = wrapOntModel(makeOntUnion(this.base.om, this.plus.om));

			this.l = makeListener();
			this.om.register(this.l);

		}

		protected abstract OntModelGroup makeOntModelGroup();

		protected abstract OntModel makeOntUnion(OntModel baseModel,
				OntModel plusModel);
	}

	/**
	 * A UnionOntModelGroup with default-style OntModels and a default-style
	 * union.
	 */
	private static class DefaultUnionOntModelGroup extends UnionOntModelGroup {
		@Override
		protected OntModelGroup makeOntModelGroup() {
			return new DefaultOntModelGroup();
		}

		@Override
		protected OntModel makeOntUnion(OntModel baseModel, OntModel plusModel) {
			return ModelFactory.createOntologyModel(OWL_MEM,
					ModelFactory.createUnion(baseModel, plusModel));
		}
	}

	/**
	 * A UnionOntModelGroup with Vitro-style OntModels and a Vitro-style union.
	 */
	private static class VitroUnionOntModelGroup extends UnionOntModelGroup {
		@Override
		protected OntModelGroup makeOntModelGroup() {
			return new VitroOntModelGroup();
		}

		@Override
		protected OntModel makeOntUnion(OntModel baseModel, OntModel plusModel) {
			return VitroModelFactory.createUnion(baseModel, plusModel);
		}
	}

	private UnionOntModelGroup uomg;

	@Test
	public void addOneToOntUnion() {
		uomg = new DefaultUnionOntModelGroup();
		uomg.om.add(SINGLE_STATEMENT);
		new MethodCalls().add(uomg.base.g, "add")
				.add(uomg.base.l, "addedStatement").add(uomg.plus.g)
				.add(uomg.plus.l)
				.add(uomg.l, "addedStatement").test();
	}

	@Test
	public void addOneToVitroOntUnion() {
		uomg = new VitroUnionOntModelGroup();
		uomg.om.add(SINGLE_STATEMENT);
		new MethodCalls().add(uomg.base.g, "add")
				.add(uomg.base.l, "addedStatement").add(uomg.plus.g)
				.add(uomg.plus.l)
				.add(uomg.l, "addedStatement").test();
	}

	@Test
	public void addMultipleToOntUnion() {
		uomg = new DefaultUnionOntModelGroup();
		uomg.om.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(uomg.base.g, "add", "add")
				.add(uomg.base.l, "addedStatement", "addedStatement")
				.add(uomg.plus.g).add(uomg.plus.l)
				.add(uomg.l, "addedStatements").test();
	}

	@Test
	public void addMultipleToVitroOntUnion() {
		uomg = new VitroUnionOntModelGroup();
		uomg.om.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(uomg.base.g, "add", "add")
				.add(uomg.base.l, "addedStatement", "addedStatement")
				.add(uomg.plus.g).add(uomg.plus.l)
				.add(uomg.l, "addedStatements").test();
	}

	// ----------------------------------------------------------------------
	// OntModel of Union of Models
	//
	// This shouldn't hold any surprises, should it?
	// ----------------------------------------------------------------------

	/**
	 * A OntModelUnionModelGroup is a UnionModelGroup wrapped by an OntModel
	 * with a listener.
	 * 
	 * But what kind of UnionModelGroup, and what kind of OntModel?
	 */
	private abstract static class OntModelUnionModelGroup extends
			TestObjectGrouping {
		final UnionModelGroup union;
		final OntModel om;
		final ModelChangedListener ol;

		protected OntModelUnionModelGroup() {
			this.union = makeUnionModelGroup();
			this.om = wrapOntModel(makeOntModel(union.m));

			this.ol = makeListener();
			this.om.register(this.ol);
			reset(om);
		}

		protected abstract UnionModelGroup makeUnionModelGroup();

		protected abstract OntModel makeOntModel(Model m);
	}

	/**
	 * A OntModelUnionModelGroup with default-style UnionModelGroup and a
	 * default-style OntModel.
	 */
	private static class DefaultOntModelUnionModelGroup extends
			OntModelUnionModelGroup {
		@Override
		protected UnionModelGroup makeUnionModelGroup() {
			return new DefaultUnionModelGroup();
		}

		@Override
		protected OntModel makeOntModel(Model m) {
			return ModelFactory.createOntologyModel(OWL_MEM, m);
		}
	}

	/**
	 * A OntModelUnionModelGroup with Vitro-style UnionModelGroup and a
	 * Vitro-style OntModel.
	 */
	private static class VitroOntModelUnionModelGroup extends
			OntModelUnionModelGroup {
		@Override
		protected UnionModelGroup makeUnionModelGroup() {
			return new VitroUnionModelGroup();
		}

		@Override
		protected OntModel makeOntModel(Model m) {
			return VitroModelFactory.createOntologyModel(m);
		}
	}

	private OntModelUnionModelGroup omumg;

	@Test
	public void addOneToOntModeledUnionModel() {
		omumg = new DefaultOntModelUnionModelGroup();
		omumg.om.add(SINGLE_STATEMENT);
		new MethodCalls().add(omumg.om, "add").add(omumg.ol, "addedStatement")
				.add(omumg.union.base.g, "add")
				.add(omumg.union.base.m)
				.add(omumg.union.base.l, "addedStatement")
				.add(omumg.union.plus.g)
				.add(omumg.union.plus.m).add(omumg.union.plus.l).test();
	}

	@Test
	public void addOneToVitroOntModeledUnionModel() {
		omumg = new VitroOntModelUnionModelGroup();
		omumg.om.add(SINGLE_STATEMENT);
		new MethodCalls().add(omumg.om, "add").add(omumg.ol, "addedStatement")
				.add(omumg.union.base.g, "add")
				.add(omumg.union.base.m)
				.add(omumg.union.base.l, "addedStatement")
				.add(omumg.union.plus.g)
				.add(omumg.union.plus.m).add(omumg.union.plus.l).test();
	}

	@Test
	public void addMultipleToOntModeledUnionModel() {
		omumg = new DefaultOntModelUnionModelGroup();
		omumg.om.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(omumg.om, "add").add(omumg.ol, "addedStatements")
				.add(omumg.union.base.g, "add", "add")
				.add(omumg.union.base.m)
				.add(omumg.union.base.l, "addedStatement", "addedStatement")
				.add(omumg.union.plus.g)
				.add(omumg.union.plus.m).add(omumg.union.plus.l).test();
	}

	@Test
	public void addMultipleToVitroOntModeledUnionModel() {
		omumg = new VitroOntModelUnionModelGroup();
		omumg.om.add(MULTIPLE_STATEMENTS);
		new MethodCalls().add(omumg.om, "add").add(omumg.ol, "addedStatements")
				.add(omumg.union.base.g, "add", "add")
				.add(omumg.union.base.m)
				.add(omumg.union.base.l, "addedStatement", "addedStatement")
				.add(omumg.union.plus.g)
				.add(omumg.union.plus.m).add(omumg.union.plus.l).test();
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private static Statement stmt(Resource subject, Property predicate,
			RDFNode object) {
		return ResourceFactory.createStatement(subject, predicate, object);
	}

	private static Resource resource(String uri) {
		return ResourceFactory.createResource(uri);
	}

	private static Property property(String uri) {
		return ResourceFactory.createProperty(uri);
	}

	private static Literal literal(String value) {
		return ResourceFactory.createPlainLiteral(value);
	}

	/** Just for debugging */
	private void dumpMethodCalls(String message, Object proxy) {
		System.out.println(message + " method calls:");
		for (MethodCall call : ((MethodCallRecorder) proxy).getMethodCalls()) {
			String formatted = "   " + call.getName();
			for (Object arg : call.getArgList()) {
				formatted += "  " + arg.getClass();
			}
			System.out.println(formatted);
		}

	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * A collection of "CallNames", each of which holds a list of expected
	 * calls, a recording proxy from which we can get the actual calls, and a
	 * method to compare them.
	 */
	private static class MethodCalls {
		private final List<CallNames> list = new ArrayList<>();

		public MethodCalls add(Object proxy, String... names) {
			list.add(new CallNames((MethodCallRecorder) proxy, names));
			return this;
		}

		/**
		 * Create a string that represents all of the expected method calls.
		 * Create a string that represents all of the interesting actual calls.
		 * Compare the strings.
		 */
		private void test() {
			try (StringWriter expectSw = new StringWriter();
					PrintWriter expectWriter = new PrintWriter(expectSw, true);
					StringWriter actualSw = new StringWriter();
					PrintWriter actualWriter = new PrintWriter(actualSw, true);) {
				for (CallNames calls : list) {
					expectWriter.println(Arrays.asList(calls.names));
					actualWriter.println(filterMethodNames(calls.proxy
							.getMethodCallNames()));
				}
				assertEquals(expectSw.toString(), actualSw.toString());
			} catch (IOException e) {
				fail(e.toString());
			}
		}

		private List<String> filterMethodNames(List<String> raw) {
			List<String> filtered = new ArrayList<>(raw);
			filtered.removeAll(Arrays.asList(BORING_METHOD_NAMES));
			return filtered;
		}

		private static class CallNames {
			private final MethodCallRecorder proxy;
			private final String[] names;

			public CallNames(MethodCallRecorder proxy, String[] names) {
				this.proxy = proxy;
				this.names = names;
			}

		}

	}

	/**
	 * Some utility methods for creating a group of test objects.
	 */
	private static abstract class TestObjectGrouping {
		protected GraphWithPerform wrapGraph(GraphMem raw) {
			return RecordingProxy.create(raw, GraphWithPerform.class);
		}

		protected static ModelChangedListener makeListener() {
			return RecordingProxy.create(new StatementListener(),
					ModelChangedListener.class);
		}

		protected Model wrapModel(Model m) {
			return RecordingProxy.create(m, Model.class);
		}

		protected OntModel wrapOntModel(OntModel om) {
			return RecordingProxy.create(om, OntModel.class);
		}

		protected <T> T reset(T proxy) {
			((MethodCallRecorder) proxy).resetMethodCalls();
			return proxy;
		}
	}

}
