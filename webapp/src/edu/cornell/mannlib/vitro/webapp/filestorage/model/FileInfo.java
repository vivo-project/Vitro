/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.model;

/**
 * An immutable packet of information about an uploaded file, with a builder
 * class to permit incremental construction.
 */
public class FileInfo {
	private final String uri;
	private final String filename;
	private final String mimeType;
	private final String bytestreamUri;
	private final String bytestreamAliasUrl;

	private FileInfo(Builder builder) {
		this.uri = builder.uri;
		this.filename = builder.filename;
		this.mimeType = builder.mimeType;
		this.bytestreamUri = builder.bytestreamUri;
		this.bytestreamAliasUrl = builder.bytestreamAliasUrl;
	}

	public String getUri() {
		return uri;
	}

	public String getFilename() {
		return filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getBytestreamUri() {
		return bytestreamUri;
	}

	public String getBytestreamAliasUrl() {
		return bytestreamAliasUrl;
	}

	@Override
	public String toString() {
		return "FileInfo[uri=" + uri + ", filename=" + filename + ", mimeType="
				+ mimeType + ", bytestreamUri=" + bytestreamUri + ", aliasUrl="
				+ bytestreamAliasUrl + "]";
	}

	/**
	 * A builder class allows us to supply the values one at a time, and then
	 * freeze them into an immutable object.
	 */
	public static class Builder {
		private String uri;
		private String filename;
		private String mimeType;
		private String bytestreamUri;
		private String bytestreamAliasUrl;

		public Builder setUri(String uri) {
			this.uri = uri;
			return this;
		}

		public Builder setFilename(String filename) {
			this.filename = filename;
			return this;
		}

		public Builder setMimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}

		public Builder setBytestreamUri(String bytestreamUri) {
			this.bytestreamUri = bytestreamUri;
			return this;
		}

		public Builder setBytestreamAliasUrl(String bytestreamAliasUrl) {
			this.bytestreamAliasUrl = bytestreamAliasUrl;
			return this;
		}

		public FileInfo build() {
			return new FileInfo(this);
		}
	}
}
