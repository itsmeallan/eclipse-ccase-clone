package net.sourceforge.eclipseccase.ui.operation;

import org.eclipse.core.resources.IFile;

import org.eclipse.compare.CompareUI;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;

import org.eclipse.compare.CompareConfiguration;

public class InternalCompareOperation {
	
	private String selected;
	private IResource resource;
	private String comparableVersion;
	private CompareConfiguration cmpConfig;
	private ClearCaseProvider provider;
	
	public InternalCompareOperation(IResource resource,String selectedFile,String comparableVersion,ClearCaseProvider provider){
		this.resource = resource;
		this.selected = selectedFile;
		this.comparableVersion = comparableVersion;
		this.provider = provider;
		setup();
		cmpConfig = new CompareConfiguration();
	}
	
	private void setup(){
		cmpConfig = new CompareConfiguration();
		cmpConfig.setLeftEditable(false);
		cmpConfig.setRightEditable(false); // Could be made editable in the future
	
	}

	public void execute() {
		// execute
		if(resource instanceof IFile){	
		VersionCompareInput input = new VersionCompareInput(cmpConfig,(IFile)resource,selected,comparableVersion,provider);
		CompareUI.openCompareEditor(input);
		}
	
	}
	
	

}
