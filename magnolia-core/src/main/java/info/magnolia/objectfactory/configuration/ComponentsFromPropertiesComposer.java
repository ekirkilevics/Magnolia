/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.objectfactory.configuration;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.VersionConfig;
import info.magnolia.cms.filters.FilterManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import info.magnolia.context.SystemContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.ComponentComposer;
import info.magnolia.objectfactory.ComponentConfigurationPath;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.PropertiesComponentProvider;

/**
 * Adds components configured as properties.
 *
 * @version $Id$
 * @see PropertiesComponentProvider
 */
public class ComponentsFromPropertiesComposer implements ComponentComposer {

    private final static Logger log = LoggerFactory.getLogger(PropertiesComponentProvider.class);

    @Override
    public void doWithConfiguration(ComponentProvider parentComponentProvider, ComponentProviderConfiguration configuration) {

        MagnoliaConfigurationProperties configurationProperties = parentComponentProvider.getComponent(MagnoliaConfigurationProperties.class);

        Properties properties = new Properties();
        for (String key : configurationProperties.getKeys()) {
            properties.put(key, configurationProperties.getProperty(key));
        }

        // FIXME These are defined in mgnl-beans.properties and if allowed would override those hard-coded in GuiceServletContextListener
        properties.remove(LicenseFileExtractor.class.getName());
        properties.remove(VersionConfig.class.getName());
        properties.remove(MessagesManager.class.getName());
        properties.remove(SystemContext.class.getName());
        properties.remove(WorkspaceAccessUtil.class.getName());
        properties.remove(ConfigLoader.class.getName());
        properties.remove(UnicodeNormalizer.Normalizer.class.getName());
        properties.remove(FilterManager.class.getName());

        createConfigurationFromProperties(properties, configuration);
    }

    protected <T> void createConfigurationFromProperties(Properties mappings, ComponentProviderConfiguration configuration) {
        for (Map.Entry<Object, Object> entry : mappings.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            final Class<T> type = (Class<T>) classForName(key);
            if (type == null) {
                log.debug("{} does not seem to resolve to a class. (property value: {})", key, value);
                continue;
            }

            if (ComponentConfigurationPath.isComponentConfigurationPath(value)) {
                ComponentConfigurationPath path = new ComponentConfigurationPath(value);
                configuration.addComponent(new ConfiguredComponentConfiguration(type, path.getRepository(), path.getPath(), true));
            } else {
                Class<? extends T> valueType = (Class<? extends T>) classForName(value);
                if (valueType == null) {
                    log.debug("{} does not seem to resolve a class or a configuration path. (property key: {})", value, key);
                } else {
                    if (ComponentFactory.class.isAssignableFrom(valueType)) {
                        configuration.addComponent(new ComponentFactoryConfiguration(type, valueType));
                    } else {
                        configuration.addComponent(new ImplementationConfiguration(type, valueType));
                        configuration.addTypeMapping(type, valueType);
                    }
                }
            }
        }
    }

    protected Class<?> classForName(String value) {
        try {
            return Classes.getClassFactory().forName(value);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
