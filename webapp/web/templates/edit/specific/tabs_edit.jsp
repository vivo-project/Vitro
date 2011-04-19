<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<vitro:requiresAuthorizationFor classNames="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseTabEditorPages" />
<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

<%
/**
 *
 * @version 1.00
 * @author Jon Corson-Rikert
 *
 * UPDATES:
 * BJL 2007-XX-XX : significant modifications for semweb-align (removed SQL tags, etc.)
 * JCR 2006-03-05 : modified references to ETypes and change property file used to display auto-affiliated vclasses (a.k.a. types)
 * JCR 2005-11-06 : modified field specifications for tab editing so tabs_retry will get numeric rather than String versions of flag2Set and flag3Set
 */
 %>
 
<c:set var="PRIMARY_TAB_TABTYPE" value="28"/>
<c:set var="SUBCOLLECTION_CATEGORY_TABTYPE" value="18"/>

<p/>
<div align="center">
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td>
                <form action="./" method="get">
                    <input type="submit" class="form-button" value="Display This Tab (Public)"/>
                    <input type="hidden" name="primary" value="${tab.tabId}" />
                    <input type="hidden" name="home" value="${portal.portalId}" />
                </form>
		<form action="listTabs" method="get">
			<input type="submit" class="form-button" value="See All Tabs"/>
			<input type="hidden" name="home" value="${portal.portalId}" />
		</form>
	</td>
	<td valign="bottom" align="center">
		<form action="editForm" method="get">
			<input name="id" type = "hidden" value="<%=request.getAttribute("tabId")%>" />
			<input type="submit" class="form-button" value="Edit Tab Details"/>
			<input type="hidden" name="home" value=""${portal.portalId}" />
			<input type="hidden" name="controller" value="Tab"/>
		</form>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="portalId" value="${portal.portalId}"/>
			<input type="hidden" name="home" value="${portal.portalId}" />
			<input type="submit" class="form-button" value="Add New Tab"/>
			<input type="hidden" name="controller" value="Tab"/>
		</form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>

<!-- ____________________________ parent tabs  ______________________________________ -->
<c:choose>
<c:when test="${tab.tabtypeId < PRIMARY_TAB_TABTYPE}">
	<tr valign="bottom">
		<td colspan="2" valign="bottom">
				<c:if test="${!empty epo.formObject.checkboxLists['parentTabs']}">
						<form action="doTabHierarchyOperation" method="get">
							<ul>
							    <c:forEach var="cb" items="${epo.formObject.checkboxLists['parentTabs']}">
									<li style="list-style-type:none"><input name="ParentId" type="checkbox" value="${cb.value}"/>${cb.body}</li>
								</c:forEach>
							</ul>
							<input type="submit" class="form-button" value="Remove checked parent tabs"/>
							<input type="hidden" name="home" value="${portal.portalId}" />
							<input type="hidden" name="ChildId" value="${tab.tabId}"/>
							<input type="hidden" name="primaryAction" value="_remove"/>
							<input type="hidden" name="_epoKey" value="${tabHierarchyEpoKey}"/>
						</form>
				</c:if>
		</td>
		<td>
			<form action="editForm" method="get">
				<input type="hidden" name="home" value="${portal.portalId}" />
				<input type="hidden" name="ChildId" value="${tab.tabId}">
				<input type="hidden" name="controller" value="Tabs2Tabs"/>
				<input type="submit" class="form-button" value="Add existing tab as parent tab"/>
			</form>
		</td>
	</tr>
</c:when>
<c:otherwise>
	<tr><td colspan="3" align="center">This is the highest level tab for any portal, so no links to higher tabs are possible.</td></tr>
</c:otherwise>
</c:choose>
<tr><td colspan="3"><hr/></td></tr>
<!-- _____________________________________ child tabs  ___________________________________________ -->
<c:choose>
<c:when test="${tab.tabtypeId>SUBCOLLECTION_CATEGORY_TABTYPE}">
	<tr valign="bottom">
		<td colspan="2" valign="bottom">
			<c:if test="${!empty epo.formObject.checkboxLists['childTabs']}">
					<form action="doTabHierarchyOperation" method="get">
					  <ul>
						<c:forEach var="cb" items="${epo.formObject.checkboxLists['childTabs']}">
								<li style="list-style-type:none"><input name="ChildId" type="checkbox" value="${cb.value}"/>${cb.body}</li>
						</c:forEach>
					  </ul>
					    <input type="hidden" name="ParentId" value="${tab.tabId}"/>
						<input type="submit" class="form-button" value="Remove checked child tabs"/>
						<input type="hidden" name="home" value="${portal.portalId}" />
						<input type="hidden" name="primaryAction" value="_remove"/>
						<input type="hidden" name="_epoKey" value="${tabHierarchyEpoKey}"/>
					</form>
			</c:if>
		</td>
		<td>
			<form action="editForm" method="get">
				<input type="hidden" name="home" value="${portal.portalId}" />
				<input type="hidden" name="ParentId" value="${tab.tabId}">
				<input type="hidden" name="controller" value="Tabs2Tabs">
				<input type="submit" class="form-button" value="Add existing tab as child tab"/>
			</form>
		</td>
	</tr>
</c:when>
<c:otherwise>
	<tr><td colspan="3" align="center">This is the lowest level tab for any portal, so no links to lower tabs are possible.</td></tr>
</c:otherwise>
</c:choose>
<tr><td colspan="3"><hr/></td></tr>
<!-- ________________________________________________ vClasses  __________________________________________________ -->
<c:choose>
<c:when test="${tab.entityLinkMethod eq 'auto' || tab.entityLinkMethod eq 'mixed'}">
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	   <c:if test="${!empty affilTypes}">
		<form action="tabs2TypesOp" method="get">
			<ul style="list-style-type:none;">
    		<c:forEach var="type" items="${affilTypes}">
    			<li style="list-style-type:none;"><input type="checkbox" name="TypeURI" value="${type.URI}">${type.name}</input></li>
    		</c:forEach>
			</ul>
			<input type="hidden" name="TabId" value="${tab.tabId}"/>
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="hidden" name="operation" value="remove"/>
			<input type="hidden" name="_epoKey" value="${epoKey}"/>
			<input type="submit" class="form-button" value="Remove Checked Class Autolinks"/>
		</form>
	    </c:if>
	</td>
	<td>
		<form action="editForm" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="hidden" name="TabId" value="${tab.tabId}"/>
			<input type="hidden" name="controller" value="Tabs2Types"/>
			<input type="submit" class="form-button" value="Add Class Autolink"/>
		</form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>
</c:when>
<c:otherwise><tr><td colspan="3" align="center">This tab is set for manual links to individuals only.  Auto-affiliation with a type is not possible.</td></tr><tr><td colspan="3"><hr/></td></tr></c:otherwise>
</c:choose>
<!-- _______________________________________ entities ___________________________________________________ -->
<c:choose>
<c:when test="${tab.entityLinkMethod eq 'manual' || tab.entityLinkMethod eq 'mixed'}">
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	   <c:if test="${!empty epo.formObject.checkboxLists['affilEnts']}">
		<form action="tabIndividualRelationOp" method="get">
			<ul style="list-style-type:none;">
			<c:forEach var="cb" items="${epo.formObject.checkboxLists['affilEnts']}">
				<li style="list-style-type:none;"><input type="checkbox" name="TirURI" value="${cb.value}" class="form-item"/>${cb.body}</li>
			</c:forEach>
			</ul>
			<input type="hidden" name="TabId" value="${tab.tabId}"/>
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="hidden" name="operation" value="remove"/>
			<input type="hidden" name="_epoKey" value="${epoKey}"/>
			<input type="submit" class="form-button" value="Remove Checked Individuals"/>
		</form>
	    </c:if>
	</td>
	<td>
		<form action="editForm" method="get">
			<select name="VClassUri" class="form-item">
		    	<form:option name="VClassURI"/>
		    </select><br/>
		    <p>Select class of individual</p>
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="hidden" name="TabId" value="${tab.tabId}"/>
			<input type="hidden" name="controller" value="Tabs2Ents"/>
			<input type="submit" class="form-button" value="Add an individual to this tab"/>
		</form>
	</td>
</tr>
</c:when>
<c:otherwise><tr><td colspan="3" align="center">This tab is set for tab-type relationships only; direct links to individuals are not possible</td></tr></c:otherwise>
</c:choose>
</table>
</div>
</div>
