<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Contact form -->

<section class="staticPageBackground feedbackForm" role="region">
    <h2>${title!}</h2>

    <#if errorMessage?has_content>
        <section id="error-alert"><img src="${urls.images}/iconAlert.png" role="error alert"/>
            <p>${errorMessage}</p>
        </section>
    </#if>

    <p>${i18n().interest_thanks(siteName)}</p>

    <form name="contact_form" id="contact_form" class="customForm" action="${formAction!}" method="post" onSubmit="return ValidateForm('contact_form');" role="contact form">
        <input type="hidden" name="RequiredFields" value="webusername,webuseremail,s34gfd88p9x1" />
        <input type="hidden" name="RequiredFieldsNames" value="Name,Email address,Comments" />
        <input type="hidden" name="EmailFields" value="webuseremail" />
        <input type="hidden" name="EmailFieldsNames" value="emailaddress" />
        <input type="hidden" name="DeliveryType" value="contact" />

        <label for="webusername">${i18n().full_name} <span class="requiredHint"> *</span></label>
        <input type="text" name="webusername" value="${webusername!}"/>

        <label for="webuseremail">${i18n().email_address} <span class="requiredHint"> *</span></label>
        <input type="text" name="webuseremail"  value="${webuseremail!}"/>

        <label>${i18n().comments_questions} <span class="requiredHint"> *</span></label>
        <textarea name="s34gfd88p9x1" rows="10" cols="90">${comments!}</textarea>


    	<p><label class="realpersonLabel">${i18n().enter_in_security_field}:<span class="requiredHint"> *</span></label>

    		<input type="text" id="defaultReal" name="defaultReal"></p>

        <div class="buttons">
            <br /><input id="submit" type="submit" value="${i18n().send_mail}" />
        </div>

        <p class="requiredHint">* ${i18n().required_fields}</p>
    </form>
</section>
<script type="text/javascript">
    var i18nStrings = {
        pleaseFormatEmail: "${i18n().please_format_email}",
        enterValidAddress: "${i18n().or_enter_valid_address}"
    };
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />',
                  '<link rel="stylesheet" href="${urls.base}/css/jquery_plugins/jquery.realperson.css" />')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/commentForm.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.realperson.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>')}
<script type="text/javascript">
  $(function() {
    $('#defaultReal').realperson();
  });
</script>
