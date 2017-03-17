package org.vivoweb.tools;

import java.io.File;

public class Utils {
    public static  File resolveFile(String baseDir, String filePath) {
        return new File(baseDir).toPath().resolve(filePath).toFile();
    }
}
