package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

public class Version implements Comparable<Version> {

    private final static String PERIOD_PATTERN = quote(".");

    private final Integer major;

    private final Integer minor;

    private final Integer patch;

    private Version(String version, boolean ceiling) {
        String[] parts = version.split(PERIOD_PATTERN);
        major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        minor = ceiling ? Integer.MAX_VALUE : parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        patch = ceiling ? Integer.MAX_VALUE : parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
    }

    public Integer getMajor() {
        return major;
    }

    public Integer getMinor() {
        return minor;
    }

    public Integer getPatch() {
        return patch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((major == null) ? 0 : major.hashCode());
        result = prime * result + ((minor == null) ? 0 : minor.hashCode());
        result = prime * result + ((patch == null) ? 0 : patch.hashCode());
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
        Version other = (Version) obj;
        if (major == null) {
            if (other.major != null) {
                return false;
            }
        } else if (!major.equals(other.major)) {
            return false;
        }
        if (minor == null) {
            if (other.minor != null) {
                return false;
            }
        } else if (!minor.equals(other.minor)) {
            return false;
        }
        if (patch == null) {
            if (other.patch != null) {
                return false;
            }
        } else if (!patch.equals(other.patch)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Version o) {
        int majorCompare = this.major.compareTo(o.major);
        if (majorCompare == 0) {
            int minorCompare = this.minor.compareTo(o.minor);
            if (minorCompare == 0) {
                return this.patch.compareTo(o.patch);
            }
            return minorCompare;
        }
        return majorCompare;
    }

    @Override
    public String toString() {
        return format("%s.%s.%s", major, minor, patch);
    }

    public static Version exact(String version) {
        return new Version(version, false);
    }

    public static Version ceiling(String version) {
        return new Version(version, true);
    }

}
