package net.sourceforge.eclipseccase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipseccase.IClearcase.Status;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ClearcaseDummy implements IClearcase
{
	private static final String SAVE_FILE_NAME = "clearcasedummy";

	private static final int info = org.eclipse.core.runtime.Status.INFO;
	private static final int err = org.eclipse.core.runtime.Status.ERROR;
	
	private Map eltMap = new HashMap();

	static class Element implements Serializable
	{
		boolean isElt = true;
		boolean checkedOut = false;
	}
	
	public ClearcaseDummy()
	{
		super();
		load();
	}
	
	private void log(int status, String msg, Throwable t)
	{
		PrintStream out;
		if (status == err)
			out = System.err;
		else
			out = System.out;
		if (t != null)
			msg += ": " + t;
		out.println("ClearcaseDummy: " + msg);
	}
	
	private Element getElt(String file)
	{
		if (! isElt(file))
			throw new IllegalArgumentException("Not an element");
		return (Element) eltMap.get(file);
	}
	
	private boolean isElt(String file)
	{
		boolean elt = eltMap.containsKey(file);
		// Always make Projects be in clearcase for the dummy as
		// we don't have a way to add em otherwise
		if (! elt)
		{
			File f = new File(file);
			IPath path = new Path(file);
			if (f.isDirectory())
			{
				IResource resource = ClearcasePlugin.getWorkspace().getRoot().getContainerForLocation(path);
				if (resource.getType() == IResource.PROJECT)
				{
					elt = true;
					eltMap.put(file, new Element());
				}
			}
		}
		return elt;
	}

	private void makeElt(String file)
	{
		Element elt = new Element();
		elt.checkedOut = true;
		eltMap.put(file, elt);
	}

	private void removeElt(String file)
	{
		eltMap.remove(file);
	}

	public Status checkout(
		String file,
		String comment,
		boolean reserved,
		boolean ptime)
	{
		String msg =
			"Checkout: '"
				+ file
				+ "', '"
				+ comment
				+ "', reserved: "
				+ reserved
				+ ", ptime: "
				+ ptime;
		log(info, msg, null);
		try
		{
			getElt(file).checkedOut = true;
		}
		catch (Throwable ex)
		{
			return new Status(false, msg + ", ex: " + ex);
		}
		return new Status(true, msg);
	}

	public Status checkin(String file, String comment, boolean ptime)
	{
		String msg =
			"Checkin: '" + file + "', '" + comment + "', ptime: " + ptime;
		log(info, msg, null);
		try
		{
			getElt(file).checkedOut = false;
		}
		catch (Throwable ex)
		{
			return new Status(false, msg + ", ex: " + ex);
		}
		return new Status(true, msg);
	}

	public Status uncheckout(String file, boolean keep)
	{
		String msg = "Uncheckout: '" + file + "', keep: " + keep;
		log(info, msg, null);
		try
		{
			getElt(file).checkedOut = false;
		}
		catch (Throwable ex)
		{
			return new Status(false, msg + ", ex: " + ex);
		}
		return new Status(true, msg);
	}

	public Status add(String file, String comment, boolean isdirectory)
	{
		String msg =
			"Add: '"
				+ file
				+ "', '"
				+ comment
				+ "', isdirectory: "
				+ isdirectory;
		log(info, msg, null);
		try
		{
			makeElt(file);
			if (isdirectory)
				new File(file).mkdir();
		}
		catch (Throwable ex)
		{
			return new Status(false, msg + ", ex: " + ex);
		}
		return new Status(true, msg);
	}

	public Status delete(String file, String comment)
	{
		String msg = "Delete: '" + file + "', '" + comment + "'";
		log(info, msg, null);
		try
		{
			getElt(file);
			removeElt(file);
			new File(file).delete();
		}
		catch (Throwable ex)
		{
			return new Status(false, msg + ", ex: " + ex);
		}
		return new Status(true, msg);
	}

	public Status move(String file, String newfile, String comment)
	{
		String msg =
			"Checkout: '" + file + "', '" + newfile + "', '" + comment + "'";
		log(info, msg, null);
		
		try
		{
			getElt(file);
			new File(file).renameTo(new File(newfile));
		}
		catch (Throwable ex)
		{
			return new Status(false, msg + ", ex: " + ex);
		}
		return new Status(true, msg);
	}

	public Status getViewName(String file)
	{
		String msg = "getViewName: '" + file + "'";
		log(info, msg, null);
		try
		{
			getElt(file);
		}
		catch (Throwable ex)
		{
			return new Status(false, msg + ", ex: " + ex);
		}
		return new Status(true, "MyViewName");
	}

	public Status cleartool(String cmd)
	{
		String msg = "Cleartool: '" + cmd + "'";
		log(info, msg, null);
		return new Status(true, msg);
	}

	public boolean isCheckedOut(String file)
	{
		return getElt(file).checkedOut;
	}

	public boolean isElement(String file)
	{
		return isElt(file);
	}

	public boolean isDifferent(String file)
	{
		return isCheckedOut(file);
	}

	public boolean isSnapShot(String file)
	{
		String msg = "isSnapShot: '" + file + "'";
		log(info, msg, null);
		return false;
	}

	public boolean isHijacked(String file)
	{
		String msg = "isHijacked: '" + file + "'";
		log(info, msg, null);
		return false;
	}

	public void destroy()
	{
		save();
	}

	public void save()
	{
		try
		{
			String saveFileName = SAVE_FILE_NAME;
			IPath statePath = ClearcasePlugin.getDefault().getStateLocation().append(saveFileName);
			File stateFile = statePath.toFile();
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(statePath.toFile()));
			os.writeObject(eltMap);
			os.flush();
			os.close();
		}
		catch (Exception ex)
		{
			log(err, "Could not persist clearcase dummy element state", ex);
		}
	}
	
	public void load()
	{
		try
		{
			String saveFileName = SAVE_FILE_NAME;
			File stateFile = ClearcasePlugin.getDefault().getStateLocation().append(saveFileName).toFile();
			if (stateFile.exists())
			{
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(stateFile));
				eltMap = (Map) is.readObject();
				is.close();
			}
		}
		catch (Exception ex)
		{
			log(err, "Could not load saved clearcase dummy state", ex);
		}
		
	}

}
