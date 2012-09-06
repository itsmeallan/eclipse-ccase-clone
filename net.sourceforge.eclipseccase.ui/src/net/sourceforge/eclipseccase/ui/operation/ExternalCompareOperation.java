package net.sourceforge.eclipseccase.ui.operation;



import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.runtime.Status;

import org.eclipse.core.runtime.jobs.Job;

import net.sourceforge.eclipseccase.ClearCasePreferences;

import net.sourceforge.eclipseccase.diff.DiffFactory;

import net.sourceforge.eclipseccase.diff.AbstractExternalToolCommands;

public class ExternalCompareOperation {


	
	private String comparableVersion;
	private IResource resource;

	public ExternalCompareOperation(IResource resource,String comparableVersion) {
		this.resource = resource;
		this.comparableVersion = comparableVersion;

	}

	public void execute() {
		// execute
		Job job = new Job("Compare") {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Compare started...", 10);
				// Run long running task here
				// Add a factory here that can decide which launcher to use.
				AbstractExternalToolCommands diff = DiffFactory.getDiffTool(ClearCasePreferences.getExtDiffTool());
				String vExtPath1 = resource.getLocation().toOSString()+"@@"+comparableVersion;
				String vExtPath2 = resource.getLocation().toOSString();//Dont use version extended path. Since view selects current version.
				diff.twoWayDiff(vExtPath1,vExtPath2);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

}
