/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

public class Version implements Comparable<Version> {

    private final static String PERIOD_PATTERN = quote(".");

    private final Integer major;

    private final Integer minor;

    private final Integer patch;

    private Version(String version) {
        String[] parts = version.split(PERIOD_PATTERN);
        major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        minor = parts.length > 1 ? Integer.parseInt(parts[1]) : Integer.MAX_VALUE;
        patch = parts.length > 2 ? Integer.parseInt(parts[2]) : Integer.MAX_VALUE;
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
        if (minor == Integer.MAX_VALUE) {
            return format("%s", major);
        } else if (patch == Integer.MAX_VALUE) {
            return format("%s.%s", major, minor);
        }
        return format("%s.%s.%s", major, minor, patch);
    }

    public static Version of(String version) {
        return new Version(version);
    }

}
