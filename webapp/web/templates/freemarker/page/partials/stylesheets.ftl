<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#-- Template for loading stylesheets in the head -->
${stylesheets.add("/css/edit.css")} <#-- temporary until edit controller can include this when needed -->
${stylesheets.add("/css/menupage/menupage.css")} <#-- we need to call it from the theme until freemarker controller and menupage.ftl are created -->
${stylesheets.add("/css/individual/individual.css")} <#-- we need to call it from the theme until freemarker controller and individual.ftl are created -->
${stylesheets.tags}

<link rel="stylesheet" href="${urls.theme}/css/screen.css" />