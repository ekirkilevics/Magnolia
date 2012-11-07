/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.beanmerger;

import info.magnolia.cms.core.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base implementation of interface BeanMerge.
 *
 * @version $Id$
 */
public abstract class BeanMergerBase implements BeanMerger {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object merge(List sources) {
        CollectionUtils.filter(sources, NotNullPredicate.INSTANCE);
        if (sources.isEmpty()) {
            return null;
        }

        if (sources.size() == 1 || isSimpleType(sources.get(0).getClass())) {
            return sources.get(0);
        }

        if (sources.get(0) instanceof Collection) {
            return mergeCollections(sources);
        }

        if (sources.get(0) instanceof Map) {
            return mergeMaps(sources);
        }

        return mergeBean(sources);
    }

    protected abstract Object mergeBean(List sources);

    protected Map mergeMaps(List<Map> sources) {
        Map mergedMap;
        try {
            mergedMap = sources.get(0).getClass().newInstance();

            for (Map map : sources) {
                for (Object key : map.keySet()) {
                    List values = new ArrayList();
                    for (Map map2 : sources) {
                        if (map2.containsKey(key)) {
                            values.add(map2.get(key));
                        }
                    }
                    mergedMap.put(key, merge(values));
                }
            }
            return mergedMap;
        }
        catch (Exception e) {
            log.error("", e);
            return sources.get(0);
        }
    }

    protected Collection mergeCollections(List<Collection> sources) {
        try {
            Collection mergedCol = sources.get(0).getClass().newInstance();
            for (Collection col : sources) {
                mergedCol.addAll(col);
            }
            return mergedCol;
        }
        catch (Exception e) {
            log.error("", e);
            return sources.get(0);
        }
    }

    protected boolean isSimpleType(Class type) {
        return type.isPrimitive() ||
            type == Boolean.class ||
            type == Byte.class ||
            type == Character.class ||
            type == Short.class ||
            type == Integer.class ||
            type == Long.class ||
            type == Float.class ||
            type == Double.class ||
            type == Void.class ||
            // well, at least property wise
            type == String.class ||
            type == Class.class ||
            // not mergable
            Content.class.isAssignableFrom(type) ||
            Node.class.isAssignableFrom(type);
    }

}
