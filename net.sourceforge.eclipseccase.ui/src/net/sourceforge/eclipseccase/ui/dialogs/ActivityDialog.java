/*******************************************************************************
 * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     mikael petterson - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.dialogs;

import net.sourceforge.eclipseccase.ucm.Activity;

import org.eclipse.jface.viewers.DoubleClickEvent;

import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;


import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.jface.viewers.ArrayContentProvider;

import org.eclipse.jface.viewers.ListViewer;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.jface.dialogs.MessageDialog;

import java.util.*;
import net.sourceforge.eclipseccase.*;
import net.sourceforge.eclipseccase.ui.CommentDialogArea;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

/**
 * @author mikael petterson
 * 
 */
public class ActivityDialog extends Dialog {

	/** trace id */
	private static final String TRACE_ACTIVITYDIALOG = "ActivityDialog"; //$NON-NLS-1$

	private ListViewer listViewer;

	// private Combo activityCombo;

	private Button newButton;

	private Button oKButton;

	private CommentDialogArea commentDialogArea;

	private ClearCaseProvider provider;

	private static final String NO_ACTIVITY = "NONE";

	private Activity selectedActivity = null;

	private static boolean test = false;

	private IResource resource;

	public ActivityDialog(Shell parentShell, ClearCaseProvider provider, IResource resource) {
		super(parentShell);
		this.setShellStyle(SWT.CLOSE);
		this.provider = provider;
		this.resource = resource;
		commentDialogArea = new CommentDialogArea(this, null);

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		System.out.println("Enter: createDialogArea()");
		getShell().setText(Messages.getString("ActivityDialog.title"));
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);

		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel.setText(Messages.getString("ActivityDialog.activityDescription")); //$NON-NLS-1$
		descriptionLabel.setLayoutData(new GridData());

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("ActivityDialog.activity")); //$NON-NLS-1$
		label.setLayoutData(new GridData());
		// refresh
		String viewName = ClearCaseProvider.getViewName(resource);
		System.out.println("view "+viewName);
		Activity[] activities = Activity.refreshActivities(viewName, provider);
		listViewer = createListViewer(composite, activities);
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (ClearCasePlugin.DEBUG_UCM) {
					ClearCasePlugin.trace(TRACE_ACTIVITYDIALOG, "Selected: " + selection.getFirstElement()); //$NON-NLS-1$
				}
				setSelectedActivity((Activity) (selection.getFirstElement()));
			}
		});

		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (ClearCasePlugin.DEBUG_UCM) {
					ClearCasePlugin.trace(TRACE_ACTIVITYDIALOG, "Double Clicked: " + selection.getFirstElement()); //$NON-NLS-1$
				}
				setSelectedActivity((Activity) (selection.getFirstElement()));
			}
		});

		addButton(parent);

		commentDialogArea.createArea(composite);
		commentDialogArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == CommentDialogArea.OK_REQUESTED) {
					okPressed();
				}
			}
		});
		return composite;

	}

	private void addButton(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		newButton = new Button(buttons, SWT.PUSH);
		newButton.setText(Messages.getString("ActivityDialog.newActivity")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, newButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		newButton.setLayoutData(data);
		newButton.setEnabled(true);
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Open new Dialog to add activity.
				Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				NewActivityDialog dlg = new NewActivityDialog(activeShell, provider, ActivityDialog.this, resource);
				if (dlg.open() == Window.OK) {
					// refresh
					String viewName = ClearCaseProvider.getViewName(resource);
					Activity[] activities = Activity.refreshActivities(viewName, provider);
					listViewer.setInput(activities);
				} else {
					return;
				}

			}
		};
		newButton.addSelectionListener(listener);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CANCEL_ID) {
			// make sure no new activity is set when dialog is cancelled.
			selectedActivity = null;

		}

		super.buttonPressed(buttonId);
	}

	protected ListViewer createListViewer(Composite parent, Activity[] activities) {
		ListViewer listViewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewer.setLabelProvider(new ActivityListLabelProvider());
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setInput(activities);
		listViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object p1, Object p2) {
				return ((Activity) p1).getHeadline().compareToIgnoreCase(((Activity) p2).getHeadline());
			}

		});

		return listViewer;

	}

	public Activity getSelectedActivity() {
		return selectedActivity;
	}

	public void setSelectedActivity(Activity selectedActivity) {
		this.selectedActivity = selectedActivity;
	}

	public boolean activityExist(String headline) {
		ArrayList<Activity> activities = Activity.getActivities();
		for (Activity activity : activities) {
			if (headline.equalsIgnoreCase(activity.getHeadline())) {
				return true;
			}

		}
		return false;
	}

	// TODO: For testing only.
	public static void main(String[] args) {
		Display display = Display.getCurrent();
		Shell activeShell = new Shell(display);
		ActivityDialog.setTest(true);
		ActivityDialog ad = new ActivityDialog(activeShell, null, null);
		ad.open();
	}

	public static void setTest(boolean value) {
		test = value;
	}

	public static boolean isTest() {
		return test;
	}

}