/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modelaccess;

import java.lang.reflect.Field;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ModelAccessFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

/**
 * A mock instance of ModelAccessFactory for use in unit tests.
 * 
 * I have only implemented the methods that I needed for my tests. Feel free to
 * implement the rest, as needed.
 */
public class ModelAccessFactoryStub extends ModelAccessFactory {
	private final ContextModelAccessStub contextMA;
	private final RequestModelAccessStub requestMA;

	public ModelAccessFactoryStub() {
		try {
			Field factoryField = ModelAccess.class.getDeclaredField("factory");
			factoryField.setAccessible(true);
			factoryField.set(null, this);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(
					"Failed to create the ModelAccessFactoryStub", e);
		}

		contextMA = new ContextModelAccessStub();
		requestMA = new RequestModelAccessStub();
	}

	@Override
	public ContextModelAccess buildContextModelAccess(ServletContext ctx) {
		return contextMA;
	}

	@Override
	public RequestModelAccess buildRequestModelAccess(HttpServletRequest req) {
		return requestMA;
	}

	public ContextModelAccessStub get(ServletContext ctx) {
		return contextMA;
	}
	
	public RequestModelAccessStub get(HttpServletRequest req) {
		return requestMA;
	}
}
