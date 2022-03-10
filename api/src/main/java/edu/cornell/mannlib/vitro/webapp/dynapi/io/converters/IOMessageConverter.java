package edu.cornell.mannlib.vitro.webapp.dynapi.io.converters;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;

public interface IOMessageConverter {

    Data loadDataFromRequest(HttpServletRequest request);

    String exportDataToResponseBody(ObjectData data);

}
