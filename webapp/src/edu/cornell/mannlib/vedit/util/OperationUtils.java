/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;

public class OperationUtils {

	private static final Log log = LogFactory.getLog(OperationUtils.class
			.getName());

	public static void beanSetAndValidate(Object newObj, String field,
			String value, EditProcessObject epo) {
		Class<?> cls = (epo.getBeanClass() != null) ? epo.getBeanClass() : newObj
				.getClass();
		Class<?>[] paramList = new Class[1];
		paramList[0] = String.class;
		boolean isInt = false;
		boolean isBoolean = false;
		Method setterMethod = null;
		try {
			setterMethod = cls.getMethod("set" + field, paramList);
		} catch (NoSuchMethodException e) {
			// let's try int
			paramList[0] = int.class;
			try {
				setterMethod = cls.getMethod("set" + field, paramList);
				isInt = true;
			} catch (NoSuchMethodException f) {
				// let's try boolean
				paramList[0] = boolean.class;
				try {
					setterMethod = cls.getMethod("set" + field, paramList);
					isBoolean = true;
					log.debug("found boolean field " + field);
				} catch (NoSuchMethodException g) {
					log.error("beanSet could not find an appropriate String, int, or boolean setter method for "
							+ field);
				}

			}
		}
		Object[] arglist = new Object[1];
		if (isInt)
			arglist[0] = Integer.decode(value);
		else if (isBoolean)
			arglist[0] = (value.equalsIgnoreCase("TRUE"));
		else
			arglist[0] = value;
		try {
			setterMethod.invoke(newObj, arglist);
		} catch (Exception e) {
			log.error("Couldn't invoke method");
			log.error(e.getMessage());
			log.error(field + " " + arglist[0]);
		}
	}

	/**
	 * Takes a bean and clones it using reflection. Any fields without standard
	 * getter/setter methods will not be copied.
	 */
	public static Object cloneBean(Object bean) {
		if (bean == null) {
			throw new NullPointerException("bean may not be null.");
		}
		return cloneBean(bean, bean.getClass(), bean.getClass());
	}

	/**
	 * Takes a bean and clones it using reflection. Any fields without standard
	 * getter/setter methods will not be copied.
	 */
	public static Object cloneBean(final Object bean, final Class<?> beanClass,
			final Class<?> iface) {
		if (bean == null) {
			throw new NullPointerException("bean may not be null.");
		}
		if (beanClass == null) {
			throw new NullPointerException("beanClass may not be null.");
		}
		if (iface == null) {
			throw new NullPointerException("iface may not be null.");
		}

		class CloneBeanException extends RuntimeException {
			public CloneBeanException(String message, Throwable cause) {
				super(message + " <" + cause.getClass().getSimpleName()
						+ ">: bean=" + bean + ", beanClass="
						+ beanClass.getName() + ", iface=" + iface.getName(),
						cause);
			}
		}

		Object newBean;
		try {
			newBean = beanClass.getConstructor().newInstance();
		} catch (NoSuchMethodException e) {
			throw new CloneBeanException("bean has no 'nullary' constructor.", e);
		} catch (InstantiationException e) {
			throw new CloneBeanException("tried to create instance of an abstract class.", e);
		} catch (IllegalAccessException e) {
			throw new CloneBeanException("bean constructor is not accessible.", e);
		} catch (InvocationTargetException e) {
			throw new CloneBeanException("bean constructor threw an exception.", e);
		} catch (Exception e) {
			throw new CloneBeanException("failed to instantiate a new bean.", e);
		}

		for (Method beanMeth : iface.getMethods()) {
			String methName = beanMeth.getName();
			if (!methName.startsWith("get")) {
				continue;
			}
			if (beanMeth.getParameterTypes().length != 0) {
				continue;
			}
			String fieldName = methName.substring(3, methName.length());
			Class<?> returnType = beanMeth.getReturnType();

			Method setterMethod;
			try {
				setterMethod = iface.getMethod("set" + fieldName, returnType);
			} catch (NoSuchMethodException nsme) {
				continue;
			}

			Object fieldVal;
			try {
				fieldVal = beanMeth.invoke(bean, (Object[]) null);
			} catch (Exception e) {
				throw new CloneBeanException("failed to invoke " + beanMeth, e);
			}

			try {
				Object[] setArgs = new Object[1];
				setArgs[0] = fieldVal;
				setterMethod.invoke(newBean, setArgs);
			} catch (Exception e) {
				throw new CloneBeanException(
						"failed to invoke " + setterMethod, e);
			}
		}
		return newBean;
	}

}