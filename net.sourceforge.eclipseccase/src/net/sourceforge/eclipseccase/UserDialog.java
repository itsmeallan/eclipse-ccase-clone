/**
 * 
 */
package net.sourceforge.eclipseccase;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * Generic dialog for presenting messages to end user. To be used together with:
 * 
 * @author mikael petterson
 *
 */
public class UserDialog implements Runnable {

	

	private int result;
	private Shell shell;
	private String title;
	private Image image;
	private String message;
	private int imageType;
	private String [] buttonLabels;
	private int defaultValue;
	
	

	public UserDialog(Shell parentShell, String dialogTitle,
            Image dialogTitleImage, String dialogMessage, int dialogImageType,
            String[] dialogButtonLabels, int defaultIndex) {
		this.shell = parentShell;
		this.title = dialogTitle;
		this.image = dialogTitleImage;
		this.imageType = dialogImageType;
		this.message = dialogMessage;
		this.buttonLabels = dialogButtonLabels;
		this.defaultValue = defaultIndex;
		
	}

	@Override
	public void run() {
		MessageDialog dialog = new MessageDialog(shell ,title,
	            image, message, imageType,
	            buttonLabels, defaultValue);
		dialog.open();
		result = dialog.getReturnCode();
		
	}

	public int getResult() {
		return result;
	}

	

}
