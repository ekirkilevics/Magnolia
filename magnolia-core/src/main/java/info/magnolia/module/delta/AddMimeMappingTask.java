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
package info.magnolia.module.delta;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;


/**
 * Task that adds a mime mapping to <code>server/MIMIMapping</code>.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class AddMimeMappingTask extends AbstractRepositoryTask {

    /**
     * Extension (without the <code>.</code>)
     */
    private String extension;

    /**
     * mime type.
     */
    private String mime;

    /**
     * Icon path.
     */
    private String icon;

    /**
     * @param extension Extension (without the <code>.</code>)
     * @param mime mime type.
     * @param icon Icon path.
     */
    public AddMimeMappingTask(String extension, String mime, String icon) {
        super("Add mime mapping task", "Adds a MIME mapping for the " + extension + " extension");
        this.extension = extension;
        this.mime = mime;
        this.icon = icon;
    }

    /**
     * {@inheritDoc}
     */
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        HierarchyManager hm = ctx.getHierarchyManager(ContentRepository.CONFIG);
        Content mimeNode = hm.getContent("/server/MIMEMapping");

        if (!mimeNode.hasContent(extension)) {
            Content m = mimeNode.createContent(extension, ItemType.CONTENTNODE);
            m.createNodeData("mime-type").setValue(mime);
            m.createNodeData("icon").setValue(icon);
        }
    }

}
