package net.sourceforge.eclipseccase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class ClearcaseCommand
{
	private String cmd;
	private Process cleartool;
	private BufferedReader stdout;
	private BufferedReader stderr;
	private Writer stdin;

	ClearcaseCommand()
	{
	}

	private void startProcess() throws Exception
	{
		cleartool = Runtime.getRuntime().exec("cleartool.exe");
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

	public void execute(String cmd) throws Exception
	{
		if (cleartool == null)
			startProcess();
		stdin.write(cmd);
	}

}