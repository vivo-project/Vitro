package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpHeaders;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public class Converter {

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
