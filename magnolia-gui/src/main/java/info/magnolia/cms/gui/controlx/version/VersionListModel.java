/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.gui.controlx.version;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.controlx.list.AbstractListModel;
import info.magnolia.cms.gui.query.SearchQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;


/**
 * @author Sameer Charles $Id:VersionListModel.java 2544 2006-04-04 12:47:32Z philipp $
 */
public class VersionListModel extends AbstractListModel {

    /**
     * versioned node
     */
    private Content content;

    /**
     * defines how many versions this model will return in maximum
     */
    private int max = -1;

    /**
     * search query to be used by sub implementation
     */
    protected SearchQuery query;

    /**
     * @param content
     * @param max
     */
    public VersionListModel(Content content, int max) {
        this.content = content;
        this.max = max;
    }

    /**
     * constructor
     */
    public VersionListModel(Content content) {
        this.content = content;
    }

    /**
     * get all versions
     * @return all versions in a collection
     */
    protected Collection getResult() throws RepositoryException {
        List allVersions = new ArrayList();

        VersionHistory versionHistory = this.content.getVersionHistory();
        if (versionHistory == null) {
            return allVersions;
        }

        VersionIterator iterator = versionHistory.getAllVersions();
        // skip root version, its safe here since this list is only meant for presentation
        // and there is always a root version
        iterator.nextVersion();
        while (iterator.hasNext()) {
            Version version = iterator.nextVersion();
            allVersions.add(this.content.getVersionedContent(version));
        }
        if (max != -1) {
            while(allVersions.size() > max){
                allVersions.remove(0);
            }
        }
        return allVersions;
    }

    /**
     * @return the max
     */
    public int getMax() {
        return this.max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * @return the content
     */
    public Content getContent() {
        return this.content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(Content content) {
        this.content = content;
    }
}
