package edu.cornell.mannlib.vitro.webapp.beans;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Recaptchav2Provider generates and manages captcha challenges using Google RECAPTCHAv2.
 * This class extends AbstractCaptchaProvider.
 *
 * @see AbstractCaptchaProvider
 * @see CaptchaBundle
 */
public class Recaptchav2Provider extends AbstractCaptchaProvider {

    @Override
    public CaptchaBundle generateRefreshChallenge() {
        return new CaptchaBundle("", "", ""); // RECAPTCHAv2 does not generate refresh challenges on backend side
    }

    @Override
    public void addCaptchaRelatedFieldsToPageContext(Map<String, Object> context) {
        context.put("siteKey",
            Objects.requireNonNull(ConfigurationProperties.getInstance().getProperty("recaptcha.siteKey"),
                "You have to provide a site key through configuration file."));
    }

    @Override
    public boolean validateCaptcha(String captchaInput, String challengeId) {
        return validateReCaptcha(captchaInput);
    }

    /**
     * Validates a reCAPTCHA response using Google's reCAPTCHA API.
     *
     * @param recaptchaResponse The reCAPTCHA response to validate.
     * @return True if the reCAPTCHA response is valid, false otherwise.
     */
    public boolean validateReCaptcha(String recaptchaResponse) {
        String secretKey =
            Objects.requireNonNull(ConfigurationProperties.getInstance().getProperty("recaptcha.secretKey"),
                "You have to provide a secret key through configuration file.");
        String verificationUrl =
            "https://www.google.com/recaptcha/api/siteverify?secret=" + secretKey + "&response=" + recaptchaResponse;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet verificationRequest = new HttpGet(verificationUrl);
            HttpResponse verificationResponse = httpClient.execute(verificationRequest);

            String responseBody = EntityUtils.toString(verificationResponse.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            ReCaptchaResponse response = objectMapper.readValue(responseBody, ReCaptchaResponse.class);

            return response.isSuccess();
        } catch (IOException e) {
            log.warn("ReCaptcha validation failed.");
        }

        return false;
    }
}
