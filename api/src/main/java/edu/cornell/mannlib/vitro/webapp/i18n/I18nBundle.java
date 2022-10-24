package edu.cornell.mannlib.vitro.webapp.i18n;

public interface I18nBundle {
	
	public static final String START_SEP = "\u25a4";
	public static final String END_SEP = "\u25a5";
	public static final String INT_SEP = "\u25a6";
	public String text(String key, Object... parameters);

}
