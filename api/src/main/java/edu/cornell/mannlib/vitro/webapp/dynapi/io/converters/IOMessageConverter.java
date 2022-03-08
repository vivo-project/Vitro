package edu.cornell.mannlib.vitro.webapp.dynapi.io.converters;

import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;

import javax.servlet.http.HttpServletRequest;

public interface IOMessageConverter {

    Data loadDataFromRequest(HttpServletRequest request);
    String exportDataToResponseBody(ObjectData data);

}
