/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaBundle;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaImplementation;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaServiceBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.junit.Before;
import org.junit.Test;
import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

public class CaptchaServiceBeanTest extends AbstractTestClass {

    private final ConfigurationPropertiesStub props = new ConfigurationPropertiesStub();

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
        boolean result = CaptchaServiceBean.validateReCaptcha("invalidResponse");

        // Then
        assertFalse(result);
    }

    @Test
    public void generateChallenge_ValidEasyChallengeGenerated() throws IOException {
        // Given
        props.setProperty("nanocaptcha.difficulty", "easy");

        // When
        CaptchaBundle captchaBundle = CaptchaServiceBean.generateChallenge();

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
        CaptchaBundle captchaBundle = CaptchaServiceBean.generateChallenge();

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
        CaptchaBundle captchaBundle = CaptchaServiceBean.generateChallenge();

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
        CaptchaBundle captchaBundle = CaptchaServiceBean.generateChallenge();

        // Then
        assertNotNull(captchaBundle);
        assertNotNull(captchaBundle.getB64Image());
        assertNotNull(captchaBundle.getCode());
        assertNotNull(captchaBundle.getCaptchaId());
    }

    @Test
    public void getChallenge_MatchingCaptchaIdAndRemoteAddress_ReturnsCaptchaBundle() {
        // Given
        String captchaId = "sampleCaptchaId";
        CaptchaBundle sampleChallenge = new CaptchaBundle("sampleB64Image", "sampleCode", captchaId);
        CaptchaServiceBean.getCaptchaChallenges().put(captchaId, sampleChallenge);

        // When
        Optional<CaptchaBundle> result = CaptchaServiceBean.getChallenge(captchaId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sampleChallenge, result.get());
    }

    @Test
    public void getChallenge_NonMatchingCaptchaIdAndRemoteAddress_ReturnsEmptyOptional() {
        // When
        Optional<CaptchaBundle> result = CaptchaServiceBean.getChallenge("nonMatchingId");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    public void addCaptchaRelatedFieldsToPageContext_RecaptchaImpl() throws IOException {
        // Given
        props.setProperty("captcha.implementation", "RECAPTCHAv2");
        props.setProperty("recaptcha.siteKey", "SITE_KEY");
        Map<String, Object> context = new HashMap<>();

        // When
        CaptchaServiceBean.addCaptchaRelatedFieldsToPageContext(context);

        // Assert
        assertNotNull(context.get("siteKey"));
        assertNull(context.get("challenge"));
        assertNull(context.get("challengeId"));
        assertEquals("RECAPTCHAv2", context.get("captchaToUse"));
    }

    @Test
    public void addCaptchaRelatedFieldsToPageContext_NanocaptchaImpl() throws IOException {
        // Given
        props.setProperty("captcha.implementation", "NANOCAPTCHA");
        Map<String, Object> context = new HashMap<>();

        // When
        CaptchaServiceBean.addCaptchaRelatedFieldsToPageContext(context);

        // Assert
        assertNull(context.get("siteKey"));
        assertNotNull(context.get("challenge"));
        assertNotNull(context.get("challengeId"));
        assertEquals("NANOCAPTCHA", context.get("captchaToUse"));
    }

    @Test
    public void getCaptchaImpl_EnabledCaptcha() {
        // Given
        props.setProperty("captcha.enabled", "true");
        props.setProperty("captcha.implementation", "RECAPTCHAv2");

        // When
        CaptchaImplementation captchaImpl = CaptchaServiceBean.getCaptchaImpl();

        // Then
        assertEquals(CaptchaImplementation.RECAPTCHAv2, captchaImpl);
    }

    @Test
    public void getCaptchaImpl_DisabledCaptcha() {
        // Given
        props.setProperty("captcha.enabled", "false");

        // When
        CaptchaImplementation captchaImpl = CaptchaServiceBean.getCaptchaImpl();

        // Then
        assertEquals(CaptchaImplementation.NONE, captchaImpl);
    }

    @Test
    public void getCaptchaImpl_DefaultImplementation() {
        // Given
        props.setProperty("captcha.enabled", "true");
        props.setProperty("captcha.implementation", null);

        // When
        CaptchaImplementation captchaImpl = CaptchaServiceBean.getCaptchaImpl();

        // Then
        assertEquals(CaptchaImplementation.NANOCAPTCHA, captchaImpl);
    }

    @Test
    public void validateCaptcha_NanoCaptchaValid() {
        // Given
        CaptchaBundle sampleChallenge = new CaptchaBundle("sampleB64Image", "validCode", "challengeId");
        CaptchaServiceBean.getCaptchaChallenges().put("challengeId", sampleChallenge);
        props.setProperty("captcha.implementation", "NANOCAPTCHA");

        // Act
        boolean result = CaptchaServiceBean.validateCaptcha("validCode", "challengeId");

        // Assert
        assertTrue(result);
    }

    @Test
    public void validateCaptcha_NoneValid() {
        // Given
        props.setProperty("captcha.enabled", "false");

        // Act
        boolean result = CaptchaServiceBean.validateCaptcha("anyInput", "anyChallengeId");

        // Assert
        assertTrue(result);
    }
}
