package net.sourceforge.eclipseccase.ui.dialogs;

import java.util.regex.Pattern;
import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseException;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NewActivityDialog extends Dialog {

	private Text activityText;

	private String activitySelector;

	private static long current = System.currentTimeMillis();

	private static Pattern p = Pattern.compile("[^A-Za-z0-9]");

	private static final String GENERATE_ID = "Auto-genrate ID";

	private static final String NO_GENERATE_ID = "Use this ID:";

	private boolean autoGen = true;

	private Text idText;

	private ClearCaseProvider provider;

	public NewActivityDialog(Shell parentShell, ClearCaseProvider provider) {
		super(parentShell);
		this.provider = provider;

		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("NewActivityDialog.addNew")); //$NON-NLS-1$
		label.setLayoutData(new GridData());
		activityText = new Text(composite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 150;
		activityText.setLayoutData(data);

		// Add radio button for generating Activity Id.
		Group group = new Group(composite, SWT.SHADOW_IN);
		group.setText("Activity ID");
		group.setLayout(new RowLayout(SWT.VERTICAL));

		Button button;
		Listener listener = new Listener() {
			public void handleEvent(Event e) {
				doSelection((Button) e.widget);
			}
		};

		button = new Button(group, SWT.RADIO);
		button.setText(GENERATE_ID);
		button.addListener(SWT.Selection, listener);
		button.setSelection(true);

		// group.setLayoutData(layout);
		button = new Button(group, SWT.RADIO);
		button.setText(NO_GENERATE_ID);

		button.addListener(SWT.Selection, listener);

		// Input field

		idText = new Text(group, SWT.BORDER);
		idText.setEditable(true);
		idText.setEnabled(false);

		return composite;
	}

	@Override
	protected void okPressed() {
		if (activityText.getText().trim().length() == 0) {
			MessageDialog.openError(getShell(), Messages.getString("NewActivityDialog.title"), Messages.getString(("NewActivityDialog.noActivity"))); //$NON-NLS-1$ //$NON-NLS-2$
			activityText.selectAll();
			activityText.setFocus();
			return;
		} else if (!match(activityText.getText().trim())) {
			MessageDialog.openError(getShell(), Messages.getString("NewActivityDialog.title"), Messages.getString(("NewActivityDialog.onlyAlphaNumericCharacters")));
			activityText.selectAll();
			activityText.setFocus();
		}

		String headline = activityText.getText().trim();
		if (autoGen) {
			activitySelector = headline.concat(getUniqueId());
		} else {
			String id = idText.getText().trim();
			activitySelector = headline.concat(id);
		}
		// Create activity
		// 1. Set your integration view if it is a dynamic view. For example:
		//
		// cleartool setview kmt_Integration
		//
		// If your integration view is a snapshot view, change directory to it.
		// 2. Issue the cleartool mkactivity command. For example:
		//
		// cleartool mkactivity –headline “Create Directories”
		// create_directories
		try {
			provider.createActivity(headline, activitySelector);
		} catch (ClearCaseException cce) {
			switch (cce.getErrorCode()) {
			case ClearCase.ERROR_UNABLE_TO_GET_STREAM:
				MessageDialog.openError(getShell(), Messages.getString("NewActivityDialog.title"), cce.getMessage());
				break;
			default:
				MessageDialog.openError(getShell(), Messages.getString("NewActivityDialog.title"), cce.getMessage());
				break;
			}
			return;
		}

		//
		// The Rational® ClearCase® GUI tools use the name specified with
		// –headline to identify the activity. The last argument,
		// create_directories, is the activity-selector. Use the
		// activity-selector when you issue cleartool commands.
		// 3. If you need to set your integration view to the activity, use the
		// cleartool setactivity command. For example:
		//
		// cleartool setactivity create_directories
		//
		// By default, when you make an activity with the cleartool mkactivity
		// command, your view is set to that activity. Your view is not set to
		// an activity if you create multiple activities in the same command
		// line or if you specify a stream with the –in option.

		super.okPressed();
	}

	public String getActivity() {
		return activitySelector;
	}

	public void setActivity(String activitySelector) {
		this.activitySelector = activitySelector;
	}

	private static synchronized String getUniqueId() {
		long id = current++;
		return String.valueOf(id);
	}

	// Make sure string only contains alaphanumeric characters.
	private static boolean match(String s) {
		return !p.matcher(s).find();
	}

	private void doSelection(Button button) {
		if (button.getSelection()) {
			if (button.getText().equalsIgnoreCase(NO_GENERATE_ID)) {
				idText.setEnabled(true);
				idText.setFocus();
				autoGen = false;

			} else {
				idText.setEnabled(false);
				idText.setFocus();
				autoGen = true;
			}
		}

	}

	// For testing of Dialog.
	public static void main(String[] args) {
		final Display display = new Display();
		Shell shell = new Shell(display);
		NewActivityDialog dlg = new NewActivityDialog(shell, null);
		dlg.open();
	}

}
