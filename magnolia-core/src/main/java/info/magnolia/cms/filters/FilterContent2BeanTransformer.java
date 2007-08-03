/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
 * A special content2bean transformer for filters.
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
        if (bean instanceof MagnoliaMainFilter) {
            for (Iterator iter = values.values().iterator(); iter.hasNext();) {
                Object value = iter.next();
                if (value instanceof MagnoliaFilter) {
                    ((MagnoliaMainFilter) bean).addFilter((MagnoliaFilter) value);
                }
            }
        }
    }

    /**
     * The default class to use is MagnoliaMainFilter
     */
    protected TypeDescriptor onResolveClass(TransformationState state) {
        if (state.getCurrentContent().isNodeType(ItemType.CONTENT.getSystemName())) {
            return this.getTypeMapping().getTypeDescriptor(MagnoliaMainFilter.class);
        }
        return super.onResolveClass(state);
    }
}
