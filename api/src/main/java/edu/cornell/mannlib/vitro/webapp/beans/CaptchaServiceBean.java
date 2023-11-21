package edu.cornell.mannlib.vitro.webapp.beans;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
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

public class CaptchaServiceBean {

    private static final SecureRandom random = new SecureRandom();

    private static final Log log = LogFactory.getLog(CaptchaServiceBean.class.getName());

    private static final ConcurrentHashMap<String, CaptchaBundle> captchaChallenges = new ConcurrentHashMap<>();


    public static boolean validateReCaptcha(String recaptchaResponse, VitroRequest vreq) {
        String secretKey =
            Objects.requireNonNull(ConfigurationProperties.getBean(vreq).getProperty("recaptcha.secretKey"),
                "You have to provide a secret key through configuration file.");
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

    public static Optional<CaptchaBundle> getChallenge(String captchaId, String remoteAddress) {
        CaptchaBundle challengeForHost = captchaChallenges.getOrDefault(remoteAddress, new CaptchaBundle("", "", ""));
        captchaChallenges.remove(remoteAddress);

        if (!challengeForHost.getCaptchaId().equals(captchaId)) {
            return Optional.empty();
        }

        return Optional.of(challengeForHost);
    }

    public static ConcurrentHashMap<String, CaptchaBundle> getCaptchaChallenges() {
        return captchaChallenges;
    }

    private static String convertToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private static Color getRandomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new Color(r, g, b);
    }
}
