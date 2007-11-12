/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.module.ServletDefinition;
import info.magnolia.cms.module.ServletParameterDefinition;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;

import java.util.Iterator;

import javax.jcr.RepositoryException;


/**
 * @author philipp
 * @version $Id$
 */
public class RegisterServletTask extends AbstractTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterServletTask.class);

    private static final String DEFAULT_SERVLET_FILTER_PATH = "/server/filters/servlets";
    private final ServletDefinition servletDefinition;

    public RegisterServletTask(ServletDefinition servletDefinition) {
        super("Servlet " + servletDefinition.getName(), "Registers servlet" + servletDefinition.getName() + " (" + servletDefinition.getComment() + ")");
        this.servletDefinition = servletDefinition;
    }

    public void execute(InstallContext installContext) throws TaskExecutionException {
        log.debug("Registering servlet " + servletDefinition.getName() + " in servlet filter.");

        final String servletFilterPath = DEFAULT_SERVLET_FILTER_PATH;

        try {
            final Content servletNode = installContext.getConfigHierarchyManager().createContent(servletFilterPath, servletDefinition.getName(), ItemType.CONTENTNODE.getSystemName());
            NodeDataUtil.getOrCreateAndSet(servletNode, "class", "info.magnolia.cms.filters.ServletDispatchingFilter");
            NodeDataUtil.getOrCreateAndSet(servletNode, "enabled", true);
            NodeDataUtil.getOrCreateAndSet(servletNode, "servletClass", servletDefinition.getClassName());
            NodeDataUtil.getOrCreateAndSet(servletNode, "servletName", servletDefinition.getName());
            NodeDataUtil.getOrCreateAndSet(servletNode, "comment", servletDefinition.getComment());

            final Content mappingsNode = servletNode.createContent("mappings", ItemType.CONTENTNODE);
            for (Iterator iter = servletDefinition.getMappings().iterator(); iter.hasNext();) {
                final String pattern = (String) iter.next();
                String mappingNodeName = Path.getUniqueLabel(mappingsNode, Path.getValidatedLabel(pattern));
                final Content mappingNode = mappingsNode.createContent(mappingNodeName, ItemType.CONTENTNODE);
                NodeDataUtil.getOrCreateAndSet(mappingNode, "pattern", pattern);
            }

            final Content parametersNode = servletNode.createContent("parameters", ItemType.CONTENTNODE);
            for (Iterator iter = servletDefinition.getParams().iterator(); iter.hasNext();) {
                final ServletParameterDefinition parameter = (ServletParameterDefinition) iter.next();

                NodeDataUtil.getOrCreateAndSet(parametersNode, parameter.getName(), parameter.getValue());
            }
        }
        catch (RepositoryException e) {
            log.error("Cannot create servlet node in servlet filter.", e);
        }
    }

    public ServletDefinition getServletDefinition() {
        return this.servletDefinition;
    }

}
