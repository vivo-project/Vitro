package edu.cornell.mannlib.vitro.webapp.dynapi.matcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.mockito.ArgumentMatcher;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

public class APIResponseMatcher implements ArgumentMatcher<String>{

    private Boolean json;

    private String path;

    public APIResponseMatcher(Boolean json, String path) {
        this.json = json;
        this.path = path;
    }

    @Override
    public boolean matches(String argument) {
        OpenAPI expected = readSpec(true, readJson(path));
        OpenAPI actual = readSpec(json, argument);

        return actual.equals(expected);
    }

    private OpenAPI readSpec(Boolean json, String spec) {
        OpenAPI api = new OpenAPI();

        try {
            if (json) {
                api = Json.mapper().readValue(spec, OpenAPI.class);
            } else {
                api = Yaml.mapper().readValue(spec, OpenAPI.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return api;
    }

    private String readJson(String path) {
        try {
            return new String(Files.readAllBytes(getJson().toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File getJson() throws IOException {
        return new File("src/test/resources/dynapi/mock/docs/response/" + path + ".json");
    }

}
