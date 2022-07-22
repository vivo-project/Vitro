package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.io.IOException;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpHeaders;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.converters.IOJsonMessageConverter;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;

public class Converter {

	private static final Log log = LogFactory.getLog(Converter.class.getName());
	private static final String SPLIT_BY_COMMA_AND_TRIM_REGEX = "\\s*,\\s*";

	public static void convert(HttpServletRequest request, Action action, DataStore dataStore)
			throws ConversionException {
		ContentType contentType = getContentType(request);
		Set<ContentType> returnTypes = getAcceptContentTypes(request);
		dataStore.setAcceptedContentTypes(returnTypes);
		Set<LangTag> acceptLangs = getAcceptLanguages(request);
		dataStore.setAcceptLangs(acceptLangs);
		if (isJson(contentType)) {
			JSONConverter.convert(request, action, dataStore);
		} else if (isForm(contentType)) {
			FormDataConverter.convert(request, action, dataStore);
		} else {
			String message = String.format("No suitable converter found for content type %s", contentType);
			throw new ConversionException(message);
		}
	}
	
	public static void convert(HttpServletResponse response, Action action, DataStore dataStore, OperationResult result) 
			throws ConversionException{
		
	}
	
	public static void prepareResponse(HttpServletResponse response, String contentType, Action action,
            OperationResult operationResult, OperationData operationData) {
        if (!operationResult.hasSuccess()) {
        	operationResult.prepareResponse(response);
        	return;
        }
    	if (!action.isOutputValid(operationData)) {
            response.setStatus(500);
            return;
    	}
    	operationResult.prepareResponse(response);
        if (contentType == null || !contentType.equalsIgnoreCase(edu.cornell.mannlib.vitro.webapp.web.ContentType.JSON.getMediaType())) {
        	return;
        } 
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
        response.setContentType(edu.cornell.mannlib.vitro.webapp.web.ContentType.JSON.getMediaType());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (out != null) {
            ObjectData resultData = operationData.getRootData().filter(action.getProvidedParams().getNames());
            out.print(IOJsonMessageConverter.getInstance().exportDataToResponseBody(resultData));
            out.flush();
        }
    }

	private static Set<LangTag> getAcceptLanguages(HttpServletRequest request) {
		Set<LangTag> result = new HashSet<>();
		String header = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
		if (StringUtils.isBlank(header)) {
			// Default
			result.add(new LangTag("*"));
			return result;
		}
		String[] rawTags = header.trim().split(SPLIT_BY_COMMA_AND_TRIM_REGEX);
		for (String rawTag : rawTags) {
			result.add(new LangTag(rawTag));
		}
		return result;
	}

	private static Set<ContentType> getAcceptContentTypes(HttpServletRequest request) {
		Set<ContentType> result = new HashSet<>();
		String header = request.getHeader(HttpHeaders.ACCEPT);
		if (StringUtils.isBlank(header)) {
			// Default
			result.add(ContentType.APPLICATION_JSON);
			return result;
		}
		String[] types = header.trim().split(SPLIT_BY_COMMA_AND_TRIM_REGEX);
		for (String type : types) {
			ContentType contentType = ContentType.parse(type);
			result.add(contentType);
		}
		return result;
	}

	private static ContentType getContentType(HttpServletRequest request) {
		String header = request.getContentType();
		if (StringUtils.isBlank(header)) {
			return ContentType.MULTIPART_FORM_DATA;
		}
		return ContentType.parse(header);
	}

	private static boolean isForm(ContentType type) {
		if (ContentType.MULTIPART_FORM_DATA.getMimeType().equals(type.getMimeType())) {
			return true;
		}
		return false;
	}

	private static boolean isJson(ContentType type) {
		if (ContentType.APPLICATION_JSON.getMimeType().equals(type.getMimeType())) {
			return true;
		}
		return false;
	}

}
