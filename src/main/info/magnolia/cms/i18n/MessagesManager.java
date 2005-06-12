/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.i18n;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * From this class you get the i18n messages. You should pass a a request, but if you can't the getMessages method will
 * handle it properly. The get() methods are easy to use.
 * @author philipp
 */
public final class MessagesManager {

    /**
     * Use this locale if no other provided
     */
    public static final String FALLBACK_LOCALE = "en";

    /**
     * Use this basename if no other is provided
     */
    public static final String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages";

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(Messages.class);

    /**
     * The node name where the configuration for i18n is stored
     */
    private static final String I18N_CONFIG_NAME = "i18n";

    /**
     * The name of the property to store the current system language
     */
    private static final String LOCALE_CONFIG_NAME = "language";

    /**
     * Under this node all the available languages are stored. They are showed in the user dialog.
     */
    private static final String AVAILABLE_LOCALES_CONFIG_NAME = "availableLanguages";

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
        context.setAttribute(Config.FMT_FALLBACK_LOCALE + ".application", FALLBACK_LOCALE);
        // setting basename
        context.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".application", MessagesManager.DEFAULT_BASENAME);
        // for Resin and other J2EE Containers
        context.setAttribute(Config.FMT_LOCALIZATION_CONTEXT, MessagesManager.DEFAULT_BASENAME);

        // setting default language (en)
        MessagesManager.setDefaultLocale(FALLBACK_LOCALE);

        // reading the configuration from the repository
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {
            log.info("Config : loading i18n configuration - " + I18N_CONFIG_NAME);

            Content serverNode = configHierarchyManager.getContent("/server");

            Content configNode;
            try {
                configNode = serverNode.getContent(I18N_CONFIG_NAME);
            }
            catch (javax.jcr.PathNotFoundException e) {
                configNode = serverNode.createContent(I18N_CONFIG_NAME, ItemType.CONTENTNODE);
                configHierarchyManager.save();
            }

            NodeData languageNodeData = configNode.getNodeData(LOCALE_CONFIG_NAME);

            if (StringUtils.isEmpty(languageNodeData.getName())) {
                languageNodeData = configNode.createNodeData(LOCALE_CONFIG_NAME);
                languageNodeData.setValue(MessagesManager.FALLBACK_LOCALE);
                configHierarchyManager.save();
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
                configHierarchyManager.save();
            }

            Collection locales = availableLanguagesContentNode.getNodeDataCollection();
            for (Iterator iter = locales.iterator(); iter.hasNext();) {
                availableLanguage = (NodeData) iter.next();
                MessagesManager.availableLocales.add(new Locale(availableLanguage.getString()));
            }

        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load i18n configuration - " + I18N_CONFIG_NAME);
            log.error(re.getMessage(), re);
        }

    }

    /**
     * Trys to make a new ContextMessages object. if not possible it creates a new Messages object.
     * @param req uses the request to find the configuration
     * @return Messages
     */
    public static Messages getMessages(HttpServletRequest req) {
        if (req != null) {
            return new ContextMessages(req);
        }

        log.debug("using i18n-messages without a request!");
        return new Messages(MessagesManager.DEFAULT_BASENAME, applicationLocale);

    }

    /**
     * Provide a basename
     * @param req request
     * @param basename basena,e
     * @return Messages object to get the messages from
     */
    public static Messages getMessages(HttpServletRequest req, String basename) {
        if (req != null) {
            return new ContextMessages(req, basename);
        }

        log.debug("using i18n-messages without a request!");
        return new Messages(basename, applicationLocale);

    }

    /**
     * Provide a special locale
     * @param req request
     * @param basename basename
     * @param locale locale
     * @return Messages object to get the messages from
     */
    public static Messages getMessages(HttpServletRequest req, String basename, Locale locale) {
        if (req != null) {
            return new ContextMessages(req, basename, locale);
        }

        log.debug("using i18n-messages without a request!");
        return new Messages(basename, locale);

    }

    /**
     * Trys to make a new ContextMessages object. if not possible it creates a new Messages object.
     * @param pc the page context to start the lookup
     * @return Messages
     */
    public static Messages getMessages(PageContext pc) {
        if (pc != null && pc.getRequest() instanceof HttpServletRequest) {
            return new ContextMessages((HttpServletRequest) pc.getRequest());
        }

        log.debug("using i18n-messages without a request inside a control!");
        return new Messages(MessagesManager.DEFAULT_BASENAME, applicationLocale);

    }

    /**
     * Get a message.
     * @param req request
     * @param key key to find
     * @return message
     */

    public static String get(HttpServletRequest req, String key) {
        return getMessages(req).get(key);
    }

    /**
     * Get a message with parameters inside: the value {0} must be a number
     * @param req request
     * @param key key to find
     * @param args replacement strings
     * @return message
     */

    public static String get(HttpServletRequest req, String key, Object[] args) {
        return getMessages(req).get(key, args);
    }

    /**
     * Use a default string.
     * @param req request
     * @param key key to find
     * @param defaultMsg default message
     * @return message
     */

    public static String getWithDefault(HttpServletRequest req, String key, String defaultMsg) {
        return getMessages(req).getWithDefault(key, defaultMsg);
    }

    /**
     * Get a message with parameters inside: the value {0} must be a number. Use a default message.
     * @param req request
     * @param key key to find
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    public static String getWithDefault(HttpServletRequest req, String key, Object[] args, String defaultMsg) {
        return getMessages(req).getWithDefault(key, args, defaultMsg);
    }

    /**
     * @return Returns the defaultLocale.
     */
    public static Locale getDefaultLocale() {
        return applicationLocale;
    }

    /**
     * @param defaultLocale The defaultLocale to set.
     */
    public static void setDefaultLocale(String defaultLocale) {
        MessagesManager.applicationLocale = new Locale(defaultLocale);
        context.setAttribute(Config.FMT_LOCALE + ".application", defaultLocale);
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
     */
    public static void setUserLanguage(String language, HttpSession session) {
        session.setAttribute(Config.FMT_LOCALE + ".session", language);
    }
}