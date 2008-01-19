package net.sourceforge.eclipseccase.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
//import org.eclipse.ui.internal.misc.OverlayComposite;
//FIXME: The above class has been removed from the misc package.
//Using a fix I found.
import net.sourceforge.eclipseccase.ui.OverlayComposite;

public class HashableComposite
{
	private Image backgroundImage;
	private List foregroundImages = new ArrayList();

	public HashableComposite(Image backgroundImage)
	{
		this.backgroundImage = backgroundImage;
	}
	
	public void addForegroundImage(ImageDescriptor desc)
	{
		foregroundImages.add(desc);
	}
	
	public Image createImage()
	{
		OverlayComposite comp = new OverlayComposite(backgroundImage.getImageData());
		for (Iterator iter = foregroundImages.iterator(); iter.hasNext();)
		{
			ImageDescriptor desc = (ImageDescriptor) iter.next();
			comp.addForegroundImage(desc.getImageData());
		}
		return comp.createImage();
	}
		
	public boolean equals(Object obj)
	{
		if (!(obj instanceof HashableComposite))
			return false;
		HashableComposite rhs = (HashableComposite) obj;
	
		return equals(backgroundImage, rhs.backgroundImage)
			&& equals(foregroundImages, rhs.foregroundImages);
	}
	
	private boolean equals(Object o1, Object o2)
	{
		return o1 == null ? o2 == null : o1.equals(o2);
	}
	
	public int hashCode()
	{
		return hashCode(backgroundImage)
			+ hashCode(foregroundImages);
	}

	private int hashCode(Object o)
	{
		return o == null ? 0 : o.hashCode();
	}

}
