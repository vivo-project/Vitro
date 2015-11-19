/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.freemarker.cache;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import freemarker.cache.TemplateLoader;

/**
 * A simple implementation where the templates are stored a strings in a map
 * instead of as files, and where the "template source" objects are just the
 * template names.
 */
public class TemplateLoaderStub implements TemplateLoader {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, String> templateMap = new HashMap<String, String>();

	public void createTemplate(String name, String contents) {
		templateMap.put(name, contents);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public Object findTemplateSource(String name) throws IOException {
		if (templateMap.containsKey(name)) {
			return name;
		} else {
			return null;
		}
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
		// Nothing to close
	}

	@Override
	public long getLastModified(Object templateSource) {
		return -1;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding)
			throws IOException {
		return new StringReader(templateMap.get(templateSource));
	}

}
