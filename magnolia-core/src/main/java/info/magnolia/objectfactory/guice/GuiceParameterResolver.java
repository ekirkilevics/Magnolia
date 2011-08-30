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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Parameter resolver that finds parameters by querying for them in a Guice Injector. Will create javax.inject.Provider
 * instances that match the parameter's type.
 *
 * @version $Id$
 */
public class GuiceParameterResolver implements ObjectManufacturer.ParameterResolver {

    private final Injector injector;

    public GuiceParameterResolver(Injector injector) {
        this.injector = injector;
    }

    public GuiceParameterResolver(GuiceComponentProvider componentProvider) {
        this.injector = componentProvider.getInjector();
    }

    @Override
    public Object resolveParameter(ObjectManufacturer.ConstructorParameter methodParameter) {

        Type genericParameterType = methodParameter.getGenericParameterType();
        Class<?> parameterType = methodParameter.getParameterType();

        // We ask for an existing binding so Guice wont create jit bindings for things like Class and String
        // This means that all parameters need to be explicitly bound

        // If the parameter is javax.inject.Provider<T> we will look for a provider of T instead
        if (genericParameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
            if (parameterizedType.getRawType() == javax.inject.Provider.class) {
                Type actualType = parameterizedType.getActualTypeArguments()[0];
                Binding<?> existingBinding = injector.getExistingBinding(Key.get(actualType));
                return existingBinding != null ? existingBinding.getProvider() : UNRESOLVED;
            }
        }

        Key<?> key = Key.get(parameterType);
        Binding<?> existingBinding = injector.getExistingBinding(key);
        return existingBinding != null ? existingBinding.getProvider().get() : UNRESOLVED;
    }
}
