package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static java.lang.String.format;

public class ResourceAPIKey implements Comparable<ResourceAPIKey>, Versioned {

    private final String name;

    private final Version version;

    private ResourceAPIKey(String name, String version) {
        this.name = name;
        this.version = Version.of(version);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResourceAPIKey other = (ResourceAPIKey) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ResourceAPIKey o) {
        int nameCompare = this.name.compareTo(o.name);
        if (nameCompare == 0) {
            return this.version.compareTo(o.version);
        }
        return nameCompare;
    }

    @Override
    public String toString() {
        return format("%s (%s)", name, version.toString());
    }

    public static ResourceAPIKey of(String name, String version) {
        return new ResourceAPIKey(name, version);
    }

}
