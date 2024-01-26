<#assign isEnabled = isEnabled!true>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/commentForm.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>')}

<#if isEnabled && captchaToUse == "RECAPTCHAV2">
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
    <script>
        document.getElementById('contact_form').addEventListener('submit', function() {
            var recaptchaResponse = grecaptcha.getResponse();
            console.log(recaptchaResponse);
            document.getElementById('g-recaptcha-response').value = recaptchaResponse;
        });
    </script>
<#elseif isEnabled && captchaToUse == "NANOCAPTCHA">
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
