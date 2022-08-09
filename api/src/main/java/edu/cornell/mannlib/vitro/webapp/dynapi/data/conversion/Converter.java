package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpHeaders;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;

public class Converter {

	private static final Log log = LogFactory.getLog(Converter.class.getName());
	private static final String SPLIT_BY_COMMA_AND_TRIM_REGEX = "\\s*,\\s*";
	private static Set<ContentType> supportedContentTypes = new HashSet<>(
			Arrays.asList(ContentType.APPLICATION_JSON, ContentType.MULTIPART_FORM_DATA));

	public static void convert(HttpServletRequest request, Action action, DataStore dataStore)
			throws ConversionException {
		ContentType contentType = getContentType(request);
		ContentType responseType = getResponseType(request, contentType);
		dataStore.setResponseType(responseType);
		Set<LangTag> acceptLangs = getAcceptLanguages(request);
		dataStore.setAcceptLangs(acceptLangs);
		if (isJson(contentType)) {
			JSONConverter.convert(request, action, dataStore);
		} else if (isForm(contentType)) {
			FormDataConverter.convert(request, action, dataStore);
		} else {
			String message = String.format("No suitable converter found for input content type %s", contentType);
			throw new ConversionException(message);
		}
		convertInternalParams(action, dataStore);
	}

	private static void convertInternalParams(Action action, DataStore dataStore) throws ConversionException {
		Parameters params = action.getInternalParams();
		for (String name : params.getNames()) {
			RawData data = new RawData(params.get(name));
			data.earlyInitialization();
			dataStore.addData(name, data);
		}
	}

	private static ContentType getResponseType(HttpServletRequest request, ContentType requestType) {
		// TODO:Content negotiation: if accept header isn't empty, filter unsupported
		// types and select
		// type with highest weight. If no supported types found throw exception.
		// If accept header is empty, use request type
		// Set<ContentType> responseTypes = getAcceptContentTypes(request);
		return ContentType.APPLICATION_JSON;
	}

	public static void convert(HttpServletResponse response, Action action, OperationResult operationResult,
			DataStore dataStore) throws ConversionException {
		if (!operationResult.hasSuccess()) {
			operationResult.prepareResponse(response);
			return;
		}
		if (!action.isOutputValid(dataStore)) {
			response.setStatus(500);
			return;
		}
		operationResult.prepareResponse(response);
		// TODO: test accepted content types and prepare response according to it
		ContentType responseType = dataStore.getResponseType();
		if (isJson(responseType)) {
			JSONConverter.convert(response, action, dataStore);
		} else if (isForm(responseType)) {
			FormDataConverter.convert(response, action, dataStore);
		} else {
			String message = String.format("No suitable converter found for output content type %s", responseType);
			throw new ConversionException(message);
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
