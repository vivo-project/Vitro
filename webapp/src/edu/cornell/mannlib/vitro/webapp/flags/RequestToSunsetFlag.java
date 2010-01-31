/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.flags;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.flags.SunsetFlag;
import edu.cornell.mannlib.vitro.webapp.flags.FlagException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 5, 2007
 * Time: 11:12:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class RequestToSunsetFlag {
    public static SunsetFlag SunsetFlag(HttpServletRequest req, ApplicationBean appBean) throws FlagException {
        SunsetFlag f = new SunsetFlag();
        if( appBean != null )
            f.filterBySunsetDate = appBean.isOnlyCurrent();
        return f;
    }
}
