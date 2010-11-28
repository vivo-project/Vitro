<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the Fake External Authentication page. -->

<section role="region">
    <h2>Fake External Authentication</h2>
    
    <p>
      Enter the userID that you want to sign in as, or click Cancel.
    </p>

		<form action="${controllerUrl}">
			Username:
			<input type="text" name="username" />
			<input type="submit" value="submit" /> 
			<input type="submit" name="cancel" value="cancel" /> 
		</form>
    <br/>
</section>
