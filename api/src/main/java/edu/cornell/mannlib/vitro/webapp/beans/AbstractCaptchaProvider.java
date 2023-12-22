package edu.cornell.mannlib.vitro.webapp.beans;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractCaptchaProvider {

    protected static final Log log = LogFactory.getLog(AbstractCaptchaProvider.class.getName());

    abstract CaptchaBundle generateRefreshChallenge() throws IOException;

    abstract void addCaptchaRelatedFieldsToPageContext(Map<String, Object> context) throws IOException;

    boolean validateCaptcha(String captchaInput, String challengeId) {
        return false;
    }
}
