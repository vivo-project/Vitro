/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Handle the storing and retrieving referrer url.
 */
public class RefererHelper {
    private final String referrerId;
    private final String defaultBack;
    private final Set<String> ignoreUrls;

    RefererHelper() {
        this.referrerId = "referrer.helper";
        this.defaultBack = null;
        this.ignoreUrls = new HashSet<>();
    }

    RefererHelper(String referrerId, String defaultBack) {
        this.referrerId = "referrer." + referrerId;
        this.defaultBack = defaultBack;
        this.ignoreUrls = new HashSet<>();
    }

    RefererHelper(String referrerId, String defaultBack, Set<String> ignoreUrls) {
        this.referrerId = "referrer." + referrerId;
        this.defaultBack = defaultBack;
        this.ignoreUrls = (ignoreUrls != null) ? new HashSet<>(ignoreUrls) : new HashSet<>();
    }

    public void addIgnoreUrl(String url) {
        if (url != null) {
            ignoreUrls.add(url);
        }
    }

    public void removeIgnoreUrl(String url) {
        ignoreUrls.remove(url);
    }

    public void captureReferringUrl(VitroRequest vreq) {
        String referrer = vreq.getHeader("Referer");
        if (referrer != null && ignoreUrls.stream().noneMatch(referrer::contains)) {
            vreq.getSession().setAttribute(this.referrerId, referrer);
        }
    }

    public String getExitUrl(VitroRequest vreq) {
        String referrer = (String) vreq.getSession().getAttribute(referrerId);

        if (referrer != null) {
            return referrer;
        }

        return defaultBack;
    }
}
