/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    public void addCaptchaRelatedFieldsToPageContext_recaptchaImpl() throws IOException {
        // Given
        props.setProperty("captcha.implementation", "RECAPTCHAv2");
        Map<String, Object> context = new HashMap<>();

        // When
        CaptchaServiceBean.addCaptchaRelatedFieldsToPageContext(context);

        // Then
        assertEquals("RECAPTCHAV2", context.get("captchaToUse"));
    }

    @Test
    public void addCaptchaRelatedFieldsToPageContext_nanocaptchaImpl() throws IOException {
        // Given
        props.setProperty("captcha.implementation", "NANOCAPTCHA");
        Map<String, Object> context = new HashMap<>();

        // When
        CaptchaServiceBean.addCaptchaRelatedFieldsToPageContext(context);

        // Then
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
        assertEquals(CaptchaImplementation.RECAPTCHAV2, captchaImpl);
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
    public void validateCaptcha_NoneValid() {
        // Given
        props.setProperty("captcha.enabled", "false");

        // Act
        boolean result = CaptchaServiceBean.validateCaptcha("anyInput", "anyChallengeId");

        // Then
        assertTrue(result);
    }
}
