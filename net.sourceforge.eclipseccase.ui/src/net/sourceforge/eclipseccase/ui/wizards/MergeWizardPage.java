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



import net.sourceforge.eclipseccase.autocomplete.ccviewer.AutocompleteComboViewerInput;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.ResourceComparator;
import net.sourceforge.eclipseccase.ui.provider.MergeLabelProvider;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * @author mikael petterson
 * 
 */
public class MergeWizardPage extends WizardPage {

	private static final ResourceComparator comparator = new ResourceComparator();

	private String[] branches;

	private IResource[] resources;

	private ClearCaseProvider provider;
		
	boolean automerge = true;
	
	private String selectedBranch;

	@SuppressWarnings("unchecked")
	protected MergeWizardPage(String pageName, IResource[] resources, ClearCaseProvider provider) {
		super(pageName);
		setTitle("Merge");
		this.resources = resources;
		this.provider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createComboViewer(composite);
		setControl(composite);

	}

	protected void createComboViewer(Composite composite) {
		final ComboViewer comboViewer = new ComboViewer(composite, SWT.SIMPLE | SWT.READ_ONLY | SWT.SCROLL_PAGE);
		comboViewer.setLabelProvider(new MergeLabelProvider());
		comboViewer.setContentProvider(new ArrayContentProvider());
		loadBranches();
		comboViewer.setInput(branches);
		if (comboViewer.getSelection().isEmpty()) {
			comboViewer.getCombo().select(0);
			setSelectedBranch(comboViewer.getCombo().getText());
		}

		// TODO:http://www.eclipsezone.com/eclipse/forums/t95207.html
		//AdvancedComboSelectionAdapter comboSelectionAdapter = new
		//AdvancedComboSelectionAdapter(comboViewer);
		AutocompleteComboViewerInput ac = new AutocompleteComboViewerInput(comboViewer);
				
		composite.setToolTipText("To select branch, type the name and search will narrow.");

	}

	private void loadBranches() {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(resources[0].getProject().getName());
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask("Loading branches for project ...", 100);
					// execute the task ...

					if (project != null) {
						File workingDir = new File(project.getLocation().toOSString());

						if (provider != null && (provider.isClearCaseElement(project))) {
							branches = provider.loadBrancheList(workingDir);

						}
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public String getSelectedBranch() {
		return selectedBranch;
	}

	public void setSelectedBranch(String selectedBranch) {
		this.selectedBranch = selectedBranch;
	}



}
