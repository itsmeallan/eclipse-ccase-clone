/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - adaption for ClearCase plug-in
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This area provides the widgets for providing the CVS commit comment
 */
public class CommentDialogArea extends DialogArea
{

    private static final int WIDTH_HINT = 350;
    private static final int HEIGHT_HINT = 150;

    Text text;
    Combo previousCommentsCombo;
    private IProject mainProject;
    String[] comments = new String[0];
    String comment = ""; //$NON-NLS-1$

    public static final String OK_REQUESTED = "OkRequested"; //$NON-NLS-1$

    boolean multiLine = false;

    /**
     * Constructor for CommentDialogArea.
     * @param parentDialog
     * @param settings
     */
    public CommentDialogArea(Dialog parentDialog, IDialogSettings settings, boolean multiLine)
    {
        super(parentDialog, settings);
        comments = ClearcasePlugin.getDefault().getPreviousComments();
        this.multiLine = multiLine;
    }

    public Control createArea(Composite parent)
    {
        Composite composite = createGrabbingComposite(parent, 1);
        initializeDialogUnits(composite);

        Label label = new Label(composite, SWT.NULL);
        label.setLayoutData(new GridData());
        label.setText("Edit the &comment:");

        GridData data = null;

        if (multiLine)
        {
            text = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
            data = new GridData(GridData.FILL_BOTH);
            data.widthHint = WIDTH_HINT;
            data.heightHint = HEIGHT_HINT;
            text.setLayoutData(data);
        }
        else
        {
            text = new Text(composite, SWT.SINGLE | SWT.BORDER);
            data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
            data.widthHint = WIDTH_HINT;
            text.setLayoutData(data);
        }
        text.selectAll();
        text.addTraverseListener(new TraverseListener()
        {
            public void keyTraversed(TraverseEvent e)
            {
                if (e.detail == SWT.TRAVERSE_RETURN && (e.stateMask & SWT.CTRL) != 0)
                {
                    e.doit = false;
                    CommentDialogArea.this.signalCtrlEnter();
                }
            }
        });
        text.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
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

        previousCommentsCombo.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                int index = previousCommentsCombo.getSelectionIndex();
                if (index != -1)
                    text.setText(multiLine ? comments[index] : flattenText(comments[index]));
            }
        });

        return composite;
    }

    /**
     * Method initializeValues.
     */
    private void initializeValues()
    {

        // populate the previous comment list
        for (int i = 0; i < comments.length; i++)
        {
            previousCommentsCombo.add(flattenText(comments[i]));
        }

        // We don't want to have an initial selection
        // (see bug 32078: http://bugs.eclipse.org/bugs/show_bug.cgi?id=32078)
        previousCommentsCombo.setText(""); //$NON-NLS-1$
    }

    /**
     * Flatten the text in the multiline comment
     * @param string
     * @return String
     */
    String flattenText(String string)
    {
        StringBuffer buffer = new StringBuffer(string.length() + 20);
        boolean skipAdjacentLineSeparator = true;
        for (int i = 0; i < string.length(); i++)
        {
            char c = string.charAt(i);
            if (c == '\r' || c == '\n')
            {
                if (!skipAdjacentLineSeparator)
                    buffer.append("/");
                skipAdjacentLineSeparator = true;
            }
            else
            {
                buffer.append(c);
                skipAdjacentLineSeparator = false;
            }
        }
        return buffer.toString();
    }

    /**
     * Method signalCtrlEnter.
     */
    void signalCtrlEnter()
    {
        firePropertyChangeChange(OK_REQUESTED, null, null);
    }

    /**
     * Method clearCommitText.
     */
    private void clearCommitText()
    {
        text.setText(""); //$NON-NLS-1$
        previousCommentsCombo.deselectAll();
    }

    /**
     * Return the entered comment
     * 
     * @return the comment
     */
    public String[] getComments()
    {
        return comments;
    }

    /**
     * Returns the comment.
     * @return String
     */
    public String getComment()
    {
        if (comment != null && comment.length() > 0)
            finished();
        return comment;
    }

    /**
     * Method setProject.
     * @param iProject
     */
    public void setProject(IProject iProject)
    {
        this.mainProject = iProject;
    }

    private void finished()
    {
        // if there is a comment, remember it
        if (comment.length() > 0)
        {
            ClearcasePlugin.getDefault().addComment(comment);
        }
    }
}
