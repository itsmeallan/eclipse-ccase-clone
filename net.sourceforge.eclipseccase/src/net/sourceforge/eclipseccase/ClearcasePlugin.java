package net.sourceforge.eclipseccase;

import net.sourceforge.clearcase.simple.ClearcaseException;
import net.sourceforge.clearcase.simple.ClearcaseFactory;
import net.sourceforge.clearcase.simple.IClearcase;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class ClearcasePlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ClearcasePlugin plugin;
	public static final String ID = "net.sourceforge.eclipseccase.ClearcasePlugin";
	private static boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;
	
	private IClearcase clearcaseImpl;
	private IResourceChangeListener updateListener = new IResourceChangeListener()
	{
		public void resourceChanged(IResourceChangeEvent event)
		{
			if (isRefreshOnChange() && event.getType() == IResourceChangeEvent.POST_CHANGE)
			{
				try
				{
					event.getDelta().accept(new IResourceDeltaVisitor()
					{
						public boolean visit(IResourceDelta delta)
							throws CoreException
						{
							StateCache cache =
							   StateCacheFactory.getInstance().get(delta.getResource());
							cache.updateAsync(true, false);
							return true;
						}
					});
				}
				catch (CoreException e)
				{
					log(IStatus.ERROR, "Unable to do a quick update of resource", null);
				}
			}
		}
	};

	/**
	 * The constructor.
	 */
	public ClearcasePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static ClearcasePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static void log(int severity, String message, Throwable ex)
	{
			ILog log = ClearcasePlugin.getDefault().getLog();
			log.log(new Status(severity, ClearcasePlugin.ID, severity, message ,ex));
	}

	public static IClearcase getEngine()
	{
		IClearcase impl = null;
		try
		{
			impl = ClearcasePlugin.getDefault().getClearcase();
		}
		catch (CoreException e)
		{
			log(IStatus.ERROR, "Could not get a clearcase engine", e);
		}
		return impl;
	}
	
	public IClearcase getClearcase() throws CoreException
	{
		try
		{
			if (clearcaseImpl == null)
			{
				if (isUseCleartool())
					clearcaseImpl = ClearcaseFactory.getInstance().createInstance(ClearcaseFactory.CLI);
				else
					clearcaseImpl = ClearcaseFactory.getInstance().getDefault();
			}
			return clearcaseImpl;
		}
		catch (ClearcaseException e)
		{
			throw new CoreException(new Status(IStatus.ERROR, ClearcasePlugin.ID, TeamException.UNABLE, "Could not retrieve a valid clearcase engine", e));
		}
	}
	
	public void resetClearcase()
	{
		if (clearcaseImpl != null)
		{
			clearcaseImpl.destroy();
			clearcaseImpl = null;
		}
	}
	
	protected void initializeDefaultPreferences(IPreferenceStore store)
	{
		// General preferences
		store.setDefault(IPreferenceConstants.RESERVED_CHECKOUT, false);
		store.setDefault(IPreferenceConstants.PERSIST_STATE, true);
		store.setDefault(IPreferenceConstants.REFRESH_ON_CHANGE, true);
		store.setDefault(IPreferenceConstants.CHECKIN_COMMENT, true);
		store.setDefault(IPreferenceConstants.CHECKIN_PRESERVE_TIME, true);
		store.setDefault(IPreferenceConstants.CHECKOUT_COMMENT, false);
		store.setDefault(IPreferenceConstants.ADD_COMMENT, true);
		store.setDefault(IPreferenceConstants.CHECKOUT_ON_EDIT, true);
		store.setDefault(IPreferenceConstants.REFACTOR_ADDS_DIR, true);
		store.setDefault(IPreferenceConstants.USE_CLEARTOOL, ! isWindows);

		// Decorator preferences
		store.setDefault(IPreferenceConstants.TEXT_VIEW_DECORATION, true);
		store.setDefault(IPreferenceConstants.TEXT_VERSION_DECORATION, false);
		store.setDefault(IPreferenceConstants.TEXT_DIRTY_DECORATION, false);
		store.setDefault(IPreferenceConstants.DEEP_DECORATIONS, false);
	}
	
	public static boolean isReservedCheckouts()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.RESERVED_CHECKOUT);
	}

	public static boolean isPersistState()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.PERSIST_STATE);
	}
	
	public static boolean isRefreshOnChange()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.REFRESH_ON_CHANGE);
	}
	
	public static boolean isCheckinComment()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.CHECKIN_COMMENT);
	}
	
	public static boolean isCheckinPreserveTime()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.CHECKIN_PRESERVE_TIME);
	}

	public static boolean isCheckoutComment()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.CHECKOUT_COMMENT);
	}

	public static boolean isAddComment()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.ADD_COMMENT);
	}

	public static boolean isCheckoutOnEdit()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.CHECKOUT_ON_EDIT);
	}

	public static boolean isRefactorAddsDir()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.REFACTOR_ADDS_DIR);
	}

	public static boolean isTextViewDecoration()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.TEXT_VIEW_DECORATION);
	}
	
	public static boolean isTextVersionDecoration()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.TEXT_VERSION_DECORATION);
	}
	
	public static boolean isTextDirtyDecoration()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.TEXT_DIRTY_DECORATION);
	}
	
	public static boolean isDeepDecoration()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.DEEP_DECORATIONS);
	}
	
	public static boolean isUseCleartool()
	{
		return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.USE_CLEARTOOL);
	}

	public void startup() throws CoreException
	{
		super.startup();
		
		// Disables plugin if clearcase is not available (throws CoreEx)
		getClearcase();
	
		StateCacheFactory cacheFactory = StateCacheFactory.getInstance();
        ISavedState lastState =
            ResourcesPlugin.getWorkspace().addSaveParticipant(this, cacheFactory);
        cacheFactory.load(lastState);

		getWorkspace().addResourceChangeListener(updateListener, IResourceChangeEvent.POST_CHANGE);

	}

	public void shutdown() throws CoreException
	{
		super.shutdown();
		resetClearcase();

		getWorkspace().removeResourceChangeListener(updateListener);
	}

}
