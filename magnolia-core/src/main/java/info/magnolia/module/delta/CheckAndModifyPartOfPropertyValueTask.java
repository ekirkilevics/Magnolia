/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.module.InstallContext;

import javax.jcr.Node;
import javax.jcr.RepositoryException;


/**
 * If the property (Property) contains the expected string, then it will be replaced by the new string.
 * 
 * @version $Id$
 */
public class CheckAndModifyPartOfPropertyValueTask extends PropertyValuesTask {
    private final String workspaceName;
    private final String nodePath;
    private final String propertyName;
    private final String expectedCurrentValue;
    private final String newValue;

    public CheckAndModifyPartOfPropertyValueTask(String name, String description, String workspaceName, String nodePath, String propertyName, String expectedCurrentValue, String newValue) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.nodePath = nodePath;
        this.propertyName = propertyName;
        this.expectedCurrentValue = expectedCurrentValue;
        this.newValue = newValue;
    }
    @Override
    public void execute(InstallContext ctx) {
        try {
            final Node node = SessionUtil.getNode(workspaceName, nodePath);
            checkAndModifyPartOfPropertyValue(ctx, node, propertyName, expectedCurrentValue, newValue);
        } catch (RepositoryException e) {
            ctx.error(format("Could not check property {0} of node at {1}.", propertyName, nodePath, null), e);
        }
    }
}
