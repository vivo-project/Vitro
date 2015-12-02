/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * All of these tests are for OperationUtils.cloneBean()
 */
public class OperationUtils_CloneBeanTest extends AbstractTestClass {

	// ----------------------------------------------------------------------
	// Allow the tests to expect a RuntimeException with a particular cause.
	// ----------------------------------------------------------------------

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	Matcher<?> causedBy(Class<? extends Throwable> causeClass) {
		return new CausedByMatcher(causeClass);
	}

	class CausedByMatcher extends BaseMatcher<Throwable> {
		private final Class<? extends Throwable> causeClass;

		public CausedByMatcher(Class<? extends Throwable> causeClass) {
			this.causeClass = causeClass;
		}

		@Override
		public boolean matches(Object actualThrowable) {
			if (!(actualThrowable instanceof RuntimeException)) {
				return false;
			}
			Throwable cause = ((RuntimeException) actualThrowable).getCause();
			return causeClass.isInstance(cause);
		}

		@Override
		public void describeTo(Description d) {
			d.appendText("RuntimeException caused by " + causeClass.getName());
		}
	}

	// ----------------------------------------------------------------------
	// Test for invalid classes
	// ----------------------------------------------------------------------

	@Test(expected = NullPointerException.class)
	public void nullArgument() {
		OperationUtils.cloneBean(null);
	}

	@Test(expected = NullPointerException.class)
	public void nullBean() {
		OperationUtils
				.cloneBean(null, SimpleSuccess.class, SimpleSuccess.class);
	}

	@Test(expected = NullPointerException.class)
	public void nullBeanClass() {
		OperationUtils
				.cloneBean(new SimpleSuccess(), null, SimpleSuccess.class);
	}

	@Test(expected = NullPointerException.class)
	public void nullInterfaceClass() {
		OperationUtils
				.cloneBean(new SimpleSuccess(), SimpleSuccess.class, null);
	}

	@Test(expected = IllegalAccessException.class)
	@Ignore("Why doesn't this throw an exception?")
	public void privateClass() {
		OperationUtils.cloneBean(new PrivateClass());
	}

	@Test
	public void privateConstructor() {
		thrown.expect(causedBy(NoSuchMethodException.class));
		OperationUtils.cloneBean(new PrivateConstructor());
	}

	@Test
	public void abstractClass() {
		thrown.expect(causedBy(InstantiationException.class));
		OperationUtils.cloneBean(new ConcreteOfAbstractClass(),
				AbstractClass.class, AbstractClass.class);
	}

	@Test
	public void interfaceClass() {
		thrown.expect(causedBy(InstantiationException.class));
		OperationUtils.cloneBean(new ConcreteOfInterfaceClass(),
				InterfaceClass.class, InterfaceClass.class);
	}

	@Test
	public void arrayClass() {
		thrown.expect(causedBy(NoSuchMethodException.class));
		OperationUtils.cloneBean(new String[0]);
	}

	@Test
	public void primitiveTypeClass() {
		thrown.expect(causedBy(NoSuchMethodException.class));
		OperationUtils.cloneBean(1, Integer.TYPE, Integer.TYPE);
	}

	@Test
	public void voidClass() {
		thrown.expect(causedBy(NoSuchMethodException.class));
		OperationUtils.cloneBean(new Object(), Void.TYPE, Void.TYPE);
	}

	@Test
	public void noNullaryConstructor() {
		thrown.expect(causedBy(NoSuchMethodException.class));
		OperationUtils.cloneBean(new NoNullaryConstructor(1));
	}

	@Test(expected = ExceptionInInitializerError.class)
	public void classThrowsExceptionWhenLoaded() {
		OperationUtils.cloneBean(new ThrowsExceptionWhenLoaded());
	}

	@Test
	public void initializerThrowsException() {
		thrown.expect(causedBy(InvocationTargetException.class));
		OperationUtils.cloneBean("random object",
				InitializerThrowsException.class,
				InitializerThrowsException.class);
	}

	@Test
	public void wrongInterfaceClass() {
		thrown.expect(causedBy(IllegalArgumentException.class));
		OperationUtils.cloneBean(new WrongConcreteClass(),
				WrongConcreteClass.class, WrongInterface.class);
	}

	@Test
	public void getThrowsException() {
		thrown.expect(causedBy(InvocationTargetException.class));
		OperationUtils.cloneBean(new GetMethodThrowsException());
	}

	@Test
	public void setThrowsException() {
		thrown.expect(causedBy(InvocationTargetException.class));
		OperationUtils.cloneBean(new SetMethodThrowsException());
	}

	private static class PrivateClass {
		public PrivateClass() {
		}
	}

	private static class PrivateConstructor {
		private PrivateConstructor() {
		}
	}

	public abstract static class AbstractClass {
		public AbstractClass() {
		}
	}

	public static class ConcreteOfAbstractClass extends AbstractClass {
		public ConcreteOfAbstractClass() {
		}
	}

	public abstract static class InterfaceClass {
		public InterfaceClass() {
		}
	}

	public static class ConcreteOfInterfaceClass extends InterfaceClass {
		public ConcreteOfInterfaceClass() {
		}
	}

	public static class NoNullaryConstructor {
		@SuppressWarnings("unused")
		public NoNullaryConstructor(int i) {
			// nothing to do
		}
	}

	public static class ThrowsExceptionWhenLoaded {
		static {
			if (true)
				throw new IllegalArgumentException();
		}
	}

	public static class InitializerThrowsException {
		{
			if (true)
				throw new IllegalStateException("Initializer throws exception");
		}
	}

	public static class WrongConcreteClass {
		private String junk = "junk";

		public String getJunk() {
			return this.junk;
		}

		@SuppressWarnings("unused")
		private void setJunk(String junk) {
			this.junk = junk;
		}
	}

	public static interface WrongInterface {
		String getJunk();

		void setJunk(String junk);
	}

	public static class GetMethodThrowsException {
		@SuppressWarnings("unused")
		private String junk = "junk";

		public String getJunk() {
			throw new UnsupportedOperationException();
		}

		public void setJunk(String junk) {
			this.junk = junk;
		}
	}

	public static class SetMethodThrowsException {
		private String junk = "junk";

		public String getJunk() {
			return this.junk;
		}

		@SuppressWarnings("unused")
		public void setJunk(String junk) {
			throw new UnsupportedOperationException();
		}
	}

	// ----------------------------------------------------------------------
	// Test simple success and innocuous variations
	// ----------------------------------------------------------------------

	@Test
	public void simpleSuccess() {
		expectSuccess(new SimpleSuccess().insertField("label", "a prize"));
	}

	@Test
	public void getButNoSet() {
		expectSuccess(new GetButNoSet().insertField("label", "shouldBeEqual"));
	}

	@Test
	public void getTakesParameters() {
		expectSuccess(new GetTakesParameters().insertField("label", "fine"));
	}

	@Test
	public void getReturnsVoid() {
		expectSuccess(new GetReturnsVoid().insertField("label", "fine"));
	}

	@Test
	public void getAndSetDontMatch() {
		expectSuccess(new GetAndSetDontMatch().insertField("label", "fine"));
	}

	@Test
	public void getIsStatic() {
		expectSuccess(new GetIsStatic().insertField("label", "fine")
				.insertField("instanceJunk", "the junk"));
	}

	@Test
	public void getMethodIsPrivate() {
		expectSuccess(new GetMethodIsPrivate().insertField("label", "fine"));
	}

	@Test
	public void setMethodIsPrivate() {
		expectSuccess(new SetMethodIsPrivate().insertField("label", "fine"));
	}

	private void expectSuccess(BeanBase original) {
		BeanBase cloned = (BeanBase) OperationUtils.cloneBean(original);
		assertEquals("Simple success", original, cloned);
	}

	public static abstract class BeanBase {
		protected final Map<String, Object> fields = new HashMap<>();

		public BeanBase insertField(String key, Object value) {
			if (value != null) {
				fields.put(key, value);
			}
			return this;
		}

		@Override
		public int hashCode() {
			return fields.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (o == null) {
				return false;
			}
			if (!this.getClass().equals(o.getClass())) {
				return false;
			}
			return this.fields.equals(((BeanBase) o).fields);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName() + fields;
		}
	}

	public static class SimpleSuccess extends BeanBase {
		public String getLabel() {
			return (String) fields.get("label");
		}

		public void setLabel(String label) {
			insertField("label", label);
		}
	}

	public static class GetButNoSet extends SimpleSuccess {
		public String getJunk() {
			throw new UnsupportedOperationException();
		}
	}

	public static class GetTakesParameters extends SimpleSuccess {
		@SuppressWarnings("unused")
		public String getJunk(String why) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unused")
		public void setJunk(String junk) {
			throw new UnsupportedOperationException();
		}
	}

	public static class GetReturnsVoid extends SimpleSuccess {
		public void getJunk() {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unused")
		public void setJunk(String junk) {
			throw new UnsupportedOperationException();
		}
	}

	public static class GetAndSetDontMatch extends SimpleSuccess {
		public String getJunk() {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unused")
		public void setJunk(Integer junk) {
			throw new UnsupportedOperationException();
		}
	}

	public static class GetIsStatic extends SimpleSuccess {
		public static String getJunk() {
			return ("the junk");
		}

		public void setJunk(String junk) {
			insertField("instanceJunk", junk);
		}
	}

	public static class GetMethodIsPrivate extends SimpleSuccess {
		@SuppressWarnings("unused")
		private String getJunk() {
			return ("the junk");
		}

		public void setJunk(String junk) {
			insertField("instanceJunk", junk);
		}
	}

	public static class SetMethodIsPrivate extends SimpleSuccess {
		public String getJunk() {
			return ("the junk");
		}

		@SuppressWarnings("unused")
		private void setJunk(String junk) {
			insertField("instanceJunk", junk);
		}
	}

}
