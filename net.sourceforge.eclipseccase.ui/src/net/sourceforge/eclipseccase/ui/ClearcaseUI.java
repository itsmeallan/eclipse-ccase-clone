package net.sourceforge.eclipseccase.ui;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import java.util.*;

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

    /**
     * The constructor.
     * 
     * @param descriptor
     */
    public ClearcaseUI(IPluginDescriptor descriptor) {
        super(descriptor);
        plugin = this;
        ClearcaseImages.initializeImages();
        try
        {
            resourceBundle = ResourceBundle
                    .getBundle("net.sourceforge.eclipseccase.ui.ClearcaseUIResources");
        } catch (MissingResourceException x)
        {
            resourceBundle = null;
        }
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
     * @param key
     * @return the string
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = ClearcaseUI.getInstance().getResourceBundle();
        try
        {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e)
        {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     * @return the resource bundle
     */
    public ResourceBundle getResourceBundle()
    {
        return resourceBundle;
    }
}
