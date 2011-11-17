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
</#if>startField-

<h2>${titleVerb} date time interval for ${subjectName}</h2>

<form class="customForm" action ="${submitUrl}" class="customForm">
    
    <fieldset class="dateTime" role="group"> 
        <h3>Start</h3>
        <label for="startField-year">Year</label>
        <input class="text-field" name="startField-year" id="startField-year" type="text" value="" size="4" maxlength="4" role="input"/>

        <label for="startField-month">Month</label>
        <select name="startField-month" id="startField-month" role="select">
            <option value=""  role="option" <#if startField-month="">selected</#if>month</option>
            <#list startField-months as startFieldMonth>
            <option value="startField-month" <#if startFieldMonth = startField-month.uri>selected</#if> >${startFieldMonth.label}</option>
            </#list>
        </select>

        <label for="startField-day">Day</label>
        <select name="startField-day" id="startField-day" role="select">
            <option value="" role="option"><#if startField-day="">selected</#if>day</option>
            <#list startField-day as startField-day>
            <option value="startField-day" role="option" <#if startField-day=startField-day.uri>selected</#if> >${startField-day.label}</option>
            </#list>
        </select>
    </fieldset>
    <fieldset class="dateTime" role="group">  
        <label for="startField-hour">Hour</label>
        <select name="startField-hour" id="startField-hour" role="select">
            <option value="" role="option"><#if startField-hour="">selected</#if>hour</option>
            <#list startField-hours as startField-hour>
            <option value="startField-hour" role="option" <#if startField-hour=startField-hour.uri>selected</#if> >${startField-hour.label}</option>
            </#list>
        </select>

        <label for="startField-minute">Minutes</label>
        <select name="startField-hour" id="startField-hour" role="select">
            <option value="" role="option"><#if startField-minute="">selected</#if>minutes</option>
            <#list startField-minutes as startField-minute>
            <option value="startField-minute" role="option" <#if startField-minute=startField-minute.uri>selected</#if> >${startField-minute.label}</option>
            </#list>
        </select>    
        
        <label for="startField-second">Seconds</label>
        <select name="startField-second" id="startField-second" role="select">
            <option value="" role="option"><#if startField-second="">selected</#if>seconds</option>
            <#list startField-seconds as startField-second>
            <option value="startField-second" role="option" <#if startField-second=startField-second.uri>selected</#if> >${startField-second.label}</option>
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