/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.naming.spi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import stubs.javax.naming.InitialContextStub;

/**
 * In order to use this and the other naming stubs.javax.naming classes, do
 * this:
 * 
 * <pre>
 * System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
 * 		InitialContextFactoryStub.class.getName());
 * </pre>
 */
public class InitialContextFactoryStub implements InitialContextFactory {
	/**
	 * It's just this easy.
	 */
	public Context getInitialContext(Hashtable<?, ?> environment)
			throws NamingException {
		return new InitialContextStub();
	}
}
