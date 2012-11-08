/**
 * This file Copyright (c) 2011-2012 Magnolia International
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

import java.util.List;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;

import info.magnolia.objectfactory.MgnlInstantiationException;
import info.magnolia.objectfactory.ParameterInfo;
import info.magnolia.objectfactory.ParameterResolver;

/**
 * Parameter resolver that finds parameters by querying for them in a Guice Injector. Will create javax.inject.Provider
 * instances that match the parameter's type.
 *
 * @version $Id$
 */
public class GuiceParameterResolver implements ParameterResolver {

    private final Injector injector;

    public GuiceParameterResolver(Injector injector) {
        this.injector = injector;
    }

    public GuiceParameterResolver(GuiceComponentProvider componentProvider) {
        this.injector = componentProvider.getInjector();
    }

    @Override
    public Object resolveParameter(ParameterInfo parameter) {

        // We ask for an existing binding so Guice wont create jit bindings for things like Class and String
        // This means that all parameters need to be explicitly bound

        Key<?> key = getKey(parameter);
        Binding<?> existingBinding = injector.getExistingBinding(key);
        return existingBinding != null ? existingBinding.getProvider().get() : UNRESOLVED;
    }

    private Key<?> getKey(ParameterInfo parameter) {
        try {

            // Get TypeLiteral for this parameter
            TypeLiteral<?> declaringType = TypeLiteral.get(parameter.getDeclaringClass());
            List<TypeLiteral<?>> parameterTypes = declaringType.getParameterTypes(parameter.getConstructor());
            TypeLiteral<?> parameterType = parameterTypes.get(parameter.getParameterIndex());

            // Create Key object for this parameter
            Errors errors = new Errors(parameter.getConstructor());
            return Annotations.getKey(
                    parameterType,
                    parameter.getConstructor(),
                    parameter.getParameterAnnotations(),
                    errors);

        } catch (ErrorsException e) {
            throw new MgnlInstantiationException(e.getMessage(), e);
        }
    }
}
