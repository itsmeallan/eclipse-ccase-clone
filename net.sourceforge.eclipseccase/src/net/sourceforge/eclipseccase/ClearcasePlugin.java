package net.sourceforge.eclipseccase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.clearcase.simple.ClearcaseException;
import net.sourceforge.clearcase.simple.ClearcaseFactory;
import net.sourceforge.clearcase.simple.IClearcase;
import net.sourceforge.clearcase.simple.IClearcaseDebugger;
import net.sourceforge.eclipseccase.tools.XMLWriter;
import net.sourceforge.eclipseccase.ui.ClearcaseImages;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * The main plugin class to be used in the desktop.
 */
public class ClearcasePlugin extends AbstractUIPlugin implements IClearcaseDebugger
{
    //The shared instance.
    private static ClearcasePlugin plugin;
    public static final String ID = "net.sourceforge.eclipseccase.ClearcasePlugin";
    static final boolean isWindows =
        System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;

    private IClearcase clearcaseImpl;

    /**
     * The constructor.
     */
    public ClearcasePlugin(IPluginDescriptor descriptor)
    {
        super(descriptor);
        plugin = this;
        ClearcaseImages.initializeImages();

        String[] args = Platform.getCommandLineArgs();
        for (int i = 0; i < args.length; i++)
        {
            if ("-debugClearCase".equalsIgnoreCase(args[i].trim()))
            {
                debug = Platform.getLocation().append("clearcase.debug.log");
                break;
            }
        }
    }

    /**
     * Returns the shared instance.
     */
    public static ClearcasePlugin getDefault()
    {
        return plugin;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public static void log(int severity, String message, Throwable ex)
    {
        ILog log = ClearcasePlugin.getDefault().getLog();
        log.log(new Status(severity, ClearcasePlugin.ID, severity, message, ex));
    }

    public static void debug(String id, String message)
    {
        if (!isDebug())
            return;

        BufferedWriter debugWriter = null;
        FileWriter debugFileWriter = null;
        try
        {

            File debugFile = debug.toFile();
            debugFile.createNewFile();
            debugFileWriter = new FileWriter(debugFile, true);
            debugWriter = new BufferedWriter(debugFileWriter);
        }
        catch (Exception e)
        {
            if (null != debugFileWriter)
            {
                try
                {
                    debugFileWriter.close();
                }
                catch (IOException e1)
                {}
            }
            log(IStatus.ERROR, "Could not debug to file " + debug, e);
            debug = null;
            return;
        }

        try
        {
            debugWriter.write(id + "\t" + message + "\n");
            debugWriter.flush();
            debugWriter.close();
        }
        catch (Exception e)
        {
            if (null != debugWriter)
            {
                try
                {
                    debugWriter.close();
                }
                catch (IOException e1)
                {}
            }
            log(IStatus.ERROR, "Could not debug to file " + debug, e);
            debug = null;
        }
    }

    public static IClearcase getEngine()
    {
        IClearcase impl = null;
        try
        {
            impl = ClearcasePlugin.getDefault().getClearcase();

            if (isDebug())
                impl.setDebugger(plugin);
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
                    clearcaseImpl =
                        ClearcaseFactory.getInstance().createInstance(ClearcaseFactory.CLI);
                else
                    clearcaseImpl = ClearcaseFactory.getInstance().getDefault();
            }
            return clearcaseImpl;
        }
        catch (ClearcaseException e)
        {
            throw new CoreException(
                new Status(
                    IStatus.ERROR,
                    ClearcasePlugin.ID,
                    TeamException.UNABLE,
                    "Could not retrieve a valid clearcase engine",
                    e));
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
        store.setDefault(IPreferenceConstants.USE_CLEARTOOL, !isWindows);
        store.setDefault(IPreferenceConstants.ESCAPE_COMMENTS, false);
        store.setDefault(IPreferenceConstants.MULTILINE_COMMENTS, true);

        // Decorator preferences
        store.setDefault(IPreferenceConstants.TEXT_VIEW_DECORATION, true);
        store.setDefault(IPreferenceConstants.TEXT_VERSION_DECORATION, false);
        store.setDefault(IPreferenceConstants.TEXT_DIRTY_DECORATION, false);
        store.setDefault(IPreferenceConstants.TEXT_NEW_DECORATION, false);
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
        return getDefault().getPreferenceStore().getBoolean(
            IPreferenceConstants.CHECKIN_PRESERVE_TIME);
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
        return getDefault().getPreferenceStore().getBoolean(
            IPreferenceConstants.TEXT_VIEW_DECORATION);
    }

    public static boolean isTextVersionDecoration()
    {
        return getDefault().getPreferenceStore().getBoolean(
            IPreferenceConstants.TEXT_VERSION_DECORATION);
    }

    public static boolean isTextDirtyDecoration()
    {
        return getDefault().getPreferenceStore().getBoolean(
            IPreferenceConstants.TEXT_DIRTY_DECORATION);
    }

    public static boolean isTextNewDecoration()
    {
        return getDefault().getPreferenceStore().getBoolean(
            IPreferenceConstants.TEXT_NEW_DECORATION);
    }
    public static boolean isDeepDecoration()
    {
        return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.DEEP_DECORATIONS);
    }

    public static boolean isUseCleartool()
    {
        return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.USE_CLEARTOOL);
    }

    public static boolean isEscapeComments()
    {
        return getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.ESCAPE_COMMENTS);
    }

    public static boolean isMultiLineComments()
    {
        return getDefault().getPreferenceStore().getBoolean(
            IPreferenceConstants.MULTILINE_COMMENTS);
    }

    public void startup() throws CoreException
    {
        super.startup();

        // Disables plugin if clearcase is not available (throws CoreEx)
        getClearcase();

        StateCacheFactory cacheFactory = StateCacheFactory.getInstance();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(
            cacheFactory,
            IResourceChangeEvent.POST_CHANGE);
        ISavedState lastState =
            ResourcesPlugin.getWorkspace().addSaveParticipant(this, cacheFactory);
        if (null != lastState)
        {
            cacheFactory.load(lastState);
            lastState.processResourceChangeEvents(cacheFactory);
        }

        loadCommentHistory();
    }

    public void shutdown() throws CoreException
    {
        getWorkspace().removeResourceChangeListener(StateCacheFactory.getInstance());

        resetClearcase();

        saveCommentHistory();

        super.shutdown();

    }

    /** The previously remembered comment */
    static String[] previousComments = new String[0];

    /** maximum comments to remember */
    static final int MAX_COMMENTS = 10;

    /** file name fo the history file */
    private static final String COMMENT_HIST_FILE = "commentHistory.xml"; //$NON-NLS-1$
    /** xml element name */
    static final String ELEMENT_COMMENT = "comment"; //$NON-NLS-1$
    /** xml element name */
    static final String ELEMENT_COMMENT_HISTORY = "comments"; //$NON-NLS-1$

    /**
     * Method addComment.
     * @param string
     */
    public void addComment(String comment)
    {
        // Only add the comment if the first entry isn't the same already
        if (previousComments.length > 0 && previousComments[0].equals(comment))
            return;
        // Insert the comment as the first element
        String[] newComments = new String[Math.min(previousComments.length + 1, MAX_COMMENTS)];
        newComments[0] = comment;
        for (int i = 1; i < newComments.length; i++)
        {
            newComments[i] = previousComments[i - 1];
        }
        previousComments = newComments;
    }

    /**
     * Answer the list of comments that were previously used when committing.
     * @return String[]
     */
    public String[] getPreviousComments()
    {
        return previousComments;
    }

    /**
     * Method getCurrentComment.
     * @return String
     */
    private String getCurrentComment()
    {
        if (previousComments.length == 0)
            return ""; //$NON-NLS-1$
        return (String) previousComments[0];
    }

    private void saveCommentHistory() throws CoreException
    {
        IPath pluginStateLocation = getStateLocation();
        File tempFile = pluginStateLocation.append(COMMENT_HIST_FILE + ".tmp").toFile(); //$NON-NLS-1$
        File histFile = pluginStateLocation.append(COMMENT_HIST_FILE).toFile();
        try
        {
            XMLWriter writer =
                new XMLWriter(new BufferedOutputStream(new FileOutputStream(tempFile)));
            try
            {
                writeCommentHistory(writer);
            }
            finally
            {
                writer.close();
            }
            if (histFile.exists())
            {
                histFile.delete();
            }
            boolean renamed = tempFile.renameTo(histFile);
            if (!renamed)
            {
                throw new CoreException(
                    new Status(
                        Status.ERROR,
                        ID,
                        TeamException.UNABLE,
                        MessageFormat.format(
                            "Could not rename file '{0}'!",
                            new Object[] { tempFile.getAbsolutePath()}),
                        null));
            }
        }
        catch (IOException e)
        {
            throw new CoreException(
                new Status(
                    Status.ERROR,
                    ID,
                    TeamException.UNABLE,
                    MessageFormat.format(
                        "Could not save file '{0}'!",
                        new Object[] { histFile.getAbsolutePath()}),
                    e));
        }
    }

    private static final BASE64Decoder BASE64_DECODER = new BASE64Decoder();
    private static final BASE64Encoder BASE64_ENCODER = new BASE64Encoder();

    private void writeCommentHistory(XMLWriter writer) throws IOException
    {
        writer.startTag(ELEMENT_COMMENT_HISTORY, null, false);
        for (int i = 0; i < previousComments.length && i < MAX_COMMENTS; i++)
            writer.printSimpleTag(
                ELEMENT_COMMENT,
                BASE64_ENCODER.encode(previousComments[i].getBytes()));
        writer.endTag(ELEMENT_COMMENT_HISTORY);
    }

    private void loadCommentHistory() throws CoreException
    {
        IPath pluginStateLocation = getStateLocation().append(COMMENT_HIST_FILE);
        File file = pluginStateLocation.toFile();
        if (!file.exists())
            return;
        try
        {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            try
            {
                readCommentHistory(is);
            }
            finally
            {
                is.close();
            }
        }
        catch (IOException e)
        {
            getLog().log(
                new Status(
                    Status.ERROR,
                    ID,
                    TeamException.UNABLE,
                    "Error while reading config file: " + e.getLocalizedMessage(),
                    e));
        }
        catch (CoreException e)
        {
            getLog().log(e.getStatus());
        }
    }

    private void readCommentHistory(InputStream stream) throws CoreException
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            InputSource source = new InputSource(stream);
            Document document = parser.parse(source);
            NodeList list = document.getChildNodes();
            for (int i = 0; i < list.getLength(); i++)
            {
                Node node = list.item(i);
                if (node instanceof Element)
                {
                    if (ELEMENT_COMMENT_HISTORY.equals(((Element) node).getTagName()))
                    {
                        NodeList commentNodes =
                            ((Element) node).getElementsByTagName(ELEMENT_COMMENT);
                        ArrayList comments = new ArrayList(MAX_COMMENTS);
                        for (int j = 0; j < commentNodes.getLength() && j < MAX_COMMENTS; j++)
                        {
                            Node commentNode = commentNodes.item(j);
                            if (commentNode instanceof Element && commentNode.hasChildNodes())
                            {
                                // the first child is expected to be a text node with our comment
                                String comment = commentNode.getFirstChild().getNodeValue();
                                if (null != comment)
                                    comments.add(new String(BASE64_DECODER.decodeBuffer(comment)));
                            }
                        }
                        previousComments = (String[]) comments.toArray(new String[comments.size()]);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new CoreException(
                new Status(
                    Status.ERROR,
                    ID,
                    TeamException.UNABLE,
                    "Error reading config file!",
                    e));
        }
    }

    private static IPath debug = null;

    public static boolean isDebug()
    {
        return null != debug;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.clearcase.simple.IClearcaseDebugger#debugClearcase(java.lang.String, java.lang.String)
     */
    public void debugClearcase(String id, String message)
    {
        debug(id, message);
    }

}
