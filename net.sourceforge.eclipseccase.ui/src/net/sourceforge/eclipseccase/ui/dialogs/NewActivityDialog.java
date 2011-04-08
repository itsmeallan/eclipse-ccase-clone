package net.sourceforge.eclipseccase.ui.dialogs;



import net.sourceforge.eclipseccase.ui.dialogs.Messages;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.window.IShellProvider;

import org.eclipse.jface.dialogs.Dialog;

public class NewActivityDialog extends Dialog {
	
	private Text activityText;
	
	private String activity;

	public NewActivityDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel.setText(Messages.getString("NewActivityDialog.addNew")); //$NON-NLS-1$
		descriptionLabel.setLayoutData(new GridData());
		activityText = new Text(composite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 150;
		activityText.setLayoutData(data);
		return composite;
	}
	
	protected void okPressed() {
        if (activityText.getText().trim().length() == 0) {
            MessageDialog.openError(getShell(), Messages.getString("NewActivityDialog.title"), Messages.getString(("NewActivityDialog.noActivity")));  //$NON-NLS-1$ //$NON-NLS-2$
            activityText.selectAll();
            activityText.setFocus();
            return; //$NON-NLS-1$
        }
        activity = activityText.getText().trim();
        //FIXME: mike 20110406 generate ID ?
        
        //Call create activity.

        super.okPressed();
    }

	// For testing of Dialog.
	public static void main(String[] args) {
		final Display display = new Display();
		Shell shell = new Shell(display);
		NewActivityDialog dlg = new NewActivityDialog(shell);
		dlg.open();
	}
}
