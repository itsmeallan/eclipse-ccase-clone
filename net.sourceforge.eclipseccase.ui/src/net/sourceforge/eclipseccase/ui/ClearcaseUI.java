package net.sourceforge.eclipseccase.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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
    public ClearcaseUI() {
        super();
        plugin = this;
        try
        {
            resourceBundle = ResourceBundle
                    .getBundle("net.sourceforge.eclipseccase.ui.ClearcaseUIResources");
        } catch (MissingResourceException x)
        {
            resourceBundle = null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#startup()
     */
    public void startup() throws CoreException
    {
        super.startup();
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

    /**
     * Returns an array of all editors that have an unsaved content. If the identical content is 
     * presented in more than one editor, only one of those editor parts is part of the result.
     * 
     * @return an array of all dirty editor parts.
     */
    public static IEditorPart[] getDirtyEditors() {
        Set inputs= new HashSet();
        List result= new ArrayList(0);
        IWorkbench workbench= getInstance().getWorkbench();
        IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
        for (int i= 0; i < windows.length; i++) {
            IWorkbenchPage[] pages= windows[i].getPages();
            for (int x= 0; x < pages.length; x++) {
                IEditorPart[] editors= pages[x].getDirtyEditors();
                for (int z= 0; z < editors.length; z++) {
                    IEditorPart ep= editors[z];
                    IEditorInput input= ep.getEditorInput();
                    if (!inputs.contains(input)) {
                        inputs.add(input);
                        result.add(ep);
                    }
                }
            }
        }
        return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
    }
    
}
