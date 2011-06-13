/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.renderer.registry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.jcr.wrapper.LazyNodeWrapper;
import info.magnolia.templating.renderer.Renderer;

/**
 * RendererProvider that instantiates a dialog from a configuration node.
 *
 * @version $Id$
 */
public class ConfiguredRendererProvider implements RendererProvider {

    private Node configNode;

    public ConfiguredRendererProvider(Node configNode) throws RepositoryException {
        // session that opened provided content might not be alive by the time we need to use this
        this.configNode = new LazyNodeWrapper(configNode);
    }

    @Override
    public Renderer getRenderer() throws RendererRegistrationException {
        // FIXME we should not constantly transform the object. the manager re-registers the providers upon changes
        try {
            Content content = new DefaultContent(configNode, null);
            return (Renderer) Content2BeanUtil.toBean(content, true, Renderer.class);
        } catch (Content2BeanException e) {
            throw new RendererRegistrationException(e);
        } catch (AccessDeniedException e) {
            throw new RendererRegistrationException(e);
        } catch (RepositoryException e) {
            throw new RendererRegistrationException(e);
        }
    }
}
