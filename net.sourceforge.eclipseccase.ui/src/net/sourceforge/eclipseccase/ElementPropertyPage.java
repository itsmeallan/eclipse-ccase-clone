package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class ElementPropertyPage extends PropertyPage
{

	private Text predecessorVersionValue;

	private Text versionLabelValue;
	private Button checkedOutValue;
	private Button hijackedValue;
	private Button dirtyValue;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public ElementPropertyPage()
	{
		super();
		noDefaultAndApplyButton();
	}

	private void addFirstSection(Composite parent)
	{
		Composite composite = createDefaultComposite(parent);

		//Label for path field
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText("Native Path:");

		// Path text field
		Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		pathValueText.setText(
			((IResource) getElement()).getLocation().toOSString());
	}

	private void addSeparator(Composite parent)
	{
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(Composite parent)
	{
		Composite composite = createDefaultComposite(parent);
		IResource resource = (IResource) getElement();
		StateCache cache = StateCacheFactory.getInstance().get(resource);		

		if (cache.hasRemote())
		{
			Label versionLabel = new Label(composite, SWT.NONE);
			versionLabel.setText("Version:");
			versionLabelValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		
			Label predecessorVersionLabel = new Label(composite, SWT.NONE);
			predecessorVersionLabel.setText("Predecessor Version:");
			predecessorVersionValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		
			Label checkedOutLabel = new Label(composite, SWT.NONE);
			checkedOutLabel.setText("Checked Out:");
			checkedOutValue = new Button(composite, SWT.CHECK);
			checkedOutValue.setEnabled(false);
	
			if (cache.isSnapShot())
			{
				Label hijackedLabel = new Label(composite, SWT.NONE);
				hijackedLabel.setText("Hijacked:");
				hijackedValue = new Button(composite, SWT.CHECK);
				hijackedValue.setEnabled(false);
			}
			
			if (cache.isCheckedOut())
			{
				Label dirtyLabel = new Label(composite, SWT.NONE);
				dirtyLabel.setText("Contents differ from predecessor:");
				dirtyValue = new Button(composite, SWT.CHECK);
				dirtyValue.setEnabled(false);
				
			}
			performRefresh();
		}
		else
		{
			Label notRemoteLabel = new Label(composite, SWT.NONE);
			notRemoteLabel.setText("The selected resource is not a clearcase element");
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent)
	{
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

	private Composite createDefaultComposite(Composite parent)
	{
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

	protected void performRefresh()
	{
		StateCache cache = StateCacheFactory.getInstance().get((IResource) getElement());
		if (versionLabelValue != null)
			versionLabelValue.setText(cache.getVersion());
		if (predecessorVersionValue != null)
			predecessorVersionValue.setText(cache.getPredecessorVersion());
		if (checkedOutValue != null)
			checkedOutValue.setSelection(cache.isCheckedOut());
		if (hijackedValue != null)
			hijackedValue.setSelection(cache.isHijacked());
	}

	public void dispose()
	{
		super.dispose();
	}

	protected void contributeButtons(Composite parent)
	{
		Button refreshButton = new Button(parent, SWT.PUSH);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				performRefresh();
			}
		});
		((GridLayout) parent.getLayout()).numColumns++;
	}

}