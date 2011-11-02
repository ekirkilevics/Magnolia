/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.i18n;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


/**
 * Default MessagesManager implementation.
 * @author philipp
 *
 * @version $Id$
 */
@Singleton
public class DefaultMessagesManager extends MessagesManager {
    private final static Logger log = LoggerFactory.getLogger(DefaultMessagesManager.class);

    /**
     * The current locale of the application.
     */
    private Locale applicationLocale;

    /**
     * List of the available locales.
     */
    private final Collection<Locale> availableLocales = new ArrayList<Locale>();

    /**
     * Map for the messages.
     */
    private Map messages;

    private String defaultBasename = DEFAULT_BASENAME;

    public DefaultMessagesManager() {
        // setting default language (en)
        setDefaultLocale(FALLBACK_LOCALE);

        initMap();
    }

    // for tests
    void setDefaultBasename(String defaultBasename) {
        this.defaultBasename = defaultBasename;
    }

    /**
     * Called through the initialization process. (startup of the container)
     */

    @Override
    public void init() {
        load();
        registerEventListener();
    }

    /**
     * The lazy Map creates messages objects with a fall back to the default locale.
     */
    protected void initMap() {
        // FIXME use LRU: new LRUMap(20);
        // LazyMap will instanciate bundles on demand.
        final Map map = LazyMap.decorate(new HashMap(), new Transformer() {

            // this transformer will wrap the Messages in a MessagesChain which
            // will fall back to a Messages instance for the same bundle with
            // default locale.
            @Override
            public Object transform(Object input) {
                final MessagesID id = (MessagesID) input;
                return newMessages(id);
            }
        });
        messages = Collections.synchronizedMap(map);
    }

    /**
     * Initializes a new Messages instances for the given MessagesID. By default, we chain to the same bundle with the
     * default Locale. (so untranslated messages show up in the default language)
     */
    protected Messages newMessages(MessagesID messagesID) {
        Messages msgs = new DefaultMessagesImpl(messagesID.basename, messagesID.locale);
        if (!getDefaultLocale().equals(messagesID.locale)) {
            msgs = new MessagesChain(msgs).chain(getMessages(messagesID.basename, getDefaultLocale()));
        }
        return msgs;
    }

    /**
     * Load i18n configuration.
     */
    protected void load() {

        // reading the configuration from the repository, no need for context
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(RepositoryConstants.CONFIG);

        try {
            log.info("Loading i18n configuration - {}", I18N_CONFIG_PATH);

            // checks if node exists
            if (!hm.isExist(I18N_CONFIG_PATH)) {
                // configNode = ContentUtil.createPath(hm, I18N_CONFIG_PATH, ItemType.CONTENT, true);
                log.warn("{} does not exist yet; skipping.", I18N_CONFIG_PATH);
                return;
            }

            final Content configNode = hm.getContent(I18N_CONFIG_PATH);

            setDefaultLocale(NodeDataUtil.getString(configNode, FALLBACK_NODEDATA, FALLBACK_LOCALE));

            // get the available languages - creates it if it does not exist - necessary to update to 3.5
            final Content languagesNode;
            if (configNode.hasContent(LANGUAGES_NODE_NAME)) {
                languagesNode = configNode.getContent(LANGUAGES_NODE_NAME);
            }
            else {
                languagesNode = configNode.createContent(LANGUAGES_NODE_NAME, ItemType.CONTENT);
            }

            final Map<String, LocaleDefinition> languageDefinitions = Content2BeanUtil.toMap(languagesNode, true, LocaleDefinition.class);

            // clear collection for reload
            availableLocales.clear();

            for (LocaleDefinition ld : languageDefinitions.values()) {
                if (ld.isEnabled()) {
                    availableLocales.add(ld.getLocale());
                }
            }
        } catch (Exception e) {
            log.error("Failed to load i18n configuration - {}", I18N_CONFIG_PATH, e);
        }
    }

    /**
     * Register an event listener: reload configuration when something changes.
     */
    private void registerEventListener() {
        log.info("Registering event listener for i18n");
        ObservationUtil.registerChangeListener(RepositoryConstants.CONFIG, I18N_CONFIG_PATH, new EventListener() {

            @Override
            public void onEvent(EventIterator iterator) {
                // reload everything
                reload();
            }
        });
    }

    /**
     * Reload i18n configuration.
     */
    @Override
    public void reload() {
        try {
            // reload all present
            for (Iterator iter = messages.values().iterator(); iter.hasNext();) {
                Messages msgs = (Messages) iter.next();
                msgs.reload();
            }
        }
        catch (Exception e) {
            log.error("Can't reload i18n messages", e);
        }
        initMap();
        load();
    }

    @Override
    public Messages getMessagesInternal(String basename, Locale locale) {
        if (StringUtils.isEmpty(basename)) {
            basename = defaultBasename;
        }
        return (Messages) messages.get(new MessagesID(basename, locale));
    }

    @Override
    public Locale getDefaultLocale() {
        return applicationLocale;
    }

    /**
     * @param defaultLocale The defaultLocale to set.
     * @deprecated since 4.0 - not used and should not be. Use setLocale() on the SystemContext instead. --note: do not
     * remove the method, make it private. applicationLocale field is still needed. --and/or remove duplication with
     * SystemContext.locale
     */
    public void setDefaultLocale(String defaultLocale) {
        this.applicationLocale = new Locale(defaultLocale);
        // MgnlContext.getSystemContext().setLocale(applicationLocale);
    }

    @Override
    public Collection getAvailableLocales() {
        return availableLocales;
    }

    public void setMessages(Map messages) {
        this.messages = messages;
    }

    /**
     * Used as the key in the Map.
     * @author Philipp Bracher
     * @version $Revision$ ($Author$)
     */
    public static class MessagesID {

        private final String basename;

        private final Locale locale;

        public MessagesID(String basename, Locale locale) {
            this.basename = basename;
            this.locale = locale;
        }

        // generated equals and hashcode methods

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MessagesID that = (MessagesID) o;

            if (basename != null ? !basename.equals(that.basename) : that.basename != null) {
                return false;
            }
            if (locale != null ? !locale.equals(that.locale) : that.locale != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = basename != null ? basename.hashCode() : 0;
            result = 31 * result + (locale != null ? locale.hashCode() : 0);
            return result;
        }

        /**
         * Returns the basename.
         * @return the basename
         */
        public String getBasename() {
            return basename;
        }

        /**
         * Returns the locale.
         * @return the locale
         */
        public Locale getLocale() {
            return locale;
        }

    }
}
