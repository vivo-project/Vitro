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
        <div>
          <input type="hidden" name="RequiredFields" value="webusername,webuseremail,s34gfd88p9x1" />
          <input type="hidden" name="RequiredFieldsNames" value="Name,Email address,Comments" />
          <input type="hidden" name="EmailFields" value="webuseremail" />
          <input type="hidden" name="EmailFieldsNames" value="emailaddress" />
          <input type="hidden" name="DeliveryType" value="contact" />

          <label for="webusername">${i18n().full_name} <span class="requiredHint"> *</span></label><br/>
          <input type="text" name="webusername" value="${webusername!}"/>
        </div>

        <div>
          <label for="webuseremail">${i18n().email_address} <span class="requiredHint"> *</span></label><br/>
          <input type="text" name="webuseremail"  value="${webuseremail!}"/>
        </div>

        <div>
          <label>${i18n().comments_questions} <span class="requiredHint"> *</span></label><br/>
          <textarea name="s34gfd88p9x1" rows="10" cols="90">${comments!}</textarea>
        </div>


        <#if captchaToUse == "RECAPTCHA">
            <div class="g-recaptcha" data-sitekey="${siteKey!}"></div>
            <input type="hidden" id="g-recaptcha-response" name="g-recaptcha-response" />
        <#elseif captchaToUse == "NANOCAPTCHA">
            <!-- Your custom captcha implementation -->
            <p>
                <label class="realpersonLabel">${i18n().enter_in_security_field}:<span class="requiredHint"> *</span></label>
                <div class="captcha-container">
                    <span><input id="refresh" type="button" value="↻" /></span>
                    <img id="captchaImage" src="data:image/png;base64,${challenge!}" alt="Refresh page if not displayed..." style="vertical-align: middle;">
                </div>
                <br />
                <span><input type="text" id="userSolution" name="userSolution" style="vertical-align: middle;"></span>
                <input type="text" id="challengeId" name="challengeId" value="${challengeId!}" style="display: none;">
            </p>
        </#if>

        <div class="buttons">
            <br /><input id="submit" type="submit" value="${i18n().send_mail}" />
        </div>

        <p class="requiredHint">* ${i18n().required_fields}</p>
    </form>
</section>
<script type="text/javascript">
    var i18nStrings = {
        pleaseFormatEmail: '${i18n().please_format_email?js_string}',
        enterValidAddress: '${i18n().or_enter_valid_address?js_string}'
    };
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />',
                  '<link rel="stylesheet" href="${urls.base}/css/jquery_plugins/jquery.realperson.css" />')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/commentForm.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>')}

<#if captchaToUse == "RECAPTCHA">
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
    <script>
        document.getElementById('contact_form').addEventListener('submit', function() {
            var recaptchaResponse = grecaptcha.getResponse();
            console.log(recaptchaResponse);
            document.getElementById('g-recaptcha-response').value = recaptchaResponse;
        });
    </script>
<#elseif captchaToUse == "NANOCAPTCHA">
    <script>
        $(document).ready(function () {
            $('#refresh').click(function () {
                var oldChallengeId = $('#challengeId').val();

                $.ajax({
                    url: '${contextPath}/refreshCaptcha',
                    type: 'GET',
                    dataType: 'json',
                    data: { oldChallengeId: oldChallengeId },
                    success: function (data) {
                        $('#userSolution').val('');
                        $('#challengeId').val(data.challengeId);
                        $('#captchaImage').attr('src', 'data:image/png;base64,' + data.challenge);
                    },
                    error: function () {
                        console.error('Error while refreshing captcha');
                    }
                });
            });
        });
    </script>
</#if>
