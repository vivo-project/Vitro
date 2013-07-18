/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.servlet.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple stub for HttpServletResponse
 */
@SuppressWarnings("deprecation")
public class HttpServletResponseStub implements HttpServletResponse {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private String redirectLocation;
	private int status = 200;
	private String errorMessage;
	private Map<String, String> headers = new HashMap<String, String>();
	private String contentType;
	private String charset = "";

	private ByteArrayOutputStream outputStream;
	private StringWriter outputWriter;

	public String getRedirectLocation() {
		return redirectLocation;
	}

	public int getStatus() {
		return status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getOutput() {
		if (outputStream != null) {
			return outputStream.toString();
		} else if (outputWriter != null) {
			return outputWriter.toString();
		} else {
			return "";
		}
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public void sendRedirect(String location) throws IOException {
		this.redirectLocation = location;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	@SuppressWarnings("hiding")
	public void sendError(int status) throws IOException {
		this.status = status;
	}

	@Override
	@SuppressWarnings("hiding")
	public void sendError(int status, String message) throws IOException {
		this.status = status;
		this.errorMessage = message;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (outputStream != null) {
			throw new IllegalStateException(
					"Can't get a Writer after getting an OutputStream.");
		}

		if (outputWriter == null) {
			outputWriter = new StringWriter();
		}

		return new PrintWriter(outputWriter, true);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (outputWriter != null) {
			throw new IllegalStateException(
					"Can't get an OutputStream after getting a Writer.");
		}

		if (outputStream == null) {
			outputStream = new ByteArrayOutputStream();
		}

		return new ServletOutputStream() {
			@Override
			public void write(int thisChar) throws IOException {
				outputStream.write(thisChar);
			}
		};
	}

	@Override
	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	@Override
	public boolean containsHeader(String name) {
		return headers.containsKey(name);
	}

	/**
	 * Calling setContentType("this/type;charset=UTF-8") is the same as calling
	 * setContentType("this/type;charset=UTF-8"); setCharacterEncoding("UTF-8")
	 */
	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
		
		Pattern p = Pattern.compile(";\\scharset=([^;]+)");
		Matcher m = p.matcher(contentType);
		if (m.find()) {
			this.charset = m.group(1);
		}
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		this.charset = charset;
	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void flushBuffer() throws IOException {
		throw new RuntimeException(
				"HttpServletResponseStub.flushBuffer() not implemented.");
	}

	@Override
	public int getBufferSize() {
		throw new RuntimeException(
				"HttpServletResponseStub.getBufferSize() not implemented.");
	}

	@Override
	public Locale getLocale() {
		throw new RuntimeException(
				"HttpServletResponseStub.getLocale() not implemented.");
	}

	@Override
	public boolean isCommitted() {
		throw new RuntimeException(
				"HttpServletResponseStub.isCommitted() not implemented.");
	}

	@Override
	public void reset() {
		throw new RuntimeException(
				"HttpServletResponseStub.reset() not implemented.");
	}

	@Override
	public void resetBuffer() {
		throw new RuntimeException(
				"HttpServletResponseStub.resetBuffer() not implemented.");
	}

	@Override
	public void setBufferSize(int arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setBufferSize() not implemented.");
	}

	@Override
	public void setContentLength(int arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setContentLength() not implemented.");
	}

	@Override
	public void setLocale(Locale arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setLocale() not implemented.");
	}

	@Override
	public void addCookie(Cookie arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.addCookie() not implemented.");
	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.addDateHeader() not implemented.");
	}

	@Override
	public void addHeader(String arg0, String arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.addHeader() not implemented.");
	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.addIntHeader() not implemented.");
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.encodeRedirectURL() not implemented.");
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.encodeRedirectUrl() not implemented.");
	}

	@Override
	public String encodeURL(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.encodeURL() not implemented.");
	}

	@Override
	public String encodeUrl(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.encodeUrl() not implemented.");
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.setDateHeader() not implemented.");
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.setIntHeader() not implemented.");
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.setStatus() not implemented.");
	}

}
