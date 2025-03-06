
<#macro listOperationPermissions operation2RolesMap>
	<#list operation2RolesMap?keys as operation>
		${i18n().operation_permissions_for_this_property(operation)}<br>
		<#assign roles = operation2RolesMap[operation] /> 
		<#list roles as role>
			<input id="${operation?html}-${role.uri?html}" type="checkbox" name="${operation?lower_case?html}Roles" value="${role.uri?html}">
			<label class="inline" for="${operation?html}-${role.uri?html}">${role.label?html}</label>
		</#list>
		<br>
	</#list>
</#macro>
