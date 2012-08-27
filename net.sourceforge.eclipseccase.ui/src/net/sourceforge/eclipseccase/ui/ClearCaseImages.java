package net.sourceforge.eclipseccase.ui;

import java.io.File;
import java.net.MalformedURLException;
import net.sourceforge.eclipseccase.ui.preferences.ClearCaseUIPreferences;
import org.eclipse.jface.resource.ImageDescriptor;

public class ClearCaseImages {
	// base path
	public static final String ICON_PATH = "icons/full/"; //$NON-NLS-1$

	// images (don't forget to add to ClearCaseUI#initialize...)
	public static final String IMG_EDITED_OVR = "edited_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_HIJACKED_OVR = "hijacked_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_LINK_OVR = "link_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_LINK_WARNING_OVR = "linkwarn_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_UNKNOWN_OVR = "unknown_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_DERIVEDOBJECT_OVR = "derivedobj_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_QUESTIONABLE_OVR = "question_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_CHECKEDOUT_OVR = "checkedout_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_DYNAMIC_OVR = "proj_dynamic_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_SNAPSHOT_OVR = "proj_snapshot_ovr.gif"; //$NON-NLS-1$

	public static final String IMG_ELEM_CO = "elem_co.png"; //$NON-NLS-1$

	public static final String IMG_ELEM_HJ = "elem_hj.png"; //$NON-NLS-1$

	public static final String IMG_ELEM_UNK = "elem_unk.png"; //$NON-NLS-1$

	public static final String IMG_REFRESH = "refresh.gif"; //$NON-NLS-1$

	public static final String IMG_REFRESH_DISABLED = "refresh_disabled.gif"; //$NON-NLS-1$

	public static final String IMG_ELEMENT_BG = "element_bg.gif"; //$NON-NLS-1$
	
	public static final String IMG_CHECKED = "checked.gif";//$NON-NLS-1$
	
	public static final String IMG_UNCHECKED = "unchecked.gif"; //$NON-NLS-1$

	/**
	 * @param string
	 * @return
	 */
	public static ImageDescriptor getImageDescriptor(String string) {
		return ClearCaseUI.getInstance().getImageRegistry().getDescriptor(string);
	}

	/**
	 * Returns the image that the ClearCase decorator should use for all
	 * ClearCase element backgrounds.
	 * 
	 * @return the background image for decorating ClearCase elements.
	 */
	public static ImageDescriptor getClearCaseElementsBackgroundImage() {
		if (!ClearCaseUIPreferences.getPluginPreferences().getBoolean(ClearCaseUIPreferences.IMAGE_CLEARCASE_ELEMENTS_BACKGROUND_CUSTOM))
			return getImageDescriptor(IMG_ELEMENT_BG);

		String customImage = ClearCaseUIPreferences.getPluginPreferences().getString(ClearCaseUIPreferences.IMAGE_CLEARCASE_ELEMENTS_BACKGROUND);
		if (null == customImage || customImage.trim().length() == 0)
			return ImageDescriptor.getMissingImageDescriptor();

		if (!(customImage.toLowerCase().endsWith(".gif") //$NON-NLS-1$
				|| customImage.toLowerCase().endsWith(".jpg") //$NON-NLS-1$
		|| customImage.toLowerCase().endsWith(".png"))) //$NON-NLS-1$
			return ImageDescriptor.getMissingImageDescriptor();

		ImageDescriptor image = getImageDescriptor(customImage);
		if (null == image) {
			// create image
			File imageFile = new File(customImage);
			if (imageFile.canRead()) {
				try {
					image = ImageDescriptor.createFromURL(imageFile.toURL());
				} catch (MalformedURLException e) {
					// ignore
					image = null;
				}
			}
			// ensure image is not null
			if (null == image) {
				image = ImageDescriptor.getMissingImageDescriptor();
			}

			// store image
			ClearCaseUI.getInstance().getImageRegistry().put(customImage, image);
		}

		return image;
	}

}
