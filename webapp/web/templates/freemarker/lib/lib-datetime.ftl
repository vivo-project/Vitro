<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Macros for datetime formatting  --> 

<#-- Macro yearInterval

     Display a year interval in a property statement
     
     Set endYearAsRange=false to display an end year without a start year without a preceding hyphen. The hyphen indicates that
     a range is expected but the start year is not provided (e.g., core:personInPosition); no hyphen indicates that
     a single date is typical (e.g., core:educationalTraining).
-->

<#macro yearInterval startDateTime endDateTime endYearAsRange=true>
    <#local yearInterval>
        <#if startDateTime?has_content>
            <#local startYear = xsdDateTimeToYear(startDateTime)>
        </#if>
        <#if endDateTime?has_content>
            <#local endYear = xsdDateTimeToYear(endDateTime)>
        </#if>
        <#if startYear?? && endYear??>
            ${startYear} - ${endYear}
        <#elseif startYear??>
            ${startYear} -
        <#elseif endYear ??>
            <#if endYearAsRange>- </#if>${endYear}
        </#if>
    </#local>
    
    <#if yearInterval?has_content>
        <span class="listDateTime">${yearInterval}</span>
    </#if>  
</#macro>

<#macro dateTimeInterval dateTimeStart precisionStart dateTimeEnd precisionEnd endAsRange=true>

    <#if dateTimeStart?has_content>   
        <#local start = formatXsdDateTime(dateTimeStart, precisionStart)>
    </#if>
    
    <#if dateTimeEnd?has_content>
        <#local end = formatXsdDateTime(dateTimeEnd, precisionEnd)>
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
    
    <#if interval?has_content>
        <span class="listDateTime">${interval}</span>
    </#if>
</#macro>

<#-- Functions for formatting and applying precision to a datetime

     Currently these do more than format the datetime string, they select the precision as well. This should change in a future
     implementation; see NIHVIVO-1567. We want the Java code to apply the precision to the datetime string to pass only the
     meaningful data to the templates. The templates can format as they like, so these functions/macros would do display formatting
     but not data extraction.
-->

<#function toDateTime dateTimeString>
    <#-- First convert the datetime string to a string format that Freemarker 
         understands, then to a datetime object -->
    <#return dateTimeString?replace("T", " ")?datetime("yyyy-MM-dd HH:mm:ss")>
</#function>

<#function formatXsdDateTime dateTime precision view="short">

    <#-- Convert to a string format that Freemarker understands,
    then to a datetime -->
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
        <#else>
             <#if precision == "yearPrecision">yyyy
            <#elseif precision == "yearMonthPrecision">M-yyyy
            <#elseif precision == "yearMonthDayPrecision">M-d-yyyy
            <#else>M-d-yyyy h:mm a
            </#if>
        </#if>
    </#local>
    
    <#return dt?string(format)>
</#function>
     
<#-- Function xsdDateTimeToYear -->

<#-- get rid of this -->

<#function xsdDateTimeToYear datetime>
    <#local format = "yyyy">
    <#return datetime?date(format)?string(format)>
</#function>
