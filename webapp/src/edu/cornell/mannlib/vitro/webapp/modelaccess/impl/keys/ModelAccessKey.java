/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ModelAccessOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;

/**
 * An immutable collection of options that can be used as a key in a hashmap.
 * 
 * The class of the key is part of the hash, so a OntModelKey and a
 * RDFServiceKey can both be used in the same map without conflict.
 */
public abstract class ModelAccessKey {
	
	// ----------------------------------------------------------------------
	// Static methods
	// ----------------------------------------------------------------------

	protected static WhichService findWhichService(ModelAccessOption... options) {
		return findWhichService(Arrays.asList(options));
	}

	protected static WhichService findWhichService(
			Iterable<ModelAccessOption> options) {
		return findOption(options, WhichService.CONTENT, WhichService.class);
	}

	protected static ReasoningOption findReasoningOption(
			ModelAccessOption... options) {
		return findReasoningOption(Arrays.asList(options));
	}

	protected static ReasoningOption findReasoningOption(
			Iterable<ModelAccessOption> options) {
		return findOption(options, ReasoningOption.ASSERTIONS_AND_INFERENCES,
				ReasoningOption.class);
	}

	protected static LanguageOption findLanguageOption(
			ModelAccessOption... options) {
		return findLanguageOption(Arrays.asList(options));
	}

	protected static LanguageOption findLanguageOption(
			Iterable<ModelAccessOption> options) {
		return findOption(options, LanguageOption.LANGUAGE_AWARE,
				LanguageOption.class);
	}

	protected static PolicyOption findPolicyOption(ModelAccessOption[] options) {
		return findPolicyOption(Arrays.asList(options));
	}

	protected static PolicyOption findPolicyOption(
			Iterable<ModelAccessOption> options) {
		return findOption(options, PolicyOption.POLICY_AWARE,
				PolicyOption.class);
	}

	/**
	 * Search through the options for values from the specified class. If none
	 * are found, use the default value.
	 * 
	 * Redundant options are silently accepted, but conflicting options will
	 * throw an exception.
	 */
	private static <T> T findOption(Iterable<ModelAccessOption> options,
			T defaultValue, Class<T> clazz) {
		T found = null;
		for (ModelAccessOption option : options) {
			if (found == option) {
				continue;
			}
			if (!clazz.isInstance(option)) {
				continue;
			}
			if (found == null) {
				found = clazz.cast(option);
				continue;
			}
			throw new IllegalArgumentException("Conflicting options: " + found
					+ " and " + option);
		}

		if (found == null) {
			return defaultValue;
		} else {
			return found;
		}
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	protected final List<ModelAccessOption> keyComponents;
	private final int hashCode;

	protected ModelAccessKey(ModelAccessOption... options) {
		this.keyComponents = Collections.unmodifiableList(new ArrayList<>(
				Arrays.asList(options)));
		this.hashCode = keyComponents.hashCode() ^ this.getClass().hashCode();
	}

	protected ReasoningOption getReasoningOption() {
		return findReasoningOption(keyComponents);
	}

	protected LanguageOption getLanguageOption() {
		return findLanguageOption(keyComponents);
	}

	protected WhichService getWhichService() {
		return findWhichService(keyComponents);
	}

	protected PolicyOption getPolicyOption() {
		return findPolicyOption(keyComponents);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		ModelAccessKey that = (ModelAccessKey) obj;
		return this.keyComponents.equals(that.keyComponents);
	}

	@Override
	public String toString() {
		List<ModelAccessOption> notDefaults = new ArrayList<>();
		for (ModelAccessOption option : keyComponents) {
			if (!option.isDefault()) {
				notDefaults.add(option);
			}
		}
		return this.getClass().getSimpleName() + notDefaults;
	}

}
