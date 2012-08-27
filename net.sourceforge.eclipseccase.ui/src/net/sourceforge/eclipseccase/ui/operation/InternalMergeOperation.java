package net.sourceforge.eclipseccase.ui.operation;

import org.eclipse.ui.PartInitException;

import org.eclipse.ui.IEditorPart;

import org.eclipse.ui.IEditorSite;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.IWorkbenchWindow;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public class InternalMergeOperation {
	
	private String selected;
	private IResource resource;
	private String comparableVersion;
	private String base;
	private CompareConfiguration cmpConfig;
	private ClearCaseProvider provider;
	
	public InternalMergeOperation(IResource resource,String selectedFile,String comparableVersion,String base,ClearCaseProvider provider){
		this.resource = resource;
		this.selected = selectedFile;
		this.comparableVersion = comparableVersion;
		this.base = base;
		this.provider = provider;
		setup();
		cmpConfig = new CompareConfiguration();
		
		
		
	}
	
	private void setup(){
		cmpConfig = new CompareConfiguration();
		cmpConfig.setLeftEditable(true);
		
		
	
	}

	public void execute() {
		// execute
		//if(resource instanceof IFile){	
		VersionMergeInput input = new VersionMergeInput(cmpConfig,(IFile)resource,selected,comparableVersion,base,provider);
		CompareUI.openCompareEditor(input);
		
		//}
	
	}

}
