package edu.cornell.mannlib.vitro.webapp.flags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;


/**
 * SunsetFlag to indicate if entities should be filtered so that
 * only those where now() < sunset are shown.
 * Entity sunrise values are typically only used for news releases as a
 * way to stop pulling releases into a tab 60 or 90 days after release.
 * There is no anticipated support for using the sunrise value to enter
 * data to appear in the future or to filter entities globally the way
 * the sunset value will be used.
 */
public class SunsetFlag {
    public boolean filterBySunsetDate = false;
    public  boolean isFilterBySunsetDate()       { return filterBySunsetDate;   }
    public void setFilterBySunsetDate(boolean b) { this.filterBySunsetDate = b; }

}
