
package net.sourceforge.eclipseccase.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

/**
 * UI plugin for the Clearcase plugin.
 * 
 * @author Gunnar Wagenknecht
 */
public class ClearcaseUI extends AbstractUIPlugin
{

    //The shared instance.
    private static ClearcaseUI plugin;

    //Resource bundle.
    private ResourceBundle resourceBundle;

    public static final String PLUGIN_ID = "net.sourceforge.eclipseccase.ui";

    /**
     * The constructor.
     * 
     * @param descriptor
     */
    public ClearcaseUI()
    {
        super();
        plugin = this;
        try
        {
            resourceBundle = ResourceBundle
                    .getBundle("net.sourceforge.eclipseccase.ui.ClearcaseUIResources");
        }
        catch (MissingResourceException x)
        {
            resourceBundle = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        ClearcaseImages.initializeImages();
    }

    /**
     * Returns the shared instance.
     * 
     * @return the default instance
     */
    public static ClearcaseUI getInstance()
    {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     * 
     * @param key
     * @return the string
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = ClearcaseUI.getInstance().getResourceBundle();
        try
        {
            return (bundle != null) ? bundle.getString(key) : key;
        }
        catch (MissingResourceException e)
        {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     * 
     * @return the resource bundle
     */
    public ResourceBundle getResourceBundle()
    {
        return resourceBundle;
    }

    /**
     * Returns an array of all editors that have an unsaved content. If the
     * identical content is presented in more than one editor, only one of those
     * editor parts is part of the result.
     * 
     * @return an array of all dirty editor parts.
     */
    public static IEditorPart[] getDirtyEditors()
    {
        Set inputs = new HashSet();
        List result = new ArrayList(0);
        IWorkbench workbench = getInstance().getWorkbench();
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++)
        {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int x = 0; x < pages.length; x++)
            {
                IEditorPart[] editors = pages[x].getDirtyEditors();
                for (int z = 0; z < editors.length; z++)
                {
                    IEditorPart ep = editors[z];
                    IEditorInput input = ep.getEditorInput();
                    if (!inputs.contains(input))
                    {
                        inputs.add(input);
                        result.add(ep);
                    }
                }
            }
        }
        return (IEditorPart[]) result.toArray(new IEditorPart[result.size()]);
    }

    public static boolean isDeepDecoration()
    {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcaseUIPreferenceConstants.DEEP_DECORATIONS);
    }

    public static boolean isTextDirtyDecoration()
    {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcaseUIPreferenceConstants.TEXT_DIRTY_DECORATION);
    }

    public static boolean isTextNewDecoration()
    {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcaseUIPreferenceConstants.TEXT_NEW_DECORATION);
    }

    public static boolean isTextVersionDecoration()
    {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION);
    }

    public static boolean isTextViewDecoration()
    {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
     */
    protected void initializeDefaultPreferences(IPreferenceStore store)
    {
        super.initializeDefaultPreferences(store);

        // Decorator preferences
        store.setDefault(IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION,
                true);
        store.setDefault(
                IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION, false);
        store.setDefault(IClearcaseUIPreferenceConstants.TEXT_DIRTY_DECORATION,
                false);
        store.setDefault(IClearcaseUIPreferenceConstants.TEXT_NEW_DECORATION,
                false);
        store
                .setDefault(IClearcaseUIPreferenceConstants.DEEP_DECORATIONS,
                        true);
    }
}