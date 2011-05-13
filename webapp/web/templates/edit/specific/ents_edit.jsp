<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<% /* For now, not using XML syntax because the output XHTML is not indented */ %>
<% /* <?xml version="1.0" encoding="UTF-8"?> */ %>
<% /* <jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" 
          xmlns:c="http://java.sun.com/jstl/core"
          xmlns:form="http://vitro.mannlib.cornell.edu/edit/tags"
          version="2.0"> */ %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.authrequestedAction.usepages.UseMiscellaneousEditorPages" %>

<%
	if (PolicyHelper.isAuthorizedForActions(new UseMiscellaneousEditorPages())) {
		request.setAttribute("isEditor", Boolean.TRUE);
	}
%>

<div name="anybody" class="editingForm">
<jsp:include page="/templates/edit/fetch/vertical.jsp"/>
<c:set var='individual' value='${requestScope.entityWebapp}'/>

<c:if test="${isEditor}">
	<div name="authorized" align="center">
	<table class="form-background" border="0" cellpadding="2" cellspacing="2" width="100%">
    	<tr valign="top" align="center">
    	<td>
        	<form action="entity" method="get">
            	<input type="submit" class="form-button" value="Display This Individual (public)"/>
            	<input type="hidden" name="uri" value="${individual.URI}"/>
        	</form>
        	
        	<c:set var="query" 
                 value="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                        SELECT   ?pred  ?predLabel ?obj ?objLabel
                        WHERE 
                        {
                         { GRAPH ?g { <${entity.URI}> ?pred ?obj} } 
                         OPTIONAL { GRAPH ?h { ?obj rdfs:label ?objLabel } }
                         OPTIONAL { GRAPH ?i { ?pred rdfs:label ?predLabel } }
                        }
                        limit 10000"/>
          <form action="admin/sparqlquery" method="get">
            <input type="hidden" name="query" value="${query}"/>
            <input type="hidden" name="resultFormat" value="RS_TEXT"/>
            <input type="submit" class="form-button" value="Raw Statements with This Resource as Subject"/>
          </form>

          <c:set var="query" 
                 value="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                        SELECT ?sub ?subL  ?pred  ?predLabel
                        WHERE 
                        {
                         { GRAPH ?g { ?sub ?pred <${entity.URI}> } }
                         OPTIONAL { GRAPH ?h { ?sub rdfs:label ?subL } }
                         OPTIONAL { GRAPH ?i { ?pred rdfs:label ?predLabel } }
                        }
                        limit 10000"/>
          <form action="admin/sparqlquery" method="get">
            <input type="hidden" name="query" value="${query}"/>
            <input type="hidden" name="resultFormat" value="RS_TEXT"/>
            <input type="submit" class="form-button" value="Raw Statements with This Resource as Object"/>
          </form>
        	
    	</td>
    	<td valign="bottom" align="center">
        	<form action="editForm" method="get">
            	<input name="uri" type = "hidden" value="${individual.URI}" />
            	<input name="controller" type = "hidden" value="Entity" />
            	<input type="submit" class="form-button" value="Edit This Individual"/>
        	</form><br/>
        	

        	<c:if test="${!empty individual.linksList}"> 
            	<form action="editForm" method="get">
					<select name="uri" class="form-item">
						<form:option name="ExtraURL"/>
					</select><br />
	                <input type="submit" class="form-button" value="Edit Extra URLs"/>
        	        <input type="hidden" name="controller" value="Link"/>
            	</form>
			</c:if>
	
        	<c:if test="${!empty individual.externalIds}"> 
	        	<form action="editForm" method="get">
	            	<select name="multiplexedParam" class="form-item">
	            		<form:option name="externalIds"/>
	            	</select><br/>
                    <input type="hidden" name="IndividualURI" value="${individual.URI}"/>
	            	<input type="submit" class="form-button" value="Edit External Identifiers"/>
	            	<input type="hidden" name="controller" value="ExternalId"/>
	        	</form>
	    	</c:if>
    	</td>
    	<td valign="bottom">
       		<form name="newEntityForm" action="editForm" method="get">
            	<select id="VClassURI" name="VClassURI" class="form-item">
                	<form:option name="VClassURI"/>
            	</select><br/>
            	<input type="submit" class="form-button" value="Add New Individual of above Type"/>
            	<input type="hidden" name="controller" value="Entity"/>
        	</form>
        	<form action="editForm" method="get">
            	<input type="submit" class="form-button" value="Add Another URL"/>
            	<input type="hidden" name="entityUri" value="${individual.URI}"/>
            	<input type="hidden" name="controller" value="Link"/>
        	</form>
        	<form action="editForm" method="get">
            	<input type="submit" class="form-button" value="Add an External Identifier"/>
            	<input type="hidden" name="IndividualURI" value="${individual.URI}"/>
            	<input type="hidden" name="controller" value="ExternalId"/>
        	</form>
        	<form action="editForm" method="get">
            	<input type="submit" class="form-button" value="Change URI"/>
            	<input type="hidden" name="oldURI" value="${individual.URI}"/>
            	<input type="hidden" name="mode" value="renameResource"/>
            	<input type="hidden" name="controller" value="Refactor"/>
        	</form>
        	<form action="uploadImages" method="get">
            	<input type="submit" class="form-button" value="Upload Image"/>
            	<input type="hidden" name="entityUri" value="${individual.URI}"/>
        	</form>
    	</td>
    </tr>
    <tr><td colspan="3"><hr/></td></tr>
    
    <!-- TYPES --> 
    
    <tr valign="bottom" align="center">
	<td colspan="1" valign="bottom" align="left">
	    <c:if test="${!empty types}">
		<form action="individualTypeOp" method="get">
			<ul style="list-style-type:none;">
			<c:forEach var="type" items="${types}">
				<c:url var="individualURL" value="entityEdit">
					<c:param name="uri" value="${type.URI}"/>
				</c:url>
				<c:url var="typeURL" value="/vclassEdit">
					<c:param name="uri" value="${type.URI}"/>
				</c:url>
				<li><input type="checkbox" name="TypeURI" value="${type.URI}" class="form-item"/><a href="${typeURL}"> ${type.localNameWithPrefix} </a></li>
			</c:forEach>	
			</ul>
			<input type="hidden" name="individualURI" value="${individual.URI}"/>
			<input type="submit" class="form-button" value="Remove Checked Asserted Types"/>
			<input type="hidden" name="operation" value="remove"/>
			<input type="hidden" name="_epoKey" value="${epoKey}"/>
		</form>
             </c:if>
        <form action="editForm" method="get">
			<input type="hidden" name="controller" value="IndividualType"/>
			<input type="hidden" name="IndividualURI" value="${individual.URI}"/> 
			<input type="submit" class="form-button" value="Add Type"/>
		</form>
	</td>    

<td colspan="2">    
    &nbsp; <!--  empty now that flags are gone -->
</td>

</tr>
  
	</table>

	<c:if test="${dwrDisabled != true}"> 
    	<div id="entityUriForDwr" style="visibility:hidden;">${individual.URI}</div>
    		<div>
        	<table class="form-background" border="0" cellpadding="2" cellspacing="2" width="100%">
            	<tr><td colspan="3" align="center"><h2>Object (individual-to-individual) Property Statements</h2></td></tr>
            	<tr><td><input id="newPropButton" class="form-button" type="button" value="add new statement" onclick="newProp();"/></td>
                	<td><input class="form-button" type="button" value="refresh list" onclick="update();"/></td>
            	</tr>
        	</table>
    		</div>
    		<div align="center">
        	<!-- ____________________ properties table using dwr ____________________ -->
        	<div id="propertyTableDiv">
            	<table class="form-background" border="1" width="100%" align="center">
                	<thead class="form-table-head">
                	<tr><th rowspan="1" colspan="1">Subject</th>
                    	<th rowspan="1" colspan="1">Predicate</th>
                    	<th rowspan="1" colspan="1">Object</th>
                    	<th colspan="3" rowspan="1">actions</th>
                	</tr>
                	</thead>
                	<tbody id="propbody">
                    <tr><td>test</td><td>values</td><td>test</td><td>values</td></tr>
                	</tbody>
            	</table>
        	</div>
<!-- ____________________  End of properties table ______________________ -->

<!--  _____________________ Start of hidden area ____________________ -->
<!-- This is hidden and a copy gets put into the table when editing happens  -->
        	<div id="propeditdiv" style="display:none" class ="form-editingRow">
            	<table width="100%">
                	<tr><td>Predicate:</td>
                    	<td colspan="9">
                    		<select id="propertyList" class="form-item"
                                    onchange="fillRangeVClassList();">
                            	<option value="">select property</option>
                            </select>
                        </td>
                    </tr>
                	<tr><td>Object Class:</td>
                    	<td colspan="9">
                    		<select id="vClassList" class="form-item" onchange="fillEntsList();">
                            	<option value="">select type</option>
                            </select>
                        </td>
                	</tr>
                	<tr><td>Object Individual:</td>
                    	<td colspan="9"><select id="entitiesList" class="form-item"><option>select individual</option></select></td>
                	</tr>
                	
                	<!-- no longer available in v.0.7 : something to reimplement in 0.8 or 0.9 -->
                	<tr style="display:none;">
                    	<td>Sunrise:</td><td><input id="sunrise" class="form-item" type="text"/></td>
                    	<td>Sunset:</td><td><input id="sunset" class="form-item" type="text"/></td>
                	</tr>
                	
                	<tr>
                    	<td><input type="button" id="saveButt" class="form-button"
                               	   value="Save" onclick="writeProp()"/></td>
                    	<td><input type="button" id="dismissButt" class="form-button" value="cancel"
                                   onclick="update()"/></td>
                	</tr>
            	</table>
        	</div>
			<div id="buildArea" style="display:none"></div>
		</div><!-- END div "entityUriForDwr" -->
<!-- _________  End hidden area _________ -->

	</c:if> <!-- end dwr section -->

<!-- __________ Relationships to object nodes (domain id is broader or parent side, range id is narrower or child side) _________________ -->

	<table class="form-background" border="0" cellpadding="2" cellspacing="2" width="100%">
		<tr><td colspan="3" align="center">
        	<div style="color: black; cursor: pointer;" onclick="javascript:switchGroupDisplay('oldEditing','oldEditingSw0','${appBean.themeDir}site_icons')" title="old editing" class="navlinkblock" onmouseover="onMouseOverHeading(this)" onmouseout="onMouseOutHeading(this)">
                <span class="entityRelationsSpan"><img src="${appBean.themeDir}site_icons/plus.gif" id="oldEditingSw0"/>
                Click here for original-style object property statement editing (should work on any browser)</span>
			</div>
		</td></tr>
	</table>

	<div id="oldEditing" style="display:none;">
		<table class="form-background" border="0" cellpadding="2" cellspacing="2" width="100%">
			<tr valign="bottom" align="center">
    			<td colspan="3"><i>This individual is the subject in the following relationships to other object individuals</i></td>
			</tr>
			<tr valign="bottom" align="center">
    			<td/>
    			<td valign="bottom" align="center">
              		<c:if test="${!empty epo.formObject.optionLists['ExistingPropertyInstances']}">
                    	<form action="editForm" method="edit">
                        	<select name="multiplexedParam" class="form-item">
                          		<form:option name="ExistingPropertyInstances"/>
                        	</select><br/>
                        	<input type="hidden" name="SubjectEntURI" value="${individual.URI}"/>
                        	<input type="submit" class="form-button" value="Edit Statement"/>
                        	<input type="hidden" name="controller" value="ObjectPropertyStatement"/>
                    	</form>
               		</c:if>
    			</td>
    			<td>
                	<form action="editForm" method="get">
                		<input type="hidden" name="SubjectEntURI" value="${individual.URI}"/>
                		<select name="PropertyURI" class="form-item">
                    		<form:option name="PropertyURI"/>
                		</select><br/>
                		<input type="hidden" name="controller" value="ObjectPropertyStatement"/>
                		<input type="hidden" name="domainSide" value="true"/>
                		<input type="submit" class="form-button" value="new link for this individual"/>
                	</form>
    			</td>
			</tr>
			<tr><td colspan="3"></td></tr>
		</table>
	</div><!-- END div  "oldEditing" -->
	</div><!-- END div "authorized" -->
</c:if><!-- end if (securityLevel less than MIN_EDIT_ROLE) -->
</div><!-- END div "anybody" -->

