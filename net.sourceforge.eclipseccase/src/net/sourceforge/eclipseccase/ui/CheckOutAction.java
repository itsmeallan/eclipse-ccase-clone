package net.sourceforge.eclipseccase.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.internal.simpleAccess.SimpleAccessOperations;


public class CheckOutAction extends org.eclipse.team.ui.actions.CheckOutAction {
	
	public CheckOutAction()
	{
		super();
	}
	
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
			SimpleAccessOperations ops = provider.getSimpleAccess();
			if (provider == null || ops == null) return false;
			if (!ops.hasRemote(resource)) return false;
			if(ops.isCheckedOut(resource)) return false;
		}
		return true;
	}
	
}
