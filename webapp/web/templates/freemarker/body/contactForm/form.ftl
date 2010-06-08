<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Contact form -->

<div class="staticPageBackground feedbackForm">

    <h2>${title}</h2>
    
    <p>Thank you for your interest in 
        <#compress>
            <#if portalType == "CALSResearch">
                the Cornell University College of Agriculture and Life Sciences Research Portal
            <#elseif portalType == "VIVO">
                VIVO        
            <#else>
                the ${siteName} portal
            </#if>
        </#compress>. 
        Please submit this form with questions, comments, or feedback about the content of this site.
    </p>
        
    <#if siteName == "CALSResearch" || siteName == "CALSImpact">
        <p>
            ${siteName} is a service that depends on regular updates and feedback.
            Please help us out by providing comments and suggestions for additional content (people, departments, courses, research services, etc.)
            that you would like to see represented. The reference librarians at Albert R. Mann Library will be in touch with you soon.
        </p>
    </#if>

    <form name="contact_form" id="contact_form" action="${formAction}" method="post" onsubmit="return ValidateForm('contact_form');">
        <input type="hidden" name="home" value="${portalId}"/>
        <input type="hidden" name="RequiredFields" value="webusername,webuseremail,s34gfd88p9x1"/>
        <input type="hidden" name="RequiredFieldsNames" value="Name,Email address,Comments"/>
        <input type="hidden" name="EmailFields" value="webuseremail"/>
        <input type="hidden" name="EmailFieldsNames" value="emailaddress"/>
        <input type="hidden" name="DeliveryType" value="contact"/>
    
        <label for="webusername">Full name</label>
        <p><input style="width:33%;" type="text" name="webusername" maxlength="255"/></p>
        <label for="webuseremail">Email address</label>
        <p><input style="width:25%;" type="text" name="webuseremail" maxlength="255"/></p>


        <label>Comments, questions, or suggestions</label>

        <textarea name="s34gfd88p9x1" rows="10" cols="90"></textarea>
        
        <div class="buttons">
            <input type="submit" value="Send Mail" class="yellowbutton"/>
            <input type="reset" value="Clear Form" class="plainbutton"/>
        </div

        <p style="font-weight: bold; margin-top: 1em">Thank you!</p>
    </form>    
    
</div>

${scripts.add("/js/commentForm.js")}
