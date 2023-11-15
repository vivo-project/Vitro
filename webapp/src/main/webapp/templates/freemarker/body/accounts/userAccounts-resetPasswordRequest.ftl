<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for notifying user about reset password request state -->

<#if isEnabled && emailConfigured>
    <#if showPasswordChangeForm == true>
        <h1>${i18n().password_reset_title}</h1>
        <br/>

        <form id="forgotPasswordForm" action="${forgotPasswordUrl}" method="POST" style="margin-top: 10px;">
            <div>
                <label for="email">${i18n().password_reset_manual}</label>
                <input type="email" id="email" name="email" class="text-field focus" placeholder="user@example.com" required>

                <p>
                    <label class="realpersonLabel">${i18n().enter_in_security_field}:<span class="requiredHint"> *</span></label>
                    <input type="text" id="defaultReal" name="defaultReal" required>
                </p>

                <p class="submit" style="margin-top: 10px;">
                    <button type="submit" class="green button">${i18n().password_reset_button}</button>
                </p>
                <#if wrongCaptcha>
                    <p class="errorMessage">${i18n().wrong_captcha}</p>
                </#if>

                <br/>
                <#if contactEmailConfigured>
                    <p>${i18n().password_reset_forgot_email} <a href="${contactUrl}">${i18n().password_reset_forgot_email_contact_us}</a>.</p>
                </#if>
            </div>
        </form>
    <#else>
        <h1>${message}</h1>
    </#if>
<#else>
    <h1>${i18n().functionality_disabled} &#x1F609;.</h1>
</#if>

<style>
    label[for="email"] {
        font-size: 18px;
        display: block;
        margin-bottom: 5px;
    }

    input#email {
        font-size: 16px;
        height: 1.5em;
        width: 20em;
        margin-bottom: 2em;
    }

    .submit button.green {
        font-size: 16px;
    }

    p {
        font-size: 16px;
    }

    a {
        color: #0072b5;
        text-decoration: none;
    }

    h1 {
        font-size: 24px;
    }

    #defaultReal {
         display: block;
         margin-top: 1em;
         margin-left: 0;
         width: 246px;
    }

    #forgotPasswordForm .errorMessage {
        color: red;
        background: #efefef;
        padding: 1em;
    }
</style>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />',
                  '<link rel="stylesheet" href="${urls.base}/css/jquery_plugins/jquery.realperson.css" />')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/commentForm.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.realperson.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>')}

<#if isEnabled == true>
    <script type="text/javascript">
      $(function() {
        $('#defaultReal').realperson();
      });
    </script>
</#if>
