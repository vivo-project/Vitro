package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Objects;

/**
 * Represents a bundle containing a CAPTCHA image in Base64 format, the associated code,
 * and a unique challenge identifier.
 *
 * @author Ivan Mrsulja
 * @version 1.0
 */
public class CaptchaBundle {

    private final String b64Image;

    private final String code;

    private final String challengeId;


    public CaptchaBundle(String b64Image, String code, String challengeId) {
        this.b64Image = b64Image;
        this.code = code;
        this.challengeId = challengeId;
    }

    public String getB64Image() {
        return b64Image;
    }

    public String getCode() {
        return code;
    }

    public String getCaptchaId() {
        return challengeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CaptchaBundle that = (CaptchaBundle) o;
        return Objects.equals(code, that.code) && Objects.equals(challengeId, that.challengeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, challengeId);
    }
}
