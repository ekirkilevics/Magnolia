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
import javax.inject.Inject;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;


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
                    throw new RuntimeException("Only one constructor can use @Inject [" + clazz + "]");
                }
                selectedConstructor = constructor;
            }
        }
        if (selectedConstructor != null) {
            selectedConstructor.setAccessible(true);
            return invoke(selectedConstructor, resolveParameters(clazz, selectedConstructor.getParameterTypes(), extraCandidates));
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
            Object[] parameters = resolveParameters(clazz, constructor.getParameterTypes(), extraCandidates);
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
        throw new RuntimeException("No suitable constructor found for class [" + clazz + "]");
    }

    private Object invoke(Constructor constructor, Object[] parameters) {
        try {
            Object instance = constructor.newInstance(resolveParameters(constructor.getDeclaringClass(), constructor.getParameterTypes(), parameters));
            injector.injectMembers(instance);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] resolveParameters(Class<?> clazz, Class[] parameterTypes, Object[] extraCandidates) {
        // FIXME we might be creating objects with guice here only to throw them away
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object parameter = resolveParameter(parameterTypes[i], extraCandidates);
            if (parameter == NOTHING) {
                return null;
            }
            parameters[i] = parameter;
        }
        return parameters;
    }

    private Object resolveParameter(Class<?> parameterType, Object[] extraCandidates) {
        Object parameter = findExtraCandidate(parameterType, extraCandidates);
        if (parameter != NOTHING) {
            return parameter;
        }
        try {
            // FIXME should maybe check if there's an explicit binding for this type
            return injector.getInstance(parameterType);
        } catch (ConfigurationException e) {
            return NOTHING;
        }
    }

    private Object findExtraCandidate(Class<?> parameterType, Object[] extraCandidates) {
        for (Object extraCandidate : extraCandidates) {
            if (parameterType.isAssignableFrom(extraCandidate.getClass())) {
                return extraCandidate;
            }
        }
        return NOTHING;
    }
}
