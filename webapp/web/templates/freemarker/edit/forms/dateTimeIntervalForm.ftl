<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Custom form for adding date time intervals -->

<#if editConfig.object?has_content>
    <#assign editMode = "edit">
<#else>
    <#assign editMode = "add">
</#if>

<#if editMode == "edit">        
        <#assign titleVerb="Edit">        
        <#assign submitButtonText="Edit Date/Time Interval">
        <#assign disabledVal="disabled">
<#else>
        <#assign titleVerb="Create">        
        <#assign submitButtonText="Create Date/Time Interval">
        <#assign disabledVal=""/>
</#if>starField-

<h2>${titleVerb} date time interval for ${subjectName}</h2>

<form class="customForm" action ="${submitUrl}" class="customForm">
    
    <fieldset class="dateTime" role="group"> 
        <h3>Start</h3>
        <label for="starField-year">Year</label>
        <input class="text-field" name="starField-year" id="starField-year" type="text" value="" size="4" maxlength="4" role="input"/>

        <label for="starField-month">Month</label>
        <select name="starField-month" id="starField-month" role="select">
            <option value=""  role="option" <#if starField-month="">selected</#if>month</option>
            <#list starField-months as starField-month>
            <option value="starField-month" <#if starField-month = starField-month.uri>selected</#if> >${starField-month.label}</option>
            </#list>
        </select>

        <label for="starField-day">Day</label>
        <select name="starField-day" id="starField-day" role="select">
            <option value="" role="option"><#if starField-day="">selected</#if>day</option>
            <#list starField-day as starField-day>
            <option value="starField-day" role="option" <#if starField-day=starField-day.uri>selected</#if> >${starField-day.label}</option>
            </#list>
        </select>
    </fieldset>
    <fieldset class="dateTime" role="group">  
        <label for="starField-hour">Hour</label>
        <select name="starField-hour" id="starField-hour" role="select">
            <option value="" role="option"><#if starField-hour="">selected</#if>hour</option>
            <#list starField-hours as starField-hour>
            <option value="starField-hour" role="option" <#if starField-hour=starField-hour.uri>selected</#if> >${starField-hour.label}</option>
            </#list>
        </select>

        <label for="starField-minute">Minutes</label>
        <select name="starField-hour" id="starField-hour" role="select">
            <option value="" role="option"><#if starField-minute="">selected</#if>minutes</option>
            <#list starField-minutes as starField-minute>
            <option value="starField-minute" role="option" <#if starField-minute=starField-minute.uri>selected</#if> >${starField-minute.label}</option>
            </#list>
        </select>    
        
        <label for="starField-second">Seconds</label>
        <select name="starField-second" id="starField-second" role="select">
            <option value="" role="option"><#if starField-second="">selected</#if>seconds</option>
            <#list starField-seconds as starField-second>
            <option value="starField-second" role="option" <#if starField-second=starField-second.uri>selected</#if> >${starField-second.label}</option>
            </#list>
        </select>
    </fieldset>                   
       
     <fieldset class="dateTime" role="group"> 
         <h3>End</h3>
         <label for="endField-year">Year</label>
         <input class="text-field" name="endField-year" id="endField-year" type="text" value="" size="4" maxlength="4" role="input"/>

         <label for="endField-month">Month</label>
         <select name="endField-month" id="endField-month" role="select">
             <option value=""  role="option" <#if endField-month="">selected</#if>month</option>
             <#list endField-months as endField-month>
             <option value="endField-month" <#if endField-month = endField-month.uri>selected</#if> >${endField-month.label}</option>
             </#list>
         </select>

         <label for="endField-day">Day</label>
         <select name="endField-day" id="endField-day" role="select">
             <option value="" role="option"><#if endField-day="">selected</#if>day</option>
             <#list endField-day as endField-day>
             <option value="endField-day" role="option" <#if endField-day=endField-day.uri>selected</#if> >${endField-day.label}</option>
             </#list>
         </select>
     </fieldset>
     <fieldset class="dateTime" role="group">  
         <label for="endField-hour">Hour</label>
         <select name="endField-hour" id="endField-hour" role="select">
             <option value="" role="option"><#if endField-hour="">selected</#if>hour</option>
             <#list endField-hours as endField-hour>
             <option value="endField-hour" role="option" <#if endField-hour=endField-hour.uri>selected</#if> >${endField-hour.label}</option>
             </#list>
         </select>

         <label for="endField-minute">Minutes</label>
         <select name="endField-hour" id="endField-hour" role="select">
             <option value="" role="option"><#if endField-minute="">selected</#if>minutes</option>
             <#list endField-minutes as endField-minute>
             <option value="endField-minute" role="option" <#if endField-minute=endField-minute.uri>selected</#if> >${endField-minute.label}</option>
             </#list>
         </select>    

         <label for="endField-second">Seconds</label>
         <select name="endField-second" id="endField-second" role="select">
             <option value="" role="option"><#if endField-second="">selected</#if>seconds</option>
             <#list endField-seconds as endField-second>
             <option value="endField-second" role="option" <#if endField-second=endField-second.uri>selected</#if> >${endField-second.label}</option>
             </#list>
         </select>
    </fieldset>  
       
    <p class="submit">
           <input type="submit" id="submit" value="${submitButtonText}" role="button" />

           <span class="or"> or </span>

           <a class="Cancel" href="${editConfiguration.cancelUrl}" title="Cancel">Cancel</a>
       </p>
</form>