/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.i18n;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

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
    private static final String I18N_CONFIG_PATH = "/server/i18n/system"; //$NON-NLS-1$

    /**
     * The name of the property to store the current system language
     */
    private static final String FALLBACK_NODEDATA = "fallbackLanguage"; //$NON-NLS-1$

    /**
     * Under this node all the available languages are stored. They are showed in the user dialog.
     */
    private static final String LANGUAGES_NODE_NAME = "languages"; //$NON-NLS-1$

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

    static{
        // setting default language (en)
        MessagesManager.setDefaultLocale(FALLBACK_LOCALE);

        initLRUMap();
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

        load();
        registerEventListener();
    }

    /**
     * The lazzy LRU Map creates messages objects with a faul back to the default locale.
     */
    private static void initLRUMap() {
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
            log.info("Config : loading i18n configuration - " + I18N_CONFIG_PATH); //$NON-NLS-1$

            // checks if node exists
            if (!hm.isExist(I18N_CONFIG_PATH)) {
                // configNode = ContentUtil.createPath(hm, I18N_CONFIG_PATH, ItemType.CONTENT, true);
                log.warn(I18N_CONFIG_PATH + " does not exist yet; skipping.");
                return;
            }

            final Content configNode = hm.getContent(I18N_CONFIG_PATH); //$NON-NLS-1$

            MessagesManager.setDefaultLocale(NodeDataUtil.getString(configNode, FALLBACK_NODEDATA, FALLBACK_LOCALE));

            // get the available languages - creates it if it does not exist - necessary to update to 3.5
            final Content languagesNode;
            if (configNode.hasContent(LANGUAGES_NODE_NAME)) {
                languagesNode = configNode.getContent(LANGUAGES_NODE_NAME);
            } else {
                languagesNode = configNode.createContent(LANGUAGES_NODE_NAME, ItemType.CONTENT);
            }
            
            Map languageDefinitions = Content2BeanUtil.toMap(languagesNode, true, LocaleDefinition.class);

            // clear collection for reload
            MessagesManager.availableLocales.clear();

            for (Iterator iter = languageDefinitions.values().iterator(); iter.hasNext();) {
                LocaleDefinition ld = (LocaleDefinition) iter.next();
                if(ld.isEnabled()){
                    availableLocales.add(ld.getLocale());
                }
            }
        }
        catch (Exception e) {
            log.error("Config : Failed to load i18n configuration - " + I18N_CONFIG_PATH, e); //$NON-NLS-1$
        }
    }

    /**
     * Register an event listener: reload configuration when something changes.
     */
    private static void registerEventListener() {

        log.info("Registering event listener for i18n"); //$NON-NLS-1$
        ObservationUtil.registerChangeListener(
            ContentRepository.CONFIG,
            I18N_CONFIG_PATH,
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
        initLRUMap();
        load();
    }

    public static Messages getMessages() {
        return getMessages(MessagesManager.DEFAULT_BASENAME, MgnlContext.getLocale());
    }

    public static Messages getMessages(String basename) {
        return getMessages(basename, MgnlContext.getLocale());
    }
    
    public static Messages getMessages(Locale locale) {
        return getMessages(MessagesManager.DEFAULT_BASENAME, locale);
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

        if(context != null){
            context.setAttribute(Config.FMT_LOCALE + ".application", defaultLocale); //$NON-NLS-1$
        }
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
     * @deprecated since 3.5, use MgnlContext instead (this is not used - can be safely removed)
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

        public MessagesID(String basename, Locale locale) {
            this.basename = basename;
            this.locale = locale;
        }

        public int hashCode() {
            return basename.hashCode();
        }

        public boolean equals(Object id) {
            return ((MessagesID) id).basename.equals(basename) && ((MessagesID) id).locale.equals(locale);
        }
    }
}
