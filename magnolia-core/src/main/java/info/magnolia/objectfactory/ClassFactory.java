/**
 * This file Copyright (c) 2010 Magnolia International
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
 * ClassFactory implementations are responsible for locating and instantiating classes. Specific implementations
 * could take care of container-specific hacks, provide support for a scripting language, etc.
 *
 * TODO ? an spi-like layer that would allow to have multiple implementations of this and delegate to the first which "accepts" the given parameters ?
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ClassFactory {

    <C> Class<C> forName(String className) throws ClassNotFoundException;

    /**
     * Instantiates the given class with the given parameters.
     * For the empty constructor, pass no parameters (or an empty array, or null). To pass "null" to a single-arg constructor,
     * use newInstance(c, new Object[]{null}) (otherwise the *array* itself will be considered null)
     */
    <T> T newInstance(Class<T> c, Object... params);

}
