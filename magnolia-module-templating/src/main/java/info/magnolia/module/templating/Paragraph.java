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
package info.magnolia.module.templating;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;

/**
 * @author Sameer Charles
 */
public class Paragraph extends AbstractRenderable {

    private final static Logger log = LoggerFactory.getLogger(Paragraph.class);

    /**
     * @deprecated since 4.0 use the type property
     */
    public String getTemplateType(){
        DeprecationUtil.isDeprecated("The property templateType in paragraph definitions has changed to type" );
        return getType();
    }

    /**
     * @deprecated since 4.0 use the type property
     */
    public void setTemplateType(String type){
        DeprecationUtil.isDeprecated("The property templateType in paragraph definitions has changed to type" );
        setType(type);
    }

    /**
     * @deprecated since 4.0 use the dialog property
     */
    public String getDialogPath(String path){
        String msg = "The property dialogPath in paragraph definitions has been deprecated, use the dialog property instead";
        DeprecationUtil.isDeprecated(msg);
        throw new UnsupportedOperationException(msg);
    }

    /**
     * @deprecated since 4.0 use the dialog property
     */
    public void setDialogPath(String path){
        DeprecationUtil.isDeprecated("The property dialogPath in paragraph definitions has been deprecated, use the dialog property instead");
        Content node;
        try {
            node = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG).getContent(path);
            String name = NodeDataUtil.getString(node, "name", node.getName());
            setDialog(name);
        }
        catch (RepositoryException e) {
            log.error("Can't determine dialog name using the path [" + path + "]", e);
        }
    }

}