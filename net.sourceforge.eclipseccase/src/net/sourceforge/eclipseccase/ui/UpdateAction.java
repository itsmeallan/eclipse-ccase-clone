package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations;

public class UpdateAction extends org.eclipse.team.ui.actions.GetAction
{
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			ClearcaseProvider provider =
				(ClearcaseProvider) RepositoryProvider.getProvider(resource.getProject());
			if (provider == null)
				return false;
			if (!provider.hasRemote(resource))
				return false;
			if (!provider.isSnapShot(resource))
				return false;
		}
		return true;
	}
}