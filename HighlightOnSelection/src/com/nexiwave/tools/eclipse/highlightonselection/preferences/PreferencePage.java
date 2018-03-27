package com.nexiwave.tools.eclipse.highlightonselection.preferences;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nexiwave.tools.eclipse.highlightonselection.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	private HashSet<String> knownExts = new HashSet<String>();

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Config");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(
				new BooleanFieldEditor(
					PreferenceConstants.P_ENABLE,
					"Enable",
					getFieldEditorParent()));
		addField(
				new BooleanFieldEditor(
					PreferenceConstants.P_CASE_SENSITIVE,
					"Case Sensitive",
					getFieldEditorParent()));
		addField(
				new BooleanFieldEditor(
					PreferenceConstants.P_WORDS_ONLY,
					"Words Only",
					getFieldEditorParent()));
		addField(
				new BooleanFieldEditor(
					PreferenceConstants.P_KEEP_HIGHLIGHTS,
					"Keep Highlights",
					getFieldEditorParent()));
		addField(
				new IntegerFieldEditor(
					PreferenceConstants.P_MAX_SIZE_IN_MB,
					"Max File Size (in MB, 0 for unlimited)",
					getFieldEditorParent(), 4));
		addField(
				new ColorFieldEditor(
					PreferenceConstants.P_BACKGROUND_COLOR,
					"Color",
					getFieldEditorParent()));

		final Group group = new Group(getFieldEditorParent(), SWT.NONE);
		group.setText("Apply to file types:");
		
		GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 1;
		group.setLayout(gridLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Composite composite = new Composite(group, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		TreeSet<IFileEditorMapping> ts = new TreeSet<IFileEditorMapping>(new Comparator<IFileEditorMapping>() {

			@Override
			public int compare(IFileEditorMapping o1, IFileEditorMapping o2) {
				return o1.getExtension().compareTo(o2.getExtension());
			}
		});
		ts.addAll(PreferenceInitializer.getAttachableContentTypes());
		for (IFileEditorMapping m : ts) {
			addField4Ext(m.getExtension(), m.getLabel(), composite);
		}		

		addField4Ext("txt", "text files", composite);
		addField4Ext("*", "all other editors", composite);
	}
	
	private void addField4Ext(String ext, String label, Composite composite) {
		if (knownExts.contains(ext))
			return;
		
		knownExts.add(ext);
		
		BooleanFieldEditor e = new BooleanFieldEditor(
				PreferenceConstants.getConfigForExt("*"),
				"." + ext, composite);
		addField(e);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}