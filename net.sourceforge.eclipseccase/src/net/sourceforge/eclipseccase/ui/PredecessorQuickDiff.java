package net.sourceforge.eclipseccase.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.compare.VersionExtendedFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.IStorageDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffProviderImplementation;

public class PredecessorQuickDiff implements IQuickDiffProviderImplementation
{

	private String id;
	private IDocumentProvider fDocumentProvider;
	private IEditorInput fEditorInput;
	private boolean fDocumentRead;
	private Document fReference;
	private boolean enabled;

	public PredecessorQuickDiff()
	{
		super();
	}

	public void setActiveEditor(ITextEditor editor)
	{
		IDocumentProvider provider = null;
		IEditorInput input = null;
		if (editor != null)
		{
			provider = editor.getDocumentProvider();
			input = editor.getEditorInput();
		}

		// dispose if the editor input or document provider have changed
		// note that they may serve multiple editors
		if (provider != fDocumentProvider || input != fEditorInput)
		{
			dispose();
			fDocumentProvider = provider;
			fEditorInput = input;
		}
		setEnabled();
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	private void setEnabled()
	{
		if (fEditorInput != null
			&& fEditorInput instanceof IFileEditorInput
			&& fDocumentProvider != null)
		{
			IFileEditorInput input = (IFileEditorInput) fEditorInput;
			IFile src = input.getFile();
			ClearcaseProvider ccaseProvider = null;
			if (src != null)
				ccaseProvider = ClearcaseProvider.getProvider(src);
			if (ccaseProvider != null)
				enabled = ccaseProvider.hasRemote(src);
		}
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public IDocument getReference(IProgressMonitor monitor)
	{
		if (!fDocumentRead)
			readDocument();
		return fReference;
	}

	private void readDocument()
	{
		if (fDocumentProvider instanceof IStorageDocumentProvider
			&& fEditorInput instanceof IFileEditorInput)
		{

			IFileEditorInput input = (IFileEditorInput) fEditorInput;
			IStorageDocumentProvider provider =
				(IStorageDocumentProvider) fDocumentProvider;

			IFile src = input.getFile();
			ClearcaseProvider ccaseProvider =
				ClearcaseProvider.getProvider(src);
			String predVer = ccaseProvider.getPredecessorVersion(src);
			IFile pred = new VersionExtendedFile(src, predVer);

			InputStream stream = null;

			try
			{
				stream = pred.getContents();
			}
			catch (CoreException e)
			{
				ClearcasePlugin.log(
					IStatus.ERROR,
					"Could not get element contents for quick diff",
					e);
			}

			if (stream == null)
				return;

			String encoding = getEncoding(input, provider);

			try
			{
				fReference = createDocument(stream, encoding);
				fDocumentRead = true;
			}
			catch (IOException e)
			{
				ClearcasePlugin.log(
					IStatus.ERROR,
					"Could not read element contents for quick diff",
					e);
				return;
			}
		}
	}

	private String getEncoding(
		IFileEditorInput input,
		IStorageDocumentProvider provider)
	{
		String encoding = provider.getEncoding(input);
		if (encoding == null)
			encoding = provider.getDefaultEncoding();
		return encoding;
	}

	private static Document createDocument(
		InputStream contentStream,
		String encoding)
		throws IOException
	{
		Document result = new Document();
		Reader in = null;
		try
		{
			final int DEFAULT_FILE_SIZE = 15 * 1024;

			in =
				new BufferedReader(
					new InputStreamReader(contentStream, encoding),
					DEFAULT_FILE_SIZE);
			StringBuffer buffer = new StringBuffer(DEFAULT_FILE_SIZE);
			char[] readBuffer = new char[2048];
			int n = in.read(readBuffer);
			while (n > 0)
			{
				buffer.append(readBuffer, 0, n);
				n = in.read(readBuffer);
			}

			result.set(buffer.toString());

		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException x)
				{
				}
			}
		}
		return result;
	}

	public void dispose()
	{
		fEditorInput = null;
		fDocumentProvider = null;
		fReference = null;
		fDocumentRead = false;
		enabled = false;
	}

	public String getId()
	{
		return id;
	}

}
