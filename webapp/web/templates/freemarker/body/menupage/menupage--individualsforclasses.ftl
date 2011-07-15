<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--Need to add restrict classes information-->
<input type="hidden" name="restrictClasses" id="restrictClasses" value="${internalClass}"/>
<#--Using the same page setup as regular class groups so including the entire template-->
<#include "menupage.ftl">
<#--add script-->
<#if !noData>
	 ${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/browseByVClasses.js"></script>')}
</#if>