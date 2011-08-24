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
package info.magnolia.objectfactory.guice;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import javax.inject.Named;

import com.google.inject.Key;
import info.magnolia.init.MagnoliaConfigurationProperties;

/**
 * Guice configuration module which exposes Magnolia properties.
 *
 * @version $Id$
 */
public class GuicePropertyComposer extends AbstractGuiceComponentComposer {

    @Override
    protected void configure() {

        // If we have a parent and it has a MagnoliaConfigurationProperties component expose all its properties
        if (parentComponentProvider != null) {
            MagnoliaConfigurationProperties configurationProperties = parentComponentProvider.getComponent(MagnoliaConfigurationProperties.class);
            if (configurationProperties != null) {
                installProperties(configurationProperties);
            }
        }
    }

    private void installProperties(MagnoliaConfigurationProperties configurationProperties) {

        for (final String key : configurationProperties.getKeys()) {

            /*
               Unfortunately there's a trade off here. We CAN register these as providers, then properties can change
               and we can get the changed values using a provider. But then we dont get conversion to primitives such as
               boolean and int.
            */

/*
            binder().bind(Key.get(String.class, new NamedImpl(propertyName))).toProvider(new Provider<String>() {
                @Override
                public String get() {
                    return SystemProperty.getProperty(configurationProperties.getProperty(key));
                }
            });
*/
            binder().bind(Key.get(String.class, new NamedImpl(key))).toInstance(configurationProperties.getProperty(key));
        }
    }

    /**
     * Represents an instantiated @Named annotation.
     */
    public static class NamedImpl implements Named, Serializable {

        private static final long serialVersionUID = 0;

        private final String value;

        public NamedImpl(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return this.value;
        }

        @Override
        public int hashCode() {
            // This is specified in java.lang.Annotation.
            return (127 * "value".hashCode()) ^ value.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Named)) {
                return false;
            }

            Named other = (Named) o;
            return value.equals(other.value());
        }

        @Override
        public String toString() {
            return "@" + Named.class.getName() + "(value=" + value + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }
    }
}
