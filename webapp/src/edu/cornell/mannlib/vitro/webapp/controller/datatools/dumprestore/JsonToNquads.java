/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An output stream decorator that converts a stream of
 * application/sparql-results+json to a stream of application/n-quads
 * 
 * This could be a lot more efficient.
 */
public class JsonToNquads extends OutputStream {
	private static final Log log = LogFactory.getLog(JsonToNquads.class);

	private final Writer writer;
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private final ByteArrayOutputStream header = new ByteArrayOutputStream();

	private boolean headerIsComplete;
	private long recordCount;

	public JsonToNquads(OutputStream out) throws IOException {
		this.writer = new OutputStreamWriter(out, "UTF-8");
		log.info("Dump beginning.");
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public void close() throws IOException {
		writer.close();
		log.info("Dump is complete: " + recordCount + " records.");
		log.debug("Left over in the buffer: '" + buffer + "'");
	}

	@Override
	public void write(int b) throws IOException {
		if (!headerIsComplete) {
			writeToHeader(b);
		} else {
			buffer.write(b);
			if (bufferHoldsARecord()) {
				processRecord();
				buffer.reset();
			}
		}
	}

	private void writeToHeader(int b) {
		header.write((byte) b);
		String text = header.toString();
		int bindingsHere = text.indexOf("\"bindings\"");
		int lastColonHere = text.lastIndexOf(":");
		int lastOpenBracket = text.lastIndexOf("[");
		headerIsComplete = (bindingsHere >= 0)
				&& (lastColonHere > bindingsHere)
				&& (lastOpenBracket > lastColonHere);
		log.debug("complete=" + headerIsComplete + ", header='" + text + "'");
	}

	private boolean bufferHoldsARecord() throws IOException {
		String text = buffer.toString("UTF-8");
		boolean inQuotes = false;
		int braceLevel = 0;
		char previous = 0;
		for (char c : text.toCharArray()) {
			if (inQuotes) {
				if ((c == '"') && (previous != '\\')) {
					inQuotes = false;
				}
			} else {
				if (c == '"') {
					inQuotes = true;
				} else if (c == '{') {
					braceLevel++;
				} else if (c == '}') {
					braceLevel--;
				}
			}
			previous = c;
		}
		return (braceLevel == 0) && (text.endsWith(",") || text.endsWith("]"));
	}

	private void processRecord() throws IOException {
		String text = buffer.toString("UTF-8");
		log.debug("Parsing record: '" + text + "'");
		try (JsonReader jsRead = Json.createReader(new StringReader(text))) {
			JsonObject jsRecord = jsRead.readObject();
			DumpNode s = DumpNode.fromJson(jsRecord.getJsonObject("s"));
			DumpNode p = DumpNode.fromJson(jsRecord.getJsonObject("p"));
			DumpNode o = DumpNode.fromJson(jsRecord.getJsonObject("o"));
			DumpNode g = DumpNode.fromJson(jsRecord.getJsonObject("g"));

			if (g == null) {
				writer.write(String.format("%s %s %s .\n", s.toNquad(),
						p.toNquad(), o.toNquad()));
			} else {
				writer.write(String.format("%s %s %s %s .\n", s.toNquad(),
						p.toNquad(), o.toNquad(), g.toNquad()));
			}

			recordCount++;
			if (recordCount % 10000 == 0) {
				log.info("dumped " + recordCount + " records.");
			}
		} catch (Exception e) {
			log.error("Failed to parse record: '" + text + "'", e);
			throw new RuntimeException(e);
		}
	}
}
