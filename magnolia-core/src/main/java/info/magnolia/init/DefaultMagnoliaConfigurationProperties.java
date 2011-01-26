/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.init;

import info.magnolia.init.properties.ClasspathPropertySource;
import info.magnolia.init.properties.InitPathsPropertySource;
import info.magnolia.init.properties.ModulePropertiesSource;
import info.magnolia.init.properties.SystemPropertySource;
import info.magnolia.module.ModuleRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This implementation of {@link MagnoliaConfigurationProperties} sets up the following list of {@link PropertySource}:
 * <ul>
 * <li>{@link InitPathsPropertySource}</li>
 * <li>/mgnl-beans.properties (list of default core components - TODO : this might move to core's module descriptor</li>
 * <li>{@link ModulePropertiesSource} (all properties exposed my modules)</li>
 * <li>Sources provided by {@link MagnoliaPropertiesResolver}</li>
 * <li>{@link SystemPropertySource}</li>
 * </ul>
 *
 * This class along with {@link DefaultMagnoliaPropertiesResolver} provide a replacement for PropertiesInitializer since 5.0.
 *
 * @since 5.0
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultMagnoliaConfigurationProperties extends AbstractMagnoliaConfigurationProperties {

    public DefaultMagnoliaConfigurationProperties(MagnoliaInitPaths initPaths, ModuleRegistry moduleRegistry, MagnoliaPropertiesResolver resolver) throws IOException {
        super(makeSources(initPaths, moduleRegistry, resolver));
    }

    protected static List<PropertySource> makeSources(MagnoliaInitPaths initPaths, ModuleRegistry moduleRegistry, MagnoliaPropertiesResolver resolver) throws IOException {
        final List<PropertySource> allSources = new ArrayList<PropertySource>();
        // TODO order ? -- sounds like it should be reversed (or we should iterate the other way around - eh)
        allSources.add(new InitPathsPropertySource(initPaths));
        allSources.add(new ClasspathPropertySource("/mgnl-beans.properties"));
        allSources.add(new ModulePropertiesSource(moduleRegistry));
        final List<PropertySource> configuredSources = resolver.getSources();
        for (PropertySource source : configuredSources) {
            allSources.add(source);
        }

        // TODO - this is not correct: we only want OVERRIDING properties here, not all.
        allSources.add(new SystemPropertySource());

        Collections.reverse(allSources);
        // TODO close streams
        // TODO resolve nested properties - now ?
        return allSources;
    }


}
