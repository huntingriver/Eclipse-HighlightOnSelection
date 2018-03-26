package com.nexiwave.tools.eclipse.highlightonselection;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nexiwave.tools.eclipse.highlightonselection.preferences.PreferenceConstants;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "HighlightOnSelection"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private TextSelectionListener selectionListener;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	private void injectListener() {
		if (selectionListener == null)
			selectionListener = new TextSelectionListener();

		IPreferenceStore store = getPreferenceStore();
		if (store.getBoolean(PreferenceConstants.P_ENABLE)) {

			final IWorkbench workbench = PlatformUI.getWorkbench();
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					if (window != null) {
						window.getSelectionService().addPostSelectionListener(selectionListener);
					}
				}
			});
		}
		store.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (PreferenceConstants.P_ENABLE.equals(event.getProperty())) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window != null) {
						if (getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE)) {
							window.getSelectionService().addPostSelectionListener(selectionListener);
						} else {
							window.getSelectionService().removePostSelectionListener(selectionListener);
						}
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		injectListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative
	 * path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
