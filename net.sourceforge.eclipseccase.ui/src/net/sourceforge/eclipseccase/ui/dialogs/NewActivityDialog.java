package net.sourceforge.eclipseccase.ui.dialogs;

import org.eclipse.swt.events.ModifyEvent;

import org.eclipse.swt.events.ModifyListener;

import java.util.regex.Pattern;
import net.sourceforge.clearcase.*;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Class creates a new Activity dialog and then let the user enter a headline.
 * Id can be generated or manually entered.
 * 
 * @author mikael
 * 
 */
public class NewActivityDialog extends Dialog {

	private Text activityText;

	private String activitySelector;

	private static long current = System.currentTimeMillis();

	// The string can contain the following characters.
	private static Pattern p = Pattern.compile("[^A-Za-z0-9_\\-]");

	private static final String GENERATE_ID = "Auto-genrate ID";

	private static final String NO_GENERATE_ID = "Use this ID:";

	private boolean autoGen = true;

	private Text idText;

	private ClearCaseProvider provider;

	private ActivityDialog activityDialog;

	private String snapshotPath;

	private IResource resource;

	public NewActivityDialog(Shell parentShell, ClearCaseProvider provider, ActivityDialog ad, IResource resource) {
		super(parentShell);
		this.provider = provider;
		this.activityDialog = ad;
		this.resource = resource;
		
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
		}

		// replace all spaces to underscore since it is not allowed.
		String noSpaceHeadline = activityText.getText().trim().replaceAll(" ", "_");

		if (!match(noSpaceHeadline)) {
			MessageDialog.openError(getShell(), Messages.getString("NewActivityDialog.title"), Messages.getString(("NewActivityDialog.onlyAlphaNumericCharacters")));
			activityText.selectAll();
			activityText.setFocus();
			return;
		}

		if (provider.isSnapShot(resource) && snapshotPath == null) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			String platform = SWT.getPlatform();
			dialog.setText(Messages.getString("NewActivityDialog.snapshotDirectory"));
			dialog.setMessage(Messages.getString("NewActivityDialog.selectSnapshotDir"));
			dialog.setFilterPath(platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");
			snapshotPath = dialog.open();
			

		}

		if (snapshotPath == null) {
			MessageDialog.openError(getShell(), Messages.getString("NewActivityDialog.title"), Messages.getString("NewActivityDialog.noSnapshotDirectory")); //$NON-NLS-1$ //$NON-NLS-2$
			activityText.selectAll();
			activityText.setFocus();
			return;
		}
		
		System.out.println("snapshotPath is "+snapshotPath);
		if (autoGen) {
			if (activityDialog.activityExist(noSpaceHeadline)) {
				// if duplicate then add unique id to headline.
				activitySelector = noSpaceHeadline.concat(getUniqueId());
			} else {
				activitySelector = noSpaceHeadline;
			}
		} else {
			String id = idText.getText().trim();
			activitySelector = noSpaceHeadline.concat(id);
		}

		try {
			ClearCaseElementState state = provider.createActivity(noSpaceHeadline, activitySelector, snapshotPath);
			if (state.state == ClearCase.ACTIVITY_CREATED) {
				System.out.println("Actvity created " + noSpaceHeadline);

			}
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
		NewActivityDialog dlg = new NewActivityDialog(shell, null, null, null);
		dlg.open();
	}

}
