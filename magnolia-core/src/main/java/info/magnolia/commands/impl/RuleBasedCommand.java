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

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.Rule;
import org.apache.commons.lang.StringUtils;

/**
 * Base class for rule based commands.
 * 
 * @author Sameer Charles $Id$
 */
public abstract class RuleBasedCommand extends BaseRepositoryCommand {

    /**
     * You can pass a rule to the command (optional).
     */
    public static final String ATTRIBUTE_RULE = "rule";

    /**
     * All subnodes of this types are activated imediately (without using the
     * recursion).
     */
    private String itemTypes = ItemType.CONTENTNODE.getSystemName();

    private Rule rule;

    public Rule getRule() {
        rule = new Rule();
        String[] nodeTypes = StringUtils.split(this.getItemTypes(), " ,");
        for (int i = 0; i < nodeTypes.length; i++) {
            String nodeType = nodeTypes[i];
            rule.addAllowType(nodeType);
        }

        // magnolia resource and metadata must always be included
        rule.addAllowType(ItemType.NT_METADATA);
        rule.addAllowType(ItemType.NT_RESOURCE);
        return rule;
    }

    public String getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(String nodeTypes) {
        this.itemTypes = nodeTypes;
    }

    /**
     * @param rule the Rule to set
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public void release() {
        super.release();
        this.itemTypes = ItemType.CONTENTNODE.getSystemName();
        this.rule = null;
    }
}
