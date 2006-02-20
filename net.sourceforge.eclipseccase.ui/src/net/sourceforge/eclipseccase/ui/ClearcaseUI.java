/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import net.sourceforge.eclipseccase.ClearcasePlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * UI plugin for the Clearcase plugin.
 * 
 * @author Gunnar Wagenknecht
 */
public class ClearcaseUI extends AbstractUIPlugin {

	// The shared instance.
	private static ClearcaseUI plugin;

	public static final String PLUGIN_ID = "net.sourceforge.eclipseccase.ui"; //$NON-NLS-1$

	/** debug option */
	private static final String DEBUG_OPTION_DECORATION = ClearcaseUI.PLUGIN_ID + "/debug/decoration"; //$NON-NLS-1$

	/** debug option */
	private static final String DEBUG_OPTION_PLUGIN = ClearcaseUI.PLUGIN_ID + "/debug/plugin"; //$NON-NLS-1$

	/** indicates if debugging is enabled */
	public static boolean DEBUG = false;

	/**
	 * Configures debug settings.
	 */
	static void configureDebugOptions() {
		if (ClearcaseUI.getInstance().isDebugging()) {

			if (getDebugOption(DEBUG_OPTION_DECORATION)) {
				trace("debugging " + DEBUG_OPTION_DECORATION); //$NON-NLS-1$
				ClearcaseUI.DEBUG_DECORATION = true;
			}

			if (getDebugOption(DEBUG_OPTION_PLUGIN)) {
				trace("debugging " + DEBUG_OPTION_PLUGIN); //$NON-NLS-1$
				ClearcaseUI.DEBUG = true;
			}
		}
	}

	/**
	 * Returns the value of the specified debug option.
	 * 
	 * @param optionId
	 * @return <code>true</code> if the option is enabled
	 */
	static boolean getDebugOption(String optionId) {
		String option = Platform.getDebugOption(optionId);
		return option != null ? Boolean.valueOf(option).booleanValue() : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		if (DEBUG)
			trace("stop"); //$NON-NLS-1$

		PlatformUI.getWorkbench().removeWindowListener(partListener);
	}

	/**
	 * Prints out a trace message.
	 * 
	 * @param message
	 */
	public static void trace(String message) {
		System.out.println("**ClearcaseUI** " + message); //$NON-NLS-1$
	}

	/**
	 * Prints out a trace message.
	 * 
	 * @param traceId
	 * @param message
	 */
	public static void trace(String traceId, String message) {
		trace("[" + traceId + "] " + message); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * The constructor.
	 * 
	 * @param descriptor
	 */
	public ClearcaseUI() {
		super();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		configureDebugOptions();

		ClearcasePlugin.getInstance().setClearcaseModificationHandler(new ClearcaseUIModificationHandler());

		PlatformUI.getWorkbench().addWindowListener(partListener);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the default instance
	 */
	public static ClearcaseUI getInstance() {
		return plugin;
	}

	/**
	 * Returns an array of all editors that have an unsaved content. If the
	 * identical content is presented in more than one editor, only one of those
	 * editor parts is part of the result.
	 * 
	 * @return an array of all dirty editor parts.
	 */
	public static IEditorPart[] getDirtyEditors() {
		Set inputs = new HashSet();
		List result = new ArrayList(0);
		IWorkbench workbench = getInstance().getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int x = 0; x < pages.length; x++) {
				IEditorPart[] editors = pages[x].getDirtyEditors();
				for (int z = 0; z < editors.length; z++) {
					IEditorPart ep = editors[z];
					IEditorInput input = ep.getEditorInput();
					if (!inputs.contains(input)) {
						inputs.add(input);
						result.add(ep);
					}
				}
			}
		}
		return (IEditorPart[]) result.toArray(new IEditorPart[result.size()]);
	}

	/**
	 * Returns the preference value for <code>GENERAL_DEEP_DECORATIONS</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isDeepDecoration() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.GENERAL_DEEP_DECORATIONS);
	}

	/**
	 * Returns the preference value for <code>GENERAL_DEEP_NEW</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isDeepNew() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.GENERAL_DEEP_NEW);
	}

	/**
	 * Returns the preference value for <code>TEXT_VERSION_DECORATION</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isTextVersionDecoration() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION);
	}

	/**
	 * Returns the preference value for <code>TEXT_VIEW_DECORATION</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isTextViewDecoration() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION);
	}

	/**
	 * Returns the preference value for <code>TEXT_VIEW_DECORATION</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isTextPrefixDecoration() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.TEXT_PREFIX_DECORATION);
	}

	/**
	 * Returns the preference value for <code>ICON_DECORATE_NEW</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isIconNewDecoration() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.ICON_DECORATE_NEW);
	}

	/**
	 * Returns the preference value for <code>ICON_DECORATE_EDITED</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isIconEditedDecoration() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.ICON_DECORATE_EDITED);
	}

	/**
	 * Returns the preference value for <code>ICON_DECORATE_UNKNOWN</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isIconUnknownDecoration() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.ICON_DECORATE_UNKNOWN);
	}

	/**
	 * Returns the preference value for <code>ICON_DECORATE_HIJACKED</code>.
	 * 
	 * @return the preference value
	 */
	public static boolean isIconHijackedDecoration() {
		return getInstance().getPluginPreferences().getBoolean(IClearcaseUIPreferenceConstants.ICON_DECORATE_HIJACKED);
	}

	/**
	 * Returns the preference value for <code>TEXT_PREFIX_NEW</code>.
	 * 
	 * @return the preference value
	 */
	public static String getTextPrefixNew() {
		return getInstance().getPluginPreferences().getString(IClearcaseUIPreferenceConstants.TEXT_PREFIX_NEW);
	}

	/**
	 * Returns the preference value for <code>TEXT_PREFIX_DIRTY</code>.
	 * 
	 * @return the preference value
	 */
	public static String getTextPrefixDirty() {
		return getInstance().getPluginPreferences().getString(IClearcaseUIPreferenceConstants.TEXT_PREFIX_DIRTY);
	}

	/**
	 * Returns the preference value for <code>TEXT_PREFIX_UNKNOWN</code>.
	 * 
	 * @return the preference value
	 */
	public static String getTextPrefixUnknown() {
		return getInstance().getPluginPreferences().getString(IClearcaseUIPreferenceConstants.TEXT_PREFIX_UNKNOWN);
	}

	/**
	 * Returns the preference value for <code>TEXT_PREFIX_HIJACKED</code>.
	 * 
	 * @return the preference value
	 */
	public static String getTextPrefixHijacked() {
		return getInstance().getPluginPreferences().getString(IClearcaseUIPreferenceConstants.TEXT_PREFIX_HIJACKED);
	}

	/**
	 * Returns the preference value for <code>TEXT_PREFIX_EDITED</code>.
	 * 
	 * @return the preference value
	 */
	public static String getTextPrefixEdited() {
		return getInstance().getPluginPreferences().getString(IClearcaseUIPreferenceConstants.TEXT_PREFIX_EDITED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);

		// objects
		createImageDescriptor(reg, ClearcaseImages.IMG_QUESTIONABLE_OVR);
		createImageDescriptor(reg, ClearcaseImages.IMG_EDITED_OVR);
		createImageDescriptor(reg, ClearcaseImages.IMG_UNKNOWN_OVR);
		createImageDescriptor(reg, ClearcaseImages.IMG_LINK_OVR);
		createImageDescriptor(reg, ClearcaseImages.IMG_LINK_WARNING_OVR);
		createImageDescriptor(reg, ClearcaseImages.IMG_HIJACKED_OVR);
		createImageDescriptor(reg, ClearcaseImages.IMG_DYNAMIC_OVR);
		createImageDescriptor(reg, ClearcaseImages.IMG_SNAPSHOT_OVR);
		createImageDescriptor(reg, ClearcaseImages.IMG_REFRESH);
		createImageDescriptor(reg, ClearcaseImages.IMG_REFRESH_DISABLED);
	}

	private static void createImageDescriptor(ImageRegistry reg, String id) {
		ImageDescriptor desc = imageDescriptorFromPlugin(ClearcaseUI.PLUGIN_ID, ClearcaseImages.ICON_PATH + id);
		reg.put(id, null != desc ? desc : ImageDescriptor.getMissingImageDescriptor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	protected ImageRegistry createImageRegistry() {
		// to overcome SWT issues we create this inside the UI thread
		final ImageRegistry[] imageRegistries = new ImageRegistry[1];
		getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				imageRegistries[0] = new ImageRegistry(getWorkbench().getDisplay());
			}
		});
		return imageRegistries[0];
	}

	/** the listener for opened editors */
	private PartListener partListener = new PartListener();

	/** indicates if additional debug output should be printed */
	public static boolean DEBUG_DECORATION = false;

	/**
	 * Returns the workbench display
	 * 
	 * @return the workbench display
	 */
	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

	/**
	 * Shows the given errors to the user.
	 * 
	 * @param Exception
	 *            the exception containing the error
	 * @param title
	 *            the title of the error dialog
	 * @param message
	 *            the message for the error dialog
	 * @param shell
	 *            the shell to open the error dialog in
	 */
	public static void handleError(Shell shell, Exception exception, String title, String message) {
		IStatus status = null;
		boolean log = false;
		boolean dialog = false;
		Throwable t = exception;
		if (exception instanceof TeamException) {
			status = ((TeamException) exception).getStatus();
			log = false;
			dialog = true;
		} else if (exception instanceof InvocationTargetException) {
			t = ((InvocationTargetException) exception).getTargetException();
			if (t instanceof TeamException) {
				status = ((TeamException) t).getStatus();
				log = false;
				dialog = true;
			} else if (t instanceof CoreException) {
				status = ((CoreException) t).getStatus();
				log = true;
				dialog = true;
			} else if (t instanceof InterruptedException)
				return;
			else {
				status = new Status(IStatus.ERROR, PLUGIN_ID, 1, Messages.getString("TeamAction.internal"), t); //$NON-NLS-1$
				log = true;
				dialog = true;
			}
		}
		if (status == null)
			return;
		if (!status.isOK()) {
			IStatus toShow = status;
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				if (children.length == 1)
					toShow = children[0];
			}
			if (title == null)
				title = status.getMessage();
			if (message == null)
				message = status.getMessage();
			if (dialog && shell != null)
				ErrorDialog.openError(shell, title, message, toShow);
			if (log || shell == null)
				ClearcasePlugin.log(toShow.getSeverity(), message, t);
		}
	}

	/**
	 * Convenience method to get the currently active workbench page. Note that
	 * the active page may not be the one that the usr perceives as active in
	 * some situations so this method of obtaining the activae page should only
	 * be used if no other method is available.
	 * 
	 * @return the active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getInstance().getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getActivePage();
	}

}