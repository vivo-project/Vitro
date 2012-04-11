<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Contact form -->

<section class="staticPageBackground feedbackForm" role="region">
    <h2>${title}</h2>
    
    <#if errorMessage?has_content>       
        <section id="error-alert"><img src="${urls.images}/iconAlert.png" role="error alert"/>
            <p>${errorMessage}</p>
        </section>
    </#if>

    <p>Thank you for your interest in ${siteName}. 
        Please submit this form with questions, comments, or feedback about the content of this site.
    </p>
        
    <form name="contact_form" id="contact_form" class="customForm" action="${formAction!}" method="post" onSubmit="return ValidateForm('contact_form');" role="contact form">
        <input type="hidden" name="RequiredFields" value="webusername,webuseremail,s34gfd88p9x1" />
        <input type="hidden" name="RequiredFieldsNames" value="Name,Email address,Comments" />
        <input type="hidden" name="EmailFields" value="webuseremail" />
        <input type="hidden" name="EmailFieldsNames" value="emailaddress" />
        <input type="hidden" name="DeliveryType" value="contact" />
    
        <label for="webusername">Full name <span class="requiredHint"> *</span></label>
        <input type="text" name="webusername" value="${webusername!}"/>
        
        <label for="webuseremail">Email address <span class="requiredHint"> *</span></label>
        <input type="text" name="webuseremail"  value="${webuseremail!}"/>

        <label>Comments, questions, or suggestions <span class="requiredHint"> *</span></label>
        <textarea name="s34gfd88p9x1" rows="10" cols="90">${comments!}</textarea>
        
       
    	<p><label class="realpersonLabel">Please enter the letters displayed below into the security field:</label>

    		<input type="text" id="defaultReal" name="defaultReal"></p>
        
        <div class="buttons">
            <br /><input id="submit" type="submit" value="Send Mail" />
        </div>
        
        <p class="requiredHint">* required fields</p>
    </form>    
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />',
                  '<link rel="stylesheet" href="${urls.base}/css/jquery_plugins/jquery.realperson.css" />')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/commentForm.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.realperson.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
<script type="text/javascript">
  $(function() {
    $('#defaultReal').realperson();
  });
</script>
