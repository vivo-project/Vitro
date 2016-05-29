package edu.ucsf.vitro.opensocial;

public class GadgetViewRequirements {
	private char viewerReq; // U for User or null for no requirement
	private char ownerReq; // R for Registered or null for no requirement
	private String view;
	private String chromeId;
	private String optParams;
	private int display_order;

	public GadgetViewRequirements(char viewerReq, char ownerReq,
			String view, String chromeId, String optParams, int display_order) {
		this.viewerReq = viewerReq;
		this.ownerReq = ownerReq;
		this.view = view;
		this.chromeId = chromeId;
		this.optParams = optParams;
		this.display_order = display_order;
	}

	public GadgetViewRequirements(String page, String viewerReq,
			String ownerReq, String view, String chromeId, String optParams, int display_order) {
		this(viewerReq != null ? viewerReq.charAt(0) : ' ',
				ownerReq != null ? ownerReq.charAt(0) : ' ', view, chromeId, optParams, display_order);
	}

	public char getViewerReq() {
		return viewerReq;
	}

	public char getOwnerReq() {
		return ownerReq;
	}

	public String getView() {
		return view;
	}

	public String getChromeId() {
		return chromeId;
	}

	public String getOptParams() {
		return optParams != null && optParams.trim().length() > 0 ? optParams : "{}";
	}

	int getDisplayOrder() {
		return display_order;
	}
}
