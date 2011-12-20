/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vedit.listener.EditPreProcessor;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.util.FormUtils.NegativeIntegerException;
import edu.cornell.mannlib.vedit.util.OperationUtils;
import edu.cornell.mannlib.vedit.validator.ValidationObject;
import edu.cornell.mannlib.vedit.validator.Validator;

public class OperationController extends BaseEditController {
	
    private static final Log log = LogFactory.getLog(OperationController.class.getName());

    public void doPost (HttpServletRequest request, HttpServletResponse response) {

        String defaultLandingPage = getDefaultLandingPage(request);

        // get the Edit Process Object which will tell us wh
        HashMap epoHash = null;
        EditProcessObject epo = null;
        try {
            epoHash = (HashMap) request.getSession().getAttribute("epoHash");
            epo = (EditProcessObject) epoHash.get(request.getParameter("_epoKey"));
        } catch (NullPointerException e) {
            //session or edit process expired
        	try {
        		response.sendRedirect(defaultLandingPage);
        	} catch (IOException ioe) {
        		log.error(this.getClass().getName() + " IOError on redirect: ", ioe);
        	}
            return;
        }

        if (epo == null) {
        	try {
        		response.sendRedirect(defaultLandingPage);
        	} catch (IOException ioe) {
        		log.error(this.getClass().getName() + " IOError on redirect: ", ioe);
        	}
            return;
        }
        
        // if we're canceling, we don't need to do anything
        if (request.getParameter("_cancel") != null){
            String referer = epo.getReferer();
            if (referer == null) {
            	try {
            		response.sendRedirect(defaultLandingPage);
            	} catch (IOException ioe) {
                    log.error(this.getClass().getName() + " IOError on redirect: ", ioe);
                }
                return;
            }
            else {
            	try {
            		response.sendRedirect(referer);
            	} catch (IOException ioe) {
            		log.error(this.getClass().getName() + " IOError on redirect: ", ioe);
            	}
                return;
            }
        }

        // reset - if reset button is of type submit
        if (request.getParameter("_reset") != null) {
        	try {
        		response.sendRedirect(request.getHeader("Referer"));
        	} catch (IOException ioe) {
        		log.error(this.getClass().getName() + " IOError on redirect: ", ioe);
        	}
            return;
        }

        try {
        	
            Object newObj = getNewObj(epo);
            
            //populate this object from the req. params
            boolean valid = populateObjectFromRequestParamsAndValidate(epo, newObj, request);
            
            //run preprocessors
            runPreprocessors(epo, newObj);
            
            //applySimpleMask(epo, newObj);

            //put the newObj back in the epo where other things can look at it
            epo.setNewBean(newObj);

            //if validation failed, go back to the form controller
            if (!valid){
            	epo.setAttribute("globalErrorMsg", "Please correct errors highlighted below.");
            	retry(request, response, epo);
            	return;
            }

            String action = getAction(request);       
            
            boolean status = performEdit(epo, newObj, action);
            if (status == FAILURE) {
            	retry(request, response, epo);
            	return;
            }
            
            /* put request parameters and attributes into epo where the listeners can see */
            epo.setRequestParameterMap(request.getParameterMap());

            notifyChangeListeners(epo, action);

            /* send the user somewhere */
            if (action.equals("insert")){
                // Object[] args = new Object[1];
                // args[0] = result;
                // epo.setNewBean(epo.getGetMethod().invoke(facade,args));
                PageForwarder pipf = epo.getPostInsertPageForwarder();
                if (pipf != null){
                    pipf.doForward(request,response,epo);
                    return;
                }
            } else if (action.equals("update")){
                PageForwarder pupf = epo.getPostUpdatePageForwarder();
                if (pupf != null) {
                    pupf.doForward(request,response,epo);
                    return;
                }
            } else if (action.equals("delete")){
                PageForwarder pdpf = epo.getPostDeletePageForwarder();
                if (pdpf != null) {
                    pdpf.doForward(request,response,epo);
                    return;
                }
            }

            //if no page forwarder was set, just go back to referring page:
            String referer = epo.getReferer();
            if (referer == null)
                response.sendRedirect(defaultLandingPage);
            else
                response.sendRedirect(referer);

        } catch (Exception e) {
            log.error("Error performing edit", e);
            
            String errMsg = (e.getMessage() != null)
                ? e.getMessage()
                : "Error performing edit";
            
            epo.setAttribute("globalErrorMsg", errMsg);
       
            try {
            	retry(request, response, epo);
            	return;
            } catch (IOException ioe) {
            	log.error(this.getClass().getName() + " IOError on redirect: ", ioe);
            }
        }
    }
    
    private void retry(HttpServletRequest request, 
    		           HttpServletResponse response, 
    		           EditProcessObject epo) throws IOException {
        String referer = request.getHeader("Referer");        
        referer = (referer == null) ? epo.getReferer() : referer;
        if( referer != null ){
            int epoKeyIndex = referer.indexOf("_epoKey");
            if (epoKeyIndex >= 0){
                String url = referer.substring(0,epoKeyIndex) + "_epoKey=" + 
                        request.getParameter("_epoKey");
                response.sendRedirect(url);
                return;
            }
            String redirectUrl = (referer.indexOf("?") > -1) 
                    ? referer + "&"  
                    : referer + "?";
            redirectUrl += "_epoKey="+request.getParameter("_epoKey");
            response.sendRedirect(redirectUrl);        
        } else {
        	response.sendRedirect(getDefaultLandingPage(request));
        }
        return;
    }
    
    private void runPreprocessors(EditProcessObject epo, Object newObj) {
    	if (epo.getPreProcessorList() != null && epo.getPreProcessorList().size()>0) {
            Iterator preIt = epo.getPreProcessorList().iterator();
            while (preIt.hasNext()) {
                try {
                	EditPreProcessor epp = (EditPreProcessor) preIt.next();
                	epp.process(newObj, epo);
                } catch (ClassCastException e) {}
            }
        }
    }
    
    private Object getNewObj(EditProcessObject epo) {
    	Object newObj = null;
    	if (epo.getOriginalBean() != null) { // we're updating or deleting an existing bean
            if (epo.getImplementationClass() != null) {
                newObj = OperationUtils.cloneBean(
                        epo.getOriginalBean(), 
                        epo.getImplementationClass(), 
                        epo.getBeanClass());
            } else {
                newObj = OperationUtils.cloneBean(epo.getOriginalBean());
            }
        } else {
            Class cls = epo.getBeanClass();
            try {
            	newObj = cls.newInstance();
            } catch (IllegalAccessException iae) {
            	throw new RuntimeException("Illegal access - see error logs.");
            } catch (InstantiationException ie) {
            	throw new RuntimeException("Unable to instantiate " + cls.getSimpleName());
            }
        }
        epo.setNewBean(newObj); // is this dangerous?
        return newObj;
    }
    
    private boolean populateObjectFromRequestParamsAndValidate(EditProcessObject epo, Object newObj, HttpServletRequest request) {
        boolean valid = true;
        String currParam="";
        Enumeration penum = request.getParameterNames();
        while (penum.hasMoreElements()){
            currParam = (String) penum.nextElement();
            if (!(currParam.indexOf("_")==0)){
                String currValue = request.getParameterValues(currParam)[0];
                // "altnew" values come in with the same input name but at position 1 of the array
                if(currValue.length()==0  && request.getParameterValues(currParam).length>1) {
                        currValue = request.getParameterValues(currParam)[1];
                }
                //validate the entry
                boolean fieldValid = true;
                if ( request.getParameter("_delete") == null ) { // don't do validation if we're deleting
                    List validatorList = (List) epo.getValidatorMap().get(currParam);
                    if (validatorList != null) {
                        Iterator valIt = validatorList.iterator();
                        String errMsg = "";
                        while (valIt.hasNext()){
                            Validator val = (Validator)valIt.next();
                            ValidationObject vo = val.validate(currValue);
                            if (!vo.getValid()){
                                valid = false;
                                fieldValid = false;
                                errMsg += vo.getMessage() + " ";
                                epo.getBadValueMap().put(currParam,currValue);
                            } else {
                                try {
                                    epo.getBadValueMap().remove(currParam);
                                    epo.getErrMsgMap().remove(currParam);
                                } catch (Exception e) {}
                            }
                        }
                        if (errMsg.length()>0) {
                            epo.getErrMsgMap().put(currParam,errMsg);
                            log.info("doPost() putting error message "+errMsg+" for "+currParam);
                        }
                    }
                }
                if (fieldValid){
                    if (currValue.length()==0) {
                        Map<String, String> defaultHash = epo.getDefaultValueMap();
                        try {
                            String defaultValue = defaultHash.get(currParam);
                            if (defaultValue != null)
                                currValue=defaultValue;
                        } catch (Exception e) {}
                    }
                    try {
                        FormUtils.beanSet(newObj,currParam,currValue,epo);
                        epo.getErrMsgMap().remove(currParam);
                        epo.getBadValueMap().remove(currParam);
                    } catch (NumberFormatException e) {
                        if (currValue.length()>0) {
                            valid = false;
                            epo.getErrMsgMap().put(currParam,"Please enter an integer");
                            epo.getBadValueMap().put(currParam,currValue);
                        }
                    } catch (NegativeIntegerException nie) {
                        valid = false;
                        epo.getErrMsgMap().put(currParam,"Please enter a positive integer");
                        epo.getBadValueMap().put(currParam,currValue);                    	
                    } catch (IllegalArgumentException f) {
                        valid=false;
                        log.error("doPost() reports IllegalArgumentException for "+currParam);
                        log.debug("doPost() error message: "+f.getMessage());
                        epo.getErrMsgMap().put(currParam, f.getMessage());
                        epo.getBadValueMap().put(currParam,currValue);
                    }
                }
            }
        }
        return valid;
    }
    
    private String getAction(HttpServletRequest request) {
    	if (request.getParameter("_update") != null ) {
        	return "update";
        } else if (request.getParameter("_delete") != null ) {
        	return "delete";
        } else {
        	return "insert";
        }
    }
    
    private void notifyChangeListeners(EditProcessObject epo, String action) {
    	List<ChangeListener> changeListeners = epo.getChangeListenerList();
        if (changeListeners != null){
            Iterator<ChangeListener> changeIt = changeListeners.iterator();
            while (changeIt.hasNext()) {
                ChangeListener cl = changeIt.next();
                if (action.equals("insert"))
                    cl.doInserted(epo.getNewBean(),epo);
                else if (action.equals("update"))
                    cl.doUpdated(epo.getOriginalBean(),epo.getNewBean(),epo);
                else if (action.equals("delete"))
                    cl.doDeleted(epo.getOriginalBean(),epo);
            }
        }
    }
    
    private boolean SUCCESS = false;
    private boolean FAILURE = !SUCCESS;
    
    private boolean performEdit(EditProcessObject epo, Object newObj, String action) {
    	/* do the actual edit operation */
        String partialClassName;
        if (epo.getBeanClass() != null) {
            partialClassName = epo.getBeanClass().getSimpleName();
        } else {
            partialClassName = epo.getNewBean().getClass().getSimpleName();
        }
        Object dataAccessObject = null;
        if (epo.getDataAccessObject() != null) {
            dataAccessObject = epo.getDataAccessObject();
        } else {
            throw new RuntimeException(OperationController.class.getName()+" needs to be passed an EPO containing a data access object with which to perform the desired operation");
        }
        Class[] classList = new Class[1];
        classList[0] = (epo.getBeanClass() != null) ? epo.getBeanClass() : newObj.getClass();
        newObj.getClass().getGenericSuperclass();
        Class[] superClassList = new Class[1];
        superClassList[0] = newObj.getClass().getSuperclass();
        Method meth=null;
        Method deleteMeth=null;
        Method insertMeth=null;
        
        // probably want to change this so it will walk up the class tree indefinitely looking for a good method to use
        if ("update".equals(action)){
        	if (epo.getUpdateMethod() != null) {
        		meth = epo.getUpdateMethod();
        	} else {
                try {
                    meth = dataAccessObject.getClass().getMethod("update"+partialClassName,classList);
                } catch (NoSuchMethodException e) {
                    try {
                        meth = dataAccessObject.getClass().getMethod("update"+partialClassName,superClassList);
                    } catch (NoSuchMethodException f) {
                        try {  // if there isn't a single update method, let's see if we can delete the old data and then insert the new
                            deleteMeth = dataAccessObject.getClass().getMethod("delete"+partialClassName,classList);
                            try {
                                insertMeth = dataAccessObject.getClass().getMethod("insert"+partialClassName,classList);
                            } catch (NoSuchMethodException ee) {
                                insertMeth = dataAccessObject.getClass().getMethod("insertNew"+partialClassName,classList);
                            }
                        } catch (NoSuchMethodException g) {
                            log.error("doPost() could not find method(s) for updating "+partialClassName);
                        }
                    }
                }
        	}
        } else if ("delete".equals(action)) {
        	if (epo.getDeleteMethod() != null) {
        		meth = epo.getDeleteMethod();
        	} else {
                try {
                    meth = dataAccessObject.getClass().getMethod("delete"+partialClassName,classList);
                } catch (NoSuchMethodException e) {
                    try {
                        meth = dataAccessObject.getClass().getMethod("delete"+partialClassName,superClassList);
                    } catch (NoSuchMethodException f) {
                        log.error("doPost() could not find method delete"+partialClassName+"() on "+dataAccessObject.getClass().getName());
                    }
                }
        	}
        } else {
        	if (epo.getInsertMethod() != null) {
        		meth = epo.getInsertMethod();
        	} else {
                try {
                    meth = dataAccessObject.getClass().getMethod("insert"+partialClassName,classList);
                } catch (NoSuchMethodException e) {
                    try {
                        meth = dataAccessObject.getClass().getMethod("insertNew"+partialClassName,classList);
                    } catch (NoSuchMethodException f) {
                        try {
                            meth = dataAccessObject.getClass().getMethod("insertNew"+partialClassName,superClassList);
                        } catch (NoSuchMethodException g) {
                            try {
                                meth = dataAccessObject.getClass().getMethod("insertNew"+partialClassName,superClassList);
                            } catch (NoSuchMethodException h) {
                                log.error("doPost() could not find method for inserting "+partialClassName);
                            }
                        }
                    }
                }
        	}
        }

        Object[] insArgList = new Object[1];
        insArgList[0] = newObj;

        Object result = null;

        if ( (meth == null) && action.equals("update") ) {
            //System.out.println("OperationController performing two-stage (deletion followed by insertion) update");
            try {
                Object[] delArgList = new Object[1];
                delArgList[0] = epo.getOriginalBean();
                deleteMeth.invoke(dataAccessObject,delArgList);
                insertMeth.invoke(dataAccessObject,insArgList);
            } catch (InvocationTargetException e) {
                log.error(this.getClass().getName()+" encountered exception performing two-stage update");
                Throwable innerE = e.getTargetException();
                log.error(innerE);
                if (innerE.getMessage()!=null) {
                	log.error(innerE.getMessage());
                	epo.setAttribute("globalErrorMsg",innerE.getMessage());
                }
                return FAILURE;
            } catch (IllegalAccessException iae) {
            	log.error(iae);
            	epo.setAttribute("globalErrorMessage", "Illegal access - see error logs.");
            	return FAILURE;
            }
        } else {
            try {
                result = meth.invoke(dataAccessObject,insArgList);
            } catch (InvocationTargetException e) {
            	log.error(this.getClass().getName()+" encountered exception performing edit action");
                Throwable innerE = e.getTargetException();
                //innerE.printStackTrace();
                log.error(innerE);
                if (innerE.getMessage()!=null) {
                    //System.out.println(innerE.getMessage());
                	log.error(innerE.getMessage());
                	epo.setAttribute("globalErrorMsg",innerE.getMessage());
                }
                return FAILURE;
            } catch (IllegalAccessException iae) {
            	log.error(iae);
            	epo.setAttribute("globalErrorMessage", "Illegal access - see error logs.");
            	return FAILURE;
            }
        }
        
        if (result != null) {
            // need to put the result of the insert in the id of the newbean
            try {
                Class[] setIdArgs = new Class[1];
                if (epo.getIdFieldClass() != null)
                    setIdArgs[0] = epo.getIdFieldClass();
                else
                    setIdArgs[0] = int.class;
                String idMutator = "set";
                if (epo.getIdFieldName() != null) {
                    idMutator += epo.getIdFieldName();
                } else {
                    idMutator += "Id";
                }
                Method setIdMeth = epo.getNewBean().getClass().getMethod(idMutator,setIdArgs);
                try {
                    Object[] idArg = new Object[1];
                    idArg[0] = result;
                    setIdMeth.invoke((Object)epo.getNewBean(),idArg);
                } catch (IllegalAccessException e) {
                    log.error("doPost() encountered IllegalAccessException setting id of new bean");
                } catch (InvocationTargetException f) {
                    log.error(f.getTargetException().getMessage());
                }
            } catch (NoSuchMethodException e) {
                //log.error("doPost() could not find setId() method for "+partialClassName);
            } catch (Exception f) {
                //log.error("doPost() could not set id of new bean.");
            }
        }
        
        return SUCCESS;
        
    }

}
