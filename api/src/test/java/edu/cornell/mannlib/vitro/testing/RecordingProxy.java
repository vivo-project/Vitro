/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.testing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The create() method creates a dynamic Proxy that wraps your inner object, and
 * implements your interfaze.
 * 
 * It also implements the MethodCallRecorder interface (although you will need
 * to cast it), so you can find out what methods were called on the proxy.
 */
public class RecordingProxy {
	public static <T> T create(T inner, Class<T> interfaze) {
		RecordingInvocationHandler handler = new RecordingInvocationHandler(
				inner);

		ClassLoader classLoader = interfaze.getClassLoader();
		Class<?>[] interfaces = new Class<?>[] { interfaze,
				MethodCallRecorder.class };
		return interfaze.cast(Proxy.newProxyInstance(classLoader, interfaces,
				handler));
	}

	/**
	 * The "add-on" interface that allows us to ask what methods were called on
	 * the proxy since it was created, or since it was reset.
	 */
	public interface MethodCallRecorder {
		List<MethodCall> getMethodCalls();

		List<String> getMethodCallNames();

		void resetMethodCalls();
	}

	public static class MethodCall {
		/** a convenience method to get just the names of the methods called. */
		public static Object justNames(List<MethodCall> methodCalls) {
			List<String> names = new ArrayList<>();
			for (MethodCall methodCall : methodCalls) {
				names.add(methodCall.getName());
			}
			return names;
		}

		private final String name;
		private final List<Object> argList;

		public MethodCall(String name, Object[] args) {
			this.name = name;
			if (args == null) {
				this.argList = Collections.emptyList();
			} else {
				this.argList = Collections.unmodifiableList(new ArrayList<>(
						Arrays.asList(args)));
			}
		}

		public String getName() {
			return name;
		}

		public List<Object> getArgList() {
			return argList;
		}

	}

	public static class RecordingInvocationHandler implements InvocationHandler {
		private final Object inner;
		private final List<MethodCall> methodCalls = new ArrayList<>();

		RecordingInvocationHandler(Object inner) {
			this.inner = inner;
		}

		List<MethodCall> getMethodCalls() {
			return new ArrayList<>(methodCalls);
		}

		void reset() {
			methodCalls.clear();
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			switch (method.getName()) {
			case "getMethodCalls":
				return new ArrayList<MethodCall>(methodCalls);
			case "getMethodCallNames":
				return MethodCall.justNames(methodCalls);
			case "resetMethodCalls":
				methodCalls.clear();
				return null;
			case "equals":
				if (args == null) return false;
				if (args.length == 0) return false;
				return args[0].equals(inner);
			default:
				methodCalls.add(new MethodCall(method.getName(), args));
				return method.invoke(inner, args);
			}
		}

	}

}