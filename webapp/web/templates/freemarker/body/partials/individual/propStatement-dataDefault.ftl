<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- VIVO-specific default data property statement template. 
    
     This template must be self-contained and not rely on other variables set for the individual page, because it
     is also used to generate the property statement during a deletion.  
 -->
<#import "lib-datetime.ftl" as dt>
<@showStatement statement />

<#macro showStatement statement>
    <#assign theValue = statement.value />
    <#if theValue?contains("<ul>") >
        <#assign theValue = theValue?replace("<ul>","<ul class='tinyMCEDisc'>") />
    </#if>
    <#if theValue?contains("<ol>") >
        <#assign theValue = theValue?replace("<ol>","<ol class='tinyMCENumeric'>") />
    </#if>
    <#if theValue?contains("<p>") >
        <#assign theValue = theValue?replace("<p>","<p style='margin-bottom:.6em'>") />
    </#if>
	<#if theValue?matches("([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})") >
		<#assign theValue = theValue + "T00:00:00" />
		${dt.formatXsdDateTimeLong(theValue, "yearMonthDayPrecision")}
	<#elseif theValue?matches("^([0-9]{4})-((0[1-9])|(1[0-2]))-((0[1-9])|([1-2][0-9])|(3[0-1]))(T|\\s)(([0-1][0-9])|(2[0-3])):([0-5][0-9]):([0-5][0-9])")>
		${dt.formatXsdDateTimeLong(theValue, "yearMonthDayTimePrecision")}
	<#elseif theValue?matches("^([0-9]{4})-(0[1-9]|1[012])")>
		<#assign theValue = theValue + "-01T00:00:00" />
		${dt.formatXsdDateTimeLong(theValue, "yearMonthPrecision")}
	<#elseif theValue?matches("^--(0[1-9]|1[012])")>
		<#assign theValue = "2000" + theValue?substring(1) + "-01T00:00:00" />
		${dt.formatXsdDateTimeLong(theValue, "monthPrecision")}
	<#else>
    	${theValue}
	</#if>
</#macro>