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
package info.magnolia.module.admininterface;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.security.AccessDeniedException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionUtil {

    private static final Logger log = LoggerFactory.getLogger(VersionUtil.class);

    public static Set<Version> getSortedNotDeletedVersions(Content content) throws RepositoryException,
    PathNotFoundException, AccessDeniedException {
        final Iterator<Version> iterator = content.getAllVersions();
        return getSortedVersions(iterator, content);
    }

    public static Set<Version> getSortedNotDeletedVersionsBefore(Content content, String versionName) throws RepositoryException {
        final Iterator<Version> iterator = IteratorUtils.arrayIterator(content.getVersionedContent(versionName).getPredecessors());
        return getSortedVersions(iterator, content);
    }

    private static Set<Version> getSortedVersions(Iterator<Version> iterator, Content versionBase) throws RepositoryException {
        Set<Version> allVersions = new TreeSet<Version>(new Comparator<Version>() {
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

        if (iterator == null) {
            return allVersions;
        }

        while (iterator.hasNext()) {
            Version version = iterator.next();
            ContentVersion versionedContent = versionBase.getVersionedContent(version);
            if (versionedContent.hasContent("MetaData") && versionedContent.getContent("MetaData").hasNodeData("mgnl:template") && "mgnlDeleted".equals(versionedContent.getContent("MetaData").getNodeData("mgnl:template").getString())) {
                // do not care about deleted content versions
                log.debug("Found multiple deletion attempts for {}", versionedContent.getHandle());
            } else {
                allVersions.add(version);
            }
        }

        return allVersions;
    }

}
