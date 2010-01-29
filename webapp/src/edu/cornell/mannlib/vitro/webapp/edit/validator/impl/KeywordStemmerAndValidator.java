package edu.cornell.mannlib.vitro.webapp.edit.validator.impl;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Iterator;
import java.util.List;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.validator.ValidationObject;
import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordDao;
import edu.cornell.mannlib.vitro.webapp.edit.listener.impl.KeywordPreProcessor;
import edu.cornell.mannlib.vitro.webapp.utils.Stemmer;

public class KeywordStemmerAndValidator implements Validator {

    private int MAX_LENGTH = 32;

    private EditProcessObject epo = null;
    private KeywordDao kDao = null;

    public KeywordStemmerAndValidator(EditProcessObject in, KeywordDao keywordDao) {
        this.epo = in;
        this.kDao = keywordDao;
    }

    public ValidationObject validate (Object obj) throws IllegalArgumentException {

        ValidationObject vo = new ValidationObject();

        if (obj==null || (obj instanceof String && ((String)obj).length()>0)) {
            String keywordString = (String) obj;
            String stemmedString = Stemmer.StemString(keywordString, MAX_LENGTH);
            epo.getPreProcessorList().add(new KeywordPreProcessor(stemmedString));
            List<Keyword> matches = kDao.getKeywordsByStem(stemmedString);
            if (matches.size() > 0) {
                vo.setValid(false);
                String suggestionsStr = new String();
                Iterator<Keyword> matchesIt = matches.iterator();
                boolean first = true;
                while (matchesIt.hasNext()) {
                    Keyword k = matchesIt.next();
                    if (!matchesIt.hasNext() && !first) {
                        suggestionsStr += "or ";
                    }
                    suggestionsStr += "'"+k.getTerm();
                    if (matchesIt.hasNext()) {
                        suggestionsStr += ",' ";
                    } else {
                        suggestionsStr += "'";
                    }
                    first = false;
                }
                vo.setMessage("Keyword identified as redundant.  Please modify term or consider canceling and instead linking individual to existing keyword "+suggestionsStr);
            } else {
                vo.setValid(true);
            }
        } else {
            vo.setValid(false);
            vo.setMessage("Please enter a keyword.");
        }
        vo.setValidatedObject(obj);

        return vo;

    }

}