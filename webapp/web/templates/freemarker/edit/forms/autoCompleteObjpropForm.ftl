<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- autocomplete template for object properties -->

<#-- NOTE the jsp contained the following which has not been converted

<jsp:include page="${preForm}">
    <jsp:param name="useTinyMCE" value="false"/>
    <jsp:param name="useAutoComplete" value="true"/>
</jsp:include>

-->


<script type="text/javascript" language="javascript">
$(this).load($(this).parent().children('a').attr('src')+" .editForm");

$(document).ready(function() {
    var key = $("input[name='editKey']").attr("value");
    $.getJSON("<c:url value="/dataservice"/>", {getN3EditOptionList:"1", field: "${objectVar}", editKey: key}, function(json){

    $("select#${objectVar}").replaceWith("<input type='hidden' id='${objectVar}' name='${objectVar}' /><input type='text' id='${objectVar}-entry' name='${objectVar}-entry' />");

    $("#${objectVar}-entry").autocomplete(json, {
            minChars: 1,
            width: 320,
            matchContains: true,
            mustMatch: 1,
            autoFill: true,
            // formatItem: function(row, i, max) {
            //     return row[0];
            // },
            // formatMatch: function(row, i, max) {
            //     return row[0];
            // },
            // formatResult: function(row) {
            //     return row[0];
            // }
           
        }).result(function(event, data, formatted) {
             $("input#${objectVar}-entry").attr("value", data[0]); // dump the string into the text box
             $("input#${objectVar}").attr("value", data[1]); // dump the uri into the hidden form input
           });
}
);
})
</script>

<h2>${formTitle}</h2>
       
    <form id="autoCompleteDatapropForm" class="editForm" action="${submitUrl}"  role="autocomplete">
    
    <#if predicate.offerCreateNewOption >
        <#assign var="createNewUrl = "/edit/editRequestDispatch.jsp?subjectUri=${param.subjectUri}&predicateUri=${param.predicateUri}&clearEditConfig=true&cmd=create >
    </#if>
    
    <#if predicate.publicDescription?has_content >       
        <p class="propEntryHelpText">${predicate.publicDescription}</p>
    </#if>
        <p>
            <select id="objectVar" name="objectVar">
                 <#list objectVar as key>
                     <opton value="${key}"
                     <#if editConfiguration.objectUri?has_contant && editConfiguration.object.Uri = key>selected</#if>
                 </#list>
            </select>
        </p>

    <p class="submit">
        <input type="submit" id="submit" value="editConfiguration.submitLabel"/>
        <span class="or"> or <a class="cancel" href="${editConfiguration.cancelUrl}">Cancel</a>
    </p>
    
    <#if predicate.offerCreateNewOption>
        <p>If you don't find the appropriate entry on the selection list,
        <button type="button" onclick="javascript:document.location.href='${createNewUrl}'">Add a new item to this list</button>
        </p>
    </#if>

    </form>

    <#if ! param.objectUri?has_content >
        <form class="deleteForm" action="${???}" method="get">       
    	 	<label for="delete"><h3>Delete this entry?</h3></label>
            <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
            <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
            <input type="hidden" name="objectUri"    value="${param.objectUri}"/>    
            <input type="hidden" name="cmd"          value="delete"/>
            <input type="submit" id="delete" value="Delete"/>
        </form>
    </#if>

