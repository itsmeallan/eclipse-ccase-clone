/*******************************************************************************
 * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.operation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;

import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import java.beans.PropertyChangeEvent;
import java.util.Iterator;

import java.beans.PropertyChangeListener;

import org.eclipse.ui.statushandlers.StatusManager;

import net.sourceforge.eclipseccase.diff.MergeFactory;

import net.sourceforge.eclipseccase.ClearCasePreferences;
import net.sourceforge.eclipseccase.diff.AbstractExternalToolCommands;
import net.sourceforge.eclipseccase.diff.DiffFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author mikael petterson
 * 
 */
public class ExternalMergeOperation {

	private IResource resource;

	private String comparableVersion;

	private String base;
	
	private static List<PropertyChangeListener> listener = new ArrayList<PropertyChangeListener>();
	
	public static final String EXTERNAL_MERGE = "extMerge";

	public ExternalMergeOperation(IResource resource, String comparableVersion, String base) {
		this.base = base;
		this.resource = resource;
		this.comparableVersion = comparableVersion;

	}

	public void execute() {
		
		// execute
		Job job = new Job("Merge") {
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				monitor.beginTask("Merge started...", 10);
				try {
					// Run long running task here
					// Add a factory here that can decide which launcher to use.
					AbstractExternalToolCommands merge = MergeFactory.getMergeTool(ClearCasePreferences.getExtMergeTool());
					String vExtPath1 = resource.getLocation().toOSString() + "@@" + comparableVersion;
					// Dont use version extended path.Since view selects current
					// version.
					String path = resource.getLocation().toOSString();
					if (base == null) {
						status = merge.twoWayMerge(vExtPath1, path);
					} else {
						String baseVExtPath = resource.getLocation().toOSString() + "@@" + base;
						status = merge.threeWayMerge(vExtPath1, path, baseVExtPath);
						
					}
					if (!status.isOK()) {
						StatusManager.getManager().handle(status, StatusManager.SHOW);
					}

				} finally {
					monitor.done();
				}
				return status;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK()){
					System.out.println("Job did complete successfully!");
					notifyListeners();
					
				}
			}
		});
		//job.setUser(true);
		job.schedule();
	}

	public static void addChangeListener(PropertyChangeListener newListener) {
		listener.add(newListener);
	}

	// To mark as merged in MergeView.
	private void notifyListeners() {
		for (Iterator iterator = listener.iterator(); iterator.hasNext();) {
			PropertyChangeListener name = (PropertyChangeListener) iterator.next();
			name.propertyChange(new PropertyChangeEvent(this, EXTERNAL_MERGE, resource, null));

		}
	}

}
