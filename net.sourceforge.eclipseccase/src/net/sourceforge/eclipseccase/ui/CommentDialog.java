package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class CommentDialog extends Dialog
{
    private CommentDialogArea commentDialogArea;
    private String title;

    /**
     * Creates a new CommentDialog instance.
     * @param parentShell
     * @param dialogTitle
     */
    public CommentDialog(Shell parentShell, String dialogTitle)
    {
        super(parentShell);
        commentDialogArea =
            new CommentDialogArea(this, null, ClearcasePlugin.isMultiLineComments());
        this.title = dialogTitle;
    }

    Button recursiveButton;
    boolean recursive = false;

    /**
     * Gets the recursive.
     * @return Returns a boolean
     */
    public boolean isRecursive()
    {
        return recursive;
    }

    /**
     * Sets the recursive.
     * @param recursive The recursive to set
     */
    public void setRecursive(boolean recursive)
    {
        this.recursive = recursive;
    }

    /**
     * @see Dialog#createDialogArea(Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        getShell().setText(title);
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        commentDialogArea.createArea(composite);
        commentDialogArea.addPropertyChangeListener(new IPropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getProperty() == CommentDialogArea.OK_REQUESTED)
                    okPressed();
            }
        });

        recursiveButton = new Button(composite, SWT.CHECK);
        recursiveButton.setText("Recurse");
        recursiveButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                recursive = recursiveButton.getSelection();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {}
        });

        // set F1 help
        //WorkbenchHelp.setHelp(composite, IHelpContextIds.RELEASE_COMMENT_DIALOG);

        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        super.okPressed();
    }

    /**
     * Returns the comment.
     * @return String
     */
    public String getComment()
    {
        return commentDialogArea.getComment();
    }
}