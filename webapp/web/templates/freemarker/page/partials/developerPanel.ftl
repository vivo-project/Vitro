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
					<li><a href="#developerTabSearch"><span>Search</span></a></li>
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
							<br/>
							<a href="${urls.base}/admin/showSources">Show RDF data sources</a>
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
							<@showCheckbox "developer_loggingRDFService_enable", "Log each query" />
							<div class="within">
								<@showCheckbox "developer_loggingRDFService_stackTrace", 
										"Show stack trace" />
								<@showTextbox "developer_loggingRDFService_queryRestriction", 
										"Restrict by query string" />
								<@showTextbox "developer_loggingRDFService_stackRestriction", 
										"Restrict by calling stack" />
							</div>
						</div>
					</div>
				</div>
				
				<div id="developerTabSearch">
					<div class="devright">
						<div class="container">
							Searching
							<@showCheckbox "developer_searchEngine_enable", "Log searches" />
							<div class="within">
								<@showCheckbox "developer_searchEngine_addStackTrace", "Show stack trace" />
								<@showCheckbox "developer_searchEngine_addResults", "Show search results" />
								<@showTextbox "developer_searchEngine_queryRestriction", 
										"Restrict by query string" />
								<@showTextbox "developer_searchEngine_stackRestriction", 
										"Restrict by calling stack" />
							</div>
						</div>
						
						<div class="container">
							Links
							<br/>
							<a href="${urls.base}/SearchIndex">Rebuild search index</a>
						</div>
					</div>
						
					<div class="devleft">
						<div class="container">
							Indexing
							<@showCheckbox "developer_searchIndex_enable", "Log indexing." />
							<div class="within">
								<@showCheckbox "developer_searchIndex_showDocuments", 
										"Show document contents" />
								<@showTextbox "developer_searchIndex_uriOrNameRestriction", 
										"Restrict by URI or name" />
								<@showTextbox "developer_searchIndex_documentRestriction", 
										"Restrict by document contents" />
							</div>
							<@showCheckbox "developer_searchIndex_logIndexingBreakdownTimings", 
									"Log breakdown timings for indexing operation." />
							<@showCheckbox "developer_searchDeletions_enable", "Log deletions." />
							<div class="container">
								<@showCheckbox "developer_searchIndex_suppressModelChangeListener", 
										"Suppress the automatic indexing of changed triples." />
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