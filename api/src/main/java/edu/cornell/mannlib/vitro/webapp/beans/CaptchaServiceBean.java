/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;


/**
 * This class provides services related to CAPTCHA challenges and validation.
 * It includes method delegates for generating challenges, validating challenge responses,
 * and managing CAPTCHA challenges for specific hosts.
 *
 * @author Ivan Mrsulja
 * @version 1.0
 */
public class CaptchaServiceBean {

    private static final Cache<String, CaptchaBundle> captchaChallenges =
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private static AbstractCaptchaProvider captchaProvider;

    static {
        CaptchaImplementation captchaImplementation = getCaptchaImpl();
        switch (captchaImplementation) {
            case RECAPTCHAV2:
                captchaProvider = new Recaptchav2Provider();
                break;
            case NANOCAPTCHA:
                captchaProvider = new NanocaptchaProvider();
                break;
            case NONE:
                captchaProvider = new DummyCaptchaProvider();
                break;
        }
    }

    /**
     * Generates a new CAPTCHA challenge (returns empty CaptchaBundle for 3rd party providers).
     *
     * @return A CaptchaBundle containing the CAPTCHA image in Base64 format, the content,
     * and a unique identifier.
     * @throws IOException If an error occurs during image conversion.
     */
    public static CaptchaBundle generateRefreshedChallenge() throws IOException {
        return captchaProvider.generateRefreshChallenge();
    }

    /**
     * Retrieves a CAPTCHA challenge for a specific host based on the provided CAPTCHA ID
     * Removes the challenge from the storage after retrieval.
     *
     * @param captchaId The CAPTCHA ID to match.
     * @return An Optional containing the CaptchaBundle if a matching challenge is found,
     * or an empty Optional otherwise.
     */
    public static Optional<CaptchaBundle> getChallenge(String captchaId) {
        CaptchaBundle challengeForHost = captchaChallenges.getIfPresent(captchaId);
        if (challengeForHost == null) {
            return Optional.empty();
        }

        captchaChallenges.invalidate(captchaId);

        return Optional.of(challengeForHost);
    }

    /**
     * Gets the map containing CAPTCHA challenges for different hosts.
     *
     * @return A ConcurrentHashMap with host addresses as keys and CaptchaBundle objects as values.
     */
    public static Cache<String, CaptchaBundle> getCaptchaChallenges() {
        return captchaChallenges;
    }

    /**
     * Retrieves the configured captcha implementation based on the application's configuration properties.
     * If captcha functionality is disabled, returns NONE. If the captcha implementation is not specified,
     * defaults to NANOCAPTCHA.
     *
     * @return The selected captcha implementation (NANOCAPTCHA, RECAPTCHAv2, or NONE).
     */
    public static CaptchaImplementation getCaptchaImpl() {
        String captchaEnabledSetting = ConfigurationProperties.getInstance().getProperty("captcha.enabled");

        if (Objects.nonNull(captchaEnabledSetting) && !Boolean.parseBoolean(captchaEnabledSetting)) {
            return CaptchaImplementation.NONE;
        }

        String captchaImplSetting =
            ConfigurationProperties.getInstance().getProperty("captcha.implementation");

        if (Strings.isNullOrEmpty(captchaImplSetting) ||
            (!captchaImplSetting.equalsIgnoreCase(CaptchaImplementation.RECAPTCHAV2.name()) &&
                !captchaImplSetting.equalsIgnoreCase(CaptchaImplementation.NANOCAPTCHA.name()))) {
            captchaImplSetting = CaptchaImplementation.NANOCAPTCHA.name();
        }

        return CaptchaImplementation.valueOf(captchaImplSetting.toUpperCase());
    }

    /**
     * Adds captcha-related fields to the given page context map. The specific fields added depend on the
     * configured captcha implementation.
     *
     * @param context The page context map to which captcha-related fields are added.
     * @throws IOException If there is an IO error during captcha challenge generation.
     */
    public static void addCaptchaRelatedFieldsToPageContext(Map<String, Object> context) throws IOException {
        CaptchaImplementation captchaImpl = getCaptchaImpl();
        context.put("captchaToUse", captchaImpl.name());
        captchaProvider.addCaptchaRelatedFieldsToPageContext(context);
    }

    /**
     * Validates a user's captcha input.
     *
     * @param captchaInput The user's input for the captcha challenge.
     * @param challengeId  The unique identifier for the challenge (if captcha is 3rd party, this param is ignored).
     * @return {@code true} if the captcha input is valid, {@code false} otherwise.
     */
    public static boolean validateCaptcha(String captchaInput, String challengeId) {
        return captchaProvider.validateCaptcha(captchaInput, challengeId);
    }
}
