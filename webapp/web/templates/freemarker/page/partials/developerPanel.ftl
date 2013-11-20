<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->


<#macro showCheckbox key>
	<input type="checkbox" id="${key}" <#if settings[key]>checked</#if>>
</#macro>

<#macro showTextbox key>
	<input type="text" id="${key}" size="30" value="${settings[key]}" >
</#macro>


<style>
div.developer {
	background-color: #f7dd8a; 
	padding: 0px 10px 0px 10px;
	font-variant: small-caps;
}

div.developer #developerPanelBody {
	display: none;
	line-height: 1em;
	font-size: small;
}

div.developer div.devleft {
	width: 49%
}

div.developer div.devright {
	float: right;
	width: 49%
}

div.developer div.container {
	border: thin groove black;
	padding: 3px 10px 0px 10px;
	margin: 3px 0px 3px 0px;
}

div.developer div.within {
	padding-left: 1em;
}

div.developer input[type="text"] { 
	padding: 2px 10px 2px 10px; 
	line-height: 1em;
	margin: 2px 2px 2px 2px; 
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
			<div>
				<label>
					<@showCheckbox "developerEnabled" />
					Enable developer mode
				</label>
			</div>
			
			<div class="devright">
				<div class="container">
					Page configuration
					<label>
						<@showCheckbox "developerPageContentsLogCustomListView" />
						Log the use of custom list view XML files.
					</label>
					<label>
						<@showCheckbox "developerPageContentsLogCustomShortView" />
						Log the use of custom short views in search, index and browse pages.
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
			</div>
				
			<div class="devleft">
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
					<div class="within">
						<label>
							<@showCheckbox "developerLoggingRDFServiceStackTrace" />
							Add stack trace
						</label>
						<label>
							Restrict by query string
							<@showTextbox "developerLoggingRDFServiceQueryRestriction" />
						</label>
						<label>
							Restrict by calling stack
							<@showTextbox "developerLoggingRDFServiceStackRestriction" />
						</label>
					</div>
				</div>
			</div>

			<div>
				<input type="button" id="developerPanelSaveButton" value="Save Settings" name="foo" />
			</div>
		</div>
	</div>
</#if>	