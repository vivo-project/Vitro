<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- autocomplete template for data properties -->

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
    $.getJSON("<c:url value="/dataservice"/>", {getN3EditOptionList:"1", field: "${dataLiteral}", editKey: key}, function(json){

    $("select#${dataLiteral}").replaceWith("<input type='hidden' id='${dataLiteral}' name='${dataLiteral}' /><input type='text' id='${dataLiteral}-entry' name='${dataLiteral}-entry' />");

    $("#${dataLiteral}-entry").autocomplete(json, {
            minChars: 1,
            width: 320,
            matchContains: true,
            mustMatch: 0,
            autoFill: false,
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
             $("input#${dataLiteral}-entry").attr("value", data[0]); // dump the string into the text box
             $("input#${dataLiteral}").attr("value", data[1]); // dump the uri into the hidden form input
           });
}
);
})
</script>

<h2>${formTitle}</h2>
       
    <form id="autoCompleteDatapropForm" class="editForm" action="${submitUrl}"  role="autocomplete">
 
    <#if predicate.publicDescription?has_content >       
        <p class="propEntryHelpText">${predicate.publicDescription}</p>
    </#if>
        <p>
            <select id="dataLiteral" name="dataLiteral">
                 <#list dataLiteral as key>
                     <opton value="${key}"
                     <#if editConfiguration.objectUri?has_contant && editConfiguration.object.Uri = key>selected</#if>
                 </#list>
            </select>
        </p>

    <p class="submit">
        <input type="submit" id="submit" value="editConfiguration.submitLabel"/>
        <span class="or"> or <a class="cancel" href="${editConfiguration.cancelUrl}" title="cancel">Cancel</a>
    </p>

    </form>
