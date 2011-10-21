/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;

/**
 * Information about the account of a user. URI, email, password, etc.
 * 
 * The "password link expires hash" is just a string that is derived from the
 * value in the passwordLinkExpires field. It doesn't have to be a hash, and
 * there is no need for it to be cryptographic, but it seems embarrassing to
 * just send the value as a clear string. There is no real need for security
 * here, except that a brute force attack would allow someone to change the
 * password on an account that they know has a password change pending.
 */
public class UserAccount {
	public static final int MIN_PASSWORD_LENGTH = 6;
	public static final int MAX_PASSWORD_LENGTH = 12;

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
	private long lastLoginTime = 0L; // Never negative.
	private Status status = Status.INACTIVE; // Might be null.
	private String externalAuthId = ""; // Never null.

	/** If this is true, the User Interface will not allow setting a password. */
	private boolean externalAuthOnly = false;

	/** This may be empty, but should never be null. */
	private Set<String> permissionSetUris = Collections.emptySet();

	private boolean rootUser = false;
	
	/** This may be empty, but should never be null. */
	private Set<String> proxiedIndividualUris = Collections.emptySet();

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

	public String getPasswordLinkExpiresHash() {
		return limitStringLength(8, Authenticator.applyMd5Encoding(String
				.valueOf(passwordLinkExpires)));
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

	public boolean isExternalAuthOnly() {
		return externalAuthOnly;
	}

	public void setExternalAuthOnly(Boolean externalAuthOnly) {
		this.externalAuthOnly = nonNull(externalAuthOnly, Boolean.FALSE);
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = Math.max(0, loginCount);
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = Math.max(0, lastLoginTime);
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

	public boolean isRootUser() {
		return rootUser;
	}

	public void setRootUser(boolean rootUser) {
		this.rootUser = rootUser;
	}

	public Set<String> getProxiedIndividualUris() {
		return new HashSet<String>(proxiedIndividualUris);
	}

	public void setProxiedIndividualUris(Collection<String> proxiedIndividualUris) {
		if (proxiedIndividualUris == null) {
			throw new NullPointerException("proxiedIndividualUris may not be null.");
		}
		this.proxiedIndividualUris = new HashSet<String>(proxiedIndividualUris);
	}

	private <T> T nonNull(T value, T defaultValue) {
		return (value == null) ? defaultValue : value;
	}

	private String limitStringLength(int limit, String s) {
		if (s == null) {
			return "";
		} else if (s.length() <= limit) {
			return s;
		} else {
			return s.substring(0, limit);
		}
	}

	@Override
	public String toString() {
		return "UserAccount[uri=" + uri + (", emailAddress=" + emailAddress)
				+ (", firstName=" + firstName) + (", lastName=" + lastName)
				+ (", md5password=" + md5Password)
				+ (", oldPassword=" + oldPassword)
				+ (", passwordLinkExpires=" + passwordLinkExpires)
				+ (", passwordChangeRequired=" + passwordChangeRequired)
				+ (", externalAuthOnly=" + externalAuthOnly)
				+ (", loginCount=" + loginCount) + (", status=" + status)
				+ (", lastLoginTime=" + lastLoginTime)
				+ (", externalAuthId=" + externalAuthId)
				+ (", rootUser=" + rootUser)
				+ (", permissionSetUris=" + permissionSetUris) + "]";
	}
}
