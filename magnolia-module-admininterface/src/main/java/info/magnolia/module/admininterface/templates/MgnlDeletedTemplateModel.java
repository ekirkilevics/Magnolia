/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admininterface.templates;

import java.util.Calendar;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentWrapper;
import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.module.templating.RenderingModel;
import info.magnolia.module.templating.RenderingModelImpl;

public class MgnlDeletedTemplateModel extends RenderingModelImpl<RenderableDefinition> {

    private static final Logger log = LoggerFactory.getLogger(MgnlDeletedTemplateModel.class);

    public MgnlDeletedTemplateModel(Content content, RenderableDefinition definition, RenderingModel parent) {
        super(content, definition, parent);
    }

    public String getLastVersion() {
        Collection<Version> allVersions = new TreeSet<Version>(new Comparator<Version>() {
            public int compare(Version o1, Version o2) {
                // nulls are not allowed
                Calendar c1, c2;
                try {
                    c1 = o1.getCreated();
                } catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
                    return 1;
                }
                try {
                    c2 = o2.getCreated();
                } catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
                    return -1;
                }

                // reverse order!!!
                return c2.before(c1) ? -1 : c2.after(c1) ? 1 : 0;
            }
        });

        Iterator<Version> iterator = null;
        Content maybeVersion = getContent();
        Content content = getContent();
        try {
            try {
                iterator = getContent().getAllVersions();
            } catch (AccessDeniedException e) {
                // Either change the ContentVersion API to delegate call to getAllVersions() to the underlying real node or do this. There is no save way of finding the node is a version since it can be wrapped by multiple different wrappers
                while (maybeVersion != null && maybeVersion instanceof ContentWrapper) {
                    maybeVersion = ((ContentWrapper) maybeVersion).getWrappedContent();
                }
            }
            if (maybeVersion instanceof ContentVersion) {
                iterator = IteratorUtils.arrayIterator(((ContentVersion) maybeVersion).getPredecessors());
                content = maybeVersion.getHierarchyManager().getContent(maybeVersion.getHandle());

            }

            if (iterator != null) {
                while (iterator.hasNext()) {
                    Version version = iterator.next();
                    ContentVersion versionedContent = content.getVersionedContent(version);
                    if (versionedContent.hasContent("MetaData") && versionedContent.getContent("MetaData").hasNodeData("mgnl:template") && "mgnlDeleted".equals(versionedContent.getContent("MetaData").getNodeData("mgnl:template").getString())) {
                        // do not care about deleted content versions
                        log.debug("Found multiple deletion attempts for {}", versionedContent.getHandle());
                    } else {
                        allVersions.add(version);
                    }
                }
            }
            if (allVersions.isEmpty()) {
                return null;
            }
            return allVersions.iterator().next().getName();
        } catch (UnsupportedRepositoryOperationException e) {
            // if this node doesn't support versioning it can't be undeleted ... nor can we display previous version of the content
            log.error("Failed to retrieve version history for " + getContent().getHandle() + ". This node doesn't support versioning", e);
        } catch (RepositoryException e) {
            log.error("Failed to retrieve version history for " + getContent().getHandle() + "", e);
        }
        return null;
    }

    public boolean hasChildren() {
        return getContent().hasChildren(ItemType.CONTENT.getSystemName());
    }

    public String getDeletionAuthor() {
        return getContent().getNodeData("mgnl:deletedBy").getString();
    }

    public String getDeletionDate() {
        return getContent().getNodeData("mgnl:deletedOn").getString();
    }
}
