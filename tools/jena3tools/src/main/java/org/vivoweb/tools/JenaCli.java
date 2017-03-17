package org.vivoweb.tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

public class JenaCli {
    static {
        Logger.getRootLogger().setLevel(Level.OFF);
    }

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

        ApplicationStores applicationStores = new ApplicationStores(options.homeDir);
        try {
            File dumpDir = Utils.resolveFile(options.homeDir, "dumps");
            if (dumpDir.exists()) {
                if (!dumpDir.isDirectory()) {
                    System.err.println("Home directory contains 'dumps', which is not a directory");
                    System.exit(1);
                }
            } else {
                if (!dumpDir.mkdirs()) {
                    System.err.println("Unable to create 'dumps' directory");
                    System.exit(1);
                }
            }

            File contentDump = Utils.resolveFile(options.homeDir, "dumps/content.nq");
            File configurationDump = Utils.resolveFile(options.homeDir, "dumps/configuration.nq");

            if (options.exportMode) {
                if (!options.force) {
                    if (contentDump.exists() || configurationDump.exists()) {
                        System.err.println("Dumps directory contains previous export");
                        System.exit(1);
                    }
                }

                System.out.println("Writing Configuration");
                applicationStores.writeConfiguration(configurationDump);

                System.out.println("Writing Content");
                applicationStores.writeContent(contentDump);

                System.out.println("Export complete");
            } else if (options.importMode) {
                if (!applicationStores.isEmpty()) {
                    System.err.println("Triple store(s) contain existing values");
                    System.exit(1);
                }

                if (!applicationStores.validateFiles(configurationDump, contentDump)) {
                    System.err.println("Dump files not present");
                    System.exit(1);
                }

                System.out.println("Reading Configuration");
                applicationStores.readConfiguration(configurationDump);

                System.out.println("Reading Content");
                applicationStores.readContent(contentDump);

                System.out.println("Import complete");
            }

            System.exit(0);
        } finally {
            applicationStores.close();
        }
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

                if ("-f".equalsIgnoreCase(arg[i]) ||
                    "--force".equalsIgnoreCase(arg[i])
                   ) {
                    options.force = true;
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
        public boolean force = false;

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
