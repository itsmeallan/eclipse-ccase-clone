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






import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import net.sourceforge.eclipseccase.ui.CommentDialogArea;

import net.sourceforge.eclipseccase.ui.dialogs.Messages;

import org.eclipse.ui.PlatformUI;

import org.eclipse.swt.events.*;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author mikael petterson
 * 
 */
public class ActivityDialog extends Dialog {

	private Combo activityCombo;
	
	private Button newButton;
	
	private String activity;
	
	private CommentDialogArea commentDialogArea;

	public ActivityDialog(Shell parentShell) {
		super(parentShell);
		this.setShellStyle(SWT.CLOSE);
		commentDialogArea = new CommentDialogArea(this, null);

	}

	protected Control createDialogArea(Composite parent) {
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

		activityCombo = createCombo(composite);
		activityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		activityCombo.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				activity = ((Combo) e.widget).getText();
				
			}
		});

		activityCombo.setFocus();
		
		addButton(parent);
		
		commentDialogArea.createArea(composite);
		commentDialogArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == CommentDialogArea.OK_REQUESTED) {
					okPressed();
				}
			}
		});
		
		initContent();
		
		return composite;

	}
	
	
	private void addButton(Composite parent){
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
			public void widgetSelected(SelectionEvent e) {
				//Open new Dialog to add activity.
				Shell activeShell = PlatformUI.getWorkbench()
				.getDisplay().getActiveShell();
				NewActivityDialog dlg = new NewActivityDialog(activeShell);
				//FIXME: mike 20110407 update list
				initContent();
				
			}
		};
		newButton.addSelectionListener(listener);
	}
	
	private void initContent(){

		//FIXME: mike 20110407 get a list of activities.
		String [] activity = new String [] {"ActOne","ActTwo" };
		for (int i = 0; i < activity.length; i++) {
			activityCombo.add(activity[i]);
		}
		activityCombo.select(0);
	}
	
		
	/*
	 * Utility method that creates a combo box
	 * 
	 * @param parent the parent for the new label
	 * 
	 * @return the new widget
	 */
	protected Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		combo.setLayoutData(data);
		return combo;
	}
		
	
	/**
	 * Returns the comment.
	 * 
	 * @return String
	 */
	public String getComment() {
		return commentDialogArea.getComment();
	}
	
	
	
	public String getActivity() {
		return activity;
	}

	// For testing of Dialog.
	public static void main(String[] args) {
		final Display display = new Display();
		Shell shell = new Shell(display);
		ActivityDialog dlg = new ActivityDialog(shell);
		dlg.open();
	}
}
