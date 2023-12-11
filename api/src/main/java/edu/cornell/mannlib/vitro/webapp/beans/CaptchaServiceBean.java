/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.backgrounds.GradiatedBackgroundProducer;
import net.logicsquad.nanocaptcha.image.filter.FishEyeImageFilter;
import net.logicsquad.nanocaptcha.image.filter.StretchImageFilter;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.StraightLineNoiseProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
     * @param secretKey         The secret key used for Google ReCaptcha validation.
     * @return True if the reCAPTCHA response is valid, false otherwise.
     */
    public static boolean validateReCaptcha(String recaptchaResponse, String secretKey) {
        String verificationUrl =
            "https://www.google.com/recaptcha/api/siteverify?secret=" + secretKey + "&response=" + recaptchaResponse;

        try {
            HttpClient httpClient = HttpClients.createDefault();
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
        ImageCaptcha imageCaptcha =
            new ImageCaptcha.Builder(200, 75)
                .addContent(random.nextInt(2) + 5)
                .addBackground(new GradiatedBackgroundProducer())
                .addNoise(new CurvedLineNoiseProducer(getRandomColor(), 2f))
                .addNoise(new StraightLineNoiseProducer(getRandomColor(), 2))
                .addFilter(new StretchImageFilter())
                .addFilter(new FishEyeImageFilter())
                .addBorder()
                .build();
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
}
