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

import org.apache.commons.beanutils.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * A ClassFactory implementation which uses the default class loader and the thread context class loader.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultClassFactory implements ClassFactory {

    @Override
    public <C> Class<C> forName(String className) throws ClassNotFoundException {
        Class<C> loadedClass;
        try {
            loadedClass = (Class<C>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            loadedClass = (Class<C>) Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        }
        return loadedClass;

    }

    @Override
    public <T> T newInstance(final Class<T> c, final Class<?>[] argTypes, final Object... params) {
        if (argTypes.length != params.length) {
            throw new IllegalStateException("Argument types and values do not match! " + Arrays.asList(argTypes) + " / " + Arrays.asList(params));
        }

        return newInstance(c, params, new Invoker<T>() {
            @Override
            public T invoke() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                return (T) ConstructorUtils.invokeConstructor(c, params, argTypes);
            }
        });
    }

    @Override
    public <T> T newInstance(final Class<T> c, final Object... params) {
        return newInstance(c, params, new Invoker<T>() {
            @Override
            public T invoke() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
                return (T) ConstructorUtils.invokeConstructor(c, params);
            }
        });
    }

    private <T> T newInstance(Class<T> c, Object[] params, Invoker<T> invoker) {
        // TODO -
        // c.isAnnotationPresent(Deprecated) - at class or constructor level,
        // output a warning - todo bis - use a subclass of java.lang.Deprecated which allows a message

        try {
            if (params == null || params.length == 0) {
                // shortcut
                return c.newInstance();
            }

            // org.apache.commons.beanutils.ConstructorUtils#getMatchingAccessibleConstructor is private,
            // otherwise, we'd simply extract a getConstructor() method to implement our 2 newInstance() methods.
            return invoker.invoke();

        } catch (NoSuchMethodException e) {
            throw new MgnlInstantiationException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new MgnlInstantiationException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new MgnlInstantiationException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new MgnlInstantiationException(e.getMessage(), e);
        }
    }

    private static interface Invoker<T> {
        T invoke() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException;
    }

}
