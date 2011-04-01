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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;

/**
 * Builds a component configured in the repository by using content2bean.
 * @param <T> the components type
 */
final class ConfiguredComponentFactory<T> implements ComponentFactory<T> {

    private final String path;

    private final String workspace;

    private ComponentProvider componentProvider;

    public ConfiguredComponentFactory(String path, String workspace, ComponentProvider componentProvider) {
        this.path = path;
        this.workspace = workspace;
        this.componentProvider = componentProvider;
    }

    public T newInstance() {
        final Content node = ContentUtil.getContent(workspace, path);
        if(node == null){
            throw new NullPointerException("Can't find a the node [" + workspace + ":" + path + "] to create an instance");
        }
        try {
            return (T) Content2BeanUtil.toBean(node, true, componentProvider);
        }
        catch (Content2BeanException e) {
            throw new RuntimeException(e);
        }
    }
}