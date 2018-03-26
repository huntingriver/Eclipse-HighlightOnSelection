package com.nexiwave.tools.eclipse.highlightonselection;

import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		// just to make sure the plugin is loaded.
		Activator.getDefault();
	}

}
