package net.sourceforge.eclipseccase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;

/**
 * @author conwaym
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class StateCacheFactory implements Serializable, ISaveParticipant
{
	private static final String SAVE_FILE_NAME = "statecache";

	private static StateCacheFactory instance = new StateCacheFactory();
	private HashMap cacheMap = new HashMap();
	
	private StateCacheFactory()
	{
	}
	
	public static StateCacheFactory getInstance()
	{
		return instance;
	}
	
	
	public synchronized StateCache get(IResource resource)
	{
		String osPath = resource.getLocation().toOSString();
		StateCache cache = (StateCache) cacheMap.get(osPath);
		if (cache == null)
		{
			cache = new StateCache(osPath);
			cache.updateAsync();
			cacheMap.put(osPath, cache);
		}
		return cache;
	}
	
	
	public synchronized void set(IResource resource, StateCache cache)
	{
		String osPath = resource.getLocation().toOSString();
		cacheMap.put(osPath, cache);
	}
	
	public synchronized void remove(IResource resource)
	{
		String osPath = resource.getLocation().toOSString();
		cacheMap.remove(osPath);
	}
	
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(ISaveContext)
	 */
	public void doneSaving(ISaveContext context)
	{
         int previousSaveNumber = context.getPreviousSaveNumber();
         String oldFileName = SAVE_FILE_NAME + Integer.toString(previousSaveNumber);
         File file = ClearcasePlugin.getDefault().getStateLocation().append(oldFileName).toFile();
         file.delete();
	}

	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(ISaveContext)
	 */
	public void prepareToSave(ISaveContext context) throws CoreException
	{
	}

	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#rollback(ISaveContext)
	 */
	public void rollback(ISaveContext context)
	{
	}

	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#saving(ISaveContext)
	 */
	public void saving(ISaveContext context) throws CoreException
	{
		switch (context.getKind())
		{
			case ISaveContext.FULL_SAVE :
				try
				{
					int saveNumber = context.getSaveNumber();
					String saveFileName = SAVE_FILE_NAME + Integer.toString(saveNumber);
					IPath statePath = ClearcasePlugin.getDefault().getStateLocation().append(saveFileName);
					File stateFile = statePath.toFile();
					if (ClearcasePlugin.isPersistState())
					{
						ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(statePath.toFile()));
						os.writeObject(cacheMap);
						os.flush();
						os.close();
						context.map(new Path(SAVE_FILE_NAME), new Path(saveFileName));
						context.needSaveNumber();
					}
				}
				catch (IOException ex)
				{
					throw new CoreException(new Status(Status.WARNING, ClearcasePlugin.ID, TeamException.IO_FAILED, "Could not persist state cache", ex));
				}
				break;
			case ISaveContext.PROJECT_SAVE :
				break;
			case ISaveContext.SNAPSHOT :
				break;
		}
	}
	
	public void load(ISavedState context) throws CoreException
	{
		try
		{
			if (context != null && ClearcasePlugin.isPersistState())
			{
				String saveFileName = context.lookup(new Path(SAVE_FILE_NAME)).toString();
				File stateFile = ClearcasePlugin.getDefault().getStateLocation().append(saveFileName).toFile();
				if (stateFile.exists())
				{
					ObjectInputStream is = new ObjectInputStream(new FileInputStream(stateFile));
					cacheMap = (HashMap) is.readObject();
					is.close();
				}
			}
		}
		catch (Exception ex)
		{
			ClearcasePlugin.log(Status.WARNING, "Could not load saved clearcase state cache, resetting cache", ex);
		}
		
	}
}
