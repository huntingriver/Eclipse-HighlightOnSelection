package com.nexiwave.tools.eclipse.highlightonselection.preferences;

import java.util.HashSet;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;

import com.nexiwave.tools.eclipse.highlightonselection.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_ENABLE, true);
		store.setDefault(PreferenceConstants.P_CASE_SENSITIVE, true);
		store.setDefault(PreferenceConstants.P_WORDS_ONLY, true);
		store.setDefault(PreferenceConstants.P_KEEP_HIGHLIGHTS, true);

		store.setDefault(PreferenceConstants.P_BACKGROUND_COLOR, "115,210,22");
		
		store.setDefault(PreferenceConstants.getConfigForExt("*"), true);
		store.setDefault(PreferenceConstants.getConfigForExt("txt"), true);
		for (IFileEditorMapping m : getAttachableContentTypes()) {
			store.setDefault(PreferenceConstants.getConfigForExt(m.getExtension()), true);
		}
	}

	public static HashSet<IFileEditorMapping> getAttachableContentTypes() {
		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		IFileEditorMapping[] mm = editorReg.getFileEditorMappings();
		HashSet<IFileEditorMapping> mappings = new HashSet<IFileEditorMapping>();
		for (IFileEditorMapping m : mm) {
			
			if (m.getExtension().contains(" "))
				continue;
			
			boolean hasTextEditor = false;
			for (IEditorDescriptor ied: m.getEditors()) {
				if (ied.isOpenExternal() || ied.isOpenInPlace())
					continue;
				
				if (ied.isInternal()) {
					hasTextEditor = true;
					break;
				}
			}
			
			if (hasTextEditor) {
				mappings.add(m);
			}
		}

		return mappings;
	}
}
