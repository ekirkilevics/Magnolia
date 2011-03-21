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
 * ComponentProvider is responsible for providing components, singletons or new instances.
 * Magnolia "beans", "managers" etc are all provided by this.
 *
 * Since Magnolia 5.0, you are encouraged to use IoC, so the cases where this class
 * is needed should be limited. Think twice !
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ComponentProvider {

    /**
     * Returns the implementation type mapped for a given type.
     *
     * Used by Content2Bean.
     */
    <T> Class<? extends T> getImplementation(Class<T> type) throws ClassNotFoundException;

    /**
     * @deprecated since 5.0, use IoC. If you really need to look up a component, then use {@link #getComponent(Class)}
     * Additionally, it should not be up to the client to decide whether this component is a singleton or not.
     */
    <T> T getSingleton(Class<T> type);

    <T> T getComponent(Class<T> type);

    /**
     * Creates a new instance of the passed interface / class by using the registered
     * implementation. The parameters are used to build the object. Most likely they are passed to
     * the constructor. If this fails a {@link MgnlInstantiationException} is thrown.
     *
     * @throws MgnlInstantiationException
     * @throws UnsupportedOperationException
     */

    <T> T newInstance(Class<T> type, Object... parameters);

}
