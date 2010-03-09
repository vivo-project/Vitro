/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.naming;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * In order to use this and the other naming stubs.javax.naming classes, do
 * this:
 * 
 * <pre>
 * System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
 * 		InitialContextFactoryStub.class.getName());
 * </pre>
 * 
 * The bindings are static, so you will probablly want to call {@link #reset()}
 * before each test.
 */
public class InitialContextStub extends InitialContext {

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private static Map<String, Object> bindings = new TreeMap<String, Object>();

	/**
	 * Make sure we start the next test with a fresh instance.
	 */
	public static void reset() {
		bindings.clear();
	}

	/**
	 * If we have properties that are bound to a level below this name, then
	 * this name represents a sub-context.
	 */
	private boolean isSubContext(String name) {
		String path = name.endsWith("/") ? name : name + "/";

		for (String key : bindings.keySet()) {
			if (key.startsWith(path)) {
				return true;
			}
		}
		return false;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	public InitialContextStub() throws NamingException {
		super();
	}

	@Override
	protected void init(Hashtable<?, ?> environment) throws NamingException {
	}

	@Override
	protected Context getURLOrDefaultInitCtx(String name)
			throws NamingException {
		return super.getURLOrDefaultInitCtx(name);
	}

	@Override
	protected Context getDefaultInitCtx() throws NamingException {
		return ContextStub.getInstance("", this);
	}

    private boolean isEmpty(String string) {
        return (string == null || string.trim().length() == 0);
    }

	@Override
	public void bind(String name, Object obj) throws NamingException {
		if (name == null) {
			throw new NullPointerException(
					"InitialContextStub: name may not be null.");
		}
		if (isEmpty(name)) {
			throw new NamingException(
					"InitialContextStub: name may not be empty.");
		}
		if (bindings.containsKey(name)) {
			throw new NamingException(
					"InitialContextStub: name is already bound.");
		}

		bindings.put(name, obj);
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException {
		if (name == null) {
			throw new NullPointerException(
					"InitialContextStub: name may not be null.");
		}
		if (isEmpty(name)) {
			throw new NamingException(
					"InitialContextStub: name may not be empty.");
		}

		bindings.put(name, obj);
	}

	@Override
	public Object lookup(String name) throws NamingException {
		if (name == null) {
			throw new NullPointerException(
					"InitialContextStub: name may not be null");
		}
		if (isEmpty(name)) {
			return this;
		}
		if (bindings.containsKey(name)) {
			return bindings.get(name);
		}
		if (isSubContext(name)) {
			return ContextStub.getInstance(name, this);
		}

		throw new NamingException("InitialContextStub: No binding for '" + name
				+ "'");
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.addToEnvironment() not implemented.");
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException {
		throw new RuntimeException("InitialContextStub.bind() not implemented.");
	}

	@Override
	public void close() throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.close() not implemented.");
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.composeName() not implemented.");
	}

	@Override
	public String composeName(String name, String prefix)
			throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.composeName() not implemented.");
	}

	@Override
	public Context createSubcontext(Name name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.createSubcontext() not implemented.");
	}

	@Override
	public Context createSubcontext(String name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.createSubcontext() not implemented.");
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.destroySubcontext() not implemented.");
	}

	@Override
	public void destroySubcontext(String name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.destroySubcontext() not implemented.");
	}

	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.getEnvironment() not implemented.");
	}

	@Override
	public String getNameInNamespace() throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.getNameInNamespace() not implemented.");
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.getNameParser() not implemented.");
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.getNameParser() not implemented.");
	}

	@Override
	protected Context getURLOrDefaultInitCtx(Name name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.getURLOrDefaultInitCtx() not implemented.");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		throw new RuntimeException("InitialContextStub.list() not implemented.");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		throw new RuntimeException("InitialContextStub.list() not implemented.");
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.listBindings() not implemented.");
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.listBindings() not implemented.");
	}

	@Override
	public Object lookup(Name name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.lookup() not implemented.");
	}

	@Override
	public Object lookupLink(Name name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.lookupLink() not implemented.");
	}

	@Override
	public Object lookupLink(String name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.lookupLink() not implemented.");
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.rebind() not implemented.");
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.removeFromEnvironment() not implemented.");
	}

	@Override
	public void rename(Name oldName, Name newName) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.rename() not implemented.");
	}

	@Override
	public void rename(String oldName, String newName) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.rename() not implemented.");
	}

	@Override
	public void unbind(Name name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.unbind() not implemented.");
	}

	@Override
	public void unbind(String name) throws NamingException {
		throw new RuntimeException(
				"InitialContextStub.unbind() not implemented.");
	}

}
