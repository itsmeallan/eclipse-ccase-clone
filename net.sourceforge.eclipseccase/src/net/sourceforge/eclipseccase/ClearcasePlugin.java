package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class ClearcasePlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ClearcasePlugin plugin;
	public static final String ID = "net.sourceforge.eclipseccase.ClearcasePlugin";
	private static boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;
	
	public static final String PREF_RESERVED_CHECKOUT = "net.sourceforge.eclipseccase.reserved_checkouts";
	public static final String PREF_RESERVED_CHECKOUT_DEFAULT = "false";
	public static final String PREF_PERSIST_STATE = "net.sourceforge.eclipseccase.persist_state";
	public static final String PREF_PERSIST_STATE_DEFAULT = "true";
	public static final String PREF_CHECKIN_COMMENT = "net.sourceforge.eclipseccase.checkin_comment";
	public static final String PREF_CHECKIN_COMMENT_DEFAULT = "true";
	public static final String PREF_CHECKOUT_COMMENT = "net.sourceforge.eclipseccase.checkout_comment";
	public static final String PREF_CHECKOUT_COMMENT_DEFAULT = "false";
	public static final String PREF_ADD_COMMENT = "net.sourceforge.eclipseccase.add_comment";
	public static final String PREF_ADD_COMMENT_DEFAULT = "true";
	public static final String PREF_CHECKOUT_ON_EDIT = "net.sourceforge.eclipseccase.checkout_on_edit";
	public static final String PREF_CHECKOUT_ON_EDIT_DEFAULT = "true";
	public static final String PREF_REFACTOR_ADDS_DIR = "net.sourceforge.eclipseccase.refactor_adds_dir";
	public static final String PREF_REFACTOR_ADDS_DIR_DEFAULT = "true";
	public static final String PREF_TEXT_VERSION_DECORATION = "net.sourceforge.eclipseccase.text_decoration";
	public static final String PREF_TEXT_VERSION_DECORATION_DEFAULT = "false";
	public static final String PREF_USE_CLEARTOOL = "net.sourceforge.eclipseccase.use_cleartool";
	public static final String PREF_USE_CLEARTOOL_DEFAULT = Boolean.toString(! isWindows);
	
	private IClearcase clearcaseImpl;
	
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
		return ClearcasePlugin.getDefault().getClearcase();
	}
	
	public IClearcase getClearcase()
	{
		if (clearcaseImpl == null)
		{
			if (isUseCleartool())
				clearcaseImpl = new ClearcaseCommand();
			else
				clearcaseImpl = new ClearcaseJNI(); 
		}
		return clearcaseImpl;
	}
	
	public void resetClearcase()
	{
			clearcaseImpl = null;
	}
	
	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(IPreferenceStore)
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(PREF_RESERVED_CHECKOUT, PREF_RESERVED_CHECKOUT_DEFAULT);
		store.setDefault(PREF_PERSIST_STATE, PREF_PERSIST_STATE_DEFAULT);
		store.setDefault(PREF_CHECKIN_COMMENT, PREF_CHECKIN_COMMENT_DEFAULT);
		store.setDefault(PREF_CHECKOUT_COMMENT, PREF_CHECKOUT_COMMENT_DEFAULT);
		store.setDefault(PREF_ADD_COMMENT, PREF_ADD_COMMENT_DEFAULT);
		store.setDefault(PREF_CHECKOUT_ON_EDIT, PREF_CHECKOUT_ON_EDIT_DEFAULT);
		store.setDefault(PREF_REFACTOR_ADDS_DIR, PREF_REFACTOR_ADDS_DIR_DEFAULT);
		store.setDefault(PREF_TEXT_VERSION_DECORATION, PREF_TEXT_VERSION_DECORATION_DEFAULT);
		store.setDefault(PREF_USE_CLEARTOOL, PREF_USE_CLEARTOOL_DEFAULT);
	}
	
	public static boolean isReservedCheckouts()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_RESERVED_CHECKOUT);
	}

	public static boolean isPersistState()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_PERSIST_STATE);
	}
	
	public static boolean isCheckinComment()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_CHECKIN_COMMENT);
	}
	
	public static boolean isCheckoutComment()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_CHECKOUT_COMMENT);
	}

	public static boolean isAddComment()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_ADD_COMMENT);
	}

	public static boolean isCheckoutOnEdit()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_CHECKOUT_ON_EDIT);
	}

	public static boolean isRefactorAddsDir()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_REFACTOR_ADDS_DIR);
	}

	public static boolean isTextVersionDecoration()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_TEXT_VERSION_DECORATION);
	}
	
	public static boolean isUseCleartool()
	{
		return getDefault().getPreferenceStore().getBoolean(PREF_USE_CLEARTOOL);
	}

	/**
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException
	{
		super.startup();
		StateCacheFactory cacheFactory = StateCacheFactory.getInstance();
        ISavedState lastState =
            ResourcesPlugin.getWorkspace().addSaveParticipant(this, cacheFactory);
        cacheFactory.load(lastState);
	}

}
