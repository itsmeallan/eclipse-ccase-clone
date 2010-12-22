/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearCasePlugin;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

/**
 * This area provides the widgets for providing the commit comment
 */
public class CommentDialogArea extends DialogArea {

	private static final int WIDTH_HINT = 350;

	private static final int HEIGHT_HINT = 150;

	Text text;

	Combo previousCommentsCombo;

	String[] comments = new String[0];

	String comment = ""; //$NON-NLS-1$

	public static final String OK_REQUESTED = "OkRequested"; //$NON-NLS-1$

	/**
	 * Constructor for CommentDialogArea.
	 * 
	 * @param parentDialog
	 * @param settings
	 */
	public CommentDialogArea(Dialog parentDialog, IDialogSettings settings) {
		super(parentDialog, settings);
		comments = ClearCasePlugin.getDefault().getPreviousComments();
	}

	@Override
	public Control createArea(Composite parent) {
		Composite composite = createGrabbingComposite(parent, 1);
		initializeDialogUnits(composite);

		Label label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Edit the &comment:");

		GridData data = null;

		text = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = WIDTH_HINT;
		data.heightHint = HEIGHT_HINT;
		text.setLayoutData(data);
		text.selectAll();
		text.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN && (e.stateMask & SWT.CTRL) != 0) {
					e.doit = false;
					CommentDialogArea.this.signalCtrlEnter();
				}
			}
		});

		text.setText(comment);
		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				comment = text.getText();
			}
		});

		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Choose a &previously entered comment:");

		previousCommentsCombo = new Combo(composite, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		previousCommentsCombo.setLayoutData(data);

		// Initialize the values before we register any listeners so
		// we don't get any platform specific selection behavior
		// (see bug 32078: http://bugs.eclipse.org/bugs/show_bug.cgi?id=32078)
		initializeValues();

		previousCommentsCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = previousCommentsCombo.getSelectionIndex();
				if (index != -1) {
					text.setText(comments[index]);
				}
			}
		});

		return composite;
	}

	/**
	 * Method initializeValues.
	 */
	private void initializeValues() {

		// populate the previous comment list
		for (int i = 0; i < comments.length; i++) {
			previousCommentsCombo.add(flattenText(comments[i]));
		}

		// We don't want to have an initial selection
		// (see bug 32078: http://bugs.eclipse.org/bugs/show_bug.cgi?id=32078)
		previousCommentsCombo.setText(""); //$NON-NLS-1$
	}

	/**
	 * Flatten the text in the multiline comment
	 * 
	 * @param string
	 * @return String
	 */
	String flattenText(String string) {
		StringBuffer buffer = new StringBuffer(string.length() + 20);
		boolean skipAdjacentLineSeparator = true;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == '\r' || c == '\n') {
				if (!skipAdjacentLineSeparator) {
					buffer.append("/");
				}
				skipAdjacentLineSeparator = true;
			} else {
				buffer.append(c);
				skipAdjacentLineSeparator = false;
			}
		}
		return buffer.toString();
	}

	/**
	 * Method signalCtrlEnter.
	 */
	void signalCtrlEnter() {
		firePropertyChangeChange(OK_REQUESTED, null, null);
	}

	/**
	 * Method clearCommitText.
	 */
	void clearCommitText() {
		text.setText(""); //$NON-NLS-1$
		previousCommentsCombo.deselectAll();
	}

	/**
	 * Return the entered comment
	 * 
	 * @return the comment
	 */
	public String[] getComments() {
		return comments;
	}

	/**
	 * Returns the comment.
	 * 
	 * @return String
	 */
	public String getComment() {
		if (comment != null && comment.length() > 0) {
			finished();
		}
		return comment;
	}

	/**
	 * Returns the comment.
	 * 
	 * @return String
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	private void finished() {
		// if there is a comment, remember it
		if (comment.length() > 0) {
			ClearCasePlugin.getDefault().addComment(comment);
		}
	}
}