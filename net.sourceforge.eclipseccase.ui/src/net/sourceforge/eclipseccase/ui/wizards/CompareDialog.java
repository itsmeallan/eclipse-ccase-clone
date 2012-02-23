package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.jface.dialogs.IDialogSettings;



import java.util.ResourceBundle;
import org.eclipse.compare.*;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

/**
 * A resizable CompareDialog
 * 
 * @author mike
 *
 */
public class CompareDialog extends Dialog {

	private static final CompareConfiguration cc = new CompareConfiguration();

	private CompareViewerSwitchingPane compareViewerPane;

	private ICompareInput myInput;
	
	//Initial size.
	private static final int WIDTH_HINT = 800;

	private static final int HEIGHT_HINT = 600;

	
	protected CompareDialog(Shell shell, ResourceBundle bundle) {
		super(shell);
		//make sure dialog is resizable.
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER
                | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
		cc.setLeftEditable(false);
		cc.setRightEditable(false);
	}

	void compare(ICompareInput input) {

		myInput = input;
		cc.setLeftLabel(myInput.getLeft().getName());
		cc.setLeftImage(myInput.getLeft().getImage());

		cc.setRightLabel(myInput.getRight().getName());
		cc.setRightImage(myInput.getRight().getImage());

		if (compareViewerPane != null) {
			compareViewerPane.setInput(myInput);
		}

	}

	/*
	 * (non Javadoc) Creates SWT control tree.
	 */
	@Override
	protected synchronized Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);

		getShell().setText("Compare"); //$NON-NLS-1$
		compareViewerPane = new ViewerSwitchingPane(composite, SWT.BORDER | SWT.FLAT);
		//Set 
		GridData gridData = new GridData();
		gridData.heightHint = HEIGHT_HINT;
		gridData.widthHint = WIDTH_HINT;
		gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
		compareViewerPane.setLayoutData(gridData);
		

		if (myInput != null) {
			compareViewerPane.setInput(myInput);
		}

		applyDialogFont(composite);
		return composite;
	}

	class ViewerSwitchingPane extends CompareViewerSwitchingPane {

		ViewerSwitchingPane(Composite parent, int style) {
			super(parent, style, false);
		}

		@Override
		protected Viewer getViewer(Viewer oldViewer, Object input) {
			if (input instanceof ICompareInput)
				return CompareUI.findContentViewer(oldViewer, (ICompareInput) input, this, cc);
			return null;
		}

		@Override
		public void setImage(Image image) {
			// don't show icon
		}
	}

	
	
	

}
