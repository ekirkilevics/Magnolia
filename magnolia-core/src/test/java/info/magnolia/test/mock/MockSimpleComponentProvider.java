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
package info.magnolia.test.mock;

import info.magnolia.objectfactory.ComponentProvider;

import java.lang.reflect.Constructor;


/**
 * Simple ComponentProvider - not configurable at all.
 *
 * @author dlipp
 */
public class MockSimpleComponentProvider implements ComponentProvider {

    @Override
    public <C> Class<? extends C> getImplementation(Class<C> type) throws ClassNotFoundException {
        // TODO implement as soon as needed
        return null;
    }

    @Override
    public <T> T getSingleton(Class<T> type) {
        // TODO implement as soon as needed
        return null;
    }

    @Override
    public <T> T getComponent(Class<T> type) {
        // TODO implement as soon as needed
        return null;
    }

    @Override
    public <T> T newInstance(Class<T> type, Object... parameters) {
        T newInstance = null;
        Class[] classesArray = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            classesArray[i] = parameters[i].getClass();
        }
        try {
            Constructor<T> constructor = type.getConstructor(classesArray);
            newInstance = constructor.newInstance(parameters);
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
        return newInstance;
    }
}
