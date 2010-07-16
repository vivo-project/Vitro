/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

public class FieldHelp {

    private String description = null;
    private String descriptionUri = null;
    private String examples = null;
    private String examplesUri = null;

    private String helpUri = null;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionUri() {
        return descriptionUri;
    }

    public void setDescriptionUri(String descriptionUri) {
        this.descriptionUri = descriptionUri;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public String getExamplesUri() {
        return examplesUri;
    }

    public void setExamplesUri(String examplesUri) {
        this.examplesUri = examplesUri;
    }

    public String getHelpUri() {
        return helpUri;
    }

    public void setHelpUri(String helpUri) {
        this.helpUri = helpUri;
    }

}
