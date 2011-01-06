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
package info.magnolia.module.cache.ehcache;

import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import java.util.Map;

/**
 * A Content2BeanTransformer for net.ehcache.config.CacheConfiguration, because
 * it does not respect javabeans conventions for setting the MemoryStoreEvictionPolicy.
 * (getter returns a MemoryStoreEvictionPolicy, setter takes a String as argument)
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheConfigurationTransformer extends Content2BeanTransformerImpl {
    public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map values) {
        final TypeDescriptor typeDescriptor = descriptor.getType();
        final String propertyName = descriptor.getName();
        if ("memoryStoreEvictionPolicyFromObject".equals(propertyName)) {
            // skip this property - it's only an illegitimate setter and we're taking care of this property just below. 
            return;
        } else if (typeDescriptor.getType().equals(MemoryStoreEvictionPolicy.class)) {
            final String memoryStoreEvictionPolicyName = (String) values.get(propertyName);
            // final MemoryStoreEvictionPolicy policy = MemoryStoreEvictionPolicy.fromString(memoryStoreEvictionPolicyName);
            final CacheConfiguration cfg = (CacheConfiguration) state.getCurrentBean();
            cfg.setMemoryStoreEvictionPolicy(memoryStoreEvictionPolicyName);
        } else {
            super.setProperty(state, descriptor, values);
        }
    }
}
