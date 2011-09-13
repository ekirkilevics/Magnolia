/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.objectfactory;

/**
 * ComponentProvider is responsible for providing components, these can be scoped as singletons or live in narrower
 * scopes such as request and session, or be non scoped in which case a new instances will be created. Magnolia "beans",
 * "managers" etc are all provided by this.
 *
 * Since Magnolia 4.5, you are encouraged to use IoC, only in rare cases should you need to directly use this class.
 *
 * @version $Id$
 * @see ComponentFactory
 */
public interface ComponentProvider {

    /**
     * Returns the implementation type mapped for a given type. This is primarily used by Content2Bean.
     */
    <T> Class<? extends T> getImplementation(Class<T> type) throws ClassNotFoundException;

    /**
     * Returns the component mapped for a given type.
     *
     * @see #getComponent(Class)
     * @deprecated since 4.5, use IoC. If you really need to look up a component, then use {@link #getComponent(Class)}
     */
    <T> T getSingleton(Class<T> type);

    /**
     * Returns the component mapped for a given type.
     *
     * @return the component that is mapped for this type or null if there is none
     */
    <T> T getComponent(Class<T> type);

    /**
     * Creates a new instance of the passed interface / class by using the registered implementation. The parameters are
     * used to build the object. Most likely they are passed to the constructor. If this fails a
     * {@link MgnlInstantiationException} is thrown.
     *
     * @throws MgnlInstantiationException
     */
    <T> T newInstance(Class<T> type, Object... parameters);

    <T> T newInstanceWithParameterResolvers(Class<T> type, ParameterResolver... parameters);

    ComponentProvider getParent();
}
