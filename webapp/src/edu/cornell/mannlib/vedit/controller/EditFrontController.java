/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.controller;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;

/**
 * This controller exists only so we can request different edit form controllers
 * without having to have entries in web.xml for each.
 * @author bjl23
 *
 */
public class EditFrontController extends VitroHttpServlet {
    private static final Log log = LogFactory.getLog(EditFrontController.class.getName());
    private static final String CONTROLLER_PKG = "edu.cornell.mannlib.vitro.webapp.controller.edit";

    public void doPost(HttpServletRequest request, 
    		           HttpServletResponse response) throws IOException, ServletException {
        String controllerName = request.getParameter("controller")+"RetryController";
        if (controllerName==null || controllerName.length()==0) {
            log.error("doPost() found no controller parameter");
        }
        Class controller = null;
        Object controllerInstance = null;
        try {
            controller = Class.forName(CONTROLLER_PKG+"."+controllerName);
            try {
                controllerInstance = controller.getConstructor(
                		(Class[]) null).newInstance((Object[]) null);
                ((HttpServlet)controllerInstance).init(getServletConfig());
            } catch (Exception e) {
            	String errMsg = "doPost() could not instantiate specific " +
        		        "controller " + controllerName; 
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        } catch (ClassNotFoundException e){
        	String errMsg = "doPost() could not find controller " + 
        	        CONTROLLER_PKG + "." + controllerName; 
            log.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        Class[] args = new Class[2];
        args[0] = HttpServletRequest.class;
        args[1] = HttpServletResponse.class;
        try {
            Method meth = controller.getDeclaredMethod("doGet",args);
            Object[] methArgs = new Object[2];
            methArgs[0] = request;
            methArgs[1] = response;
            try {
                meth.invoke(controllerInstance,methArgs);
            } catch (IllegalAccessException e) {
            	String errMsg = "doPost() encountered IllegalAccessException " +
        		        "while invoking " + controllerName;
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            } catch (InvocationTargetException e) {
            	String errMsg = "doPost() encountered InvocationTargetException " +
        		        "while invoking " + controllerName;
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }	
        } catch (NoSuchMethodException e){
            log.error("could not find doPost() method in " + controllerName);
            throw new RuntimeException("could not find doPost() method in " +
            		controllerName); 
        }
    }

    public void doGet(HttpServletRequest request, 
    		          HttpServletResponse response) throws IOException, ServletException {
        doPost(request,response);
    }

}
