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
package info.magnolia.test;

import info.magnolia.init.AbstractMagnoliaConfigurationProperties;
import info.magnolia.init.PropertySource;
import info.magnolia.init.properties.AbstractPropertySource;
import info.magnolia.init.properties.AbstractStreamBasedPropertySource;
import info.magnolia.init.properties.ClasspathPropertySource;
import info.magnolia.init.properties.InitPathsPropertySource;
import org.junit.Ignore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * A {@link info.magnolia.init.MagnoliaConfigurationProperties} which only exposes given Properties,
 * while {@link info.magnolia.init.DefaultMagnoliaConfigurationProperties} registers a bunch of default
 * {@link PropertySource}s which might not be present or relevant for tests.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
@Ignore
public class TestMagnoliaConfigurationProperties extends AbstractMagnoliaConfigurationProperties {

    public TestMagnoliaConfigurationProperties() throws IOException {
        this(new Properties());
    }

    /**
     * If your component-under-test relies on a single property, this constructor will save you a pair of lines of code.
     */
    public TestMagnoliaConfigurationProperties(final String key, final String value) throws IOException {
        this(new Properties() {{
            put(key, value);
        }});
    }

    public TestMagnoliaConfigurationProperties(Properties p) throws IOException {
        this(new AbstractPropertySource(p) {
        });
    }

    public TestMagnoliaConfigurationProperties(InputStream in) throws IOException {
        this(new AbstractStreamBasedPropertySource(in, "test") {
        });
    }

    public TestMagnoliaConfigurationProperties(PropertySource... sauces) {
        super(Arrays.asList(sauces));
    }

    TestMagnoliaConfigurationProperties(PropertySource sauce) throws IOException {
        super(Arrays.asList(
                sauce,
                new ClasspathPropertySource("/test-magnolia.properties"),
                new InitPathsPropertySource(new TestMagnoliaInitPaths())
        ));
    }
}
