package edu.cornell.mannlib.vitro.webapp.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.CaptchaBundle;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaServiceBean;
import edu.cornell.mannlib.vitro.webapp.beans.NanocaptchaProvider;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.junit.Before;
import org.junit.Test;
import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

public class NanocaptchaProviderTest {

    private final ConfigurationPropertiesStub props = new ConfigurationPropertiesStub();

    private final NanocaptchaProvider provider = new NanocaptchaProvider();


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
    public void generateChallenge_ValidEasyChallengeGenerated() throws IOException {
        // Given
        props.setProperty("nanocaptcha.difficulty", "easy");

        // When
        CaptchaBundle captchaBundle = provider.generateRefreshChallenge();

        // Then
        assertNotNull(captchaBundle);
        assertNotNull(captchaBundle.getB64Image());
        assertNotNull(captchaBundle.getCode());
        assertNotNull(captchaBundle.getCaptchaId());
    }

    @Test
    public void generateChallenge_ValidEmptyChallengeGenerated() throws IOException {
        // Given
        props.setProperty("nanocaptcha.difficulty", "");

        // When
        CaptchaBundle captchaBundle = provider.generateRefreshChallenge();

        // Then
        assertNotNull(captchaBundle);
        assertNotNull(captchaBundle.getB64Image());
        assertNotNull(captchaBundle.getCode());
        assertNotNull(captchaBundle.getCaptchaId());
    }

    @Test
    public void generateChallenge_ValidInvalidDifficultyChallengeGenerated() throws IOException {
        // Given
        props.setProperty("nanocaptcha.difficulty", "asdasdasd");

        // When
        CaptchaBundle captchaBundle = provider.generateRefreshChallenge();

        // Then
        assertNotNull(captchaBundle);
        assertNotNull(captchaBundle.getB64Image());
        assertNotNull(captchaBundle.getCode());
        assertNotNull(captchaBundle.getCaptchaId());
    }

    @Test
    public void generateChallenge_ValidHardChallengeGenerated() throws IOException {
        // Given
        props.setProperty("nanocaptcha.difficulty", "hard");

        // When
        CaptchaBundle captchaBundle = provider.generateRefreshChallenge();

        // Then
        assertNotNull(captchaBundle);
        assertNotNull(captchaBundle.getB64Image());
        assertNotNull(captchaBundle.getCode());
        assertNotNull(captchaBundle.getCaptchaId());
    }

    @Test
    public void validateCaptcha_NanoCaptchaValid() {
        // Given
        CaptchaBundle sampleChallenge = new CaptchaBundle("sampleB64Image", "validCode", "challengeId");
        CaptchaServiceBean.getCaptchaChallenges().put("challengeId", sampleChallenge);
        props.setProperty("captcha.implementation", "NANOCAPTCHA");

        // Act
        boolean result = provider.validateCaptcha("validCode", "challengeId");

        // Assert
        assertTrue(result);
    }

    @Test
    public void addCaptchaRelatedFieldsToPageContext_NanocaptchaImpl() throws IOException {
        // Given
        Map<String, Object> context = new HashMap<>();

        // When
        provider.addCaptchaRelatedFieldsToPageContext(context);

        // Assert
        assertNull(context.get("siteKey"));
        assertNotNull(context.get("challenge"));
        assertNotNull(context.get("challengeId"));
    }
}
