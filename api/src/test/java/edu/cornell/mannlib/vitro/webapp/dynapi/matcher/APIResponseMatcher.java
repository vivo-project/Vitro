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
        OpenAPI expected = readSpec(readFile(path));
        OpenAPI actual = readSpec(argument);

        return actual.equals(expected);
    }

    private OpenAPI readSpec(String spec) {
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

    private String readFile(String path) {
        try {
            return new String(Files.readAllBytes(getFile().toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File getFile() throws IOException {
        return new File("src/test/resources/dynapi/mock/docs/response/" + path);
    }

}
