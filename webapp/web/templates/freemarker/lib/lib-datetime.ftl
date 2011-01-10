<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros and functions for datetime formatting --> 

<#-- Convenience macros to display year and year interval attributes in a classed span -->
<#macro yearSpan dateTime>
    <#if dateTime?has_content>
        <span class="listDateTime">${xsdDateTimeToYear(dateTime)}</span>
    </#if>
</#macro>

<#macro yearIntervalSpan startDateTime endDateTime endYearAsRange=true>
    <#local yearInterval = yearInterval(startDateTime, endDateTime, endYearAsRange)>
    <#if yearInterval?has_content>
        <span class="listDateTime">${yearInterval}</span>
    </#if>  
</#macro>


<#-- Assign a year precision and generate the interval -->
<#function yearInterval dateTimeStart dateTimeEnd endYearAsRange=true>
    <#local precision = "yearPrecision">
    <#return dateTimeIntervalShort(dateTimeStart, precision, dateTimeEnd, precision, endYearAsRange)>
</#function>

<#-- Generate a datetime interval with dates displayed as "January 1, 2011" -->
<#function dateTimeIntervalLong dateTimeStart precisionStart dateTimeEnd precisionEnd endAsRange=true>
    <#return dateTimeInterval(dateTimeStart, precisionStart, dateTimeEnd, precisionEnd, "long", endAsRange) >
</#function>

<#-- Generate a datetime interval with dates displayed as "1/1/2011" -->
<#function dateTimeIntervalShort dateTimeStart precisionStart dateTimeEnd precisionEnd endAsRange=true>
    <#return dateTimeInterval(dateTimeStart, precisionStart, dateTimeEnd, precisionEnd, "short", endAsRange)>
</#function>

<#-- Generate a datetime interval -->
<#function dateTimeInterval dateTimeStart precisionStart dateTimeEnd precisionEnd view="short" endAsRange=true>

    <#if dateTimeStart?has_content>   
        <#local start = formatXsdDateTime(dateTimeStart, precisionStart, view)>
    </#if>
    
    <#if dateTimeEnd?has_content>
        <#local end = formatXsdDateTime(dateTimeEnd, precisionEnd, view)>
    </#if>
    
    <#local interval>
        <#if start?? && end??>
            ${start} - ${end}
        <#elseif start??>
            ${start} -
        <#elseif end??>
            <#if endAsRange>- </#if>${end}
        </#if>
    </#local>
    
    <#return interval>
</#function>

<#-- Functions for formatting and applying precision to a datetime

     Currently these do more than format the datetime string, they select the precision as well. This should change in a future
     implementation; see NIHVIVO-1567. We want the Java code to apply the precision to the datetime string to pass only the
     meaningful data to the templates. The templates can format as they like, so these functions/macros would do display formatting
     but not data extraction.
     
     On the other hand, this is so easy that it may not be worth re-implementing to gain a bit more MVC compliance.
-->

<#-- Generate a datetime with date formatted as "January 1, 2011" -->
<#function formatXsdDateTimeLong dateTime precision>
    <#return formatXsdDateTime(dateTime, precision, "long")>
</#function>

<#-- Generate a datetime with date formatted as "1/1/2011" -->
<#function formatXsdDateTimeShort dateTime precision>
    <#return formatXsdDateTime(dateTime, precision)>
</#function>

<#-- Generate a datetime as a year -->
<#function xsdDateTimeToYear dateTime>
    <#local precision = "yearPrecision">
    <#return formatXsdDateTime(dateTime, precision)>
</#function>

<#-- Convert the string dateTimeString to a datetime object -->
<#function toDateTime dateTimeString>
    <#-- First convert the datetime string to a string format that Freemarker 
         understands, then to a datetime object --> 
    <#return dateTimeString?replace("T", " ")?replace("Z", "")?datetime("yyyy-MM-dd HH:mm:ss")>
</#function>

<#-- Apply a precision and view to format a datetime -->
<#function formatXsdDateTime dateTime precision view="short">

    <#-- First convert the string dateTime to a datetime object -->
    <#local dt = toDateTime(dateTime)>
    
    <#-- Use the precision to determine which portion to display, 
         and the view to determine how to display it.  -->
    <#local format>
        <#if view == "long">
            <#if precision == "yearPrecision">yyyy
            <#elseif precision == "yearMonthPrecision">MMMM yyyy
            <#elseif precision == "yearMonthDayPrecision">MMMM d, yyyy
            <#else>MMMM d, yyyy h:mm a
            </#if>
        <#else> <#-- view == "short" -->
             <#if precision == "yearPrecision">yyyy
            <#elseif precision == "yearMonthPrecision">M/yyyy
            <#elseif precision == "yearMonthDayPrecision">M/d/yyyy
            <#else>M/d/yyyy h:mm a
            </#if>
        </#if>
    </#local>
    
    <#return dt?string(format)>
</#function>
     

