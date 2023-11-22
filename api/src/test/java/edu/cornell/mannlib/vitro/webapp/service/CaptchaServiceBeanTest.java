package edu.cornell.mannlib.vitro.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaBundle;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaServiceBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import org.junit.Before;
import org.junit.Test;
import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

public class CaptchaServiceBeanTest extends AbstractTestClass {

    HttpServletRequestStub httpServletRequest;


    @Before
    public void createConfigurationProperties() throws Exception {
        ConfigurationPropertiesStub props = new ConfigurationPropertiesStub();
        props.setProperty("recaptcha.secretKey", "secretKey");

        ServletContextStub ctx = new ServletContextStub();
        props.setBean(ctx);

        HttpSessionStub session = new HttpSessionStub();
        session.setServletContext(ctx);

        httpServletRequest = new HttpServletRequestStub();
        httpServletRequest.setSession(session);
    }

    @Test
    public void validateReCaptcha_InvalidResponse_ReturnsFalse() throws IOException {
        // Given
        VitroRequest vitroRequest = new VitroRequest(httpServletRequest);

        // When
        boolean result = CaptchaServiceBean.validateReCaptcha("invalidResponse", vitroRequest);

        // Then
        assertFalse(result);
    }

    @Test
    public void generateChallenge_ValidChallengeGenerated() throws IOException {
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
        String remoteAddress = "sampleRemoteAddress";
        CaptchaBundle sampleChallenge = new CaptchaBundle("sampleB64Image", "sampleCode", captchaId);
        CaptchaServiceBean.getCaptchaChallenges().put(remoteAddress, sampleChallenge);

        // When
        Map<String, CaptchaBundle> captchaChallenges = CaptchaServiceBean.getCaptchaChallenges();
        Optional<CaptchaBundle> result = CaptchaServiceBean.getChallenge(captchaId, remoteAddress);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sampleChallenge, result.get());
        assertTrue(captchaChallenges.isEmpty()); // Ensure the challenge is removed from the map
    }

    @Test
    public void getChallenge_NonMatchingCaptchaIdAndRemoteAddress_ReturnsEmptyOptional() {
        // When
        Map<String, CaptchaBundle> captchaChallenges = CaptchaServiceBean.getCaptchaChallenges();
        Optional<CaptchaBundle> result = CaptchaServiceBean.getChallenge("nonMatchingId", "nonMatchingAddress");

        // Then
        assertFalse(result.isPresent());
        assertTrue(captchaChallenges.isEmpty()); // Ensure the map remains empty
    }
}
