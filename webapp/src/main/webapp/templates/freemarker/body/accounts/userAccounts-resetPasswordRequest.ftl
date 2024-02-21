<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for notifying user about reset password request state -->

<#if isEnabled && emailConfigured>
    <#if showPasswordChangeForm == true>
        <h1>${i18n().password_reset_title}</h1>
        <br/>

        <form id="forgotPasswordForm" action="${forgotPasswordUrl}" method="POST" style="margin-top: 10px;">
            <div>
                <label for="email">${i18n().password_reset_manual}</label>
                <input type="email" id="email" name="email" value="${emailValue}" class="text-field focus" placeholder="user@example.com" required>

                <#if errorMessage??>
                    <p class="errorMessage">${errorMessage!}</p>
                </#if>

                <#if captchaToUse == "RECAPTCHAV2">
                    <div class="g-recaptcha" data-sitekey="${siteKey!}"></div>
                    <input type="hidden" id="g-recaptcha-response" name="g-recaptcha-response" />
                <#elseif captchaToUse == "NANOCAPTCHA">
                    <p>
                        <label class="realpersonLabel">${i18n().enter_in_security_field}:<span class="requiredHint"> *</span></label>
                        <div class="captcha-container">
                            <span><input id="refresh" type="button" value="↻" /></span>
                            <img id="captchaImage" src="data:image/png;base64,${challenge!}" alt="${i18n().captcha_not_displayed}" style="vertical-align: middle;">
                        </div>
                        <br />
                        <span><input type="text" id="userSolution" name="userSolution" style="vertical-align: middle; width:220px;"></span>
                        <input type="text" id="challengeId" name="challengeId" value="${challengeId!}" style="display: none;">
                    </p>
                </#if>

                <p class="submit" style="margin-top: 10px;">
                    <button type="submit" class="green button">${i18n().password_reset_button}</button>
                </p>
                <#if wrongCaptcha>
                    <p class="errorMessage">${i18n().captcha_user_sol_invalid}</p>
                </#if>

                <br/>
                <#assign contactFormUrl = "${contactUrl}" />
                <#if contactEmailConfigured>
                    <p>${i18n().password_reset_forgot_email(contactFormUrl)}</p>
                </#if>
            </div>
        </form>
    <#else>
        <h1>${message}</h1>
    </#if>
<#else>
    <h1>${i18n().functionality_disabled}</h1>
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

<#include "webapp/src/main/webapp/templates/freemarker/body/captcha/captcha-clientExecutionLogic.ftl">
