/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;

public class UserOnThread implements AutoCloseable {

    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public UserOnThread(String userId) {
        threadLocal.set(userId);
    }

    public UserOnThread(HttpServletRequest vreq) {
        threadLocal.set(PolicyHelper.getUserAccount(vreq).getUri());
    }

    @Override
    public void close() {
        threadLocal.remove();
    }

    public static String get() {
        return threadLocal.get();
    }

}
