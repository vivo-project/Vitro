/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.web.ContentType;

public class FileResponseValues extends BaseResponseValues {
    
	private String outputFileName; 
	private String DEFAULT_HEADER_KEY = "Content-Disposition";
	private String DEFAULT_HEADER_VALUE_PREFIX = "attachment;filename=";
	private final Map<String, Object> map;
	
	
    public FileResponseValues(ContentType contentType, String outputFileName, Map<String, Object> map) {
        super(contentType);
        this.outputFileName = outputFileName;
        this.map = map;
    }
    
    @Override
    public Map<String, String> getHeader() {
    	Map<String, String> headerKeyToValue = new HashMap<String, String>();
    	headerKeyToValue.put(DEFAULT_HEADER_KEY, DEFAULT_HEADER_VALUE_PREFIX + outputFileName);
    	return headerKeyToValue;
    }
    
    @Override
    public Map<String, Object> getMap() {
        return Collections.unmodifiableMap(this.map);
    }

}
