package edu.cornell.mannlib.vitro.webapp.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.Recaptchav2Provider;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.junit.Before;
import org.junit.Test;
import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

public class Recaptchav2ProviderTest extends AbstractTestClass {

    private final ConfigurationPropertiesStub props = new ConfigurationPropertiesStub();

    private final Recaptchav2Provider provider = new Recaptchav2Provider();


    @Before
    public void createConfigurationProperties() {
        props.setProperty("captcha.enabled", "true");
        ServletContextStub ctx = new ServletContextStub();
        ConfigurationProperties.setInstance(props);

        HttpSessionStub session = new HttpSessionStub();
        session.setServletContext(ctx);

        HttpServletRequestStub httpServletRequest = new HttpServletRequestStub();
        httpServletRequest.setSession(session);
    }

    @Test
    public void validateReCaptcha_InvalidResponse_ReturnsFalse() {
        // Given
        props.setProperty("recaptcha.secretKey", "WRONG_SECRET_KEY");

        // When
        boolean result = provider.validateReCaptcha("invalidResponse");

        // Then
        assertFalse(result);
    }

    @Test
    public void addCaptchaRelatedFieldsToPageContext_RecaptchaImpl() throws IOException {
        // Given
        props.setProperty("recaptcha.siteKey", "SITE_KEY");
        Map<String, Object> context = new HashMap<>();

        // When
        provider.addCaptchaRelatedFieldsToPageContext(context);

        // Assert
        assertNotNull(context.get("siteKey"));
        assertNull(context.get("challenge"));
        assertNull(context.get("challengeId"));
    }
}
