package net.sourceforge.eclipseccase.ui;

import java.net.URL;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ui.TeamImages;

public class ClearcaseImages extends TeamImages
{
	public static final String IMG_DIRTY_OVR = "dirty.gif";
	public static final String IMG_UNKNOWN_OVR = "unknown.gif";
	public static final String IMG_HIJACKED_OVR = "hijacked.gif";
	
	static
	{
		initImages();
	}
	
	private static void initImages()
	{
		URL baseURL = ClearcasePlugin.getDefault().getDescriptor().getInstallURL();

		// View decoration overlays
		TeamImages.createImageDescriptor(IMG_DIRTY_OVR, baseURL);
		TeamImages.createImageDescriptor(IMG_UNKNOWN_OVR, baseURL);
		TeamImages.createImageDescriptor(IMG_HIJACKED_OVR, baseURL);
	}
	
	public static ImageDescriptor getImageDescriptor(String id) {
		return TeamImages.getImageDescriptor(id);
	}	

}
