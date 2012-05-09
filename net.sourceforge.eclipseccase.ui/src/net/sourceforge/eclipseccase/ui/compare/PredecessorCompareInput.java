package net.sourceforge.eclipseccase.ui.compare;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * Implements compare editor input for the version selected by the view and its
 * predecessor version. 
 * 
 * @author eplatti
 */

public class PredecessorCompareInput extends CompareEditorInput {

	private final IFile resource;
	private final String previousVersion;
	private final ClearCaseProvider provider;
	private final ITypedElement rightElement;

	/**
	 * Create a new PredecessorCompareInput input instance.
	 * 
	 * @param configuration The compare configuration.
	 * @param resource The ClearCase element resource.
	 * @param provider The ClearCase provider.
	 */
	
	public PredecessorCompareInput(CompareConfiguration configuration, IFile resource, ClearCaseProvider provider) {
		super(configuration);
		this.resource = resource;
		this.provider = provider;
		previousVersion = provider.getPredecessorVersion(resource);
		
		configuration.setLeftImage(CompareUI.getImage(resource));
		configuration.setLeftLabel(previousVersion);
		
		configuration.setRightImage(CompareUI.getImage(resource));
		if(provider.isCheckedOut(resource))
		{
			configuration.setRightEditable(true);
			configuration.setRightLabel(resource.getLocation().toOSString());	
			rightElement = new ResourceNode(resource);

		}
		else
		{
			String ver = provider.getVersion(resource);
			configuration.setRightLabel(ver);	
			rightElement = new ClearCaseResourceNode(resource, ver, provider);
		}
		
    setTitle(resource.getName());
	}
	
	@Override
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {

		return new DiffNode(null, Differencer.CHANGE, null,
				new ClearCaseResourceNode(resource, previousVersion,provider),
				rightElement);
	}

	/**
	 * Method is used to saving compare results.
	 */
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
		if (!(rightElement instanceof ResourceNode && rightElement instanceof BufferedContent)) {
			return;
		}
		ResourceNode rn = (ResourceNode) rightElement;
		BufferedContent bc = (BufferedContent) rightElement;
		IResource resource = rn.getResource();
		if (resource instanceof IFile) {
			byte[] bytes = bc.getContent();
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			try {
				IFile file = (IFile) resource;
				if (file.exists()) {
					file.setContents(is, false, true, pm);
				} else {
					file.create(is, false, pm);
				}
			} finally {
				if (is != null)
					try {
						is.close();
						setDirty(false);
					} catch (IOException ex) {
						// Ignored
					}
				resource.getParent().refreshLocal(IResource.DEPTH_INFINITE, pm);
			}
		}
	}
}
