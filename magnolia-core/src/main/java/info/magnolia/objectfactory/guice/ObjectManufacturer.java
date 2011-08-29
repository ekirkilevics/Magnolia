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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import info.magnolia.objectfactory.MgnlInstantiationException;


/**
 * Creates objects using both direct arguments and finding suitable arguments from a Guice injector. Will first look
 * for a constructor annotated with &#64;Inject, it can be public, private, protected or package private. It will then
 * look at public constructors and use the greediest. The greediest constructor is the one that has the most number of
 * arguments that it can fulfill. If there is more than one constructor that qualifies as greediest it is unspecified
 * which one will be used.
 *
 * @version $Id$
 */
public class ObjectManufacturer {

    protected static final Object NOTHING = new Object();

    @Inject
    private Injector injector;

    public ObjectManufacturer(Injector injector) {
        this.injector = injector;
    }

    public Object manufacture(Class<?> clazz, Object... extraCandidates) {

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        Constructor<?> selectedConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                if (selectedConstructor != null) {
                    throw new MgnlInstantiationException("Only one constructor can use @Inject [" + clazz + "]");
                }
                selectedConstructor = constructor;
            }
        }
        if (selectedConstructor != null) {
            selectedConstructor.setAccessible(true);
            Object[] parameters = resolveParameters(selectedConstructor, extraCandidates);
            if (parameters == null) {
                throw new MgnlInstantiationException("Unable to resolve parameters for constructor " + selectedConstructor);
            }
            return invoke(selectedConstructor, parameters);
        }

        // Find greediest satisfiable constructor
        int bestScore = -1;
        Object[] bestParameters = null;
        for (Constructor<?> constructor : constructors) {
            if (!Modifier.isPublic(constructor.getModifiers())) {
                continue;
            }
            int score = constructor.getParameterTypes().length;
            if (score < bestScore) {
                continue;
            }
            Object[] parameters = resolveParameters(constructor, extraCandidates);
            if (parameters == null) {
                continue;
            }
            selectedConstructor = constructor;
            bestScore = score;
            bestParameters = parameters;
        }
        if (selectedConstructor != null) {
            return invoke(selectedConstructor, bestParameters);
        }
        throw new MgnlInstantiationException("No suitable constructor found for class [" + clazz + "]");
    }

    private Object invoke(Constructor constructor, Object[] parameters) {
        try {
            Object instance = constructor.newInstance(parameters);
            injector.injectMembers(instance);
            return instance;
        } catch (InstantiationException e) {
            throw new MgnlInstantiationException(e);
        } catch (IllegalAccessException e) {
            throw new MgnlInstantiationException(e);
        } catch (InvocationTargetException e) {
            throw new MgnlInstantiationException(e);
        }
    }

    private Object[] resolveParameters(Constructor<?> constructor, Object[] extraCandidates) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();

        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {

            Class parameterType = parameterTypes[i];
            Type genericParameterType = genericParameterTypes[i];

            Object parameter = findExtraCandidate(parameterType, genericParameterType, extraCandidates);

            if (parameter == NOTHING) {
                parameter = getParameterFromInjector(parameterType, genericParameterType);
            }

            if (parameter == NOTHING) {
                return null;
            }
            parameters[i] = parameter;
        }
        return parameters;
    }

    private Object getParameterFromInjector(Class<?> parameterType, Type genericParameterType) {

        // We ask for an existing binding so Guice wont create jit bindings for things like Class and String
        // This means that all parameters need to be explicitly bound

        // If the parameter is javax.inject.Provider<T> we will look for a provider of T instead
        if (genericParameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
            if (parameterizedType.getRawType() == javax.inject.Provider.class) {
                Type actualType = parameterizedType.getActualTypeArguments()[0];
                Binding<?> existingBinding = injector.getExistingBinding(Key.get(actualType));
                return existingBinding != null ? existingBinding.getProvider() : NOTHING;
            }
        }
        Key<?> key = Key.get(parameterType);
        Binding<?> existingBinding = injector.getExistingBinding(key);
        return existingBinding != null ? existingBinding.getProvider().get() : NOTHING;
    }

    private Object findExtraCandidate(Class<?> parameterType, Type genericParameterType, Object[] extraCandidates) {
        // If the parameter is javax.inject.Provider<T> we will look for T instead and return a provider for it
        if (genericParameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
            if (parameterizedType.getRawType() == javax.inject.Provider.class) {
                Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                for (final Object extraCandidate : extraCandidates) {
                    if (actualType.isAssignableFrom(extraCandidate.getClass())) {
                        return new Provider() {
                            @Override
                            public Object get() {
                                return extraCandidate;
                            }
                        };
                    }
                }
                return NOTHING;
            }
        }
        for (Object extraCandidate : extraCandidates) {
            if (parameterType.isAssignableFrom(extraCandidate.getClass())) {
                return extraCandidate;
            }
        }
        return NOTHING;
    }
}
