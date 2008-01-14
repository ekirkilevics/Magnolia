/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.filters;

import info.magnolia.cms.core.ItemType;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

import java.util.Iterator;
import java.util.Map;

/**
 * A special content2bean transformer for filters. For the CompositeFilter we omit the extra filters node.
 *
 * @author Philipp Bracher
 * @version $Revision: $ ($Author: $)
 */
class FilterContent2BeanTransformer extends Content2BeanTransformerImpl {

    FilterContent2BeanTransformer() {
    }

    public void initBean(TransformationState state, Map values) throws Content2BeanException {
        super.initBean(state, values);

        Object bean = state.getCurrentBean();
        // we do not have a filters subnode again
        if (bean instanceof CompositeFilter) {
            for (Iterator iter = values.values().iterator(); iter.hasNext();) {
                Object value = iter.next();
                if (value instanceof MgnlFilter) {
                    ((CompositeFilter) bean).addFilter((MgnlFilter) value);
                }
            }
        }
    }

    /**
     * The default class to use is MagnoliaMainFilter
     */
    protected TypeDescriptor onResolveClass(TransformationState state) {
        if (state.getCurrentContent().isNodeType(ItemType.CONTENT.getSystemName())) {
            return this.getTypeMapping().getTypeDescriptor(CompositeFilter.class);
        }
        return super.onResolveClass(state);
    }
}
