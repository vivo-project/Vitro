/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handle the storing and retreiving referrer url.
 */
public class ReferrerHelper {
	private static final Log log = LogFactory.getLog(ReferrerHelper.class);

	private final String referrerId;
	private final String defaultBack;

	ReferrerHelper() {
		this.referrerId = "referrer.helper";
		this.defaultBack = null;
	}

	ReferrerHelper(String referrerId, String defaultBack) {
		this.referrerId = "referrer." + referrerId;
		this.defaultBack = defaultBack;
	}

	public void captureReferringUrl(VitroRequest vreq) {
		String referrer = vreq.getHeader("Referer");
		if (referrer == null) {
			vreq.getSession().removeAttribute(this.referrerId);
		} else {
			vreq.getSession().setAttribute(this.referrerId, referrer);
		}
	}

	public String getExitUrl(VitroRequest vreq) {
		String referrer = (String) vreq.getSession().getAttribute(
				referrerId);

		if (referrer != null) {
			return referrer;
		}

		return defaultBack;
    }

}
