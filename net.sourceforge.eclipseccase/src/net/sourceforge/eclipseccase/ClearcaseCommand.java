package net.sourceforge.eclipseccase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.sourceforge.eclipseccase.IClearcase.Status;

public class ClearcaseCommand implements IClearcase
{
	private String prompt = "cleartool> ";
	private Process cleartool;
	private BufferedReader stdout;
	private BufferedReader stderr;
	private Writer stdin;

	ClearcaseCommand()
	{
	}

	private boolean isRunning()
	{
		boolean running = false;
		if (cleartool != null)
		{
			try
			{
				cleartool.exitValue();
			}
			catch (IllegalThreadStateException ex)
			{
				running = true;
			}
		}
		return running;
	}

	private void validateProcess() throws Exception
	{
		if (! isRunning())
		{
			cleartool = Runtime.getRuntime().exec("cleartool");
			stdout = new BufferedReader(new InputStreamReader(new BufferedInputStream(cleartool.getInputStream())));
			stderr = new BufferedReader(new InputStreamReader(new BufferedInputStream(cleartool.getErrorStream())));
			stdin = new OutputStreamWriter(cleartool.getOutputStream());
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				public void run()
				{
					cleartool.destroy();
				}
			});
		}
	}

	private Status execute(String cmd)
	{
		Status result = null;
		try
		{
			validateProcess();
			stdin.write(cmd);
			stdin.write("\n");
			stdin.flush();
			return readOutput();
		}
		catch (Exception ex)
		{
			return new Status(false, "Could not execute command due to unexpected exception: " + ex);
		}
	}

	private Status readOutput()
	{
		int timeout = 10;
		int count = 0;
		char[] buf = new char[4096];
		StringBuffer out = new StringBuffer();
		StringBuffer err = new StringBuffer();
		StringBuffer msg = new StringBuffer();
		boolean status = false;
		
		try
		{
			for(int iter = 0; iter < timeout; iter++)
			{
				if (stdout.ready())
				{
					count = stdout.read(buf);
					out.append(buf, 0, count);
				}
				if (stderr.ready())
				{
					count = stderr.read(buf);
					err.append(buf, 0, count);
				}
				if (out.toString().endsWith(prompt))
				{
					out.delete(out.length() - prompt.length(), out.length());
					status = true;
					break;
				}
			}
		}
		catch (IOException ex)
		{
			err.append("IOException while trying to parse cleartool output: ");
			err.append(ex);
			status = false;
		}
		
		if (out.length() > 0)
		{
			msg.append(out);
		}
		if (err.length() > 0)
		{
			msg.append(err);
			status = false;
		}
			
		return new Status(status, msg.toString());
	}

	private String readStderr()
	{
		char[] buf = new char[4096];
		StringBuffer result = new StringBuffer();
		try
		{
			while (stdout.ready())
			{
				int count = stdout.read(buf);
				result.append(buf, 0, count);
			}
		}
		catch (IOException e)
		{
		}
		return result.toString();
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#add(String, String, boolean)
	 */
	public Status add(String file, String comment, boolean isdirectory)
	{
		if (isdirectory)
			return execute("mkdir -c \"" + comment + "\" " + file);
		else
			return execute("mkelem -c \"" + comment + "\" " + file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#checkin(String, String, boolean)
	 */
	public Status checkin(String file, String comment, boolean ptime)
	{
		String ptimeFlag = ptime ? "-ptime " : "";
		return execute("checkin -c \"" + comment + "\" " + ptimeFlag + file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#checkout(String, String, boolean, boolean)
	 */
	public Status checkout(
		String file,
		String comment,
		boolean reserved,
		boolean ptime)
	{
		String ptimeFlag = ptime ? "-ptime " : "";
		String resFlag = reserved ? "-reserved " : "-unreserved ";
		return execute("checkout -c \"" + comment + "\" " + ptimeFlag + resFlag + file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#cleartool(String)
	 */
	public Status cleartool(String cmd)
	{
		return execute(cmd);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#delete(String, String)
	 */
	public Status delete(String file, String comment)
	{
		return execute("rmname -c \"" + comment + "\" " + file);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#getViewName(String)
	 */
	public Status getViewName(String file)
	{
		return new Status(true, "unimplemented");
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isCheckedOut(String)
	 */
	public boolean isCheckedOut(String file)
	{
		Status ret = execute("describe -fmt \"%f\" " + file);
		if (ret.status && ret.message.trim().length() > 0)
			return true;
		else
			return false;
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isDifferent(String)
	 */
	public boolean isDifferent(String file)
	{
		return false;
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isElement(String)
	 */
	public boolean isElement(String file)
	{
		Status ret = execute("describe -fmt \"%Vn\" " + file);
		if (ret.status && ret.message.trim().length() > 0)
			return true;
		else
			return false;
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isHijacked(String)
	 */
	public boolean isHijacked(String file)
	{
		return false;
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#isSnapShot(String)
	 */
	public boolean isSnapShot(String file)
	{
		return false;
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#move(String, String, String)
	 */
	public Status move(String file, String newfile, String comment)
	{
		return execute("move -c \"" + comment + "\" " + file + " " + newfile);
	}

	/**
	 * @see net.sourceforge.eclipseccase.IClearcase#uncheckout(String, boolean)
	 */
	public Status uncheckout(String file, boolean keep)
	{
		String flag = keep ? "-keep " : "-rm ";
		return execute("uncheckout " + flag + file);
	}

}