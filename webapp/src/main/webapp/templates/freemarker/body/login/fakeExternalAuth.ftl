<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the Fake External Authentication page. -->

<section role="region">
    <h2>${i18n().fake_external_auth}</h2>
    
    <p>
      ${i18n().enter_id_to_login}
    </p>

		<form action="${controllerUrl}">
			${i18n().username}:
			<input type="text" name="username" />
			<input type="submit" value="${i18n().submit_button}" /> 
			<input type="submit" name="cancel" value="${i18n().cancel_link}" /> 
		</form>
    <br/>
</section>
