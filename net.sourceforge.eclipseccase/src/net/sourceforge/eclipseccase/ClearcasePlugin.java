/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *******************************************************************************/
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
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.clearcase.simple.ClearcaseException;
import net.sourceforge.clearcase.simple.ClearcaseFactory;
import net.sourceforge.clearcase.simple.IClearcase;
import net.sourceforge.clearcase.simple.IClearcaseDebugger;
import net.sourceforge.eclipseccase.tools.XMLWriter;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.team.core.TeamException;
import org.osgi.framework.BundleContext;
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
public class ClearcasePlugin extends Plugin implements IClearcaseDebugger {

    private static final BASE64Decoder BASE64_DECODER = new BASE64Decoder();

    private static final BASE64Encoder BASE64_ENCODER = new BASE64Encoder();

    /** job family for all clearcase operations */
    public static final Object FAMILY_CLEARCASE_OPERATION = new Object();

    /** the scheduling rule for the whole clearcase engine */
    public static final ISchedulingRule RULE_CLEARCASE_ENGING = new ISchedulingRule() {

        public boolean contains(ISchedulingRule rule) {
            return RULE_CLEARCASE_ENGING == rule;
        }

        public boolean isConflicting(ISchedulingRule rule) {
            return RULE_CLEARCASE_ENGING == rule;
        }
    };

    /** file name fo the history file */
    private static final String COMMENT_HIST_FILE = "commentHistory.xml"; //$NON-NLS-1$

    private static IPath debug = null;

    /** xml element name */
    static final String ELEMENT_COMMENT = "comment"; //$NON-NLS-1$

    /** xml element name */
    static final String ELEMENT_COMMENT_HISTORY = "comments"; //$NON-NLS-1$

    /** maximum comments to remember */
    static final int MAX_COMMENTS = 10;

    /** the shared instance */
    private static ClearcasePlugin plugin;

    /** the plugin id */
    public static final String PLUGIN_ID = "net.sourceforge.eclipseccase"; //$NON-NLS-1$

    /** The previously remembered comment */
    static LinkedList previousComments = new LinkedList();

    /** constant (value <code>UTF-8</code>) */
    public static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    /** debug option */
    private static final String DEBUG_OPTION_PROVIDER = ClearcasePlugin.PLUGIN_ID
            + "/debug/provider"; //$NON-NLS-1$

    /** debug option */
    private static final String DEBUG_OPTION_PROVIDER_IGNORED_RESOURCES = ClearcasePlugin.PLUGIN_ID
            + "/debug/provider/ignoredResources"; //$NON-NLS-1$

    /** debug option */
    private static final String DEBUG_OPTION_PLUGIN = ClearcasePlugin.PLUGIN_ID
            + "/debug/plugin"; //$NON-NLS-1$

    /** debug option */
    private static final String DEBUG_OPTION_STATE_CACHE = ClearcasePlugin.PLUGIN_ID
            + "/debug/stateCache"; //$NON-NLS-1$

    /** indicates if debugging is enabled */
    public static boolean DEBUG = false;

    /**
     * Configures debug settings.
     */
    static void configureDebugOptions() {
        if (ClearcasePlugin.getInstance().isDebugging()) {

            if (getDebugOption(DEBUG_OPTION_PROVIDER)) {
                trace("debugging " + DEBUG_OPTION_PROVIDER); //$NON-NLS-1$
                ClearcasePlugin.DEBUG_PROVIDER = true;
            }

            if (getDebugOption(DEBUG_OPTION_PROVIDER_IGNORED_RESOURCES)) {
                trace("debugging " + DEBUG_OPTION_PROVIDER_IGNORED_RESOURCES); //$NON-NLS-1$
                ClearcasePlugin.DEBUG_PROVIDER_IGNORED_RESOURCES = true;
            }

            if (getDebugOption(DEBUG_OPTION_PLUGIN)) {
                trace("debugging " + DEBUG_OPTION_PLUGIN); //$NON-NLS-1$
                ClearcasePlugin.DEBUG = true;
            }

            if (getDebugOption(DEBUG_OPTION_STATE_CACHE)) {
                trace("debugging " + DEBUG_OPTION_STATE_CACHE); //$NON-NLS-1$
                ClearcasePlugin.DEBUG_STATE_CACHE = true;
            }

            String[] args = Platform.getCommandLineArgs();
            for (int i = 0; i < args.length; i++) {
                if ("-debugClearCase".equalsIgnoreCase(args[i].trim())) { //$NON-NLS-1$
                    debug = Platform.getLocation()
                            .append("clearcase.debug.log"); //$NON-NLS-1$
                    break;
                }
            }
        }
    }

    /**
     * Returns the value of the specified debug option.
     * 
     * @param optionId
     * @return <code>true</code> if the option is enabled
     */
    static boolean getDebugOption(String optionId) {
        String option = Platform.getDebugOption(optionId);
        return option != null ? Boolean.valueOf(option).booleanValue() : false; //$NON-NLS-1$
    }

    /**
     * Prints out a trace message.
     * 
     * @param message
     */
    public static void trace(String message) {
        System.out.println("**Clearcase** " + message); //$NON-NLS-1$
    }

    /**
     * Prints out a trace message.
     * 
     * @param traceId
     * @param message
     */
    public static void trace(String traceId, String message) {
        trace("[" + traceId + "] " + message); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /** the file modification validator */
    private ClearcaseModificationHandler clearcaseModificationHandler = new ClearcaseModificationHandler();

    /**
     * Prints out a debug string.
     * 
     * @param id
     * @param message
     */
    public static void debug(String id, String message) {
        if (!isDebug()) return;

        BufferedWriter debugWriter = null;
        FileWriter debugFileWriter = null;
        try {

            File debugFile = debug.toFile();
            debugFile.createNewFile();
            debugFileWriter = new FileWriter(debugFile, true);
            debugWriter = new BufferedWriter(debugFileWriter);
        } catch (Exception e) {
            if (null != debugFileWriter) {
                try {
                    debugFileWriter.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
            log(IStatus.ERROR, Messages
                    .getString("ClearcasePlugin.error.debug") + debug, e); //$NON-NLS-1$
            debug = null;
            return;
        }

        try {
            debugWriter.write(id + "\t" + message + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            debugWriter.flush();
            debugWriter.close();
        } catch (Exception e) {
            if (null != debugWriter) {
                try {
                    debugWriter.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
            log(IStatus.ERROR, Messages
                    .getString("ClearcasePlugin.error.debug") + debug, e); //$NON-NLS-1$
            debug = null;
        }
    }

    /**
     * Returns the ClearCase engine for performing ClearCase operations.
     * <p>
     * If no engine is available <code>null</code> is returned.
     * </p>
     * 
     * @return the ClearCase engine (maybe <code>null</code>)
     */
    public static IClearcase getEngine() {
        IClearcase impl = null;
        try {
            impl = ClearcasePlugin.getInstance().getClearcase();

            if (isDebug()) impl.setDebugger(plugin);
        } catch (CoreException e) {
            log(IStatus.ERROR, Messages
                    .getString("ClearcasePlugin.error.noClearcase"), e); //$NON-NLS-1$
        }
        return impl;
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static ClearcasePlugin getInstance() {
        return plugin;
    }

    /**
     * Returns the workspace.
     * 
     * @return the workspace
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Returns the preference value for <code>ADD_AUTO</code>.
     * 
     * @return the preference value
     */
    public static boolean isAddAuto() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.ADD_AUTO);
    }

    /**
     * Returns the preference value for <code>ADD_AUTO</code>.
     * 
     * @return the preference value
     */
    public static boolean isAddWithCheckin() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.ADD_WITH_CHECKIN);
    }

    /**
     * Returns the preference value for <code>CHECKOUT_AUTO</code>.
     * 
     * @return the preference value
     */
    public static boolean isCheckoutAutoAlways() {
        return IClearcasePreferenceConstants.ALWAYS.equals(getInstance()
                .getPluginPreferences().getString(
                        IClearcasePreferenceConstants.CHECKOUT_AUTO));
    }

    /**
     * Returns the preference value for <code>CHECKOUT_AUTO</code>.
     * 
     * @return the preference value
     */
    public static boolean isCheckoutAutoNever() {
        return IClearcasePreferenceConstants.NEVER.equals(getInstance()
                .getPluginPreferences().getString(
                        IClearcasePreferenceConstants.CHECKOUT_AUTO));
    }

    /**
     * Returns the preference value for <code>CHECKOUT_LATEST</code>.
     * 
     * @return the preference value
     */
    public static boolean isCheckoutLatest() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.CHECKOUT_LATEST);
    }

    /**
     * Returns the preference value for <code>COMMENT_ADD</code>.
     * 
     * @return the preference value
     */
    public static boolean isCommentAdd() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.COMMENT_ADD);
    }

    /**
     * Returns the preference value for <code>COMMENT_ADD_NEVER_ON_AUTO</code>.
     * 
     * @return the preference value
     */
    public static boolean isCommentAddNeverOnAuto() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.COMMENT_ADD_NEVER_ON_AUTO);
    }

    /**
     * Returns the preference value for <code>COMMENT_CHECKIN</code>.
     * 
     * @return the preference value
     */
    public static boolean isCommentCheckin() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.COMMENT_CHECKIN);
    }

    /**
     * Returns the preference value for <code>COMMENT_CHECKOUT</code>.
     * 
     * @return the preference value
     */
    public static boolean isCommentCheckout() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.COMMENT_CHECKOUT);
    }

    /**
     * Returns the preference value for
     * <code>COMMENT_CHECKOUT_NEVER_ON_AUTO</code>.
     * 
     * @return the preference value
     */
    public static boolean isCommentCheckoutNeverOnAuto() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.COMMENT_CHECKOUT_NEVER_ON_AUTO);
    }

    /**
     * Indicates if debug output is enabled.
     * 
     * @return <code>true</code> if debug mode is enabled
     */
    public static boolean isDebug() {
        return null != debug;
    }

    /**
     * Returns the preference value for <code>COMMENT_ESCAPE</code>.
     * 
     * @return the preference value
     */
    public static boolean isCommentEscape() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.COMMENT_ESCAPE);
    }

    /**
     * Returns the preference value for <code>IGNORE_NEW</code>.
     * 
     * @return the preference value
     */
    public static boolean isIgnoreNew() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.IGNORE_NEW);
    }

    /**
     * Returns the preference value for <code>PRESERVE_TIMES</code>.
     * 
     * @return the preference value
     */
    public static boolean isPreserveTimes() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.PRESERVE_TIMES);
    }

    /**
     * Returns the preference value for <code>RECURSIVE</code>.
     * 
     * @return the preference value
     */
    public static boolean isRecursive() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.RECURSIVE);
    }

    /**
     * Returns the preference value for <code>CHECKOUT_RESERVED</code>.
     * 
     * @return the preference value
     */
    public static boolean isReservedCheckoutsAlways() {
        return IClearcasePreferenceConstants.ALWAYS.equals(getInstance()
                .getPluginPreferences().getString(
                        IClearcasePreferenceConstants.CHECKOUT_RESERVED));
    }

    /**
     * Returns the preference value for <code>CHECKOUT_RESERVED</code>.
     * 
     * @return the preference value
     */
    public static boolean isReservedCheckoutsIfPossible() {
        return IClearcasePreferenceConstants.IF_POSSIBLE.equals(getInstance()
                .getPluginPreferences().getString(
                        IClearcasePreferenceConstants.CHECKOUT_RESERVED));
    }

    /**
     * Returns the preference value for <code>CHECKOUT_RESERVED</code>.
     * 
     * @return the preference value
     */
    public static boolean isReservedCheckoutsNever() {
        return IClearcasePreferenceConstants.NEVER.equals(getInstance()
                .getPluginPreferences().getString(
                        IClearcasePreferenceConstants.CHECKOUT_RESERVED));
    }

    /**
     * Returns the preference value for <code>USE_CLEARTOOL</code>.
     * 
     * @return the preference value
     */
    public static boolean isUseCleartool() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.USE_CLEARTOOL);
    }

    /**
     * Returns the preference value for <code>HIDE_REFRESH_STATE_ACTIVITY</code>.
     * 
     * @return the preference value
     */
    public static boolean isHideRefreshActivity() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.HIDE_REFRESH_STATE_ACTIVITY);
    }

    /**
     * Returns the preference value for <code>USE_CLEARDLG</code>.
     * 
     * @return the preference value
     */
    public static boolean isUseClearDlg() {
        return getInstance().getPluginPreferences().getBoolean(
                IClearcasePreferenceConstants.USE_CLEARDLG);
    }

    /**
     * Logs an exception with the specified severity an message.
     * 
     * @param severity
     * @param message
     * @param ex
     *            (maybe <code>null</code>)
     */
    public static void log(int severity, String message, Throwable ex) {
        ILog log = ClearcasePlugin.getInstance().getLog();
        log.log(new Status(severity, ClearcasePlugin.PLUGIN_ID, severity,
                message, ex));
    }

    /**
     * Logs an error message with the specified exception.
     * 
     * @param message
     * @param ex
     *            (maybe <code>null</code>)
     */
    public static void log(String message, Throwable ex) {
        log(IStatus.ERROR, message, ex);
    }

    private IClearcase clearcaseImpl;

    /** debug flag */
    public static boolean DEBUG_PROVIDER = false;

    /** debug flag */
    public static boolean DEBUG_PROVIDER_IGNORED_RESOURCES = false;

    /** debug flag */
    public static boolean DEBUG_STATE_CACHE = false;

    /**
     * The constructor.
     */
    public ClearcasePlugin() {
        super();
        plugin = this;
    }

    /**
     * Method addComment.
     * 
     * @param string
     */
    public void addComment(String comment) {
        synchronized (previousComments) {
            // ensure the comment is UTF-8 encoded
            try {
                comment = new String(comment.getBytes(UTF_8));
            } catch (UnsupportedEncodingException ex) {
                return;
            }

            // remove existing comment (avoid duplicates)
            if (previousComments.contains(comment))
                    previousComments.remove(comment);

            // insert the comment as the first element
            previousComments.addFirst(comment);

            // check length
            while (previousComments.size() > MAX_COMMENTS) {
                previousComments.removeLast();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.clearcase.simple.IClearcaseDebugger#debugClearcase(java.lang.String,
     *      java.lang.String)
     */
    public void debugClearcase(String id, String message) {
        debug(id, message);
    }

    /**
     * Returns the current ClearCase engine that can be used for performing
     * ClearCase operations.
     * 
     * <p>
     * The engine type depends on the current platform and on preference
     * settings. It is cached internally. After changing the preferences you
     * have to do a reset (see {@link #resetClearcase()}.
     * </p>
     * 
     * @return the ClearCase engine
     * @throws CoreException
     *             if no engine is available
     */
    public IClearcase getClearcase() throws CoreException {
        try {
            if (clearcaseImpl == null) {
                if (DEBUG) trace("initializing clearcase engine"); //$NON-NLS-1$
                if (isUseCleartool()) {
                    if (DEBUG) trace("using cleartool engine"); //$NON-NLS-1$
                    clearcaseImpl = ClearcaseFactory.getInstance()
                            .createInstance(ClearcaseFactory.CLI);
                } else {
                    if (DEBUG) trace("using default engine"); //$NON-NLS-1$
                    clearcaseImpl = ClearcaseFactory.getInstance().getDefault();
                }
            }
            return clearcaseImpl;
        } catch (ClearcaseException e) {
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            ClearcasePlugin.PLUGIN_ID,
                            TeamException.UNABLE,
                            Messages
                                    .getString("ClearcasePlugin.error.noValidClearcase"), e)); //$NON-NLS-1$
        }
    }

    /**
     * Answer the list of comments that were previously used when committing.
     * 
     * @return String[]
     */
    public String[] getPreviousComments() {
        String[] comments = (String[]) previousComments
                .toArray(new String[previousComments.size()]);

        // encode all strings to the platform default encoding
        for (int i = 0; i < comments.length; i++) {
            comments[i] = new String(comments[i].getBytes());
        }

        return comments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugin#initializeDefaultPluginPreferences()
     */
    protected void initializeDefaultPluginPreferences() {
        Preferences pref = getPluginPreferences();

        // General preferences
        pref.setDefault(IClearcasePreferenceConstants.USE_CLEARTOOL,
                !isWindows());
        pref.setDefault(IClearcasePreferenceConstants.CACHE_TIMEOUT,
                "2");
        pref.setDefault(IClearcasePreferenceConstants.USE_CLEARDLG, false);
        pref.setDefault(IClearcasePreferenceConstants.PRESERVE_TIMES, false);
        pref.setDefault(IClearcasePreferenceConstants.IGNORE_NEW, false);
        pref.setDefault(IClearcasePreferenceConstants.RECURSIVE, true);
        pref.setDefault(IClearcasePreferenceConstants.SAVE_DIRTY_EDITORS,
                IClearcasePreferenceConstants.PROMPT);
        pref
                .setDefault(
                        IClearcasePreferenceConstants.HIDE_REFRESH_STATE_ACTIVITY,
                        true);

        // source management
        pref.setDefault(IClearcasePreferenceConstants.ADD_AUTO, true);
        pref.setDefault(IClearcasePreferenceConstants.CHECKOUT_AUTO,
                IClearcasePreferenceConstants.PROMPT);
        pref.setDefault(IClearcasePreferenceConstants.ADD_WITH_CHECKIN, false);
        pref.setDefault(IClearcasePreferenceConstants.CHECKOUT_RESERVED,
                IClearcasePreferenceConstants.NEVER);
        pref.setDefault(IClearcasePreferenceConstants.CHECKOUT_LATEST, true);

        // comments
        pref.setDefault(IClearcasePreferenceConstants.COMMENT_ADD, true);
        pref.setDefault(
                IClearcasePreferenceConstants.COMMENT_ADD_NEVER_ON_AUTO, true);
        pref.setDefault(IClearcasePreferenceConstants.COMMENT_CHECKIN, true);
        pref.setDefault(IClearcasePreferenceConstants.COMMENT_CHECKOUT, false);
        pref.setDefault(
                IClearcasePreferenceConstants.COMMENT_CHECKOUT_NEVER_ON_AUTO,
                true);
        pref.setDefault(IClearcasePreferenceConstants.COMMENT_ESCAPE, false);
    }

    /**
     * Indicates if this plugin runs on a Microsoft Windows operating system.
     * 
     * @return <code>true</code> if this is a Windows operating system,
     *         <code>false</code> otherwise
     */
    public static boolean isWindows() {
        return Constants.OS_WIN32.equals(Platform.getOS());
    }

    /**
     * Loads the comment history.
     */
    private void loadCommentHistory() {
        IPath pluginStateLocation = getStateLocation()
                .append(COMMENT_HIST_FILE);
        File file = pluginStateLocation.toFile();
        if (!file.exists()) return;
        try {
            BufferedInputStream is = new BufferedInputStream(
                    new FileInputStream(file));
            try {
                readCommentHistory(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            getLog()
                    .log(
                            new Status(
                                    IStatus.ERROR,
                                    PLUGIN_ID,
                                    TeamException.UNABLE,
                                    Messages
                                            .getString("ClearcasePlugin.error.readingConfig.1") //$NON-NLS-1$
                                            + e.getLocalizedMessage(), e));
        } catch (CoreException e) {
            getLog().log(e.getStatus());
        }
    }

    /**
     * Builds (reads) the comment history from the specified input stream.
     * 
     * @param stream
     * @throws CoreException
     */
    private void readCommentHistory(InputStream stream) throws CoreException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            InputSource source = new InputSource(stream);
            Document document = parser.parse(source);
            NodeList list = document.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node instanceof Element) {
                    if (ELEMENT_COMMENT_HISTORY.equals(((Element) node)
                            .getTagName())) {
                        synchronized (previousComments) {
                            previousComments.clear();
                            NodeList commentNodes = ((Element) node)
                                    .getElementsByTagName(ELEMENT_COMMENT);
                            for (int j = 0; j < commentNodes.getLength()
                                    && j < MAX_COMMENTS; j++) {
                                Node commentNode = commentNodes.item(j);
                                if (commentNode instanceof Element
                                        && commentNode.hasChildNodes()) {
                                    // the first child is expected to be a text
                                    // node with our comment
                                    String comment = commentNode
                                            .getFirstChild().getNodeValue();
                                    if (null != comment) {
                                        comment = new String(BASE64_DECODER
                                                .decodeBuffer(comment), UTF_8);
                                        if (!previousComments.contains(comment))
                                                previousComments
                                                        .addLast(comment);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            PLUGIN_ID,
                            TeamException.UNABLE,
                            Messages
                                    .getString("ClearcasePlugin.error.readingConfig.2"), e)); //$NON-NLS-1$
        }
    }

    /**
     * Resets this plugin so that a new ClearCase engine will be created next
     * time it is requested.
     */
    public void resetClearcase() {
        // cancel pending refresh jobs
        StateCacheFactory.getInstance().getJobQueue().cancel(true);

        // destroy clearcase engine
        if (clearcaseImpl != null) {
            clearcaseImpl.destroy();
            clearcaseImpl = null;
        }
    }

    /**
     * Indicates if there are state refreshes pending.
     * 
     * @return <code>true</code> if there are state refreshes pending
     */
    public boolean hasPendingRefreshes() {
        return !StateCacheFactory.getInstance().getJobQueue().isEmpty();
    }

    /**
     * Cancels all pending state refreshes.
     */
    public void cancelPendingRefreshes() {
        StateCacheFactory.getInstance().getJobQueue().cancel(true);
    }

    /**
     * Saves the comment history.
     * 
     * @throws CoreException
     */
    private void saveCommentHistory() throws CoreException {
        IPath pluginStateLocation = getStateLocation();
        File tempFile = pluginStateLocation.append(COMMENT_HIST_FILE + ".tmp") //$NON-NLS-1$
                .toFile(); //$NON-NLS-1$
        File histFile = pluginStateLocation.append(COMMENT_HIST_FILE).toFile();
        try {
            XMLWriter writer = new XMLWriter(new BufferedOutputStream(
                    new FileOutputStream(tempFile)));
            try {
                writeCommentHistory(writer);
            } finally {
                writer.close();
            }
            if (histFile.exists()) {
                histFile.delete();
            }
            boolean renamed = tempFile.renameTo(histFile);
            if (!renamed) { throw new CoreException(new Status(IStatus.ERROR,
                    PLUGIN_ID, TeamException.UNABLE,
                    MessageFormat.format(Messages
                            .getString("ClearcasePlugin.error.renameFile"), //$NON-NLS-1$
                            new Object[] { tempFile.getAbsolutePath() }), null)); }
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
                    TeamException.UNABLE, MessageFormat.format(Messages
                            .getString("ClearcasePlugin.error.saveFile"), //$NON-NLS-1$
                            new Object[] { histFile.getAbsolutePath() }), e));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);

        configureDebugOptions();

        // Disables plugin if clearcase is not available (throws CoreEx)
        getClearcase();

        // process deltas since last activated in another thread
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=67449
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=60566
        Job processSavedState = new Job(Messages
                .getString("savedState.jobName")) { //$NON-NLS-1$

            protected IStatus run(IProgressMonitor monitor) {
                try {
                    final IWorkspace workspace = ResourcesPlugin.getWorkspace();

                    // add save participant and process delta atomically
                    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=67449
                    workspace.run(new IWorkspaceRunnable() {

                        public void run(IProgressMonitor progress)
                                throws CoreException {
                            StateCacheFactory cacheFactory = StateCacheFactory
                                    .getInstance();
                            ISavedState savedState = workspace
                                    .addSaveParticipant(ClearcasePlugin.this,
                                            cacheFactory);
                            if (savedState != null) {
                                if (DEBUG) trace("loading saved state"); //$NON-NLS-1$
                                cacheFactory.load(savedState);
                                // the event type coming from the saved state is
                                // always POST_AUTO_BUILD
                                // force it to be POST_CHANGE so that the delta
                                // processor can handle it
                                savedState
                                        .processResourceChangeEvents(cacheFactory);
                            }
                            workspace.addResourceChangeListener(cacheFactory,
                                    IResourceChangeEvent.POST_CHANGE);
                        }
                    }, monitor);
                } catch (CoreException e) {
                    return e.getStatus();
                }
                return Status.OK_STATUS;
            }
        };
        processSavedState.setSystem(!DEBUG);
        processSavedState.setPriority(Job.LONG); 
        processSavedState.schedule(500);

        loadCommentHistory();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);

        getWorkspace().removeResourceChangeListener(
                StateCacheFactory.getInstance());

        StateCacheFactory.getInstance().getJobQueue().cancel();

        resetClearcase();

        saveCommentHistory();

        super.stop(context);

    }

    /**
     * Writes the comment history to the specified writer.
     * 
     * @param writer
     * @throws IOException
     */
    private void writeCommentHistory(XMLWriter writer) throws IOException {
        synchronized (previousComments) {
            writer.startTag(ELEMENT_COMMENT_HISTORY, null, false);
            for (int i = 0; i < previousComments.size() && i < MAX_COMMENTS; i++)
                writer.printSimpleTag(ELEMENT_COMMENT, BASE64_ENCODER
                        .encode(((String) previousComments.get(i))
                                .getBytes(UTF_8)));
            writer.endTag(ELEMENT_COMMENT_HISTORY);
        }
    }

    /**
     * Returns the ClearCase modification handler.
     * <p>
     * Allthough this method is exposed in API it is not inteded to be called by
     * clients.
     * </p>
     * 
     * @return returns the ClearCase modification handler
     */
    ClearcaseModificationHandler getClearcaseModificationHandler() {
        return clearcaseModificationHandler;
    }

    /**
     * Sets the ClearCase modification handler.
     * <p>
     * Allthough this method is exposed in API it is not inteded to be called by
     * clients.
     * </p>
     * 
     * @param clearcaseModificationHandler
     *            the ClearCase modification handler to set
     */
    public void setClearcaseModificationHandler(
            ClearcaseModificationHandler clearcaseModificationHandler) {
        this.clearcaseModificationHandler = clearcaseModificationHandler;
    }
}