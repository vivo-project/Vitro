<%-- $This file is distributed under the terms of the license in LICENSE$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Ontology name</b><br/>
				<input type="text" name="Name" value="<form:value name="Name"/>" size="40" maxlength="120" />
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
            <b>Namespace URI*</b>&nbsp;<span class="note"> (must begin with http:// or https://)</span><br/>
             <c:choose>
               <c:when test="${_action eq 'update'}">
                    <i>Change via the "change URI" button on previous screen</i><br/>
                    <input disabled="disabled" type="text" name="URI" value="<form:value name="URI"/>" size="50" maxlength="240" />
                </c:when>
                <c:otherwise>
                    <input type="text" name="URI" value="<form:value name="URI"/>" size="50" maxlength="240" />
                </c:otherwise>
              </c:choose>
              <c:set var="URIError"><form:error name="URI"/></c:set>
              <c:if test="${!empty URIError}">
                  <span class="notice"><c:out value="${URIError}"/></span>
              </c:if>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Namespace prefix</b><br/>
				<input type="text" name="Prefix" value="<form:value name="Prefix"/>" size="8" maxlength="25" />
            <c:set var="PrefixError"><form:error name="Prefix"/></c:set>
            <c:if test="${!empty PrefixError}">
                <span class="notice"><c:out value="${PrefixError}"/></span>
            </c:if>
		</td>
	</tr>
<script  type="text/javascript">
$(document).ready(function() {
      var source = "";
    $('input#primaryAction').click(function() {
        source = "submit";
    });
    $('form#editForm').submit(function() {
        if (source == "submit") {
            var str = $('input[name=URI]').val();
            if ( str.indexOf('http://') >= 0 || str.indexOf('https://') >= 0 ) {
                return true;
            }
            else {
                alert('The Namespace URI must begin with either http:// \n\n or https://');
                $('input[name=URI]').focus();
                source = "";
                return false;
            }
        }
    });
});
</script>
