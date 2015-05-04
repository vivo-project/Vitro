<?xml version="1.0" encoding="UTF-8"?>

<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:c  ="http://java.sun.com/jsp/jstl/core"
          xmlns:fn ="http://java.sun.com/jsp/jstl/functions">

<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

<!--
/**
 *
 * @version 1.00
 * @author Jon Corson-Rikert
 *
 * UPDATES:
 * BJL 2007-08-01 : complete overhaul to remove database tags and scriptlets
 * BDC 2005-12-12 : refactoring for etypeless operation.
 * JCR 2005-06-15 : added fields 21 and 22: domainFlag1Set and rangeFlag1Set, and field 23: statusId
 */
-->

<div align="center">
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
    <td>
        <form action="showObjectPropertyHierarchy" method="get">
	        <input type="hidden" name="iffRoot" value="true" />
            <input type="submit" class="form-button" value="Root Properties"/>
        </form>
        <form action="listPropertyWebapps" method="get">
            <input type="submit" class="form-button" value="See All Properties"/>
        </form>
        <form action="showObjectPropertyHierarchy" method="get">
	    	<input type="hidden" name="propertyUri" value="${property.URI}"/>
            <input type="submit" class="form-button" value="Show Hierarchy below This Property"/>
        </form>      
        <form action="listVClassWebapps" method="get">
			<input type="hidden" name="showPropertyRestrictions" value="true"/>
			<input type="hidden" name="propertyURI" value="${property.URI}"/>
			<input type="hidden" name="propertyName" value="${property.domainPublic}"/>
			<input type="hidden" name="propertyType" value="object"/>
			<input type="submit" class="form-button" value="Show Classes With a Restriction on This Property"/>
		</form>
        <form action="listObjectPropertyStatements" method="get">
        	<input type="hidden" name="propertyURI" value="${property.URI}"/>
        	<input type="hidden" name="assertedStmts" value="true"/>
        	<input type="hidden" name="showVClasses" value="true"/>
        	from <input type="text" name="startAt" value="1" size="2"/>
        	to <input type="text" name="endAt" value="50" size="3"/><br/>
        	<input type="submit" class="form-button" value="Show Examples of Statements Using This Property"/>
        </form>
    </td>
    <td valign="bottom" align="center">
        <form action="editForm" method="get">
            <input name="uri" type = "hidden" value="${property.URI}" />
            <input type="submit" class="form-button" value="Edit Property Record"/>
	    <input type="hidden" name="controller" value="Property"/>
        </form><br/>
    </td>
    <td valign="bottom">
        <form action="editForm" method="get">
            <input type="hidden" name="parentId" value="${property.URI}" />
	    <input type="hidden" name="controller" value="Property"/>
            <input type="submit" class="form-button" value="Add New Child Property"/>
        </form>
        <form action="editForm" method="get">
            <input type="submit" class="form-button" value="Add New Property"/>
	    <input type="hidden" name="controller" value="Property"/>
        </form>
            <form action="editForm" method="get">
            <input type="submit" class="form-button" value="Change URI"/>
            <input type="hidden" name="oldURI" value="${property.URI}"/>
            <input type="hidden" name="mode" value="renameResource"/>
            <input type="hidden" name="controller" value="Refactor"/>
        </form>
        <form action="editForm" method="get">
            <input type="submit" class="form-button" value="Move Statements to Different Property"/>
            <input type="hidden" name="propertyURI" value="${property.URI}"/>
            <input type="hidden" name="mode" value="movePropertyStatements"/>
            <input type="hidden" name="propertyType" value="ObjectProperty"/>
            <input type="hidden" name="controller" value="Refactor"/>
        </form>
        <c:if test="${!empty property.URIInverse}">
	        <form action="propertyEdit" method="get">
	            <input type="submit" class="form-button" value="Go to Inverse Property"/>
	            <input type="hidden" name="uri" value="${property.URIInverse}"/>
	        </form>
		</c:if>
    </td>
</tr>

<tr><td colspan="3"><hr/></td></tr>
<!-- _____________________________________________ faux properties __________________________________________ -->
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	  <c:if test="${!empty fauxproperties}">
		<c:forEach var="fauxproperty" items="${fauxproperties}">
		  <ul style="list-style-type:none;">
			<li>
			  <c:choose>
			    <c:when test="${empty fauxproperty.domainLabel}">
		          <c:url var="fauxpropertyURL" value="editForm">
			        <c:param name="controller" value="FauxProperty"/>
			        <c:param name="baseUri" value="${property.URI}"/>
			        <c:param name="rangeUri" value="${fauxproperty.rangeURI}" />
			      </c:url>
			      <a href="${fauxpropertyURL}">${fauxproperty.pickListName}</a>
			      no domain,
			    </c:when>
			    <c:otherwise>
		          <c:url var="fauxpropertyURL" value="editForm">
			        <c:param name="controller" value="FauxProperty"/>
			        <c:param name="baseUri" value="${property.URI}"/>
			        <c:param name="domainUri" value="${fauxproperty.domainURI}" />
			        <c:param name="rangeUri" value="${fauxproperty.rangeURI}" />
			      </c:url>
			      <a href="${fauxpropertyURL}">${fauxproperty.pickListName}</a>
			      domain: ${fauxproperty.domainLabel},
			    </c:otherwise>
			  </c:choose> 
			  range: ${fauxproperty.rangeLabel}
			</li>
		  </ul>
		</c:forEach>
	  </c:if>
	</td>
	<td>
	<form action="editForm" method="get">
	  <input type="hidden" name="create" value="create"/>
	  <input type="hidden" name="baseUri" value="${property.URI}"/>
	  <input type="hidden" name="controller" value="FauxProperty"/>
	  <input type="submit" class="form-button" value="Create New Faux Property"/>
	</form>
	</td>
</tr>



<tr><td colspan="3"><hr/></td></tr>
<!-- _____________________________________________ superproperties __________________________________________ -->
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	   <c:if test="${!empty superproperties}">
		<form action="props2PropsOp" method="post">
			<ul style="list-style-type:none;">
			<c:forEach var="superproperty" items="${superproperties}">
			<c:url var="superpropertyURL" value="propertyEdit">
				<c:param name="uri" value="${superproperty.URI}"/>
			</c:url>
				<li><input type="checkbox" name="SuperpropertyURI" value="${superproperty.URI}" class="form-item"/>
					<a href="${superpropertyURL}">${superproperty.pickListName}</a>
				</li>
			</c:forEach>
			</ul>
			<input type="hidden" name="SubpropertyURI" value="${property.URI}"/>
			<input type="hidden" name="operation" value="remove"/>
		    <input type="hidden" name="_epoKey" value="${epoKey}"/>	
			<input type="submit" class="form-button" value="Remove Checked Superproperty Links"/>
		</form>
	    </c:if>
	</td>
	<td>
		<form action="editForm" method="get">
			<input type="hidden" name="SubpropertyURI" value="${property.URI}"/>
            <input type="hidden" name="opMode" value="superproperty"/>
			<input type="hidden" name="controller" value="Properties2Properties"/>
			<input type="submit" class="form-button" value="New Link to Superproperty"/>
		</form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>			
<!-- _______________________________________________ subproperties _____________________________________________ -->
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	    <c:if test="${!empty subproperties}">
		<form action="props2PropsOp" method="post">
			<ul style="list-style-type:none;">
			<c:forEach var="subproperty" items="${subproperties}">
				<c:url var="subpropertyURL" value="propertyEdit">
					<c:param name="uri" value="${subproperty.URI}"/>
				</c:url>
				<li><input type="checkbox" name="SubpropertyURI" value="${subproperty.URI}" class="form-item"/>
					 <a href="${subpropertyURL}"> ${subproperty.pickListName} </a>
				</li>						
			</c:forEach>	
			</ul>
			<input type="hidden" name="SuperpropertyURI" value="${property.URI}"/>
			<input type="submit" class="form-button" value="Remove Checked Subproperty Links"/>
            <input type="hidden" name="_epoKey" value="${epoKey}"/>
			<input type="hidden" name="operation" value="remove"/>
		</form>
             </c:if>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="controller" value="Properties2Properties"/>
			<input type="hidden" name="SuperpropertyURI" value="${property.URI}"/>
            <input type="hidden" name="opMode" value="subproperty"/>
			<input type="submit" class="form-button" value="New Link to Subproperty"/>
		</form>
		<form action="editForm" method="get">
			<input type="hidden" name="controller" value="Property"/> 
			<input type="hidden" name="parentId" value="${property.URI}" />
			<input type="submit" class="form-button" value="Add New Subproperty of This Property"/>
		</form>
	</td>
</tr>

<!-- _______________________________________________ equivalent properties _____________________________________________ -->
<tr valign="bottom" align="center">
    <td colspan="2" valign="bottom" align="left">
        <c:if test="${!empty equivalentProperties}">
        <form action="props2PropsOp" method="post">
            <ul style="list-style-type:none;">
            <c:forEach var="eqproperty" items="${equivalentProperties}">
                <c:url var="eqpropertyURL" value="datapropEdit">
                    <c:param name="uri" value="${eqproperty.URI}"/>
                </c:url>
                <li><input type="checkbox" name="SubpropertyURI" value="${eqproperty.URI}" class="form-item"/>
                     <a href="${eqpropertyURL}"> ${eqproperty.pickListName} </a>
                </li>                       
            </c:forEach>    
            </ul>
            <input type="hidden" name="SuperpropertyURI" value="${property.URI}"/>
            <input type="submit" class="form-button" value="Remove Checked Equivalent Property Links"/>
            <input type="hidden" name="_epoKey" value="${epoKey}"/>
            <input type="hidden" name="operation" value="remove"/>
            <input type="hidden" name="opMode" value="equivalentProperty"/>
        </form>
             </c:if>
    </td>
    <td valign="bottom">
        <form action="editForm" method="get">
            <input type="hidden" name="controller" value="Properties2Properties"/>
            <input type="hidden" name="SuperpropertyURI" value="${property.URI}"/>
            <input type="hidden" name="opMode" value="equivalentProperty"/>
            <input type="hidden" name="propertyType" value="object"/>
            <input type="submit" class="form-button" value="New Link to Equivalent Property"/>
        </form>
    </td>
</tr>


</table>
</div>

</div>

</jsp:root>
