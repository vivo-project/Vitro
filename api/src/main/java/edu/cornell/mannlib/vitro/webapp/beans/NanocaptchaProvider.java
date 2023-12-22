package edu.cornell.mannlib.vitro.webapp.beans;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.backgrounds.GradiatedBackgroundProducer;
import net.logicsquad.nanocaptcha.image.filter.FishEyeImageFilter;
import net.logicsquad.nanocaptcha.image.filter.StretchImageFilter;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.StraightLineNoiseProducer;

public class NanocaptchaProvider extends AbstractCaptchaProvider {

    private final SecureRandom random = new SecureRandom();

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

    @Override
    public CaptchaBundle generateRefreshChallenge() throws IOException {
        return generateChallenge();
    }

    @Override
    public void addCaptchaRelatedFieldsToPageContext(Map<String, Object> context) throws IOException {
        CaptchaBundle captchaChallenge = generateChallenge();
        CaptchaServiceBean.getCaptchaChallenges().put(captchaChallenge.getCaptchaId(), captchaChallenge);

        context.put("challenge", captchaChallenge.getB64Image());
        context.put("challengeId", captchaChallenge.getCaptchaId());
    }

    @Override
    public boolean validateCaptcha(String captchaInput, String challengeId) {
        Optional<CaptchaBundle> optionalChallenge = CaptchaServiceBean.getChallenge(challengeId);
        return optionalChallenge.isPresent() && optionalChallenge.get().getCode().equals(captchaInput);
    }

    private CaptchaBundle generateChallenge() throws IOException {
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
     * Retrieves the configured difficulty level for generating captchas.
     * If the difficulty level is not specified or is not HARD, the default difficulty is set to EASY.
     *
     * @return The difficulty level for captcha generation (EASY or HARD).
     */
    private CaptchaDifficulty getCaptchaDifficulty() {
        String difficulty = ConfigurationProperties.getInstance().getProperty("nanocaptcha.difficulty");
        try {
            return CaptchaDifficulty.valueOf(difficulty.toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            return CaptchaDifficulty.EASY;
        }
    }

    /**
     * Generates a random Color object.
     *
     * @return A randomly generated Color object.
     */
    private Color getRandomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new Color(r, g, b);
    }
}
