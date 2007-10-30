/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.setup.for3_1;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.TaskExecutionException;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class IPConfigRulesUpdate extends AllChildrenNodesOperation {

    public IPConfigRulesUpdate() {
        super("IPConfig rules changed", "Updates the existing ip access rules to match the new configuration structure.",
                ContentRepository.CONFIG, "/server/IPConfig");
    }

    /**
     *  old configuration:
     *  rule-name (p) IP
     *            (n) Access
     *                (n) 0001
     *                    (p) Method = GET
     *                (n) 0002
     *                    (p) Method = POST
     *
     *  new configuration:
     *  rule-name (p) IP = *
     *            (p) methods = GET,POST
     */
    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
        if (node.hasContent("Access")) {
            final Content accessNode = node.getContent("Access");
            final Set methods = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            final Iterator it = accessNode.getChildren().iterator();
            while (it.hasNext()) {
                final Content methodNode = (Content) it.next();
                if (methodNode.hasNodeData("Method")) {
                    final String method = methodNode.getNodeData("Method").getString();
                    methods.add(method.toUpperCase());
                }
            }
            final String methodsStr = StringUtils.join(methods, ',');
            node.createNodeData("methods", methodsStr);
            accessNode.delete();
        }
    }

}
