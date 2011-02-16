<?xml version="1.0" encoding="UTF-8"?>

<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jstl/core"  xmlns:form="http://vitro.mannlib.cornell.edu/edit/tags" version="2.0">

	<tr class="editformcell">
		<td valign="bottom" colspan="2">
			<b>Email address*</b><br/>
			<input type="text" name="Username" value="${formValue['Username']}" size="60" maxlength="120" />
			<span class="warning"><form:error name="Username"/></span>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="1">
			<b>First Name*</b><br/>
                        <input type="text" name="FirstName" value="${formValue['FirstName']}" size="30" maxlength="120"/>
			<span class="warning"><form:error name="FirstName"/></span>
		</td>
                <td valign="bottom" colspan="1">
                        <b>Last Name*</b><br/>
                        <input type="text" name="LastName" value="${formValue['LastName']}" size="30" maxlength="120"/>
                        <span class="warning"><form:error name="LastName"/></span>
                </td>
	</tr>
	<tr class="editformcell">
                <td valign="bottom" colspan="2">
                        <b>Role*</b><br/>
                        <select name="RoleURI">
                            <form:option name="Role"/>
                        </select>
                        <span class="warning"><form:error name="Role"/></span>
                </td>
	</tr>
        <c:if test="${empty user.md5password}">
          <tr class="editformcell">
              <td valign="bottom" colspan="2">
                      <b>Temporary Password* (6-12 characters; must be changed on first login)</b><br/>
                      <input type="password" name="Md5password" value="${formValue['Md5password']}" size="64" maxlength="128"/>
                      <span class="warning"><form:error name="Md5password"/></span>
              </td>
          </tr>
          <tr class="editformcell">
              <td valign="bottom" colspan="2">
                      <b>Confirm password*</b><br/>
                      <input type="password" name="passwordConfirmation" value="${formValue['passwordConfirmation']}" size="64" maxlength="128"/>
                      <span class="warning"><form:error name="passwordConfirmation"/></span>
              </td>
          </tr>
        </c:if>
</jsp:root>
