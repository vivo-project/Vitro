package edu.cornell.mannlib.vitro.webapp.web.templatemodels.preprocessors;

import java.util.List;
import java.util.Map;

public abstract class BaseObjectPropertyDataPreprocessor implements
        ObjectPropertyDataPreprocessor {

    @Override
    public abstract void preprocess(List<Map<String, String>> data);


}
