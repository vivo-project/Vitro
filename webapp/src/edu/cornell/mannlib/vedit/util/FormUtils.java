/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.util;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class FormUtils {

    protected static final Log log = LogFactory.getLog(FormUtils.class.getName());
    protected static final int BASE_10 = 10;
    protected static final Class[] SUPPORTED_TYPES = { String.class, 
            int.class, 
            Integer.class, 
            boolean.class, 
            Date.class 
          };

    protected static final List<Class> SUPPORTED_TYPE_LIST = Arrays
            .asList(SUPPORTED_TYPES);

    /* this class needs to be reworked */

    public static void populateFormFromBean (Object bean, 
    		                                 String action, 
    		                                 FormObject foo) {
        populateFormFromBean(bean,action,null,foo,new HashMap());
    }

    public static void populateFormFromBean (Object bean, 
    		                                 String action, 
    		                                 FormObject foo, 
    		                                 Map<String, String> badValuesHash) {
        populateFormFromBean(bean,action,null,foo,badValuesHash);
    }

    /**
     * Populates form objects with bean values
     */
    public static void populateFormFromBean (Object bean, 
    		                                 String action, 
    		                                 EditProcessObject epo, 
    		                                 FormObject foo, 
    		                                 Map<String, String> BadValuesHash) {
        Class beanClass = 
        	    (epo != null && epo.getBeanClass() != null) 
        	            ? epo.getBeanClass() 
        	            : bean.getClass();

        Method[] meths = beanClass.getMethods();

        for (int i=0; i<meths.length; i++) {

            if (meths[i].getName().indexOf("set") == 0) {

                // we have a setter method
                Method currMeth = meths[i];
                Class[] currMethParamTypes = currMeth.getParameterTypes();
                Class currMethType = currMethParamTypes[0];
                
                if (SUPPORTED_TYPE_LIST.contains(currMethType)) {
                	//we only want people directly to type in ints, strings, and dates
                	//of course, most of the ints are probably foreign keys anyway...

                    String elementName = currMeth.getName().substring(
                    		3,currMeth.getName().length());

                    //see if there's something in the bean using
                    //the related getter method

                    Class[] paramClass = new Class[1];
                    paramClass[0] = currMethType;
                    try {
                        Method getter = beanClass.getMethod(
                        		"get" + elementName, (Class[]) null);
                        Object existingData = null;
                        try {
                            existingData = getter.invoke(bean, (Object[]) null);
                        } catch (Exception e) {
                            log.error ("Exception invoking getter method");
                        }
                        String value = "";
                        if (existingData != null){
                            if (existingData instanceof String){
                                value += existingData;
                            }
                            else if (!(existingData instanceof Integer 
                            		&& (Integer)existingData < 0)) {
                                value += existingData.toString();
                            }
                        }
                        String badValue = (String) BadValuesHash.get(elementName);
                        if (badValue != null) {
                            value = badValue;
                        }
                        foo.getValues().put(elementName, value);
                    } catch (NoSuchMethodException e) {
                        //ignore it
                    }
                }
            }
        }
    }

    public static List<Option> makeOptionListFromBeans (List beanList, 
    		                                            String valueField, 
    		                                            String bodyField, 
    		                                            String selectedValue, 
    		                                            String selectedBody) {
        return makeOptionListFromBeans (
        		beanList, valueField, bodyField, selectedValue, selectedBody, true);
    }

    public static List<Option> makeOptionListFromBeans(List beanList, 
    		                                           String valueField, 
    		                                           String bodyField, 
    		                                           String selectedValue, 
    		                                           String selectedBody, 
    		                                           boolean forceSelectedInclusion) {
        List<Option> optList = new LinkedList();

        if (beanList == null)
            return optList;

        Iterator beanIt = beanList.iterator();
        boolean foundSelectedValueInBeans = false;

        while (beanIt.hasNext()){
            Object bean = beanIt.next();

            String value="";
            Method valueMeth = null;
            Object valueObj = null;
            try {
                valueMeth = bean.getClass().getMethod(
                		"get" + valueField, (Class[]) null);
                valueObj = valueMeth.invoke(bean, (Object[]) null);
            } catch (Exception e) {
                log.warn("Could not find method get" + valueField + " on " + 
                		bean.getClass());
            }

            if (valueObj != null){
                value = valueObj.toString();
            }

            String body="";
            Method bodyMeth = null;
            Object bodyObj = null;
            try {
                bodyMeth = bean.getClass().getMethod(
                		"get" + bodyField, (Class[]) null);
                bodyObj = bodyMeth.invoke(bean, (Object[]) null);
            } catch (Exception e) {
                log.warn(" could not find method get"+bodyField);
            }

            if (bodyObj != null){
                body = bodyObj.toString();
            }

            Option opt = new Option();
            opt.setValue(value);
            opt.setBody(body);

            if (selectedValue != null){
                if (selectedValue.equals(value)) {
                    opt.setSelected(true);
                    foundSelectedValueInBeans = true;
                }
            } else {
                if (selectedBody != null){
                    if (selectedBody.equals(body)) {
                        opt.setSelected(true);
                        foundSelectedValueInBeans = true;
                    }
                }
            }

            optList.add(opt);

        }

        // if the list of beans doesn't include the selected value/body, 
        // insert it anyway so we don't inadvertently change the value of the
        // field to the first thing that happens to be in the select list 
        boolean skipThisStep = !forceSelectedInclusion;
        // For now, if the value is a negative integer, we won't try to 
        // preserve it, as the bean was probably just instantiated.
        // Should switch to a more robust way of handling inital bean values.
        if (selectedValue == null) {
            skipThisStep = true;
        } else {
            try {
                int selectedValueInt = Integer.decode(selectedValue);
                if (selectedValueInt < 0)
                    skipThisStep = true;
            } catch (NumberFormatException e) {}
        }
        if (!foundSelectedValueInBeans && !skipThisStep) {
            log.trace("Adding the selected option!");
            Option sOpt = new Option();
            sOpt.setValue(selectedValue);
            if (selectedBody == null || selectedBody.length() == 0)
                sOpt.setBody(selectedValue.toString());
            else
                sOpt.setBody(selectedBody);
            sOpt.setSelected(true);
            optList.add(sOpt);
        }

        return optList;

    }
    
    public static List<Option> makeVClassOptionList(WebappDaoFactory wadf, 
    		                                        String selectedVClassURI) {
        List<Option> vclassOptionList = new LinkedList<Option>();
        for (VClass vclass : wadf.getVClassDao().getAllVclasses()) {
        	Option option = new Option();
        	option.setValue(vclass.getURI());
        	if ( (selectedVClassURI != null) 
        			&& (vclass.getURI() != null) 
        			&& (selectedVClassURI.equals(vclass.getURI())) ) {
        		option.setSelected(true);
        	}
        	String ontologyName = null;
        	if (vclass.getNamespace() != null) {
        		Ontology ont = wadf.getOntologyDao().getOntologyByURI(
        				vclass.getNamespace());
        		if ( (ont != null) && (ont.getName() != null) ) {
        			ontologyName = ont.getName();
        		}
        	}
        	StringBuffer classNameBuffer = new StringBuffer();
        	if (vclass.getName() != null) {
        		classNameBuffer.append(vclass.getName());
        	}
        	if (ontologyName != null) {
        		classNameBuffer.append(" (").append(ontologyName).append(")");
        	}
        	option.setBody(classNameBuffer.toString());
        	vclassOptionList.add(option);
        }
        return vclassOptionList;
    }

    public static void beanSet(Object newObj, String field, String value) {
        beanSet (newObj, field, value, null);
    }

    public static void beanSet(Object newObj, 
    		                   String field, 
    		                   String value, 
    		                   EditProcessObject epo) {
        SimpleDateFormat standardDateFormat = new SimpleDateFormat(
        		"yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat minutesOnlyDateFormat = new SimpleDateFormat(
        		"yyyy-MM-dd HH:mm");
        Class cls = 
        	    (epo != null && epo.getBeanClass() != null) 
        	            ? epo.getBeanClass() 
        	            : newObj.getClass();
        Class[] paramList = new Class[1];
        Method setterMethod = getSetterMethod(cls, field, SUPPORTED_TYPE_LIST);
        if (setterMethod == null) {
        	log.debug("Could not find method set" + field + " on " 
        			+ cls.getName());
        	return;
        }
        Class argumentType = setterMethod.getParameterTypes()[0];   
        Object[] arglist = new Object[1];
        if (int.class.equals(argumentType) 
        		|| Integer.class.equals(argumentType)) {
            arglist[0] = (int.class.equals(argumentType)) ? -1 : null;
            if (!value.isEmpty()) { 
                int parsedInt = Integer.parseInt(value, BASE_10);
                if (parsedInt < 0) {
                	throw new FormUtils.NegativeIntegerException();    	
                } else {
                	arglist[0] = parsedInt;
                }
            }                	
        } else if (Date.class.equals(argumentType)) {
            // this isn't so great ; should probably be in a validator
            if (value != null && value.length() > 0 && value.indexOf(":") < 1) {
                value += " 00:00:00";
            }
            if (value != null && value.length()>0) {
                try {
                    arglist[0] = standardDateFormat.parse(value);
                } catch (ParseException p) {
                    try {
                        arglist[0] = minutesOnlyDateFormat.parse(value);
                    } catch (ParseException q) {
                        log.error(FormUtils.class.getName()+" could not parse" +
                        		value + " to a Date object.");
                        throw new IllegalArgumentException(
                        		"Please enter a date/time in one of these " +
                        		"formats: '2007-07-07', '2007-07-07 07:07' " +
                        		"or '2007-07-07 07:07:07'");
                    }
                }
            } else {
                arglist[0] = null;
            }
        } else if (boolean.class.equals(argumentType)) {      	
            arglist[0] = (value.equalsIgnoreCase("true"));
        } else {
            arglist[0] = value;
        }
        try {
            setterMethod.invoke(newObj,arglist);
        } catch (Exception e) {
        	log.error(e,e);
        }
    }

    private static Method getSetterMethod(Class beanClass, 
    		                              String fieldName, 
    		                              List<Class> supportedTypes) {
    	for (Class clazz : supportedTypes) {
    		try {
    			Class[] argList = new Class[1];
    			argList[0] = clazz;
    			return beanClass.getMethod("set" + fieldName, argList);
    		} catch (NoSuchMethodException nsme) {
    			// just try the next type
    		}
     	} 
    	return null;
    }

    /**
     * Decodes a Base-64-encoded String of format 
     * key:value;key2:value2;key3:value, and puts the keys and values in a Map
     * @param params
     * @return
     */
    public static Map beanParamMapFromString(String params) {
        String[] param = params.split(";");
        Map beanParamMap = new HashMap();
        for (int i=0; i<param.length; i++) {
            String[] p = param[i].split(":");
            beanParamMap.put(p[0],new String(Base64.decodeBase64(p[1].getBytes())));
        }
        return beanParamMap;
    }
    
    public static class NegativeIntegerException extends RuntimeException {}

}
