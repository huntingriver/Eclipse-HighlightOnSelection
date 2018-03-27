package com.nexiwave.tools.eclipse.highlightonselection.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {


	public static final String P_ENABLE = "ENABLED";

	public static final String P_CASE_SENSITIVE = "CASE_SENSITIVE";
	
	public static final String P_KEEP_HIGHLIGHTS = "KEEP_HIGHLIGHTS";

	public static final String P_WORDS_ONLY = "WORDS_ONLY";

	public static final String P_BACKGROUND_COLOR = "BACKGROUND_COLOR";
	
	public static final String P_MAX_SIZE_IN_MB = "MAX_SIZE_IN_MB";
	
	private static final String P_ENABLED_FOR_EDITOR = "ENABLED_FOR_EDITOR";
	
	public static final String getConfigForExt(String ext) {
		return P_ENABLED_FOR_EDITOR + "-" + ext;
	}
}
