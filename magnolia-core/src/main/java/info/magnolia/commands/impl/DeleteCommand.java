/**
 * This file Copyright (c) 2003-2013 Magnolia International
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
package info.magnolia.commands.impl;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The command to delete one node.
 * 
 * @author jackie
 */
public class DeleteCommand extends BaseRepositoryCommand {

    private static Logger log = LoggerFactory.getLogger(DeleteCommand.class);

    @Override
    public boolean execute(Context ctx) {
        try {
            String parentPath = StringUtils.substringBeforeLast(getPath(), "/");
            String label = StringUtils.substringAfterLast(getPath(), "/");

            Content parentNode = MgnlContext.getHierarchyManager(this.getRepository()).getContent(parentPath);
            parentNode.delete(label);
            parentNode.save();
        }
        catch (Exception e) {
            AlertUtil.setException("cannot do delete", e, ctx);
            return false;
        }
        return true;
    }

}
