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
package info.magnolia.rendering.template.registry;

import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.template.TemplateDefinition;

/**
 * Default implementation of {@link TemplateAvailability}.
 *
 * @version $Id$
 */
@Singleton
public class DefaultTemplateAvailability implements TemplateAvailability {

    @Override
    public boolean isAvailable(Node content, TemplateDefinition templateDefinition) {
        return hasReadAccess(content);
    }

    protected boolean hasReadAccess(Node content) {
        try {
            // should not fact that we are able to get path already show that we can read this node???
            // ... unless of course this "content" was created with system session ... so make sure we check using user session and not the node session
            return PermissionUtil.isGranted(
                    MgnlContext.getJCRSession(content.getSession().getWorkspace().getName()),
                    content.getPath(),
                    Session.ACTION_READ);
        } catch (RepositoryException e) {
            return false;
        }
    }
}
