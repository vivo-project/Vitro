/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.matcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.mockito.ArgumentMatcher;

public class APIResponseMatcher implements ArgumentMatcher<String> {

    private Boolean json;

    private String path;

    private boolean update = false;

    public APIResponseMatcher(Boolean json, String path) {
        this.json = json;
        this.path = path;
    }

    @Override
    public boolean matches(String actualString) {
        String expectedJson = readJson(path);
        OpenAPI expected = readSpec(true, expectedJson);
        OpenAPI actual = readSpec(json, actualString);
        boolean isEquals = actual.equals(expected);
        if (update && !isEquals) {
            updateFile(actualString);
        }
        return isEquals;
    }

    private void updateFile(String actualString) {
        File file;
        try {
            file = getJson();
            try (FileWriter writer = new FileWriter(file);) {
                writer.write(actualString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
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
