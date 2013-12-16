/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.cache.TemplateLoader;

/**
 * Wrap a TemplateLoader, so each time a template is read, delimiters will be
 * inserted at the beginning and end. This makes it easier for a developer can
 * see what lines of HTML come from which templates.
 * 
 * TemplateLoader returns a token object when finding a template, and then
 * recognizes that object when it is used as an argument to getLastModified() or
 * getReader(). In order to keep track of the template name, we wrap the token
 * object and the name in a token of our own.
 * 
 * Taking the easy way out and reading in the entire template into a string.
 * This limits the template size to less than 2^31 characters (~2 GBytes). That
 * seems adequate.
 */
public class DelimitingTemplateLoader implements TemplateLoader {
	private static final Log log = LogFactory
			.getLog(DelimitingTemplateLoader.class);

	private final TemplateLoader innerLoader;

	public DelimitingTemplateLoader(TemplateLoader innerLoader) {
		this.innerLoader = innerLoader;
	}

	@Override
	public Object findTemplateSource(String name) throws IOException {
		Object innerTS = innerLoader.findTemplateSource(name);
		log.debug("template source for '" + name + "' is '" + innerTS + "'");
		if (innerTS == null) {
			return null;
		} else {
			return new DelimitingTemplateSource(name, innerTS);
		}
	}

	@Override
	public long getLastModified(Object templateSource) {
		DelimitingTemplateSource dts = (DelimitingTemplateSource) templateSource;
		return innerLoader.getLastModified(dts.ts);
	}

	@Override
	public Reader getReader(Object templateSource, String encoding)
			throws IOException {
		DelimitingTemplateSource dts = (DelimitingTemplateSource) templateSource;
		StringBuilder sb = new StringBuilder();
		sb.append("<!-- FM_BEGIN ").append(dts.name).append(" -->");
		sb.append(readTemplateSource(encoding, dts.ts));
		sb.append("<!-- FM_END ").append(dts.name).append(" -->\n");
		return new StringReader(sb.toString());
	}

	private StringBuilder readTemplateSource(String encoding, Object ts)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		Reader reader = innerLoader.getReader(ts, encoding);
		char[] buffer = new char[8192];
		int howmany;
		while (-1 != (howmany = reader.read(buffer))) {
			sb.append(buffer, 0, howmany);
		}
		return sb;
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
		DelimitingTemplateSource dts = (DelimitingTemplateSource) templateSource;
		innerLoader.closeTemplateSource(dts.ts);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * Data object, wrapping the template name and the templateSource object
	 * from the inner TemplateLoader.
	 */
	private static class DelimitingTemplateSource {
		public final String name;
		public final Object ts;

		public DelimitingTemplateSource(String name, Object ts) {
			if (name == null) {
				throw new NullPointerException("name may not be null.");
			}
			if (ts == null) {
				throw new NullPointerException("ts may not be null.");
			}
			this.name = name;
			this.ts = ts;
		}

		@Override
		public int hashCode() {
			return name.hashCode() ^ ts.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (o == null) {
				return false;
			}
			if (!o.getClass().equals(this.getClass())) {
				return false;
			}
			DelimitingTemplateSource that = (DelimitingTemplateSource) o;
			return this.name.equals(that.name) && this.ts.equals(that.ts);
		}
	}
}
