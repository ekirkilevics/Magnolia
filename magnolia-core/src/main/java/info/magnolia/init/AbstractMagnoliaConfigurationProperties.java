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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract implementation, providing the basic behavior expected from a {@link MagnoliaConfigurationProperties} implementation.
 *
 * TODO: cache results.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractMagnoliaConfigurationProperties implements MagnoliaConfigurationProperties {
    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMagnoliaConfigurationProperties.class);

    protected final List<PropertySource> sources;

    protected AbstractMagnoliaConfigurationProperties(List<PropertySource> propertySources) {
        this.sources = propertySources;
    }

    @Override
    public Set<String> getKeys() {
        final Set<String> allKeys = new HashSet<String>();
        for (PropertySource source : sources) {
            allKeys.addAll(source.getKeys());
        }
        return allKeys;
    }

    @Override
    public PropertySource getPropertySource(String key) {
        for (PropertySource source : sources) {
            if (source.hasProperty(key)) {
                return source;
            }
        }
        return null;
    }

    @Override
    public String getProperty(String key) {
        final PropertySource propertySource = getPropertySource(key);
        if (propertySource != null) {
            final String value = propertySource.getProperty(key);
            return parseStringValue(value, new HashSet<String>());
        }
        return null;
    }

    @Override
    public boolean hasProperty(String key) {
        return getPropertySource(key) != null;
    }

    @Override
    public String describe() {
        final StringBuilder s = new StringBuilder()
                .append("[")
                .append(getClass().getSimpleName())
                .append(" with sources: ");
        for (PropertySource source : sources) {
            s.append(source.describe());
        }
        s.append("]");
        return s.toString();
    }

    @Override
    public String toString() {
        return describe() + " with sources: " + sources;
    }

    /**
     * Parse the given String value recursively, to be able to resolve nested placeholders. Partly borrowed from
     * org.springframework.beans.factory.config.PropertyPlaceholderConfigurer (original author: Juergen Hoeller)
     */
    protected String parseStringValue(String strVal, Set<String> visitedPlaceholders) {
        final StringBuffer buf = new StringBuffer(strVal.trim());

        int startIndex = strVal.indexOf(PLACEHOLDER_PREFIX);
        while (startIndex != -1) {
            int endIndex = -1;

            int index = startIndex + PLACEHOLDER_PREFIX.length();
            int withinNestedPlaceholder = 0;
            while (index < buf.length()) {
                if (PLACEHOLDER_SUFFIX.equals(buf.subSequence(index, index + PLACEHOLDER_SUFFIX.length()))) {
                    if (withinNestedPlaceholder > 0) {
                        withinNestedPlaceholder--;
                        index = index + PLACEHOLDER_SUFFIX.length();
                    } else {
                        endIndex = index;
                        break;
                    }
                } else if (PLACEHOLDER_PREFIX.equals(buf.subSequence(index, index + PLACEHOLDER_PREFIX.length()))) {
                    withinNestedPlaceholder++;
                    index = index + PLACEHOLDER_PREFIX.length();
                } else {
                    index++;
                }
            }

            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                if (!visitedPlaceholders.add(placeholder)) {
                    log.warn("Circular reference detected in properties, \"{}\" is not resolvable", strVal);
                    return strVal;
                }
                // Recursive invocation, parsing placeholders contained in the placeholder key.
                placeholder = parseStringValue(placeholder, visitedPlaceholders);
                // Now obtain the value for the fully resolved key...
                // Can't call getProperty() directly, as it would blow the call stack
                final PropertySource propertySource = getPropertySource(placeholder);
                String propVal = propertySource != null ? propertySource.getProperty(placeholder) : null;
                if (propVal != null) {
                    // Recursive invocation, parsing placeholders contained in the
                    // previously resolved placeholder value.
                    propVal = parseStringValue(propVal, visitedPlaceholders);
                    buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
                    startIndex = buf.indexOf(PLACEHOLDER_PREFIX, startIndex + propVal.length());
                } else {
                    // Proceed with unprocessed value.
                    startIndex = buf.indexOf(PLACEHOLDER_PREFIX, endIndex + PLACEHOLDER_SUFFIX.length());
                }
                visitedPlaceholders.remove(placeholder);
            } else {
                startIndex = -1;
            }
        }

        return buf.toString();
    }

    protected static final String PLACEHOLDER_PREFIX = "${";
    protected static final String PLACEHOLDER_SUFFIX = "}";


}
