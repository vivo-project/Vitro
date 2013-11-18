<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->


<#macro showCheckbox key>
	<input type="checkbox" id="${key}" <#if settings[key]>checked</#if>>
</#macro>

<#macro showTextbox key>
	<input type="text" id="${key}" size="40" value="${settings[key]}" >
</#macro>


<style>
div.developer {
	background-color: red; 
	padding: 0px 10px 0px 10px;
	font-size: small;
	font-variant: small-caps;
}

div.developer #developerPanelBody {
	display: none;
}

div.developer .container {
	border: thin groove black
}
</style>

<#if !settings.developerEnabled>
<#elseif !settings.mayControl>
	<div class="developer">
		<h1>${siteName} is running in developer mode.</h1>
	</div>
<#else>
	<div class="developer">
		<h1 id="developerPanelClickMe">${siteName} is running in developer mode.  
			<span id="developerPanelClickText">(click for Options)</span>
		</h1>
		<div id="developerPanelBody">
			<form>
				<label>
					<@showCheckbox "developerEnabled" />
					Enable developer mode
				</label>
				
				<div class="container">
					Freemarker templates
					<label>
						<@showCheckbox "developerDefeatFreemarkerCache" />
						Defeat the template cache
					</label>
					<label>
						<@showCheckbox "developerInsertFreemarkerDelimiters" />
						Insert HTML comments at start and end of templates
					</label>
				</div>
				
				<div class="container">
					SPARQL Queries
					<label>
						<@showCheckbox "developerLoggingRDFServiceEnable" />
						Log each query 
					</label>
					<label>
						<@showCheckbox "developerLoggingRDFServiceStackTrace" />
						Add stack trace
					</label>
					<label>
						Restrict by calling stack
						<@showTextbox "developerLoggingRDFServiceRestriction" />
					</label>
				</div>

				<div class="container">
					Language support
					<label>
						<@showCheckbox "developerI18nDefeatCache" />
						Defeat the cache of language property files
					</label>
					<label>
						<@showCheckbox "developerI18nLogStringRequests" />
						Log the retrieval of language strings
					</label>
				</div>

				<input type="button" id="developerPanelSaveButton" value="Save Settings" name="foo" />
			</form>
		</div>
	</div>
</#if>	