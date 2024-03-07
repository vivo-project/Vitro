package edu.cornell.mannlib.vitro.webapp.dynapi.components;

public class NullResourceAPI extends ResourceAPI {

    private static final NullResourceAPI INSTANCE = new NullResourceAPI();

    public static NullResourceAPI getInstance() {
        return INSTANCE;
    }

    private NullResourceAPI() {
    }
}
