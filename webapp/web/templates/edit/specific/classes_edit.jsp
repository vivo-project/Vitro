<?xml version="1.0" encoding="UTF-8"?>

<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" 
          xmlns:c="http://java.sun.com/jstl/core" 
          xmlns:form="http://vitro.mannlib.cornell.edu/edit/tags"
          version="2.0">
<jsp:directive.page import="edu.cornell.mannlib.vitro.webapp.beans.VClass"/>
<jsp:directive.page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"/>

<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

<!--
/**
 *
 * @version 1.00
 * @author Jon Corson-Rikert
 *
 * UPDATES:
 * JCR 2005-11-06 : added code to show properties of parent class(es) as adapted from props_retry.jsp
 * BJL 2007-07-XX : general overhaul to remove database tags and scriptlets
 */
-->
<div align="center">
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td valign="bottom">
		<form action="listVClassWebapps" method="get">
			<input type="submit" class="form-button" value="Show All Classes"/>
		</form>
                <form action="showClassHierarchy" method="get">
                        <input type="submit" class="form-button" value="Show Class Hierarchy"/>
                </form>
                <form action="showClassHierarchy" method="get">
                        <input type="hidden" name="vclassUri" value="${VClass.URI}"/>
                        <input type="submit" class="form-button" value="Show Hierarchy below This Class"/>
                </form>
                <form action="listIndividuals" method="get">
                    <input type="hidden" name="VClassURI" value="${VClass.URI}" />
                    <input type="submit" class="form-button" value="Show All Individuals in This Class"/>
                </form>
                <form action="listIndividuals" method="get">
                    <input type="hidden" name="VClassURI" value="${VClass.URI}" />
                    <input type="hidden" name="assertedOnly" value="true"/>
                    <input type="submit" class="form-button" value="Show Individuals Asserted To Be in This Class"/>
                </form>
	</td>
	<td valign="bottom" align="center">
		<form action="vclass_retry" method="get">
			<input name="uri" type = "hidden" value="${VClass.URI}" />
			<input type="submit" class="form-button" value="Edit Class"/>
		</form>
	</td>
	<td valign="bottom">
		<form action="vclass_retry" method="get">
			<input type="submit" class="form-button" value="Add New Class"/>
		</form>
		<form action="editForm" method="get">
			<input type="submit" class="form-button" value="Add New Individual in This Class"/>
       		<input type="hidden" name="controller" value="Entity"/>
       		<input type="hidden" name="VClassURI" value="${VClass.URI}"/>
		</form>
		<form action="editForm" method="get">
           	<input type="submit" class="form-button" value="Change URI"/>
           	<input type="hidden" name="oldURI" value="${VClass.URI}"/>
           	<input type="hidden" name="mode" value="renameResource"/>
          	<input type="hidden" name="controller" value="Refactor"/>
        </form>
       	<form action="editForm" method="get">
           	<input type="submit" class="form-button" value="Move Instances to Another Class"/>
           	<input type="hidden" name="VClassURI" value="${VClass.URI}"/>
			<c:choose>
				<c:when test="${VClass.namespace eq 'http://vitro.mannlib.cornell.edu/ns/bnode#'}">
					<input type="hidden" name="VClassName" value="this anonymous class"/>
				</c:when>
				<c:otherwise>
           			<input type="hidden" name="VClassName" value="${VClass.localNameWithPrefix}"/>
				</c:otherwise>
			</c:choose>
           	<input type="hidden" name="mode" value="moveInstances"/>
          	<input type="hidden" name="controller" value="Refactor"/>
        </form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>
<!-- _____________________________________________ superclasses __________________________________________ -->
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	   <c:if test="${!empty superclasses}">
		<form action="classes2ClassesOp" method="post">
			<ul style="list-style-type:none;">
			<c:forEach var="superclass" items="${superclasses}">
			<c:url var="superclassURL" value="vclassEdit">
				<c:param name="uri" value="${superclass.URI}"/>
			</c:url>
				<li><input type="checkbox" name="SuperclassURI" value="${superclass.URI}" class="form-item"/>
					<c:choose>
						<c:when test="${!superclass.anonymous}">
							<a href="${superclassURL}">${superclass.localNameWithPrefix}</a>
						</c:when>
						<c:otherwise>
							${superclass.localNameWithPrefix}
						</c:otherwise>
					</c:choose>
				</li>
			</c:forEach>
			</ul>
			<input type="hidden" name="SubclassURI" value="${VClass.URI}"/>
			<input type="hidden" name="operation" value="remove"/>
			<input type="hidden" name="_epoKey" value="${epoKey}"/>
			<input type="submit" class="form-button" value="Remove Checked Superclass Links"/>
		</form>
	    </c:if>
	</td>
	<td>
		<form action="editForm" method="get">
			<input type="hidden" name="SubclassURI" value="${VClass.URI}"/>
			<input type="hidden" name="controller" value="Classes2Classes"/>
			<input type="submit" class="form-button" value="New Link to Superclass"/>
		</form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>			
<!-- _______________________________________________ subclasses _____________________________________________ -->
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	    <c:if test="${!empty subclasses}">
		<form action="classes2ClassesOp" method="post">
			<ul style="list-style-type:none;">
			<c:forEach var="subclass" items="${subclasses}">
				<c:url var="subclassURL" value="vclassEdit">
					<c:param name="uri" value="${subclass.URI}"/>
				</c:url>
				<li><input type="checkbox" name="SubclassURI" value="${subclass.URI}" class="form-item"/>
					<c:choose>
					    <c:when test="${!subclass.anonymous}">
					    	<a href="${subclassURL}"> ${subclass.localNameWithPrefix} </a>
					    </c:when>
					    <c:otherwise>
						${subclass.localNameWithPrefix}
					    </c:otherwise>
				        </c:choose>
				</li>						
			</c:forEach>	
			</ul>
			<input type="hidden" name="SuperclassURI" value="${VClass.URI}"/>
			<input type="submit" class="form-button" value="Remove Checked Subclass Links"/>
			<input type="hidden" name="operation" value="remove"/>
			<input type="hidden" name="_epoKey" value="${epoKey}"/>
		</form>
             </c:if>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="controller" value="Classes2Classes"/>
			<input type="hidden" name="SuperclassURI" value="${VClass.URI}"/>
			<input type="submit" class="form-button" value="New Link to Subclass"/>
		</form>
		<form action="vclass_retry" method="get">
			<input type="hidden" name="superclassUri" value="${VClass.URI}" />
			<input type="submit" class="form-button" value="Add New Subclass of This Class"/>
		</form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>

<!-- _______________________________________________ equivalent classes _____________________________________________ -->
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	    <c:if test="${!empty equivalentClasses}">
		<form action="classes2ClassesOp" method="post">
		    <input type="hidden" value="equivalentClass" name="opMode"/>
			<ul style="list-style-type:none;">
			<c:forEach var="subclass" items="${equivalentClasses}">
				<c:url var="subclassURL" value="vclassEdit">
					<c:param name="uri" value="${subclass.URI}"/>
				</c:url>
				<li><input type="checkbox" name="SubclassURI" value="${subclass.URI}" class="form-item"/>
				    <c:choose>
					    <c:when test="${!subclass.anonymous}">
					        <a href="${subclassURL}"> ${subclass.localNameWithPrefix} </a>
					    </c:when>
					    <c:otherwise>
					        ${subclass.localNameWithPrefix}
					    </c:otherwise>
                    </c:choose>
				</li>
			</c:forEach>	
			</ul>
			<input type="hidden" name="SuperclassURI" value="${VClass.URI}"/>
			<input type="submit" class="form-button" value="Remove Checked Equivalent Classes"/>
			<input type="hidden" name="operation" value="remove"/>
			<input type="hidden" name="_epoKey" value="${epoKey}"/>
		</form>
             </c:if>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="controller" value="Classes2Classes"/>
			<input type="hidden" name="SuperclassURI" value="${VClass.URI}"/>
			<input type="hidden" name="opMode" value="equivalentClass"/>
			<input type="submit" class="form-button" value="Assert Class Equivalence"/>
		</form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>


<!-- _______________________________________________ disjoint classes _____________________________________________ -->
<tr valign="bottom" align="center">
	<td colspan="2" valign="bottom" align="left">
	    <c:if test="${!empty disjointClasses}">
		<form action="classes2ClassesOp" method="post">
		    <input type="hidden" value="disjointWith" name="opMode"/>
			<ul style="list-style-type:none;">
			<c:forEach var="subclass" items="${disjointClasses}">
				<c:url var="subclassURL" value="vclassEdit">
					<c:param name="uri" value="${subclass.URI}"/>
				</c:url>
				<li><input type="checkbox" name="SubclassURI" value="${subclass.URI}" class="form-item"/>
				    <c:choose>
					<c:when test="${!subclass.anonymous}">
					    <a href="${subclassURL}"> ${subclass.localNameWithPrefix} </a>
					</c:when>
					<c:otherwise>
					    ${subclass.localNameWithPrefix}
					</c:otherwise>
				    </c:choose>
				</li>
			</c:forEach>	
			</ul>
			<input type="hidden" name="SuperclassURI" value="${VClass.URI}"/>
			<input type="submit" class="form-button" value="Remove Checked Disjoint Classes"/>
			<input type="hidden" name="operation" value="remove"/>
			<input type="hidden" name="_epoKey" value="${epoKey}"/>
		</form>
             </c:if>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="controller" value="Classes2Classes"/>
			<input type="hidden" name="SuperclassURI" value="${VClass.URI}"/>
			<input type="hidden" name="opMode" value="disjointWith"/>
			<input type="submit" class="form-button" value="New Disjointness Axiom"/>
		</form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>

<!-- ___________________________________________ domain properties ___________________________________ -->
<tr valign="top" align="center">
	<td>
		<form action="listPropertyWebapps" method="get">
			<input type="submit" class="form-button" value="Show All Object Properties Applicable to This Class"/>
			<input type="hidden" name="vclassUri" value="${VClass.URI}"/>
			<input type="hidden" name="propsForClass" value="true"/>
		</form><br/>
		<form action="listDatatypeProperties" method="get">
			<input type="submit" class="form-button" value="Show All Data Properties Applicable to This Class"/>
			<input type="hidden" name="vclassUri" value="${VClass.URI}"/>
			<input type="hidden" name="propsForClass" value="true"/>
		</form><br/>
	</td>
	<td></td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="domainClassUri" value="${VClass.URI}"/>
			<input type="hidden" name="controller" value="Property"/>
			<input type="submit" class="form-button" value="Define New Domain Property at This Class"/>
		</form>
	</td>
    </tr>
    <tr>
        <td colspan="2">
                <form action="editForm" method="get">
                    <input type="hidden" name="controller" value="Restriction"/>
                    <select name="restrictionType">
                        <option value="allValuesFrom">all values from</option>
                        <option value="someValuesFrom">some values from</option>
                        <option value="hasValue">has value</option>
                        <option value="minCardinality">minimum cardinality</option>
                        <option value="maxCardinality">maximum cardinality</option>
                        <option value="cardinality">exact cardinality</option>
                    </select>
                    <input type="submit" class="form-button" value="Apply Restriction"/>
                    <input type="hidden" name="VClassURI" value="${VClass.URI}"/>
                    <p>Restrict: 
                        <input type="radio" name="propertyType" value="object" checked="checked"/> object property
                        <input type="radio" name="propertyType" value="data"/> data property
                    </p>
                </form>       
        </td>
</tr>
</table>
</div>
</div>
</jsp:root>
