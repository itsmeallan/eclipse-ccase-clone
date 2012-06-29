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
package net.sourceforge.eclipseccase.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.ClearCaseUI;
import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;
import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

import org.eclipse.ui.INewWizard;

/**
 * @author mikael petterson
 *
 */
public class MergeWizard extends ResizableWizard implements INewWizard {
	
	private MergeWizardPage page;

	private IResource[] resources;

	private IStructuredSelection selection;

	private ClearCaseProvider provider;

	public static final String WIZARD_DIALOG_SETTINGS = "MergeWizard"; //$NON-NLS-1$

	public static final int SCALE = 100;

	/**
	 * Constructor for CheckinWizard.
	 */
	public MergeWizard(IResource[] resources, ClearCaseProvider provider) {
		super(WIZARD_DIALOG_SETTINGS, ClearCaseUI.getInstance().getDialogSettings());
		setNeedsProgressMonitor(true);
		this.resources = resources;
		this.provider = provider;
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new MergeWizardPage("Merge", resources, provider);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		final String comment = page.getComment();
		final IResource[] resources = page.getResourceList();
		final boolean recursive = page.isRecursive();
		/*
		 * Build a process that will run using the IRunnableWithProgress
		 * interface so the UI can handle showing progress bars, etc.
		 */
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					/*
					 * The method (see below) which contains the "real"
					 * implementation code.
					 */
					doFinish(provider, comment, resources, recursive, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			/* This runs the process built above in a seperate thread */
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;

	}

	/**
	 * The worker method. It will make the actual checkin of the resource.
	 */

	private void doFinish(ClearCaseProvider provider, String comment, IResource[] resources, boolean isRecursive, IProgressMonitor monitor) throws CoreException {
		int depth = isRecursive ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
		monitor.beginTask("Checking in...", resources.length);
		ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
		Arrays.sort(resources, new DirectoryLastComparator());
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (provider != null) {
				provider.setComment(comment);
				provider.setOperationListener(opListener);
				provider.checkin(new IResource[] { resource }, depth, new SubProgressMonitor(monitor, 1 * SCALE));
			}
		}

	}

	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;

	}

}
