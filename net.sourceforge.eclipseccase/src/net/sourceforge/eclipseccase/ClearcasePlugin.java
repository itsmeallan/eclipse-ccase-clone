package net.sourceforge.eclipseccase;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class ClearcasePlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ClearcasePlugin plugin;
	
	/**
	 * The constructor.
	 */
	public ClearcasePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ClearcasePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
}
