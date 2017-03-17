package org.vivoweb.tools;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class JenaCli {
    public static void main (String[] arg) {
        Options options = parseArguments(arg);
        if (options == null) {
            System.err.println("Incorrect arguments supplied.");
            System.err.println("");
            System.err.println("Export: java -jar jena3tools.jar -e -d <home dir>");
            System.err.println("Import: java -jar jena3tools.jar -i -d <home dir>");
            System.exit(1);
        }

        if (!isValidHomeDir(options.homeDir)) {
            System.err.println("Can't find a valid home dir at " + options.homeDir);
            System.exit(1);
        }

        if (options.exportMode) {
            Exporter exporter = new Exporter(options.homeDir);
        } else if (options.importMode) {
            Importer importer = new Importer(options.homeDir);

        }

        System.exit(0);
    }

    private static Options parseArguments(String[] arg) {
        Options options = new Options();

        if (arg != null) {
            for (int i = 0; i < arg.length; i++) {
                if ("-d".equalsIgnoreCase(arg[i]) ||
                    "--dir".equalsIgnoreCase(arg[i])
                   ) {
                    if (i < arg.length - 1) {
                        i++;
                        options.homeDir = arg[i];
                    }
                }

                if ("-e".equalsIgnoreCase(arg[i]) ||
                    "--export".equalsIgnoreCase(arg[i])
                   ) {
                    options.exportMode = true;
                }

                if ("-i".equalsIgnoreCase(arg[i]) ||
                    "--import".equalsIgnoreCase(arg[i])
                   ) {
                    options.importMode = true;
                }
            }
        }

        if (options.isValid()) {
            return options;
        }

        return null;
    }

    private static boolean isValidHomeDir(String homeDir) {
        File homeDirFile = new File(homeDir);
        if (!homeDirFile.isDirectory()) {
            return false;
        }

        if (!homeDirFile.canRead() || !homeDirFile.canWrite()) {
            return false;
        }

        boolean hasConfigDir = false;
        boolean hasRuntimeProperties = false;
        for (File child : homeDirFile.listFiles()) {
            if ("config".equals(child.getName())) {
                for (File configs : child.listFiles()) {
                    if ("applicationSetup.n3".equals(configs.getName())) {
                        hasConfigDir = true;
                    }
                }

            }

            if ("runtime.properties".equals(child.getName())) {
                hasRuntimeProperties = true;
            }
        }

        if (!hasConfigDir || !hasRuntimeProperties) {
            return false;
        }

        return true;
    }

    private static class Options {
        public String homeDir = null;
        public boolean importMode = false;
        public boolean exportMode = false;

        private boolean isValid() {
            if (StringUtils.isEmpty(homeDir)) {
                return false;
            }

            if (importMode == exportMode) {
                return false;
            }

            return true;
        }
    }
}
