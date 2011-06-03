/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.validator.ValidationObject;
import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageUserAccounts;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;

public class UserRetryController extends BaseEditController {

    private static final String ROLE_PROTOCOL = "role:/";  // this is weird; need to revisit
    private static final Log log = LogFactory.getLog(UserRetryController.class.getName());

    @Override
    public void doPost (HttpServletRequest req, HttpServletResponse response) {
    	if (!isAuthorizedToDisplayPage(req, response, new Actions(new ManageUserAccounts()))) {
    		return;
    	}

    	VitroRequest request = new VitroRequest(req);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);
        epo.setDataAccessObject(request.getFullWebappDaoFactory().getVClassDao());

        String action = null;
        if (epo.getAction() == null) {
            action = "insert";
            epo.setAction("insert");
        } else {
            action = epo.getAction();
        }

        UserDao uDao = request.getFullWebappDaoFactory().getUserDao();
        epo.setDataAccessObject(uDao);

        User userForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("uri") != null) {
                try {
                    userForEditing = uDao.getUserByURI(request.getParameter("uri"));
                    userForEditing.setRoleURI(ROLE_PROTOCOL+userForEditing.getRoleURI());
                    action = "update";
                    epo.setAction("udpate");
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
            } else {
                userForEditing = new User();
                userForEditing.setRoleURI(ROLE_PROTOCOL+"1");
            }
            epo.setOriginalBean(userForEditing);
        } else {
            userForEditing = (User) epo.getNewBean();
        }

        populateBeanFromParams(userForEditing, request);

        //validators
        Validator v = new PairedPasswordValidator();
        HashMap<String, List<Validator>> validatorMap = new HashMap<String, List<Validator>>();
        List<Validator> vList = Collections.singletonList(v);
		validatorMap.put("Md5password", vList);
		validatorMap.put("passwordConfirmation", vList);
        epo.setValidatorMap(validatorMap);

        //preprocessors
       
        //set up any listeners
        epo.setChangeListenerList(Collections.singletonList(new UserPasswordChangeListener()));

        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new UserInsertPageForwarder());
        //make a postdelete pageforwarder that will send us to the list of classes
        epo.setPostDeletePageForwarder(new UrlForwarder("listUsers"));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class<?>[] args = new Class[] {String.class};
            epo.setGetMethod(uDao.getClass().getDeclaredMethod("getUserByURI",args));
        } catch (NoSuchMethodException e) {
            log.error(this.getClass().getName()+" could not find the getVClassByURI method");
        }

        HashMap<String, List<Option>> optionMap = new HashMap<String, List<Option>>();

        LoginStatusBean loginBean = LoginStatusBean.getBean(request);
        List<Option> roleOptionList = new LinkedList<Option>();
        
        /* bdc34: Datastar needs non-backend-editing users for logging in non-Cornell people*/
        /* SelfEditingPolicySetup.SELF_EDITING_POLICY_WAS_SETUP is set by the SelfEditingPolicySetup context listener */
        Option nonEditor = new Option(ROLE_PROTOCOL+1, "self editor");
        /* self editing should be displayed if we are editing a user account that is already  
         *  self-editing even if self editing is off. */
        roleOptionList.add(nonEditor); 
        
        Option editor = new Option(ROLE_PROTOCOL+4, "editor");
        editor.setSelected(userForEditing.getRoleURI().equals(editor.getValue()));
        Option curator = new Option(ROLE_PROTOCOL+5, "curator");
        curator.setSelected(userForEditing.getRoleURI().equals(curator.getValue()));
        Option administrator = new Option (ROLE_PROTOCOL+50, "system administrator");
        administrator.setSelected(userForEditing.getRoleURI().equals(administrator.getValue()));        
        
        roleOptionList.add(editor);
        roleOptionList.add(curator);
        roleOptionList.add(administrator);

        optionMap.put("Role", roleOptionList);

        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());
        foo.setOptionLists(optionMap);
        epo.setFormObject(foo);

        request.setAttribute("formValue",foo.getValues());

        String html = FormUtils.htmlFormFromBean(userForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("user",userForEditing);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        if (userForEditing.getMd5password()==null || userForEditing.getMd5password().equals("")) {
            request.setAttribute("formOnSubmit", "return validatePw(this);");
            request.setAttribute("formOnCancel", "forceCancel(this.form);");
        }
       else {
            request.setAttribute("formOnSubmit", "return validateUserFields(this);");
            request.setAttribute("formOnCancel", "forceCancelTwo(this.form);");
        }

        request.setAttribute("formJsp","/templates/edit/specific/user_retry.jsp");
        request.setAttribute("scripts","/templates/edit/specific/user_retry_head.jsp");
        request.setAttribute("title","User Account Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","User");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    @Override
	public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    class UserInsertPageForwarder implements PageForwarder {

        @Override
		public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newUserUrl = "userEdit?uri=";
            User u = (User) epo.getNewBean();
            try {
                newUserUrl += URLEncoder.encode(u.getURI(),"UTF-8");
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not use UTF-8 encoding to encode new URL");
            }
            try {
                response.sendRedirect(newUserUrl);
            } catch (IOException ioe) {
                log.error(this.getClass().getName()+" could not send redirect.");
            }
        }
    }

    /**
     * Create one of these and assign it to both password fields.
     */
    class PairedPasswordValidator implements Validator {
    	private String otherValue;
    	
		/**
		 * Validate the length of this password, and stash it for the other
		 * validator to compare to.
		 * 
		 * This relies on the fact that {@link #validate(Object)} will be called
		 * once for each of the password fields.
		 */
		@Override
		public ValidationObject validate(Object value)
				throws IllegalArgumentException {
			log.trace("validate password pair: " + value + ", " + otherValue);

			// Must be a non-null String
			if (!(value instanceof String)) {
				log.trace("not a string: " + value);
				return ValidationObject.failure(value, "Please enter a value");
			}

			// Must be within the length limits.
			String string = (String) value;
			if ((string.length() < User.MIN_PASSWORD_LENGTH)
					|| (string.length() > User.MAX_PASSWORD_LENGTH)) {
				log.trace("bad length: " + value);
				return ValidationObject.failure(value,
						"Please enter a password between "
								+ User.MIN_PASSWORD_LENGTH + " and "
								+ User.MAX_PASSWORD_LENGTH
								+ " characters long.");
			}

			// If we haven't validate the other yet, just store this value.
			if (otherValue == null) {
				log.trace("first of the pair: " + value);
				otherValue = string;
				return ValidationObject.success(value);
			}

			// Compare this value to the stored one.
			String otherString = otherValue;
			otherValue = null;
			if (string.equals(otherString)) {
				log.trace("values are equal: " + value);
				return ValidationObject.success(value);
			} else {
				log.trace("values are not equal: " + value + ", " + otherValue);
				return ValidationObject.failure(value,
						"The passwords do not match.");
			}
		}
    }

    /**
     * When a new password is created, encode it.
     */
	class UserPasswordChangeListener implements ChangeListener {
		/** 
		 * Encode the password for a new user.
		 */
		@Override
		public void doInserted(Object newObj, EditProcessObject epo) {
			try {
				User newUser = convertToUser(newObj);
				UserDao userDao = getUserDaoFromEPO(epo);
				encodePasswordAndUpdateUser("insert", newUser, userDao);
			} catch (PwException e) {
				log.error(e.getMessage());
			}
		}

		/**
		 * Encode the password for an updated user, if it has changed.
		 */
		@Override
		public void doUpdated(Object oldObj, Object newObj,
				EditProcessObject epo) {
			try {
				User newUser = convertToUser(newObj);
				User oldUser = convertToUser(oldObj);
				UserDao userDao = getUserDaoFromEPO(epo);
				if (passwordHasChanged(newUser, oldUser)) {
					encodePasswordAndUpdateUser("update", newUser, userDao);
				} else {
					log.debug("update: password has not changed.");
				}
			} catch (PwException e) {
				log.error(e.getMessage());
			}
		}

		/** 
		 * Do nothing for a deleted user.
		 */
		@Override
		public void doDeleted(Object oldObj, EditProcessObject epo) {
			log.debug("delete: nothing to do");
		}
		
		private User convertToUser(Object o) throws PwException {
			if (o instanceof User) {
				return (User) o;
			} else {
				throw new PwException("Can't apply password encoding without a "
						+ "User object: " + o);
			}
		}

		private UserDao getUserDaoFromEPO(EditProcessObject epo)
				throws PwException {
			if (epo == null) {
				throw new PwException(
						"Can't apply password encoding without an "
								+ "EditProcessObject");
			}
			
			Object dao = epo.getDataAccessObject();
			
			if (dao instanceof UserDao) {
				return (UserDao) dao;
			} else {
				throw new PwException(
						"Can't apply password encoding without a "
								+ "UserDao object: " + dao);
			}
		}

		private boolean passwordHasChanged(User newUser, User oldUser)
				throws PwException {
			String newPw = newUser.getMd5password();
			String oldPw = oldUser.getMd5password();
			if (newPw == null) {
				throw new PwException("Can't encode a null password");
			}
			return !newPw.equals(oldPw);
		}

		private void encodePasswordAndUpdateUser(String action, User user, UserDao userDao) {
			String rawPassword = user.getMd5password();
			if (rawPassword == null) {
				log.error("Can't encode a null password");
			}

			String encodedPassword = Authenticator.applyMd5Encoding(rawPassword);
			log.trace(action + ": Raw password '" + rawPassword
					+ "', encoded '" + encodedPassword + "'");

			user.setMd5password(encodedPassword);

			userDao.updateUser(user);
		}
	}
	
	class PwException extends Exception {
		public PwException(String message) {
			super(message);
		}
	}
	
}
