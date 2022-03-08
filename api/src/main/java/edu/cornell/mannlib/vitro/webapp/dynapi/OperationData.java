package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.io.converters.IOJsonMessageConverter;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class OperationData {

	private final Map<String, String[]> params;
	private final ServletContext context;
	private ObjectData data;

	public OperationData(HttpServletRequest request) {
		params = request.getParameterMap();
		context = request.getServletContext();
		data = IOJsonMessageConverter.getInstance().loadDataFromRequest(request);
	}

	public ServletContext getContext() {
		return context;
	}

	public ObjectData getRootData(){
		return data;
	}

	public Data getData(String paramName){
		return data.getElement(paramName);
	}

	public boolean has(String paramName) {
		Data data = getData(paramName);
		return data != null || params.containsKey(paramName);
	}

	public String[] get(String paramName) {
		String[] retVal = new String[0];
		Data internalData= getData(paramName);
		if (internalData != null){
			List<String> listString = internalData.getAsString();
			retVal = (listString != null)?listString.toArray(new String[0]):retVal;
		} else {
			retVal = params.get(paramName);
		}
		return retVal;
	}

	public void add(String key, Data newData){
		data.setElement(key, newData);
	}

}
