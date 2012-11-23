/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.link;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;

/**
 * Transformer creating links relative to provided path.
 *
 * @version $Id$
 */
public class RelativePathTransformer extends AbsolutePathTransformer {

    protected String absolutSourcePath;

    public void setAbsolutSourcePath(Node sourceNode){
        try {
            Link link = new Link(sourceNode);
            link.setRepository(sourceNode.getSession().getWorkspace().getName());
            link.setExtension("html");
            absolutSourcePath = super.transform(link);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAbsolutSourcePath(String absolutSourcePath){
        this.absolutSourcePath = absolutSourcePath;
    }

    public RelativePathTransformer(){
    }

    public RelativePathTransformer(Content sourcePage, boolean useURI2RepositoryMapping, boolean useI18N) {
        this(sourcePage.getJCRNode(), useURI2RepositoryMapping, useI18N);
    }

    public RelativePathTransformer(Node sourcePage, boolean useURI2RepositoryMapping, boolean useI18N) {
        super(false, useURI2RepositoryMapping, useI18N);
        try {
            Link link = new Link(sourcePage);
            link.setRepository(sourcePage.getSession().getWorkspace().getName());
            link.setExtension("html");
            absolutSourcePath = super.transform(link);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public RelativePathTransformer(String absoluteSourcePath, boolean useURI2RepositoryMapping, boolean useI18N) {
        super(false, useURI2RepositoryMapping, useI18N);
        this.absolutSourcePath = absoluteSourcePath;
    }

    @Override
    public String transform(Link target) {
        String link = super.transform(target);
        return LinkUtil.makePathRelative(absolutSourcePath, link);
    }
}
