package net.sourceforge.eclipseccase;


public class ClearcaseJNI implements IClearcase
{

	public ClearcaseJNI()
	{
	}

	public void destroy()
	{
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#add(String, String, boolean)
	 */
	public Status add(String file, String comment, boolean isdirectory)
	{
		return jniadd(file, comment, isdirectory);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#checkin(String, String, boolean)
	 */
	public Status checkin(String file, String comment, boolean ptime)
	{
		return jnicheckin(file, comment, ptime);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#checkout(String, String, boolean, boolean)
	 */
	public Status checkout(String file, String comment, boolean reserved, boolean ptime)
	{
		return jnicheckout(file, comment, reserved, ptime);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#cleartool(String)
	 */
	public Status cleartool(String cmd)
	{
		return jnicleartool(cmd);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#delete(String, String)
	 */
	public Status delete(String file, String comment)
	{
		return jnidelete(file, comment);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#getViewName(String)
	 */
	public Status getViewName(String file)
	{
		return jnigetViewName(file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isCheckedOut(String)
	 */
	public boolean isCheckedOut(String file)
	{
		return jniisCheckedOut(file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isDifferent(String)
	 */
	public boolean isDifferent(String file)
	{
		return jniisDifferent(file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isElement(String)
	 */
	public boolean isElement(String file)
	{
		return jniisElement(file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isHijacked(String)
	 */
	public boolean isHijacked(String file)
	{
		return jniisHijacked(file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isSnapShot(String)
	 */
	public boolean isSnapShot(String file)
	{
		return jniisSnapShot(file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#move(String, String, String)
	 */
	public Status move(String file, String newfile, String comment)
	{
		return jnimove(file, newfile, comment);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#uncheckout(String, boolean)
	 */
	public Status uncheckout(String file, boolean keep)
	{
		return jniuncheckout(file, keep);
	}

	private static native Status jnicheckout(String file, String comment, boolean reserved, boolean ptime);
	private static native Status jnicheckin(String file, String comment, boolean ptime);
	private static native Status jniuncheckout(String file, boolean keep);
	private static native Status jniadd(String file, String comment, boolean isdirectory);
	private static native Status jnidelete(String file, String comment);
	private static native Status jnimove(String file, String newfile, String comment);
	private static native Status jnigetViewName(String file);
	private static native Status jnicleartool(String cmd);
	private static native boolean jniisCheckedOut(String file);
	private static native boolean jniisElement(String file);
	private static native boolean jniisDifferent(String file);
	private static native boolean jniisSnapShot(String file);
	private static native boolean jniisHijacked(String file);
	private static native void initialize();


	/** For testing puposes only */
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("Usage: Clearcase existing_ccase_elt nonexisting_ccase_elt");
			System.exit(1);
		}
		String file = args[0];
		System.out.println("isElement: " + jniisElement(file));
		System.out.println("isCheckedOut: " + jniisCheckedOut(file));
		System.out.println("checkout: " + jnicheckout(file, "", false, true).message);
		System.out.println("isCheckedOut: " + jniisCheckedOut(file));
		System.out.println("uncheckout: " + jniuncheckout(file, false).message);
		System.out.println("isCheckedOut: " + jniisCheckedOut(file));

		if (args.length > 1)
		{
			String newfile = args[1];
			System.out.println("isElement: " + jniisElement(newfile));
			System.out.println("add: " + jniadd(newfile, "", false).message);
			System.out.println("isElement: " + jniisElement(newfile));
			System.out.println("checkin: " + jnicheckin(newfile, "", true).message);
			System.out.println("delete: " + jnidelete(newfile, "").message);
			System.out.println("isElement: " + jniisElement(newfile));
		}
	}

	static {
		System.loadLibrary("ccjni");
		initialize();
	}



}