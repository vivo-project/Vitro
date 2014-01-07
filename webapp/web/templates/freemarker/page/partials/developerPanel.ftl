<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->


<#macro showCheckbox key, labelText>
	<label>
		<input type="checkbox" id="${key}" <#if settings[key]>checked</#if>>
		${labelText}
	</label>
</#macro>

<#macro showTextbox key, labelText>
	<label>
		${labelText}
		<input type="text" id="${key}" size="30" value="${settings[key]}" >
	</label>
</#macro>

<#if !settings.developer_enabled>
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
				<@showCheckbox "developer_enabled", 
						"Enable developer mode" />
				<@showCheckbox "developer_permitAnonymousControl", 
						"Allow anonymous user to see and modify developer settings" />
			</div>
			
			<div id="developerTabs">
				<ul>
					<li><a href="#developerTabGeneral"><span>General</span></a></li>
					<li><a href="#developerTabAuthorization"><span>Authorization</span></a></li>
				</ul>
			
				<div id="developerTabGeneral">
					<div class="devright">
						<div class="container">
							Page configuration
							<@showCheckbox "developer_pageContents_logCustomListView", 
									"Log the use of custom list view XML files." />
							<@showCheckbox "developer_pageContents_logCustomShortView" , 
									"Log the use of custom short views in search, index and browse pages."/>
						</div>
						
						<div class="container">
							Language support
							<@showCheckbox "developer_i18n_defeatCache", 
									"Defeat the cache of language property files" />
							<@showCheckbox "developer_i18n_logStringRequests", 
									"Log the retrieval of language strings" />
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
							<@showCheckbox "developer_defeatFreemarkerCache", 
									"Defeat the template cache" />
							<@showCheckbox "developer_insertFreemarkerDelimiters", 
									"Insert HTML comments at start and end of templates" />
						</div>
		
						<div class="container">
							SPARQL Queries
							<@showCheckbox "developer_loggingRDFService_enable", 
									"Log each query" />
							<div class="within">
								<@showCheckbox "developer_loggingRDFService_stackTrace", 
										"Add stack trace" />
								<@showTextbox "developer_loggingRDFService_queryRestriction", 
										"Restrict by query string" />
								<@showTextbox "developer_loggingRDFService_stackRestriction", 
										"Restrict by calling stack" />
							</div>
						</div>
					</div>
				</div>
				
				<div id="developerTabAuthorization">
					<@showCheckbox "developer_authorization_logDecisions_enable", 
							"Write policy decisions to the log" />
					<div class="within">
						<@showCheckbox "developer_authorization_logDecisions_skipInconclusive", 
								"Skip inconclusive decisions" />
						<@showCheckbox "developer_authorization_logDecisions_addIdentifiers", 
								"Include the user identifiers in the log record" />
						<@showTextbox "developer_authorization_logDecisions_actionRestriction",
								"Restrict by requested action" />
						<@showTextbox "developer_authorization_logDecisions_policyRestriction",
								"Restrict by policy name" />
						<@showTextbox "developer_authorization_logDecisions_userRestriction",
								"Restrict by user identifiers" />
					</div>
				</div>
			</div>

			<div>
				<input type="button" id="developerPanelSaveButton" value="Save Settings" name="foo" />
			</div>
		</div>
	</div>
</#if>	