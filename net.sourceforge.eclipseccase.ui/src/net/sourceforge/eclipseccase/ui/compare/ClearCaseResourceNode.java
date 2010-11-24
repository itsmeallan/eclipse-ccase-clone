package net.sourceforge.eclipseccase.ui.compare;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;

/**
 * Implements resource node for comparing ClearCase element versions.  
 * 
 * <p>Limitation: Do not handle directories.</p> 
 * 
 * @author eplatti
 */

public class ClearCaseResourceNode extends BufferedContent implements ITypedElement{

	private final IResource resource;
	private final String vextPath;
	
	public ClearCaseResourceNode(IResource resource, String version){
		this.resource = resource;
		this.vextPath = resource.getLocation().toOSString() + "@@" + version;
	}
	
	@Override
	protected InputStream createStream() throws CoreException {
		try {
			return new BufferedInputStream(new FileInputStream(vextPath)) ;
		} catch (FileNotFoundException e) {
			throw new CoreException(new Status(IStatus.WARNING, "net.sourceforge.eclipseccase.ui.compare", "Internal, can't open compare stream", e));
		}
	}

	public String getName() {
		return vextPath;
	}

	public Image getImage() {
		return CompareUI.getImage(resource);
	}

	public String getType() {
		if (resource instanceof IContainer)
			return ITypedElement.FOLDER_TYPE;
		if (resource != null) {
			String s= resource.getFileExtension();
			if (s != null){
				return s;
			}
		}
		return ITypedElement.UNKNOWN_TYPE;
	}
}
