package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class CheckedoutMarkerResolutionGenerator
	implements IMarkerResolutionGenerator
{

	Shell shell = ClearcasePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();	

	public CheckedoutMarkerResolutionGenerator()
	{
		super();
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	public IMarkerResolution[] getResolutions(IMarker marker)
	{
		return new IMarkerResolution[] { new UncheckoutResolution(), new CheckinResolution() };
	}

	private class CheckinResolution implements IMarkerResolution
	{
		public String getLabel()
		{
			return "Checkin";
		}
		
		public void run(IMarker marker)
		{
			String maybeComment = "";
			int maybeDepth = IResource.DEPTH_ZERO;

			if (ClearcasePlugin.isCheckinComment())
			{
				CommentDialog dlg =
					new CommentDialog(
						shell,
						"Checkin comment",
						"Enter a checkin comment",
						"",
						null);
				if (dlg.open() == CommentDialog.CANCEL)
					return;
				maybeComment = dlg.getValue();
				maybeDepth =
					dlg.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
			}

			final String comment = maybeComment;
			final int depth = maybeDepth;
			IResource resource = marker.getResource();
			ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
			provider.setComment(comment);
			try
			{
				provider.checkin(new IResource[] {resource},
								depth, null);
			}
			catch (TeamException e)
			{
				MessageDialog.openError(
							shell,
							"Clearcase Plugin",
							e.toString());
			}
		}
		
	}

	private class UncheckoutResolution implements IMarkerResolution
	{
		public String getLabel()
		{
			return "Uncheckout";
		}
		
		public void run(IMarker marker)
		{
			IResource resource = marker.getResource();
			ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
			try
			{
				provider.uncheckout(new IResource[] {resource},
								IResource.DEPTH_ZERO, null);
			}
			catch (TeamException e)
			{
				MessageDialog.openError(
							shell,
							"Clearcase Plugin",
							e.toString());
			}
		}
		
	}

}
