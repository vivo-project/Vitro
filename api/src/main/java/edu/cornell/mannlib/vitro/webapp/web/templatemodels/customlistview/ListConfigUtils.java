/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.customlistview;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.sdb.RDFServiceSDB;
import org.apache.commons.lang3.StringUtils;

public final class ListConfigUtils {
    private static Boolean usePrecise = null;

    public static final boolean getUsePreciseSubquery(VitroRequest vreq) {
        if (usePrecise == null) {
            String usePreciseProp = ConfigurationProperties.getBean(vreq).getProperty("listview.usePreciseSubquery");
            if (!StringUtils.isEmpty(usePreciseProp)) {
                usePrecise = Boolean.parseBoolean(usePreciseProp);
            } else if (vreq != null && vreq.getRDFService() != null) {
                usePrecise = vreq.getRDFService().preferPreciseOptionals();
            } else {
                usePrecise = false;
            }
        }

        return usePrecise;
    }
}
