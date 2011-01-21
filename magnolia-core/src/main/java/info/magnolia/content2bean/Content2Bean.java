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
package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;

/**
 * Entry point for content2bean. The implementation is responsible for instantiating the proper bean and populating it, using the appropriate {@link Content2BeanTransformer}.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface Content2Bean {
    Object toBean(Content node) throws Content2BeanException;

    // TODO - Content2BeanUtil seems to think the default behavior is to not recurse, but then again, all or most usage points do recurse. Maybe it's a transformer's job to stop recursion ?
    // TODO implement if necessary - void transform(Object bean, Content data) throws Content2BeanException; - this is a replacement for Content2BeanProcessor.setProperties()

}
