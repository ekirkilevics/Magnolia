/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.i18n;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * From this class you get the i18n messages. You should pass a a request, but if you can't the getMessages method will
 * handle it properly. The get() methods are easy to use.
 * @author philipp
 */
public final class MessagesManager {

    /**
     * Use this locale if no other provided
     */
    public static final String FALLBACK_LOCALE = "en"; //$NON-NLS-1$

    /**
     * Use this basename if no other is provided
     */
    public static final String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages"; //$NON-NLS-1$

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(AbstractMessagesImpl.class);

    /**
     * The node name where the configuration for i18n is stored
     */
    private static final String I18N_CONFIG_NAME = "i18n"; //$NON-NLS-1$

    /**
     * The name of the property to store the current system language
     */
    private static final String LOCALE_CONFIG_NAME = "language"; //$NON-NLS-1$

    /**
     * Under this node all the available languages are stored. They are showed in the user dialog.
     */
    private static final String AVAILABLE_LOCALES_CONFIG_NAME = "availableLanguages"; //$NON-NLS-1$

    /**
     * The current locale of the application
     */
    private static Locale applicationLocale;

    /**
     * List of the available locales
     */
    private static Collection availableLocales = new ArrayList();

    /**
     * The context used for the messages
     */
    private static ServletContext context;

    /**
     * LRU Map for the messages
     */
    private static Map messages;

    /**
     * Util has no public constructor
     */
    private MessagesManager() {
    }

    /**
     * Called through the initialization process (startup of the container)
     * @param context servlet context
     */
    public static void init(ServletContext context) {
        MessagesManager.context = context;

        // setting fallback
        context.setAttribute(Config.FMT_FALLBACK_LOCALE + ".application", FALLBACK_LOCALE); //$NON-NLS-1$
        // setting basename
        context.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".application", MessagesManager.DEFAULT_BASENAME); //$NON-NLS-1$
        // for Resin and other J2EE Containers
        context.setAttribute(Config.FMT_LOCALIZATION_CONTEXT, MessagesManager.DEFAULT_BASENAME);

        // setting default language (en)
        MessagesManager.setDefaultLocale(FALLBACK_LOCALE);

        load();
        registerEventListener();
    }

    /**
     * The lazzy LRU Map creates messages objects with a faul back to the default locale.
     */
    private static void intiLRUMap() {
        // FIXME use LRU
        // Map map = new LRUMap(20);
        Map map = new HashMap();
        map = LazyMap.decorate(map, new Transformer() {

            public Object transform(Object input) {
                MessagesID id = (MessagesID) input;
                // check http://jira.magnolia.info/browse/MAGNOLIA-1060
                // We are now chaining current user (LOCALE) messages with system default messages
                // so that it fallsback to default locale if string is not found instead of displaying broken
                // ???LABELS???
                Messages msgs = new DefaultMessagesImpl(id.basename, id.locale);
                if(!MessagesManager.getDefaultLocale().equals(id.locale)){
                    msgs = new MessagesChain(msgs).chain(MessagesManager.getMessages(id.basename, MessagesManager.getDefaultLocale()));
                }
                return msgs;
            }
        });
        messages = Collections.synchronizedMap(map);
    }

    /**
     * Load i18n configuration.
     */
    public static void load() {

        // reading the configuration from the repository, no need for context
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {
            intiLRUMap();

            log.info("Config : loading i18n configuration - " + I18N_CONFIG_NAME); //$NON-NLS-1$

            Content serverNode = hm.getContent("/server"); //$NON-NLS-1$

            Content configNode;
            try {
                configNode = serverNode.getContent(I18N_CONFIG_NAME);
            }
            catch (javax.jcr.PathNotFoundException e) {
                configNode = serverNode.createContent(I18N_CONFIG_NAME, ItemType.CONTENTNODE);
                hm.save();
            }

            NodeData languageNodeData = configNode.getNodeData(LOCALE_CONFIG_NAME);

            if (StringUtils.isEmpty(languageNodeData.getName())) {
                languageNodeData = configNode.createNodeData(LOCALE_CONFIG_NAME);
                languageNodeData.setValue(MessagesManager.FALLBACK_LOCALE);
                hm.save();
            }

            MessagesManager.setDefaultLocale(languageNodeData.getString());

            // get the available languages
            Content availableLanguagesContentNode;

            NodeData availableLanguage;

            try {
                availableLanguagesContentNode = configNode.getContent(AVAILABLE_LOCALES_CONFIG_NAME);
            }
            catch (javax.jcr.PathNotFoundException e) {
                availableLanguagesContentNode = configNode.createContent(
                    AVAILABLE_LOCALES_CONFIG_NAME,
                    ItemType.CONTENTNODE);

                availableLanguage = availableLanguagesContentNode.createNodeData(MessagesManager.FALLBACK_LOCALE);
                availableLanguage.setValue(MessagesManager.FALLBACK_LOCALE);
                hm.save();
            }

            Collection locales = availableLanguagesContentNode.getNodeDataCollection();

            // clear collection for reload
            MessagesManager.availableLocales.clear();

            for (Iterator iter = locales.iterator(); iter.hasNext();) {
                availableLanguage = (NodeData) iter.next();
                String name = availableLanguage.getString();
                String language = name;
                String country = StringUtils.EMPTY;

                if (name.indexOf("_") == 2) { //$NON-NLS-1$
                    language = name.substring(0, 2);
                    country = name.substring(3);
                }
                Locale locale = new Locale(language, country);
                MessagesManager.availableLocales.add(locale);
            }

        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load i18n configuration - " + I18N_CONFIG_NAME); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    /**
     * Register an event listener: reload configuration when something changes.
     */
    private static void registerEventListener() {

        log.info("Registering event listener for i18n"); //$NON-NLS-1$
        ObservationUtil.registerChangeListener(
            ContentRepository.CONFIG,
            "/server/" + I18N_CONFIG_NAME,
            new EventListener() {

                public void onEvent(EventIterator iterator) {
                    // reload everything
                    reload();
                }
            });
    }

    /**
     * Reload i18n configuration.
     */
    public static void reload() {
        try {
            reloadBundles();
        }
        catch (Exception e) {
            log.error("can't reload i18n messages", e);
        }
        load();
    }

    public static Messages getMessages() {
        return getMessages(MessagesManager.DEFAULT_BASENAME, MgnlContext.getLocale());
    }

    public static Messages getMessages(String basename) {
        return getMessages(basename, MgnlContext.getLocale());
    }

    public static Messages getMessages(String basename, Locale locale) {
        if (StringUtils.isEmpty(basename)) {
            basename = MessagesManager.DEFAULT_BASENAME;
        }
        return (Messages) messages.get(new MessagesID(basename, locale));
    }

    public static String get(String key) {
        return MgnlContext.getMessages().get(key);
    }

    /**
     * Get a message with parameters inside: the value {0} must be a number
     * @param key key to find
     * @param args replacement strings
     * @return message
     */

    public static String get(String key, Object[] args) {
        return MgnlContext.getMessages().get(key, args);
    }

    /**
     * Use a default string.
     * @param key key to find
     * @param defaultMsg default message
     * @return message
     */

    public static String getWithDefault(String key, String defaultMsg) {
        return MgnlContext.getMessages().getWithDefault(key, defaultMsg);
    }

    /**
     * Get a message with parameters inside: the value {0} must be a number. Use a default message.
     * @param key key to find
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    public static String getWithDefault(String key, Object[] args, String defaultMsg) {
        return MgnlContext.getMessages().getWithDefault(key, args, defaultMsg);
    }

    /**
     * @return Returns the defaultLocale.
     */
    public static Locale getDefaultLocale() {
        return applicationLocale;
    }

    /**
     * Deprectated! Use MgnlContext
     * @return Returns the current locale for the current user
     * @deprecated
     */
    public static Locale getCurrentLocale(HttpServletRequest request) {
        try {
            return MgnlContext.getLocale();
        }
        catch (Exception e) {
            return getDefaultLocale();
        }
    }

    /**
     * @param defaultLocale The defaultLocale to set.
     */
    public static void setDefaultLocale(String defaultLocale) {
        MessagesManager.applicationLocale = new Locale(defaultLocale);
        MgnlContext.getSystemContext().setLocale(applicationLocale);
        context.setAttribute(Config.FMT_LOCALE + ".application", defaultLocale); //$NON-NLS-1$
    }

    /**
     * @return Returns the availableLocals.
     */
    public static Collection getAvailableLocales() {
        return availableLocales;
    }

    /**
     * Set the user language in the session
     * @param language lagnguage to ste
     * @param session current session
     * @deprecated use MgnlContext instead
     */
    public static void setUserLanguage(String language, HttpSession session) {
        MgnlContext.setAttribute(Config.FMT_LOCALE + ".session", language, Context.SESSION_SCOPE); //$NON-NLS-1$
    }

    public static void reloadBundles() throws Exception {
        // reload all present
        for (Iterator iter = messages.values().iterator(); iter.hasNext();) {
            Messages msgs = (Messages) iter.next();
            msgs.reload();
        }
    }

    /**
     * Getter for <code>context</code>.
     * @return Returns the context.
     */
    public static ServletContext getContext() {
        return context;
    }

    /**
     * Used as the key in the LRUMap
     * @author Philipp Bracher
     * @version $Revision$ ($Author$)
     */
    static private class MessagesID {

        String basename;

        Locale locale;

        /**
         * @param basename
         * @param locale
         */
        public MessagesID(String basename, Locale locale) {
            this.basename = basename;
            this.locale = locale;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return basename.hashCode();
        }

        public boolean equals(Object id) {
            return ((MessagesID) id).basename.equals(basename) && ((MessagesID) id).locale.equals(locale);
        }
    }
}