package net.sourceforge.eclipseccase.ui;

import org.eclipse.jface.resource.ImageDescriptor;


public class ClearcaseImages
{
    public static final String ICON_PATH = "icons/full/";

    public static final String IMG_HIJACKED = "hijacked.gif";
    public static final String IMG_LINK = "link_ovr.gif";
    public static final String IMG_LINK_WARNING = "linkwarn_ovr.gif";
	public static final String IMG_QUESTIONABLE = "question_ov.gif"; //$NON-NLS-1$
	public static final String IMG_EDITED = "edited_ov.gif"; //$NON-NLS-1$
	public static final String IMG_NO_REMOTEDIR = "no_remotedir_ov.gif"; //$NON-NLS-1$
	
    /**
     * @param string
     * @return
     */
    public static ImageDescriptor getImageDescriptor(String string)
    {
        return ClearcaseUI.getInstance().getImageRegistry().getDescriptor(string);
    }
}
