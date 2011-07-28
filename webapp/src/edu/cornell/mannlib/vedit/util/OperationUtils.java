/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;


public class OperationUtils{
	
	private static final Log log = LogFactory.getLog(OperationUtils.class.getName());

    public static void beanSetAndValidate(Object newObj, String field, String value, EditProcessObject epo){
        Class cls = (epo.getBeanClass() != null) ? epo.getBeanClass() : newObj.getClass();
        Class[] paramList = new Class[1];
        paramList[0] = String.class;
        boolean isInt = false;
        boolean isBoolean = false;
        Method setterMethod = null;
        try {
            setterMethod = cls.getMethod("set"+field,paramList);
        } catch (NoSuchMethodException e) {
            //let's try int
            paramList[0] = int.class;
            try {
                setterMethod = cls.getMethod("set"+field,paramList);
                isInt = true;
            } catch (NoSuchMethodException f) {
            	//let's try boolean
            	paramList[0]=boolean.class;
            	try {
            		setterMethod = cls.getMethod("set"+field,paramList);
            		isBoolean = true;
            		log.debug("found boolean field "+field);
            	} catch (NoSuchMethodException g) {
            		log.error("beanSet could not find an appropriate String, int, or boolean setter method for "+field);	
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
            setterMethod.invoke(newObj,arglist);
        } catch (Exception e) {
            log.error("Couldn't invoke method");
            log.error(e.getMessage());
            log.error(field+" "+arglist[0]);
        }
    }

    /**
     * Takes a bean and clones it using reflection.
     * Any fields without standard getter/setter methods will not be copied.
     * @param bean
     * @return
     */
    public static Object cloneBean (Object bean) {
        return cloneBean(bean, bean.getClass(), bean.getClass());
    }

    /**
     * Takes a bean and clones it using reflection.
     * Any fields without standard getter/setter methods will not be copied.
     * @param bean
     * @return
     */
    public static Object cloneBean (Object bean, Class beanClass, Class iface){
        Object newBean = null;
        try {
            newBean = beanClass.newInstance();
            Method[] beanMeths = iface.getMethods();
            for (int i=0; i<beanMeths.length ; ++i) {
                Method beanMeth = beanMeths[i];
                String methName = beanMeth.getName();
                if (methName.startsWith("get") 
                        && beanMeth.getParameterTypes().length == 0 ) {
                    String fieldName = methName.substring(3,methName.length());
                    Class returnType = beanMeth.getReturnType();
                    try {
                        Class[] args = new Class[1];
                        args[0] = returnType;
                        Method setterMethod = iface.getMethod("set"+fieldName,args);
                        try {
                            Object fieldVal = beanMeth.invoke(bean,(Object[])null);
                            try {
                                Object[] setArgs = new Object[1];
                                setArgs[0] = fieldVal;
                                setterMethod.invoke(newBean,setArgs);
                            } catch (IllegalAccessException iae) {
                                log.error(OperationUtils.class.getName() +
                                        ".cloneBean() " +
                                        " encountered IllegalAccessException " +
                                        " invoking " + 
                                        setterMethod.getName(), iae);
                                throw new RuntimeException(iae);
                            } catch (InvocationTargetException ite) {
                                log.error(OperationUtils.class.getName() +
                                        ".cloneBean() " +
                                        " encountered InvocationTargetException" 
                                        + " invoking " 
                                        + setterMethod.getName(), ite);
                                throw new RuntimeException(ite);
                            }
                        } catch (IllegalAccessException iae) {
                            log.error(OperationUtils.class.getName() + 
                                    ".cloneBean() encountered " +
                                    " IllegalAccessException invoking " + 
                                    beanMeths[i].getName(), iae);
                            throw new RuntimeException(iae);
                        } catch (InvocationTargetException ite) {
                            log.error(OperationUtils.class.getName() + 
                                    ".cloneBean() encountered " +
                                    " InvocationTargetException invoking " + 
                                    beanMeths[i].getName(), ite);
                            throw new RuntimeException(ite);
                        } catch (IllegalArgumentException iae) {
                            log.error(OperationUtils.class.getName() + 
                                    ".cloneBean() encountered " +
                                    " IllegalArgumentException invoking " + 
                                    beanMeths[i].getName(), iae);
                            throw new RuntimeException(iae);
                        }
                    } catch (NoSuchMethodException nsme){
                        // ignore this field because there is no setter method
                    }
                }
            }
        } catch (InstantiationException ie){
            log.error("edu.cornell.mannlib.vitro.edit.utils.OperationUtils.cloneBean("+bean.getClass().toString()+") could not instantiate new instance of bean.");
            log.error(ie.getStackTrace());
        } catch (IllegalAccessException iae){
            log.error("edu.cornell.mannlib.vitro.edit.utils.OperationUtils.cloneBean("+bean.getClass().toString()+") encountered illegal access exception instantiating new bean.");
            log.error(iae.getStackTrace());
        }
        return newBean;
    }

}