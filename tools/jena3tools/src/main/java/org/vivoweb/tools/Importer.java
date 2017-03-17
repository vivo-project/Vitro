package org.vivoweb.tools;

public class Importer {
    final private String homeDir;
    final private ApplicationSetup applicationSetup;

    private boolean importContent = false;
    private boolean importConfiguration = false;

    public Importer(String pHomeDir) {
        applicationSetup = new ApplicationSetup(pHomeDir);
        homeDir = pHomeDir;
    }


}
