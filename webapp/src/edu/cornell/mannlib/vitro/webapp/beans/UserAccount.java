/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Information about the account of a user. URI, email, password, etc.
 */
public class UserAccount {

	public final static int MIN_PASSWORD_LENGTH = 6;
	public final static int MAX_PASSWORD_LENGTH = 12;

	public enum Status {
		ACTIVE, INACTIVE;

		public static Status fromString(String s) {
			if (s == null) {
				return null;
			}

			for (Status status : Status.values()) {
				if (status.toString().equals(s)) {
					return status;
				}
			}

			return null;
		}

	}

	private String uri = ""; // Never null.

	private String emailAddress = ""; // Never null.
	private String firstName = ""; // Never null.
	private String lastName = ""; // Never null.

	private String md5Password = ""; // Never null.
	private String oldPassword = ""; // Never null.
	private long passwordLinkExpires = 0L; // Never negative.
	private boolean passwordChangeRequired = false;

	private int loginCount = 0; // Never negative.
	private Status status = Status.INACTIVE; // Might be null.
	private String externalAuthId = ""; // Never null.

	/** This may be empty, but should never be null. */
	private Set<String> permissionSetUris = Collections.emptySet();

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		if (uri == null) {
			throw new NullPointerException("uri may not be null.");
		}
		this.uri = uri;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = nonNull(emailAddress, "");
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = nonNull(firstName, "");
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = nonNull(lastName, "");
	}

	public String getMd5Password() {
		return md5Password;
	}

	public void setMd5Password(String md5Password) {
		this.md5Password = nonNull(md5Password, "");
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = nonNull(oldPassword, "");
	}

	public long getPasswordLinkExpires() {
		return passwordLinkExpires;
	}

	public void setPasswordLinkExpires(long passwordLinkExpires) {
		this.passwordLinkExpires = Math.max(0, passwordLinkExpires);
	}

	public boolean isPasswordChangeRequired() {
		return passwordChangeRequired;
	}

	public void setPasswordChangeRequired(Boolean passwordChangeRequired) {
		this.passwordChangeRequired = nonNull(passwordChangeRequired,
				Boolean.FALSE);
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = Math.max(0, loginCount);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setStatusFromString(String statusString) {
		this.status = Status.fromString(statusString);
	}

	public String getExternalAuthId() {
		return externalAuthId;
	}

	public void setExternalAuthId(String externalAuthId) {
		this.externalAuthId = nonNull(externalAuthId, "");
	}

	public Set<String> getPermissionSetUris() {
		return new HashSet<String>(permissionSetUris);
	}

	public void setPermissionSetUris(Collection<String> permissionSetUris) {
		if (permissionSetUris == null) {
			throw new NullPointerException("permissionSetUris may not be null.");
		}
		this.permissionSetUris = new HashSet<String>(permissionSetUris);
	}

	private <T> T nonNull(T value, T defaultValue) {
		return (value == null) ? defaultValue : value;
	}

	@Override
	public String toString() {
		return "UserAccount[uri=" + uri + (", emailAddress=" + emailAddress)
				+ (", firstName=" + firstName) + (", lastName=" + lastName)
				+ (", md5password=" + md5Password)
				+ (", oldPassword=" + oldPassword)
				+ (", passwordLinkExpires=" + passwordLinkExpires)
				+ (", passwordChangeRequired=" + passwordChangeRequired)
				+ (", loginCount=" + loginCount) + (", status=" + status)
				+ (", externalAuthId=" + externalAuthId)
				+ (", permissionSetUris=" + permissionSetUris) + "]";
	}
}
