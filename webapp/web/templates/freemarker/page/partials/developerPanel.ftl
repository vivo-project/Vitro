<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->


<#macro showCheckbox key>
	<input type="checkbox" id="${key}" <#if settings[key]>checked</#if>>
</#macro>

<#macro showTextbox key>
	<input type="text" id="${key}" size="30" value="${settings[key]}" >
</#macro>

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
				<label>
					<@showCheckbox "developerPermitAnonymousControl" />
					Allow anonymous user to see and modify developer settings
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

				<div class="container">
					Links
					<br/>
					<a href="${urls.base}/admin/log4j.jsp">Set log levels</a>
					<a href="${urls.base}/admin/showConfiguration">Show Configuration</a>
					<br/>
					<a href="${urls.base}/admin/showAuth">Show authorization info</a>
					<a href="${urls.base}/admin/showThreads">Show background threads</a>
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