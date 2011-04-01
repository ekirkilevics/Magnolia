/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.objectfactory;

import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;


/**
 * Util class to for manipulating {@link ComponentProvider}s.
 */
public class ComponentProviderUtil {

    public static MutableComponentProvider createChild(ComponentProvider componentProvider){
        if(!(componentProvider instanceof HierarchicalComponentProvider)){
            throw new IllegalArgumentException("Only ComponentProviderUtil of type HierarchicalComponentProvider can be extended");
        }
        final MutableComponentProvider child = ((HierarchicalComponentProvider)componentProvider).createChild();
        child.registerInstance(ComponentProvider.class, child);
        return child;
    }

    public static MutableComponentProvider createChild(ComponentProvider componentProvider, ComponentProviderConfiguration... configurations) {
        final MutableComponentProvider newComponentProvider = createChild(componentProvider);

        for (ComponentProviderConfiguration configuration : configurations) {
            newComponentProvider.configure(configuration);
        }
        return newComponentProvider;

    }
}
