package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Version;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Versionable;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Versioned;

public abstract class VersionableAbstractPool<K extends Versioned, C extends Versionable<K>, P extends Pool<K, C>>
        extends AbstractPool<K, C, P> {

    public List<C> getComponents(Version version) {
        return getComponents()
                .values()
                .stream()
                .filter(component -> isInRange(component, version))
                .collect(Collectors.toList());
    }

    public C get(K key) {
        C component = null;
        Entry<K, C> entry = getComponents().floorEntry(key);
        if (entry != null) {
            component = entry.getValue();
            String keyName = key.getName();
            Version keyVersion = key.getVersion();
            if (!matchesName(component, keyName) || !isInRange(component, keyVersion)) {
                component = null;
            }
        }

        if (component == null) {
            return getDefault();
        }

        component.addClient();
        return component;
    }

    private boolean matchesName(C component, String keyName) {
        // ensure key name matches component key name
        return keyName.equals(component.getKey().getName());
    }

    private boolean isInRange(C component, Version keyVersion) {
        boolean hasVersionMax = component.getVersionMax() != null;
        boolean hasVersionMin = component.getVersionMin() != null;

        if (hasVersionMax) {
            // ensure key version is not greater than component version max
            if (greaterThanVersionMax(component, keyVersion)) {
                return false;
            }
        } else if (hasVersionMin) {
            // ensure key version specific values are respected
            if (mismatchSpecificVersion(component, keyVersion)) {
                return false;
            }
        }

        return true;
    }

    private boolean greaterThanVersionMax(C component, Version keyVersion) {
        Version componentVersionMax = Version.of(component.getVersionMax());
        boolean majorVersionGreater = keyVersion.getMajor().compareTo(componentVersionMax.getMajor()) > 0;

        boolean minorVersionGreater = minorVersionSpecific(keyVersion)
                && keyVersion.getMinor().compareTo(componentVersionMax.getMinor()) > 0;

        boolean patchVersionGreater = patchVersionSpecific(keyVersion)
                && keyVersion.getPatch().compareTo(componentVersionMax.getPatch()) > 0;

        return majorVersionGreater || minorVersionGreater || patchVersionGreater;
    }

    private boolean mismatchSpecificVersion(C component, Version keyVersion) {
        Version componentVersionMin = Version.of(component.getVersionMin());

        boolean mismatchMinorVersion = minorVersionSpecific(keyVersion)
                && !keyVersion.getMinor().equals(componentVersionMin.getMinor());

        boolean mismatchPatchVersion = patchVersionSpecific(keyVersion)
                && !keyVersion.getPatch().equals(componentVersionMin.getPatch());

        return mismatchMinorVersion || mismatchPatchVersion;
    }

    private boolean minorVersionSpecific(Version keyVersion) {
        return !keyVersion.getMinor().equals(Integer.MAX_VALUE);
    }

    private boolean patchVersionSpecific(Version keyVersion) {
        return !keyVersion.getPatch().equals(Integer.MAX_VALUE);
    }

}
