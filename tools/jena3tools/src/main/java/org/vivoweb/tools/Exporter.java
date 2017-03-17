package org.vivoweb.tools;

public class Exporter {
    final private String homeDir;
    final private ApplicationSetup applicationSetup;

    private boolean exportContent = false;
    private boolean exportConfiguration = false;

    public Exporter(String pHomeDir) {
        applicationSetup = new ApplicationSetup(pHomeDir);
        homeDir = pHomeDir;
    }


}
