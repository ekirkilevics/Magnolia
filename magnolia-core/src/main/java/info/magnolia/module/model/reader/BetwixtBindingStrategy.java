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
package info.magnolia.module.model.reader;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.betwixt.strategy.TypeBindingStrategy;

import java.util.HashSet;
import java.util.Set;

/**
 * A TypeBindingStrategy for Betwixt where we can register our own {@link Converter}s. When such a Converter
 * is registered, betwixt will attempt to convert the xml element's value from String to object.
 * <strong>Beware</strong>, these converters are registered globally, thanks to the singleton-esque nature
 * of BeanUtils. If a cleaner solution is found, it will be much welcome.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class BetwixtBindingStrategy extends TypeBindingStrategy {
    private final TypeBindingStrategy delegate = new TypeBindingStrategy.Default();
    private final Set<Class<?>> convertedClasses;

    BetwixtBindingStrategy() {
        this.convertedClasses = new HashSet<Class<?>>();
    }

    void registerConverter(Class<?> clazz, Converter converter) {
        convertedClasses.add(clazz);
        // yuck, configuring the beanutils singleton - should be OK though, it's unlikely a 3rd party would use our model classes.
        ConvertUtils.register(converter, clazz);
    }

    @Override
    public TypeBindingStrategy.BindingType bindingType(Class type) {
        if (convertedClasses.contains(type)) {
            return TypeBindingStrategy.BindingType.PRIMITIVE;
        }
        return delegate.bindingType(type);
    }
}
