package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Map;

public class DummyCaptchaProvider extends AbstractCaptchaProvider {

    @Override
    CaptchaBundle generateRefreshChallenge() {
        return new CaptchaBundle("", "", ""); // No refresh challenges if there is no implementation
    }

    @Override
    void addCaptchaRelatedFieldsToPageContext(Map<String, Object> context) {
        // No added fields necessary if there is no implementation
    }

    @Override
    boolean validateCaptcha(String captchaInput, String challengeId) {
        return true; // validation always passes
    }
}
