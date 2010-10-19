<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>


<div class="staticPageBackground">

<h2>Add or Remove RDF Data</h2>

<form action="uploadRDF" method="post"  enctype="multipart/form-data" >

<c:if test="${!empty param.errMsg}">       
    <p><strong class="warning">${errMsg}</strong></p>
</c:if>

    <p>Enter Web-accessible URL of RDF document:</p>
    <p><input name="rdfUrl" type="text" style="width:67%;" value="<c:out value='${param.rdfUrl}'/>"/></p>
    
    <p>Or upload a file from your computer: </p>
    <p><input type="file" name="rdfStream"/> </p>
    
    <p><input type="radio" name="mode" value="add" checked="checked"/>add RDF <input type="radio" name="mode" value="remove"/>remove RDF</p>
    <select name="language">
        	<option value="RDF/XML">RDF/XML</option>
        	<option value="N3">N3</option>
        	<option	value="N-TRIPLE">N-Triples</option>
            <option value="TTL">Turtle</option>
    </select>
    <p><input type="checkbox" name="makeClassgroups" value="true"/> create classgroups automatically</p>
            
    <c:if test="${requestScope.singlePortal == true}">
      <input type="hidden" name="checkIndividualsIntoPortal" value="current"/>
    </c:if>
    
    <c:if test="${requestScope.singlePortal == false }">
      <p>
      <input type="radio" name="checkIndividualsIntoPortal" value="current"/> make individuals visible in current portal
      <input type="radio" name="checkIndividualsIntoPortal" value="all"/> make individuals visible in all portals
      </p>
    </c:if>

    <p><input type="submit" name="submit" value="submit"/></p>     
</form>

</div>
