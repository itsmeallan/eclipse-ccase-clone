package net.sourceforge.eclipseccase;

import org.eclipse.swt.widgets.Text;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.PropertyPage;

public class ElementPropertyPage extends PropertyPage {

	private Text predecessorVersionValue;

	private Text versionLabelValue;

	private Text checkedOutValue;

	private Text hijackedValue;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public ElementPropertyPage() {
		super();
		noDefaultAndApplyButton();
	}

	private void addFirstSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Label for path field
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText("Native Path:");

		// Path text field
		Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		pathValueText.setBackground(pathValueText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		pathValueText.setText(((IResource) getElement()).getLocation().toOSString());
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		IResource resource = (IResource) getElement();
		StateCache cache = StateCacheFactory.getInstance().get(resource);

		if (cache.isClearCaseElement()) {
			if (cache.isDerivedObject()) {
				Label versionLabel = new Label(composite, SWT.NONE);
				versionLabel.setText("State:");
				versionLabelValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
				versionLabelValue.setBackground(versionLabelValue.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			} else {
				Label versionLabel = new Label(composite, SWT.NONE);
				versionLabel.setText("Version:");
				versionLabelValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
				versionLabelValue.setBackground(versionLabelValue.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

				Label predecessorVersionLabel = new Label(composite, SWT.NONE);
				predecessorVersionLabel.setText("Predecessor Version:");
				predecessorVersionValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
				predecessorVersionValue.setBackground(versionLabelValue.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

				Label checkedOutLabel = new Label(composite, SWT.NONE);
				checkedOutLabel.setText("Checked Out:");
				checkedOutValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
				checkedOutValue.setBackground(versionLabelValue.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

				if (cache.isSnapShot()) {
					Label hijackedLabel = new Label(composite, SWT.NONE);
					hijackedLabel.setText("Hijacked:");
					hijackedValue = new Text(composite, SWT.CHECK);
					hijackedValue.setEnabled(false);
					hijackedValue.setBackground(versionLabelValue.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				}

			}
			performRefresh();
		} else {
			Label noElementLabel = new Label(composite, SWT.NONE);
			noElementLabel.setText("The selected resource is not a clearcase element");
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
		addSeparator(composite);
		addSecondSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performRefresh() {
		StateCache cache = StateCacheFactory.getInstance().get((IResource) getElement());
		if (cache.isDerivedObject()) {
			versionLabelValue.setText("derived object");
		} else {
			if (versionLabelValue != null) {
				String version = cache.getVersion();
				if (cache.isCheckedOut())
					version = version.replaceFirst("/[0-9]+$", "/CHECKEDOUT");
				versionLabelValue.setText(version);
			}

			if (predecessorVersionValue != null) {
				predecessorVersionValue.setText(cache.getPredecessorVersion());
			}
			if (checkedOutValue != null) {
				checkedOutValue.setText(cache.isCheckedOut() ? "yes" : "no");
			}
			if (hijackedValue != null) {
				hijackedValue.setText(cache.isHijacked() ? "yes" : "no");
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void contributeButtons(Composite parent) {
		Button refreshButton = new Button(parent, SWT.PUSH);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performRefresh();
			}
		});
		((GridLayout) parent.getLayout()).numColumns++;
	}

}