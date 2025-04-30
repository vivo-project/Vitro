<h2>User roles</h2>

<section id="show-auth" role="region">
	<#assign rolesControllerUrl = urls.base + "/admin/roles" >
	<script type="text/javascript">
		function editRole(roleUri, roleLabel){
		  let newLabel = prompt("${i18n().role_rename_prompt?js_string}", roleLabel);
		  if (newLabel && !(/^\s*$/.test(newLabel))) {
		    let form = document.forms["edit-" + roleUri];
		    form.label.value = newLabel;
		    form.submit();
		  }
		}
		function deleteRole(roleUri, roleLabel){
		   if(confirm("${i18n().remove_role_confirmation?js_string} (" + roleLabel + ")")){
		     document.forms["remove-" + roleUri].submit();
		   }
		}
	</script>
	<#if roles?has_content>
		<#list roles as role>
			<p>${role.label}
				<a href="javascript:editRole('${role.uri?js_string?html}', '${role.label?js_string?html}');"><img src="${urls.images!}/individual/editIcon.gif"></a>
				<#if role.uri != "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#PUBLIC">
					<a href="javascript:deleteRole('${role.uri?js_string?html}', '${role.label?js_string?html}');"><img src="${urls.images!}/individual/deleteIcon.gif"></a>
				</#if>
			</p>
			<form name="edit-${role.uri}" id="$edit-${role.uri}" action="${rolesControllerUrl}" method="POST">
				<input type="hidden" name="label">
				<input type="hidden" name="uri" value="${role.uri}">
				<input type="hidden" name="action" value="edit">
			</form>
			<form name="remove-${role.uri}" id="remove-${role.uri}" action="${rolesControllerUrl}" method="POST">
				<input type="hidden" name="uri" value="${role.uri}">
				<input type="hidden" name="action" value="remove">
			</form>
		</#list>
	</#if>
	<form action="${rolesControllerUrl}" method="POST">
		<input type="hidden" name="action" value="add">
		<input type="text" name="label">
		<input type="submit" class="submit" role="input" value="${i18n().add_new_role?html}">
	</form>
</section>
