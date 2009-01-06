/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;
import java.text.MessageFormat;

/**
 * A tasks that offers helper methods to check on certain properties.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class PropertyValuesTask extends AbstractTask {

    public PropertyValuesTask(String name, String description) {
        super(name, description);
    }

    /**
     * Checks that the given String property has the expected value. Changes it if so, logs otherwise.
     */
    protected void checkAndModifyPropertyValue(InstallContext ctx, Content node, String propertyName, String expectedCurrentValue, String newValue) throws RepositoryException {
        final NodeData prop = node.getNodeData(propertyName);
        final String currentvalue = prop.getString();
        if (prop.isExist() && currentvalue.equals(expectedCurrentValue)) {
            prop.setValue(newValue);
        } else {
            final String msg = format("Property \"{0}\" was expected to exist at {1} with value \"{2}\" but {3,choice,0#does not exist|1#has the value \"{4}\" instead}.",
                    propertyName, node.getHandle(), expectedCurrentValue, new Integer(prop.isExist() ? 1 : 0), currentvalue);
            ctx.warn(msg);
        }
    }

    protected void checkOrCreateProperty(InstallContext ctx, Content node, String propertyName, String expectedValue) throws RepositoryException {
        if (node.hasNodeData(propertyName)) {
            final NodeData prop = node.getNodeData(propertyName);
            final String currentvalue = prop.getString();
            if (!currentvalue.equals(expectedValue)) {
                final String msg = format("Property \"{0}\" was expected to exist at {1} with value \"{2}\" but {3,choice,0#does not exist|1#has the value \"{4}\" instead}.",
                        propertyName, node.getHandle(), expectedValue, new Integer(prop.isExist() ? 1 : 0), currentvalue);
                ctx.warn(msg);
            }
        } else {
            node.createNodeData(propertyName, expectedValue);
        }
    }

    /**
     * Checks that the given property does not exist and creates it with the given value, logs otherwise.
     */
    protected void newProperty(InstallContext ctx, Content node, String propertyName, String value) throws RepositoryException {
        newProperty(ctx, node, propertyName, value, true);
    }

    protected void newProperty(InstallContext ctx, Content node, String propertyName, String value, boolean log) throws RepositoryException {
        final NodeData prop = node.getNodeData(propertyName);
        if (!prop.isExist()) {
            node.createNodeData(propertyName, value);
        } else if (log) {
            final String msg = format("Property \"{0}\" was expected not to exist at {1}, but exists with value \"{2}\" and was going to be created with value \"{3}\".",
                    propertyName, node.getHandle(), prop.getString(), value);
            ctx.warn(msg);
        }
    }

    // TODO move this to the InstallContext interface ?
    protected String format(String pattern, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
        return MessageFormat.format(pattern, new Object[]{arg0, arg1, arg2, arg3, arg4});
    }

    protected String format(String pattern, Object arg0, Object arg1, Object arg2, Object arg3) {
        return format(pattern, arg0, arg1, arg2, arg3, null);
    }

    protected String format(String pattern, Object arg0, Object arg1, Object arg2) {
        return format(pattern, arg0, arg1, arg2, null);
    }
}
