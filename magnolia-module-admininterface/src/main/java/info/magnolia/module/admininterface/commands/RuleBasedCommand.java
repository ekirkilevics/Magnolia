/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.Rule;
import org.apache.commons.lang.StringUtils;

/**
 * @author Sameer Charles
 * $Id$
 */
public abstract class RuleBasedCommand extends BaseRepositoryCommand {

    /**
     * You can pass a rule to the command (optional)
     */
    public static final String ATTRIBUTE_RULE = "rule";

    /**
     * All subnodes of this types are activated imediately (without using the recursion)
     */
    private String itemTypes = ItemType.CONTENTNODE.getSystemName();

    private Rule rule;

    public Rule getRule() {
        // lazy bound but only if this is a clone
        if (rule == null && isClone()) {
            rule = new Rule();
            String[] nodeTypes = StringUtils.split(this.getItemTypes(), " ,");
            for (int i = 0; i < nodeTypes.length; i++) {
                String nodeType = nodeTypes[i];
                rule.addAllowType(nodeType);
            }

            // magnolia resource and metadata must always be included
            rule.addAllowType(ItemType.NT_METADATA);
            rule.addAllowType(ItemType.NT_RESOURCE);
        }
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
    
}
