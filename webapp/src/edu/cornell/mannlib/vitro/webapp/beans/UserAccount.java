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
	public enum Status {
		ACTIVE, INACTIVE
	}

	private String uri = "";

	private String emailAddress = "";
	private String firstName = "";
	private String lastName = "";

	private String md5password = "";
	private long passwordChangeExpires = 0L;
	private int loginCount = 0;
	private Status status = Status.INACTIVE;

	private Set<String> permissionSetUris = Collections.emptySet();

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMd5password() {
		return md5password;
	}

	public void setMd5password(String md5password) {
		this.md5password = md5password;
	}

	public long getPasswordChangeExpires() {
		return passwordChangeExpires;
	}

	public void setPasswordChangeExpires(long passwordChangeExpires) {
		this.passwordChangeExpires = passwordChangeExpires;
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = loginCount;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Set<String> getPermissionSetUris() {
		return new HashSet<String>(permissionSetUris);
	}

	public void setPermissionSetUris(Collection<String> permissionSetUris) {
		this.permissionSetUris = new HashSet<String>(permissionSetUris);
	}

	@Override
	public String toString() {
		return "UserAccount[uri=" + uri + ", emailAddress=" + emailAddress
				+ ", firstName=" + firstName + ", lastName=" + lastName
				+ ", md5password=" + md5password + ", passwordChangeExpires="
				+ passwordChangeExpires + ", loginCount=" + loginCount
				+ ", status=" + status + ", permissionSetUris="
				+ permissionSetUris + "]";
	}
}
