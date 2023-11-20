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
import java.util.UUID;

import javax.imageio.ImageIO;

import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.backgrounds.GradiatedBackgroundProducer;
import net.logicsquad.nanocaptcha.image.filter.FishEyeImageFilter;
import net.logicsquad.nanocaptcha.image.filter.StretchImageFilter;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;
import net.logicsquad.nanocaptcha.image.noise.StraightLineNoiseProducer;

public class CaptchaServiceBean {

    public static CaptchaBundle generateChallenge() throws IOException {
        ImageCaptcha imageCaptcha =
            new ImageCaptcha.Builder(200, 75)
                .addContent(5)
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

    private static String convertToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private static Color getRandomColor() {
        SecureRandom random = new SecureRandom();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new Color(r, g, b);
    }

    private static ArrayList<Color> getRandomColors(int count) {
        ArrayList<Color> randomFontList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            randomFontList.add(getRandomColor());
        }

        return randomFontList;
    }

    private static ArrayList<Font> getRandomFonts(int count) {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

        ArrayList<Font> randomFontList = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < count; i++) {
            int randomIndex = random.nextInt(fonts.length);
            randomFontList.add(fonts[randomIndex]);
        }

        return randomFontList;
    }
}
