/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.backgrounds.GradiatedBackgroundProducer;
import net.logicsquad.nanocaptcha.image.filter.FishEyeImageFilter;
import net.logicsquad.nanocaptcha.image.filter.StretchImageFilter;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.StraightLineNoiseProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


/**
 * This class provides services related to CAPTCHA challenges and reCAPTCHA validation.
 * It includes methods for generating CAPTCHA challenges, validating reCAPTCHA responses,
 * and managing CAPTCHA challenges for specific hosts.
 *
 * @author Ivan Mrsulja
 * @version 1.0
 */
public class CaptchaServiceBean {

    private static final SecureRandom random = new SecureRandom();

    private static final Log log = LogFactory.getLog(CaptchaServiceBean.class.getName());

    private static final Cache<String, CaptchaBundle> captchaChallenges =
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();


    /**
     * Validates a reCAPTCHA response using Google's reCAPTCHA API.
     *
     * @param recaptchaResponse The reCAPTCHA response to validate.
     * @return True if the reCAPTCHA response is valid, false otherwise.
     */
    public static boolean validateReCaptcha(String recaptchaResponse) {
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

    /**
     * Generates a new CAPTCHA challenge using the nanocaptcha library.
     *
     * @return A CaptchaBundle containing the CAPTCHA image in Base64 format, the content,
     * and a unique identifier.
     * @throws IOException If an error occurs during image conversion.
     */
    public static CaptchaBundle generateChallenge() throws IOException {
        CaptchaDifficulty difficulty = getCaptchaDifficulty();
        ImageCaptcha.Builder imageCaptchaBuilder = new ImageCaptcha.Builder(220, 85)
            .addContent(random.nextInt(2) + 5)
            .addBackground(new GradiatedBackgroundProducer())
            .addNoise(new StraightLineNoiseProducer(getRandomColor(), 2))
            .addFilter(new StretchImageFilter())
            .addBorder();

        if (difficulty.equals(CaptchaDifficulty.HARD)) {
            imageCaptchaBuilder
                .addNoise(new CurvedLineNoiseProducer(getRandomColor(), 2f))
                .addFilter(new StretchImageFilter())
                .addFilter(new FishEyeImageFilter())
                .build();
        }

        ImageCaptcha imageCaptcha = imageCaptchaBuilder.build();
        return new CaptchaBundle(convertToBase64(imageCaptcha.getImage()), imageCaptcha.getContent(),
            UUID.randomUUID().toString());
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
     * Converts a BufferedImage object to Base64 format.
     *
     * @param image The BufferedImage to convert.
     * @return The Base64-encoded string representation of the image.
     * @throws IOException If an error occurs during image conversion.
     */
    private static String convertToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
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
        if (Strings.isNullOrEmpty(captchaImplSetting)) {
            captchaImplSetting = "NANOCAPTCHA";
        }

        return CaptchaImplementation.valueOf(captchaImplSetting);
    }

    /**
     * Adds captcha-related fields to the given page context map. The specific fields added depend on the
     * configured captcha implementation.
     * <p>
     * If the captcha implementation is "RECAPTCHA," the "siteKey" field is added to the context. If the
     * implementation is "NANOCAPTCHA" or "NONE," a captcha challenge is generated, and "challenge" and
     * "challengeId" fields are added to the context. Additionally, the "captchaToUse" field is added
     * to the context, indicating the selected captcha implementation.
     *
     * @param context The page context map to which captcha-related fields are added.
     * @throws IOException If there is an IO error during captcha challenge generation.
     */
    public static void addCaptchaRelatedFieldsToPageContext(Map<String, Object> context) throws IOException {
        CaptchaImplementation captchaImpl = getCaptchaImpl();

        if (captchaImpl.equals(CaptchaImplementation.RECAPTCHAv2)) {
            context.put("siteKey",
                Objects.requireNonNull(ConfigurationProperties.getInstance().getProperty("recaptcha.siteKey"),
                    "You have to provide a site key through configuration file."));
        } else {
            CaptchaBundle captchaChallenge = generateChallenge();
            CaptchaServiceBean.getCaptchaChallenges().put(captchaChallenge.getCaptchaId(), captchaChallenge);

            context.put("challenge", captchaChallenge.getB64Image());
            context.put("challengeId", captchaChallenge.getCaptchaId());
        }

        context.put("captchaToUse", captchaImpl.name());
    }

    /**
     * Validates a user's captcha input against the stored captcha challenge.
     *
     * @param captchaInput The user's input for the captcha challenge.
     * @param challengeId  The unique identifier for the captcha challenge.
     * @return {@code true} if the captcha input is valid, {@code false} otherwise.
     */
    public static boolean validateCaptcha(String captchaInput, String challengeId) {
        CaptchaImplementation captchaImpl = getCaptchaImpl();

        switch (captchaImpl) {
            case RECAPTCHAv2:
                if (CaptchaServiceBean.validateReCaptcha(captchaInput)) {
                    return true;
                }
                break;
            case NANOCAPTCHA:
                Optional<CaptchaBundle> optionalChallenge = CaptchaServiceBean.getChallenge(challengeId);
                if (optionalChallenge.isPresent() && optionalChallenge.get().getCode().equals(captchaInput)) {
                    return true;
                }
                break;
            case NONE:
                return true;
        }

        return false;
    }

    /**
     * Generates a random Color object.
     *
     * @return A randomly generated Color object.
     */
    private static Color getRandomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new Color(r, g, b);
    }

    /**
     * Retrieves the configured difficulty level for generating captchas.
     * If the difficulty level is not specified or is not HARD, the default difficulty is set to EASY.
     *
     * @return The difficulty level for captcha generation (EASY or HARD).
     */
    private static CaptchaDifficulty getCaptchaDifficulty() {
        String difficulty = ConfigurationProperties.getInstance().getProperty("nanocaptcha.difficulty");
        try {
            return CaptchaDifficulty.valueOf(difficulty.toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            return CaptchaDifficulty.EASY;
        }
    }
}
