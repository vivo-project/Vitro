package edu.ucsf.vitro.opensocial;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PreparedGadget implements Comparable<PreparedGadget> {
	private GadgetSpec gadgetSpec;
	private OpenSocialManager helper;
	private int moduleId;
	private String securityToken;

	public PreparedGadget(GadgetSpec gadgetSpec, OpenSocialManager helper,
			int moduleId, String securityToken) {
		this.gadgetSpec = gadgetSpec;
		this.helper = helper;
		this.moduleId = moduleId;
		this.securityToken = securityToken;
	}

	public int compareTo(PreparedGadget other) {
		GadgetViewRequirements gvr1 = this.getGadgetViewRequirements();
		GadgetViewRequirements gvr2 = other.getGadgetViewRequirements();
		return ("" + this.getView() + (gvr1 != null ? gvr1.getDisplayOrder()
				: Integer.MAX_VALUE)).compareTo("" + other.getView()
				+ (gvr2 != null ? gvr2.getDisplayOrder() : Integer.MAX_VALUE));
	}

	public GadgetSpec getGadgetSpec() {
		return gadgetSpec;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public int getAppId() {
		return gadgetSpec.getAppId();
	}

	public String getName() {
		return gadgetSpec.getName();
	}

	public int getModuleId() {
		return moduleId;
	}

	public String getGadgetURL() {
		return gadgetSpec.getGadgetURL();
	}

	GadgetViewRequirements getGadgetViewRequirements() {
		return gadgetSpec.getGadgetViewRequirements(helper.getPageName());
	}

	public String getView() {
		GadgetViewRequirements reqs = getGadgetViewRequirements();
		if (reqs != null) {
			return reqs.getView();
		}
		// default behavior that will get invoked when there is no reqs. Useful
		// for sandbox gadgets
		else if (helper.getPageName().equals("individual-EDIT-MODE")) {
			return "home";
		} else if (helper.getPageName().equals("individual")) {
			return "profile";
		} else if (helper.getPageName().equals("gadgetDetails")) {
			return "canvas";
		} else if (gadgetSpec.getGadgetURL().contains("Tool")) {
			return "small";
		} else {
			return null;
		}
	}

	public String getChromeId() {
		GadgetViewRequirements reqs = getGadgetViewRequirements();
		if (reqs != null) {
			return reqs.getChromeId();
		}
		// default behavior that will get invoked when there is no reqs. Useful
		// for sandbox gadgets
		else if (gadgetSpec.getGadgetURL().contains("Tool")) {
			return "gadgets-tools";
		} else if (helper.getPageName().equals("individual-EDIT-MODE")) {
			return "gadgets-edit";
		} else if (helper.getPageName().equals("individual")) {
			return "gadgets-view";
		} else if (helper.getPageName().equals("gadgetDetails")) {
			return "gadgets-detail";
		} else if (helper.getPageName().equals("search")) {
			return "gadgets-search";
		} else {
			return null;
		}
	}

	public String getOptParams() {
		GadgetViewRequirements reqs = getGadgetViewRequirements();
		return reqs != null ? reqs.getOptParams() : "{}";
	}

	public String getCanvasURL() throws UnsupportedEncodingException {
		return "~/gadget?appId=" + getAppId() + "&Person="
				+ URLEncoder.encode(helper.getOwnerId(), "UTF-8");
	}

	public String toString() {
		return "" + this.moduleId + ", (" + this.gadgetSpec.toString() + ")";
	}
}
