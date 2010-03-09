/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.naming;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
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
 */
public class ContextStub implements Context {

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	/**
	 * Keep a single context instance for each path.
	 */
	private static final Map<String, ContextStub> instances = new HashMap<String, ContextStub>();

	/**
	 * Get the context instance for this path. Create one if necessary.
	 */
	static ContextStub getInstance(String contextPath, InitialContextStub parent) {
		if (!instances.containsKey(contextPath)) {
			instances.put(contextPath, new ContextStub(contextPath, parent));
		}
		return instances.get(contextPath);
	}

	private final String contextPath;
	private final InitialContextStub parent;

	private ContextStub(String contextPath, InitialContextStub parent) {
		this.contextPath = contextPath;
		this.parent = parent;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	/**
	 * Let the parent handle it.
	 */
	public void rebind(String name, Object obj) throws NamingException {
		if (name == null) {
			throw new NullPointerException("ContextStub: name may not be null.");
		}
		if (isEmpty(name)) {
			throw new NamingException("ContextStub: name may not be empty.");
		}
		parent.rebind(contextPath + "/" + name, obj);
	}

	/**
	 * In most cases, just let the parent handle it.
	 */
	public Object lookup(String name) throws NamingException {
		if (name == null) {
			throw new NamingException("ContextStub: name may not be null.");
		}
		if (isEmpty(name)) {
			return this;
		}
		return parent.lookup(contextPath + "/" + name);
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		throw new RuntimeException(
				"ContextStub.addToEnvironment() not implemented.");
	}

	public void bind(Name name, Object obj) throws NamingException {
		throw new RuntimeException("ContextStub.bind() not implemented.");
	}

	public void bind(String name, Object obj) throws NamingException {
		throw new RuntimeException("ContextStub.bind() not implemented.");
	}

	public void close() throws NamingException {
		throw new RuntimeException("ContextStub.close() not implemented.");
	}

	public Name composeName(Name name, Name prefix) throws NamingException {
		throw new RuntimeException("ContextStub.composeName() not implemented.");
	}

	public String composeName(String name, String prefix)
			throws NamingException {
		throw new RuntimeException("ContextStub.composeName() not implemented.");
	}

	public Context createSubcontext(Name name) throws NamingException {
		throw new RuntimeException(
				"ContextStub.createSubcontext() not implemented.");
	}

	public Context createSubcontext(String name) throws NamingException {
		throw new RuntimeException(
				"ContextStub.createSubcontext() not implemented.");
	}

	public void destroySubcontext(Name name) throws NamingException {
		throw new RuntimeException(
				"ContextStub.destroySubcontext() not implemented.");
	}

	public void destroySubcontext(String name) throws NamingException {
		throw new RuntimeException(
				"ContextStub.destroySubcontext() not implemented.");
	}

	public Hashtable<?, ?> getEnvironment() throws NamingException {
		throw new RuntimeException(
				"ContextStub.getEnvironment() not implemented.");
	}

	public String getNameInNamespace() throws NamingException {
		throw new RuntimeException(
				"ContextStub.getNameInNamespace() not implemented.");
	}

	public NameParser getNameParser(Name name) throws NamingException {
		throw new RuntimeException(
				"ContextStub.getNameParser() not implemented.");
	}

	public NameParser getNameParser(String name) throws NamingException {
		throw new RuntimeException(
				"ContextStub.getNameParser() not implemented.");
	}

	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		throw new RuntimeException("ContextStub.list() not implemented.");
	}

	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		throw new RuntimeException("ContextStub.list() not implemented.");
	}

	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		throw new RuntimeException(
				"ContextStub.listBindings() not implemented.");
	}

	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		throw new RuntimeException(
				"ContextStub.listBindings() not implemented.");
	}

	public Object lookup(Name name) throws NamingException {
		throw new RuntimeException("ContextStub.lookup() not implemented.");
	}

	public Object lookupLink(Name name) throws NamingException {
		throw new RuntimeException("ContextStub.lookupLink() not implemented.");
	}

	public Object lookupLink(String name) throws NamingException {
		throw new RuntimeException("ContextStub.lookupLink() not implemented.");
	}

	public void rebind(Name name, Object obj) throws NamingException {
		throw new RuntimeException("ContextStub.rebind() not implemented.");
	}

	public Object removeFromEnvironment(String propName) throws NamingException {
		throw new RuntimeException(
				"ContextStub.removeFromEnvironment() not implemented.");
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		throw new RuntimeException("ContextStub.rename() not implemented.");
	}

	public void rename(String oldName, String newName) throws NamingException {
		throw new RuntimeException("ContextStub.rename() not implemented.");
	}

	public void unbind(Name name) throws NamingException {
		throw new RuntimeException("ContextStub.unbind() not implemented.");
	}

	public void unbind(String name) throws NamingException {
		throw new RuntimeException("ContextStub.unbind() not implemented.");
	}

    private boolean isEmpty(String string) {
        return (string == null || string.trim().length() == 0);
    }

}
