package net.sourceforge.eclipseccase.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

public class ClearcaseImages
{
    public static final String ICON_PATH = "icons/full/";
    public static final String IMG_REFRESH = "refresh.gif";
    public static final String IMG_CHECKEDIN_OVR = "checkedin.gif"; //$NON-NLS-1$
    public static final String IMG_CHECKEDOUT_OVR = "checkedout.gif"; //$NON-NLS-1$
    public static final String IMG_DIRTY_OVR = "dirty.gif";
    public static final String IMG_DIRTY_UNKNOWN_OVR = "dirty-unknown.gif";
    public static final String IMG_UNKNOWN_OVR = "unknown.gif";
    public static final String IMG_HIJACKED_OVR = "hijacked.gif";
    public static final String IMG_NEW_OVR = "new.gif";

    private static Map imageDescriptors;

    public static void initializeImages()
    {
        if (imageDescriptors == null)
        {
            IPluginDescriptor descriptor = Platform.getPluginRegistry().getPluginDescriptor(ClearcaseUI.PLUGIN_ID);
            URL baseURL = null != descriptor ? descriptor.getInstallURL() : null;
            if(null != baseURL)
            {
                imageDescriptors = new HashMap();
    			
                // View decoration overlays
                createImageDescriptor(IMG_REFRESH, baseURL);
                createImageDescriptor(IMG_CHECKEDIN_OVR, baseURL);
                createImageDescriptor(IMG_CHECKEDOUT_OVR, baseURL);
                createImageDescriptor(IMG_DIRTY_OVR, baseURL);
                createImageDescriptor(IMG_DIRTY_UNKNOWN_OVR, baseURL);
                createImageDescriptor(IMG_UNKNOWN_OVR, baseURL);
                createImageDescriptor(IMG_NEW_OVR, baseURL);
                createImageDescriptor(IMG_HIJACKED_OVR, baseURL);
            }
        }
    }
	
    protected static void createImageDescriptor(String id, URL baseURL)
    {
        URL url = null;
        try
        {
            url = new URL(baseURL, ICON_PATH + id);
        }
        catch (MalformedURLException e)
        {
            // ignore
        }
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        imageDescriptors.put(id, desc);
    }

    public static ImageDescriptor getImageDescriptor(String id)
    {
        if(null == imageDescriptors)
        {
            initializeImages();
            if(null == imageDescriptors) return ImageDescriptor.getMissingImageDescriptor();
        }
        
        if (! imageDescriptors.containsKey(id))
        {
            URL baseURL =
                ClearcasePlugin.getDefault().getDescriptor().getInstallURL();
            createImageDescriptor(id, baseURL);
        }
        return (ImageDescriptor) imageDescriptors.get(id);
    }

}
