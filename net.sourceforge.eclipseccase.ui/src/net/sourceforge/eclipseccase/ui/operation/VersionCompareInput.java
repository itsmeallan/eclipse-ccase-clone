package net.sourceforge.eclipseccase.ui.operation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.compare.ClearCaseResourceNode;
import org.eclipse.compare.*;
import org.eclipse.core.resources.IFile;

public class VersionCompareInput extends CompareEditorInput {

	private final ITypedElement leftElement;

	private final ITypedElement rightElement;

	private IFile resource;

	private String rightVersion = "";
	
	private String leftVersion = "";

	public VersionCompareInput(CompareConfiguration configuration, IFile resource, String selected, String comparableVersion, ClearCaseProvider provider) {
		super(configuration);
		
		this.resource = resource;
		this.rightVersion = selected;
		this.leftVersion = comparableVersion;
		
		 leftElement = selected != null ? new
		 ClearCaseResourceNode(resource, selected,provider) : new
		 ResourceNode(resource);
		 rightElement = comparableVersion != null ? new
		 ClearCaseResourceNode(resource, comparableVersion,provider) : new
		 ResourceNode(resource);

		

		configuration.setLeftImage(CompareUI.getImage(resource));
		if (leftVersion != null) {
			configuration.setLeftLabel(leftVersion);
		}

		configuration.setRightImage(CompareUI.getImage(resource));
		if (rightVersion != null) {
			configuration.setRightLabel(rightVersion);
		}

		setTitle(resource.getName());
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

	@Override
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		return new DiffNode(null, Differencer.CHANGE, null, new ClearCaseResourceNode(resource, leftVersion, null), new ResourceNode(resource));
	}

}
