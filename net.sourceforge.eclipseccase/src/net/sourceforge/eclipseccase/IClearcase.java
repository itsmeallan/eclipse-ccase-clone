package net.sourceforge.eclipseccase;

public interface IClearcase
{
	public static class Status
	{
		public boolean status;
		public String message;

		public Status(boolean status, String message)
		{
			this.status = status;
			this.message = message;
		}
	}

	/**
	 Does a clearcase checkout of the given file.  Comment can be
	 empty string.  If reserved is true, does a reserved checkout.
	 ptime preserves file timestamp
	 */
	public Status checkout(
		String file,
		String comment,
		boolean reserved,
		boolean ptime);

	/**
	 Does a clearcase checkin of the given file.  Comment can be
	 empty string.  ptime preserves file timestamp
	 */
	public Status checkin(String file, String comment, boolean ptime);

	/**
	 Does a clearcase uncheckout of the given file.  If keep is true,
	 the file is copied to a ".keep" file
	 */
	public Status uncheckout(String file, boolean keep);

	/**
	 Adds the given file to clearcase source control.  This requires
	 the parent directory to be under version control and checked
	 out.  The isdirectory flag causes creation of a directory element
	 when true.  Comment can be empty string.
	 */
	public Status add(String file, String comment, boolean isdirectory);

	/**
	 Removes the given file from clearcase source control (rmname NOT
	 rmelem).  This requires the parent directory to be under version
	 control and checked out.  Comment can be empty string.
	 */
	public Status delete(String file, String comment);

	/** Moves file to newfile.  The parent directories of both file and newfile must be checked out.  Comment can be empty string. */
	public Status move(String file, String newfile, String comment);

	/** Gets the view tag name for the view associated with file. */
	public Status getViewName(String file);

	/**
	 Executes the command "cmd" just like a command line "cleartool cmd".
	 */
	public Status cleartool(String cmd);

	/**
	 Returns true if the file is under version control and checked
	 out
	 */
	public boolean isCheckedOut(String file);

	/**
	 Returns true if the file is under clearcase version control
	 */
	public boolean isElement(String file);

	/**
	 Returns true if the file is checked out and different from its
	 predecessor
	 */
	public boolean isDifferent(String file);

	/**
	 Returns true if the file is under version control and part of a snapshot view
	 */
	public boolean isSnapShot(String file);

	/**
	 Returns true if the file is under version control and hijacked from a snapshot view
	 */
	public boolean isHijacked(String file);
}